# AGENTS.md

Resume Tailor: scrapes a job posting, tailors a resume with DeepSeek, and exports a PDF. Kotlin CLI + Compose Desktop
GUI.

## Build system

- Kotlin Toolchain (Amper) project. Always use the checked-in wrapper `./kotlin` from the repo root. There is no
  Gradle — never run `gradle`.
- `project.yaml` lists modules; each module has a `module.yaml`. Dependencies come from `libs.versions.toml` referenced
  as `$libs.<key>`; module deps use `//module` paths.
- The only registered check is `tests` — there is no lint or formatter check, so style enforcement relies on review.
- Module layout:
    - `generator` — jvm/lib, core pipeline (no `main`).
    - `cli` — jvm/app, `mainClass: EntrypointKt`.
    - `app` — jvm/app, Compose Desktop GUI.
    - `testing` — jvm/lib, shared test utilities, consumed via `test-dependencies: - //testing`.
- Adding a module: create its `module.yaml` (product/dependencies/settings), list it in `project.yaml`, and add any
  new library version to `libs.versions.toml`.

## Commands

- Build: `./kotlin build`
- All tests: `./kotlin check`
- One module: `./kotlin test -m generator`
- One test class: `./kotlin test -m generator --include-classes 'ResumeGeneratorTest'`
- One test method: `./kotlin test --include-test '<FQN with package and class>'`
- Run CLI: `./kotlin run -m cli -- --website=<url> [--input --output --filename --apikey --verbose]`
- Run GUI: `./kotlin run -m app`; hot reload: `./kotlin run -m app --compose-hot-reload-mode` (auto-reload on save only
  works via the IDE's "Run with Compose Hot Reload"; the CLI does not watch files)
- List modules/checks: `./kotlin show modules`, `./kotlin show checks`

## Guides

Detailed guidance is split by domain; read the relevant file when the task needs it:

- `docs/ARCHITECTURE.md` — pipeline overview, wiring, and architecture conventions.
- `docs/TESTING.md` — test setup, fakes, fixtures, e2e tests.
- `docs/SECURITY.md` — security invariants; hard requirements, do not weaken.
- `docs/STYLE.md` — code style and commit messages.
