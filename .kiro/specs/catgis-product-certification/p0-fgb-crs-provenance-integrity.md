# P0 #151 — FlatGeobuf CRS source provenance (staging)

BASE_MAIN: `de080ce87480b9716d11d751d3f03679f0f2334e`
STATUS: CODE_PREPARED / NOT_CI_VERIFIED / NOT_MERGED.

## Problem
The bounded, real-GDAL-tested FlatGeobuf loader from #177
correctly reconstructs typed features, but its GeoTools SimpleFeatureType
lacks the FlatBuffer header CRS. During direct import the source CRS
is blank, and project reload `LayerVectorDataSupport` still invents
EPSG:4326 when missing. A projected layer could be silently misplaced.

## Tranche A (staging branch only)
- Reuse audited, project-authored `FlatGeobufCrsSupport` from draft
  #158 to decode authority/numeric code, WKT and unknown provenance.
- Retain the strict bounded framed-header and generated-header validation
  from #177; return header metadata PLUS a typed `Resolution` value
  to the raster-neutral schema builder; set source CRS only when genuine.
- Direct UI import reads schema CRS; if absent, require explicit user
  selection, or fail closed for headless/batch import. Never default the
  chooser to current project or EPSG:4326.
- Tiny full-frame FlatGeobuf unit fixture proves EPSG:32719 survives
  header to actual GeoTools schema and absent CRS stays unknown.
- Preserve original native 3-GDAL proof for indexed/unindexed FGB.

## Next blocking tranche B
After #180 LAS merge to fresh main, reconcile LayerVectorDataSupport
without clobbering its complete-vs-preview protections. Remove invented
EPSG:4326 for FGB and LAS; require actual FGB header CRS or previously
user-selected source for persisted projects; LAS CRS unknown demands
a user assignment, not silent WGS84. Add real projected/GDAL source
fixtures, then full exact-SHA 15/15 native + 8/8 runtime and post-main.
No PR until that merge, to avoid overlapping native CI.
#151 stays OPEN. No PC/OpenCode/production data touched.
