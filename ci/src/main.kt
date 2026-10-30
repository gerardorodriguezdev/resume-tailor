import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) =
    NoOpCliktCommand(name = "ci")
        .subcommands(SignNestedBinaries(), Notarize())
        .main(args)
