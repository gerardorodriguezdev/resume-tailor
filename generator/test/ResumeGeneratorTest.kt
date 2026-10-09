import kotlinx.coroutines.test.runTest
import models.Section
import testing.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResumeGeneratorTest {
    private val logger = FakeLogger()
    private val timeProvider = FakeTimeProvider()
    private val mdFilesReader = FakeMdFilesReader(result = MD_CONTENT)
    private val websiteScanner = FakeWebsiteScanner(result = WEBSITE_CONTENT)
    private val agent = FakeAgent(result = AGENT_CONTENT)
    private val sectionsParser = FakeSectionsParser(result = SECTIONS)
    private val htmlGenerator = FakeHtmlGenerator(result = HTML)
    private val htmlToPdfConverter = FakeHtmlToPdfConverter(result = TestData.PDF_BYTES)
    private val pdfSaver = FakePdfSaver()
    private val generator = DefaultResumeGenerator(
        logger = logger,
        timeProvider = timeProvider,
        mdFilesReader = mdFilesReader,
        websiteScanner = websiteScanner,
        agent = agent,
        sectionsParser = sectionsParser,
        htmlGenerator = htmlGenerator,
        htmlToPdfConverter = htmlToPdfConverter,
        pdfSaver = pdfSaver,
    )

    @Test
    fun `given inputs when generating then calls all dependencies once with chained data`() = runTest {
        generator.generate(
            inputPath = INPUT_PATH,
            outputPath = OUTPUT_PATH,
            url = TestData.JOB_URL,
            fileName = TestData.FILE_NAME,
        )

        assertEquals(1, mdFilesReader.calls)
        assertEquals(listOf(INPUT_PATH), mdFilesReader.inputPaths)

        assertEquals(1, websiteScanner.calls)
        assertEquals(listOf(TestData.JOB_URL), websiteScanner.urls)

        assertEquals(1, agent.calls)
        assertEquals(listOf(MD_CONTENT), agent.mdFilesContents)
        assertEquals(listOf(WEBSITE_CONTENT), agent.websiteContents)

        assertEquals(1, sectionsParser.calls)
        assertEquals(listOf(AGENT_CONTENT), sectionsParser.contents)

        assertEquals(1, htmlGenerator.calls)
        assertEquals(listOf(SECTIONS), htmlGenerator.sections)

        assertEquals(1, htmlToPdfConverter.calls)
        assertEquals(listOf(HTML), htmlToPdfConverter.htmlContents)

        assertEquals(1, pdfSaver.calls)
        assertEquals(listOf(OUTPUT_PATH), pdfSaver.outputPaths)
        assertEquals(listOf(TestData.FILE_NAME), pdfSaver.fileNames)
        assertContentEquals(TestData.PDF_BYTES, pdfSaver.pdfs.single())

        assertEquals(TIMED_STEPS, logger.calls)
        assertEquals(TIMED_STEPS * 2, timeProvider.calls)
        assertTrue(logger.messages.all { "in " in it && "ms" in it })
    }

    private companion object {
        const val INPUT_PATH = "resume"
        const val OUTPUT_PATH = "output"

        const val MD_CONTENT = "# Resume"
        const val WEBSITE_CONTENT = "<html>Job posting</html>"
        const val AGENT_CONTENT = "[{\"type\":\"header\",\"content\":\"John Doe\"}]"
        val SECTIONS = listOf(Section.Header("John Doe"))
        const val HTML = "<html><body>Resume</body></html>"

        const val TIMED_STEPS = 7
    }
}
