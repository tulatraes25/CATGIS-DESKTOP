# CATGIS — SPEC/Kiro A0.7 Windows app-image package certification

Status: CANDIDATE / NOT_CERTIFIED
Exact protected-main base: `d5e7e41f9219e77a359ca1b22a037d1e4591f4f5`
Branch: `quality/a0-windows-appimage-contract-post61`
Windows + Linux runtime-bundled packaging must both be reproducible.

## Source-confirmed gap

CATGIS has Windows jpackage app-image/exe installer Gradle tasks but the
Windows Full Suite only executes headless Java tests; no GitHub runner
previously invoked `packageWindowsAppImage` or checked that the app-image
actually contains launchers, main JAR, CATMAP secondary launcher and
embedded Java runtime. Green JUnit does NOT certify Windows packaging.

## Requirements

1. Add a Windows-only `verifyWindowsAppImage` Gradle task depending on
   the existing `packageWindowsAppImage`. Run existing jpackage task as-is
   first; resolve actual reproducible faults if CI finds them. No new
   external WiX/signing requirement for app-image; .exe installer remains
   out of scope.
2. Validate `CATGIS Desktop.exe`, `catmap.exe`, JDK image
   `runtime/release`, `runtime/bin/server/jvm.dll`, main application
   `app/catgis-desktop-1.0.jar`, and both main-class entries in that JAR.
   Never open Swing GUI in CI.
3. New GitHub Certification Windows job
   `Windows App Image Package Contract` depends on bounded Windows Gradle
   dependency cache and JDK17 setup. Run the new Gradle verification.
4. Log only aggregate image size and SHA-256 of main JAR; no exe/zip/bundle
   uploads that would increase storage. Do not modify Windows installer
   signing/WiX configuration or install dependencies into production.
5. Preserve already certified Linux app-image build, Windows/Linux headless
   tests, PostGIS real integration and architecture feature freeze.
6. Obtain SUCCESS on exact PR head for CI and Certification including both
   platform app-image jobs, Linux/Windows Full Suites and PostGIS; then
   exact merged-main authenticated push CI/Certification and both package
   jobs. A0.7 remains OPEN until native GDAL, GUI, signature and installer
   release gates are validated.

## Negative evidence and portability limits

Windows app-image packaging does not prove Windows .exe installer creation
(under WiX), code-signing, SmartScreen, first-launch GUI or geodata correctness.
Linux app-image has not been certified on every Linux distribution/glibc
version. JDK17 package layout depends on jpackage platform; do not impose
Linux paths on Windows or force Java executable visibility when jlink strips
native tools by default.
