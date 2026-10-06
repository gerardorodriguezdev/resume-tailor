import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.io.path.createTempDirectory
import kotlin.test.*

class ArgumentsParserTest {

    private val parser = ArgumentsParser()
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
        const val VALID_API_KEY = "sk-abc123_XYZ-456"
        const val VALID_FILE_NAME = "My_Resume-v2"

        @JvmStatic
        fun invalidArguments(): List<Arguments> = listOf(
            Arguments.of("blank website", arrayOf("--website=")),
            Arguments.of("file scheme website", arrayOf("--website=file:///etc/passwd")),
            Arguments.of("localhost website", arrayOf("--website=https://localhost/admin")),
            Arguments.of("http website", arrayOf("--website=http://example.com")),
            Arguments.of("loopback website", arrayOf("--website=https://127.0.0.1/admin")),
            Arguments.of("javascript scheme website", arrayOf("--website=javascript:alert(1)")),
            Arguments.of("missing input path", arrayOf("--website=$VALID_WEBSITE", "--input=/no/such/directory")),
            Arguments.of("blank input path", arrayOf("--website=$VALID_WEBSITE", "--input=")),
            Arguments.of("missing output path", arrayOf("--website=$VALID_WEBSITE", "--output=/no/such/directory")),
            Arguments.of("blank output path", arrayOf("--website=$VALID_WEBSITE", "--output=")),
            Arguments.of("blank file name", arrayOf("--website=$VALID_WEBSITE", "--filename=")),
            Arguments.of(
                "file name with path separator",
                arrayOf("--website=$VALID_WEBSITE", "--filename=../Resume")
            ),
            Arguments.of(
                "file name starting with dot",
                arrayOf("--website=$VALID_WEBSITE", "--filename=.hidden")
            ),
            Arguments.of("blank api key", arrayOf("--website=$VALID_WEBSITE", "--apikey=")),
            Arguments.of(
                "api key with spaces and symbols",
                arrayOf("--website=$VALID_WEBSITE", "--apikey=sk-abc123; rm -rf /")
            ),
            Arguments.of("api key with special characters", arrayOf("--website=$VALID_WEBSITE", "--apikey=key!@#")),
            Arguments.of("blank verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=")),
            Arguments.of("non-boolean verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=a")),
            Arguments.of("uppercase boolean verbose", arrayOf("--website=$VALID_WEBSITE", "--verbose=TRUE")),
            Arguments.of("malformed argument", arrayOf("garbage")),
            Arguments.of("argument without value", arrayOf("--website")),
            Arguments.of("input argument without value", arrayOf("--input")),
        )
    }
}
