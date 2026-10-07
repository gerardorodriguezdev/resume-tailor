import androidx.compose.ui.window.singleWindowApplication
import ui.screens.MainScreen

fun main() {
    systemSetup()

    singleWindowApplication(title = "Resume Tailor") {
        MainScreen(
            onEvent = { event -> }
        )
    }
}

private fun systemSetup() {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    System.setProperty("apple.awt.application.appearance", "system")
}