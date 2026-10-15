# CATGIS product direction and WCS spatial preflight — SPEC/Kiro

BASE: 6dad84ec927fab6d432fc37f0bd077fa42b00fad
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Product principle

CATGIS should make the first successful professional map faster and safer, rather than expose every GIS capability simultaneously. Comparable patterns: QGIS Processing Toolbox and layouts, ArcGIS Pro Tasks, and guided desktop editing. Do not copy code or third-party UX assets.

## Architecture and user-experience backlog (post A0.7)

1. Progressive modes: **Simple** tasks and **Advanced** access over the SAME deterministic GIS services and project format; no second editor or duplicate execution engine.
2. A first-run workflow: create project, choose a verified CRS, import sample real vectors/rasters, set style, verify, save, print CATMAP.
3. Safe operation preview: input/output CRS and transformations, area/distance units, NoData, estimates of bytes, runtime and disk footprint; display provenance and reversible output.
4. Unified background GIS Job API: cancellable, bounded, atomic publish, progress events, resource budgets, no Swing EDT I/O, clear recoverable errors.
5. Consistent layout/export UX: templates, print preview, authoritative legend/scale bars, DPI and real PDF/SVG capabilities rather than extension marketing.
6. Contextual search for actions and commands with discoverable keyboard navigation; search results point to certified workflows and unavailable dependencies are explicit.
7. Reliable native workflows on Windows, Linux, Intel/ARM macOS before polished visual changes; instrument time-to-first-map, error recovery, render FPS and output reproducibility.

## Gate A0.3: WCS spatial preflight

### Root cause
The form previously validated dimensions but accepted malformed extent/CRS until file selection and network activity. WCS 2.x currently builds SUBSET=x/y and SCALE SIZE=x/y without proof that a server exposes those axis labels. Do not assume EPSG:4326 longitude-first is universally the service native axis order.

### Requirements
- Validate finite ordered x/y min/max and EPSG:4326 lon/lat ranges in both UI and disk-download API before creating staging or contacting network.
- Reject blank or malformed CRS; preserve project and prior export bytes.
- Allow projected CRS coordinate ranges that are not limited to +/-180/90.
- Deterministic headless success and negative tests.
- Do NOT silently swap axis order, invent WCS 2 axis names or assert general DescribeCoverage compatibility.
- Defer true WCS 2.x CoverageDescription axis labels, native/output CRS, subsets and supported format contract to a separate issue and real mocked-service test.

### Verification
Candidate JUnit class: WcsSpatialPreflightContractTest.
Required: exact HEAD CI 4/4, Certification 15/15, authenticated main predecessor push and expected-head normal merge, then fresh event=push CI/Certification and independent post-main attestation.
Physical GIS scientific/GUI and DescribeCoverage remain NOT_VERIFIED. No local workstation changes.
