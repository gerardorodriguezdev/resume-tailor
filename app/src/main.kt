import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import presentation.MainScreenViewModel
import ui.screens.MainScreen
import utils.AwtPathPicker
import utils.DefaultDispatchersProvider

fun main() {
    systemSetup()

    val pathPicker = AwtPathPicker()
    val dispatchersProvider = DefaultDispatchersProvider()

    singleWindowApplication(title = "Resume Tailor") {
        val viewModel = viewModel {
            MainScreenViewModel(
                pathPicker = pathPicker,
                dispatchersProvider = dispatchersProvider,
            )
        }
        val state by viewModel.state.collectAsState()

        MainScreen(
            state = state,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun systemSetup() {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    System.setProperty("apple.awt.application.appearance", "system")
}
