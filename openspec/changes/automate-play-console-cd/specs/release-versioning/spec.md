## ADDED Requirements

### Requirement: versionCode SHALL be computed automatically for CD builds
The release `versionCode` used by the CD publish workflow SHALL be computed as `1000 + <GitHub Actions run number>` at build time, rather than read from a hand-maintained constant in `app/build.gradle`. The computed value SHALL be strictly greater than the last hand-maintained `versionCode` checked into the repository at the time this capability is introduced.

#### Scenario: Publish workflow run computes versionCode
- **WHEN** the CD publish workflow builds a release AAB
- **THEN** the resulting artifact's `versionCode` equals `1000 + github.run_number` for that workflow run

#### Scenario: Repeated publish runs never collide
- **WHEN** the publish workflow runs multiple times (e.g. a retry after a failed upload)
- **THEN** each run produces a strictly higher `versionCode` than any previous run, because `github.run_number` is monotonically increasing per workflow

### Requirement: versionName SHALL remain a manually-maintained value
`versionName` SHALL continue to be set directly in `app/build.gradle` as a human-maintained semantic version string. The CD publish workflow SHALL read and surface this value (e.g. in the workflow run summary or release tag reference) without attempting to derive, auto-bump, or override it.

#### Scenario: Publish workflow surfaces versionName
- **WHEN** the CD publish workflow runs
- **THEN** the workflow's output/summary includes the `versionName` currently set in `app/build.gradle`, unmodified
