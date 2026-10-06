import java.net.InetAddress
import java.net.URI

fun isSafePublicUrl(url: String): Boolean =
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

private val UNSAFE_CONTENT = Regex(
    "</?[A-Za-z][^>]*>|" +
            "<!--|" +
            "<!\\[CDATA\\[|" +
            "<!DOCTYPE\\b|" +
            "<\\?|" +
            "[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F" +
            "\\u200B-\\u200F\\u202A-\\u202E\\u2066-\\u2069\\uFEFF]",
    setOf(RegexOption.IGNORE_CASE),
)

internal fun validateContent(content: String) {
    require(!UNSAFE_CONTENT.containsMatchIn(content)) { "Unsafe content detected" }
}