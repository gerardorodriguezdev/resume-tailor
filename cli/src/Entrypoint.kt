import ArgumentsParser.ParsingResult
import io.github.oshai.kotlinlogging.KotlinLogging

//TODO: Test
suspend fun main(args: Array<String>) {
    val argumentsParser = ArgumentsParser()
    when (val result = argumentsParser.parse(args)) {
        is ParsingResult.Help -> result.printHelp()
        is ParsingResult.Arguments -> result.handleArguments()
    }
}

private fun ParsingResult.Help.printHelp() {
    println(text)
}

private suspend fun ParsingResult.Arguments.handleArguments() {
    val environment = Environment(
        inputPath = inputPath ?: System.getProperty("user.dir"),
        url = url,
        outputPath = outputPath ?: System.getProperty("user.dir"),
        apiKey = apiKey ?: System.getenv("API_KEY"),
        verbose = verbose ?: false,
        fileName = fileName ?: "Resume",
    )
    val logger = KotlinLogger(
        delegate = KotlinLogging.logger("DefaultLogger"),
        verbose = environment.verbose,
    )
    val resumeGenerator = ResumeGenerator.build(environment.apiKey, logger)
    resumeGenerator.generate(
        inputPath = environment.inputPath,
        outputPath = environment.outputPath,
        url = environment.url,
        fileName = environment.fileName,
    )
}

internal class Environment(
    val inputPath: String,
    val url: String,
    val apiKey: String,
    val outputPath: String,
    val fileName: String,
    val verbose: Boolean,
)
