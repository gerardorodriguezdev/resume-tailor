# Resume Tailor

Tailors your Markdown resume to a job posting with DeepSeek and exports it as a PDF with a Compose Desktop app.

```
Markdown resume + job posting URL ─► DeepSeek ─► JSON sections ─► HTML ─► PDF
```

The model only uses facts from your resume; its output is re-validated before rendering.

## Requirements

- JDK 25
- A [DeepSeek API key](https://platform.deepseek.com/) (`sk-` + 32 hex chars)

The project uses the [Kotlin Toolchain (Amper)](https://github.com/JetBrains/amper) via the `./kotlin` wrapper.

## Updates

The app checks GitHub for a newer release on startup and shows a download banner when one is available.

## Resume input

Put one or more `.md` files in a directory. Only headings, paragraphs, emphasis, and lists are allowed.

## Usage

```bash
./kotlin run -m app
```

The job posting URL must be `https` and resolve to a public host.

## Modules

- `generator`: core pipeline
- `app`: Compose Desktop GUI
- `testing`: shared test fakes and fixtures
- `ci`: release automation used by the deploy workflow

## Development

```bash
./kotlin check
```

E2E tests run only when `TEST_DEEPSEEK_API_KEY` is set. CI builds a signed, notarized macOS `.dmg` on every push
and publishes it as a GitHub release. See [CLAUDE.md](CLAUDE.md) for conventions.
