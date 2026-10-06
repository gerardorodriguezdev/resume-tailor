package testing

import SectionsParser
import models.Section

internal class FakeSectionsParser(
    var result: List<Section> = emptyList(),
) : SectionsParser {
    val contents: List<String>
        field = MutableList(0) { "" }
    var calls: Int = 0
        private set

    override fun parse(content: String): List<Section> {
        calls++
        contents.add(content)
        return result
    }
}
