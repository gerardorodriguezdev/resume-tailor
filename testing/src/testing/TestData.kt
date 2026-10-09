package testing

import models.Section

object TestData {

    const val CONTACT_EMAIL = "john.doe@example.com"
    const val CONTACT_PHONE = "+1 555 1234"
    const val CONTACT_LINKEDIN = "linkedin.com/in/johndoe"
    const val CONTACT_GITHUB = "github.com/johndoe"

    val CONTACT_DETAILS = listOf(CONTACT_EMAIL, CONTACT_PHONE, CONTACT_LINKEDIN, CONTACT_GITHUB)

    const val WEBSITE_URL = "https://example.com"
    const val JOB_URL = "https://example.com/job"
    const val API_KEY = "sk-" + "0123456789abcdef0123456789abcdef"
    const val FILE_NAME = "Resume"
    const val MY_RESUME_FILE_NAME = "MyResume"
    const val FILE_NAME_WITH_SYMBOLS = "My_Resume-v2"
    const val INPUT_PATH = "/tmp/input"
    const val OUTPUT_PATH = "/tmp/output"

    val PDF_BYTES = byteArrayOf(1, 2, 3, 4, 5)

    val RESUME = """
        # John Doe
        ${CONTACT_DETAILS.joinToString(" | ")}

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

    val testSections: List<Section> = listOf(
        Section.Header("$CONTACT_EMAIL | $CONTACT_PHONE | $CONTACT_LINKEDIN | $CONTACT_GITHUB"),
        Section.Content(
            "summary",
            lines(
                "Senior software engineer with 10+ years of experience building scalable backend systems.",
                "Specialized in Kotlin, distributed systems, and event-driven architectures.",
                "Passionate about developer experience, observability, and mentoring teams.",
            ),
        ),
        Section.Content(
            "experience",
            lines(
                "Senior Software Engineer - Acme Corp (2021 - Present)",
                "- Led the migration of a monolith to event-driven microservices, cutting p99 latency by 40%.",
                "- Designed a multi-tenant billing pipeline processing 5M invoices per day.",
                "- Mentored a team of 6 engineers and drove adoption of Kotlin across the org.",
                "- Introduced SLO-based alerting that reduced on-call noise by 60%.",
                "- Owned the roadmap for the payments platform serving 20 markets.",
                "- Ran load tests before every major release, preventing 3 production incidents.",
                "Software Engineer - Globex (2018 - 2021)",
                "- Built a real-time fraud detection service scoring 10k events per second.",
                "- Reduced database costs by 35% by introducing read replicas and caching.",
                "- Shipped a partner API used by 200+ external integrations.",
                "- Automated load testing, catching regressions before every release.",
                "- Ran quarterly architecture reviews for the backend guild.",
                "Software Engineer - Initech (2016 - 2018)",
                "- Developed inventory management features for a SaaS platform.",
                "- Improved CI pipelines, cutting build times from 25 to 8 minutes.",
                "- Implemented role-based access control across 4 products.",
                "- Collaborated with design to ship an accessible UI component library.",
                "Junior Developer - Umbrella (2014 - 2016)",
                "- Maintained internal reporting tools used by 15 departments.",
                "- Wrote integration tests that raised coverage from 40% to 80%.",
                "- Migrated legacy PHP endpoints to a modern REST API.",
            ),
        ),
        Section.Content(
            "projects",
            lines(
                "OpenLedger - Double-entry bookkeeping library for Kotlin",
                "- Maintained an OSS library with 2k stars and 40 contributors.",
                "- Published 30+ releases with a fully automated changelog pipeline.",
                "Dashboards-as-Code - Internal observability toolkit",
                "- Lets teams define Grafana dashboards in YAML and review them in PRs.",
                "- Adopted by 12 teams and cut dashboard drift to zero.",
                "DevBox - Reproducible development environments",
                "- Built on Docker Compose with hot reload for 5 services.",
                "- Onboarding time for new engineers dropped from 2 days to 2 hours.",
            ),
        ),
        Section.Content(
            "skills",
            lines(
                "Languages: Kotlin, Java, TypeScript, Python, SQL",
                "Frameworks: Ktor, Spring Boot, React, Next.js",
                "Infrastructure: Kubernetes, Terraform, AWS, GCP",
                "Data: PostgreSQL, Kafka, Redis, Elasticsearch",
                "Observability: Prometheus, Grafana, OpenTelemetry",
                "Practices: TDD, DDD, event sourcing, CI/CD",
                "Tools: Docker, Git, GitHub Actions, Playwright",
                "Other: technical writing, public speaking, mentoring",
            ),
        ),
        Section.Content(
            "education",
            lines(
                "MSc Computer Science - State University (2012 - 2014)",
                "- Thesis on consistency models in distributed key-value stores.",
                "BSc Software Engineering - City College (2008 - 2012)",
                "- Graduated cum laude, GPA 3.8/4.0.",
            ),
        ),
        Section.Content(
            "certifications",
            lines(
                "AWS Certified Solutions Architect - Professional",
                "Kubernetes Certified Application Developer (CKAD)",
                "Google Cloud Professional Data Engineer",
                "Certified Scrum Master (CSM)",
                "Oracle Certified Professional, Java SE 11",
                "Kotlin Developer Certification (JetBrains)",
            ),
        ),
        Section.Content(
            "open source",
            lines(
                "kotlinx-serialization - Contributor (2019 - Present)",
                "- Added support for custom serial names and binary formats.",
                "ktor - Contributor (2020 - Present)",
                "- Improved WebSocket reconnect behavior and documentation.",
                "kotest - Contributor (2021 - Present)",
                "- Added new matchers and fixed flaky property tests.",
                "awesome-kotlin - Maintainer (2022 - Present)",
                "- Curated 400+ entries and automated link checking.",
            ),
        ),
        Section.Content(
            "speaking",
            lines(
                "\"Kafka at Scale: Lessons from 5M Events per Second\" - KotlinConf (2023)",
                "\"SLOs That Don't Page You at 3am\" - DevOpsDays (2022)",
                "\"Migrating a Monolith Without Losing Sleep\" - DDD Europe (2021)",
                "\"Property-Based Testing in Practice\" - JBCNConf (2020)",
                "Guest lecturer, Distributed Systems, State University (2019 - Present)",
            ),
        ),
        Section.Content(
            "awards",
            lines(
                "Acme Corp Engineering Excellence Award (2023)",
                "Speaker of the Year, local Kotlin user group (2022)",
                "Top 10 finalist, Global Hackathon for Good (2020)",
                "Employee of the Quarter, Globex (2019)",
            ),
        ),
        Section.Content(
            "languages",
            lines(
                "English - native",
                "Spanish - professional working proficiency",
                "German - elementary",
            ),
        ),
    )

    private fun lines(vararg items: String): String = items.joinToString("\n")
}
