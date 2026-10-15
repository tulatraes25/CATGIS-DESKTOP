# CATGIS product direction and WCS spatial preflight - SPEC/Kiro

BASE: 6dad84ec927fab6d432fc37f0bd077fa42b00fad
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Product principle

Make the first successful professional map faster and safer, rather than expose every GIS capability simultaneously. Simple and Advanced presentation must use the same deterministic GIS engine and project format. Do not copy third-party code or UX assets.

## Architecture and user-experience backlog (post A0.7)

1. Progressive Simple tasks and Advanced access over the SAME services and project format, not a second editor or duplicated execution engine.
2. First-run workflow: create project, choose a verified CRS, import real vectors/rasters, style, verify, save, print CATMAP.
3. Safe operation preview: input/output CRS and transforms, units, NoData, bounded byte/disk estimates; provenance and recoverable output. Time estimates require measured evidence.
4. Unified background GIS Job API: cancellation, bounded resources, atomic publish, progress, no Swing EDT I/O and actionable errors.
5. Consistent layout/export: templates, print preview, authoritative legend/scale, DPI and truthful PDF/SVG capabilities.
6. Contextual action search with keyboard navigation and explicit unavailable dependencies.
7. Native Windows/Linux/Intel/ARM Mac workflows before cosmetic changes; measure time-to-first-map, recovery and output reproducibility.

## Source-confirmed regression and root cause

Initial PR #107 head 3a5b7ad04ab63e9950d6059d19bfc2da7fe88182 failed CATGIS CI 38753546480 and Certification 38753546285. It applied a strict interactive-form validator to the public streaming API, breaking documented omitted-BBOX/CRS compatibility. It also rejected the API's existing nonpositive-dimension defaults. This was a source regression, not permission to weaken existing tests or mark them skipped.

## Revised contract and minimal architecture

- Interactive form: require explicit finite ordered BBOX and syntactically valid CRS; reject missing coordinates and nonpositive user-entered dimensions before chooser or network.
- Compatibility API (file AND byte array): null/blank BBOX/CRS retain the existing world/EPSG:4326 request defaults; nonpositive API dimensions retain 512. Explicit malformed spatial inputs fail before network or destination lease/staging.
- Preserve the distinction between omitted and explicit geographic expectations. Omitted-both API calls retain existing structural/pixel TIFF validation; they do NOT become scientifically georeferenced just because defaults were used in the request. Explicit inputs still require delivered GeoTIFF CRS/envelope checks.
- Use one independent pure BBOX parser, shared by preflight and delivered metadata validation. No helper-to-client dependency, Swing, HTTP or CRS database lookup in this validator.
- Reject NaN, infinity, reversed/zero extents, overflowed derived width/height, oversized coordinate input and Java-only hexadecimal/suffixed literals. Permit ordinary decimal/scientific notation and projected coordinate ranges.
- Escape WCS 2 SUBSET values so plus signs and whitespace are not reinterpreted by HTTP query decoding. This does NOT fix coverage-specific axis negotiation.
- Never silently swap axes or claim syntax validation proves that a CRS exists. WCS 2 DescribeCoverage, native/output CRS and axis labels remain issue #108. Product roadmap remains #109.

## Acceptance and evidence

Original WcsAtomicStreamDownloadContractTest, WcsSpatialPreflightContractTest and WcsDimensionsPreflightContractTest remain unchanged.
New WcsSpatialApiCompatibilityContractTest exercises real loopback HTTP and real generated TIFF pixels: zero requests/no files on invalid input in both APIs, omitted defaults with 2.x/1.x fallback, exact dimensions, old-file preservation, corrupt TIFF refusal, explicit georeference rejection, escaping and released Windows file handles. No external service or binary fixtures in git.

Required: exact-head CI 4/4 and Certification 15/15, authenticated predecessor-main push, expected-head normal merge, fresh post-main push CI/Certification and independent attestation. No disabled/skipped tests added. Local pure-parser smoke on JVM 21 with --release 17 is supplementary only, not the Java 17/full-suite certification.

Physical GUI/native full workflows and A0.7 remain NOT_VERIFIED/OPEN. User workstation untouched.
