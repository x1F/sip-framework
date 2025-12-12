# SIP Framework Release Process

Supports two flows:
- **Mainline** (from `develop`)
- **Backport/hotfix** (from an older train/tag)

## Mainline (from `develop`)

1) Create `release/<version>` branch; push it. `prepare-release` runs:
   - Records provenance (`.release-base`, `.release-develop-head`).
   - Sets all module versions to `<version>`.
   - Initializes `changelogs/current-release-changelog.md` for that version (if needed).
   - Builds or updates changelog from fragments, removes them, commits, pushes.

2) Avoid changing the release branch; if you must (preferably don’t for mainline), re-run prepare to consume any new fragments.

3) Manually trigger `Release` on the branch (inputs: dry-run, auto-publish, check toggles, optional next snapshot override).
   - Guards: no fragments, changelog up-to-date, versions match; stale-mainline fails if a higher semver exists and branch was cut from develop head; provenance files required; base must be ancestor (rebases require re-running prepare).
   - Execution order (non-dry-run):
     - Build/test/validate (formatter, javadoc, licenses, Spring Boot alignment).
     - Create tag `releases/<version>`.
     - GitHub release (Latest only if semver-highest).
     - Publish Javadoc to gh-pages.
     - Sync changelog to `develop` by prepending the release block (newer fragments on develop are kept); update `mkdocs.yml`.
     - Bump `develop` to next patch `-SNAPSHOT` (or override if provided).
     - Deploy to Maven Central (last step, after everything else succeeds).

4) Post-release: don’t touch the release branch after tagging; re-releasing under the same tag is not supported.

## Backport / hotfix (older train)

1) Create `release/<older-version>` from the target commit/tag; push. `prepare-release` runs the same steps (versions, changelog, fragments removal, commit).
2) Trigger `Release` on that branch. It will not sync changelog or bump `develop` (since it’s not latest). Stale-mainline guard won’t block backports.
3) Post-release: keep changes confined to the backport branch; they won’t propagate to `develop`. Don’t modify after tagging.

## Failure recovery (mainline)
- If the release fails before tagging: fix and rerun; nothing external published.
- If tagging/GitHub release/Javadoc/changelog sync/snapshot bump succeed but Maven Central deploy fails: rerun release to retry deploy, or deploy manually; changelog/snapshot/tag already exist.
- If deploy succeeds but earlier steps failed (shouldn’t happen with current ordering): manual cleanup required (tag/GitHub release/changelog/snapshot).

## Notes
- Changelog fragments (`changelogs/major|feature|bugfix|documentation|other`) are consumed during prepare; re-running is additive.
- Release workflow is read-only on the release branch; it fails if prep is incomplete.
- Concurrency: one release workflow at a time across branches.
- Rebase a release branch? Re-run prepare to refresh `.release-base`/`.release-develop-head`.
