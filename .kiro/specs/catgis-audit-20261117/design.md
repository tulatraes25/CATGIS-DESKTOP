# CATGIS — Remediación audit #263 — Design

## Estrategia

Tranches verticales pequenos; cada uno con:

```
issue(s) -> caso RED (test/oraculo) -> fix -> docs/manifest -> PR -> gates -> merge -> post-main -> attestation
```

No se agrupan fixes no relacionados. Los cambios de datos/proyecto llevan
migración versionada y fixtures deterministas.

## T0 — Reconciliación (este PR)

- `.kiro/specs/catgis-audit-20261117/` (spec).
- `docs/CAPABILITY_MANIFEST.md` como única fuente de versión/capacidad; deriva de
  `AppVersion`, release y evidencia de CI.
- `docs/quality/AUDIT_263_RECONCILIATION_20261117.md`: snapshot de `main`, runs,
  ruleset antes/después, estado real de issues y plan.
- Corrección de `docs/BETA_README.md` (beta.2, comandos, T2–T4 certificado).
- Ruleset: se agregan los 25 contextos de certificación/runtime/nativos/
  GeoParquet/inventarios/attestation al required set (snapshot previo guardado).
- Ledger de certificación: entrada de auditoría; **A0.7/F3 permanecen
  NOT_CERTIFIED**.

## T2 — Ciencia (siguiente)

- #258 IDW: origen vertical unico y envelope exacto; test de centros y GeoTIFF.
- #262 ASCII grid: fallo cerrado si dx!=dy (o AAIGrid cuadrado declarado);
  stats sólo muestras finitas.
- #257 basin/volume: normalización métrica desde CRS (m/grados/pies) y signos
  corte/relleno; oráculos.
- #256 raster calculator: raster float/NoData y alineación de grillas/CRS
  declarada; no ARGB como único resultado.
- #259 CRS favoritos: catálogo EPSG autoritativo (IGN/POSGAR), testigo de
  transformación; retirar favoritos inventados.
- #162 NetCDF: respetar CF grid_mapping/units/NoData; sin relabel 4326.

## T1 — Datos/proyecto (tras T2 o en paralelo si no hay conflicto)

- #144/#146: apertura staged + commit atómico; roundtrip por tipo.
- #155/#260/#148/#149: publicación de familia SHP con journal; identidad física
  por capa; codec reversible; autosnapshot/recovery.
- #153/#151/#150/#195/#193/#175/#176: CRS de fuente real, preview LAS explícito,
  FlatGeobuf acotado.

## T3/T4/T5

Ver requirements; no se abordan hasta cerrar T0–T2 con evidencia.

## Riesgos

- Cambiar el ruleset puede bloquear merges si un contexto no se produce en PRs;
  se prueba con el PR de T0 y se revierte el payload anterior si es necesario
  (snapshot `.ruleset-before-20261117.json`).
- Cada fix científico puede alterar goldens/formatos: políticas de golden
  actualizadas sólo con justificación y regeneración Linux.
