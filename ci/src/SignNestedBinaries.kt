import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.nio.ByteBuffer
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.io.path.*

internal class SignNestedBinaries : CliktCommand() {
    private val jarPaths by argument().path(mustExist = true, canBeDir = false).multiple(required = true)
    private val signingName by option(envvar = "SIGNING_NAME").required()
    private val keychain by option(envvar = "KEYCHAIN").required()
    private val workDirectory by lazy { createTempDirectory() }

    override fun run() {
        jarPaths.forEach { jarPath ->
            FileSystems.newFileSystem(jarPath, STORED_ENTRIES).use { jar ->
                jar.getPath(NESTED_JARS_DIRECTORY).listDirectoryEntries(JAR_GLOB).forEach { nestedJarPath ->
                    nestedJarPath.signMachOBinaries()
                }
            }
        }
    }

    private fun Path.signMachOBinaries() {
        val extractedJarPath = copyTo(workDirectory.resolve(name), REPLACE_EXISTING)
        val signedBinaryPaths = FileSystems.newFileSystem(extractedJarPath).use { nestedJar ->
            nestedJar.findMachOBinaries().onEach { binaryPath -> binaryPath.sign() }
        }
        if (signedBinaryPaths.isNotEmpty()) extractedJarPath.copyTo(this, REPLACE_EXISTING)
    }

    private fun FileSystem.findMachOBinaries(): List<Path> =
        Files.walk(getPath(ROOT_DIRECTORY)).use { paths ->
            paths.filter { path -> path.isMachOBinary() }.toList()
        }

    private fun Path.isMachOBinary(): Boolean {
        if (!isRegularFile()) return false
        val header = inputStream().use { inputStream -> inputStream.readNBytes(MACH_O_HEADER_SIZE) }
        if (header.size < MACH_O_HEADER_SIZE) return false

        val headerBuffer = ByteBuffer.wrap(header)
        val magicNumber = headerBuffer.int.toUInt()
        val fatArchitectureCount = headerBuffer.int.toUInt()

        val isThinBinary = magicNumber in THIN_MAGIC_NUMBERS
        val isFatBinary = magicNumber == FAT_MAGIC_NUMBER && fatArchitectureCount < MAX_FAT_ARCHITECTURES
        return isThinBinary || isFatBinary
    }

    private fun Path.sign() {
        val extractedBinaryPath = copyTo(workDirectory.resolve(name), REPLACE_EXISTING)
        exec(
            "codesign", "--force", "--timestamp",
            "--options", "runtime",
            "--keychain", keychain,
            "--sign", "$SIGNING_CERTIFICATE_TYPE: $signingName",
            "$extractedBinaryPath",
        )
        extractedBinaryPath.copyTo(this, REPLACE_EXISTING)
    }

    private companion object {
        const val NESTED_JARS_DIRECTORY = "BOOT-INF/lib"
        const val JAR_GLOB = "*.jar"
        const val ROOT_DIRECTORY = "/"
        const val SIGNING_CERTIFICATE_TYPE = "Developer ID Application"
        const val MACH_O_HEADER_SIZE = 8
        const val FAT_MAGIC_NUMBER = 0xCAFEBABEu
        const val MAX_FAT_ARCHITECTURES = 20u
        const val THIN_32_BIT_MAGIC_NUMBER = 0xFEEDFACEu
        const val THIN_64_BIT_MAGIC_NUMBER = 0xFEEDFACFu
        const val THIN_32_BIT_REVERSED_MAGIC_NUMBER = 0xCEFAEDFEu
        const val THIN_64_BIT_REVERSED_MAGIC_NUMBER = 0xCFFAEDFEu
        val THIN_MAGIC_NUMBERS = setOf(
            THIN_32_BIT_MAGIC_NUMBER,
            THIN_64_BIT_MAGIC_NUMBER,
            THIN_32_BIT_REVERSED_MAGIC_NUMBER,
            THIN_64_BIT_REVERSED_MAGIC_NUMBER,
        )
        val STORED_ENTRIES = mapOf("compressionMethod" to "STORED")
    }
}
