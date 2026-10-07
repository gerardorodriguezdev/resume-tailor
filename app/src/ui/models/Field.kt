package ui.models

import androidx.compose.runtime.Immutable

@Immutable
internal data class Field<T>(
    val value: T?,
    val isValid: Boolean,
)