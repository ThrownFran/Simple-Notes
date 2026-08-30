## Why

Releases to the Play Store are currently a fully manual process: there is no signing config in Gradle (release builds are unsigned as checked in), `versionCode` is a hand-edited constant, and there is no automated path from a merged commit to a build on Play Console. Now that CI (ktlint + tests + debug/release assembly, gated on `master` via a required status check) is in place, the natural next step is closing the loop so a release can be cut and shipped to testers without manually building, signing, and uploading an AAB by hand.

## What Changes

- Add a Gradle release `signingConfig` sourced from a local, gitignored `keystore.properties` file (for developer machines) or CI-injected secrets (for GitHub Actions), instead of shipping releases unsigned. **BREAKING**: `assembleRelease`/`bundleRelease` will fail without one of those two sources configured, since there is intentionally no fallback to an unsigned or debug-signed release build.
- Replace the static, hand-edited `versionCode` in `app/build.gradle` with a CI-computed value (and confirm/adjust `versionName` sourcing), so every CD run produces a unique, installable build without a manual edit + commit.
- Introduce a one-time, human-performed Play Console API access setup (GCP service account with Play Developer API access, granted permission on this app) — not code, but a documented prerequisite the pipeline depends on.
- Add the Gradle Play Publisher plugin, configured to use the service-account credentials from a CI secret, plus a new GitHub Actions workflow (manually triggered via `workflow_dispatch`, or on a release tag — not on every push to `master`) that builds a signed release AAB and publishes it to Play's **internal testing** track.
- Add a separate, manually-triggered promotion path (e.g. a second `workflow_dispatch` workflow/job) to move a release from internal testing to closed/production tracks, kept deliberately decoupled from the internal-track publish step.

## Capabilities

### New Capabilities
- `release-signing`: Gradle release build signing sourced from CI-managed secrets (keystore + credentials), with no unsigned/insecure fallback for release builds.
- `release-versioning`: CI-computed `versionCode`/`versionName` for release builds, replacing the hand-maintained constant.
- `play-store-publishing`: Building and publishing a signed release AAB to Play Console's internal testing track via CD, plus a separate manual promotion path to higher tracks.

### Modified Capabilities
- None — this is a greenfield CD capability; no existing `openspec/specs/` entries exist yet for build/release behavior.

## Impact

- **Code**: `app/build.gradle` (signing config, versioning logic, Gradle Play Publisher plugin application), root `build.gradle`/`settings.gradle` (plugin declaration), no application source changes.
- **CI/CD**: new GitHub Actions workflow(s) alongside the existing `.github/workflows/ci.yml`; new required GitHub Actions secrets (base64 keystore, store password, key alias, key password, Play service-account JSON).
- **External/manual**: one-time Play Console + GCP service-account setup performed by the user outside of this repo; this is a hard prerequisite for the `play-store-publishing` capability and is called out explicitly in tasks so implementation isn't blocked silently on it.
- **Local dev**: contributors building `assembleRelease`/`bundleRelease` locally will need the same env vars CI uses (or a documented local `keystore.properties`-style override), otherwise release builds fail fast instead of silently producing an unsigned artifact.
