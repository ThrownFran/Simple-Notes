# AGENTS.md

How we work in this repo. See `CLAUDE.md` for architecture, build commands, and coding conventions — this file covers the branch/PR workflow.

## Workflow

1. **Branch from `master`.** `master` is the trunk; always cut new branches from the latest `origin/master`, not from another feature branch.

   ```sh
   git fetch origin master
   git checkout -b <type>/<short-description> origin/master
   ```

   Use a `type/` prefix matching Conventional Commits where it makes sense (`fix/`, `feat/`, `chore/`), e.g. `fix/about-email-address`.

2. **Commit on that branch.** Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`, etc.) for commit messages — see `CLAUDE.md`'s Commit style section. Existing history predates this convention, so don't pattern-match old messages.

3. **Pass the quality gates before opening a PR.** CI (`.github/workflows/ci.yml`) runs on every PR into `master` and requires:
   - `./gradlew ktlintCheck`
   - `./gradlew test`
   - `./gradlew assembleDebug`
   - `./gradlew assembleRelease`

   Run these locally before pushing so the PR doesn't open red. Note: `CategoriesViewModelTest.kt` currently fails to compile (pre-existing, unrelated to most changes — see `CLAUDE.md`).

4. **Test visually when the change is user-facing.** If the change touches UI, behavior, or anything not fully covered by unit tests, build and run the app (or use the `run` skill) to exercise the change before opening the PR. Pure resource/string/config changes with no logic impact don't need this.

5. **Open a PR against `master` on GitHub**, not against another feature branch. Use `gh pr create --base master`.

## Notes

- `gh` in this environment may pick up a stale/invalid `GITHUB_TOKEN` env var over the keyring login — if `gh auth status` or `gh pr create` fails with "Bad credentials", `unset GITHUB_TOKEN` and retry.
- Don't force-push or rewrite history on branches once a PR is open, unless explicitly asked.
