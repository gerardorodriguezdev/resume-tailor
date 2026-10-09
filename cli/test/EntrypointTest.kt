import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import testing.TestData
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "TEST_DEEPSEEK_API_KEY", matches = ".+")
class EntrypointTest {

    @TempDir
    lateinit var inputDirectory: Path

    @TempDir
    lateinit var outputDirectory: Path

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    fun `given valid arguments when running cli then saves pdf in output directory`() = runBlocking {
        inputDirectory.resolve("resume.md").toFile().writeText(TestData.RESUME)

        main(
            arrayOf(
                "--website=${TestData.WEBSITE_URL}",
                "--input=$inputDirectory",
                "--output=$outputDirectory",
                "--filename=${TestData.FILE_NAME}",
                "--apikey=${System.getenv("TEST_DEEPSEEK_API_KEY")}",
            )
        )

        assertTrue(File(outputDirectory.toFile(), "${TestData.FILE_NAME}.pdf").exists())
    }
}
