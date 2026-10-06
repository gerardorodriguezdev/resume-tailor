package testing

import WebsiteScanner

internal class FakeWebsiteScanner(
    var result: String = "",
) : WebsiteScanner {
    val urls: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun scan(url: String): String {
        calls++
        urls.add(url)
        return result
    }
}
