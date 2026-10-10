# Architecture

## Pipeline (generator)

Md files in input dir -> `WebsiteScanner` (Playwright) scrapes job posting -> `Agent` (DeepSeek via koog-agents)
produces JSON -> `SectionsParser` -> `HtmlGenerator` -> `HtmlToPdfConverter` (Playwright) -> `PdfSaver`. Wired in
`ResumeGenerator.build()`; each step is measured via the injected `TimeProvider`.

## Conventions

- One interface plus one `Default*` implementation per file, co-located in the same file (e.g. `WebsiteScanner` +
  `PlaywrightWebsiteScanner` in `WebsiteScanner.kt`). A `Kotlin*` prefix marks a thin adapter over an external library
  (`KotlinLogger`).
- No DI framework: everything is wired manually in `ResumeGenerator.build()`, the interface's companion `build()`
  factory is the only public entry point.
- Visibility: `internal` by default. Only cross-module API is public: `ResumeGenerator`, `InputValidator`, `Logger`,
  `KotlinLogger`.
- Packages: `generator` has none except `models`; `cli` has none; `app` is package-per-layer (`presentation`,
  `ui.screens`, `ui.models`, `ui.components`, `utils`); `testing` uses package `testing`.
- Invalid input is rejected with `require { }` (throws `IllegalArgumentException`); tests rely on that.
- Logging: `Logger` interface with lazy lambda messages; `KotlinLogger` delegates to kotlin-logging and gates on
  `verbose`; each app ships a `logback.xml` that routes only the `DefaultLogger` logger and silences everything else.
- GUI: keep `DispatchersProvider` injectable in the ViewModel (never call `Dispatchers` directly); generation runs on
  `DispatchersProvider.io.limitedParallelism(1)` so a new run cancels the previous one.

## Gotchas

- `Section` serialization uses the `type` discriminator (`Header`/`Content`); the order of `CONTENT_SECTION_TITLES` in
  `Constants.kt` is authoritative (fed into the prompt and validated by `JsonSectionsParser`) — changing it affects
  the prompt, `SectionsParserTest`, and both fixtures.
