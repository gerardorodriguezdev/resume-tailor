import models.Section
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SectionsParserTest {
    private val parser = JsonSectionsParser()

    @Test
    fun `given sections json wrapped in markdown fence when parsing then returns sections`() {
        val result = parser.parse("```json\n$VALID_JSON\n```")

        assertEquals(VALID_SECTIONS, result)
    }

    @Test
    fun `given content without json array when parsing then throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse("No sections here")
        }
    }

    @ParameterizedTest(name = "given {0} when parsing then throws IllegalArgumentException")
    @MethodSource("invalidSections")
    fun `given invalid sections when parsing then throws IllegalArgumentException`(description: String, json: String) {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(json)
        }
    }

    private companion object {
        val HEADER_JSON = """{"type":"Header","content":"John Doe\njohn@example.com"}"""

        val VALID_SECTIONS = listOf(Section.Header(content = "John Doe\njohn@example.com")) +
                CONTENT_SECTION_TITLES.map { Section.Content(title = it, content = defaultContent(it)) }

        val VALID_JSON = jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.map { contentJson(it) })

        @JvmStatic
        fun invalidSections(): List<Arguments> = listOf(
            Arguments.of(
                "empty sections",
                "[]",
            ),
            Arguments.of(
                "header not first",
                jsonArray(listOf(contentJson(CONTENT_SECTION_TITLES.first()), HEADER_JSON)),
            ),
            Arguments.of(
                "more than one header",
                jsonArray(listOf(HEADER_JSON, HEADER_JSON)),
            ),
            Arguments.of(
                "missing content section",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.drop(1).map { contentJson(it) }),
            ),
            Arguments.of(
                "unknown content section title",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.mapIndexed { index, title ->
                    contentJson(if (index == 0) "skills" else title)
                }),
            ),
            Arguments.of(
                "content sections out of order",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.reversed().map { contentJson(it) }),
            ),
            Arguments.of(
                "blank content title",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.mapIndexed { index, title ->
                    contentJson(if (index == 0) "   " else title)
                }),
            ),
            Arguments.of(
                "blank content",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.mapIndexed { index, title ->
                    contentJson(title, content = if (index == 0) "   " else defaultContent(title))
                }),
            ),
            Arguments.of(
                "blank header content",
                jsonArray(listOf("""{"type":"Header","content":"   "}""") + CONTENT_SECTION_TITLES.map { contentJson(it) }),
            ),
            Arguments.of(
                "header content with html tag",
                jsonArray(listOf("""{"type":"Header","content":"John<script>alert(1)</script>"}""") + CONTENT_SECTION_TITLES.map {
                    contentJson(
                        it
                    )
                }),
            ),
            Arguments.of(
                "content title with control character",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.mapIndexed { index, title ->
                    contentJson(if (index == 0) "skills\u0000" else title)
                }),
            ),
            Arguments.of(
                "content with html tag",
                jsonArray(listOf(HEADER_JSON) + CONTENT_SECTION_TITLES.mapIndexed { index, title ->
                    contentJson(
                        title,
                        content = if (index == 0) "<img src=x onerror=alert(1)>" else defaultContent(title)
                    )
                }),
            ),
        )

        fun defaultContent(title: String) = "Content for $title"

        fun contentJson(title: String, content: String = defaultContent(title)) =
            """{"type":"Content","title":"$title","content":"$content"}"""

        fun jsonArray(sections: List<String>) = "[${sections.joinToString(",")}]"
    }
}
