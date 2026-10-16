# SPEC/Kiro #135 - raster band numeric integrity and truthful spectral formulas

BASE: e8aba016621ae0265ce090f3a3e5d6f5d37f9167
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## User failure being prevented

A professional user must never receive a plausible raster calculated from a different band or from samples silently reduced to 8-bit. The previous batch and spectral-dialog helpers copied source samples into TYPE_BYTE_GRAY and clamped high band indices. Characterization observed USHORT 513 -> 1, FLOAT 0.5 -> 0 and band99 -> final available band. Batch shortcuts display human band numbers B4/B3 etc. but forwarded them directly into a zero-based helper.

EVI and BSI were also advertised with their standard 3/4-band formulas while the two-band execution path computed simplified substitutes. A visually reasonable result is not acceptable evidence of the named scientific index.

## Minimal design

- One RasterBandExtractor serves dialog and batch analytical extraction. It rejects invalid indices and preserves the source sample transfer type and exact sampleDouble values in a one-band image.
- Batch spectral parameters are explicitly one-based; dialog keeps its existing one-based spinners and converts once.
- SpectralIndexEngine declares formula arity. Two-band execution rejects EVI/BSI and unknown IDs rather than creating neutral or simplified output.
- Proper 3-band EVI and 4-band BSI engine paths are available and have pure raw-value formula seams plus display-normalized image output.
- Current two-band dialog explains and blocks indices needing additional bands. It does not guess extra bands. The hidden description/formula area is placed in the visible center above an independent footer.
- TIFF writer absence/null result becomes a batch error instead of a successful callback.

## Acceptance

New RasterBandExtractorContractTest uses genuine USHORT/FLOAT rasters and proves 513, 0.5, -2.25 and exact B4/B3 identity survive extraction; low/high invalid bands throw. SpectralIndexEngineTest replaces the old simplified EVI/BSI expectations with insufficient-arity rejection and exact raw EVI/BSI formula oracles, plus unknown-ID and dimension mismatch failures. Existing ordinary two-band index contracts stay.

Supporting isolated javac --release 17 / JVM21 probe passed the same USHORT/FLOAT/band/NDVI/EVI/BSI assertions before publication. It is not the repository suite or cross-platform certification.

## Explicit residuals

This tranche does NOT claim scientific batch raster completion. Spectral output remains a display-normalized byte image; a future analytical-output path must preserve raw index values, CRS/grid, NoData and georeferencing. Batch TIFF publication is still direct rather than staged/atomic. Reclassification still needs signed-rule parsing and output-type correctness. Metadata precedence stays #128. Full EVI/BSI GUI input for 3/4 explicit user-selected bands is a separate UX/capability tranche; blocking an incorrect formula is safer than guessing.

Require exact-head CI/Certification/runtime gates and authenticated prior-main evidence, normal expected-head merge, then fresh-main proof. No user PC, OpenCode, datasets or releases.
