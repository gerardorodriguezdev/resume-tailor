package utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal interface UpdateChecker {
    suspend fun newerVersionOrNull(): String?
}

internal class GitHubUpdateChecker(
    private val currentVersion: String?,
    private val dispatchersProvider: DispatchersProvider,
    private val httpClient: HttpClient,
) : UpdateChecker {

    override suspend fun newerVersionOrNull(): String? =
        withContext(dispatchersProvider.io) {
            try {
                findNewerVersion()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun findNewerVersion(): String? {
        if (currentVersion == null) return null

        val currentVersionParts = currentVersion.toVersionParts()
        val latestVersionParts = fetchLatestTag().toVersionParts()

        return latestVersionParts
            .takeIf { VERSION_COMPARATOR.compare(latestVersionParts, currentVersionParts) > 0 }
            ?.joinToString(".")
    }

    private suspend fun fetchLatestTag(): String {
        val response = httpClient.get(LATEST_RELEASE_API_URL)
        check(response.status.isSuccess()) { "Latest release request failed with status ${response.status}" }
        return JSON.decodeFromString<Release>(response.bodyAsText()).tagName
    }

    private fun String.toVersionParts(): List<Int> {
        val match = requireNotNull(VERSION_REGEX.matchEntire(this)) { "Invalid version: $this" }
        return match.groupValues.drop(1).map(String::toInt)
    }

    @Serializable
    private class Release(@SerialName("tag_name") val tagName: String)

    private companion object {
        const val LATEST_RELEASE_API_URL =
            "https://api.github.com/repos/gerardorodriguezdev/resume-tailor/releases/latest"
        val VERSION_REGEX = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$")
        val VERSION_COMPARATOR = compareBy<List<Int>>({ parts -> parts[0] }, { parts -> parts[1] }, { parts -> parts[2] })
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
