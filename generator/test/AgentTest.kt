import ai.koog.utils.time.KoogClock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import testing.E2eTest
import testing.TestData.CONTACT_DETAILS
import testing.TestData.RESUME
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@E2eTest
class AgentTest {

    private val agent = DeepSeekAgent(
        apiKey = System.getenv("TEST_DEEPSEEK_API_KEY"),
        clock = KoogClock.System,
        uuid = TEST_UUID,
    )

    @Test
    fun `given resume and job posting when generating then returns valid sections`() = runTest {
        val result = agent.content(mdFilesContent = RESUME, websiteContent = JOB_POSTING)

        val sections = assertDoesNotThrow { JsonSectionsParser().parse(result) }

        val header = sections.first()
        CONTACT_DETAILS.forEach { contact ->
            assertTrue(
                header.content.contains(contact),
                "Header should contain $contact but was missing"
            )
        }

        sections.forEach { section ->
            assertTrue(
                !section.content.contains("Kubernetes", ignoreCase = true),
                "Section ${section.title} should not mention Kubernetes"
            )
        }

        val numberOfWords = sections.sumOf { section ->
            section.content.split(Regex("\\s+")).count { word -> word.isNotBlank() }
        }
        assertTrue(numberOfWords < 600, "Expected less than 600 words across all sections")
    }

    private companion object {
        val TEST_UUID = Uuid.parse("123e4567-e89b-12d3-a456-426614174000")

        val JOB_POSTING = """
            Senior Backend Engineer - Kotlin

            We are looking for a senior backend engineer to design and build distributed
            services on the JVM. You will work with Kotlin, Kafka, and Kubernetes, and
            mentor other engineers. Experience with event-driven architectures and
            PostgreSQL is a plus.
        """.trimIndent()
    }
}
