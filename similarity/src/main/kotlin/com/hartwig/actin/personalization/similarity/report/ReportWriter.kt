package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.PersonalizationReportWriterApplication
import com.itextpdf.io.exceptions.IOException
import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.CompressionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.IBlockElement
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.UnitValue
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import kotlin.io.path.Path


private const val FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf"
private const val FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf"

private val PALETTE_BLACK = DeviceRgb(0, 0, 0)
private val PALETTE_BLUE = DeviceRgb(74, 134, 232)
private val PALETTE_MID_GREY = DeviceRgb(101, 106, 108)
private val BORDER = SolidBorder(PALETTE_MID_GREY, 0.25f)
private val METADATA_TITLE = "ACTIN Personalization Report v${PersonalizationReportWriterApplication.VERSION}"
private val PAGE_SIZE = PageSize.A4

private const val METADATA_AUTHOR = "Hartwig ACTIN System"
private const val PAGE_MARGIN_TOP = 100f // Top margin also excludes the chapter title, which is rendered in the header
private const val PAGE_MARGIN_LEFT = 30f
private const val PAGE_MARGIN_RIGHT = 30f
private const val PAGE_MARGIN_BOTTOM = 40f
private const val IMAGE_FILE_EXTENSION = ".png"

class ReportWriter(
    private val writer: PdfWriter, private val outputPath: String, fontRegular: PdfFont, fontBold: PdfFont
) {

    private val chapterTitleStyle = Style().setFont(fontBold).setFontSize(11f).setFontColor(PALETTE_BLACK)
    private val tableTitleStyle = Style().setFont(fontBold).setFontSize(9f).setFontColor(PALETTE_BLUE)
    private val tableHeaderStyle = Style().setFont(fontBold).setFontSize(8f).setFontColor(PALETTE_MID_GREY)
    private val tableBoldContentStyle = Style().setFont(fontBold).setFontSize(8f).setFontColor(PALETTE_BLACK)
    private val tableContentStyle = Style().setFont(fontRegular).setFontSize(8f).setFontColor(PALETTE_BLACK)

    fun writeReport(title: String, tables: List<TableContent>, plots: Map<String, Plot>) {
        val document = document()
        addChapterTitle(document, title)
        writeTables(document, tables)
        addPlots(document, plots)
        document.close()
    }

    private fun writeTables(document: Document, tables: List<TableContent>) {
        val contentWidth = contentWidth()
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f))).setWidth(contentWidth)

        tables.flatMap { tableContent ->
            sequenceOf(
                titleCellWithText(tableContent.title),
                borderlessCellWithElement(renderTable(tableContent)),
                emptyCell()
            )
        }
            .dropLast(1)
            .forEach(table::addCell)
        
        document.add(table)
    }

    private fun renderTable(content: TableContent): Table {
        content.check()
        val table = Table(
            content.sizes?.let { UnitValue.createPointArray(it.toTypedArray().toFloatArray()) }
                ?: UnitValue.createPercentArray(content.headers.size)
        )
        content.headers.map(::headerCellWithText).forEach(table::addHeaderCell)
        content.rows.flatMap { rowElements ->
            rowElements.map(::cellWithTableElement).let { listOf(it.first().setBorderRight(BORDER)) + it.drop(1) }
        }.forEach(table::addCell)
        return table
    }

    private fun addPlots(document: Document, plots: Map<String, Plot>) {
        val outputDir = Path(outputPath).parent
        plots.forEach { (name, plot) ->
            plot.save("$outputDir/$name$IMAGE_FILE_EXTENSION")

            val image = Image(ImageDataFactory.create("$outputDir/lets-plot-images/$name$IMAGE_FILE_EXTENSION"))
            image.setWidth(contentWidth())
            document.add(image)
        }
    }

    private fun contentWidth(): Float {
        return PAGE_SIZE.width - (5 + PAGE_MARGIN_LEFT + PAGE_MARGIN_RIGHT)
    }

    private fun addChapterTitle(document: Document, title: String) {
        document.add(Paragraph(title).addStyle(chapterTitleStyle))
    }

    private fun document(): Document {
        val pdf = PdfDocument(writer)
        pdf.defaultPageSize = PageSize.A4
        pdf.documentInfo.title = METADATA_TITLE
        pdf.documentInfo.author = METADATA_AUTHOR
        val document = Document(pdf)
        document.setMargins(
            PAGE_MARGIN_TOP,
            PAGE_MARGIN_RIGHT,
            PAGE_MARGIN_BOTTOM,
            PAGE_MARGIN_LEFT
        )
        return document
    }

    private fun cellWithTableElement(element: TableElement): Cell {
        val textElements = listOfNotNull(
            element.boldContent?.let { Text(it).addStyle(tableBoldContentStyle) },
            element.content?.let { Text(it).addStyle(tableContentStyle) },
        )
        val paragraph = Paragraph()
        paragraph.addAll(textElements)
        return borderlessCellWithElement(paragraph, element.shading)
    }

    private fun titleCellWithText(text: String): Cell {
        return textCellWithStyle(text, tableTitleStyle)
    }

    private fun headerCellWithText(text: String): Cell {
        val cell = textCellWithStyle(text, tableHeaderStyle)
        cell.setBorderBottom(BORDER)
        return cell
    }

    private fun textCellWithStyle(text: String, style: Style): Cell {
        val cell = borderlessCellWithText(text)
        cell.addStyle(style)
        return cell
    }

    private fun borderlessCellWithText(text: String): Cell {
        return borderlessCellWithElement(Paragraph(text))
    }

    private fun borderlessCellWithElement(element: IBlockElement, shading: Double? = null): Cell {
        val cell = emptyCell()
        cell.add(element)
        if (shading != null) {
            val rgb = 1 - (shading / 2).toFloat()
            cell.setBackgroundColor(DeviceRgb(rgb, rgb, rgb))
        }
        return cell
    }

    private fun emptyCell(): Cell {
        val cell = Cell(1, 1)
        cell.setBorder(Border.NO_BORDER)
        return cell
    }

    companion object {
        fun create(outputPath: String): ReportWriter {
            val properties = WriterProperties().setFullCompressionMode(true)
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                .useSmartMode()

            val writer = PdfWriter(outputPath, properties)
            writer.compressionLevel = 9
            return ReportWriter(writer, outputPath, createFont(FONT_REGULAR_PATH), createFont(FONT_BOLD_PATH))
        }

        private fun createFont(fontPath: String): PdfFont {
            return PdfFontFactory.createFont(loadFontProgram(fontPath), PdfEncodings.IDENTITY_H)
        }

        private fun loadFontProgram(resourcePath: String): FontProgram {
            return try {
                FontProgramFactory.createFont(resourcePath)
            } catch (exception: IOException) {
                // Should never happen, fonts are loaded from code
                throw IllegalStateException(exception)
            }
        }
    }
}
