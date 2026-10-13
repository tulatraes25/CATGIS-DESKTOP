# CATGIS — A0.3/A0.4: XYZ source-identity cache integrity

Status: CANDIDATE / NOT_CERTIFIED
Base: protected main after PR #40, SHA `4a3d14a457b512857df265de89a05f57be9aeae6`
Branch: `quality/a0-xyz-source-cache-isolation`
Architecture status: A0.7 OPEN; desktop runtime remains gated.

## Confirmed root cause

`OnlineTileCache.buildKey` previously used only `source.getId() + z/x/y`, and
`resolveTileFile` used only a sanitized source ID for its disk directory.
`OnlineRasterSource` is immutable, but a new source can legitimately reuse the
same ID after its URL template, tile size, or CRS changes. The old implementation
can immediately return a wrong provider's cached pixel as if it were current.
This is a GIS **data-integrity** problem, not merely a cosmetic cache bug.

## Requirements

1. Memory cache, disk cache, pending request keys and network retry state must
   share a stable namespace derived from the *effective source configuration*:
   ID, service type, URL template, tile size, CRS and API-key requirement.
2. Distinct provider URLs with the same ID must never share images, failure
   cooldown or pending work. Same configuration across application restarts must
   deterministically reuse its own disk cache.
3. Never store URL credentials, source IDs or untrusted path fragments literally
   in cache directory names. Namespace format is `v2-` plus 128 bits of SHA-256
   in hexadecimal, computed over length-prefixed configuration fields.
4. Legacy ID-only disk cache files must not be reused because their source
   origin cannot be independently authenticated. They remain subject to the
   existing 200 MB disk eviction; expect a one-time cache warmup after update.
5. Preserve immediate RAM hits, asynchronous disk image decoding / HTTP work,
   EDT repaint callback, bounded worker pool and retry behavior.
6. Feature freeze: do not introduce additional online providers, UI commands or
   unrelated networking behavior in this tranche.

## Acceptance contracts

- Same ID + distinct local HTTP provider URLs: red and blue PNG tiles must be
  independently retrieved and rendered; each endpoint is requested once.
- Evict the matching RAM entries: both sources must reload their own correct
  images from disk without any further HTTP request.
- Same source => deterministic path; changing tile size or CRS => different
  paths; malicious source IDs and embedded URL tokens never appear in paths.
- Existing offline/backoff/EDT tests must be updated to the namespaced identity
  rather than silently passing through a legacy source-ID fixture.
- Exact candidate head must receive CATGIS CI and CATGIS Certification SUCCESS
  (Linux, Windows, real PostGIS and architecture inventories).
- A protected-main merge requires exact SHA lease and a fresh post-main
  certification; the separate push-workflow observability gate #42 applies.

## Explicit residual risk

`OnlineTileCache.prefetchNeighbors` still checks `File.isFile` and `length`
on its calling thread. This needs a separate deterministic A0.3 EDT contract.
The tile HTTP body reader remains unbounded and is another independently scoped
network-resource risk; this tranche does **not** certify all cache behavior.

## Rollback and safety

This change alters the local disk-cache namespace. It does not alter project
files or server data, does not rewrite Git history and does not delete legacy
cache directories. Reverting code restores legacy lookups, but those entries
must not be accepted as evidence of current provider identity.
