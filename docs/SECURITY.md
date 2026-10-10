# Security invariants (do not weaken)

- URL validation (`InputValidator.isValidUrl`) is SSRF-safe: https only, no userinfo, host must resolve and must not
  be loopback/link-local/site-local/any-local. All new URL inputs must go through this validator.
- `PlaywrightWebsiteScanner` aborts every route whose request is neither a navigation request nor a validated URL, and
  extracts text only from `<main>`/`<article>` (falling back to `<body>`), then runs it through `validateContent`.
- `validateContent` (`generator` `Extensions.kt`) rejects raw HTML tags, comments, CDATA, DOCTYPE, and control /
  zero-width / bidi characters. LLM output is untrusted and must be re-validated before rendering.
- `MdFilesReader` parses each md file with commonmark and only allows a fixed node allowlist; anything else throws.
- `Agent`'s system prompt declares `<user-resume>`/`<job-posting>` content untrusted data and demands a bare JSON
  array; `SectionsParser` re-validates section structure, order, and content.
- `HtmlToPdfConverter` disables JavaScript, blocks service workers, and aborts all routes.
- API keys: strict regex validation (`sk-` + 32 hex), never logged; e2e keys come from `TEST_DEEPSEEK_API_KEY`.
