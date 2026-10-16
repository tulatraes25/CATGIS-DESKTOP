# SPEC/Kiro #137 - batch dialog user workflow integrity

BASE: e8aba016621ae0265ce090f3a3e5d6f5d37f9167
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed user-facing defects

BatchProcessorDialog added both header and form to BorderLayout.NORTH and both progress and controls to SOUTH. The later components own those regions, so title/progress can disappear from the actual layout. The visible "Incluir subdirectorios" checkbox was never read. Directory scan executed synchronously on the EDT, and a manual path/options change after scanning could execute a stale file list. Output mkdirs failure was ignored.

## Minimal design

- Header stays NORTH. Form + file preview share CENTER. Progress + controls share one SOUTH container.
- BatchFileDiscovery is UI-free, reuses BatchProcessor's current GIS extension classifier per directory, sorts stable absolute paths, does not traverse nested directory symlinks and fails above 100000 discovered GIS files.
- scanDirectory uses SwingWorker, cancellation generation tokens and publishes results only on EDT. It displays paths relative to the scanned root so duplicate basenames are understandable.
- Successful scans remember the normalized input and recursive option; execution refuses stale results if either changes.
- Output directory creation is checked before starting processing. Existing BatchOutputPlan remains the authoritative whole-plan collision guard.
- Buttons are disabled while scan or batch execution owns the corresponding operation. This is not a new cancellation UX.

## Tests and explicit limits

BatchFileDiscoveryTest covers root-only vs recursive behavior, nested files, unsupported extensions, deterministic order and invalid input. No symlink test is added because cross-platform privilege requirements would create an environmental skip; implementation uses Files.walk without FOLLOW_LINKS plus NOFOLLOW_LINKS directory classification.

This tranche does not claim physical Swing/HiDPI/focus certification, batch cancellation, recursive huge-tree performance or crash rollback. It does not change raster calculations. Full GUI evidence remains after A0.7/F3.

Require exact-head repository CI/Certification/runtime evidence and predecessor-main proof before merge, then fresh-main proof. No PC/OpenCode/release changes.
