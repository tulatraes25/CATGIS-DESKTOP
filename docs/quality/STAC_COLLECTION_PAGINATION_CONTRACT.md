# CATGIS F2.7 / A0.4 — STAC Collection Pagination Integrity

Status: CANDIDATE / NOT_CERTIFIED
Baseline: `d860bdcea233307eb4378145b7dccdf00105f5ae`
Branch: `quality/f2-stac-pagination-integrity`

## Root cause

`StacClient.getCollections` assigned the next-page URL and then called
`nextUrl.equals(nextUrl)`, which always returns true for a non-null URL.
It therefore returned only page one without indicating partial data.

## Acceptance criteria

- Collect collections from multiple pages in order.
- Resolve relative next URLs against their originating page.
- Accept reordered fields and whitespace in next-link JSON.
- Reject cyclic pagination and non-HTTP(S) URLs, and cap pages at 100.
- Execute real HTTP requests only against a loopback fixture in JUnit.
- Preserve existing client behavior for single-page responses.

## Remaining gates

This is not a complete STAC 1.0 parser or overall A0.4 certification.
It is a scoped F2.7 regression repair. Full GitHub CI and Certification
on exact PR and post-merge main SHAs remain mandatory; no GUI/OpenCode
before A0.7.
