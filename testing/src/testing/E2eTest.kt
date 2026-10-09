package testing

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "TEST_DEEPSEEK_API_KEY", matches = ".+")
annotation class E2eTest
