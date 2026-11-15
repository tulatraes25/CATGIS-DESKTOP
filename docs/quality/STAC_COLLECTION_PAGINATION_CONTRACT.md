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

## Current state on main (repository audit, 2026-11-15)

Observed on `aecb4d20621bb5065f8acb388813eefe594f3a27`:

- the repair is implemented in `StacClient.getCollections` (visited-page set,
  relative next-link resolution against the originating page, HTTP(S)
  enforcement and a page cap);
- `StacCollectionPaginationContractTest` covers multi-page ordering with
  reordered keys/whitespace, cyclic pagination, non-HTTP schemes and a null
  link array, using a loopback HTTP fixture only;
- the local suite is GREEN on the audit branch (`fix/main-audit`).

Status remains `CANDIDATE / NOT_CERTIFIED`: this document does not record an
exact-PR CI/Certification run or post-merge main evidence. Only those GitHub
runs can move it out of CANDIDATE.
