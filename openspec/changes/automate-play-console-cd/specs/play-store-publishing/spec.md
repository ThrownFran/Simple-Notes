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

### Requirement: Promotion beyond internal testing SHALL be a separate, explicit action
The system SHALL provide a distinct, separately-triggered mechanism (not a follow-on step of the internal-track publish workflow) for promoting a release from internal testing to closed testing or production tracks on Play Console.

#### Scenario: Successful internal publish does not auto-promote
- **WHEN** the internal-track publish workflow completes successfully
- **THEN** the release remains on the internal testing track only; no closed-testing or production track is updated as a result

#### Scenario: Maintainer explicitly promotes a release
- **WHEN** a maintainer manually triggers the promotion workflow/job and specifies the target track
- **THEN** the previously-published internal-testing release is promoted to the specified track on Play Console

### Requirement: Play Developer API access SHALL be established as a prerequisite, not part of the pipeline
Access to the Play Developer API (a GCP service account granted permission on this app in Play Console, with its JSON key stored as a GitHub Actions secret) SHALL be configured by the maintainer outside of the codebase before the publish workflow can succeed. The pipeline SHALL NOT attempt to provision this access itself.

#### Scenario: Publish workflow run without configured API access
- **WHEN** the publish workflow runs before the Play Developer API service account has been configured and granted permission
- **THEN** the upload step fails with an authentication/permission error surfaced in the workflow logs, and no other capability in this change is blocked by that failure
