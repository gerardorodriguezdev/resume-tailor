import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.*

internal class ResumeTailor(
    inputValidator: InputValidator,
    private val buildResumeGenerator: (apiKey: String, logger: Logger) -> ResumeGenerator,
) : SuspendingCliktCommand(name = "resume-tailor") {

    private val url by option("--website", metavar = "<url>", help = "Job posting website url")
        .required()
        .check("Invalid website url") { url -> inputValidator.isValidUrl(url) }

    private val inputPath by option("--input", metavar = "<path>", help = "Directory with md files")
        .default(System.getProperty("user.dir"))
        .check("Invalid input path") { inputPath -> inputValidator.isValidPath(inputPath) }

    private val outputPath by option("--output", metavar = "<path>", help = "Directory to save the generated pdf")
        .default(System.getProperty("user.dir"))
        .check("Invalid output path") { outputPath -> inputValidator.isValidPath(outputPath) }

    private val fileName by option("--filename", metavar = "<name>", help = "Name of the generated pdf file")
        .default(DEFAULT_FILE_NAME)
        .check("Invalid file name") { fileName -> inputValidator.isValidFileName(fileName) }

    private val apiKey by option("--apikey", metavar = "<key>", envvar = API_KEY_ENVVAR, help = "LLM api key")
        .prompt("LLM api key", hideInput = true)
        .check("Invalid api key") { apiKey -> inputValidator.isValidApiKey(apiKey) }

    private val verbose by option("-v", "--verbose", help = "Log each generation step").flag()

    override suspend fun run() {
        val logger = EchoLogger(
            echo = { message -> echo(message) },
            verbose = verbose,
        )
        buildResumeGenerator(apiKey, logger).generate(
            inputPath = inputPath,
            outputPath = outputPath,
            url = url,
            fileName = fileName,
        )
    }

    private companion object {
        const val DEFAULT_FILE_NAME = "Resume"
        const val API_KEY_ENVVAR = "API_KEY"
    }
}
