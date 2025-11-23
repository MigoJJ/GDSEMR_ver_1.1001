# GDSEMR_ver_0.3

JavaFX EMR prototype targeting Java 25 and JavaFX 25.

## Requirements
- JDK 25 (Gradle toolchains will download/use it automatically if available)
- JavaFX 25 SDK artifacts (fetched from Maven Central by the OpenJFX Gradle plugin)
- SQLite JDBC (declared as a dependency; no manual install needed)

## Build & Run
- Root task: `./gradlew run` (delegates to `:app:run`)
- Module tasks: `./gradlew :app:run`, `./gradlew :list:test`, etc.
- If multiple JDKs are installed, point Gradle at Java 25 with `export ORG_GRADLE_JAVA_HOME=/path/to/jdk-25`.
- `./run-gradle.sh` is available as a convenience wrapper; update its paths if you move the project.

## Notes
- Java toolchain and version properties are centralized in `gradle.properties`.
- JavaFX version is configurable via `gradle.properties` (`javafxVersion`).
- Kotlin DSL templates for Gradle 9.2 live in `templates/` (`build.gradle.kts.template`, `app.build.gradle.kts.template`, `build-logic.build.gradle.kts.template`) to help migrate without version drift between app and build-logic.
