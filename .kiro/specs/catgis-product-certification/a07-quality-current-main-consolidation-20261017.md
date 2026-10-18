# SPEC/Kiro — source-compatible WFS, WCS and UX integrity consolidation

**BASE_SHA:** `5784f7a340ba1148aa26e0ed86d35e0749d12614`
**STATUS:** DRAFT / NOT_CERTIFIED
**A0.7:** OPEN
**Historical exact-head source candidates:** PR #189 `6ec0ad5f472cf8222d7ee5d97138e211a3608547`, PR #191 `1bbc266e47d1b33db8232241f1d7d263b5e23495`, PR #192 `1ca320101c96cefdf6dc23941cb038a3e847771f`.

## Why this tranche exists

All three independent prior candidates had completed full exact-head CI,
15/15 native Certification, 8/8 Runtime Identity, GeoParquet, and a successful
attestation against predecessor main. Their old source branches predated the
fully certified FlatGeobuf integration #190. Rather than merge stale
whole-class snapshots over #190 or keep three isolated native queues,
this branch copies only their exact modified-file contents where the old
base blobs and the new main blobs were independently verified identical.

## Guardrails / scope

### WFS
- Preserve an UNSPECIFIED request CRS as empty, never fabricated EPSG:4326.
- Respect an explicitly requested CRS for GeoTools query reprojection.
- Use the service feature schema to determine the true source CRS when
  no CRS is requested; unknown schema must remain unknown.
- Tests: `WfsRequestCrsTruthContractTest` with EPSG:32719, explicit 4326,
  blank, invalid and unknown metadata.

### WCS
- The real local chunked HTTP total deadline must be deterministic on cold
  Windows runners, with a 1.5 s budget and 3 s response fixture.
- Keep one network request, timeout, previous target bytes intact, no
  fallback to WCS 1.0, no staging leftovers.
- Production WCS networking code is unchanged.

### UI/CATMAP text
- Restore 63 known garbled literals in MapPanel/MapEditingEngine,
  LayoutPreviewPanel, MapLayoutComposerDialog, LayoutPageRenderer.
- No behavioral logic, map geometry, typography themes or encoding of
  entire files changes.
- Source-tree UTF-8 sentinel test and exact visible-text fixtures preserved.

## Proof and limitations

- Strict diff limited to 8 existing code/test source files, 2 new contract
  classes and corresponding SPEC. No credentials or third-party sources.
- Must earn *fresh* exact-head CI, 15/15 Certification, 8/8 Runtime Identity,
  GeoParquet and predecessor main-push authenticated attestation.
- No merge until exact previous-main push proof, followed by fresh merged-main
  push CI/certification/attestation.
- This does NOT claim complete native GUI, live WFS server integration or
  architectural A0.7 closure. Cross-platform signed installers and user
  acceptance remain separate.


## Post-LAS exact-main port
Source and contract code ported without intersecting the already merged P0 #194 file provenance fix on main `5784f7a340ba1148aa26e0ed86d35e0749d12614`. Must certify this new candidate and exact predecessor-main push; earlier PR #196 full matrix alone is insufficient.
