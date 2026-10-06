import ai.koog.utils.time.KoogClock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import testing.TestData.CONTACT_DETAILS
import testing.TestData.CONTACT_EMAIL
import testing.TestData.CONTACT_GITHUB
import testing.TestData.CONTACT_LINKEDIN
import testing.TestData.CONTACT_PHONE
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "TEST_DEEPSEEK_API_KEY", matches = ".+")
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

        val RESUME = """
            # John Doe
            $CONTACT_EMAIL | $CONTACT_PHONE | $CONTACT_LINKEDIN | $CONTACT_GITHUB

            ## Summary
            Senior software engineer with 10+ years of experience building scalable backend systems.

            ## Experience
            Senior Software Engineer - Acme Corp (2021 - Present)
            - Led the migration of a monolith to event-driven microservices, cutting p99 latency by 40%.
            - Mentored a team of 6 engineers and drove adoption of Kotlin across the org.

            Software Engineer - Globex (2018 - 2021)
            - Built a real-time fraud detection service scoring 10k events per second.

            ## Skills
            Languages: Kotlin, Java, TypeScript, Python, SQL
            Infrastructure: Terraform, AWS
            Data: PostgreSQL, Kafka, Redis

            ## Projects
            OpenLedger - Double-entry bookkeeping library for Kotlin
            - Maintained an OSS library with 2k stars and 40 contributors.

            ## Languages
            English - native
            Spanish - professional working proficiency

            ## Publications
            "Kafka at Scale: Lessons from 5M Events per Second" - InfoQ (2023)

            ## Presentations
            "Migrating a Monolith Without Losing Sleep" - DDD Europe (2021)

            ## Courses
            Kotlin Coroutines Deep Dive - Online course (2022)

            ## Education
            MSc Computer Science - State University (2012 - 2014)
            BSc Software Engineering - City College (2008 - 2012)
        """.trimIndent()

        val JOB_POSTING = """
            Senior Backend Engineer - Kotlin

            We are looking for a senior backend engineer to design and build distributed
            services on the JVM. You will work with Kotlin, Kafka, and Kubernetes, and
            mentor other engineers. Experience with event-driven architectures and
            PostgreSQL is a plus.
        """.trimIndent()
    }
}
