# Normalización CRS — política explícita vs default (2026-11-17)

Issue #163 (audit #263). Estado: **implementado y certificado**.

## Problema

`CRSDefinitions.normalizeCode("")` devuelve `EPSG:4326`. Varias rutas
CRS-sensibles normalizaban primero y luego preguntaban `isBlank()`, un chequeo
muerto: un CRS ausente se convertía silenciosamente en WGS84 y luego se
transformaba/validaba con un CRS inventado (misma causa raíz que #128 y #151).

## Política

| Método | Contrato | Uso |
|---|---|---|
| `normalizeExplicit(code)` | Vacío/`null` → `""`; `32719`/`epsg:32719`/`urn:` → código normalizado | Decisiones/provenance/validación |
| `normalizeCode(code)` | Igual que explícito pero vacío → **`EPSG:4326`** | Defaults de UI (selectores, proyecto inicial) |

`normalizeCode` ahora delega en `normalizeExplicit`, por lo que la semántica
para códigos no vacíos es idéntica.

Rutas migradas a `normalizeExplicit`:

- `CoordinateTransformSupport` (transformaciones puntuales y por lotes; un CRS
  faltante devuelve `null`/falla en vez de asumir WGS84).
- `BooleanRiskService` (reproyección de muestreo y detección geográfica).
- `VectorCrsTransformSupport` (resolución de CRS de vector; sin autoridad
  inventada).
- `FlatGeobufCrsSupport`.
- `RasterCoverageSupport.declaredCode` (delegado directo).

## Evidencia

`CrsNormalizationSplitContractTest` (4):

1. `normalizeCode("")` conserva el default `EPSG:4326` para UI.
2. `normalizeExplicit` preserva vacío/`null` y normaliza códigos válidos.
3. `transformPoint` con CRS faltante → `null` (no WGS84); transformación
   explícita 4326→3857 sigue funcionando.
4. `declaredCode` no inventa CRS.

Suite local: 1654 tests, 0 fallos, 15 skips.

## Límites

- Los defaults de UI siguen siendo `EPSG:4326` por política de producto; no se
  presentan como proveniencia del dato.
- El resto de llamadas a `normalizeCode` se revisó por categoría; las de
  presentación/selector conservan el default.
