package ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class Field<T>(
    val value: T?,
    val isValid: Boolean,
)