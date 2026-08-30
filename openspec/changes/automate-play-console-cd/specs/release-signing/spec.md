## ADDED Requirements

### Requirement: Release builds SHALL be signed with the maintainer's release keystore
The Gradle release build type SHALL define a `signingConfig` that signs `assembleRelease`/`bundleRelease` output with the maintainer's existing release keystore. The system SHALL NOT fall back to an unsigned or debug-signed release artifact when a proper signing source is unavailable.

#### Scenario: Local build with keystore.properties present
- **WHEN** a developer runs `./gradlew assembleRelease` or `./gradlew bundleRelease` locally with a valid `keystore.properties` file present at the repo root
- **THEN** the resulting release APK/AAB is signed with the keystore referenced in that file

#### Scenario: Local build without any signing source configured
- **WHEN** a developer runs `./gradlew assembleRelease` locally with no `keystore.properties` file and no equivalent environment variables set
- **THEN** the build fails fast with a clear error identifying the missing signing configuration, rather than producing an unsigned release artifact

#### Scenario: CI build with injected signing secrets
- **WHEN** the CD publish workflow runs with the keystore and credential secrets available as environment variables / decoded files
- **THEN** the resulting release AAB is signed with the same keystore used for local release builds

### Requirement: Signing credentials SHALL NOT be committed to the repository
No keystore file, `keystore.properties` file, or signing password SHALL be committed to version control. A template (`keystore.properties.example`) documenting the expected keys, with placeholder values only, MAY be committed.

#### Scenario: Repository scan for committed secrets
- **WHEN** the repository's tracked files are inspected
- **THEN** no real keystore file, populated `keystore.properties`, or plaintext signing password is present, and `.gitignore` excludes `keystore.properties` and keystore file extensions

### Requirement: Day-to-day CI SHALL remain unsigned
The existing `ci.yml` workflow (running on every push/PR to `master`) SHALL continue building `assembleRelease` unsigned, without access to real signing secrets. Only the dedicated CD publish workflow SHALL have access to signing secrets.

#### Scenario: Pull request CI run
- **WHEN** `ci.yml` runs `assembleRelease` as part of a pull request build
- **THEN** the task completes without requiring or referencing the keystore/signing secrets, producing an unsigned release artifact solely to validate the R8/shrinking build path
