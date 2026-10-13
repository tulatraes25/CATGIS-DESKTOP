# CATGIS — SPEC/Kiro native GDAL path portability (Windows/Linux)

Status: CANDIDATE / NOT_CERTIFIED
Protected main base: `43360c9881bbe95d1d1defb2c40bd28ef9af3d90`
Branch: `quality/a0-linux-gdal-native-path-portability-post55`
A0.7 and desktop GUI certification remain OPEN.

## Source-confirmed defect

The application Gradle plugin previously always set
`-Djava.library.path=${findGdalBin()}`. `findGdalBin` searches only
OSGeo4W's Windows `gdal_translate.exe`, returning empty on ordinary Linux.
An empty `-Djava.library.path=` overrides Java's standard native-library
search path, disabling resolution of otherwise installed Linux JNI libraries.
Windows users without OSGeo4W also inherited this empty override.

## Corrective requirements

- Preserve the existing Windows OSGeo4W discovery behavior and its native
  library path when a valid `gdal_translate.exe` is present.
- With no detected OSGeo4W path (normal Linux/CI), **omit** the system property
  entirely and let the JVM perform its platform-native lookup. Never emit a
  blank `-Djava.library.path=` argument in generated start scripts.
- Preserve UTF-8 encoding, main class, app distribution, Java 17 and all
  existing Gradle test/integration entrypoints.
- Add a Gradle verification task depending on actual configured launcher
  arguments. Run it as a dependency of `test`, so existing GitHub Linux and
  Windows suites test the invariant without needing GUI or native GDAL packages.
- No downloading/installing native binaries or bundling unverified DLL/.so
  files; do not claim GDAL functions work on Linux without installed GDAL/JNI.
- Do not make any GUI, repository history, dependency version or launcher
  feature changes in this tranche.

## Acceptance & verification

- Linux GitHub CI with no OSGeo4W must see ZERO native-library-path overrides.
- Windows without detected OSGeo4W also uses platform Java defaults; Windows
  with OSGeo4W preserves its existing GDAL native directory in JVM options.
- Compile/build/JUnit, Linux Full Suite, Windows Full Suite, PostGIS and
  architecture must succeed on **exact** PR head.
- Replay onto latest main if WCS #55 or XYZ #57 merges change its SHA, rerun
  complete certification, then obtain push-triggered main CI evidence.
- A0.7 remains OPEN: no signed Linux installer, Linux GDAL native integration,
  or GUI runtime smoke test is established by this Gradle configuration fix.

## Observability

The Gradle verification is a configuration invariant, not a test that any
specific `libgdalalljni.so` can be loaded. Linux native binding availability,
platform launchers, and self-contained Linux packaging are separate gates.
