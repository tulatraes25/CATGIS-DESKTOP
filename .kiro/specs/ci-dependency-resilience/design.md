# CI Dependency Resilience — Design

Status: ACTIVE
Baseline: `4da6b3036d6d59c7b4726d3b906f710215c3c269`

## Problem

The storage gate removed unbounded Gradle caches, but CATGIS then performed many cold dependency resolutions in parallel. GeoTools 34.0 and climate dependencies are served from OSGeo; an OSGeo HTTP 502 caused every Java/GIS post-main job to fail before compilation/tests could run.

The fix is one reusable Gradle dependency cache per OS and dependency hash, not the previous per-job cache model.

## Cache model

Use `actions/cache@v4` explicitly.

Paths:
- Linux: `~/.gradle/caches/modules-2`;
- Windows hosted runner: `D:\\a\\.gradle\\caches\\modules-2` (the actual `GRADLE_USER_HOME` selected by `setup-gradle` on the runner).

Key:
- `catgis-gradle-deps-v1-${{ runner.os }}-${{ hashFiles('catgis-desktop/build.gradle', 'catgis-desktop/settings.gradle*', 'catgis-desktop/gradle/wrapper/gradle-wrapper.properties') }}`

The key deliberately excludes workflow name, job name, branch SHA and run number.

## Gradle prime task

Add `resolveCiDependencies` to `catgis-desktop/build.gradle`.

It resolves every Gradle configuration where `canBeResolved == true`, in deterministic name order. This includes Java compile/runtime/test classpaths plus plugin/tool configurations such as Checkstyle and JaCoCo that downstream jobs also need in offline mode.

No product behavior changes.

## Workflow ordering

### CATGIS Certification

Add Linux and Windows dependency-prime jobs. Each:
1. checkout;
2. setup JDK 17;
3. restore shared dependency cache;
4. setup Gradle with internal cache disabled;
5. on cache miss, run `resolveCiDependencies`.

Downstream jobs depend on the corresponding prime, restore the same cache with `fail-on-cache-miss: true`, and run Gradle `--offline`.

### CATGIS CI

Add a Linux dependency-prime job. Java Desktop Build & Test depends on it and runs offline. Repository Safety and Web Lint remain independent.

## Storage maintenance

Routine cleanup preserves keys beginning with `catgis-gradle-deps-v1-` and deletes unrelated Actions caches. GitHub also evicts inactive caches, providing a second storage bound.

## Failure semantics

- Cold cache + OSGeo unavailable: prime fails explicitly as external infrastructure blocker.
- Warm cache + OSGeo unavailable: downstream jobs continue offline.
- Dependency definition changes: one new prime is required.
- Downstream cache miss after prime: fail immediately.

## Non-goals

- no vendored GeoTools jars;
- no unofficial Maven mirror;
- no dependency version changes;
- no weakened tests;
- no re-enable of setup-gradle per-job cache.
