package ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import ui.models.Field

@Composable
internal fun MainScreen(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onEvent: (MainScreenEvent) -> Unit,
) {
    MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
        Scaffold { padding ->

        }
    }
}

sealed interface MainScreenEvent {
    data class ChangeUrl(val url: String) : MainScreenEvent
    data object OpenInputPathPicker : MainScreenEvent
    data object OpenOutputPathPicker : MainScreenEvent
    data class ChangeFileName(val fileName: String) : MainScreenEvent
    data object Generate : MainScreenEvent
    data object DismissError : MainScreenEvent
}

data class MainScreenState(
    val urlField: Field<String?>,
    val inputField: Field<String?>,
    val outputField: Field<String?>,
    val fileNameField: Field<String?>,
    val isLoading: Boolean,
    val error: String?,
    val isGenerateButtonEnabled: Boolean,
)