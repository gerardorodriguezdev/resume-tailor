import kotlinx.coroutines.test.runTest
import testing.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlToPdfConverterTest {

    private val expectedPdf = getExpectedPdf(EXPECTED_PDF_PATH)

    @Test
    fun `given html content when converting then matches saved pdf fixture`() = runTest {
        val actualPdf = PlaywrightHtmlToPdfConverter().convert(HTML)

        assertTrue(actualPdf.isNotEmpty(), "Converted PDF should not be empty")
        assertTrue(pageCount(actualPdf) > 1, "Converted PDF should span multiple pages")
        assertEquals(removeDateMetadata(expectedPdf), removeDateMetadata(actualPdf))
    }

    private fun getExpectedPdf(path: String): ByteArray =
        javaClass.classLoader.getResourceAsStream(path).use { it!!.readBytes() }

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
