# AGENTS.md

## Build system

- Kotlin Toolchain (Amper) project. Always use the checked-in wrapper `./kotlin` from the repo root. There is no
  Gradle — never run `gradle`.
- `project.yaml` lists modules; each module has a `module.yaml`. Dependencies come from `libs.versions.toml` referenced
  as `$libs.<key>`; module deps use `//module` paths.
- Modules: `generator` (jvm/lib, core pipeline), plus two apps each in its own module: `cli` (jvm/app, mainClass
  `EntrypointKt`) and `app` (Compose Desktop GUI). Both reuse the `ResumeGenerator` from `generator`.

## Commands

- Build: `./kotlin build`
- All tests: `./kotlin check`
- One module: `./kotlin test -m generator`
- One test class: `./kotlin test -m generator --include-classes 'ResumeGeneratorTest'`
- One test method: `./kotlin test --include-test '<FQN with package and class>'`
- Run CLI: `./kotlin run -m cli -- --website=<url> [--input --output --filename --apikey --verbose]`
- List modules/checks: `./kotlin show modules`, `./kotlin show checks`

## Pipeline (generator)

Md files in input dir -> `WebsiteScanner` (Playwright) scrapes job posting -> `Agent` (DeepSeek via koog-agents)
produces JSON -> `SectionsParser` -> `HtmlGenerator` -> `HtmlToPdfConverter` (Playwright) -> `PdfSaver`. Wired in
`ResumeGenerator.build()`.

## Gotchas

- `HtmlToPdfConverterTest` compares against fixture `generator/testResources/expected-resume.pdf` (date metadata
  stripped). Changing `HtmlGenerator` output requires regenerating this fixture.

## Conventions

- Tests: `kotlin.test` + `runTest` (coroutines-test), backtick names `given ... when ... then ...`, hand-rolled fakes in
  `generator/test/testing/` (no mocking library).
- Commit messages: short present-tense summary ("Adds ...", "Updates ...", "Fixes ...").
