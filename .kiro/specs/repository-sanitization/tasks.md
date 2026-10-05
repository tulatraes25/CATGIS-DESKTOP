# Repository Sanitization — Tasks

Status: ACTIVE
Current state: `T10C_CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION / T10D_CLOSED_CERTIFIED_BY_PRIVACY_ISOLATION / T10E_CLOSURE_PR_IN_PROGRESS`

## Completed foundation

- [x] **T0 — Sanitize the active `main` tree**
  - Requirements: REQ-1, REQ-2, REQ-3
  - Evidence: credential file, decompiled third-party source, operational inventories and machine-specific public paths removed from active tree; CI-green sanitation commits.

- [x] **T1 — Neutralize public branch tips**
  - Requirements: REQ-2, REQ-9
  - Evidence: public branch refs aligned to sanitized canonical state; repository branch inventory records the verified ref state.

- [x] **T2 — Commit guarded rewrite automation and runbook**
  - Requirements: REQ-3, REQ-4, REQ-5
  - Evidence: `docs/repository/Invoke-HistorySanitization.ps1` and `HISTORY_SANITIZATION_RUNBOOK.md`.

- [x] **T3 — Introduce Kiro Spec governance and CATGIS engineering skill**
  - Requirements: supports all requirements through traceable execution
  - Evidence: PR #32; reviewed head `8ffb64c66aad596411c46cae883406d71dfec454`; merge `0b4acce89c2896f08764f730289c2304ed06885c`; PR CI #200 success; post-main CI #201 success.

## Corrective gate discovered during T4

- [x] **T2R — Repair historical source-root replacement rule generation**
  - Requirements: REQ-3, REQ-4
  - Trigger evidence: first T4 dry run on baseline `dc3cd310292791b0defccb4c049f7ff7a933884d` removed all banned paths and passed both fsck checks, but source-root anonymization remained present because the helper emitted `==` instead of the `==>` delimiter required by `git-filter-repo --replace-text`.
  - Required correction:
    - generate literal replacement rules with `==>`;
    - validate generated rules before invoking `git filter-repo`;
    - add a CI regression guard for the `==>` rule syntax and PowerShell parseability;
    - keep the replacement file local and private.
  - Completion evidence:
    - PR #34 merged and CI-green before rewrite;
    - T4R proved the corrected `==>` replacement file worked and the private source root was absent;
    - the remaining T4R blocker was isolated to the independent PowerShell pickaxe invocation and moved to T2R-2.

- [x] **T2R-2 — Preserve spaced historical source-root literals in git pickaxe verification**
  - Requirements: REQ-3, REQ-4
  - Trigger evidence: T4R from `e2ce99903ca47e49e26aebf08e61e3e24e3ba441` produced a clean rewritten mirror, but the helper aborted in [5/8] because PowerShell 5.1 split the interpolated `-S` argument when the historical source root contained spaces.
  - Required correction:
    - construct the pickaxe option as one explicit native argument (for example `"-S$Needle"`);
    - fail if `git log` exits non-zero instead of treating execution failure as a clean history result;
    - add CI guards against the legacy unquoted invocations.
  - Completion evidence:
    - PR #35 merged and PR CI #206 green;
    - T4R-2 reached `VALIDACION LOCAL COMPLETA`;
    - pickaxe spaced-literal verification passed;
    - independent scan of 554 rewritten commits found zero private source-root occurrences in both slash forms.

## Remaining destructive gate

- [x] **T4 — Execute local no-push history rewrite**
  - Requirements: REQ-1, REQ-2, REQ-3, REQ-4, REQ-9
  - Executor: Kiro/OpenCode or operator on Windows workstation
  - Action:
    - install/verify `git-filter-repo`;
    - invoke `Invoke-HistorySanitization.ps1` without `-PushRewrittenHistory`;
    - pass any private historical path/email values only locally if anonymization is desired.
  - Evidence:
    - T4R-2 = PASS from source `e1dd34e61f93121ccc41475956b2cafeb2fad0be`;
    - deterministic rewritten `main` = `c11318c843dafc11d1f9009974db5160601e91b4`;
    - helper completion, pre/post `git fsck --full`, canonical tree and private-path checks all passed;
    - refs preserved 41/41 branches and 0/0 tags;
    - zero remote mutation during the local gate.

