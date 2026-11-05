package utils

import java.awt.Desktop
import java.net.URI

internal fun interface UrlOpener {
    fun open(url: String): Boolean
}

internal class DesktopUrlOpener : UrlOpener {

    override fun open(url: String): Boolean =
        runCatching { Desktop.getDesktop().browse(URI(url)) }.isSuccess
}
