import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import kotlinx.coroutines.test.runTest
import testing.TestData
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlGeneratorTest {

    private val expectedHtml = getExpectedHtml(EXPECTED_HTML_PATH)

    @Test
    fun `given sections when generating then matches saved html fixture`() = runTest {
        val actualHtml = DefaultHtmlGenerator().generate(TestData.testSections)

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                browser.newContext().use { context ->
                    context.newPage().use { page ->
                        page.setContent(expectedHtml)
                        val expectedResume = page.extractResume()

                        page.setContent(actualHtml)
                        val actualResume = page.extractResume()

                        assertEquals(expectedResume, actualResume)
                    }
                }
            }
        }
    }

    private fun Page.extractResume(): ResumeDocument = ResumeDocument(
        language = locator("html").getAttribute("lang").orEmpty(),
        charset = locator("meta[charset]").getAttribute("charset").orEmpty(),
        viewport = locator("meta[name=viewport]").getAttribute("content").orEmpty(),
        title = title(),
        resumeClass = locator(RESUME_SELECTOR).first().getAttribute("class").orEmpty(),
        headerClass = locator("$RESUME_SELECTOR > header").first().getAttribute("class").orEmpty(),
        headerTitle = locator("$RESUME_SELECTOR > header h1").innerText(),
        contactClass = locator("$RESUME_SELECTOR .resume-header-contact").getAttribute("class").orEmpty(),
        headerContact = normalizeWhitespace(locator("$RESUME_SELECTOR .resume-header-contact").innerText()),
        sections = locator("$RESUME_SELECTOR > section").all().map { section ->
            SectionData(
                heading = section.locator("h2").innerText(),
                contentClass = section.locator("div").first().getAttribute("class").orEmpty(),
                paragraphs = section.locator("div p").allInnerTexts().map(::normalizeWhitespace),
            )
        },
    )

    private fun normalizeWhitespace(text: String): String =
        text.replace(Regex("\\s+"), " ").trim()

    private fun getExpectedHtml(path: String): String =
        javaClass.classLoader.getResourceAsStream(path).use { it!!.readBytes().decodeToString() }

    private data class ResumeDocument(
        val language: String,
        val charset: String,
        val viewport: String,
        val title: String,
        val resumeClass: String,
        val headerClass: String,
        val headerTitle: String,
        val contactClass: String,
        val headerContact: String,
        val sections: List<SectionData>,
    )

    private data class SectionData(
        val heading: String,
        val contentClass: String,
        val paragraphs: List<String>,
    )

    private companion object {
        const val EXPECTED_HTML_PATH = "expected-resume.html"
        const val RESUME_SELECTOR = "body > div"
    }
}
