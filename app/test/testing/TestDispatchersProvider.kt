package testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import utils.DispatchersProvider

internal class TestDispatchersProvider(
    dispatcher: TestDispatcher,
) : DispatchersProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
