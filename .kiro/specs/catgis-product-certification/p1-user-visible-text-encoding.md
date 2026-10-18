# SPEC/Kiro #164 - user-visible UTF-8 text integrity

BASE: 5784f7a340ba1148aa26e0ed86d35e0749d12614
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed defect

Literal mojibake was committed in five user-facing source files. This is stored source corruption, not a runtime font issue. Build already compiles Java with UTF-8 and launches with file.encoding=UTF-8.

Exact-main audit found 196 sentinel occurrences across affected lines:
- MapPanel: 70 sentinel hits;
- MapEditingEngine: 91;
- LayoutPreviewPanel: 10;
- MapLayoutComposerDialog: 24;
- LayoutPageRenderer: 1.

## Minimal change

Only proven corrupted literals are replaced with intended Spanish Unicode/punctuation. No whole-file encoding conversion, no logic changes, no line-ending policy changes.

UserVisibleTextEncodingContractTest:
- scans CATGIS source/resources as UTF-8 and rejects known mojibake sentinel families;
- checks representative map/editing/CATMAP/layout strings exactly;
- contains sentinel code points only as Java Unicode escapes so the guard does not self-trigger.

## Non-claims

This certifies stored source text, compilation and automated cross-platform contracts. It does not certify physical font availability, DPI, truncation, screen-reader pronunciation or every translated string. Those remain physical/accessibility UX evidence under A0.7/F3.

Require exact-head CI/Certification/Runtime Identity/GeoParquet plus authenticated predecessor-main evidence, normal expected-head merge and fresh-main proof.

Ported into a clean branch from main after PR #187; exact-head and post-main certification required. Historical PR #165 remains stale and must not be merged independently.

Consolidated and independently retested with #189/#191/#192 source and tests on current main 5784f7a340ba1148aa26e0ed86d35e0749d12614; prior green runs on old SHA are not substituted for this candidate's native gates.


## Post-LAS exact-main port
Source and contract code ported without intersecting the already merged P0 #194 file provenance fix on main `5784f7a340ba1148aa26e0ed86d35e0749d12614`. Must certify this new candidate and exact predecessor-main push; earlier PR #196 full matrix alone is insufficient.
