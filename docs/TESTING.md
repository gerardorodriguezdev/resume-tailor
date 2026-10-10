# Testing

- `kotlin.test` assertions on JUnit 5 (Platform); coroutine tests use `runTest`, e2e uses `runBlocking`, Compose UI
  tests use `runComposeUiTest` with `@OptIn(ExperimentalTestApi::class)`.
- Test names are backtick sentences: `given ... when ... then ...`.
- Hand-rolled fakes (no mocking library):
    - Fakes for `generator` public interfaces shared across modules -> `testing` module, public (`FakeResumeGenerator`,
      `FakeInputValidator`).
    - Fakes for `internal` implementations used by one module -> `<module>/test/testing`, `internal`
      (`FakeWebsiteScanner`, `FakeLogger`, ...).
    - Fake shape: configurable `var result` / `var error`, recorded argument lists, and a `var calls: Int = 0` with a
      `private set`.
- Shared fixtures: constants and sample data in `testing.TestData`; helpers in `testing.TestExtensions`
  (`writeResumeTo`, `pdfExists`, `readTestResource`, `readTestResourceText`).
- Parameterized tests: `junit-jupiter-params` with `@ParameterizedTest(name = "given {0} when ... then ...")`,
  `@MethodSource` and a `@JvmStatic` provider in the companion object.
- E2E tests: annotate with `testing.E2eTest` (`@Tag("e2e")` + `@EnabledIfEnvironmentVariable(TEST_DEEPSEEK_API_KEY)`);
  they are skipped unless that env var is set (run with `TEST_DEEPSEEK_API_KEY=... ./kotlin check`). Always add
  `@Timeout` (120-180s) to e2e and Playwright-backed tests.
- `testing/resources/junit-platform.properties` enables parallel test execution — keep tests independent, never share
  mutable global state.
- Compose UI: assert via `testTag`; tag constants live in a `MainScreenTestTags` object with a `MainScreen.` prefix.
- Use `@TempDir` for temporary directories and an in-process `HttpServer` for local web fixtures (see
  `WebsiteScannerTest`).

## Gotchas

- `HtmlToPdfConverterTest` compares against fixture `generator/testResources/expected-resume.pdf` (date metadata
  stripped). Changing `HtmlGenerator` output requires regenerating this fixture. `HtmlGeneratorTest` does the same
  against `expected-resume.html`.
