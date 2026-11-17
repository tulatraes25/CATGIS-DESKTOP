# Beta 1.0.0-beta.1 — Certificacion GitHub-first

Estado: tranches B1–B6 certificadas; B7 cierra con la pre-release verificada.

| PR | Tranches | Head exacto | Post-main | Attestation |
|---|---|---|---|---|
| #245 | B1 spec Kiro + identidad/CLI | `8e09a8e` | `a8f9674` cert/identity/CI/GeoParquet SUCCESS | SUCCESS (`43991875801`) |
| #246 | B2 splash + B3 menu Analisis + B4 docs + B5 smoke + B6 release workflow | `4999afe` | `5803ecb` cert/identity/CI/GeoParquet SUCCESS (cert rerun por HTTP 429 externo) | SUCCESS (`43996181703`) |
| #247 | B7 log/workflow de pre-release | `136c1b9` | `e93dc47` cert/identity/CI/GeoParquet SUCCESS (rerun por timeout externo de Gradle) | SUCCESS (`43999907468`) |
| #248 | fix runner macOS de pre-release | `713c860` | `ac82b3e` cert/identity/CI/GeoParquet SUCCESS | SUCCESS (`44003370669`) |

Notas:

- Fix real de CI en B1: los contratos macOS/Windows tenian `catgis-desktop-1.0.jar`
  hardcodeado; ahora descubren el JAR por glob.
- Fix real de B6: `release-beta.yml` usaba `--offline` sin cache en runners nuevos;
  corregido antes de ejecutar la pre-release.
- Smoke `--self-test` agregado a los jobs de certificacion macOS/Linux/Windows.
- **Pre-release publicada**: `v1.0.0-beta.1` (run `44003466861`, 4/4 jobs
  SUCCESS) con `catgis-catmap-beta-linux.tar.gz`, `catgis-catmap-beta-windows.zip`
  y `catgis-catmap-beta-macos-arm64.zip`. Los jobs `macos-14` se cancelaban al
  iniciar (runner retirado); se corrigio a `macos-15` en PR #248.
- Limitaciones declaradas: WMTS no soportado, splash simple, instaladores sin
  firma, T2–T4 SQL fuera de esta beta, A0.5/A0.7/F3 `NOT_CERTIFIED`.
