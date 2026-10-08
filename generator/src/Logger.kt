import io.github.oshai.kotlinlogging.KLogger

interface Logger {
    fun debug(message: () -> Any?)
}

class KotlinLogger(
    private val delegate: KLogger,
    private val verbose: Boolean,
) : Logger {
    override fun debug(message: () -> Any?) {
        if (verbose) {
            delegate.debug(message)
        }
    }
}