- [x] **T5 — Review the rewritten mirror before publication**
  - Requirements: REQ-1, REQ-2, REQ-4
  - Executor: GPT review of returned local evidence + operator/Kiro
  - Evidence:
    - rewritten `main` tree contains required canonical components;
    - no banned paths;
    - no unexpected branch/tag loss;
    - no unresolved integrity warning.
  - Gate result: `LOCAL_REWRITE_VALIDATED`.
  - Evidence: GitHub remained frozen on the exact T4R-2 source SHA with no open PRs before publication; rewritten tree, refs and integrity evidence were accepted.

- [x] **T6 — Publish rewritten branches/tags in a maintenance window**
  - Requirements: REQ-5, REQ-9
  - Executor: local operator/Kiro
  - Precondition: T5 complete.
  - Action: rerun helper with explicit `-PushRewrittenHistory`.
  - Evidence:
    - preflight remote refs matched T4R-2 source state;
    - force-push dry-run passed with zero mutation;
    - helper terminated with `HISTORY_REWRITE_PUSH_COMPLETE`;
    - remote `main` became exactly `c11318c843dafc11d1f9009974db5160601e91b4`;
    - remote refs preserved 41 branches and 0 tags.
  - Gate result: `REMOTE_REWRITE_PUBLISHED`.

- [x] **T7 — Certify rewritten GitHub state**
  - Requirements: REQ-1, REQ-2, REQ-6
  - Executor: GPT/GitHub
  - Evidence:
    - GitHub reports canonical `main` = `c11318c843dafc11d1f9009974db5160601e91b4`;
    - 41 public branches, 0 tags, 0 open PRs;
    - 41 branches reduce to 8 unique public tips; GitHub commit-history queries return zero matches for the credential file and `kosmo_decompilado` on all 8 histories;
    - recursive Git trees for all 8 unique tips contain zero `CATSERVER/inventory/*.csv` files;
    - exact deterministic publication of the T4R-2 mirror carries forward its full historical inventory-CSV absence proof;
    - push-triggered CATGIS CI #208 on rewritten `main` completed successfully: Java Desktop Build & Test, Web Viewer Lint, and Repository Safety Checks all passed.
  - Gate result: `POST_REWRITE_CERTIFIED`.

- [x] **T8 — Enable protection/ruleset for `main`**
  - Requirements: REQ-8
  - Executor: GitHub repository administrator
  - Evidence:
    - ruleset ID `24466311`, name `CATGIS main protection`, target `branch`, enforcement `active`;
    - condition targets `~DEFAULT_BRANCH` only;
    - pull request required, merge method restricted to `merge`, required approving reviews = 0, review-thread resolution required;
    - required status checks: `Java Desktop Build & Test`, `Web Viewer Lint`, `Repository Safety Checks`;
    - strict up-to-date status-check policy enabled;
    - non-fast-forward and deletion rules enabled;
    - bypass actors = 0 and current user cannot bypass;
    - GitHub reports `main protected=true`;
    - GitHub's default `require_extra_approval_for_unattributed_changes=true` has no effect while required approving reviews = 0.
  - Gate result: `MAIN_PROTECTED`.

