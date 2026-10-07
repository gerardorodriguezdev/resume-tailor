package ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ui.models.Field
import androidx.compose.material3.TextField as MaterialTextField

@Composable
fun TextField(
    placeholder: String,
    field: Field<String?>,
    onTextChanged: (newText: String) -> Unit,
    error: String?,
) {
    MaterialTextField(
        value = field.value.orEmpty(),
        onValueChange = onTextChanged,
        placeholder = { Text(placeholder) },
        supportingText = { error?.let { Text(it) } },
        isError = error != null,
    )
}
