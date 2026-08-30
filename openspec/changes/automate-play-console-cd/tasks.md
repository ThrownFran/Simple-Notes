## 1. Prerequisites (manual, outside the codebase)

- [x] 1.1 Back up the existing release keystore offline (outside this repo) before wiring it into any automated system — done by maintainer
- [x] 1.2 In Google Cloud Console, create a service account for Play Developer API access (or reuse an existing project) and generate a JSON key for it — done by maintainer; note the flow changed: no Play Console project-linking step needed, service account created directly in Cloud Console
- [x] 1.3 In Play Console → Users and permissions (the old "API access" page has been replaced), invite the service account's email and grant it permission on this app (release manager access to at least the internal testing track) — done by maintainer
- [ ] 1.4 Confirm the service account can see the app by listing it via the Play Developer API (e.g. a one-off `gh`/`curl` check or the Gradle Play Publisher plugin's own validation task) before proceeding to workflow-authoring tasks — will be verified together with the 4.9 dry-run once secrets (4.8) are added

## 2. Release signing (Gradle)

- [x] 2.1 Add `keystore.properties` and common keystore file extensions (`*.jks`, `*.keystore`) to `.gitignore`
- [x] 2.2 Add a committed `keystore.properties.example` documenting the expected non-secret keys (`storeFile`, `keyAlias`) with placeholder values; `storePassword`/`keyPassword` are documented as macOS Keychain items instead, not file fields
- [x] 2.3 Add a `signingConfigs.release` block in `app/build.gradle` that loads `storeFile`/`keyAlias` from `keystore.properties` (local) and reads `storePassword`/`keyPassword` from macOS Keychain (service `SimpleNotes-ReleaseKeystore`) locally, or from `keystore.properties` in CI; fails the build with a clear, actionable error if the required source is missing — enforced via `gradle.taskGraph.whenReady`, scoped to release-packaging tasks only, so plain `test`/`ktlintCheck`/`assembleDebug` never require a keystore or touch Keychain
- [x] 2.4 Wire `buildTypes.release.signingConfig` to the new `signingConfigs.release`
- [ ] 2.5 Verify locally: add the real `storePassword`/`keyPassword` to macOS Keychain (`security add-generic-password -a storePassword -s SimpleNotes-ReleaseKeystore -w` and same for `keyPassword`), fill in the real `storeFile`/`keyAlias` in `keystore.properties`, run `./gradlew assembleRelease` and `./gradlew bundleRelease`, confirm the output is signed (e.g. via `apksigner verify` or `jarsigner -verify`) — **blocked: requires your real keystore/passwords, cannot be done by the agent**
- [x] 2.6 Verify the failure path: temporarily rename/remove local `keystore.properties`, confirm `assembleRelease` fails fast with a clear, actionable error message — verified locally, `assembleRelease` fails with the expected message and `test`/`assembleDebug` remain unaffected
- [x] 2.7 Confirm `ci.yml`'s existing `assembleRelease` step still passes unsigned (no secrets added to that workflow) — verified locally via `CI=true ./gradlew assembleRelease` (builds unsigned successfully); no changes needed to `ci.yml`

## 3. Release versioning (Gradle)

- [x] 3.1 Replace the static `versionCode` constant in `app/build.gradle` with logic that reads a `versionCode` override from an environment variable (e.g. `CD_VERSION_CODE`), falling back to the existing checked-in constant when unset (so local/CI-unsigned builds are unaffected)
- [x] 3.2 Confirm `versionName` stays a manually-edited constant, unchanged by this task group
- [x] 3.3 Verify locally: run `./gradlew assembleRelease` with `CD_VERSION_CODE` set, confirm the built artifact's manifest reflects the overridden value; run without it set and confirm the fallback constant is used — verified via `output-metadata.json` (1234 with override set, 9 without)

## 4. Play Store publish workflow

- [x] 4.1 Add the Gradle Play Publisher plugin to the root/`app` `build.gradle` (or plugins block, per current Gradle plugin management style) — pinned to GPP `3.13.0` (last version supporting Gradle 8.13/AGP 8.13; 4.x requires Gradle 9.1+/AGP 9), verified `./gradlew help`/`tasks` resolve and `publishReleaseBundle` is registered
- [x] 4.2 Configure the plugin to read Play service-account credentials from a file path supplied via environment variable/secret, targeting the internal testing track by default — `play { serviceAccountCredentials.set(file(System.getenv("PLAY_SERVICE_ACCOUNT_JSON_PATH") ?: ...)); track.set("internal") }` in `app/build.gradle`
- [x] 4.3 Add a new GitHub Actions workflow (`.github/workflows/publish.yml`) triggered on `push` of tags matching `v*.*.*` and on `workflow_dispatch`
- [x] 4.4 In that workflow: checkout, JDK 21 setup, decode the base64 keystore secret to a file, write `keystore.properties` from secrets, write the Play service-account JSON from its secret to a file
- [x] 4.5 Compute `CD_VERSION_CODE` from `1000 + github.run_number` and export it for the Gradle invocation
- [x] 4.6 Run the Gradle Play Publisher task to build and upload the signed release AAB to the internal testing track — `./gradlew publishReleaseBundle`
- [x] 4.7 Surface `versionName` and the computed `versionCode` in the workflow run summary
- [ ] 4.8 Add the required repository secrets: base64 keystore, store password, key alias, key password, Play service-account JSON — names documented in `publish.yml`'s header comment — **blocked: requires your real secret values in GitHub repo settings, cannot be done by the agent**
- [ ] 4.9 Dry-run: push a test `v0.0.0-test`-style tag (or use `workflow_dispatch`), confirm the build lands on Play Console's internal testing track with the expected `versionCode`/`versionName` — **blocked: requires 4.8 plus completed Section 1 (Play Console API access)**

## 5. Promotion workflow

- [ ] 5.1 Add a second GitHub Actions workflow (e.g. `.github/workflows/promote.yml`), triggered only via `workflow_dispatch`, with an input for the target track (e.g. `closed`/`production`) and the release/version to promote
- [ ] 5.2 Implement the promotion using the Gradle Play Publisher plugin's promote task (or direct Play Developer API call), reusing the same service-account secret as the publish workflow
- [ ] 5.3 Verify the promotion workflow does not run automatically as a result of the publish workflow completing (confirm via workflow trigger configuration, not just observation)
- [ ] 5.4 Dry-run: promote the test release from task 4.9 to a non-production track (e.g. closed testing) and confirm it appears correctly in Play Console

## 6. Documentation

- [ ] 6.1 Update `CLAUDE.md`'s "Gotchas"/build section to reflect that release signing now requires `keystore.properties` (local) or CI secrets, replacing the "release builds are unsigned as checked in" note
- [ ] 6.2 Document the required GitHub Actions secrets and the manual Play Console/GCP prerequisite steps (from section 1) in a README or CONTRIBUTING note so they're discoverable without re-reading this change
