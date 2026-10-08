package utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal fun interface PathPicker {
    fun pick(): String?
}

internal class AwtPathPicker : PathPicker {

    override fun pick(): String? {
        val dialog = FileDialog(null as Frame?, "Select folder", FileDialog.LOAD)
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName).absolutePath
    }
}
