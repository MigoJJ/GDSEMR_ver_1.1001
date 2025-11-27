# Repository Guidelines

## Project Structure & Modules
- `app/`: JavaFX desktop client (`src/main/java`, resources under `src/main/resources`) packaged as the default `run` target.
- `server/`: Spring Boot REST skeleton (`bootRun`), with tests in `server/src/test/java`.
- `list/` and `utilities/`: Shared libraries consumed by `app`; `list` houses JUnit tests.
- Build configuration lives in `build.gradle`, module `build.gradle` files, and shared versions in `gradle.properties`. Wrapper scripts (`gradlew`, `run-gradle.sh`) keep toolchains consistent.

## Build, Test, and Development Commands
- `./gradlew run`: Launch JavaFX client (delegates to `:app:run`).
- `./gradlew runServer`: Start Spring Boot stub on port 8080 (delegates to `:server:bootRun`).
- `./gradlew test`: Run all module tests; scope to a module with `./gradlew :list:test` or `./gradlew :server:test`.
- `./run-gradle.sh <gradle-args>`: Convenience wrapper if you prefer local JDK settings pinned in the script.
- If multiple JDKs are installed, export `ORG_GRADLE_JAVA_HOME=/path/to/jdk-25` before running tasks.

## Coding Style & Naming Conventions
- Target Java 25 toolchain (set in `gradle.properties`); `server` compiles bytecode at release 21 for Spring classpath scanning.
- Use 4-space indentation; keep package names lower-case dot-separated (`com.emr.*`).
- Prefer descriptive class names matching file names; favor immutable data where practical.
- Use Gradle toolchains rather than local `JAVA_HOME` overrides; configure JavaFX version via `gradle.properties` (`javafxVersion`).

## Testing Guidelines
- Unit tests use JUnit Jupiter; Spring Boot tests rely on `spring-boot-starter-test`.
- Name tests after behavior (`ClassNameTests`, methods like `doesReturnResultsForValidQuery`).
- Run `./gradlew test` before commits; add focused module runs while iterating (e.g., `:server:test --tests '...ServiceTests'`).
- No enforced coverage threshold yet—aim to exercise new branches and integration points (database access, controllers, and UI-side service calls).

## Commit & Pull Request Guidelines
- History is sparse; prefer imperative, scoped messages (`module: brief change`, e.g., `app: fix patient list refresh`).
- For PRs, include: summary of changes, testing performed (`./gradlew test` output), linked issues, and screenshots or logs for UI/API changes.
- Keep PRs small and focused (one feature or bugfix per PR). Document migrations or config changes in the description and update `README.md` if developer steps change.
