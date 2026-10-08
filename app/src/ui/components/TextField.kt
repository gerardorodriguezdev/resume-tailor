package ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ui.models.Field
import androidx.compose.material3.TextField as MaterialTextField

@Composable
internal fun TextField(
    placeholder: String = "",
    field: Field<String> = Field(value = null, isValid = false),
    onTextChanged: (newText: String) -> Unit = {},
    supportingText: String? = null,
    readOnly: Boolean = false,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    MaterialTextField(
        value = field.value.orEmpty(),
        onValueChange = onTextChanged,
        label = { Text(placeholder) },
        supportingText = { supportingText?.let { Text(it) } },
        isError = field.value != null && !field.isValid,
        readOnly = readOnly,
        singleLine = true,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                TextButton(
                    onClick = { isPasswordVisible = !isPasswordVisible },
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text(if (isPasswordVisible) "Hide" else "Show")
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}
