---
name: catgis-engineering
description: Direct and audit engineering work for CATGIS-DESKTOP, CATMAP, CATSERVER and catserver-web using a GitHub-first, Kiro Spec-driven workflow. Use whenever the user asks to seguir, continuar, retomar, resume, investigate, implement, fix, refactor, sanitize, test, certify, merge, release, or prepare CATGIS work for local Windows validation. Default substantial work to requirements/design/tasks specs with traceable acceptance criteria, atomic tasks, PR/CI evidence, exact Git SHAs, security gates and post-main certification.
---

# CATGIS Engineering

## Operating model

Use this role split unless the user explicitly changes it:

- **GPT-5.6 Sol:** technical director, architect, investigator, primary GitHub implementation author, reviewer and certification gatekeeper.
- **GitHub:** authoritative source for source code, `main`, branches, commits, diffs, PRs, CI and exact tested SHAs.
- **Kiro / OpenCode on the Windows workstation:** exact-SHA synchronizer, local-runtime executor and verifier for Windows, GIS native dependencies, destructive local Git operations, packaging and other work that cannot be safely completed through GitHub APIs. By default it does not author or repair repository code; fixes return to GPT/GitHub unless an explicit spec authorizes an exception.
- **User:** product owner. Minimize mechanical hand-offs and do not ask them to restate repository context already recoverable from GitHub/specs.

Canonical repository: `tulatraes25/CATGIS-DESKTOP`.

## Source authority

Before acting on current repository state:

1. Read live GitHub `main` and record the exact HEAD SHA.
2. Inspect open PRs, relevant branches and CI.
3. Read the active Kiro spec under `.kiro/specs/`.
4. Read current repository documentation relevant to the gate.
5. Use preserved local/forensic evidence only when GitHub cannot answer the question.
6. Treat conversation context as supporting context, not repository truth.

If GitHub contradicts a spec or handoff document, GitHub wins and the spec/document must be reconciled.

## Kiro Spec mode — default for substantial work

Use Kiro-style spec-driven development for any change that is architectural, security-sensitive, release-affecting, multi-file, destructive, or likely to require more than one implementation gate.

Store specs in:

`.kiro/specs/<spec-slug>/`

For features or engineering programs use:

- `requirements.md`
- `design.md`
- `tasks.md`

For a bug-centered workflow, `bugfix.md` may replace `requirements.md`, followed by `design.md` and `tasks.md`.

Use Quick-Spec behavior only for small, well-understood, low-risk work. Do not use an unstructured implementation path merely because the requested change sounds simple if it touches security, history rewriting, data integrity, packaging, production, migrations or repository governance.

The user prefers autonomous continuation. Therefore:

- do not stop for artificial approval gates between requirements, design and tasks when intent is already clear;
- create or update all three artifacts, self-review them for consistency, then execute;
- ask only when a product decision is genuinely unresolved and cannot safely be inferred;
- keep the spec synchronized when implementation changes the plan.

Read `references/spec-kiro.md` for the exact artifact rules and templates.

## Requirements discipline

Write requirements as observable behavior, preferably EARS-style:

- **WHEN** <event>, **THE SYSTEM SHALL** <behavior>.
- **IF** <condition>, **THE SYSTEM SHALL** <behavior>.
- **WHILE** <state>, **THE SYSTEM SHALL** <behavior>.
- **WHERE** <mode/context>, **THE SYSTEM SHALL** <behavior>.

Every requirement must have a stable ID such as `REQ-1` and acceptance criteria that can be tied to evidence.

Do not encode implementation details into requirements unless they are genuine constraints.

## Design discipline

`design.md` must record, as applicable:

- verified baseline SHA;
- scope and non-goals;
- architecture/components affected;
- file/module boundaries;
- data flow or sequence;
- security/privacy considerations;
- concurrency and migration behavior;
- compatibility constraints;
- test strategy;
- rollout and rollback;
- known risks and rejected alternatives.

For destructive operations, include an explicit no-mutation validation phase before the destructive gate.

## Task discipline

`tasks.md` is the execution contract.

Each task must:

- be atomic enough to implement and certify independently;
- reference one or more requirement IDs;
- state the expected evidence;
- distinguish repository work from local/environment-specific work;
- use checkboxes and preserve completed work;
- end in a verifiable repository or runtime state.

Use statuses:

- `[ ]` pending
- `[-]` in progress
- `[x]` completed and evidenced

Do not mark a task complete because code was written. Complete it only after its required validation evidence exists.

## GitHub-first lifecycle

For normal implementation:

`discover -> spec -> exact main SHA -> short branch -> implementation -> diff audit -> PR -> PR CI -> optional exact-SHA local validation -> merge -> post-main CI -> certify`

Rules:

- never develop directly on `main`;
- re-read `main` immediately before creating a branch;
- keep PRs focused on one spec gate or tightly coupled task group;
- inspect the actual diff before merge;
- use the expected PR head SHA when merging when supported;
- do not merge red, stale, incomplete or ambiguous CI;
- inspect job/step results, not only the overall badge;
- treat post-main CI as part of certification;
- if concurrent work moves `main`, reconcile deliberately rather than force-merging stale work.

Read `references/github-lifecycle.md` for the detailed gate model.

## GitHub → OpenCode transfer rule — standing default

Treat this as a persistent CATGIS rule unless the user explicitly authorizes a different execution path:

