import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class PdfSaverTest {

    private val pdfSaver = DefaultPdfSaver()
    private val outputDirectory = createTempDirectory("pdf-saver-output")

    @Test
    fun `given output path and pdf bytes when saving then writes pdf file`() {
        val outputPath = outputDirectory.toString()
        val pdfBytes = byteArrayOf(1, 2, 3, 4, 5)

        pdfSaver.save(outputPath, "Resume", pdfBytes)

        assertContentEquals(pdfBytes, File(outputDirectory.toFile(), "Resume.pdf").readBytes())
    }

    @Test
    fun `given existing file when saving then writes file with next number`() {
        val outputPath = outputDirectory.toString()
        val pdfBytes = byteArrayOf(1, 2, 3, 4, 5)

        pdfSaver.save(outputPath, "Resume", pdfBytes)
        pdfSaver.save(outputPath, "Resume", pdfBytes)

        val files = outputDirectory.toFile().listFiles().orEmpty()
        assertTrue(files.any { it.name == "Resume.pdf" })
        assertTrue(files.any { it.name == "Resume-2.pdf" })
    }
}
