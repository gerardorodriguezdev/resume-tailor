package presentation

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal fun interface PathPicker {
    //TODO: Allow title + allow current
    suspend fun pick(): String?
}

internal class AwtPathPicker : PathPicker {

    //TODO: No callback?
    override suspend fun pick(): String? {
        val dialog = FileDialog(null as Frame?, "Select folder", FileDialog.LOAD)
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName).absolutePath
    }
}
