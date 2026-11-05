package testing

import utils.UrlOpener

internal class FakeUrlOpener(
    var result: Boolean = true,
) : UrlOpener {
    val openedUrls: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override fun open(url: String): Boolean {
        calls++
        openedUrls.add(url)
        return result
    }
}
