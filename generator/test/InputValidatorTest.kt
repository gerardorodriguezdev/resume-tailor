import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testing.TestData
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputValidatorTest {

    private val validator = DefaultInputValidator()

    @TempDir
    lateinit var directory: Path

    @TempDir
    lateinit var fileDirectory: Path

    @ParameterizedTest(name = "given {0} when validating api key then returns true")
    @MethodSource("validApiKeys")
    fun `given valid api key when validating then returns true`(description: String, apiKey: String) {
        assertTrue(validator.isValidApiKey(apiKey))
    }

    @ParameterizedTest(name = "given {0} when validating api key then returns false")
    @MethodSource("invalidApiKeys")
    fun `given invalid api key when validating then returns false`(description: String, apiKey: String) {
        assertFalse(validator.isValidApiKey(apiKey))
    }

    @ParameterizedTest(name = "given {0} when validating file name then returns true")
    @MethodSource("validFileNames")
    fun `given valid file name when validating then returns true`(description: String, fileName: String) {
        assertTrue(validator.isValidFileName(fileName))
    }

    @ParameterizedTest(name = "given {0} when validating file name then returns false")
    @MethodSource("invalidFileNames")
    fun `given invalid file name when validating then returns false`(description: String, fileName: String) {
        assertFalse(validator.isValidFileName(fileName))
    }

    @Test
    fun `given existing directory path when validating then returns true`() {
        assertTrue(validator.isValidPath(directory.toString()))
    }

    @Test
    fun `given existing file path when validating then returns false`() {
        val file = fileDirectory.resolve("file.txt")
        file.toFile().writeText("content")

        assertFalse(validator.isValidPath(file.toString()))
    }

    @ParameterizedTest(name = "given {0} when validating path then returns false")
    @MethodSource("invalidPaths")
    fun `given invalid path when validating then returns false`(description: String, path: String) {
        assertFalse(validator.isValidPath(path))
    }

    @ParameterizedTest(name = "given {0} when validating url then returns true")
    @MethodSource("validUrls")
    fun `given valid url when validating then returns true`(description: String, url: String) {
        assertTrue(validator.isValidUrl(url))
    }

    @ParameterizedTest(name = "given {0} when validating url then returns false")
    @MethodSource("invalidUrls")
    fun `given invalid url when validating then returns false`(description: String, url: String) {
        assertFalse(validator.isValidUrl(url))
    }

    private companion object {
        @JvmStatic
        fun validApiKeys(): List<Arguments> = listOf(
            Arguments.of("deepseek key", TestData.API_KEY),
            Arguments.of("uppercase hex", "sk-${"A".repeat(32)}"),
            Arguments.of("mixed case hex", "sk-${"aBcDeF".repeat(5)}aB"),
        )

        @JvmStatic
        fun invalidApiKeys(): List<Arguments> = listOf(
            Arguments.of("empty string", ""),
            Arguments.of("blank string", " "),
            Arguments.of("missing prefix", TestData.API_KEY.removePrefix("sk-")),
            Arguments.of("uppercase prefix", "SK-${TestData.API_KEY.removePrefix("sk-")}"),
            Arguments.of("short hex", TestData.API_KEY.dropLast(2)),
            Arguments.of("long hex", TestData.API_KEY + "aa"),
            Arguments.of("non-hex characters", "sk-g4f927f3ee0a4361be38e051a4ed88f1"),
            Arguments.of("underscore separators", "sk-abc123_XYZ-456"),
            Arguments.of("spaces and symbols", "sk-abc123; rm -rf /"),
            Arguments.of("dot", "key.with.dot"),
        )

        @JvmStatic
        fun validFileNames(): List<Arguments> = listOf(
            Arguments.of("single character", "R"),
            Arguments.of("letters and digits", "Resume2"),
            Arguments.of("letters, digits and symbols", TestData.FILE_NAME_WITH_SYMBOLS),
            Arguments.of("extension", "resume.pdf"),
            Arguments.of("128 characters", "a".repeat(128)),
        )

        @JvmStatic
        fun invalidFileNames(): List<Arguments> = listOf(
            Arguments.of("empty string", ""),
            Arguments.of("blank string", " "),
            Arguments.of("starting with dot", ".hidden"),
            Arguments.of("starting with hyphen", "-Resume"),
            Arguments.of("starting with underscore", "_Resume"),
            Arguments.of("path separator", "../Resume"),
            Arguments.of("absolute path", "/etc/passwd"),
            Arguments.of("inner path separator", "Resume/path"),
            Arguments.of("129 characters", "a".repeat(129)),
        )

        @JvmStatic
        fun invalidPaths(): List<Arguments> = listOf(
            Arguments.of("empty string", ""),
            Arguments.of("blank string", " "),
            Arguments.of("non-existent directory", "/no/such/directory"),
        )

        @JvmStatic
        fun validUrls(): List<Arguments> = listOf(
            Arguments.of("https url", TestData.WEBSITE_URL),
            Arguments.of("uppercase scheme", "HTTPS://example.com"),
        )

        @JvmStatic
        fun invalidUrls(): List<Arguments> = listOf(
            Arguments.of("empty string", ""),
            Arguments.of("plain text", "not a url"),
            Arguments.of("http scheme", "http://example.com"),
            Arguments.of("javascript scheme", "javascript:alert(1)"),
            Arguments.of("file scheme", "file:///etc/passwd"),
            Arguments.of("ftp scheme", "ftp://example.com"),
            Arguments.of("localhost host", "https://localhost/admin"),
            Arguments.of("localhost subdomain", "https://foo.localhost/admin"),
            Arguments.of("loopback address", "https://127.0.0.1/admin"),
            Arguments.of("site local address", "https://192.168.1.1/admin"),
            Arguments.of("user info", "https://user@example.com"),
            Arguments.of("missing host", "https://"),
        )
    }
}
