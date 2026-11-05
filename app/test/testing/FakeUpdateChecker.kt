package testing

import utils.UpdateChecker

internal class FakeUpdateChecker(
    var result: String? = null,
) : UpdateChecker {
    var calls: Int = 0
        private set

    override suspend fun newerVersionOrNull(): String? {
        calls++
        return result
    }
}
