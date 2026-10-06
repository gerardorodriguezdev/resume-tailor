import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFailsWith

class ExtensionsTest {

    @ParameterizedTest(name = "given {0} when validating then throws IllegalArgumentException")
    @MethodSource("unsafeContent")
    fun `given unsafe content when validating then throws IllegalArgumentException`(
        description: String,
        content: String
    ) {
        assertFailsWith<IllegalArgumentException> {
            validateContent(content)
        }
    }

    @ParameterizedTest(name = "given {0} when validating then passes")
    @MethodSource("safeContent")
    fun `given safe content when validating then passes`(description: String, content: String) {
        validateContent(content)
    }

    private companion object {
        @JvmStatic
        fun unsafeContent(): List<Arguments> = listOf(
            Arguments.of("script tag", "John Doe<script>alert(1)</script>"),
            Arguments.of("opening tag", "hello <b>bold</b>"),
            Arguments.of("uppercase tag", "hello <SCRIPT>evil</SCRIPT>"),
            Arguments.of("self closing tag", "text <img src=x onerror=alert(1)>"),
            Arguments.of("html comment", "text <!-- comment -->"),
            Arguments.of("cdata", "text <![CDATA[evil]]>"),
            Arguments.of("doctype", "<!DOCTYPE html>"),
            Arguments.of("processing instruction", "<?php evil ?>"),
            Arguments.of("null character", "text\u0000more"),
            Arguments.of("control character", "text\u001Fmore"),
            Arguments.of("bidi character", "skills\u202Eevil"),
            Arguments.of("zero width character", "Kotlin\u200B, Java"),
        )

        @JvmStatic
        fun safeContent(): List<Arguments> = listOf(
            Arguments.of("plain text", "John Doe\njohn@example.com"),
            Arguments.of("comparison symbols", "x < y and y > z"),
            Arguments.of("tabs and newlines", "Kotlin,\tJava\nSQL"),
            Arguments.of("empty string", ""),
        )
    }
}
