# Bootstrap (first-time setup)

This scaffold intentionally does NOT commit the `gradle-wrapper.jar` binary. You have
two ways to get the wrapper:

## Option A — Android Studio (easiest)

1. Open this folder in Android Studio (File → Open → select the `app` directory).
2. When prompted about the missing wrapper, let it **download Gradle 8.10.2**
   (matches `gradle/wrapper/gradle-wrapper.properties`).
3. Studio will auto-generate `gradle/wrapper/gradle-wrapper.jar` on first sync.

## Option B — Command line (if you have Gradle installed globally)

```bash
gradle wrapper --gradle-version 8.10.2 --distribution-type bin
```

This generates:
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew` (Unix shell)
- `gradlew.bat` (Windows cmd)

Commit the three files once generated.

## Verify

```bash
./gradlew --version     # Unix / Git Bash
gradlew.bat --version   # Windows cmd
```

Should print:
```
Gradle 8.10.2
Launcher JVM: 17.x.x
```

## Pre-commit hooks (one time)

```bash
npm install
npx lefthook install
```

This wires ktlint, spotless, and commitlint into your local git hooks.

## First build

```bash
./gradlew :app:assembleDebug
```

First build takes ~2–5 minutes (dependency download). Subsequent builds use Gradle's
incremental and configuration caches and should complete in < 30 seconds.
