## ADDED Requirements

### Requirement: Release builds SHALL be signed with the maintainer's release keystore
The Gradle release build type SHALL define a `signingConfig` that signs `assembleRelease`/`bundleRelease` output with the maintainer's existing release keystore. Outside of ambient CI validation runs (see the "Day-to-day CI SHALL remain unsigned" requirement), the system SHALL NOT fall back to an unsigned or debug-signed release artifact when a proper signing source is unavailable.

#### Scenario: Local build with keystore.properties and Keychain entries present
- **WHEN** a developer runs `./gradlew assembleRelease` or `./gradlew bundleRelease` locally (macOS) with a valid `keystore.properties` file (`storeFile`, `keyAlias`) present at the repo root, and `storePassword`/`keyPassword` present in macOS Keychain under the service `SimpleNotes-ReleaseKeystore`
- **THEN** the resulting release APK/AAB is signed with the keystore referenced in `keystore.properties`, using the passwords read from Keychain at build time

#### Scenario: Local build without any signing source configured, outside CI
- **WHEN** a developer runs `./gradlew assembleRelease` locally (the `CI` environment variable is unset) with no `keystore.properties` file, or with `keystore.properties` present but `storePassword`/`keyPassword` missing from Keychain
- **THEN** the build fails fast with a clear error identifying the missing signing configuration (naming the `security add-generic-password` commands needed, when the file is present but Keychain entries are missing), rather than producing an unsigned release artifact

#### Scenario: CI build with injected signing secrets
- **WHEN** the CD publish workflow runs with the keystore and credential secrets available as environment variables / decoded files
- **THEN** the resulting release AAB is signed with the same keystore used for local release builds

### Requirement: Signing credentials SHALL NOT be committed to the repository, and passwords SHALL NOT be persisted to disk locally
No keystore file, `keystore.properties` file, or signing password SHALL be committed to version control. A template (`keystore.properties.example`) documenting the expected non-secret keys (`storeFile`, `keyAlias`), with placeholder values only, MAY be committed. On the maintainer's local (macOS) machine, `storePassword`/`keyPassword` SHALL NOT be written to `keystore.properties` or any other file on disk — they SHALL be stored in macOS Keychain and read at build time only for tasks that need them.

#### Scenario: Repository scan for committed secrets
- **WHEN** the repository's tracked files are inspected
- **THEN** no real keystore file, populated `keystore.properties`, or plaintext signing password is present, and `.gitignore` excludes `keystore.properties` and keystore file extensions

#### Scenario: Local keystore.properties never contains passwords
- **WHEN** a developer's local `keystore.properties` is inspected after following the setup instructions
- **THEN** it contains only `storeFile` and `keyAlias`; `storePassword`/`keyPassword` exist solely as macOS Keychain items (service `SimpleNotes-ReleaseKeystore`), never as plaintext in the file

### Requirement: Day-to-day CI SHALL remain unsigned
The existing `ci.yml` workflow (running on every push/PR to `master`) SHALL continue building `assembleRelease` unsigned, without access to real signing secrets. Only the dedicated CD publish workflow SHALL have access to signing secrets. The signing config SHALL distinguish this ambient case from an unconfigured local dev machine by checking the standard `CI` environment variable (set to `true` by GitHub Actions on every runner): when `CI=true` and no signing source is present, the release build type SHALL proceed without a `signingConfig` (producing an unsigned artifact) rather than failing; when `CI` is unset, the fail-fast behavior from the previous requirement applies.

#### Scenario: Pull request CI run
- **WHEN** `ci.yml` runs `assembleRelease` as part of a pull request build, with `CI=true` and no keystore/signing secrets present
- **THEN** the task completes without requiring or referencing the keystore/signing secrets, producing an unsigned release artifact solely to validate the R8/shrinking build path

#### Scenario: Publish workflow always provides real credentials
- **WHEN** the dedicated CD publish workflow runs (also with `CI=true`)
- **THEN** it has already written a valid `keystore.properties` (or equivalent) from secrets before invoking Gradle, so the build is signed via the "CI build with injected signing secrets" scenario rather than falling through to the unsigned ambient-CI path
