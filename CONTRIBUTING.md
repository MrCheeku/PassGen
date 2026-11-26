# Contributing to PassGen

Thanks for helping improve PassGen, a local-first Android password generator and credential vault.

## Development setup

- Android Studio with an Android SDK for API 36
- JDK 17
- Gradle 9.3.1 (the repository currently does not include the Gradle wrapper scripts)

Clone the repository, open it in Android Studio, or run the checks from the project root:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

## Pull request checklist

- Keep credential generation, storage, and encryption changes covered by tests.
- Never use real passwords or personal vault backups in tests, screenshots, issues, or pull requests.
- Never commit `.env` files, API keys, signing credentials, keystores, or generated secrets.
- Explain security-sensitive changes and their compatibility implications.
- Update the README when user-facing behavior or setup changes.

Please report suspected vulnerabilities privately using the instructions in [SECURITY.md](SECURITY.md), if available, rather than publishing exploit details in an issue.
