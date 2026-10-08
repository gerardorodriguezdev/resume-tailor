import ai.koog.utils.time.KoogClock
import kotlin.uuid.Uuid

interface ResumeGenerator {
    suspend fun generate(inputPath: String, outputPath: String, url: String, fileName: String)

    companion object {
        fun build(apiKey: String, logger: Logger): ResumeGenerator {
            val clock = KoogClock.System
            val timeProvider = SystemTimeProvider()
            val uuid = Uuid.random()
            val mdFilesReader = DefaultMdFilesReader()
            val inputValidator = DefaultInputValidator()
            val websiteScanner = PlaywrightWebsiteScanner(inputValidator)
            val agent = DeepSeekAgent(
                apiKey = apiKey,
                clock = clock,
                uuid = uuid,
            )
            val sectionsParser = JsonSectionsParser()
            val htmlGenerator = DefaultHtmlGenerator()
            val htmlToPdfConverter = PlaywrightHtmlToPdfConverter()
            val pdfSaver = DefaultPdfSaver()
            return DefaultResumeGenerator(
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
        }
    }
}

internal class DefaultResumeGenerator(
    private val logger: Logger,
    private val timeProvider: TimeProvider,
    private val mdFilesReader: MdFilesReader,
    private val websiteScanner: WebsiteScanner,
    private val agent: Agent,
    private val sectionsParser: SectionsParser,
    private val htmlGenerator: HtmlGenerator,
    private val htmlToPdfConverter: HtmlToPdfConverter,
    private val pdfSaver: PdfSaver,
) : ResumeGenerator {

    override suspend fun generate(inputPath: String, outputPath: String, url: String, fileName: String) {
        val mdFilesContent = timed("Markdown files read") {
            mdFilesReader.read(inputPath)
        }

        val websiteContent = timed("Website scanned") {
            websiteScanner.scan(url)
        }

        val content = timed("Resume content generated") {
            agent.content(mdFilesContent = mdFilesContent, websiteContent = websiteContent)
        }

        val sections = timed("Resume sections parsed") {
            sectionsParser.parse(content)
        }

        val html = timed("HTML generated") {
            htmlGenerator.generate(sections)
        }

        val pdf = timed("HTML converted to PDF") {
            htmlToPdfConverter.convert(html)
        }

        timed("PDF saved") {
            pdfSaver.save(outputPath = outputPath, fileName = fileName, pdf = pdf)
        }
    }

    private suspend fun <T> timed(description: String, action: suspend () -> T): T {
        val start = timeProvider.now()
        val result = action()
        val end = timeProvider.now()
        val difference = end - start
        logger.debug { "$description in ${difference.inWholeMilliseconds}ms" }
        return result
    }
}
