# CATGIS — A0.7 macOS support (Intel x64 and Apple Silicon arm64)

Status: CANDIDATE / NOT_CERTIFIED
Protected-main base SHA: `34d73ed47c6720e03ef49d49715f77b81d86a70c`
Branch: `quality/a0-macos-intel-apple-silicon-appimage`
Feature freeze applies to all Windows/Linux/macOS tranches.

## Revalidated baseline and need

Windows and Linux now build reproducible `jpackage --type app-image` runtime-bundled
desktop applications (PR #62), with headless Java, PostGIS, architecture and
platform package checks. macOS was previously neither a CI test platform nor
a bundled-app distribution. Portable Java code alone does NOT certify a Mac
application. Online XYZ tile cache already uses the OS-specific
`~/Library/Caches/CATGIS/online-tile-cache` path for Mac OS X, and its pure
resolver test simulates macOS; no change to project/raster file paths required.

## Requirements

1. macOS is the third official *target* platform, independently validated on:
   Apple Silicon arm64 (`macos-15`) and Intel x64 (`macos-15-intel`).
   Record `uname -m` and check actual Mach-O launcher architecture.
2. Build `CATGIS Desktop.app` using JDK 17 `jpackage --type app-image`,
   Gradle `installDist` Java dependencies and existing CATGIS Main.class.
   Bundle runtime Java and two native launchers: CATGIS Desktop and CATMAP.
3. Generate a genuine macOS `.icns` icon from the existing CATGIS image source
   via Java PNG generation and macOS built-in `iconutil`. Do not commit
   generated PNG/ICNS, archives, app bundles or foreign binaries to git.
4. Validate real bundle layout under `Contents/`, `Info.plist`,
   executable Mac-native Mach-O launcher(s), main and CATMAP classes, and
   bundled Java runtime. Smoke-test the *embedded* Java with `-version`.
   Override jpackage's default jlink `--strip-native-commands` so this
   executable remains present. Do not launch Swing UI during headless CI.
5. Run a full Java headless suite, no missing test output, no failing/errors,
   expected minimum 700 tests and reviewed skip allowance no more than 15,
   on **both** Mac architectures. Keep all Linux/Windows/PostGIS/architecture
   suites and both platform package jobs required.
6. Add a macOS matrix job to CATGIS Certification and include both exact
   named jobs in the authenticated Main Push Attestation required-jobs list.
   Preserve SHA-specific CI/Certification lease rules on PR and protected main.
7. Avoid extra Actions storage. No `actions/upload-artifact` and **no
   actions/cache** for the two Mac variants: resolve dependencies per runner,
   verify via Gradle offline for the rest of the job, report app size,
   architecture and main JAR digest to job summary only. Dependency resolution
   can retry transient upstream repository failures.
8. No Apple Developer credentials, `--mac-sign`, notarization, DMG, PKG,
   universal binary, Gatekeeper bypass or release publication. Native GDAL,
   other native libraries, GUI interaction and physical-device runtime smoke
   remain separate A0.7 gates. Do not claim them green from `.app` creation.

## Acceptance

- Apple Silicon arm64 macOS15 runner: full Java suite + executable arm64
  CATGIS/CATMAP `.app` bundle with bundled JDK17, success.
- Intel x64 macOS15 runner: independently repeat same tests/package with x64
  native launchers, success.
- CI/Certification success at exact PR SHA and later authenticated main
  `push` SHA, covering both macOS jobs and all existing Windows/Linux checks.
- No generated artifacts persisted beyond ephemeral runner filesystem.
- Clear exception handling and no runtime OS-library assumptions in Gradle.

## Explicit residual uncertainty

macOS Intel and Apple Silicon builds must be packaged separately; the output
is NOT a universal/fat binary. JDK17 app images are not automatically
notarized, signed for general Mac download, or validated for every older
macOS version. Functional native GDAL and actual GUI execution remain OPEN.
A0.7 NOT_CERTIFIED until these gates are independently resolved.
