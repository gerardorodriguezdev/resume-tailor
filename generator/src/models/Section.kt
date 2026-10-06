package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface Section {
    val title: String
    val content: String

    @Serializable
    @SerialName("Header")
    data class Header(
        override val content: String,
    ) : Section {
        override val title: String = "Contact"
    }

    @Serializable
    @SerialName("Content")
    data class Content(
        override val title: String,
        override val content: String,
    ) : Section
}
