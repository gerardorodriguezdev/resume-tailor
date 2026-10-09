import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import testing.E2eTest
import testing.TestData
import testing.TestExtensions
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

@E2eTest
class EntrypointTest {

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    fun `given valid arguments when running cli then saves pdf in output directory`() = runBlocking {
        TestExtensions.writeResumeTo(inputDirectory)

        main(
            arrayOf(
                "--website=${TestData.WEBSITE_URL}",
                "--input=$inputDirectory",
                "--output=$outputDirectory",
                "--filename=${TestData.FILE_NAME}",
                "--apikey=${System.getenv("TEST_DEEPSEEK_API_KEY")}",
            )
        )

        assertTrue(TestExtensions.pdfExists(outputDirectory, TestData.FILE_NAME))
    }
}
