import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testing.FakeInputValidator
import testing.TestData
import java.nio.file.Path
import kotlin.test.*

class ArgumentsParserTest {

    private val inputValidator = FakeInputValidator()
    private val parser = ArgumentsParser(inputValidator)

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `given help argument when parsing then returns help request with text`() {
        val result = assertIs<ArgumentsParser.ParsingResult.Help>(parser.parse(arrayOf("--help")))

        assertTrue(result.text.isNotBlank())
    }

    @Test
    fun `given valid website without input and output when parsing then succeeds`() {
        val result =
            assertIs<ArgumentsParser.ParsingResult.Arguments>(parser.parse(arrayOf("--website=${TestData.WEBSITE_URL}")))

        assertNull(result.inputPath)
        assertEquals(TestData.WEBSITE_URL, result.url)
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
                    "--website=${TestData.WEBSITE_URL}",
                    "--input=$inputDirectory",
                    "--output=$outputDirectory",
                    "--filename=${TestData.FILE_NAME_WITH_SYMBOLS}",
                    "--apikey=${TestData.API_KEY}",
                    "--verbose=true"
                )
            )
        )

        assertEquals(inputDirectory.toString(), result.inputPath)
        assertEquals(TestData.WEBSITE_URL, result.url)
        assertEquals(outputDirectory.toString(), result.outputPath)
        assertEquals(TestData.FILE_NAME_WITH_SYMBOLS, result.fileName)
        assertEquals(TestData.API_KEY, result.apiKey)
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
            parser.parse(arrayOf("--website=${TestData.WEBSITE_URL}", "--input=/any/path"))
        }
    }

    @Test
    fun `given invalid output path when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidPathResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=${TestData.WEBSITE_URL}", "--output=/any/path"))
        }
    }

    @Test
    fun `given invalid file name when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidFileNameResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=${TestData.WEBSITE_URL}", "--filename=whatever"))
        }
    }

    @Test
    fun `given invalid api key when parsing then throws IllegalArgumentException`() {
        inputValidator.isValidApiKeyResult = false

        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("--website=${TestData.WEBSITE_URL}", "--apikey=whatever"))
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
        @JvmStatic
        fun invalidArguments(): List<Arguments> = listOf(
            Arguments.of("blank verbose", arrayOf("--website=${TestData.WEBSITE_URL}", "--verbose=")),
            Arguments.of("non-boolean verbose", arrayOf("--website=${TestData.WEBSITE_URL}", "--verbose=a")),
            Arguments.of("uppercase boolean verbose", arrayOf("--website=${TestData.WEBSITE_URL}", "--verbose=TRUE")),
            Arguments.of("malformed argument", arrayOf("garbage")),
            Arguments.of("argument without value", arrayOf("--website")),
            Arguments.of("input argument without value", arrayOf("--input")),
        )
    }
}
