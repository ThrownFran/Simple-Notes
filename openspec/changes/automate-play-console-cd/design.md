## Context

Simple Notes is a single-module Android app (`applicationId brillembourg.notes.simple.fast`), built with Gradle + kapt (Hilt), currently on Java/Kotlin target 21. CI already exists (`.github/workflows/ci.yml` on GitHub Actions): every push/PR to `master` runs `ktlintCheck`, `test`, `assembleDebug`, and `assembleRelease` (unsigned), and `master` branch protection requires that `build` check to pass (strict mode) before merge. There is no reviewer-approval requirement — this is a solo-maintained repo.

Today, release builds are unsigned as checked in (no `signingConfigs` block), `versionCode`/`versionName` are hand-edited constants in `app/build.gradle`, and there is no connection at all to Google Play — the app already has a listing in Play Console, but no service account / API access has been configured for it.

This design covers how to get from "CI validates the code" to "a maintainer can trigger a CD run that ships a real, signed build to Play's internal testing track," while keeping production releases behind a deliberate manual gate.

## Goals / Non-Goals

**Goals:**
- Release builds are signed with the maintainer's existing keystore in CI, with the private key material never committed to the repo and never persisted on a local machine.
- Every CD run produces a strictly increasing `versionCode` so repeated internal-track uploads are always accepted by Play.
- A maintainer can trigger a CD run (not on every push) that builds a signed AAB and uploads it to Play's internal testing track.
- The existing CI workflow (build/test on every push/PR) is untouched by this work; CD is additive.

**Non-Goals:**
- Creating a new release keystore — the maintainer already has one; this only wires an existing key in.
- Automating Play Store listing content (screenshots, descriptions, release notes copy) — Fastlane-style metadata management is explicitly out of scope; this is upload-only via Gradle Play Publisher.
- Automating promotion from internal testing to closed/open/production tracks, or any staged rollout automation — deliberately deferred to a future, separate change; for now, promotion is a manual action in the Play Console UI.
- Local, Gradle-CLI-driven signed release builds — a real signed local build (if ever needed) goes through Android Studio's Generate Signed Bundle/APK wizard instead.
- Multi-flavor / multi-module signing complexity — the project is single-module today.

## Decisions

**Gradle Play Publisher (triple-t/gradle-play-publisher) over Fastlane.**
Fastlane brings a Ruby toolchain and its own credential/config format into a project that is otherwise pure Gradle/Kotlin; Gradle Play Publisher is a Gradle plugin that reads a service-account JSON and publishes via Gradle tasks directly. Given this repo has no Ruby dependency today and the requirement is "upload a build," the Gradle-native option is the smaller addition. Trade-off: less tooling for store-listing metadata management if that's ever wanted later — acceptable since it's explicitly a non-goal now.

**Signing config sourced from `keystore.properties`, written only by CI, never persisted locally.**
`app/build.gradle`'s `signingConfigs.release` reads `storeFile`/`storePassword`/`keyAlias`/`keyPassword` from a gitignored `keystore.properties` at the repo root, if present. The dedicated CD publish workflow decodes a base64-encoded keystore secret to a file and writes that `keystore.properties` from repository secrets to the ephemeral runner disk before invoking Gradle, scoped to the lifetime of that single job. Locally, no `keystore.properties` is ever created as part of this workflow, so `assembleRelease`/`bundleRelease` on a developer's machine always produce an unsigned artifact — a real signed local build (e.g. to sanity-check a release-shaped/minified artifact before shipping) is produced via Android Studio's Build > Generate Signed Bundle/APK wizard, which already manages its own credentials independent of any Gradle config. Alternative considered: also supporting local signing through `keystore.properties` plus macOS Keychain for the passwords — implemented and verified working, but reverted at the maintainer's request once it became clear Android Studio's built-in signing wizard already covers the "test a real signed build locally" need without any Gradle-level plumbing, so the added complexity (Keychain reads, `CI`-env branching, a `gradle.taskGraph.whenReady` gate) bought nothing. Alternative considered: reading raw environment variables directly in `build.gradle` — rejected because `keystore.properties` is the pattern most Android devs already expect, and CI writing that one file format keeps the Gradle logic to a single code path.

**`versionCode` derived from the GitHub Actions run number, not a manually incremented constant.**
`versionCode = <baseline> + github.run_number` (baseline chosen so it stays above the current checked-in value of 9) is monotonic across CD runs by construction, requires no extra state (Play Console API round-trip to read the last version, a committed counter file, etc.), and is trivially reproducible from the workflow run that produced a given build. Alternative considered: querying Play Developer API for the latest published `versionCode` and incrementing — rejected as unnecessary complexity and an extra failure mode (network call before the build even starts) for a solo-maintainer project. `versionName` stays a manually-edited value (semantic, human-facing) but CD should surface it in the workflow run summary so it's visible without cross-referencing the repo.

