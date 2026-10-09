package presentation

import InputValidator
import ResumeGenerator
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
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
import utils.PathPicker
import kotlin.coroutines.cancellation.CancellationException

internal class MainScreenViewModel(
    private val pathPicker: PathPicker,
    private val dispatchersProvider: DispatchersProvider,
    private val inputValidator: InputValidator,
    private val resumeGeneratorProvider: (apiKey: String) -> ResumeGenerator,
) : ViewModel() {
    private val asyncScope = CoroutineScope(dispatchersProvider.io.limitedParallelism(1))
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
        val isValid = inputValidator.isValidUrl(url)
        state.update { state -> state.copy(urlField = Field(value = url, isValid = isValid)) }
    }

    private fun changeFileName(fileName: String) {
        val isValid = inputValidator.isValidFileName(fileName)
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
        val isValid = inputValidator.isValidApiKey(apiKey)
        state.update { state -> state.copy(apiKeyField = Field(value = apiKey, isValid = isValid)) }
    }

    private fun openInputPathPicker() {
        val path = pathPicker.pick(state.value.inputField.value) ?: return
        val isValid = inputValidator.isValidPath(path)
        state.update { state -> state.copy(inputField = Field(value = path, isValid = isValid)) }
    }

    private fun openOutputPathPicker() {
        val path = pathPicker.pick(state.value.outputField.value) ?: return
        val isValid = inputValidator.isValidPath(path)
        state.update { state -> state.copy(outputField = Field(value = path, isValid = isValid)) }
    }

    private fun generate() {
        generateJob?.cancel()

        updateLoading(true)

        val currentState = state.value
        generateJob = asyncScope.launch {
            try {
                generate(
                    apiKey = currentState.apiKeyField.value!!,
                    inputPath = currentState.inputField.value!!,
                    outputPath = currentState.outputField.value!!,
                    url = currentState.urlField.value!!,
                    fileName = currentState.fileNameField.value!!,
                )

                withContext(dispatchersProvider.main) {
                    notifySuccess()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                notifyError(error.message)
            } finally {
                updateLoading(false)
            }
        }
    }

    private suspend fun generate(apiKey: String, inputPath: String, outputPath: String, url: String, fileName: String) {
        val resumeGenerator = resumeGeneratorProvider(apiKey)
        resumeGenerator.generate(
            inputPath = inputPath,
            outputPath = outputPath,
            url = url,
            fileName = fileName,
        )
    }

    private fun updateLoading(isLoading: Boolean) {
        state.update { state -> state.copy(isGenerateButtonLoading = isLoading) }
    }

    private fun notifySuccess() {
        state.update { state ->
            state.copy(
                notificationMessage = MainScreenState.NotificationMessage(
                    message = SUCCESSFUL_GENERATE_MESSAGE,
                    isError = false
                )
            )
        }
    }

    private fun notifyError(error: String?) {
        state.update { state ->
            state.copy(
                notificationMessage = MainScreenState.NotificationMessage(
                    message = error ?: "Unknown error",
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

    companion object {
        const val SUCCESSFUL_GENERATE_MESSAGE = "Generation successful"

        private val initialState = MainScreenState(
            urlField = Field(value = null, isValid = false),
            fileNameField = Field(value = "Resume", isValid = false),
            apiKeyField = Field(value = null, isValid = false),
            inputField = Field(value = null, isValid = false),
            outputField = Field(value = null, isValid = false),
            notificationMessage = null,
            isGenerateButtonLoading = false,
        )
    }
}
