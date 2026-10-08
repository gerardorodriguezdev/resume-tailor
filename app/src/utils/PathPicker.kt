package utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal fun interface PathPicker {
    fun pick(initialPath: String?): String?
}

internal class AwtPathPicker : PathPicker {

    override fun pick(initialPath: String?): String? {
        val dialog = FileDialog(null as Frame?, "Select folder", FileDialog.LOAD)
        initialPath?.let { dialog.directory = it }
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName).absolutePath
    }
}
