package presentation

import ResumeGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isValidApiKey
import isValidFileName
import isValidPath
import isValidUrl
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
    dispatchersProvider: DispatchersProvider,
) : ViewModel() {
    private val asyncDispatcher = dispatchersProvider.io.limitedParallelism(1)
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
            is MainScreenEvent.DismissNotification -> dismissError()
        }
    }

    private fun changeUrl(url: String) {
        val isValid = isValidUrl(url)
        state.update { state -> state.copy(urlField = Field(value = url, isValid = isValid)) }
    }

    private fun changeFileName(fileName: String) {
        val isValid = isValidFileName(fileName)
        state.update { state ->
            state.copy(
                fileNameField = Field(
                    value = fileName,
                    isValid = isValid
                )
            )
        }
    }

    private fun changeApiKey(apiKey: String) {
        val isValid = isValidApiKey(apiKey)
        state.update { state -> state.copy(apiKeyField = Field(value = apiKey, isValid = isValid)) }
    }

    private fun openInputPathPicker() {
        val path = pathPicker.pick() ?: return
        val isValid = isValidPath(path)
        state.update { state -> state.copy(inputField = Field(value = path, isValid = isValid)) }
    }

    private fun openOutputPathPicker() {
        val path = pathPicker.pick() ?: return
        val isValid = isValidPath(path)
        state.update { state -> state.copy(outputField = Field(value = path, isValid = isValid)) }
    }

    private fun generate() {
        generateJob = viewModelScope.launch {
            generateJob?.cancel()

            updateLoading(true)

            try {
                generateJob = withContext(asyncDispatcher) {
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
                    notifyError(error = error.message ?: "Unknown error")
                }
            } finally {
                updateLoading(false)
            }
        }
    }

    private fun updateLoading(isLoading: Boolean) {
        state.update { state -> state.copy(isGenerateButtonLoading = isLoading) }
    }

    private fun notifyError(error: String) {
        state.update { state ->
            state.copy(
                notificationMessage = MainScreenState.NotificationMessage(
                    message = error,
                    isError = true
                )
            )
        }
    }

    private fun dismissError() {
        state.update { state -> state.copy(notificationMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()

        generateJob?.cancel()
    }

    private companion object {
        val initialState = MainScreenState(
            urlField = Field(value = null, isValid = false),
            fileNameField = Field(value = null, isValid = false),
            apiKeyField = Field(value = null, isValid = false),
            inputField = Field(value = null, isValid = false),
            outputField = Field(value = null, isValid = false),
            notificationMessage = null,
            isGenerateButtonLoading = false,
        )
    }
}
