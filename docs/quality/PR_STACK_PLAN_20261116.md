# Plan de certificación de la pila local — 2026-11-16 (CERRADO: PRs #238–#243 mergeados y certificados)

Estado: **COMPLETADO**. La pila se publico en PRs #238–#243 con checks PASS, merge con head exacto, post-main SUCCESS y attestation SUCCESS; `main` final `7341d65`. Detalle en `docs/quality/CERTIFICATION_LOG_20261116.md`. `repo.osgeo.org` devuelve 502/timeout,
por lo que el job `Real Native GDAL` no puede resolver `org.geotools:gt-bom:34.0` y la
Certification/attestation de `main` (`11c6753`, merge de PR #237/T20) sigue pendiente.
Ninguna rama se declara certificada hasta completar el ciclo exacto en GitHub.

## Pila local (todas lineales sobre `main = 11c6753`, build local 1562 tests / 0 fallos)

| # | Rama | Tarea | Head SHA | Commits vs main |
|---|---|---|---|---|
| 1 | `feat/terrain-idw` | T24 | `512e50a` | 3 |
| 2 | `feat/terrain-viewshed` | T21 | `dfef169` | 6 |
| 3 | `feat/terrain-volume` | T23 | `5253bf1` | 9 |
| 4 | `feat/terrain-profile` | T22 | `c806661` | 12 |
| 5 | `feat/risk-calibrated` | T25 | `d9a566b` | 15 |
| 6 | `feat/catmap-command-palette` | T27 | `2c4782c` | 18 |
| 7 | `feat/catmap-safe-exit` | T26 | `e70dd38` | 21 |
| 8 | `feat/catmap-theme-hidpi` | T28 | `0350481` | 24 |
| 9 | `feat/i18n-honest` | T29 | `f19bcee` | 27 |
| 10 | `feat/ui-cleanup` | T30 | `18e8c96` | 30 |
| 11 | `feat/golden-masters` | T31 | `9779180` | 33 |
| 12 | `feat/ui-snapshot` | T32 | `0e4a1ff` | 35 |
| 13 | `feat/catmap-launcher` | T33 | `0df298f` | 36 |
| 14 | `feat/s1-evidence` | S1 | `5beb876` | 37 |

## Orden de publicación (una rama por PR, desde `main` vivo)

Para cada rama, en este orden:

```bash
git fetch origin main
git switch -c <rama> origin/main
git cherry-pick <commits de la rama desde la base anterior>   # o push directo de la rama
git push -u origin <rama>
gh pr create --draft --base main --head <rama> --title "<título>" --body-file <cuerpo>
gh pr checks <PR> --watch        # esperar 30/30 PASS
gh pr ready <PR>
HEAD=$(git rev-parse <rama>)
gh pr merge <PR> --merge --match-head-commit $HEAD
# post-main: CI + Certification + Runtime Identity + GeoParquet SUCCESS en $HEAD
gh workflow run main-push-attestation.yml --ref main
```

## Títulos de PR propuestos

- **T24** — `feat(terrain): IDW propio con grilla acotada y estadisticas` (`feat/terrain-idw`)
- **T21** — `feat(terrain): viewshed de producto con observador/radio` (`feat/terrain-viewshed`)
- **T23** — `feat(terrain): volumen corte/relleno sobre rasters analiticos` (`feat/terrain-volume`)
- **T22** — `feat(terrain): perfil con pendiente, hipsometria y linea 3D` (`feat/terrain-profile`)
- **T25** — `feat(risk): riesgo e inundacion calibrados con parametros persistidos` (`feat/risk-calibrated`)
- **T27** — `feat(ux): paleta de comandos por tarea` (`feat/catmap-command-palette`)
- **T26** — `feat(ux): salida segura y toasts para carga masiva` (`feat/catmap-safe-exit`)
- **T28** — `feat(ux): temas claro/oscuro e HiDPI` (`feat/catmap-theme-hidpi`)
- **T29** — `feat(ux): i18n honesta sin traduccion automatica parcial` (`feat/i18n-honest`)
- **T30** — `refactor(ux): duplicados de UI y welcome page alcanzable` (`feat/ui-cleanup`)
- **T31** — `feat(verify): golden masters deterministas A0.2` (`feat/golden-masters`)
- **T32** — `test(verify): harness de snapshot UI con Xvfb` (`feat/ui-snapshot`)
- **T33** — `feat(packaging): launcher CATMAP en app-image Linux` (`feat/catmap-launcher`)
- **S1** — `docs(s1): evidencia A0.5 fgb/las y fichas SQL` (`feat/s1-evidence`)

## Evidencia a adjuntar en cada PR

- Build local: `1562 tests / 0 fallos / 15 skips` (el número crece con cada tranche; regenerar por rama).
- `JAVA_SOURCE_INTEGRITY_PASS files=<n>` de `python tools/qa/check_java_source_integrity.py`.
- Inventarios `docs/quality/*.tsv` y baselines actualizados cuando aplica.
- Spec `.kiro/specs/catgis-catmap-product/tasks.md` con evidencia de la tarea.
- Capturas/logs headless cuando la tarea toca UI (sin claim de runtime).

## Bloqueos y riesgos

- **OSGeo 502**: reintentar Certification/atestación de `11c6753` antes de abrir PRs; si el
  host sigue caído, todo PR nuevo hereda el job rojo y no puede certificarse.
- **Conflictos**: la pila es lineal; publicar en orden evita conflictos. Si `main` avanza,
  rebase la rama correspondiente y regenerar evidencia.
- **T20 atestación**: re-ejecutar `main-push-attestation.yml` para `11c6753` cuando el host vuelva.
- **T32/T33**: quedan en progreso parcial; no se declaran completas sin CI Linux real.
- **T2–T4 SQL**: siguen bloqueadas por decisión del owner (`SQL_DECISION_RECORDS_20261116.md`).

## Checklist de reanudación

- [ ] `curl -I https://repo.osgeo.org/.../gt-bom-34.0.pom` → 200
- [ ] Reintentar Certification + attestation de `11c6753`
- [ ] Publicar PR de `feat/terrain-idw` (T24) y seguir el orden
- [ ] Cerrar T20 con attestation PASS y actualizar `tasks.md`
- [ ] Revisar restos de A0.5 y registrar decisión DC-6/7/8 del owner

