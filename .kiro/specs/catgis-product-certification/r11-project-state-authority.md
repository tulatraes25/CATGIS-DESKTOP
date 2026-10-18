# SPEC/Kiro #153 - authoritative current project state

BASE: 5784f7a340ba1148aa26e0ed86d35e0749d12614
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed data-loss guard defect

CATGIS is migrating project state into AppContext, but unsaved-change protection,
window/header rendering, startup CRS and rename still read the deprecated
CatgisDesktopApp.currentProject field. Many production flows create a project
through AppContext.setCurrentProject only. A stale legacy field can therefore
miss a dirty live project or warn about an obsolete one.

## Minimal design

- AppContext-first CatgisDesktopApp.getProject is the single read authority.
- Keep currentProject only as a compatibility mirror when legacy setProject is
  explicitly called; do not introduce AppContext -> Swing coupling.
- Startup syncs through setProject once.
- unsaved detection, prompt naming, title/header, startup CRS and rename read
  the authoritative project.
- markProjectClean refreshes the title just like dirty.
- MapPanel's direct editing dirty callback routes through the existing wrapper
  so edit operations update both model dirty state and visible title.

## Contracts

ProjectStateAuthorityContractTest creates deliberately contradictory state:
a stale clean legacy project vs a dirty AppContext project, and the reverse.
Unsaved detection must always follow AppContext. It also covers direct
AppContext dirty -> wrapper clean transitions and legacy setProject identity.

No physical close-dialog/focus claim is made by these headless contracts.
Crash recovery remains #149 and staged project replacement remains #144.

Require exact-head CI/Certification/RuntimeIdentity/GeoParquet and authenticated
predecessor-main proof before merge, then fresh-main proof.

## Current-main selective port — 2026-10-17

Original PR #154 head `c7f774c3ea7e8b8ae7303aa3830963f581ddc388` contains the changes and headless tests. Both production files CatgisDesktopApp.java and MapPanel.java were byte-for-byte identical between the former PR base `228e30c60c330eedf8f918d3a4ffae9e660c953d` and current main `5784f7a340ba1148aa26e0ed86d35e0749d12614`. Re-port only their tested versions and the regression test. This draft requires entirely fresh exact-head CI/native/attestation plus post-main verification. The MapPanel file also appears in UTF-8 candidate #199, so avoid merging a stale whole-file snapshot after that PR without rebasing/surgical retest.
