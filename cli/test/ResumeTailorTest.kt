import com.github.ajalt.clikt.command.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testing.FakeInputValidator
import testing.FakeResumeGenerator
import testing.TestData
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResumeTailorTest {

    private val inputValidator = FakeInputValidator()
    private val resumeGenerator = FakeResumeGenerator()
    private val apiKeys = mutableListOf<String>()
    private val command = ResumeTailor(
        inputValidator = inputValidator,
        buildResumeGenerator = { apiKey, _ ->
            apiKeys.add(apiKey)
            resumeGenerator
        },
    )

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `given help argument when running then prints help without generating`() = runTest {
        val result = command.test(arrayOf("--help"))

        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "--website")
        assertEquals(0, resumeGenerator.calls)
    }

    @Test
    fun `given only website and api key env var when running then generates with defaults`() = runTest {
        val result = command.test(
            arrayOf("--website=${TestData.WEBSITE_URL}"),
            envvars = mapOf("API_KEY" to TestData.API_KEY),
        )

        assertEquals(0, result.statusCode)
        assertEquals(1, resumeGenerator.calls)
        assertEquals(listOf(System.getProperty("user.dir")), resumeGenerator.inputPaths)
        assertEquals(listOf(TestData.WEBSITE_URL), resumeGenerator.urls)
        assertEquals(listOf(System.getProperty("user.dir")), resumeGenerator.outputPaths)
        assertEquals(listOf("Resume"), resumeGenerator.fileNames)
        assertEquals(listOf(TestData.API_KEY), apiKeys)
        assertEquals(listOf(TestData.API_KEY), inputValidator.validatedApiKeys)
    }

    @Test
    fun `given all arguments when running then generates with them`() = runTest {
        val result = command.test(
            arrayOf(
                "--website=${TestData.WEBSITE_URL}",
                "--input=$inputDirectory",
                "--output",
                "$outputDirectory",
                "--filename=${TestData.FILE_NAME_WITH_SYMBOLS}",
                "--apikey=${TestData.API_KEY}",
                "--verbose",
            ),
        )

        assertEquals(0, result.statusCode)
        assertEquals(listOf(inputDirectory.toString()), resumeGenerator.inputPaths)
        assertEquals(listOf(TestData.WEBSITE_URL), resumeGenerator.urls)
        assertEquals(listOf(outputDirectory.toString()), resumeGenerator.outputPaths)
        assertEquals(listOf(TestData.FILE_NAME_WITH_SYMBOLS), resumeGenerator.fileNames)
        assertEquals(listOf(TestData.API_KEY), apiKeys)
    }

    @Test
    fun `given invalid website when running then fails without generating`() = runTest {
        inputValidator.isValidUrlResult = false

        val result = command.test(arrayOf("--website=whatever", "--apikey=${TestData.API_KEY}"))

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Invalid website url")
        assertEquals(0, resumeGenerator.calls)
    }

    @Test
    fun `given invalid input path when running then fails without generating`() = runTest {
        inputValidator.isValidPathResult = false

        val result = command.test(
            arrayOf("--website=${TestData.WEBSITE_URL}", "--apikey=${TestData.API_KEY}", "--input=/any/path"),
        )

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Invalid input path")
        assertEquals(0, resumeGenerator.calls)
    }

    @Test
    fun `given invalid output path when running then fails without generating`() = runTest {
        inputValidator.isValidPathResult = false

        val result = command.test(
            arrayOf("--website=${TestData.WEBSITE_URL}", "--apikey=${TestData.API_KEY}", "--output=/any/path"),
        )

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Invalid output path")
        assertEquals(0, resumeGenerator.calls)
    }

    @Test
    fun `given invalid file name when running then fails without generating`() = runTest {
        inputValidator.isValidFileNameResult = false

        val result = command.test(
            arrayOf("--website=${TestData.WEBSITE_URL}", "--apikey=${TestData.API_KEY}", "--filename=whatever"),
        )

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Invalid file name")
        assertEquals(0, resumeGenerator.calls)
    }

    @Test
    fun `given invalid api key env var when running then fails without generating`() = runTest {
        inputValidator.isValidApiKeyResult = false

        val result = command.test(
            arrayOf("--website=${TestData.WEBSITE_URL}"),
            envvars = mapOf("API_KEY" to "whatever"),
        )

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Invalid api key")
        assertEquals(0, resumeGenerator.calls)
    }

    @ParameterizedTest(name = "given {0} when running then fails without generating")
    @MethodSource("invalidArguments")
    fun `given invalid arguments when running then fails without generating`(
        description: String,
        args: Array<String>,
    ) = runTest {
        val result = command.test(args)

        assertEquals(1, result.statusCode)
        assertEquals(0, resumeGenerator.calls)
    }

    private companion object {
        @JvmStatic
        fun invalidArguments(): List<Arguments> = listOf(
            Arguments.of("missing website", arrayOf("--apikey=${TestData.API_KEY}")),
            Arguments.of("missing api key", arrayOf("--website=${TestData.WEBSITE_URL}")),
            Arguments.of(
                "verbose with value",
                arrayOf("--website=${TestData.WEBSITE_URL}", "--apikey=${TestData.API_KEY}", "--verbose=true"),
            ),
            Arguments.of("unexpected argument", arrayOf("--website=${TestData.WEBSITE_URL}", "garbage")),
            Arguments.of("website without value", arrayOf("--website")),
            Arguments.of("input without value", arrayOf("--website=${TestData.WEBSITE_URL}", "--input")),
        )
    }
}
