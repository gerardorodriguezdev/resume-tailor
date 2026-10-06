import java.io.File

internal interface PdfSaver {
    fun save(outputPath: String, fileName: String, pdf: ByteArray)
}

internal class DefaultPdfSaver : PdfSaver {

    override fun save(outputPath: String, fileName: String, pdf: ByteArray) {
        var targetFile = File(outputPath, "$fileName.pdf")

        var index = 1
        while (targetFile.exists()) {
            targetFile = File(outputPath, "$fileName-${++index}.pdf")
        }

        targetFile.writeBytes(pdf)
    }
}
