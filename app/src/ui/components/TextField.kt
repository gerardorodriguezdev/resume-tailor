package ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ui.models.Field
import androidx.compose.material3.TextField as MaterialTextField

@Composable
fun TextField(
    placeholder: String = "",
    field: Field<String> = Field(value = null, isValid = false),
    onTextChanged: (newText: String) -> Unit = {},
    error: String? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    MaterialTextField(
        value = field.value.orEmpty(),
        onValueChange = onTextChanged,
        label = { Text(placeholder) },
        supportingText = { error?.let { Text(it) } },
        isError = !field.isValid,
        readOnly = readOnly,
        singleLine = true,
        modifier = modifier,
    )
}
