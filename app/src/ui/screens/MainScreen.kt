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
                    .padding(padding),
                //.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    placeholder = "Website url",
                    field = state.urlField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeUrl(it)) },
                    error = if (state.urlField.isValid) null else "Invalid URL",
                )

                TextField(
                    placeholder = "File name",
                    field = state.fileNameField,
                    onTextChanged = { onEvent(MainScreenEvent.ChangeFileName(it)) },
                    error = if (state.fileNameField.isValid) null else "Invalid file name",
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        placeholder = "Input folder",
                        field = state.inputField,
                        error = if (state.inputField.isValid) null else "Invalid path",
                        readOnly = true,
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
                        error = if (state.outputField.isValid) null else "Invalid path",
                        readOnly = true,
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

        LaunchedEffect(state.error) {
            if (state.error != null) {
                snackbarHostState.showSnackbar(state.error)
                onEvent(MainScreenEvent.DismissError)
            }
        }
    }
}

sealed interface MainScreenEvent {
    data class ChangeUrl(val url: String) : MainScreenEvent
    data class ChangeFileName(val fileName: String) : MainScreenEvent
    data object OpenInputPathPicker : MainScreenEvent
    data object OpenOutputPathPicker : MainScreenEvent
    data object Generate : MainScreenEvent
    data object DismissError : MainScreenEvent
}

data class MainScreenState(
    val urlField: Field<String>,
    val fileNameField: Field<String>,
    val inputField: Field<String>,
    val outputField: Field<String>,
    val error: String?,
    val isGenerateButtonEnabled: Boolean,
    val isGenerateButtonLoading: Boolean,
)

