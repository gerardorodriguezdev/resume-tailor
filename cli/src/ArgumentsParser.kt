internal class ArgumentsParser {

    fun parse(args: Array<String>): ParsingResult {
        if (args.hasHelp) return ParsingResult.Help(HELP_TEXT)

        val arguments = parseArguments(args.filterNot { it == VERBOSE_ARG }.toTypedArray())

        val inputPath = arguments[INPUT_PATH_ARG]
        inputPath?.let { require(isValidPath(it)) { "Invalid input path" } }

        val url = arguments[WEBSITE_ARG].orEmpty()
        require(isValidUrl(url)) { "Invalid website url" }

        val outputPath = arguments[OUTPUT_PATH_ARG]
        outputPath?.let { require(isValidPath(it)) { "Invalid output path" } }

        val fileName = arguments[FILE_NAME_ARG]
        fileName?.let { require(isValidFileName(it)) { "Invalid file name" } }

        val apiKey = arguments[API_KEY_ARG]
        apiKey?.let { require(isValidApiKey(it)) { "Invalid api key" } }

        val verbose = arguments[VERBOSE_ARG]?.toBooleanStrict()

        return ParsingResult.Arguments(
            inputPath = inputPath,
            url = url,
            outputPath = outputPath,
            fileName = fileName,
            apiKey = apiKey,
            verbose = verbose,
        )
    }

    private val Array<String>.hasHelp: Boolean
        get() = any { it == HELP_ARG }

    private fun parseArguments(args: Array<String>): Map<String, String> =
        args.associate { arg ->
            val separator = arg.indexOf('=')
            if (separator == -1) {
                throw IllegalArgumentException("Malformed argument")
            }
            arg.substring(0, separator) to arg.substring(separator + 1)
        }

    sealed interface ParsingResult {
        data class Arguments(
            val inputPath: String?,
            val url: String,
            val outputPath: String?,
            val fileName: String?,
            val apiKey: String?,
            val verbose: Boolean?,
        ) : ParsingResult

        data class Help(val text: String) : ParsingResult
    }

    private companion object {
        const val INPUT_PATH_ARG = "--input"
        const val WEBSITE_ARG = "--website"
        const val OUTPUT_PATH_ARG = "--output"
        const val FILE_NAME_ARG = "--filename"
        const val API_KEY_ARG = "--apikey"
        const val VERBOSE_ARG = "--verbose"
        const val HELP_ARG = "--help"

        const val HELP_TEXT = """
            Usage: resume-tailor --website=<url> [options]

            Options:
              --input=<path>    Directory with md files (default: current directory)
              --website=<url>   Job posting website url (required)
              --output=<path>   Directory to save the generated pdf (default: current directory)
              --filename=<name> Name of the generated pdf file (default: Resume)
              --apikey=<key>    LLM api key
              --verbose         Log each generation step
              --help            Show this help message
        """
    }
}
