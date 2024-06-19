package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.PersonalizationReportWriterApplication
import com.itextpdf.io.exceptions.IOException
import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
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
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue

private val FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf";
private val FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf";
private val PALETTE_BLACK = DeviceRgb(0, 0, 0)
private val PALETTE_BLUE = DeviceRgb(74, 134, 232)
private val PALETTE_MID_GREY = DeviceRgb(101, 106, 108)
private val METADATA_TITLE = "ACTIN Personalization Report v${PersonalizationReportWriterApplication.VERSION}"
private val PAGE_SIZE = PageSize.A4
private const val METADATA_AUTHOR = "Hartwig ACTIN System"
private const val PAGE_MARGIN_TOP = 100f // Top margin also excludes the chapter title, which is rendered in the header
private const val PAGE_MARGIN_LEFT = 30f
private const val PAGE_MARGIN_RIGHT = 30f
private const val PAGE_MARGIN_BOTTOM = 40f

class ReportWriter(private val fontRegular: PdfFont, private val fontBold: PdfFont, private val writer: PdfWriter) {
    private val chapterTitleStyle = Style().setFont(fontBold).setFontSize(11f).setFontColor(PALETTE_BLACK)
    private val tableTitleStyle = Style().setFont(fontBold).setFontSize(9f).setFontColor(PALETTE_BLUE)
    private val tableHeaderStyle = Style().setFont(fontBold).setFontSize(8f).setFontColor(PALETTE_MID_GREY)
    private val tableContentStyle = Style().setFont(fontRegular).setFontSize(8f).setFontColor(PALETTE_BLACK)

    fun writeReport(title: String, tables: List<TableContent>) {
        val document = document()
        addChapterTitle(document, title)
        writeTables(document, tables)
        document.close()
    }

    private fun writeTables(document: Document, tables: List<TableContent>) {
        val contentWidth = contentWidth()
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f))).setWidth(contentWidth)

        tables.forEach { tableContent ->
            table.addCell(titleCellWithText(tableContent.title))
            table.addCell(renderTable(tableContent))
        }
        document.add(table)
    }

    private fun renderTable(content: TableContent): Table {
        content.check()
        val table = Table(
            content.sizes?.let { UnitValue.createPointArray(it.toTypedArray().toFloatArray()) }
                ?: UnitValue.createPercentArray(content.headers.size)
        )
        content.headers.map(::headerCellWithText).forEach(table::addHeaderCell)
        content.rows.flatten().map(::cellWithText).forEach(table::addCell)
        return table
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

    private fun cellWithText(text: String): Cell {
        return textCellWithStyle(text, tableContentStyle)
    }

    private fun titleCellWithText(text: String): Cell {
        return textCellWithStyle(text, tableTitleStyle)
    }

    private fun headerCellWithText(text: String): Cell {
        return textCellWithStyle(text, tableHeaderStyle)
    }

    private fun textCellWithStyle(text: String, style: Style): Cell {
        val cell = borderlessCellWithText(text)
        cell.addStyle(style)
        return cell
    }

    private fun borderlessCellWithText(text: String): Cell {
        val cell = Cell(1, 1)
        cell.setBorder(Border.NO_BORDER)
        cell.add(Paragraph(text))
        return cell
    }

    companion object {
        fun create(outputPath: String): ReportWriter {
            val properties = WriterProperties().setFullCompressionMode(true)
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                .useSmartMode()

            val writer = PdfWriter(outputPath, properties)
            writer.compressionLevel = 9
            return ReportWriter(createFont(FONT_REGULAR_PATH), createFont(FONT_BOLD_PATH), writer)
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