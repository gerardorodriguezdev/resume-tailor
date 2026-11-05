import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import presentation.MainScreenViewModel
import ui.screens.MainScreen
import utils.AwtPathPicker
import utils.DefaultDispatchersProvider
import utils.DispatchersProvider
import utils.DesktopUrlOpener
import utils.GitHubUpdateChecker
import utils.PathPicker
import utils.UpdateChecker
import utils.UrlOpener

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
    updateChecker: UpdateChecker = GitHubUpdateChecker(
        currentVersion = System.getProperty("app.version"),
        dispatchersProvider = dispatchersProvider,
        httpClient = HttpClient(CIO),
    ),
    urlOpener: UrlOpener = DesktopUrlOpener(),
    logger: Logger = KotlinLogger(
        delegate = KotlinLogging.logger("DefaultLogger"),
        verbose = false,
    ),
): MainScreenViewModel = MainScreenViewModel(
    pathPicker = pathPicker,
    dispatchersProvider = dispatchersProvider,
    inputValidator = inputValidator,
    updateChecker = updateChecker,
    urlOpener = urlOpener,
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
