# Style

- Trailing commas on multi-line argument and parameter lists.
- Constants live in a `private companion object` when local to a class, or file-level `internal` (e.g.
  `Constants.kt`) when shared within the module.
- Code is comment-free; `//language=...` injections for embedded prompts/HTML are fine.
- Commit messages: short present-tense summary ("Adds ...", "Updates ...", "Fixes ...").
