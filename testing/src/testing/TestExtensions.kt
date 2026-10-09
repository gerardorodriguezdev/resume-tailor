package testing

import java.io.File
import java.nio.file.Path
import kotlin.io.path.writeText

object TestExtensions {

    fun writeResumeTo(directory: Path): Path =
        directory.resolve("resume.md").also { it.writeText(TestData.RESUME) }

    fun pdfExists(directory: Path, fileName: String): Boolean =
        File(directory.toFile(), "$fileName.pdf").exists()

    fun readTestResource(path: String): ByteArray =
        TestExtensions::class.java.classLoader.getResourceAsStream(path).use { it!!.readBytes() }

    fun readTestResourceText(path: String): String = readTestResource(path).decodeToString()
}
