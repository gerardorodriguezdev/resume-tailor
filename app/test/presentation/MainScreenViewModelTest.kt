package presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import presentation.MainScreenViewModel.Companion.SUCCESSFUL_GENERATE_MESSAGE
import testing.*
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

        viewModel.onEvent(MainScreenEvent.ChangeUrl(TestData.JOB_URL))

        assertEquals(TestData.JOB_URL, viewModel.state.value.urlField.value)
        assertTrue(viewModel.state.value.urlField.isValid)
        assertEquals(listOf(TestData.JOB_URL), inputValidator.validatedUrls)
    }

    @Test
    fun `given invalid url change event when handling then updates url field as invalid`() = runTest {
        val viewModel = createViewModel()
        inputValidator.isValidUrlResult = false

        viewModel.onEvent(MainScreenEvent.ChangeUrl(TestData.JOB_URL))

        assertEquals(TestData.JOB_URL, viewModel.state.value.urlField.value)
        assertFalse(viewModel.state.value.urlField.isValid)
    }

    @Test
    fun `given file name change event when handling then updates file name field`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainScreenEvent.ChangeFileName(TestData.MY_RESUME_FILE_NAME))

        assertEquals(TestData.MY_RESUME_FILE_NAME, viewModel.state.value.fileNameField.value)
        assertTrue(viewModel.state.value.fileNameField.isValid)
        assertEquals(listOf(TestData.MY_RESUME_FILE_NAME), inputValidator.validatedFileNames)
    }

    @Test
    fun `given api key change event when handling then updates api key field`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainScreenEvent.ChangeApiKey(TestData.API_KEY))

        assertEquals(TestData.API_KEY, viewModel.state.value.apiKeyField.value)
        assertTrue(viewModel.state.value.apiKeyField.isValid)
        assertEquals(listOf(TestData.API_KEY), inputValidator.validatedApiKeys)
    }

    @Test
    fun `given input path picker event when handling then updates input field with picked path`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = TestData.INPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)

        assertEquals(TestData.INPUT_PATH, viewModel.state.value.inputField.value)
        assertTrue(viewModel.state.value.inputField.isValid)
        assertEquals(listOf(null), pathPicker.initialPaths)
        assertEquals(listOf(TestData.INPUT_PATH), inputValidator.validatedPaths)
    }

    @Test
    fun `given input path picker with current value when picking again then starts from current path`() = runTest {
        val viewModel = createViewModel()
        pathPicker.result = TestData.INPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)
        pathPicker.result = NEW_INPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)

        assertEquals(NEW_INPUT_PATH, viewModel.state.value.inputField.value)
        assertEquals(listOf(null, TestData.INPUT_PATH), pathPicker.initialPaths)
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
        pathPicker.result = TestData.OUTPUT_PATH

        viewModel.onEvent(MainScreenEvent.OpenOutputPathPicker)

        assertEquals(TestData.OUTPUT_PATH, viewModel.state.value.outputField.value)
        assertTrue(viewModel.state.value.outputField.isValid)
        assertEquals(listOf(null), pathPicker.initialPaths)
        assertEquals(listOf(TestData.OUTPUT_PATH), inputValidator.validatedPaths)
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
        assertEquals(listOf(TestData.API_KEY), providedApiKeys)
        assertEquals(1, resumeGenerator.calls)
        assertEquals(listOf(TestData.INPUT_PATH), resumeGenerator.inputPaths)
        assertEquals(listOf(TestData.OUTPUT_PATH), resumeGenerator.outputPaths)
        assertEquals(listOf(TestData.JOB_URL), resumeGenerator.urls)
        assertEquals(listOf(TestData.MY_RESUME_FILE_NAME), resumeGenerator.fileNames)

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
        viewModel.onEvent(MainScreenEvent.ChangeUrl(TestData.JOB_URL))
        viewModel.onEvent(MainScreenEvent.ChangeFileName(TestData.MY_RESUME_FILE_NAME))
        viewModel.onEvent(MainScreenEvent.ChangeApiKey(TestData.API_KEY))
        pathPicker.result = TestData.INPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenInputPathPicker)
        pathPicker.result = TestData.OUTPUT_PATH
        viewModel.onEvent(MainScreenEvent.OpenOutputPathPicker)
    }

    private companion object {
        const val NEW_INPUT_PATH = "/tmp/other-input"
    }
}
