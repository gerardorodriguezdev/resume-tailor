package presentation

import ResumeGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.models.Field
import ui.screens.MainScreenEvent
import ui.screens.MainScreenState
import utils.DispatchersProvider
import utils.NoOpLogger
import kotlin.coroutines.cancellation.CancellationException

internal class MainScreenViewModel(
    private val pathPicker: PathPicker,
    private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {
    private var generateJob: Job? = null

    val state: StateFlow<MainScreenState>
        field = MutableStateFlow(initialState)

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ChangeUrl -> changeUrl(event.url)
            is MainScreenEvent.ChangeFileName -> changeFileName(event.fileName)
            is MainScreenEvent.ChangeApiKey -> changeApiKey(event.apiKey)
            is MainScreenEvent.OpenInputPathPicker -> openInputPathPicker()
            is MainScreenEvent.OpenOutputPathPicker -> openOutputPathPicker()
            is MainScreenEvent.Generate -> generate()
            is MainScreenEvent.DismissError -> dismissError()
        }
    }

    private fun changeUrl(url: String) =
        reduce { state ->
            state.copy(urlField = Field(value = url, isValid = isValidUrl(url)))
        }

    private fun changeFileName(fileName: String) =
        reduce { state ->
            state.copy(fileNameField = Field(value = fileName, isValid = isValidFileName(fileName)))
        }

    private fun changeApiKey(apiKey: String) =
        reduce { state ->
            state.copy(apiKeyField = Field(value = apiKey, isValid = isValidApiKey(apiKey)))
        }

    //TODO Review
    private fun openInputPathPicker() {
        viewModelScope.launch {
            val path = pathPicker.pick() ?: return@launch
            reduce { state -> state.copy(inputField = Field(value = path, isValid = isValidPath(path))) }
        }
    }

    //TODO Review
    private fun openOutputPathPicker() {
        viewModelScope.launch {
            val path = pathPicker.pick() ?: return@launch
            reduce { state -> state.copy(outputField = Field(value = path, isValid = isValidPath(path))) }
        }
    }

    //TODO: Update
    private fun generate() {
        generateJob = viewModelScope.launch {
            generateJob?.cancel()

            setLoading(true)

            try {
                generateJob = withContext(dispatchersProvider.io) {
                    launch {
                        val resumeGenerator = ResumeGenerator.build(
                            apiKey = state.value.apiKeyField.value!!,
                            logger = NoOpLogger,
                        )
                        resumeGenerator.generate(
                            inputPath = state.value.inputField.value!!,
                            outputPath = state.value.outputField.value!!,
                            url = state.value.urlField.value!!,
                            fileName = state.value.fileNameField.value!!,
                        )
                    }
                }
            } catch (error: Exception) {
                if (error !is CancellationException) {
                    generateError()
                }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        reduce { state -> state.copy(isGenerateButtonLoading = isLoading) }
    }

    //TODO: Update other errors
    private fun generateError() {
        reduce { state -> state.copy(generateError = GENERATE_ERROR_MESSAGE) }
    }

    private fun dismissError() {
        reduce { state -> state.copy(generateError = null) }
    }

    //TODO: Fix
    private fun reduce(transform: (MainScreenState) -> MainScreenState) {
        state.update { current ->
            val updated = transform(current)
            updated.copy(isGenerateButtonEnabled = updated.isGenerateButtonEnabled())
        }
    }

    //TODO: Fix
    private fun MainScreenState.isGenerateButtonEnabled(): Boolean =
        urlField.isValid && fileNameField.isValid && apiKeyField.isValid && inputField.isValid && outputField.isValid && !isGenerateButtonLoading

    //TODO: Update
    private fun isValidUrl(url: String): Boolean = false

    //TODO: Update
    private fun isValidFileName(fileName: String): Boolean = false

    //TODO: Update
    private fun isValidApiKey(apiKey: String): Boolean = false

    //TODO: Update
    private fun isValidPath(path: String): Boolean = false

    override fun onCleared() {
        super.onCleared()

        generateJob?.cancel()
    }

    private companion object {
        const val GENERATE_ERROR_MESSAGE = "Failed to generate resume"

        val initialState = MainScreenState(
            urlField = Field(value = null, isValid = false),
            fileNameField = Field(value = null, isValid = false),
            apiKeyField = Field(value = null, isValid = false),
            inputField = Field(value = null, isValid = false),
            outputField = Field(value = null, isValid = false),
            generateError = null,
            isGenerateButtonEnabled = false,
            isGenerateButtonLoading = false,
        )
    }
}
