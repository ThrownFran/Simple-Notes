## ADDED Requirements

### Requirement: A signed release AAB SHALL be publishable to Play's internal testing track via CD
The system SHALL provide a GitHub Actions workflow, using the Gradle Play Publisher plugin, that builds a signed release AAB and uploads it to Play Console's internal testing track, authenticating with a Play Developer API service account.

#### Scenario: Publish workflow triggered by version tag
- **WHEN** a maintainer pushes a tag matching `v*.*.*` to the repository
- **THEN** the publish workflow runs, builds a signed release AAB using the `release-signing` and `release-versioning` capabilities, and uploads it to Play Console's internal testing track

#### Scenario: Publish workflow triggered manually
- **WHEN** a maintainer manually triggers the publish workflow via `workflow_dispatch`
- **THEN** the workflow performs the same build-and-upload-to-internal-testing sequence as the tag-triggered run

#### Scenario: Plain push to master does not publish
- **WHEN** a commit is pushed directly to `master` (not tagged)
- **THEN** no publish workflow run is triggered and nothing is uploaded to Play Console

### Requirement: Promotion beyond internal testing SHALL NOT happen automatically
Publishing to internal testing SHALL NOT, by itself or as a side effect, promote a release to closed testing, open testing, or production. Automating promotion (e.g. a separate `workflow_dispatch` workflow) is out of scope for this change and deferred to a future change; until then, promotion is a manual action performed directly in the Play Console UI.

#### Scenario: Successful internal publish does not auto-promote
- **WHEN** the internal-track publish workflow completes successfully
- **THEN** the release remains on the internal testing track only; no closed-testing or production track is updated as a result

### Requirement: Play Developer API access SHALL be established as a prerequisite, not part of the pipeline
Access to the Play Developer API (a GCP service account granted permission on this app in Play Console, with its JSON key stored as a GitHub Actions secret) SHALL be configured by the maintainer outside of the codebase before the publish workflow can succeed. The pipeline SHALL NOT attempt to provision this access itself.

#### Scenario: Publish workflow run without configured API access
- **WHEN** the publish workflow runs before the Play Developer API service account has been configured and granted permission
- **THEN** the upload step fails with an authentication/permission error surfaced in the workflow logs, and no other capability in this change is blocked by that failure
