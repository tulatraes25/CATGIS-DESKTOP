# Java source integrity - mechanical LF repair

Status: CANDIDATE; exact PR and post-main CI required.
Base: d10ad77247e47755b7da35bf6460604a396e8188.
Mechanical-only commit: 7f211a19ac7ed470bda8205c5c969296089cf7da.

## Evidence and cause

PR #122 source c48a42c57327c5a9bdca8a3af234f808677a5a48 ran row matrix
38829646364. Windows and both native Mac architectures completed successfully.
Linux job 116745464821 executed 17 tests with zero failures/errors/skips,
verified all 19 input hashes, and then failed the strict git diff check.
The twelve differing Java blobs already existed in the protected base. They
mix CRLF, LF and a lone CR in the opening CR-CRLF sequence. Git attributes
require Java LF, but changing attributes did not normalize those old blobs.
This is NOT remaining evidence of numeric row-reader failure.

## Requirements and minimal design

1. Normalize exactly the twelve reviewed Java files, preserving logical line
   terminators, UTF-8 bytes, whitespace, literals and final-newline presence.
   Exact transform: CRLF to LF, then lone CR to LF. No other byte edits.
2. Keep this mechanical-only commit separate from the QA guard commit so a
   machine-checkable before/after proof can reject extra files or code edits.
3. Add a read-only gate to the existing mandatory Repository Safety Checks.
   Check committed Java blob bytes (not only checkout), authenticate Git blob
   hashes, require regular files and byte-identical LF checkouts. Retain strict
   staged and unstaged git diff checks; no normalize/reset/ignore-whitespace.
4. Exercise the guard with eight real disposable Git-repository tests: clean
   LF, Unicode/space paths, absent final newline, preserved BAT CRLF, bad index
   hidden by good checkout, mixed/lone CR, source mutation, staged non-Java
   mutation, empty-source rejection and valid/invalid mechanical proofs.
5. On this repair PR, fetch only the two exact proof commits and verify that
   every changed file equals the transform. That historical proof is not run
   on unrelated future PRs; the actual-current-tree integrity gate always runs.

## Acceptance and residuals

Require exact-head CI, all 15 Certification jobs, independent metadata interop,
prior-main attestation and successful twelve-file byte proof. After normal
merge, require fresh main push CI/Certification/interop and attestation.
Then update research #122 from the verified main and rerun its unchanged
17-test/four-platform matrix plus the 19-file integrity check.

No test expectations, application behavior, runtime dependency or release is
changed. No work on the user's PC or C:\\catgis. A0.7, full GeoParquet layer
import and physical GUI/native acceptance remain OPEN.

Reference: https://git-scm.com/docs/gitattributes (text/eol and renormalization).
