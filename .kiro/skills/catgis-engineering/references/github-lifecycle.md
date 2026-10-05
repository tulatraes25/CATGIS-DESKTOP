# GitHub lifecycle for CATGIS

## Baseline

GitHub is authoritative for CATGIS repository state.

At the start of every engineering gate:

1. fetch live `main`;
2. record exact SHA;
3. inspect open PRs and concurrent work;
4. inspect relevant CI;
5. reconcile the active Kiro spec.

## Implementation route

Default:

1. branch from exact current `main`;
2. implement the smallest coherent task set;
3. inspect branch diff;
4. open focused PR;
5. wait for required CI;
6. patch the branch if CI exposes a real issue;
7. optionally synchronize exact tested SHA to Windows/Kiro/OpenCode;
8. merge using expected head SHA;
9. inspect post-main CI;
10. mark tasks/spec state accordingly.

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
