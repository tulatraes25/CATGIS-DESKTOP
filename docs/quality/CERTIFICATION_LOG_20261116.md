# Certificacion GitHub-first — 2026-11-16

Estado: tranches completados y certificados sobre `main`; A0.5/A0.7/F3 siguen
`NOT_CERTIFIED` y no se declara GUI/runtime.

| PR | Tranches | Head exacto | Post-main | Attestation |
|---|---|---|---|---|
| #238 | T24 | `96b79c1` | `95eaafb` cert/CI/identity/GeoParquet SUCCESS | SUCCESS (`43942385374`) |
| #239 | T21/T23/T22 | `7f34311` | `cc9b9b6` cert/identity SUCCESS | SUCCESS (`43949363998`) |
| #240 | T25/T27/T26/T28/T29/T30 | `b61b04c` | `93b7801` cert/identity SUCCESS | SUCCESS (`43954783237`) |
| #241 | T31/T32/T33/A0.5/decisiones SQL | `bf1408a` | `9edac76` cert/identity SUCCESS | SUCCESS (`43962661700`) |
| #242 | T15 | `0d1d5c0` | `1fa78da` cert/identity SUCCESS | SUCCESS |
| #243 | T16 | `0994a40` | `7341d65` cert/identity/CI/GeoParquet SUCCESS | SUCCESS (`43976754341`) |

Notas:

- `main` previo `11c6753` (T20) cerro sus gates tras la caida de OSGeo:
  Certification `43885489397` SUCCESS y Attestation `43936521401` SUCCESS.
- UI Snapshot (T32) ejecutado en Actions/Xvfb: run `43960374643` SUCCESS
  (artefacto `catmap-ui-snapshot`), sin claim de runtime.
- Pendientes declarados: T15 WMTS; T33 splash propio; T2–T4 implementacion SQL
  (decisiones A/A/A ya registradas); A0.5/A0.7/F3 gates de arquitectura.
