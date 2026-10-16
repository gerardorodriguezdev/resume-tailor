# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Resume Tailor: scrapes a job posting, tailors a Markdown resume with DeepSeek, and exports a PDF. Kotlin CLI + Compose
Desktop GUI.

## Build system

- Kotlin Toolchain (Amper) project. Always use the checked-in wrapper `./kotlin` from the repo root. There is no
  Gradle — never run `gradle`.
- `project.yaml` lists modules; each has a `module.yaml`. Library versions live in `libs.versions.toml`, referenced as
  `$libs.<key>`; module deps use `//module` paths.
- The only registered check is `tests` — no lint or formatter, so style is enforced by review.
- Modules:
    - `generator` — jvm/lib, core pipeline (no `main`).
    - `cli` — jvm/app, `mainClass: EntrypointKt`.
    - `app` — jvm/app, Compose Desktop GUI.
    - `testing` — jvm/lib, shared test utilities, consumed via `test-dependencies: - //testing`.
- Adding a module: create its `module.yaml`, list it in `project.yaml`, add new library versions to
  `libs.versions.toml`.

## Commands

- Build: `./kotlin build`
- All tests: `./kotlin check`
- One module: `./kotlin test -m generator`
- One test class: `./kotlin test -m generator --include-classes 'ResumeGeneratorTest'`
- One test method: `./kotlin test --include-test '<FQN with package and class>'`
- E2E tests (skipped otherwise): `TEST_DEEPSEEK_API_KEY=... ./kotlin check`
- Run CLI: `./kotlin run -m cli -- --website=<url> [--input --output --filename --apikey --verbose]` (arguments are
  `--key=value`; input/output default to the working dir, the API key falls back to the `API_KEY` env var)
- Run GUI: `./kotlin run -m app`; hot reload: `./kotlin run -m app --compose-hot-reload-mode` (auto-reload on save only
  works via the IDE's "Run with Compose Hot Reload"; the CLI does not watch files)
- List modules/checks: `./kotlin show modules`, `./kotlin show checks`

## Architecture

Pipeline in `generator`: md files in input dir -> `WebsiteScanner` (Playwright) scrapes the job posting -> `Agent`
(DeepSeek via koog-agents) produces JSON -> `SectionsParser` -> `HtmlGenerator` -> `HtmlToPdfConverter` (Playwright)
-> `PdfSaver`. Wired in `ResumeGenerator.build()`; each step is timed via the injected `TimeProvider`. `cli` and `app`
are thin front-ends that validate input with `InputValidator` and call `ResumeGenerator`.

Conventions:

- One interface plus its `Default*` implementation per file (e.g. `WebsiteScanner` + `PlaywrightWebsiteScanner` in
  `WebsiteScanner.kt`). A `Kotlin*` prefix marks a thin adapter over an external library (`KotlinLogger`).
- No DI framework: everything is wired manually in `ResumeGenerator.build()`; the interface's companion `build()`
  factory is the only public entry point.
- Visibility is `internal` by default. Only cross-module API is public: `ResumeGenerator`, `InputValidator`, `Logger`,
  `KotlinLogger`.
- Packages: `generator` has none except `models`; `cli` has none; `app` is package-per-layer (`presentation`,
  `ui.screens`, `ui.models`, `ui.components`, `utils`); `testing` uses package `testing`.
- Invalid input is rejected with `require { }` (throws `IllegalArgumentException`); tests rely on that.
- Logging: `Logger` interface with lazy lambda messages; `KotlinLogger` delegates to kotlin-logging and gates on
  `verbose`; each app's `logback.xml` routes only the `DefaultLogger` logger and silences everything else.
- GUI: keep `DispatchersProvider` injectable in the ViewModel (never call `Dispatchers` directly); generation runs on
  `DispatchersProvider.io.limitedParallelism(1)` so a new run cancels the previous one.

## Security invariants (do not weaken)

- `InputValidator.isValidUrl` is SSRF-safe: https only, no userinfo, host must resolve and must not be
  loopback/link-local/site-local/any-local. All new URL inputs must go through it.
- `PlaywrightWebsiteScanner` aborts every route that is neither a navigation request nor a validated URL, extracts text
  only from `<main>`/`<article>` (falling back to `<body>`), then runs it through `validateContent`.
- `validateContent` (`generator/src/Extensions.kt`) rejects raw HTML tags, comments, CDATA, DOCTYPE, and control /
  zero-width / bidi characters. LLM output is untrusted and must be re-validated before rendering.
- `MdFilesReader` parses md with commonmark and allows only a fixed node allowlist; anything else throws.
- `Agent`'s system prompt declares `<user-resume>`/`<job-posting>` content untrusted and demands a bare JSON array;
  `SectionsParser` re-validates section structure, order, and content.
- `HtmlToPdfConverter` disables JavaScript, blocks service workers, and aborts all routes.
- API keys: strict regex validation (`sk-` + 32 hex), never logged.

## Testing

- `kotlin.test` assertions on JUnit 5. Coroutine tests use `runTest`, e2e uses `runBlocking`, Compose UI tests use
  `runComposeUiTest` with `@OptIn(ExperimentalTestApi::class)`.
- Test names are backtick sentences: `given ... when ... then ...`.
- Hand-rolled fakes, no mocking library:
    - Fakes for `generator` public interfaces shared across modules go in the `testing` module, public
      (`FakeResumeGenerator`, `FakeInputValidator`).
    - Fakes for `internal` implementations used by one module go in `<module>/test/testing`, `internal`.
    - Shape: configurable `var result` / `var error`, recorded argument lists, and `var calls: Int = 0` with
      `private set`.
- Shared fixtures: sample data in `testing.TestData`; helpers in `testing.TestExtensions` (`writeResumeTo`,
  `pdfExists`, `readTestResource`, `readTestResourceText`).
- Parameterized tests: `@ParameterizedTest(name = "given {0} when ... then ...")` + `@MethodSource` with a `@JvmStatic`
  provider in the companion object.
- E2E tests are annotated with `testing.E2eTest` (`@Tag("e2e")` + enabled only if `TEST_DEEPSEEK_API_KEY` is set).
  Always add `@Timeout` (120-180s) to e2e and Playwright-backed tests.
- `testing/resources/junit-platform.properties` enables parallel execution — keep tests independent, no shared mutable
  global state.
- Compose UI: assert via `testTag`; tag constants live in `MainScreenTestTags` with a `MainScreen.` prefix.
- Use `@TempDir` for temp dirs and an in-process `HttpServer` for local web fixtures (see `WebsiteScannerTest`).

## Style

- Trailing commas on multi-line argument and parameter lists.
- Constants: `private companion object` when local to a class; file-level `internal` (e.g. `Constants.kt`) when shared
  within the module.
- Code is comment-free; `//language=...` injections for embedded prompts/HTML are fine.
- Commit messages: short present-tense summary ("Adds ...", "Updates ...", "Fixes ...").

## Gotchas

- `Section` serialization uses the `type` discriminator (`Header`/`Content`). The order of `CONTENT_SECTION_TITLES` in
  `Constants.kt` is authoritative (fed into the prompt and validated by `JsonSectionsParser`) — changing it affects the
  prompt, `SectionsParserTest`, and both fixtures.
- `HtmlGeneratorTest` and `HtmlToPdfConverterTest` compare against `generator/testResources/expected-resume.html` /
  `expected-resume.pdf` (PDF date metadata stripped). Changing `HtmlGenerator` output requires regenerating both.
