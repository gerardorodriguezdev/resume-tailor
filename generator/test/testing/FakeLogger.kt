package testing

import Logger

internal class FakeLogger : Logger {
    val messages: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override fun debug(message: () -> Any?) {
        calls++
        messages.add(message().toString())
    }
}
