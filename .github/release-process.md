# SIP Framework Release Process

This project supports two release flows:

- **Mainline releases** (cut from `develop`)
- **Backport/hotfix releases** (cut from an older train/tag)

Below is the end-to-end process and the key guardrails built into the workflows.

## Mainline release (from `develop`)

1. **Create the release branch**  
   - Branch name: `release/<version>` (e.g., `release/4.2.0`).  
   - Push the branch; the `prepare-release` workflow runs and:
     - Records the develop base commit (`.release-base`, `.release-develop-head`).
     - Stamps all modules to `<version>`.
     - Initializes `changelogs/current-release-changelog.md` for the version.
     - Applies Spotless (optional toggle).
     - Generates the release changelog from fragments and removes them.
     - Commits and pushes the changes.

2. **Verify / add changes if needed**  
   - Avoid changing a mainline release branch after prepare; changes there will **not** flow back to `develop`. If you must, re-run the prepare workflow to consume any new fragments.

3. **Trigger the release workflow manually**  
   - Run `Release` workflow_dispatch targeting your `release/<version>` branch.
   - Inputs: `dry-run`, `auto-publish`, and check toggles remain available.
   - The workflow checks:
     - No changelog fragments remain.
     - `current-release-changelog.md` and `CHANGELOG.md` are up to date.
     - Version matches POMs/modules.
     - Guard: if a higher semver tag already exists and this branch was cut from develop head, it fails (stale mainline).
   - On non-dry-run it:
     - Builds/tests, deploys (with optional auto-publish to Sonatype).
     - Tags `releases/<version>`.
     - Creates GitHub release (only marked “Latest” if semver-highest).
     - Syncs the release changelog to `develop` by prepending the release block, leaving newer fragments on `develop` untouched.
     - Bumps `develop` to the next patch `-SNAPSHOT`.

4. **Post-release**  
   - Do not modify the release branch after it’s tagged; re-releasing under the same tag is not supported.

## Backport / hotfix release (older train)

1. **Create the branch**  
   - Branch name: `release/<older-version>` based on the target commit/tag.
   - `prepare-release` runs with the same steps as mainline (versions, changelog, fragments removal, commit).

2. **Release**  
   - Manually trigger `Release` workflow on that branch.  
   - Guards:
     - It will **not** sync changelog or bump `develop` (because `make_latest` is false).
     - Higher-semver guard does not block backports (it only blocks stale mainline).

3. **Post-release**  
   - Keep changes confined to the backport branch; they won’t propagate to `develop`.
   - Don’t modify the branch after tagging; re-releasing under the same tag isn’t supported.

## Notes and cautions

- Changelog fragments (`changelogs/major|feature|bugfix|documentation`) are consumed during prepare. Keep fragments on the release branch until it’s ready; re-running prepare is safe (additive).
- Release workflow is read-only on the release branch; it fails if preparation wasn’t done.
- Concurrency: avoid multiple mainline release branches at once; stale mainline is blocked.
- If you rebase a release branch onto a newer develop, re-run prepare to refresh `.release-base`/`.release-develop-head`.

## Quick commands (local preview)

- Regenerate changelog on a release branch (non-destructive if fragments remain):  
  ```bash
  cd changelogs
  ./update-changelog.sh <version>
  ```
- Dry-run release workflow: trigger `Release` with `dry-run=true` (no deploy/tag).
