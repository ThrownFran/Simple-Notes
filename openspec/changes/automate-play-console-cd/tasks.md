## 1. Prerequisites (manual, outside the codebase)

- [x] 1.1 Back up the existing release keystore offline (outside this repo) before wiring it into any automated system — done by maintainer
- [x] 1.2 In Google Cloud Console, create a service account for Play Developer API access (or reuse an existing project) and generate a JSON key for it — done by maintainer; note the flow changed: no Play Console project-linking step needed, service account created directly in Cloud Console
- [x] 1.3 In Play Console → Users and permissions (the old "API access" page has been replaced), invite the service account's email and grant it permission on this app (release manager access to at least the internal testing track) — done by maintainer
- [ ] 1.4 Confirm the service account can see the app by listing it via the Play Developer API (e.g. a one-off `gh`/`curl` check or the Gradle Play Publisher plugin's own validation task) before proceeding to workflow-authoring tasks — will be verified together with the 4.9 dry-run once secrets (4.8) are added

## 2. Release signing (Gradle)

- [x] 2.1 Add `keystore.properties` and common keystore file extensions (`*.jks`, `*.keystore`) to `.gitignore`
- [x] 2.2 ~~Add a committed `keystore.properties.example`~~ — superseded: signing is CI-only now (see design.md decision), there's no local file for a maintainer to copy/fill in, so no example template is needed
- [x] 2.3 Add a `signingConfigs.release` block in `app/build.gradle` that loads `storeFile`/`storePassword`/`keyAlias`/`keyPassword` from `keystore.properties` when present, leaving `buildTypes.release` unsigned otherwise — no fail-fast error, since local `assembleRelease` is expected to be unsigned (real local signed builds go through Android Studio's Generate Signed Bundle/APK wizard instead)
- [x] 2.4 Wire `buildTypes.release.signingConfig` to the new `signingConfigs.release`
- [x] 2.5 Verify signing end-to-end: generated a throwaway test keystore (`keytool -genkeypair`), pointed a local `keystore.properties` at it, ran `./gradlew assembleRelease`, confirmed via `apksigner verify --print-certs` that the output APK was signed with that keystore's certificate; cleaned up the test keystore and `keystore.properties` afterward — validates the exact code path CI's publish workflow uses. Real-keystore verification happens implicitly on the first real `publishReleaseBundle` run (task 4.9)
- [x] 2.6 Verify the no-signing-source path: confirmed `assembleRelease` with no `keystore.properties` present builds successfully and unsigned (no error) — this is the expected default for local builds
- [x] 2.7 Confirm `ci.yml`'s existing `assembleRelease` step still passes unsigned (no secrets added to that workflow) — verified locally via `assembleRelease` with no `keystore.properties` (builds unsigned successfully); no changes needed to `ci.yml`

## 3. Release versioning (Gradle)

- [x] 3.1 Replace the static `versionCode` constant in `app/build.gradle` with logic that reads a `versionCode` override from an environment variable (e.g. `CD_VERSION_CODE`), falling back to the existing checked-in constant when unset (so local/CI-unsigned builds are unaffected)
- [x] 3.2 Revised: `versionName` also reads a `CD_VERSION_NAME` env override (falling back to the checked-in constant), so the publish workflow can derive it from the pushed tag rather than relying on a manually-edited constant staying in sync with the tag — see 4.5
- [x] 3.3 Verify locally: run `./gradlew assembleRelease` with `CD_VERSION_CODE`/`CD_VERSION_NAME` set, confirm the built artifact's manifest reflects both overridden values; run without them set and confirm the fallback constants are used — verified via `output-metadata.json` (`versionCode` 1234, `versionName` "2.0.0-test" with overrides; checked-in constants otherwise)

## 4. Play Store publish workflow

- [x] 4.1 Add the Gradle Play Publisher plugin to the root/`app` `build.gradle` (or plugins block, per current Gradle plugin management style) — pinned to GPP `3.13.0` (last version supporting Gradle 8.13/AGP 8.13; 4.x requires Gradle 9.1+/AGP 9), verified `./gradlew help`/`tasks` resolve and `publishReleaseBundle` is registered
- [x] 4.2 Configure the plugin to read Play service-account credentials from a file path supplied via environment variable/secret, targeting the internal testing track by default — `play { serviceAccountCredentials.set(file(System.getenv("PLAY_SERVICE_ACCOUNT_JSON_PATH") ?: ...)); track.set("internal") }` in `app/build.gradle`
- [x] 4.3 Add a new GitHub Actions workflow (`.github/workflows/publish.yml`) triggered on `push` of tags matching `v*.*.*` and on `workflow_dispatch`
- [x] 4.4 In that workflow: checkout, JDK 21 setup, decode the base64 keystore secret to a file, write `keystore.properties` from secrets, write the Play service-account JSON from its secret to a file
- [x] 4.5 Compute `CD_VERSION_CODE` from `1000 + github.run_number`; compute `CD_VERSION_NAME` from the pushed tag (`v*.*.*`, `v` stripped) on tag-triggered runs, or from a required `version_name` `workflow_dispatch` input on manual runs — export both for the Gradle invocation
- [x] 4.6 Run the Gradle Play Publisher task to build and upload the signed release AAB to the internal testing track — `./gradlew publishReleaseBundle`
- [x] 4.7 Surface `versionName` and the computed `versionCode` in the workflow run summary — reads `CD_VERSION_NAME`/`CD_VERSION_CODE` directly rather than parsing `build.gradle`
- [x] 4.8 Add the required repository secrets: base64 keystore, store password, key alias, key password, Play service-account JSON — names documented in `publish.yml`'s header comment — done by maintainer via GitHub web UI
- [ ] 4.9 Dry-run: push a test `v0.0.0-test`-style tag (or use `workflow_dispatch`), confirm the build lands on Play Console's internal testing track with the expected `versionCode`/`versionName` — **blocked: `publish.yml` must be merged to `master` before `workflow_dispatch` is available (confirmed via `gh workflow list` — GitHub only registers dispatchable workflows from the default branch); PR #2 pending merge**

## 5. Promotion workflow

**Deferred — out of scope for this change.** Promoting a release from internal testing to closed/open/production tracks stays a manual Play Console action for now (Testing → Internal testing → your release → Promote release). Automating it is a reasonable follow-up once internal-track publishing has been exercised for a while, but will be proposed as its own separate OpenSpec change rather than built here.

## 6. Documentation

- [ ] 6.1 Update `CLAUDE.md`'s "Gotchas"/build section to reflect that release signing now requires `keystore.properties` (local) or CI secrets, replacing the "release builds are unsigned as checked in" note
- [ ] 6.2 Document the required GitHub Actions secrets and the manual Play Console/GCP prerequisite steps (from section 1) in a README or CONTRIBUTING note so they're discoverable without re-reading this change
