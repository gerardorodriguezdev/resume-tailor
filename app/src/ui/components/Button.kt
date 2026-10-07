package ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button as MaterialButton

@Composable
internal fun Button(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    MaterialButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier,
    ) {
        Text(text)
        if (isLoading) {
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
    }
}
