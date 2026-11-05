package utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testing.TestDispatchersProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateCheckerTest {

    @ParameterizedTest(name = "given current {0} and latest {1} when checking then returns {2}")
    @MethodSource("versions")
    fun `given current and latest versions when checking then returns newer version`(
        currentVersion: String,
        latestTag: String,
        expected: String?,
    ) = runTest {
        val engine = mockEngine(HttpStatusCode.OK, latestTag)

        val result = createUpdateChecker(currentVersion, engine).newerVersionOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun `given no current version when checking then returns null without requesting`() = runTest {
        val engine = mockEngine(HttpStatusCode.OK, "v1.0.1")

        val result = createUpdateChecker(null, engine).newerVersionOrNull()

        assertNull(result)
        assertTrue(engine.requestHistory.isEmpty())
    }

    @Test
    fun `given failing server when checking then returns null`() = runTest {
        val engine = mockEngine(HttpStatusCode.InternalServerError, "v1.0.1")

        val result = createUpdateChecker("1.0.0", engine).newerVersionOrNull()

        assertNull(result)
    }

    private fun TestScope.createUpdateChecker(currentVersion: String?, engine: MockEngine) =
        GitHubUpdateChecker(
            currentVersion = currentVersion,
            dispatchersProvider = TestDispatchersProvider(StandardTestDispatcher(testScheduler)),
            httpClient = HttpClient(engine),
        )

    private fun mockEngine(status: HttpStatusCode, tag: String) =
        MockEngine {
            respond(
                content = """{"tag_name":"$tag","name":"$tag"}""",
                status = status,
            )
        }

    private companion object {
        @JvmStatic
        fun versions(): List<Arguments> = listOf(
            Arguments.of("1.0.0", "v1.0.1", "1.0.1"),
            Arguments.of("1.9.0", "v1.10.0", "1.10.0"),
            Arguments.of("1.0.1", "v1.0.1", null),
            Arguments.of("1.1.0", "v1.0.9", null),
        )
    }
}
