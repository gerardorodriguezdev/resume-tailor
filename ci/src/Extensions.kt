internal fun exec(vararg command: String): String {
    val process = ProcessBuilder(*command).redirectError(ProcessBuilder.Redirect.INHERIT).start()
    process.outputStream.close()

    val output = process.inputStream.readAllBytes().decodeToString()
    check(process.waitFor() == 0) { "${command.first()} failed with exit code ${process.exitValue()}" }

    return output
}
