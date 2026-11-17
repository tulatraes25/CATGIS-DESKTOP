# CATGIS / CATMAP — Primera beta usable — Tasks

Estado: ACTIVE. Base: `20d811d` (todas las tranches previas certificadas).
Cada tarea se certifica con el mismo gate GitHub-first.

- [x] **B1 — Identidad de beta y CLI (`AppVersion`, `--version`, `--self-test`)**
  - Producto/version/canal, parser testeable, About de CATMAP con version real,
    `build.gradle` en `1.0.0-beta.1` manteniendo `installerVersion` numerico.
  - Tests: `AppVersionContractTest`, `BetaCliContractTest`.
  - Evidence local (2026-11-16): `AppVersion.VERSION=1.0.0-beta.1`, canal beta;
    `catgis --version` y `--self-test` verificados en local (13 checks,
    `SELFTEST_OK`); About de CATMAP sin literal "Version 1.0"; guard de
    arquitectura permite solo a `BetaCli` la salida de consola documentada.
    Build local **1576 tests / 0 fallos / 15 skips**. Certificacion CI en curso.

- [ ] **B2 — Splash propio y cierre de T33**
  - `catmap/ui/CatmapSplash` tolerante a headless; arranque de CATMAP lo muestra
    y cierra; docs de empaquetado actualizadas.
  - Tests: `CatmapSplashContractTest`; launcher `bin/CATMAP` sigue verificado por
    `verifyLinuxAppImage`.

- [ ] **B3 — Menu Analisis en CATMAP standalone**
  - Curvas de nivel, drenaje, cuencas, inundacion, riesgo, viewshed y perfil con
    los dialogos existentes; inventarios/baselines actualizados.
  - Tests de inventario + contrato de acciones (sin duplicar dialogos).

- [ ] **B4 — Documentacion de beta**
  - `docs/BETA_README.md`, `docs/release/BETA_NOTES.md`, `docs/CATMAP_STANDALONE.md`
    actualizado (requisitos, flujo, capacidades y limitaciones declaradas).

- [ ] **B5 — Smoke de beta en CI**
  - Paso `--self-test` en el workflow de certificacion/CI para Linux, Windows y
    macOS (headless), con salida `SELFTEST_OK` y codigo 0.

- [ ] **B6 — Pre-release de GitHub**
  - `release-beta.yml` (workflow_dispatch, `contents: write`): app-images
    Linux/Windows/macOS, artefactos adjuntos y `gh release create --prerelease`
    con las notas de B4. Ejecucion manual verificada.

- [ ] **B7 — Certificacion y cierre**
  - Cada tranche con PR/checks/merge expected head/post-main/attestation; log de
    certificacion de beta y cierre del objetivo.

## Dependencias

- B2–B3 dependen de B1 (version/canal para About y splash).
- B6 depende de B4 (notas) y B2 (launcher/splash).
- B5 puede ir con B1 o B3; B7 cierra todo.
