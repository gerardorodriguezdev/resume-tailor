package testing

import HtmlToPdfConverter

internal class FakeHtmlToPdfConverter(
    var result: ByteArray = ByteArray(0),
) : HtmlToPdfConverter {
    val htmlContents: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun convert(htmlContent: String): ByteArray {
        calls++
        htmlContents.add(htmlContent)
        return result
    }
}
