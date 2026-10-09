package ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import ui.models.Field
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class MainScreenTest {

    private val events = mutableListOf<MainScreenEvent>()

    @Test
    fun `given url field input when typing then emits change url event`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.URL_FIELD).performTextInput("https://example.com")

        assertEquals(listOf<MainScreenEvent>(MainScreenEvent.ChangeUrl("https://example.com")), events)
    }

    @Test
    fun `given file name field input when typing then emits change file name event`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.FILE_NAME_FIELD).performTextInput("MyResume")

        assertEquals(listOf<MainScreenEvent>(MainScreenEvent.ChangeFileName("MyResume")), events)
    }

    @Test
    fun `given api key field input when typing then emits change api key event`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.API_KEY_FIELD).performTextInput("sk-" + "0123456789abcdef0123456789abcdef")

        assertEquals(
            listOf<MainScreenEvent>(MainScreenEvent.ChangeApiKey("sk-" + "0123456789abcdef0123456789abcdef")),
            events
        )
    }

    @Test
    fun `given api key field when clicking toggle then switches between hide and show`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithText("Show").performClick()

        onNodeWithText("Hide").assertExists()

        onNodeWithText("Hide").performClick()

        onNodeWithText("Show").assertExists()
    }

    @Test
    fun `given input browse button when clicking then emits open input path picker event`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.INPUT_BROWSE_BUTTON).performClick()

        assertEquals(listOf<MainScreenEvent>(MainScreenEvent.OpenInputPathPicker), events)
    }

    @Test
    fun `given output browse button when clicking then emits open output path picker event`() = runComposeUiTest {
        setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.OUTPUT_BROWSE_BUTTON).performClick()

        assertEquals(listOf<MainScreenEvent>(MainScreenEvent.OpenOutputPathPicker), events)
    }

    @Test
    fun `given valid state when clicking generate then emits generate event`() = runComposeUiTest {
        setContent { MainScreen(state = completeState, onEvent = { events.add(it) }) }

        onNodeWithTag(MainScreenTestTags.GENERATE_BUTTON).performClick()

        assertEquals(listOf<MainScreenEvent>(MainScreenEvent.Generate), events)
    }

    @Test
    fun `given incomplete state when rendering then generate button is disabled and emits nothing`() =
        runComposeUiTest {
            setContent { MainScreen(state = emptyState, onEvent = { events.add(it) }) }

            onNodeWithTag(MainScreenTestTags.GENERATE_BUTTON).assertIsNotEnabled()
            onNodeWithTag(MainScreenTestTags.GENERATE_BUTTON).performClick()

            assertTrue(events.isEmpty())
        }

    @Test
    fun `given complete state when rendering then shows all fields and values`() = runComposeUiTest {
        setContent { MainScreen(state = completeState, onEvent = {}) }

        val semanticsTree = onRoot(useUnmergedTree = true).printToString()

        val expectedTexts = listOf(
            "Website url",
            "https://example.com/job",
            "File name",
            "MyResume",
            "MyResume.pdf",
            "Api key",
            "Show",
            "Input folder",
            INPUT_PATH,
            "Output folder",
            OUTPUT_PATH,
            "Browse",
            "Generate",
        )
        assertTrue(
            expectedTexts.all { it in semanticsTree },
            "Semantics tree is missing some expected texts: $semanticsTree"
        )
    }

    private companion object {
        const val INPUT_PATH = "/tmp/input"
        const val OUTPUT_PATH = "/tmp/output"

        val emptyState = MainScreenState(
            urlField = Field(value = null, isValid = false),
            fileNameField = Field(value = null, isValid = false),
            apiKeyField = Field(value = null, isValid = false),
            inputField = Field(value = null, isValid = false),
            outputField = Field(value = null, isValid = false),
            notificationMessage = null,
            isGenerateButtonLoading = false,
        )

        val completeState = MainScreenState(
            urlField = Field(value = "https://example.com/job", isValid = true),
            fileNameField = Field(value = "MyResume", isValid = true),
            apiKeyField = Field(value = "sk-" + "0123456789abcdef0123456789abcdef", isValid = true),
            inputField = Field(value = INPUT_PATH, isValid = true),
            outputField = Field(value = OUTPUT_PATH, isValid = true),
            notificationMessage = null,
            isGenerateButtonLoading = false,
        )
    }
}
