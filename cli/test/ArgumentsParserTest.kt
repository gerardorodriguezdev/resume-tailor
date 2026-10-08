import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testing.FakeInputValidator
import kotlin.io.path.createTempDirectory
import kotlin.test.*

class ArgumentsParserTest {

    private val inputValidator = FakeInputValidator()
    private val parser = ArgumentsParser(inputValidator)
    private val inputDirectory = createTempDirectory("input-directory")
    private val outputDirectory = createTempDirectory("output-directory")

    @Test
    fun `given help argument when parsing then returns help request with text`() {
        val result = assertIs<ArgumentsParser.ParsingResult.Help>(parser.parse(arrayOf("--help")))

        assertTrue(result.text.isNotBlank())
    }

    @Test
    fun `given valid website without input and output when parsing then succeeds`() {
        val result =
            assertIs<ArgumentsParser.ParsingResult.Arguments>(parser.parse(arrayOf("--website=$VALID_WEBSITE")))

        assertNull(result.inputPath)
        assertEquals(VALID_WEBSITE, result.url)
        assertNull(result.outputPath)
        assertNull(result.fileName)
        assertNull(result.apiKey)
        assertNull(result.verbose)
    }

    @Test
    fun `given valid website, input and output when parsing then succeeds`() {
        val result = assertIs<ArgumentsParser.ParsingResult.Arguments>(
            parser.parse(
                arrayOf(
                    "--website=$VALID_WEBSITE",
                    "--input=$inputDirectory",
                    "--output=$outputDirectory",
                    "--filename=$VALID_FILE_NAME",
                    "--apikey=$VALID_API_KEY",
                    "--verbose=true"
                )
            )
        )

        assertEquals(inputDirectory.toString(), result.inputPath)
        assertEquals(VALID_WEBSITE, result.url)
        assertEquals(outputDirectory.toString(), result.outputPath)
        assertEquals(VALID_FILE_NAME, result.fileName)
        assertEquals(VALID_API_KEY, result.apiKey)
        assertEquals(true, result.verbose)
    }

    @Test
    fun `given invalid website when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidUrlResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=whatever"))
        }
    }

    @Test
    fun `given invalid input path when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidPathResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=$VALID_WEBSITE", "--input=/any/path"))
        }
    }

    @Test
    fun `given invalid output path when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidPathResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=$VALID_WEBSITE", "--output=/any/path"))
        }
    }

    @Test
    fun `given invalid file name when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidFileNameResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=$VALID_WEBSITE", "--filename=whatever"))
        }
    }

    @Test
    fun `given invalid api key when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidApiKeyResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=$VALID_WEBSITE", "--apikey=whatever"))
        }
    }

    @ParameterizedTest(name = "given {0} when parsing then throws IllegalArgumentException")
    @MethodSource("invalidArguments")
    fun `given invalid arguments when parsing then throws IllegalArgumentException`(
        description: String,
        args: Array<String>
    ) {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(args)
        }
    }

    private companion object {
        const val VALID_WEBSITE = "https://example.com"
        const val VALID_API_KEY = "sk-" + "0123456789abcdef0123456789abcdef"
        const val VALID_FILE_NAME = "My_Resume-v2"

        @JvmStatic
        fun invalidArguments(): List<Arguments> = listOf(
            Arguments.of("blank verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=")),
            Arguments.of("non-boolean verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=a")),
            Arguments.of("uppercase boolean verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=TRUE")),
            Arguments.of("malformed argument", arrayOf("garbage")),
            Arguments.of("argument without value", arrayOf("--website")),
            Arguments.of("input argument without value", arrayOf("--input")),
        )
    }
}
