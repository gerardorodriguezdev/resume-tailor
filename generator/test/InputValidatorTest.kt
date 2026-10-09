import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EmptySource
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
    @EmptySource
    @MethodSource("invalidPaths")
    fun `given invalid path when validating then returns false`(path: String) {
        assertFalse(validator.isValidPath(path))
    }

    @ParameterizedTest(name = "given {0} when validating url then returns true")
    @MethodSource("validUrls")
    fun `given valid url when validating then returns true`(url: String) {
        assertTrue(validator.isValidUrl(url))
    }

    @ParameterizedTest(name = "given {0} when validating url then returns false")
    @EmptySource
    @MethodSource("invalidUrls")
    fun `given invalid url when validating then returns false`(url: String) {
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
        fun invalidPaths(): List<String> = listOf(
            " ",
            "/no/such/directory",
        )

        @JvmStatic
        fun validUrls(): List<String> = listOf(
            TestData.WEBSITE_URL,
            "HTTPS://example.com",
        )

        @JvmStatic
        fun invalidUrls(): List<String> = listOf(
            " ",
            "not a url",
            "http://example.com",
            "javascript:alert(1)",
            "file:///etc/passwd",
            "ftp://example.com",
            "https://localhost/admin",
            "https://foo.localhost/admin",
            "https://127.0.0.1/admin",
            "https://192.168.1.1/admin",
            "https://user@example.com",
            "https://",
        )
    }
}
