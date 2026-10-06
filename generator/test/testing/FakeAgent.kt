package testing

import Agent

internal class FakeAgent(
    var result: String = "",
) : Agent {
    val mdFilesContents: List<String>
        field = MutableList(0) { "" }
    val websiteContents: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override suspend fun content(mdFilesContent: String, websiteContent: String): String {
        calls++
        mdFilesContents.add(mdFilesContent)
        websiteContents.add(websiteContent)
        return result
    }
}
