import androidx.compose.ui.window.singleWindowApplication

fun main() {
    systemSetup()

    singleWindowApplication(title = "Resume Tailor") {
        
    }
}

private fun systemSetup() {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    System.setProperty("apple.awt.application.appearance", "system")
}