**Internal-track publish is `workflow_dispatch`-triggered (or on a version tag), not on every `master` push.**
Publishing to Play — even to internal testing — is an externally-visible, semi-irreversible action (testers get notified/updated). Tying it to every merge would mean every merged PR ships to testers automatically, which doesn't match the deliberate-gate pattern already established for this repo (required CI check, PR-based merges). A manual trigger (or optionally a `v*` tag push) keeps "merge to master" and "ship a build" as separate decisions, mirroring how branch protection already separates "code is green" from "code is on master."

**Promotion to closed/production tracks is deferred to a future change, not built here.**
Even after internal testing looks good, promoting to production stays a manual Play Console action for now — this avoids a single click accidentally fanning out to production and keeps the blast radius of the "publish" workflow scoped to internal testers only. A `workflow_dispatch`-triggered promotion workflow is a reasonable follow-up once internal-track publishing has been exercised for a while, but isn't part of this change.

**Publish workflow triggers on pushing a `v*.*.*` tag, with `workflow_dispatch` as a manual fallback.**
Tag-triggered releases are the standard convention for shipping from a CI/CD pipeline (mirrors `npm publish`-on-tag, PyPI, and most mobile release pipelines): pushing `v1.2.0` is an unambiguous, auditable "ship this exact commit" signal, and the tag itself becomes the human-readable record of what was released. `workflow_dispatch` stays available alongside it for a manual re-run (e.g. retrying a failed upload) without needing to re-tag. Plain `master` pushes never trigger a publish, keeping "merge to master" and "ship a build" separate, as decided above.

**Only the tag-triggered/`workflow_dispatch` publish workflow gets the signing and Play service-account secrets — the existing `ci.yml` (running on every push/PR) stays unsigned.**
This is the standard secret-hygiene convention for GitHub Actions: secrets should only be exposed to workflows that run in trusted, deliberate contexts, not to every PR build (which, for a public repo, could in principle run from a fork). `ci.yml`'s existing `assembleRelease` step keeps validating that R8/shrinking doesn't break, unsigned, exactly as it does today; real signing only ever happens in the dedicated publish workflow.

**`versionCode = <baseline> + github.run_number`, with baseline `1000`.**
`1000` is comfortably clear of the current checked-in value (9), leaves headroom, and reads unambiguously as "generated, not hand-picked" to anyone reading `app/build.gradle` later. `versionName` remains a manually-edited semantic version in `app/build.gradle` (the standard approach for a simple, non-release-trained app) — the publish workflow reads it and surfaces it in the run summary/tag rather than trying to derive or auto-bump it.

## Risks / Trade-offs

- **[Risk] Keystore secret leakage via CI logs or a misconfigured workflow.** → Mitigation: keystore and passwords only ever exist as GitHub Actions encrypted secrets, decoded to a temp file inside the job's ephemeral workspace, never echoed or persisted as an artifact; add `keystore.properties` and the decoded `.jks`/`.keystore` to `.gitignore` explicitly as part of implementation.
- **[Risk] Losing the release keystore (no backup) would permanently break the ability to update the app on Play with the same signing identity.** → Mitigation: out of scope for this change to enforce, but tasks.md should include a reminder/checklist item for the maintainer to keep an offline backup of the keystore before wiring it into CI.
- **[Risk] `versionCode` baseline miscalculated, causing a CD run to produce a code Play already has.** → Mitigation: pick the baseline comfortably above the current value (9) and verify the very first CD run's resulting `versionCode` against Play Console before treating the pipeline as trustworthy.
- **[Risk] Play Console API access (service account + permissions) is a manual, external, one-time setup step done outside the codebase — implementation of `play-store-publishing` is blocked until the maintainer completes it.** → Mitigation: called out explicitly as a prerequisite task in tasks.md, sequenced before the workflow-authoring tasks that depend on it, so it isn't discovered as a surprise mid-implementation.
- **[Trade-off] No automated store-listing/metadata management (Fastlane-style).** → Acceptable given the stated non-goal; can be revisited later without re-architecting the signing/versioning/publish pieces built here.

## Migration Plan

1. Land `release-signing` + `release-versioning` first (Gradle changes only, additive, no CD workflow yet) — verify `assembleRelease`/`bundleRelease` continue to build unsigned locally (unchanged behavior) and that CI's existing `assembleRelease` step continues to pass unsigned.
2. Maintainer completes the manual Play Console/GCP service-account setup out-of-band.
3. Land `play-store-publishing`'s internal-track publish workflow, dry-run it via `workflow_dispatch` on a throwaway/test change, confirm the build lands on Play Console's internal testing track with the expected `versionCode`.
4. Promotion automation, if wanted, becomes its own future change once internal-track publishing has been exercised for a while.

No rollback complexity beyond reverting the relevant workflow/Gradle changes — nothing here is a runtime/data migration; worst case a bad CD run produces a rejected or unwanted Play Console upload, which can be deactivated from Play Console directly.

## Open Questions

None outstanding — trigger strategy, secret scoping, and versioning scheme are resolved above using standard convention. Remaining unknowns are external and tracked as prerequisite tasks (Play Console/GCP service-account setup) rather than design decisions.
