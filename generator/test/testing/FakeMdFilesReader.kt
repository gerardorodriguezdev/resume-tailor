package testing

import MdFilesReader

internal class FakeMdFilesReader(
    var result: String = "",
) : MdFilesReader {
    val inputPaths: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun read(inputPath: String): String {
        calls++
        inputPaths.add(inputPath)
        return result
    }
}
