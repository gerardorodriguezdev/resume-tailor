import java.io.File
import java.net.InetAddress
import java.net.URI

interface InputValidator {
    fun isValidApiKey(apiKey: String): Boolean

    fun isValidFileName(fileName: String): Boolean

    fun isValidPath(path: String): Boolean

    fun isValidUrl(url: String): Boolean
}

class DefaultInputValidator : InputValidator {

    override fun isValidApiKey(apiKey: String): Boolean = API_KEY_REGEX.matches(apiKey)

    override fun isValidFileName(fileName: String): Boolean = FILE_NAME_REGEX.matches(fileName)

    override fun isValidPath(path: String): Boolean = path.isNotEmpty() && File(path).isDirectory

    override fun isValidUrl(url: String): Boolean =
        runCatching {
            val uri = URI(url)
            uri.scheme.equals("https", ignoreCase = true) &&
                    uri.userInfo == null &&
                    isPublicHost(uri.host)
        }.getOrDefault(false)

    private fun isPublicHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        if (host.equals("localhost", ignoreCase = true) || host.endsWith(".localhost", ignoreCase = true)) {
            return false
        }
        val address = runCatching { InetAddress.getByName(host.removeSurrounding("[", "]")) }.getOrNull()
            ?: return false
        return !address.isLoopbackAddress &&
                !address.isLinkLocalAddress &&
                !address.isSiteLocalAddress &&
                !address.isAnyLocalAddress
    }

    private companion object {
        val API_KEY_REGEX = Regex("^sk-[A-Fa-f0-9]{32}$")
        val FILE_NAME_REGEX = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$")
    }
}
