package utils

import Logger

data object NoOpLogger : Logger {
    override fun debug(message: () -> Any?) = Unit
}