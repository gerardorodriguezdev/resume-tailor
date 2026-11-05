package testing

import WebsiteScanner

internal class FakeWebsiteScanner(
    var result: String = "",
    var error: Throwable? = null,
) : WebsiteScanner {
    val urls: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun scan(url: String): String {
        calls++
        urls.add(url)
        error?.let { throw it }
        return result
    }
}
