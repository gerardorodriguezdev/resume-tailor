internal fun validateContent(content: String) {
    require(!UNSAFE_CONTENT.containsMatchIn(content)) { "Unsafe content detected" }
}

private val UNSAFE_CONTENT = Regex(
    "</?[A-Za-z][^>]*>|" +
            "<!--|" +
            "<!\\[CDATA\\[|" +
            "<!DOCTYPE\\b|" +
            "<\\?|" +
            "[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F" +
            "\\u200B-\\u200F\\u202A-\\u202E\\u2066-\\u2069\\uFEFF]",
    setOf(RegexOption.IGNORE_CASE),
)
