package ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
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
                    modifier = Modifier.fillMaxWidth(),
                )

                TextField(
                    placeholder = "File name",
                    field = state.fileNameField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeFileName(it)) },
                    supportingText = state.fileNameField.supportingText(invalidText = "Invalid file name"),
                    modifier = Modifier.fillMaxWidth(),
                )

                TextField(
                    placeholder = "Api key",
                    field = state.apiKeyField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeApiKey(it)) },
                    supportingText = state.apiKeyField.supportingText(invalidText = "Invalid api key"),
                    modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        text = "Browse",
                        onClick = { onEvent(MainScreenEvent.OpenInputPathPicker) },
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
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        text = "Browse",
                        onClick = { onEvent(MainScreenEvent.OpenOutputPathPicker) },
                    )
                }

                Button(
                    text = "Generate",
                    isEnabled = state.isGenerateButtonEnabled,
                    isLoading = state.isGenerateButtonLoading,
                    onClick = { onEvent(MainScreenEvent.Generate) },
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

private fun Field<String>.supportingText(emptyText: String? = null, invalidText: String? = null): String? =
    when {
        value == null -> emptyText
        !isValid -> invalidText
        else -> null
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

