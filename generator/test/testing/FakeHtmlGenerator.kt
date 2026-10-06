package testing

import HtmlGenerator
import models.Section

internal class FakeHtmlGenerator(
    var result: String = "",
) : HtmlGenerator {
    val sections: List<List<Section>>
        field = MutableList(0) { emptyList() }
    var calls: Int = 0
        private set

    override fun generate(sections: List<Section>): String {
        calls++
        this.sections.add(sections)
        return result
    }
}
