import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import testing.FakePathPicker
import ui.screens.MainScreenTestTags
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "TEST_DEEPSEEK_API_KEY", matches = ".+")
class EntrypointTest {

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    private val pathPicker = FakePathPicker()

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    fun `given complete inputs when generating then saves pdf in output directory`() {
        inputDirectory.resolve("resume.md").toFile().writeText(RESUME)
        pathPicker.result = inputDirectory.toString()

        runComposeUiTest {
            setContent { MainApp(viewModel = buildMainScreenViewModel(pathPicker = pathPicker)) }

            onNodeWithTag(MainScreenTestTags.URL_FIELD).performTextInput(URL)
            onNodeWithTag(MainScreenTestTags.FILE_NAME_FIELD).performTextClearance()
            onNodeWithTag(MainScreenTestTags.FILE_NAME_FIELD).performTextInput(FILE_NAME)
            onNodeWithTag(MainScreenTestTags.API_KEY_FIELD).performTextInput(System.getenv("TEST_DEEPSEEK_API_KEY"))

            onNodeWithTag(MainScreenTestTags.INPUT_BROWSE_BUTTON).performClick()
            pathPicker.result = outputDirectory.toString()
            onNodeWithTag(MainScreenTestTags.OUTPUT_BROWSE_BUTTON).performClick()

            onNodeWithTag(MainScreenTestTags.GENERATE_BUTTON).performClick()

            waitUntil("notification is shown") {
                onAllNodesWithTag(MainScreenTestTags.SNACKBAR).fetchSemanticsNodes().isNotEmpty()
            }

            onNodeWithText("Generation successful").assertExists()
        }

        assertTrue(File(outputDirectory.toFile(), "$FILE_NAME.pdf").exists())
    }

    private companion object {
        const val URL = "https://example.com"
        const val FILE_NAME = "Resume"

        val RESUME = """
            # John Doe
            john.doe@example.com | +1 555 1234

            ## Summary
            Senior software engineer with 10+ years of experience building scalable backend systems.

            ## Skills
            Languages: Kotlin, Java, TypeScript, Python, SQL
        """.trimIndent()
    }
}
