## 1. Prerequisites (manual, outside the codebase)

- [ ] 1.1 Back up the existing release keystore offline (outside this repo) before wiring it into any automated system
- [ ] 1.2 In Google Cloud Console, create a service account for Play Developer API access (or reuse an existing project) and generate a JSON key for it
- [ ] 1.3 In Play Console → API access, link the GCP project and grant the service account permission on this app (release manager access to at least the internal testing track)
- [ ] 1.4 Confirm the service account can see the app by listing it via the Play Developer API (e.g. a one-off `gh`/`curl` check or the Gradle Play Publisher plugin's own validation task) before proceeding to workflow-authoring tasks

## 2. Release signing (Gradle)

- [ ] 2.1 Add `keystore.properties` and common keystore file extensions (`*.jks`, `*.keystore`) to `.gitignore`
- [ ] 2.2 Add a committed `keystore.properties.example` documenting the expected keys (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) with placeholder values
- [ ] 2.3 Add a `signingConfigs.release` block in `app/build.gradle` that loads `keystore.properties` (local) and fails the build with a clear error if neither the properties file nor the equivalent environment variables are present
- [ ] 2.4 Wire `buildTypes.release.signingConfig` to the new `signingConfigs.release`
- [ ] 2.5 Verify locally: create a real local `keystore.properties` from the maintainer's existing keystore, run `./gradlew assembleRelease` and `./gradlew bundleRelease`, confirm the output is signed (e.g. via `apksigner verify` or `jarsigner -verify`)
- [ ] 2.6 Verify the failure path: temporarily rename/remove local `keystore.properties`, confirm `assembleRelease` fails fast with a clear, actionable error message
- [ ] 2.7 Confirm `ci.yml`'s existing `assembleRelease` step still passes unsigned (no secrets added to that workflow) — update it only if needed to tolerate the new fail-fast signing config for non-release-signing contexts

## 3. Release versioning (Gradle)

- [ ] 3.1 Replace the static `versionCode` constant in `app/build.gradle` with logic that reads a `versionCode` override from an environment variable (e.g. `CD_VERSION_CODE`), falling back to the existing checked-in constant when unset (so local/CI-unsigned builds are unaffected)
- [ ] 3.2 Confirm `versionName` stays a manually-edited constant, unchanged by this task group
- [ ] 3.3 Verify locally: run `./gradlew assembleRelease` with `CD_VERSION_CODE` set, confirm the built artifact's manifest reflects the overridden value; run without it set and confirm the fallback constant is used

## 4. Play Store publish workflow

- [ ] 4.1 Add the Gradle Play Publisher plugin to the root/`app` `build.gradle` (or plugins block, per current Gradle plugin management style)
- [ ] 4.2 Configure the plugin to read Play service-account credentials from a file path supplied via environment variable/secret, targeting the internal testing track by default
- [ ] 4.3 Add a new GitHub Actions workflow (e.g. `.github/workflows/publish.yml`) triggered on `push` of tags matching `v*.*.*` and on `workflow_dispatch`
- [ ] 4.4 In that workflow: checkout, JDK 21 setup, decode the base64 keystore secret to a file, write `keystore.properties` from secrets, write the Play service-account JSON from its secret to a file
- [ ] 4.5 Compute `CD_VERSION_CODE` from `1000 + github.run_number` and export it for the Gradle invocation
- [ ] 4.6 Run the Gradle Play Publisher task to build and upload the signed release AAB to the internal testing track
- [ ] 4.7 Surface `versionName` and the computed `versionCode` in the workflow run summary
- [ ] 4.8 Add the required repository secrets: base64 keystore, store password, key alias, key password, Play service-account JSON (document names in the workflow file's comments)
- [ ] 4.9 Dry-run: push a test `v0.0.0-test`-style tag (or use `workflow_dispatch`), confirm the build lands on Play Console's internal testing track with the expected `versionCode`/`versionName`

## 5. Promotion workflow

- [ ] 5.1 Add a second GitHub Actions workflow (e.g. `.github/workflows/promote.yml`), triggered only via `workflow_dispatch`, with an input for the target track (e.g. `closed`/`production`) and the release/version to promote
- [ ] 5.2 Implement the promotion using the Gradle Play Publisher plugin's promote task (or direct Play Developer API call), reusing the same service-account secret as the publish workflow
- [ ] 5.3 Verify the promotion workflow does not run automatically as a result of the publish workflow completing (confirm via workflow trigger configuration, not just observation)
- [ ] 5.4 Dry-run: promote the test release from task 4.9 to a non-production track (e.g. closed testing) and confirm it appears correctly in Play Console

## 6. Documentation

- [ ] 6.1 Update `CLAUDE.md`'s "Gotchas"/build section to reflect that release signing now requires `keystore.properties` (local) or CI secrets, replacing the "release builds are unsigned as checked in" note
- [ ] 6.2 Document the required GitHub Actions secrets and the manual Play Console/GCP prerequisite steps (from section 1) in a README or CONTRIBUTING note so they're discoverable without re-reading this change
