# SPEC/Kiro #153 - authoritative current project state

BASE: f7e0a43155a92f9d2a0062ffe6e8c6749a1a1237
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
