import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import presentation.MainScreenViewModel
import ui.screens.MainScreen
import utils.AwtPathPicker
import utils.DefaultDispatchersProvider
import utils.DispatchersProvider
import utils.PathPicker

fun main() {
    systemSetup()

    singleWindowApplication(title = "Resume Tailor") {
        val viewModel = viewModel { buildMainScreenViewModel() }
        MainApp(viewModel = viewModel)
    }
}

private fun systemSetup() {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    System.setProperty("apple.awt.application.appearance", "system")
}


internal fun buildMainScreenViewModel(
    pathPicker: PathPicker = AwtPathPicker(),
    dispatchersProvider: DispatchersProvider = DefaultDispatchersProvider(),
    inputValidator: InputValidator = DefaultInputValidator(),
    logger: Logger = KotlinLogger(
        delegate = KotlinLogging.logger("DefaultLogger"),
        verbose = false,
    ),
): MainScreenViewModel = MainScreenViewModel(
    pathPicker = pathPicker,
    dispatchersProvider = dispatchersProvider,
    inputValidator = inputValidator,
    resumeGeneratorProvider = { apiKey -> ResumeGenerator.build(apiKey, logger) },
)

@Composable
internal fun MainApp(viewModel: MainScreenViewModel) {
    val state by viewModel.state.collectAsState()

    MainScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
