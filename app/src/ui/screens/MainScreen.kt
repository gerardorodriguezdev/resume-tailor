package ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
internal fun MainScreen(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
        Scaffold { padding ->

        }
    }
}
