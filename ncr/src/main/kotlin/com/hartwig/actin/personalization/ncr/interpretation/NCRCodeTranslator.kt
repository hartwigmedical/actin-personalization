package com.hartwig.actin.personalization.ncr.interpretation

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File

data class CodeListEntry(
    @JsonProperty("Codelijst")
    val listName: String,
    @JsonProperty("Code")
    val code: String,
    @JsonProperty("Omschrijving")
    val value: String,
    @JsonProperty("Hoofdgroep")
    val group: String,
    @JsonProperty("Variabele")
    val referencingVariables: String
)

private data class VariableEntry(
    @JsonProperty("Num")
    val index: Int,
    @JsonProperty("Variabele")
    val key: String,
    @JsonProperty("Datatype")
    val type: String,
    @JsonProperty("Format")
    val format: String,
    @JsonProperty("Max_Deci")
    val max: Int?,
    @JsonProperty("Omschrijving")
    val description: String,
    @JsonProperty("Codelijst")
    val codeList: String
)

data class TranslationEntry(
    val codeList: String,
    val code: String,
    val value: String,
    val enumClass: String?,
    val name: String?
)

class NCRCodeTranslator(
    private val codeListMap: Map<String, Map<String, CodeListEntry>>,
    private val keyToCodeListName: Map<String, String>,
    private val translationMap: Map<String, Map<String, TranslationEntry>>
) {
    
    fun getValue(key: String, code: String): String? {
        val codeListName = keyToCodeListName[key] ?: throw IllegalArgumentException("No code list found for key $key")
        val codeList = codeListMap[codeListName] ?: throw IllegalArgumentException("No code list found with name $codeListName")
        return codeList[code]?.value
    }
    
    fun <T> translate(key: String, code: Any, valueOf: (String) -> T): T {
        val codeListName = keyToCodeListName[key] ?: throw IllegalArgumentException("No code list found for key $key")
        val translationList = translationMap[codeListName]
            ?: throw IllegalArgumentException("No translation list found for code list $codeListName")
        val translated = translationList[code.toString()]?.name ?: throw IllegalArgumentException("No translation found for code $code")
        return valueOf(translated)
    }
    
    companion object {
        private const val CODE_LIST_FILE = "NCR data dictionary + overview - Codelijsten.tsv"
        private const val VARIABLE_FILE = "NCR data dictionary + overview - Variabelen.tsv"
        private const val TRANSLATION_FILE = "NCR data dictionary + overview - Translation.tsv"

        fun createFromDirectory(path: String): NCRCodeTranslator {
            val codeListMap = readListFromTSV<CodeListEntry>(path, CODE_LIST_FILE)
                .groupBy(CodeListEntry::listName)
                .mapValues { (_, entries) -> entries.associateBy(CodeListEntry::code) }
            
            val keyToCodeListName = readListFromTSV<VariableEntry>(path, VARIABLE_FILE)
                .associate { it.key to it.codeList }
            
            val translationMap = readListFromTSV<TranslationEntry>(path, TRANSLATION_FILE)
                .groupBy(TranslationEntry::codeList)
                .mapValues { (_, entries) -> entries.associateBy(TranslationEntry::code) }
            
            return NCRCodeTranslator(codeListMap, keyToCodeListName, translationMap)
        }

        private inline fun <reified T> readListFromTSV(dirPath: String, file: String): List<T> {
            val mapper = CsvMapper()
            val schema = CsvSchema.emptySchema().withHeader()
            val reader = mapper.readerFor(T::class.java).with(schema)
            val rows = reader.readValues<T>(File("$dirPath/$file")).readAll()
            return rows
        }
    }
}