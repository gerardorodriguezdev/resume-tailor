package testing

import PdfSaver

internal class FakePdfSaver : PdfSaver {
    val outputPaths: List<String>
        field = MutableList(0) { "" }
    val fileNames: List<String>
        field = MutableList(0) { "" }
    val pdfs: List<ByteArray>
        field = MutableList(0) { ByteArray(0) }
    var calls: Int = 0
        private set

    override fun save(outputPath: String, fileName: String, pdf: ByteArray) {
        calls++
        outputPaths.add(outputPath)
        fileNames.add(fileName)
        pdfs.add(pdf)
    }
}
