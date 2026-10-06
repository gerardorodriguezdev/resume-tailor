import org.commonmark.node.*
import org.commonmark.parser.Parser
import java.io.File

internal interface MdFilesReader {
    suspend fun read(inputPath: String): String
}

internal class DefaultMdFilesReader : MdFilesReader {

    override suspend fun read(inputPath: String): String =
        File(inputPath)
            .walkTopDown()
            .filter { it.isFile && it.extension == TARGET_FILE_EXTENSION }
            .sortedBy { file -> file.name }
            .map { file -> file.extractContent() }
            .joinToString("\n")

    private fun File.extractContent(): String {
        val content = readText()
        validateMarkdown(content)
        return content
    }

    private fun validateMarkdown(content: String): String {
        val document = parser.parse(content)
        document.accept(allowedNodesVisitor)
        return content
    }

    private class AllowedNodesVisitor : AbstractVisitor() {
        override fun visitChildren(node: Node) {
            if (node !is Document && !ALLOWED_NODE_TYPES.contains(node::class.java)) {
                throw IllegalArgumentException("Invalid markdown file")
            }
            super.visitChildren(node)
        }
    }

    private companion object {
        const val TARGET_FILE_EXTENSION = "md"

        val ALLOWED_NODE_TYPES = setOf(
            Heading::class.java,
            Paragraph::class.java,
            Emphasis::class.java,
            StrongEmphasis::class.java,
            BulletList::class.java,
            OrderedList::class.java,
            ListItem::class.java,
            Text::class.java,
            SoftLineBreak::class.java,
        )

        val allowedNodesVisitor = AllowedNodesVisitor()

        val parser = Parser.builder().build()
    }
}
