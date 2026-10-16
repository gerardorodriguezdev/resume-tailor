# Resume Tailor

Tailors your Markdown resume to a job posting with DeepSeek and exports it as a PDF. Available as a Compose Desktop
app and a CLI.

```
Markdown resume + job posting URL ─► DeepSeek ─► JSON sections ─► HTML ─► PDF
```

The model only uses facts from your resume; its output is re-validated before rendering.

## Requirements

- JDK 25
- A [DeepSeek API key](https://platform.deepseek.com/) (`sk-` + 32 hex chars)

The project uses the [Kotlin Toolchain (Amper)](https://github.com/JetBrains/amper) via the `./kotlin` wrapper.

## Resume input

Put one or more `.md` files in a directory. Only headings, paragraphs, emphasis, and lists are allowed.

## Usage

Desktop app:

```bash
./kotlin run -m app
```

CLI:

```bash
API_KEY=sk-... ./kotlin run -m cli -- --website=https://example.com/job --input=./resume --output=./out
```

| Argument            | Default           |
|---------------------|-------------------|
| `--website=<url>`   | required          |
| `--input=<path>`    | working directory |
| `--output=<path>`   | working directory |
| `--filename=<name>` | `Resume`          |
| `--apikey=<key>`    | `API_KEY` env var |
| `--verbose`         | off               |

The URL must be `https` and resolve to a public host.

## Modules

- `generator`: core pipeline
- `cli`: command-line front-end
- `app`: Compose Desktop GUI
- `testing`: shared test fakes and fixtures

## Development

```bash
./kotlin check
```

E2E tests run only when `TEST_DEEPSEEK_API_KEY` is set. CI builds signed, notarized macOS `.dmg` (app) and `.pkg`
(CLI) packages on every push. See [CLAUDE.md](CLAUDE.md) for conventions.
