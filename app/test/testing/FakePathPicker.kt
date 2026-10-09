package testing

import utils.PathPicker

internal class FakePathPicker(
    var result: String? = null,
) : PathPicker {
    val initialPaths: List<String?>
        field = MutableList(0) { null }
    var calls: Int = 0
        private set

    override fun pick(initialPath: String?): String? {
        calls++
        initialPaths.add(initialPath)
        return result
    }
}
