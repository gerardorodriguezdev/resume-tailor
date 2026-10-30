import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

internal class Notarize : CliktCommand() {
    private val apiKey by option(envvar = "APPLE_API_KEY").required()
    private val apiKeyId by option(envvar = "APPLE_API_KEY_ID").required()
    private val apiIssuerId by option(envvar = "APPLE_API_ISSUER_ID").required()

    private val notaryCredentials by lazy {
        val apiKeyFile = createTempFile(suffix = API_KEY_FILE_SUFFIX).apply { writeText(apiKey) }
        arrayOf("--key", "$apiKeyFile", "--key-id", apiKeyId, "--issuer", apiIssuerId)
    }

    override fun run() {
        Path(DISTRIBUTION_DIRECTORY).listDirectoryEntries(INSTALLERS_GLOB).forEach { installerPath ->
            val submission = submitAndWaitForNotarization(installerPath)
            submission.requireAccepted(installerPath)
            stapleNotarizationTicket(installerPath)
        }
    }

    private fun submitAndWaitForNotarization(installerPath: Path): Submission {
        val submissionJson = exec(
            "xcrun", "notarytool", "submit", "$installerPath", *notaryCredentials,
            "--wait",
            "--output-format", "json",
        )
        return JSON.decodeFromString(submissionJson)
    }

    private fun Submission.requireAccepted(installerPath: Path) {
        if (status == Submission.Status.Accepted) return
        val notarizationLog = exec("xcrun", "notarytool", "log", id, *notaryCredentials)
        echo(notarizationLog)
        error("Notarization of $installerPath failed with status $status: $message")
    }

    private fun stapleNotarizationTicket(installerPath: Path) {
        exec("xcrun", "stapler", "staple", "$installerPath")
    }

    @Serializable
    private class Submission(val id: String, val status: Status, val message: String) {
        @Serializable
        enum class Status {
            Accepted,

            @SerialName("In Progress")
            InProgress,
            Invalid,
            Rejected,
        }
    }

    private companion object {
        const val DISTRIBUTION_DIRECTORY = "dist"
        const val INSTALLERS_GLOB = "*.{dmg,pkg}"
        const val API_KEY_FILE_SUFFIX = ".p8"
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
