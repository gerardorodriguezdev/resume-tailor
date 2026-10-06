import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import models.Section
import kotlinx.css.body as bodySelector
import kotlinx.css.h1 as h1Selector
import kotlinx.css.h2 as h2Selector
import kotlinx.css.section as sectionSelector

internal interface HtmlGenerator {
    fun generate(sections: List<Section>): String
}

internal class DefaultHtmlGenerator : HtmlGenerator {

    override fun generate(sections: List<Section>): String =
        buildString {
            appendHTML().html {
                lang = "en"
                head {
                    meta(charset = "utf-8")
                    meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                    title { +"Resume" }
                    style { unsafe { +RESUME_CSS } }
                }
                body {
                    div(classes = RESUME_CLASS) {
                        sections.forEach { section ->
                            when (section) {
                                is Section.Header -> renderHeaderSection(section)
                                is Section.Content -> renderContentSection(section)
                            }
                        }
                    }
                }
            }
        }

    private companion object {
        const val RESUME_CLASS = "resume"
        const val RESUME_HEADER_CLASS = "resume-header"
        const val RESUME_HEADER_CONTACT_CLASS = "resume-header-contact"
        const val SECTION_CONTENT_CLASS = "section-content"

        val HEADING_COLOR = Color("#16324f")
        val BODY_TEXT_COLOR = Color("#2f3542")
        val HEADING_DIVIDER_COLOR = Color("#c3ccd6")
        val HEADER_CONTACT_COLOR = Color("#5b6470")

        const val DEFAULT_FONT_FAMILY = "Helvetica Neue, Helvetica, Arial, Segoe UI, sans-serif"

        val RESUME_CSS: String = CssBuilder().apply {
            universal {
                boxSizing = BoxSizing.borderBox
            }
            bodySelector {
                margin = Margin(0.px)
                fontFamily = DEFAULT_FONT_FAMILY
                fontSize = 10.5.pt
                lineHeight = LineHeight("1.5")
                color = BODY_TEXT_COLOR
            }
            classSelector(RESUME_CLASS) {
                maxWidth = 190.mm
                margin = Margin(0.px, LinearDimension.auto)
                padding = Padding(0.px, 16.mm)
            }
            classSelector(RESUME_HEADER_CLASS) {
                marginBottom = 6.mm
            }
            h1Selector {
                fontSize = 20.pt
                fontWeight = FontWeight.w600
                letterSpacing = 0.02.em
                lineHeight = LineHeight("1.2")
                color = HEADING_COLOR
                margin = Margin(0.px, bottom = 1.5.mm)
            }
            classSelector(RESUME_HEADER_CONTACT_CLASS) {
                whiteSpace = WhiteSpace.preLine
                fontSize = 9.5.pt
                color = HEADER_CONTACT_COLOR
            }
            sectionSelector {
                marginBottom = 4.mm
            }
            h2Selector {
                fontSize = 11.5.pt
                fontWeight = FontWeight.w600
                textTransform = TextTransform.uppercase
                letterSpacing = 0.12.em
                color = HEADING_COLOR
                borderBottom = Border(0.6.pt, BorderStyle.solid, HEADING_DIVIDER_COLOR)
                paddingBottom = 1.2.mm
                margin = Margin(0.px, bottom = 2.5.mm)
            }
            classSelector(SECTION_CONTENT_CLASS) {
                textAlign = TextAlign.justify
            }
            rule(".$SECTION_CONTENT_CLASS p") {
                margin = Margin(0.px)
            }
        }.toString()

        private fun CssBuilder.classSelector(className: String, block: RuleSet) {
            rule(".$className", block = block)
        }

        private fun FlowContent.renderHeaderSection(section: Section.Header) {
            header(classes = RESUME_HEADER_CLASS) {
                h1 { +section.title }
                div(classes = RESUME_HEADER_CONTACT_CLASS) { +section.content }
            }
        }

        private fun FlowContent.renderContentSection(section: Section.Content) {
            section {
                h2 { +section.title }
                div(classes = SECTION_CONTENT_CLASS) {
                    section.content.lines().forEach { line ->
                        p { +line }
                    }
                }
            }
        }
    }
}
