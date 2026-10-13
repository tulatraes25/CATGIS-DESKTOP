# CATGIS — SPEC/Kiro A0.7: self-contained Linux app-image verification

Status: CANDIDATE / NOT_CERTIFIED
Protected-main base: `08aa5d5ad5e1f3aa53d51fd5ceb89c3ce000c033`
Branch: `quality/a0-linux-jpackage-appimage-post59`
Cross-platform requirement: both Windows and Linux; feature freeze in force.

## Confirmed gap

CATGIS currently builds, tests and integrates on Windows and Linux, and
Gradle has Windows-specific `jpackage` app-image/installer tasks, but no
equivalent Linux app-image packaging task or GitHub Actions certification.
Java Full Suite success does not prove a Linux distribution can be assembled
from CATGIS jars and bundled JRE. Gradle `installDist` needs a system Java
installation, so it is not a self-contained desktop release.

## Requirements

1. Add Linux-only Gradle task `packageLinuxAppImage` with `jpackage
   --type app-image`, based on `installDist` library/JAR files and JDK17
   toolchain. No APT/YUM packaging, no publishing or signing in this tranche.
2. Bundle the JDK with the app-image. Provide the standard CATGIS launcher,
   main-class `ar.com.catgis.Main`, existing app version, vendor and a
   generated PNG icon with no new binary fixture. Override jpackage's default
   jlink `--strip-native-commands` so the **embedded** `bin/java` remains
   independently executable by the Linux smoke contract. Retain
   `--strip-debug --no-man-pages --no-header-files` to bound runtime size.
   Keep Windows packaging tasks and their launcher options unchanged.
3. Add `verifyLinuxAppImage` checking launcher executable, bundled
   `lib/runtime/bin/java`, main app JAR and `Main.class`. Smoke-test the
   bundled runtime with `java -version` only; never launch Swing in CI.
4. Fail if Linux jpackage, JDK or expected files are absent; never return a
   misleading SKIPPED/PASS on Linux. Non-Linux hosts must explicitly skip
   this Linux-specific task.
5. Add a Linux job `Linux App Image Package Contract` to CATGIS Certification
   running Gradle `verifyLinuxAppImage` under the same offline/cache/Java17
   policy. Do not upload large package artifacts, since repository/actions
   storage must remain bounded. Include size and hash evidence in logs only.
6. No mandatory OS-native GDAL/JNI claim. The application may still require
   additional `.so` libraries for specific GDAL-only formats. Document this
   as an unresolved runtime A0.7 gate, separate from package creation.
7. Use exact-head Linux+Windows CI/Certification, PostGIS, architecture and
   the independent new Linux package job. Require successful exact-main
   push-run attestation after merge, including evidence for this new job.

## CI review: actual failure and root cause

The first Linux App Image Package Contract assembled its app-image
(`packageLinuxAppImage` PASS) but `verifyLinuxAppImage` FAILed because
the expected `lib/runtime/bin/java` was absent. This follows the JDK17
`jpackage` default jlink flags, which include `--strip-native-commands`.
The corrected jpackage invocation explicitly retains the Java executable so
`java -version` can test the bundled runtime. The failed run does **not**
prove the original app-image was invalid; the initial acceptance contract
was stricter than jpackage defaults. Only the new exact-head CI outcome can
certify this corrected configuration.

## Acceptance

- A Linux GitHub hosted runner produces a bundled app-image in
  `build/installer/linux/app-image/CATGIS Desktop`, including Linux
  executable launcher, main JAR, supporting dependencies and JDK17 runtime.
- Bundled runtime executes `-version` on Linux without relying on the host
  Java executable path; verify jar manifest contains CATGIS Main.class.
- Packaging adds no generated binaries or archives to the git repository.
- The existing Windows Full Suite and Windows jpackage definitions are
  unchanged and still green.
- A0.7 remains OPEN: no real Linux GUI rendering, GDAL native bindings,
  automatic install/upgrade, desktop integration, signing or end-user
  acceptance has been certified.
