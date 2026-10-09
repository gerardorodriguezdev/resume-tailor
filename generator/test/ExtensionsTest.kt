import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFailsWith

class ExtensionsTest {

    @ParameterizedTest(name = "given {0} when validating then throws IllegalArgumentException")
    @MethodSource("unsafeContent")
    fun `given unsafe content when validating then throws IllegalArgumentException`(content: String) {
        assertFailsWith<IllegalArgumentException> {
            validateContent(content)
        }
    }

    @ParameterizedTest(name = "given {0} when validating then passes")
    @MethodSource("safeContent")
    fun `given safe content when validating then passes`(content: String) {
        validateContent(content)
    }

    companion object {

        @JvmStatic
        fun unsafeContent(): List<String> = listOf(
            "John Doe<script>alert(1)</script>",
            "hello <b>bold</b>",
            "hello <SCRIPT>evil</SCRIPT>",
            "text <img src=x onerror=alert(1)>",
            "text <!-- comment -->",
            "text <![CDATA[evil]]>",
            "<!DOCTYPE html>",
            "<?php evil ?>",
            "text\u0000more",
            "text\u001Fmore",
            "skills\u202Eevil",
            "Kotlin\u200B, Java",
        )

        @JvmStatic
        fun safeContent(): List<String> = listOf(
            "John Doe\njohn@example.com",
            "x < y and y > z",
            "Kotlin,\tJava\nSQL",
            "",
        )
    }
}
