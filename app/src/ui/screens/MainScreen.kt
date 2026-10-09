package ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ui.components.Button
import ui.components.TextField
import ui.models.Field

@Composable
internal fun MainScreen(
    state: MainScreenState,
    onEvent: (MainScreenEvent) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { snackbarData ->
                    val isError = state.notificationMessage?.isError == true
                    Snackbar(
                        snackbarData = snackbarData,
                        modifier = Modifier.testTag(MainScreenTestTags.SNACKBAR),
                        containerColor = if (isError) MaterialTheme.colorScheme.error else SnackbarDefaults.color,
                        contentColor = if (isError) MaterialTheme.colorScheme.onError else SnackbarDefaults.contentColor,
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    placeholder = "Website url",
                    field = state.urlField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeUrl(it)) },
                    supportingText = state.urlField.supportingText(invalidText = "Invalid URL"),
                    modifier = Modifier.fillMaxWidth().testTag(MainScreenTestTags.URL_FIELD),
                )

                TextField(
                    placeholder = "File name",
                    field = state.fileNameField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeFileName(it)) },
                    supportingText = state.fileNameField.supportingText(
                        templateText = state.fileNameField.value?.let { value -> "$value.pdf" } ?: "MyFileName.pdf",
                        invalidText = "Invalid file name"
                    ),
                    modifier = Modifier.fillMaxWidth().testTag(MainScreenTestTags.FILE_NAME_FIELD),
                )

                TextField(
                    placeholder = "Api key",
                    field = state.apiKeyField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeApiKey(it)) },
                    supportingText = state.apiKeyField.supportingText(invalidText = "Invalid api key"),
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth().testTag(MainScreenTestTags.API_KEY_FIELD),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        placeholder = "Input folder",
                        field = state.inputField,
                        supportingText = state.inputField.supportingText(invalidText = "Invalid path"),
                        readOnly = true,
                        modifier = Modifier.weight(1f).testTag(MainScreenTestTags.INPUT_FIELD),
                    )
                    Button(
                        text = "Browse",
                        onClick = { onEvent(MainScreenEvent.OpenInputPathPicker) },
                        modifier = Modifier.testTag(MainScreenTestTags.INPUT_BROWSE_BUTTON),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        placeholder = "Output folder",
                        field = state.outputField,
                        supportingText = state.outputField.supportingText(invalidText = "Invalid path"),
                        readOnly = true,
                        modifier = Modifier.weight(1f).testTag(MainScreenTestTags.OUTPUT_FIELD),
                    )
                    Button(
                        text = "Browse",
                        onClick = { onEvent(MainScreenEvent.OpenOutputPathPicker) },
                        modifier = Modifier.testTag(MainScreenTestTags.OUTPUT_BROWSE_BUTTON),
                    )
                }

                Button(
                    text = "Generate",
                    isEnabled = state.isGenerateButtonEnabled,
                    isLoading = state.isGenerateButtonLoading,
                    onClick = { onEvent(MainScreenEvent.Generate) },
                    modifier = Modifier.testTag(MainScreenTestTags.GENERATE_BUTTON),
                )
            }
        }

        LaunchedEffect(state.notificationMessage) {
            if (state.notificationMessage != null) {
                snackbarHostState.showSnackbar(state.notificationMessage.message)
                onEvent(MainScreenEvent.DismissNotification)
            }
        }
    }
}

private fun Field<String>.supportingText(
    templateText: String? = null,
    invalidText: String? = null,
): String? =
    when {
        value == null -> templateText
        isValid -> templateText
        !isValid -> invalidText
        else -> templateText
    }

internal sealed interface MainScreenEvent {
    data class ChangeUrl(val url: String) : MainScreenEvent
    data class ChangeFileName(val fileName: String) : MainScreenEvent
    data class ChangeApiKey(val apiKey: String) : MainScreenEvent
    data object OpenInputPathPicker : MainScreenEvent
    data object OpenOutputPathPicker : MainScreenEvent
    data object Generate : MainScreenEvent
    data object DismissNotification : MainScreenEvent
}

internal data class MainScreenState(
    val urlField: Field<String>,
    val fileNameField: Field<String>,
    val apiKeyField: Field<String>,
    val inputField: Field<String>,
    val outputField: Field<String>,
    val notificationMessage: NotificationMessage?,
    val isGenerateButtonLoading: Boolean,
) {
    val isGenerateButtonEnabled: Boolean =
        urlField.isValid && fileNameField.isValid && apiKeyField.isValid && inputField.isValid && outputField.isValid && !isGenerateButtonLoading

    data class NotificationMessage(
        val message: String,
        val isError: Boolean,
    )
}

internal object MainScreenTestTags {
    const val URL_FIELD = "MainScreen.urlField"
    const val FILE_NAME_FIELD = "MainScreen.fileNameField"
    const val API_KEY_FIELD = "MainScreen.apiKeyField"
    const val INPUT_FIELD = "MainScreen.inputField"
    const val OUTPUT_FIELD = "MainScreen.outputField"
    const val INPUT_BROWSE_BUTTON = "MainScreen.inputBrowseButton"
    const val OUTPUT_BROWSE_BUTTON = "MainScreen.outputBrowseButton"
    const val GENERATE_BUTTON = "MainScreen.generateButton"
    const val SNACKBAR = "MainScreen.snackbar"
}