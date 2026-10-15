# CATMAP PDF multiline positioning - SPEC/Kiro

BASE: ea715cdfd3ee528cf76bd2a7d1e1f367d23d435c (PR #107 merged)
ISSUE: #110; related professional-export gate #99
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Objective and root cause

Professional output must preserve visible multiline label positions, not merely produce a parseable PDF.
LayoutExportEngine.exportLabel calculated absolute tx/ty on every row but called PDFBox newLineAtOffset, whose Td operator is relative to the current line matrix. Second and later lines accumulated the page offset and could leave the visible page. The old tests validated a single-line title and file integrity, so they did not expose this layout defect.

Primary API: https://javadoc.io/static/org.apache.pdfbox/pdfbox/3.0.0/org/apache/pdfbox/pdmodel/PDPageContentStream.html (newLineAtOffset and setTextMatrix).

## Minimal design and non-goals

- Set the text matrix to an absolute translation on every logical row. Preserve the first baseline, font mapping/size, padding and 1.2x line spacing.
- Split logical lines with Java \\R and preserve blank lines; CRLF is one separator, not an unsupported font character.
- Keep the existing atomic PDF publisher and its error cleanup unchanged.
- Do not enlarge the GIS engine, add dependencies, change the project format or require user actions.
- No claim that this fixes Java2D/PNG preview multiline rendering, DPI/font parity, dynamic expressions, page-number tokens, arbitrary Unicode fonts, clipping/overflow, raster fallback coordinates or ellipse fill/stroke. Those need separate contracts and remain residuals under #99/A0.7.

## Regression contracts

CatmapPdfMultilineContractTest has five real PDFBox export/save/reopen tests:
1. Exact glyph X/Y and three visible in-page baselines, at export DPI 72 and 300.
2. CRLF/CR/LF and an empty logical row retain their row spacing.
3. Two labels have independent matrices; single-line placement does not change.
4. PDFRenderer pixels prove every expected line is actually visible in its expected strip.
5. An unsupported glyph after a valid first line cannot overwrite an existing valid PDF; previous bytes are identical and no .part remains.

Older CatmapAtomicExportContractTest remains unchanged. No mocks, skipped tests, binary fixtures, additional font files or external services are introduced.

## Supplementary operator reproduction

An isolated minimal PDF containing the same three-row Td sequence was rendered with PyMuPDF: only A was visible/extractable. Replacing Td with absolute Tm rendered A/B/C in aligned successive rows. Both rendered crops were inspected. This confirms the PDF operator mechanism but is NOT execution of CATGIS or its PDFBox integration; the new real repository tests and CI are authoritative.

## Certification

Require exact candidate CI 4/4 and Certification 15/15, independent attestation of predecessor main ea715cdf... event=push CI/Certification, then normal expected-head merge and fresh post-main push evidence. The prior PR #107 had candidate CI 38757487082 and Certification 38757486668 SUCCESS, but its new merged SHA requires its own post-main proof.

Physical GUI/print/native full-workflow evidence stays NOT_VERIFIED; workstation C:\\catgis untouched.
