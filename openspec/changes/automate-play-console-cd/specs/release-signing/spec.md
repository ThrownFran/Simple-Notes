## ADDED Requirements

### Requirement: Release builds SHALL be signed with the maintainer's release keystore, only in CI
The Gradle release build type SHALL define a `signingConfig`, sourced from a `keystore.properties` file at the repo root, that signs `assembleRelease`/`bundleRelease`/`publishReleaseBundle` output with the maintainer's existing release keystore when that file is present. Signing only ever happens in the dedicated CD publish workflow, which writes `keystore.properties` from repository secrets before invoking Gradle; there is no local, machine-persisted signing configuration.

#### Scenario: CI build with injected signing secrets
- **WHEN** the CD publish workflow runs, having written `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) from repository secrets to an ephemeral file before invoking Gradle
- **THEN** the resulting release AAB is signed with the keystore referenced in that file

#### Scenario: Local build with no keystore.properties present
- **WHEN** a developer runs `./gradlew assembleRelease` or `./gradlew bundleRelease` locally, where no `keystore.properties` file exists
- **THEN** the build succeeds and produces an unsigned release artifact, without error — a real signed local build, if ever needed, is produced via Android Studio's Build > Generate Signed Bundle/APK wizard instead, which manages its own credentials independent of this Gradle config

### Requirement: Signing credentials SHALL NOT be committed to the repository
No keystore file, `keystore.properties` file, or signing password SHALL be committed to version control.

#### Scenario: Repository scan for committed secrets
- **WHEN** the repository's tracked files are inspected
- **THEN** no real keystore file, populated `keystore.properties`, or plaintext signing password is present, and `.gitignore` excludes `keystore.properties` and keystore file extensions

### Requirement: Day-to-day CI SHALL remain unsigned
The existing `ci.yml` workflow (running on every push/PR to `master`) SHALL continue building `assembleRelease` unsigned, without access to real signing secrets — it never writes a `keystore.properties` file, so the release build type naturally has no `signingConfig` attached. Only the dedicated CD publish workflow SHALL write `keystore.properties` and SHALL have access to signing secrets.

#### Scenario: Pull request CI run
- **WHEN** `ci.yml` runs `assembleRelease` as part of a pull request build, with no `keystore.properties` present
- **THEN** the task completes without requiring or referencing any signing secrets, producing an unsigned release artifact solely to validate the R8/shrinking build path
