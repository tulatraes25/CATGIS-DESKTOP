# SPEC/Kiro #164 - user-visible UTF-8 text integrity

BASE: 2d2c1c608ef82e75da22524486d0f9d368a2ddce
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
