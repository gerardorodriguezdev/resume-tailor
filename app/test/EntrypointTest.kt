import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import presentation.MainScreenViewModel
import testing.E2eTest
import testing.FakePathPicker
import testing.TestData
import testing.TestExtensions
import ui.screens.MainScreenTestTags
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
@E2eTest
class EntrypointTest {

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    private val pathPicker = FakePathPicker()

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    fun `given complete inputs when generating then saves pdf in output directory`() {
        TestExtensions.writeResumeTo(inputDirectory)
        pathPicker.result = inputDirectory.toString()

        runComposeUiTest(testTimeout = GENERATION_TIMEOUT_MILLIS.milliseconds) {
            setContent { MainApp(viewModel = buildMainScreenViewModel(pathPicker = pathPicker)) }

            onNodeWithTag(MainScreenTestTags.URL_FIELD).performTextInput(TestData.WEBSITE_URL)
            onNodeWithTag(MainScreenTestTags.FILE_NAME_FIELD).performTextClearance()
            onNodeWithTag(MainScreenTestTags.FILE_NAME_FIELD).performTextInput(TestData.FILE_NAME)
            onNodeWithTag(MainScreenTestTags.API_KEY_FIELD).performTextInput(System.getenv("TEST_DEEPSEEK_API_KEY"))

            onNodeWithTag(MainScreenTestTags.INPUT_BROWSE_BUTTON).performClick()
            pathPicker.result = outputDirectory.toString()
            onNodeWithTag(MainScreenTestTags.OUTPUT_BROWSE_BUTTON).performClick()

            onNodeWithTag(MainScreenTestTags.GENERATE_BUTTON).performClick()

            waitUntil("notification is shown", timeoutMillis = GENERATION_TIMEOUT_MILLIS) {
                onAllNodesWithTag(MainScreenTestTags.SNACKBAR).fetchSemanticsNodes().isNotEmpty()
            }

            val notificationTexts = onAllNodes(
                matcher = hasAnyAncestor(hasTestTag(MainScreenTestTags.SNACKBAR)),
                useUnmergedTree = true,
            )
                .fetchSemanticsNodes()
                .flatMap { node -> node.config.getOrElse(SemanticsProperties.Text) { emptyList() } }
                .map { text -> text.text }

            assertContains(notificationTexts, MainScreenViewModel.SUCCESSFUL_GENERATE_MESSAGE)
        }

        assertTrue(TestExtensions.pdfExists(outputDirectory, TestData.FILE_NAME))
    }

    private companion object {
        const val GENERATION_TIMEOUT_MILLIS = 150_000L
    }
}
