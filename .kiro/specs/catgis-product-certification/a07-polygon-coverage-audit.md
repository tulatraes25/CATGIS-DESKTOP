# SPEC/Kiro - polygon coverage semantics and honest evaluation

BASE: 28f23e272c97580377f87c2e4d70ac022026d526 (PR #115 merged)
ISSUE: #113; product capability track #109
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Objective and source-confirmed cause

TopologyValidator.validateNoGaps subtracted each pair union from a rectangular envelope. Exterior corners of a valid L-shaped domain were reported as gaps, while full-layer holes could be missed. Gap/overlap exceptions were logged but could leave issues empty and valid=true. Four existing tests actually required a clean result for null/empty data; another gap test only asserted that a result existed.

## Scientific definitions and minimum design

- Enclosed gaps: union the complete polygon set, union its polygon exterior shells, subtract the complete coverage including islands. Never use a bounding rectangle to invent the expected domain.
- Explicit study area: compute study-area minus coverage, respecting boundary holes and ignoring coverage outside that boundary. An empty coverage inside a supplied valid area means that whole area is missing, not an invalid input list.
- Area overlaps: an indexed candidate-pair intersection with positive area; shared edges are not area overlaps. Input geometry validity is checked BEFORE any partial result is returned.
- Use existing JTS UnaryUnionOp rather than CoverageUnion, which assumes a valid clean coverage. These checks are distinct from JTS CoverageValidator's edge-matching definition and narrow-gap-width checks; a valid OGC coverage can intentionally have holes.
- Pure core.geometry.PolygonCoverageAudit depends on JTS/JDK only. GeoTools feature/schema handling remains in the TopologyValidator adapter. No new dependency, UI toolkit, network or persistence engine.
- Validate finite coordinates, polygon type, nonempty/valid geometry, consistent SRID/precision and comparable declared layer CRSs. Unknown CRS is allowed only consistently as unreferenced planar input; never assume metres or reproject automatically. The area threshold is in squared input-coordinate units and applies to each missing polygon, not a linear distance.
- Copy geometries before operations. Preserve result SRIDs even when an input's GeometryFactory has a different default. Overlaps preserve original feature indices/IDs; a missing area has index -1 because no single source feature owns it.
- Gap, overlap and study-area adapters distinguish VALID, INVALID and NOT_EVALUATED while retaining existing two-argument TopologyResult construction. Invalid/missing data, cancellation, resource-limit refusal or calculation exceptions return false plus NOT_EVALUATED, never a clean or partially successful result.

Primary references consulted: https://locationtech.github.io/jts/javadoc/org/locationtech/jts/operation/union/UnaryUnionOp.html ; https://locationtech.github.io/jts/javadoc/org/locationtech/jts/coverage/CoverageValidator.html . These are API semantics references, not copied competitor implementation.

## Tests and acceptance

Sixteen new PolygonCoverageAuditContractTest tests use genuine JTS WKT geometries and GeoTools features: concave L; distinct L versus rectangular AOI; four-piece enclosed hole; islands; disjoint regions; AOI holes; empty coverage with an AOI; MultiPolygon; input order; translation and squared scale; precise area tolerance; overlaps versus edge contact; null/empty/invalid/nonpolygon input; SRID/schema CRS mismatch; interrupt preservation; source immutability; feature-count limit; result SRID; nonfinite coordinates and mixed precision.

All 16 old TopologyValidatorTest methods remain present. Four null/empty gap/overlap assertions are deliberately STRENGTHENED to require false and NOT_EVALUATED; the old non-null-only gap assertion now proves the intended disjoint-domain result. No tests removed, disabled or skipped; unrelated legacy rule tests remain unchanged. A test claiming disjointness from an empty boundary intersection was corrected BEFORE candidate publication: a hole and an island share a boundary, so require zero intersection AREA and excluded island interior, not an empty intersection.

Supplementary independent Shapely 2.1.2 oracle: 12 geometry checks PASS (L 0 internal/1 rectangular-AOI missing area, frame hole area 1, island-adjusted missing area 32, boundary-contact semantics, order/translation invariance, scale factor 3 -> area factor 9, AOI area 64). This is NOT CATGIS/JTS execution. The isolated container could not download Java dependencies; no local Java/full-suite PASS is claimed. Exact GitHub JDK17 regression remains authoritative.

## Limits and next gate

Counts are bounded at 100000 polygon features, 1000000 vertices, 1000000 overlap candidate pairs and 10000 overlap results; exceeding a limit rejects the audit rather than truncating a report. These are conservative work caps, NOT measured heap ceilings. JTS primitive union/overlay is not preemptible: interrupts are honored between stages and loops, not a hard timeout within one primitive. Larger datasets need partitioning/provider work and performance measurements.

No new UI menu is activated. validateCoverageWithin is a tested backend building block for the later AOI tool; UI selection, async execution, progress/cancel controls, issue-layer export, repair preview and real GUI acceptance remain OPEN. Existing MainMenuBar topology call paths require their own EDT/worker audit; this change does not certify interactive responsiveness. Other legacy rules (cross-layer overlaps, covered-by, dangles, self-intersection null handling) are NOT fixed or reclassified by this tranche. Unit/CRS authority reconciliation beyond consistent declarations is not a geodesic or full reprojection certification. Keep #113 open until its remaining integration requirements pass.

Require candidate CI 4/4, Certification 15/15 and prior-main push attestation; normal expected-head merge; fresh merged-main push CI/Certification and independent attestation. Report the actual PR checkout commit/tree when GitHub runs a synthetic merge. No user-PC files, native installation or production datasets changed.
