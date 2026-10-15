# SPEC/Kiro #113 - mixed-dimensional overlap correction before merge

PARENT CANDIDATE: 7863f9e2ed9fbb6f04ac85544a47aeb3427b1edf
PR: #116 (still draft, not merged)
A0.7: OPEN

## Additional counterexample found during review

Two valid MultiPolygons can overlap in one component and only share an edge or point in another. JTS Geometry overlay returns a GeometryCollection for such heterogeneous results. The first candidate's area>0 check then passed that collection to a Polygonal-only output guard, rejecting genuine overlapping area as NOT_EVALUATED. The first CI suite passed because it did not contain this case; green tests did not prove the case was covered.

Primary contract: https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html (Overlay Methods: heterogeneous output is a GeometryCollection).

Independent Shapely reproduction: valid MultiPolygons A=((0,0)-(2,2)) + ((4,0)-(6,2)), B=((1,1)-(3,3)) + ((6,0)-(8,2)) yield a GeometryCollection with polygon area 1 and a line at x=6. This is an independent geometry oracle, not execution of the Java candidate.

## Minimal correction and tests

Keep all Polygon components of a mixed intersection using the existing JTS GeometryFilter/buildGeometry APIs; exclude line/point-only contacts from the AREA overlap issue. Do not union, buffer, repair or silently adjust coordinates. Validate the resulting polygonal geometry and preserve the original SRID.

Two additional real JTS/GeoTools tests in PolygonMixedIntersectionContractTest exercise area+edge and area+point. They assert genuinely mixed raw output, exact polygon geometry/area, order invariance, source immutability, SRID and adapter INVALID rather than NOT_EVALUATED. They do not weaken any earlier tests.

The candidate now has 18 added tests across two new classes, plus the five strengthened old assertions described in the main coverage SPEC. This extension is a correction within the same acceptance scope, not an unrelated feature added to a green candidate. Prior SHA run results cannot certify the updated head; new full CI/Certification and predecessor-main attestation are required before a normal expected-head merge. All existing GUI/EDT, AOI interface, other legacy rules and runtime/resource limits remain explicit residuals.
