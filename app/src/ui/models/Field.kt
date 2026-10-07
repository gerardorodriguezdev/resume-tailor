package ui.models

data class Field<T>(
    val value: T,
    val isValid: Boolean,
)