- [x] **T9 — Replace all active pre-rewrite clones**
  - Requirements: REQ-7, REQ-9
  - Executor: local operator/Kiro
  - Certified source SHA: `4b912d22a3fc63dcfceb61dcd49f2e4d947e9e30`.
  - T9A evidence:
    - old clone inventoried at HEAD `0bcd4c6f2b916f9fc0a4dc8bdd3cc05a4ecbcb0a`, branch `main`, 2 modified, 0 staged, 2 untracked;
    - old-clone push URL disabled before cutover;
    - fresh clone created exclusively from GitHub with no old-to-new file, commit or Git-object import;
    - fresh clone HEAD/origin-main matched the certified source SHA;
    - worktree clean, `git fsck --full` passed;
    - Windows `gradlew.bat --no-daemon clean test` passed (BUILD SUCCESSFUL; 0 failures; 14 environment-dependent integration tests skipped).
  - T9B recovery evidence:
    - Windows/OpenCode handles forced a two-phase physical cutover and exposed two script defects before the final pass; neither failed attempt mutated GitHub;
    - final recovery archived the broken pre-rewrite root as `C:\CATGIS_PRE_REWRITE_BROKEN_ROOT_20261004_184931`;
    - canonical `C:\CATGIS` now points to the fresh sanitized clone;
    - canonical HEAD = origin/main = `4b912d22a3fc63dcfceb61dcd49f2e4d947e9e30`, branch `main`, clean worktree, official fetch/push origin, `git fsck --full` PASS;
    - forensic fragments remain preserved outside active development;
    - `C:\CATGIS_PRE_REWRITE_QUARANTINE_20261004` is still a Git repository with push disabled;
    - `C:\CATGIS_PRE_REWRITE_QUARANTINE_20261004_182444` and the archived broken root are not valid Git repositories;
    - no file copy, commit import or Git-object import was used;
    - remote mutations during T9 = 0.
  - Accepted exception:
    - the original pre-rewrite repository metadata was physically fragmented by failed Windows directory moves before final recovery, so the exact old checkout is no longer intact as one Git repository;
    - REQ-7 remains satisfied because the operational checkout is a clean post-rewrite clone, no active development resumes from the old SHA, and all remaining pre-rewrite material is quarantined/non-pushable.
  - Gate result: `FRESH_CLONE_RECOVERED_VALIDATED`.
