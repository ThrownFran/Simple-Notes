## Context

Simple Notes is a single-module Android app (`applicationId brillembourg.notes.simple.fast`), built with Gradle + kapt (Hilt), currently on Java/Kotlin target 21. CI already exists (`.github/workflows/ci.yml` on GitHub Actions): every push/PR to `master` runs `ktlintCheck`, `test`, `assembleDebug`, and `assembleRelease` (unsigned), and `master` branch protection requires that `build` check to pass (strict mode) before merge. There is no reviewer-approval requirement — this is a solo-maintained repo.

Today, release builds are unsigned as checked in (no `signingConfigs` block), `versionCode`/`versionName` are hand-edited constants in `app/build.gradle`, and there is no connection at all to Google Play — the app already has a listing in Play Console, but no service account / API access has been configured for it.

This design covers how to get from "CI validates the code" to "a maintainer can trigger a CD run that ships a real, signed build to Play's internal testing track," while keeping production releases behind a deliberate manual gate.

## Goals / Non-Goals

**Goals:**
- Release builds are signed with the maintainer's existing keystore, both locally and in CI, with the private key material never committed to the repo.
- Every CD run produces a strictly increasing `versionCode` so repeated internal-track uploads are always accepted by Play.
- A maintainer can trigger a CD run (not on every push) that builds a signed AAB and uploads it to Play's internal testing track.
- Promotion beyond internal testing (closed testing, production) is always a separate, explicit action — never a side effect of the internal-track publish.
- The existing CI workflow (build/test on every push/PR) is untouched by this work; CD is additive.

**Non-Goals:**
- Creating a new release keystore — the maintainer already has one; this only wires an existing key in.
- Automating Play Store listing content (screenshots, descriptions, release notes copy) — Fastlane-style metadata management is explicitly out of scope; this is upload-only via Gradle Play Publisher.
- Auto-promoting from internal → production, or any staged rollout automation.
- Multi-flavor / multi-module signing complexity — the project is single-module today.

## Decisions

**Gradle Play Publisher (triple-t/gradle-play-publisher) over Fastlane.**
Fastlane brings a Ruby toolchain and its own credential/config format into a project that is otherwise pure Gradle/Kotlin; Gradle Play Publisher is a Gradle plugin that reads a service-account JSON and publishes via Gradle tasks directly. Given this repo has no Ruby dependency today and the requirement is "upload a build," the Gradle-native option is the smaller addition. Trade-off: less tooling for store-listing metadata management if that's ever wanted later — acceptable since it's explicitly a non-goal now.

**Signing config sourced from `keystore.properties` + macOS Keychain (local) / injected secrets (CI), not committed anywhere.**
A gitignored `keystore.properties` file at the repo root (with a committed `keystore.properties.example` template) holds only the non-secret fields — `storeFile`, `keyAlias` — for local builds. `storePassword`/`keyPassword` are deliberately **not** stored in that file at all: on the maintainer's macOS dev machine, `app/build.gradle` reads them from Keychain at build time via `security find-generic-password`, scoped to release-packaging tasks only (`gradle.taskGraph.whenReady`) so `test`/`ktlintCheck`/`assembleDebug` never touch Keychain or prompt for access. In CI, the workflow decodes a base64-encoded keystore secret to a file and writes a full `keystore.properties` (including passwords) from secrets to the ephemeral runner disk before invoking Gradle — there's no Keychain on Linux runners, so CI keeps the plaintext-file path, scoped to the lifetime of that single job. Rationale for splitting local vs. CI: a plaintext password file sitting persistently on a developer's laptop is a meaningfully different risk than the same plaintext existing for the few minutes of a single ephemeral CI job — Keychain gives the local case OS-level encryption-at-rest and per-app access control that a flat file can't. Alternative considered: plaintext `keystore.properties` with all four fields locally (the more common convention) — rejected in favor of the added Keychain step at the maintainer's explicit request, since it removes password material from disk entirely on the one machine where it persists indefinitely. Alternative considered: reading raw environment variables directly in `build.gradle` for everything — rejected because it forces extra code paths for what's conceptually the same input, and `keystore.properties` (for the non-secret fields) is the pattern most Android devs already expect.

**`versionCode` derived from the GitHub Actions run number, not a manually incremented constant.**
`versionCode = <baseline> + github.run_number` (baseline chosen so it stays above the current checked-in value of 9) is monotonic across CD runs by construction, requires no extra state (Play Console API round-trip to read the last version, a committed counter file, etc.), and is trivially reproducible from the workflow run that produced a given build. Alternative considered: querying Play Developer API for the latest published `versionCode` and incrementing — rejected as unnecessary complexity and an extra failure mode (network call before the build even starts) for a solo-maintainer project. `versionName` stays a manually-edited value (semantic, human-facing) but CD should surface it in the workflow run summary so it's visible without cross-referencing the repo.

**Internal-track publish is `workflow_dispatch`-triggered (or on a version tag), not on every `master` push.**
Publishing to Play — even to internal testing — is an externally-visible, semi-irreversible action (testers get notified/updated). Tying it to every merge would mean every merged PR ships to testers automatically, which doesn't match the deliberate-gate pattern already established for this repo (required CI check, PR-based merges). A manual trigger (or optionally a `v*` tag push) keeps "merge to master" and "ship a build" as separate decisions, mirroring how branch protection already separates "code is green" from "code is on master."

**Promotion to closed/production tracks is a distinct, separate workflow/job — never automatic.**
Even after internal testing looks good, promoting to production is treated as its own deliberate action (separate `workflow_dispatch`), not a follow-on step of the publish workflow. This avoids a single click accidentally fanning out to production and keeps the blast radius of the "publish" workflow scoped to internal testers only.

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

1. Land `release-signing` + `release-versioning` first (Gradle changes only, additive, no CD workflow yet) — verify `assembleRelease`/`bundleRelease` still work locally with a `keystore.properties` file and that CI's existing `assembleRelease` step continues to pass once it also has secrets available (or is adjusted to skip signing verification if CI intentionally doesn't sign on every push — decide explicitly in tasks).
2. Maintainer completes the manual Play Console/GCP service-account setup out-of-band.
3. Land `play-store-publishing`'s internal-track publish workflow, dry-run it via `workflow_dispatch` on a throwaway/test change, confirm the build lands on Play Console's internal testing track with the expected `versionCode`.
4. Land the separate promotion workflow/job last, once internal-track publishing has been exercised at least once successfully.

No rollback complexity beyond reverting the relevant workflow/Gradle changes — nothing here is a runtime/data migration; worst case a bad CD run produces a rejected or unwanted Play Console upload, which can be deactivated from Play Console directly.

## Open Questions

None outstanding — trigger strategy, secret scoping, and versioning scheme are resolved above using standard convention. Remaining unknowns are external and tracked as prerequisite tasks (Play Console/GCP service-account setup) rather than design decisions.
