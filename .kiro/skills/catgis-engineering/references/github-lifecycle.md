# GitHub lifecycle for CATGIS

## Baseline

GitHub is authoritative for CATGIS repository state.

At the start of every engineering gate:

1. fetch live `main`;
2. record exact SHA;
3. inspect open PRs and concurrent work;
4. inspect relevant CI;
5. reconcile the active Kiro spec.

## Execution authority and transfer

Default authority split:

- GPT/ChatGPT authors and repairs repository changes in GitHub.
- GitHub branch + exact commit SHA + CI are the implementation/testing source of truth.
- OpenCode synchronizes the exact GitHub SHA and performs Windows/local/runtime validation.
- OpenCode must not author repository fixes by default. If local validation finds a defect, report evidence and stop; repair in GitHub, produce a new SHA, rerun CI, then resynchronize.
- Local-first code/spec changes require an explicit user instruction or a spec-declared exception proving the change cannot reasonably be authored GitHub-first.
- Never use copied source trees or pasted patches as the normal transfer mechanism; use the exact Git SHA.

Repository automated tests run on the GitHub commit before OpenCode receives it. Environment-only checks (Windows GUI, GIS-native dependencies, hardware, local filesystem, packaging) may run only after that GitHub-tested SHA is synchronized.

## Implementation route

Default:

1. branch from exact current `main`;
2. implement the smallest coherent task set;
3. inspect branch diff;
4. open focused PR;
5. wait for required CI;
6. patch the GitHub branch if CI exposes a real issue and rerun CI on the new exact SHA;
7. synchronize the exact GitHub-tested SHA to Windows/Kiro/OpenCode when local/runtime validation is required;
8. if local validation finds a defect, stop local mutation, return evidence to GitHub, patch there, rerun CI, and resynchronize the new SHA;
9. merge using expected head SHA;
10. inspect post-main CI;
11. mark tasks/spec state accordingly.

## Concurrency

CATGIS may receive concurrent repository changes.

Before any write that assumes a baseline:

- re-read `main`;
- if it moved, determine whether the active branch is still cleanly based on the needed state;
- recreate/rebase conceptually through a new branch when safer;
- never hide unrelated concurrent changes in a PR.

## Merge gate

Do not merge when:

- the diff contains unrelated functionality;
- CI is red/incomplete;
- the head changed since review;
- a security-sensitive task lacks its required evidence;
- the spec says local validation is blocking and it has not occurred.

## Main protection target

Normal steady state should require:

- PRs into `main`;
- CATGIS CI;
- no routine force-push to `main`;
- no routine direct development on `main`.

A temporary exception may be required only for a controlled history rewrite described by an active destructive-operation spec.
