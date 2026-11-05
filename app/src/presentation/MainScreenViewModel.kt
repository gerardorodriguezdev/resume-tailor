package presentation

import InputValidator
import ResumeGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ui.models.Field
import ui.screens.MainScreenEvent
import ui.screens.MainScreenState
import utils.DispatchersProvider
import utils.PathPicker
import utils.UpdateChecker
import utils.UrlOpener

internal class MainScreenViewModel(
    private val pathPicker: PathPicker,
    private val dispatchersProvider: DispatchersProvider,
    private val inputValidator: InputValidator,
    private val updateChecker: UpdateChecker,
    private val urlOpener: UrlOpener,
    private val resumeGeneratorProvider: (apiKey: String) -> ResumeGenerator,
) : ViewModel(viewModelScope = CoroutineScope(dispatchersProvider.main + SupervisorJob())) {
    private var generateJob: Job? = null

    val state: StateFlow<MainScreenState>
        field = MutableStateFlow(initialState)

    init {
        checkForUpdate()
    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ChangeUrl -> changeUrl(event.url)
            is MainScreenEvent.ChangeFileName -> changeFileName(event.fileName)
            is MainScreenEvent.ChangeApiKey -> changeApiKey(event.apiKey)
            is MainScreenEvent.OpenInputPathPicker -> openInputPathPicker()
            is MainScreenEvent.OpenOutputPathPicker -> openOutputPathPicker()
            is MainScreenEvent.Generate -> generate()
            is MainScreenEvent.DismissNotification -> dismissError()
            is MainScreenEvent.DownloadUpdate -> downloadUpdate()
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            val newVersion = updateChecker.newerVersionOrNull()
            showAvailableUpdate(newVersion)
        }
    }

    private fun showAvailableUpdate(newVersion: String?) {
        state.update { state -> state.copy(availableUpdateVersion = newVersion) }
    }

    private fun downloadUpdate() {
        urlOpener.open(LATEST_RELEASE_URL)
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
        generateJob = viewModelScope.launch {
            val resumeGenerator = resumeGeneratorProvider(currentState.apiKeyField.value!!)
            val isSuccess = resumeGenerator.generate(
                inputPath = currentState.inputField.value!!,
                outputPath = currentState.outputField.value!!,
                url = currentState.urlField.value!!,
                fileName = currentState.fileNameField.value!!,
            )

            if (isSuccess) notifySuccess() else notifyError()
            updateLoading(false)
        }
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

    private fun notifyError() {
        state.update { state ->
            state.copy(
                notificationMessage = MainScreenState.NotificationMessage(
                    message = FAILED_GENERATE_MESSAGE,
                    isError = true
                )
            )
        }
    }

    private fun dismissError() {
        state.update { state -> state.copy(notificationMessage = null) }
    }

    companion object {
        const val SUCCESSFUL_GENERATE_MESSAGE = "Generation successful"
        const val FAILED_GENERATE_MESSAGE = "Generation failed"

        const val LATEST_RELEASE_URL = "https://github.com/gerardorodriguezdev/resume-tailor/releases/latest"

        private val initialState = MainScreenState(
            urlField = Field(value = null, isValid = false),
            fileNameField = Field(value = "Resume", isValid = false),
            apiKeyField = Field(value = null, isValid = false),
            inputField = Field(value = null, isValid = false),
            outputField = Field(value = null, isValid = false),
            notificationMessage = null,
            isGenerateButtonLoading = false,
            availableUpdateVersion = null,
        )
    }
}
