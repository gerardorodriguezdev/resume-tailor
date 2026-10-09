import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Timeout
import testing.TestData
import testing.TestExtensions
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Timeout(value = 120, unit = TimeUnit.SECONDS)
class HtmlToPdfConverterTest {

    private val expectedPdf = TestExtensions.readTestResource(EXPECTED_PDF_PATH)

    @Test
    fun `given html content when converting then matches saved pdf fixture`() = runTest {
        val actualPdf = PlaywrightHtmlToPdfConverter().convert(HTML)

        assertTrue(actualPdf.isNotEmpty(), "Converted PDF should not be empty")
        assertTrue(pageCount(actualPdf) > 1, "Converted PDF should span multiple pages")
        assertEquals(removeDateMetadata(expectedPdf), removeDateMetadata(actualPdf))
    }

    private fun pageCount(pdf: ByteArray): Int =
        PDF_PAGE_REGEX.findAll(pdf.decodeToString(0, pdf.size, false)).count()

    private fun removeDateMetadata(pdf: ByteArray): String =
        pdf.decodeToString(0, pdf.size, false)
            .replace(PDF_DATE_METADATA, "")

    private companion object {
        const val EXPECTED_PDF_PATH = "expected-resume.pdf"
        val HTML = DefaultHtmlGenerator().generate(TestData.testSections)
        val PDF_DATE_METADATA = Regex("""(CreationDate|ModDate) \(D:[^)]*\)""")
        val PDF_PAGE_REGEX = Regex("""/Type\s*/Page[^s]""")
    }
}
