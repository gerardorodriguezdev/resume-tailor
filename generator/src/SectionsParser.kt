import kotlinx.serialization.json.Json
import models.Section

internal interface SectionsParser {
    fun parse(content: String): List<Section>
}

internal class JsonSectionsParser : SectionsParser {

    override fun parse(content: String): List<Section> {
        val json = content.extractJson()
        val sections = Json.decodeFromString<List<Section>>(json)
        validateSections(sections)
        return sections
    }

    private fun validateSections(sections: List<Section>) {
        validateHeaderSection(sections)
        validateContentSections(sections)
    }

    private fun validateHeaderSection(sections: List<Section>) {
        require(sections.isNotEmpty()) { "sections list should not be empty" }
        val header = sections.first()
        require(header is Section.Header) { "Header section must be the first section" }
        require(sections.count { it is Section.Header } == 1) { "Sections must contain exactly one Header section" }
        validateSection(header)
    }

    private fun validateContentSections(sections: List<Section>) {
        val contentSections = sections.drop(1)
        require(contentSections.all { it is Section.Content }) { "All sections after the Header must be Content sections" }
        contentSections.forEach { validateSection(it) }

        val titles = contentSections.map { it.title }
        require(titles == CONTENT_SECTION_TITLES) { "Sections in incorrect order" }
    }

    private fun validateSection(section: Section) {
        require(section.title.isNotBlank()) { "Section title must not be blank" }
        require(section.content.isNotBlank()) { "Section content must not be blank" }
        validateContent(section.title)
        validateContent(section.content)
    }

    private fun String.extractJson(): String =
        JSON_FENCE.find(this)?.groupValues?.get(1) ?: this

    private companion object {
        val JSON_FENCE = Regex("```(?:[A-Za-z]+)?\\s*\\n([\\s\\S]*?)\\n\\s*```")
    }
}
