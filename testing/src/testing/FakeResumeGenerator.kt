package testing

import ResumeGenerator

class FakeResumeGenerator(
    var error: Throwable? = null,
) : ResumeGenerator {
    val inputPaths: List<String>
        field = MutableList(0) { "" }
    val outputPaths: List<String>
        field = MutableList(0) { "" }
    val urls: List<String>
        field = MutableList(0) { "" }
    val fileNames: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun generate(inputPath: String, outputPath: String, url: String, fileName: String) {
        calls++
        inputPaths.add(inputPath)
        outputPaths.add(outputPath)
        urls.add(url)
        fileNames.add(fileName)
        error?.let { throw it }
    }
}