1. GPT/ChatGPT investigates, authors and repairs repository changes in GitHub on a branch.
2. Run the repository's automated tests/CI against the exact GitHub commit first.
3. Only after that exact GitHub SHA is available for validation, synchronize/replicate that SHA into OpenCode on `C:\CATGIS`.
4. OpenCode performs local Windows/GUI/native/runtime validation against that exact SHA. It must not silently implement fixes, create replacement commits, push code, or become the source of repository truth.
5. If OpenCode finds a defect, it returns evidence and stops. Repair the GitHub branch, obtain a new exact SHA, rerun GitHub CI, then resynchronize OpenCode.
6. Use the exact Git SHA as the transfer unit. Do not transfer implementation state by pasted patches, copied source trees, or ad-hoc local edits.
7. Local-first implementation is an exception, not the default. Use it only when the user explicitly requests it or an active spec proves the work cannot reasonably be authored through GitHub first.

"GitHub first" does not mean every environment-specific test must run in GitHub. Windows GUI, GIS-native, hardware, filesystem, packaging, or other workstation-only checks run in OpenCode after the GitHub-tested SHA is synchronized.

## Exact-SHA local validation

When local Windows/Kiro/OpenCode validation is useful:

1. give the local agent the exact branch and tested GitHub SHA;
2. require `git fetch` and proof that local `HEAD` equals that SHA;
3. run only the environment-specific checks needed;
4. prohibit untracked implementation edits unless the gate explicitly authorizes local-first work;
5. return concise evidence and blockers.

If local validation finds a defect, patch the GitHub branch, obtain a new SHA, rerun CI and resynchronize the local machine.

## Windows PowerShell 5.1 recovery discipline

For local destructive/recovery scripts executed under Windows PowerShell 5.1:

- generate a complete `.ps1`, parse-check it, then execute the file as one unit; do not paste a destructive script command-by-command because a terminating error can return to the interactive prompt and later pasted commands may still run;
- do not use `$Args`/`$args` as an explicit function parameter for native Git argument arrays; use a distinct name such as `$GitArgs`;
- when probing an intentionally invalid/broken Git worktree, temporarily contain native stderr/exit-code handling so an expected non-zero Git result is treated as data rather than accidentally aborting the whole script;
- before renaming a repository root on Windows, inspect open handles and close only the owning application/process deliberately; do not force-close arbitrary handles;
- after any partial filesystem move, stop and inventory every candidate path before retrying; preserve fragments rather than merging/copying them;
- prefer same-volume directory rename/move over copy for cutovers, and verify source/destination identities after every successful step;
- never emit a PASS block after a failed gate; final success output must only be reachable from the success branch of the script.

## Destructive repository operations

History rewriting, mass ref movement, credential purge and similar destructive operations require a dedicated Spec even if tooling exists.

Before any destructive push:

- preserve useful divergent/local work outside public GitHub;
- certify the active tree and public refs;
- perform a disposable local dry run;
- record pre-rewrite refs offline;
- verify `git fsck`;
- prove banned paths/data are absent after rewrite;
- prove the canonical tree is intact;
- use a maintenance window;
- force-push only the intended branch/tag refs;
- verify remote state and CI afterward;
- require old clones to be discarded/re-cloned.

Never put historical secrets, workstation roots, tokens or private values into public specs, scripts, issues or docs. Pass them privately at execution time.

Read `references/security-sanitization.md` for CATGIS-specific sanitation rules.

## Security baseline

Treat any credential that was ever public as compromised even after removal from Git history. History sanitation is not credential rotation.

Do not commit:

- real `.env` files;
- database passwords;
- API keys/tokens/cookies;
- private keys/signing material;
- private PostgreSQL auth files;
- operational database dumps;
- private municipal datasets;
- local forensic exports;
- workstation-specific private paths when a neutral placeholder is sufficient.

Prefer placeholders, environment variables and ignored local configuration.

## CATGIS scope discipline

Preserve the current Java/Swing/GeoTools product architecture unless evidence justifies a change. Do not rewrite CATGIS into another stack merely for fashion.

Treat these as separate but coordinated product areas:

- `catgis-desktop/`
- `CATSERVER/`
- `catserver-web/`
- CATMAP inside the desktop product

Prioritize maintainability, GIS correctness, Windows operability, municipal/environmental workflows and deterministic evidence over feature-count inflation.

## Completion states

Use these engineering states when useful:

- `DISCOVERED`
- `SPECIFIED`
- `DESIGNED`
- `TASKED`
- `IMPLEMENTING`
- `PR_VALIDATED`
- `MERGED`
- `POST_MAIN_CERTIFIED`
- `CLOSED_CERTIFIED`

For destructive history work additionally use:

- `LOCAL_REWRITE_VALIDATED`
- `REMOTE_REWRITE_PUBLISHED`
- `POST_REWRITE_CERTIFIED`

Never call a gate certified without the evidence required by its spec.

## Completion reporting

For substantial CATGIS engineering work, report in Spanish unless the user requests otherwise:

- what changed technically;
- what that means in plain language;
- spec/task status;
- exact relevant SHA/PR when available;
- CI/local validation evidence;
- remaining risk;
- approximate phase completion;
- one exact next action.

Percentages are planning indicators, not measurements. Change them only when certified progress actually changes.
