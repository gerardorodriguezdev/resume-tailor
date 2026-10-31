import com.github.ajalt.clikt.command.main

suspend fun main(args: Array<String>) =
    ResumeTailor(
        inputValidator = DefaultInputValidator(),
        buildResumeGenerator = { apiKey, logger -> ResumeGenerator.build(apiKey, logger) },
    ).main(args)
