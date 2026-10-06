package testing

import TimeProvider
import kotlin.time.Instant

internal class FakeTimeProvider(
    var result: Instant = Instant.DISTANT_PAST,
) : TimeProvider {
    var calls: Int = 0
        private set

    override fun now(): Instant {
        calls++
        return result
    }
}
