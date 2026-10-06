import kotlinx.coroutines.test.runTest
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MdFilesReaderTest {

    @Test
    fun `given md files with valid content when reading then returns appended contents ignoring non md files`() =
        runTest {
            val firstContent = """
            # Title
            Some **bold** and *italic* text.
        """.trimIndent()
            val secondContent = """
            - item one
            - item two
        """.trimIndent()
            val thirdContent = """
            1. first
            2. second
        """.trimIndent()
            val directory = createTempDirectory("files-reader").apply {
                resolve("a.md").writeText(firstContent)
                resolve("b.md").writeText(secondContent)
                resolve("c.txt").writeText("<script>alert('xss')</script>")
                resolve("sub").createDirectories()
                resolve("sub/d.md").writeText(thirdContent)
            }

            val result = DefaultMdFilesReader().read(directory.toString())

            assertEquals("$firstContent\n$secondContent\n$thirdContent", result)
        }

    @Test
    fun `given md file with malicious content when reading then throws IllegalArgumentException`() = runTest {
        val directory = createTempDirectory("files-reader").apply {
            resolve("resume.md").writeText("<script>alert('xss')</script>")
        }

        assertFailsWith<IllegalArgumentException> {
            DefaultMdFilesReader().read(directory.toString())
        }
    }
}
