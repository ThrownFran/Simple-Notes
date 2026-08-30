## Why

Releases to the Play Store are currently a fully manual process: there is no signing config in Gradle (release builds are unsigned as checked in), `versionCode` is a hand-edited constant, and there is no automated path from a merged commit to a build on Play Console. Now that CI (ktlint + tests + debug/release assembly, gated on `master` via a required status check) is in place, the natural next step is closing the loop so a release can be cut and shipped to testers without manually building, signing, and uploading an AAB by hand.

## What Changes

- Add a Gradle release `signingConfig` sourced from a `keystore.properties` file that only the CD publish workflow ever writes (from CI-injected secrets) — instead of shipping releases unsigned. Local `assembleRelease`/`bundleRelease` remains unsigned (no `keystore.properties` is ever created locally by this change); a real signed local build, if ever needed, goes through Android Studio's Generate Signed Bundle/APK wizard instead.
- Replace the static, hand-edited `versionCode` in `app/build.gradle` with a CI-computed value (and confirm/adjust `versionName` sourcing), so every CD run produces a unique, installable build without a manual edit + commit.
- Introduce a one-time, human-performed Play Console API access setup (GCP service account with Play Developer API access, granted permission on this app) — not code, but a documented prerequisite the pipeline depends on.
- Add the Gradle Play Publisher plugin, configured to use the service-account credentials from a CI secret, plus a new GitHub Actions workflow (manually triggered via `workflow_dispatch`, or on a release tag — not on every push to `master`) that builds a signed release AAB and publishes it to Play's **internal testing** track.
- **Out of scope for this change**: promoting a release from internal testing to closed/open/production tracks. That stays a manual click in Play Console for now; automating it (e.g. a second `workflow_dispatch` workflow) is deliberately deferred to a future, separate change once internal-track publishing has been exercised for a while.

## Capabilities

### New Capabilities
- `release-signing`: Gradle release build signing sourced from CI-managed secrets (keystore + credentials), with no unsigned/insecure fallback for release builds.
- `release-versioning`: CI-computed `versionCode`/`versionName` for release builds, replacing the hand-maintained constant.
- `play-store-publishing`: Building and publishing a signed release AAB to Play Console's internal testing track via CD. Promotion to higher tracks stays a manual Play Console action, out of scope here.

### Modified Capabilities
- None — this is a greenfield CD capability; no existing `openspec/specs/` entries exist yet for build/release behavior.

## Impact

- **Code**: `app/build.gradle` (signing config, versioning logic, Gradle Play Publisher plugin application), root `build.gradle`/`settings.gradle` (plugin declaration), no application source changes.
- **CI/CD**: new GitHub Actions workflow(s) alongside the existing `.github/workflows/ci.yml`; new required GitHub Actions secrets (base64 keystore, store password, key alias, key password, Play service-account JSON).
- **External/manual**: one-time Play Console + GCP service-account setup performed by the user outside of this repo; this is a hard prerequisite for the `play-store-publishing` capability and is called out explicitly in tasks so implementation isn't blocked silently on it.
- **Local dev**: no change required — `assembleRelease`/`bundleRelease` continue to build unsigned locally, exactly as today; Android Studio's Generate Signed Bundle/APK wizard remains the path for a real signed local build if ever needed.
