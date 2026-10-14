# SPEC/Kiro A0.7 P2 — WhiteboxTools multiplatform executable discovery

Status: CANDIDATE / NOT_CERTIFIED
Issue: #74
Source baseline: `6792e441df3d01fee198bd316845594abc8716cc` (post PR #76 merge)
Branch: `quality/a0-whitebox-portable-executable-resolution`

## Confirmed defect

Previous `ExternalToolService.findWhiteboxTools()` and
`isToolAvailable()` invoked Windows `where` through `ProcessBuilder`
on every OS, so an installed Linux/macOS WhiteboxTools runtime could not
be discovered. It also accepted mere file existence as evidence of a
valid executable and could resolve an unrelated PATH executable.

## Change and scope

- New `WhiteboxExecutableResolver` uses pure OS-aware approved directories
  and Java NIO file checks only: NO shell, `where`, `which`, PATH wildcard
  scanning or network downloads. `CATGIS_WHITEBOX_BIN` accepts an explicit
  absolute binary or directory; invalid explicit configuration rejects
  without silently falling back to another installed binary.
- Candidate order: user tools dir and WBT/WhiteboxTools subdirs, trusted
  Windows Program Files/WhiteboxTools, Linux /usr/bin, /usr/local/bin,
  /opt/bin and /opt/whitebox/bin, or macOS Homebrew ARM, Intel, MacPorts
  and /usr/bin. Names are allowlisted: whitebox_tools(.exe), wbt(.exe).
- Files must be real executable regular files. Verified symlinks such as
  Homebrew's installed packages resolve to an absolute real file. CWD
  and arbitrary PATH entries are never consulted.
- `ExternalToolService.isToolAvailable` has no OS subprocess and probes
  only WhiteboxTools or a reviewed list of optional GIS command names.
- `executeWhitebox` refuses invalid tool IDs before lookup, preserves
  direct ProcessBuilder argv and 300s bounded-output timeout, and refuses
  synchronous launch on Swing EDT.
- Existing `ToolAvailabilityProbe` uses SwingWorker and callback on EDT;
  UI will show `CATGIS_WHITEBOX_BIN` configuration guidance on absence.
- 11 deterministic JUnit cases cover Windows/Linux/macOS names and bins,
  explicit override, nonexecutable/dir/traversal, path spaces, Homebrew
  symlink, approved search ordering and invalid tool names.
- Native Whitebox hydrology requires a licensed/provisioned executable
  and a tiny DEM with verified output cells/CRS. **NOT_VERIFIED** until
  installed in controlled CI; JVM-only resolver tests are not that evidence.

## Acceptance and merge gate

- Exact PR head CI/Certification for all four OS/architecture jobs,
  protected prior main event=push CI/Certification, authenticated
  Main Push Attestation and normal expected-head merge.
- Exact merged main CI/Certification and post-main attestation.
- No changes to local Windows C:\catgis or private geospatial datasets.
- Preserve A0.7 OPEN for native Whitebox, Windows OSGeo4W/JNI/GUI and other
  unclosed architecture requirements.