- [-] **T10 — Close sanitation program**
  - Requirements: REQ-6, REQ-7, REQ-8, REQ-10
  - Executor: GPT/GitHub + local operator for secret rotation
  - **T10A — Governance cleanup: COMPLETE**
    - active clone fast-forwarded cleanly to `ea326612b1c4199c3370ce8800e862856f1b7842`;
    - local commit email configured to GitHub noreply;
    - 0 open PRs before cleanup;
    - 44 obsolete remote branches deleted; exactly `main` remains;
    - 0 tags, 0 forks;
    - private GitHub Support purge bundle prepared outside the repository;
    - forensic remnants preserved;
    - remote `main` not mutated by the cleanup.
  - **T10B — Credential rotation/revocation: CLOSED_CERTIFIED**
    - T10B-R1 recovered a current PostgreSQL administrative channel without using historical credentials and restored the original `pg_hba.conf` bytes/hash after the temporary local-only trust window;
    - live inventory ultimately established `postgres` as the only in-scope PostgreSQL LOGIN; no CATSERVER/CATGIS LOGIN roles were present;
    - final rotation result: `T10B_STATUS=CREDENTIAL_ROTATION_VALIDATED`, `LOGIN_ROLES_IN_SCOPE=postgres`, `LOGIN_ROLES_ROTATED=postgres`, `NEW_AUTH_ALL=PASS`, `POSTGRES_ROTATION=ROTATED`, `TEMP_RECOVERY_ROLE_REMOVED=YES`;
    - post-rotation certification: HBA SHA256 `0C8DC6E6E57399790417A6E13B3A8E1B5E27AA19708A2122148FBFE3BDCECD42`, temporary trust absent, PostgreSQL service running, DPAPI vault present with passing file-level ACL, Git worktree clean;
    - no historical credential recovery was attempted; no secret was printed or committed;
    - gate result: `CREDENTIALS_ROTATED=YES`, `REQ_10_1=PASS`, `T10C_AUTHORIZED=YES`.
  - **T10C — Repository isolation (selected method): CLOSED_CERTIFIED**
    - selected method: `REPOSITORY_ISOLATION` (`PRIVATE_ARCHIVE + NEW_SINGLE_ROOT_PUBLIC_REPOSITORY`);
    - **T10C-RI1 PASS:** isolation spec merged before cutover; certified source tree = `816795841a35996a45bbfbd4871387a0c4f5f679`;
    - **T10C-RI2 PASS:** public root commit = `325d3cb71124bc70e43dd6a0e31cb8869917e484`, parent count = 0, history count = 1, tree match = YES, noreply identity = PASS;
    - **T10C-RI3 PASS:** original repository ID `1199177324` is private and preserved as `tulatraes25/CATGIS-DESKTOP-FORENSIC-ARCHIVE-20261005`; never deleted;
    - **T10C-RI4 PASS:** new canonical public repository ID `1405554812` is distinct; root CI run `37295650195` succeeded 3/3; ruleset `24496251` is active with PR-only integration, strict required checks, merge-only, non-fast-forward/deletion protection and zero bypass actors;
    - **T10C-RI5 PASS:** canonical `C:\\CATGIS` now points to the one-commit clone; HEAD/root/tree/fsck/clean/origin/noreply checks all passed; former local clone is archived at `C:\\CATGIS_PRE_REPOSITORY_ISOLATION_ARCHIVE_20261005` and its push is disabled;
    - **T10C-RI6 PASS:** the known pre-rewrite control SHAs and first changed commit are publicly unreachable from the new canonical repository;
    - cutover performed no file copy and no remote mutation;
    - gate result: `T10C_STATUS=CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION`;
    - GitHub Support path is retired as unnecessary for T10C; sanitized Support evidence remains offline as fallback/archive only.
  - **T10D — Privacy/license governance: CLOSED_CERTIFIED_BY_PRIVACY_ISOLATION**
    - local noreply configured;
    - owner decision: `LICENSE_DECISION=PROPRIETARY`; proprietary/source-available notice and README declaration are implemented by the T10 license PR.
    - **T10D-R2 finding:**
      - the first public repository isolation succeeded technically;
      - subsequent connector/web Git operations introduced **non-noreply** author/committer metadata;
      - **5** reachable commits from public `main` are affected (1 commit is noreply-only);
      - the personal address is intentionally omitted from this spec;
      - account primary email visibility has now been set to **PRIVATE** (`ACCOUNT_PRIMARY_EMAIL_VISIBILITY=PRIVATE`);
      - a **second repository isolation** is selected to remove those public metadata objects completely.
    - Historical-author rewrite for the first pre-rewrite history: `NOT_REQUIRED_FOR_FIRST_PRE_REWRITE_HISTORY` (that history is already inside the first private forensic archive and is not part of the canonical public repository).
    - Current post-isolation non-noreply commits: `MUST_BE_REMOVED_BY_SECOND_REPOSITORY_ISOLATION`.
    - T10D **CLOSED**: the second repository isolation was executed — the superseded public repository (ID `1405554812`) is now the private `CATGIS-DESKTOP-PRIVACY-ARCHIVE-20261005`; the private staging repository (ID `1406486908`) was published as the new canonical `tulatraes25/CATGIS-DESKTOP` from the single parentless noreply root commit `1cfd0050c831cdd2b34f7621f8ac2692466e5504` (tree `629ee866f009594ee2c69c2d34c9412a3cd1ac81`); ruleset `24546459` active; private CI `37389728300` 3/3 SUCCESS; `ACCOUNT_PRIMARY_EMAIL_VISIBILITY=PRIVATE`; `PUBLIC_PERSONAL_EMAIL_METADATA=ABSENT`; the superseded non-noreply commits are publicly unreachable.
  - **T10E — Final closure: CLOSURE_PR_IN_PROGRESS**
    - blocked until the second (privacy) repository isolation publishes a public lineage whose entire reachable history is noreply-only;
    - then: from the fresh final public clone, open `docs/t10-final-close` locally, merge through the protected workflow, and reconcile issue #28 (issue #28 lives in the first private forensic archive);
    - branch inventory and README reflect final state;
    - no open sanitation PR;
    - final post-main CI green;
    - spec state set to `CLOSED_CERTIFIED`.

