import org.junit.jupiter.api.io.TempDir
import testing.TestData
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class PdfSaverTest {

    private val pdfSaver = DefaultPdfSaver()

    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `given output path and pdf bytes when saving then writes pdf file`() {
        val outputPath = outputDirectory.toString()

        pdfSaver.save(outputPath, TestData.FILE_NAME, TestData.PDF_BYTES)

        assertContentEquals(TestData.PDF_BYTES, File(outputDirectory.toFile(), "${TestData.FILE_NAME}.pdf").readBytes())
    }

    @Test
    fun `given existing file when saving then writes file with next number`() {
        val outputPath = outputDirectory.toString()

        pdfSaver.save(outputPath, TestData.FILE_NAME, TestData.PDF_BYTES)
        pdfSaver.save(outputPath, TestData.FILE_NAME, TestData.PDF_BYTES)

        val files = outputDirectory.toFile().listFiles().orEmpty()
        assertTrue(files.any { it.name == "${TestData.FILE_NAME}.pdf" })
        assertTrue(files.any { it.name == "${TestData.FILE_NAME}-2.pdf" })
    }
}
