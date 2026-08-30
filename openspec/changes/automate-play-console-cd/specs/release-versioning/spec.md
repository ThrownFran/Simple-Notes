## ADDED Requirements

### Requirement: versionCode SHALL be computed automatically for CD builds
The release `versionCode` used by the CD publish workflow SHALL be computed as `1000 + <GitHub Actions run number>` at build time, rather than read from a hand-maintained constant in `app/build.gradle`. The computed value SHALL be strictly greater than the last hand-maintained `versionCode` checked into the repository at the time this capability is introduced.

#### Scenario: Publish workflow run computes versionCode
- **WHEN** the CD publish workflow builds a release AAB
- **THEN** the resulting artifact's `versionCode` equals `1000 + github.run_number` for that workflow run

#### Scenario: Repeated publish runs never collide
- **WHEN** the publish workflow runs multiple times (e.g. a retry after a failed upload)
- **THEN** each run produces a strictly higher `versionCode` than any previous run, because `github.run_number` is monotonically increasing per workflow

### Requirement: versionName SHALL be derived from the tag, or an explicit input, not a drifting constant
On a tag-triggered publish run, `versionName` SHALL be derived from the pushed tag (stripping the leading `v` from `v*.*.*`) rather than read from a separately-maintained `app/build.gradle` constant, so the shipped version can never disagree with the tag that shipped it. On a `workflow_dispatch` run, `versionName` SHALL be taken from a required workflow input, since there is no tag to derive it from. The `app/build.gradle` constant SHALL remain only as the fallback used by local/unsigned builds.

#### Scenario: Tag-triggered run derives versionName from the tag
- **WHEN** the CD publish workflow runs from a pushed tag `v1.2.0`
- **THEN** the resulting release's `versionName` is `1.2.0`

#### Scenario: Manual run requires an explicit version input
- **WHEN** the CD publish workflow is triggered via `workflow_dispatch` without a `version_name` input provided
- **THEN** the workflow run fails to start (GitHub enforces the required input) rather than falling back to a stale or ambiguous version

#### Scenario: Publish workflow surfaces versionName
- **WHEN** the CD publish workflow runs
- **THEN** the workflow's output/summary includes the `versionName` actually used for that run (from the tag or the manual input), not the `app/build.gradle` fallback constant
