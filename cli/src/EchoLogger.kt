internal class EchoLogger(
    private val echo: (message: String) -> Unit,
    private val verbose: Boolean,
) : Logger {
    override fun debug(message: () -> Any?) {
        if (verbose) {
            echo(message().toString())
        }
    }
}
