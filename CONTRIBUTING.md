# Contributing

## Build

```bash
./mvnw verify
```

Java 17. CI runs the same command on every push to `main` and on every pull request.

## Branches

`feature/<stage>-<short-name>`, for example `feature/1-transport` or `feature/3-route-patterns`.
Stages are listed in [docs/ROADMAP.md](docs/ROADMAP.md).

## Commits

Prefix the subject with the kind of change:

| Prefix | Use for |
|---|---|
| `feat:` | New behaviour |
| `fix:` | Bug fix |
| `docs:` | Documentation only |
| `test:` | Tests only |
| `refactor:` | Code change without behaviour change |
| `build:` | Maven, CI, dependencies |

Example: `feat: route callback queries by RoutePattern`.

## Rules

- Every feature comes with a test. A pull request without one is not finished.
- telegrambots classes are used only in the `transport` and `render` packages.
- Developer mistakes (duplicate routes, unknown parameters) fail at startup with a clear message.
- Code style follows `.editorconfig`: 4 spaces, LF, UTF-8.
