package presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import presentation.MainScreenViewModel.Companion.SUCCESSFUL_GENERATE_MESSAGE
import testing.FakeInputValidator
import testing.FakePathPicker
import testing.FakeResumeGenerator
import testing.TestDispatchersProvider
import ui.screens.MainScreenEvent
import kotlin.test.*

class MainScreenViewModelTest {

    private val inputValidator = FakeInputValidator()
    private val pathPicker = FakePathPicker()
    private val resumeGenerator = FakeResumeGenerator()
    private val providedApiKeys = mutableListOf<String>()

    @Test
    fun `given valid url change event when handling then updates url field as valid`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainScreenEvent.ChangeUrl(URL))

        assertEquals(URL, viewModel.state.value.urlField.value)
        assertTrue(viewModel.state.value.urlField.isValid)
        assertEquals(listOf(URL), inputValidator.validatedUrls)
    }

    @Test
    fun `given invalid url change event when handling then updates url field as invalid`() = runTest {
        val viewModel = createViewModel()
        inputValidator.isValidUrlResult = false

        viewModel.onEvent(MainScreenEvent.ChangeUrl(URL))

        assertEquals(URL, viewModel.state.value.urlField.value)
        assertFalse(viewModel.state.value.urlField.isValid)
    }

    @Test
    fun `given file name change event when handling then updates file name field`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainScreenEvent.ChangeFileName(FILE_NAME))

        assertEquals(FILE_NAME, viewModel.state.value.fileNameField.value)
        assertTrue(viewModel.state.value.fileNameField.isValid)
        assertEquals(listOf(FILE_NAME), inputValidator.validatedFileNames)
    }

    @Test
    fun `given api key change event when handling then updates api key field`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainScreenEvent.ChangeApiKey(API_KEY))

        assertEquals(API_KEY, viewModel.state.value.apiKeyField.value)
        assertTrue(viewModel.state.value.apiKeyField.isValid)
        assertEquals(listOf(API_KEY), inputValidator.validatedApiKeys)
    }

    @Test
    fun `given input path picker event when handling then updates input field with picked path`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = INPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)

        assertEquals(INPUT_PATH, viewModel.state.value.inputField.value)
        assertTrue(viewModel.state.value.inputField.isValid)
        assertEquals(listOf(null), pathPicker.initialPaths)
        assertEquals(listOf(INPUT_PATH), inputValidator.validatedPaths)
    }

    @Test
    fun `given input path picker with current value when picking again then starts from current path`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = INPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)
        pathPicker.result = NEW_INPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)

        assertEquals(NEW_INPUT_PATH, viewModel.state.value.inputField.value)
        assertEquals(listOf(null, INPUT_PATH), pathPicker.initialPaths)
    }

    @Test
    fun `given cancelled input path picker when handling then leaves input field unchanged`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = null

        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)

        assertNull(viewModel.state.value.inputField.value)
        assertFalse(viewModel.state.value.inputField.isValid)
    }

    @Test
    fun `given output path picker event when handling then updates output field with picked path`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = OUTPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenOutputPathPicker)

        assertEquals(OUTPUT_PATH, viewModel.state.value.outputField.value)
        assertTrue(viewModel.state.value.outputField.isValid)
        assertEquals(listOf(null), pathPicker.initialPaths)
        assertEquals(listOf(OUTPUT_PATH), inputValidator.validatedPaths)
    }

    @Test
    fun `given cancelled output path picker when handling then leaves output field unchanged`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = null

        viewModel.onEvent(MainScreenEvent.OpenOutputPathPicker)

        assertNull(viewModel.state.value.outputField.value)
        assertFalse(viewModel.state.value.outputField.isValid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given valid state when generating then generates with field values and shows success`() = runTest {
        val viewModel = createViewModel()
        setValidState(viewModel)

        assertTrue(viewModel.state.value.isGenerateButtonEnabled)

        viewModel.onEvent(MainScreenEvent.Generate)

        assertTrue(viewModel.state.value.isGenerateButtonLoading)
        assertFalse(viewModel.state.value.isGenerateButtonEnabled)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isGenerateButtonLoading)
        assertEquals(listOf(API_KEY), providedApiKeys)
        assertEquals(1, resumeGenerator.calls)
        assertEquals(listOf(INPUT_PATH), resumeGenerator.inputPaths)
        assertEquals(listOf(OUTPUT_PATH), resumeGenerator.outputPaths)
        assertEquals(listOf(URL), resumeGenerator.urls)
        assertEquals(listOf(FILE_NAME), resumeGenerator.fileNames)

        val notification = assertNotNull(viewModel.state.value.notificationMessage)
        assertFalse(notification.isError)
        assertEquals(SUCCESSFUL_GENERATE_MESSAGE, notification.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given failing generator when generating then shows error notification`() = runTest {
        val viewModel = createViewModel()
        resumeGenerator.error = IllegalStateException("boom")
        setValidState(viewModel)

        viewModel.onEvent(MainScreenEvent.Generate)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isGenerateButtonLoading)
        val notification = assertNotNull(viewModel.state.value.notificationMessage)
        assertTrue(notification.isError)
        assertEquals("boom", notification.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given dismiss notification event when handling then clears notification`() = runTest {
        val viewModel = createViewModel()
        resumeGenerator.error = IllegalStateException("boom")
        setValidState(viewModel)

        viewModel.onEvent(MainScreenEvent.Generate)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.notificationMessage)

        viewModel.onEvent(MainScreenEvent.DismissNotification)

        assertNull(viewModel.state.value.notificationMessage)
    }

    private fun TestScope.createViewModel(): MainScreenViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return MainScreenViewModel(
            pathPicker = pathPicker,
            dispatchersProvider = TestDispatchersProvider(dispatcher),
            inputValidator = inputValidator,
            resumeGeneratorProvider = { apiKey ->
                providedApiKeys.add(apiKey)
                resumeGenerator
            },
        )
    }

    private fun setValidState(viewModel: MainScreenViewModel) {
        viewModel.onEvent(MainScreenEvent.ChangeUrl(URL))
        viewModel.onEvent(MainScreenEvent.ChangeFileName(FILE_NAME))
        viewModel.onEvent(MainScreenEvent.ChangeApiKey(API_KEY))
        pathPicker.result = INPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)
        pathPicker.result = OUTPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenOutputPathPicker)
    }

    private companion object {
        const val URL = "https://example.com/job"
        const val FILE_NAME = "MyResume"
        const val API_KEY = "sk-" + "fedcba9876543210fedcba9876543210"
        const val INPUT_PATH = "/tmp/input"
        const val OUTPUT_PATH = "/tmp/output"
        const val NEW_INPUT_PATH = "/tmp/other-input"
    }
}
