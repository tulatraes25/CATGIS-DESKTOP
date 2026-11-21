# Release readiness — estado exacto (2026-11-21)

## Veredicto

**NO es la versión final.** Es una **beta funcional certificada** (`1.0.0-beta.2`)
cuyo código y documentación están corregidos y verificados en CI/headless. La
versión final `1.0.0` requiere cerrar los gates físicos **A0.7/F3** y las
decisiones de programa; no se puede certificar desde este entorno.

## Evidencia de la candidata

| Elemento | Estado |
|---|---|
| `main` de código certificado | `68a9f96` |
| Certification post-main | `44906799689` SUCCESS |
| Runtime Identity post-main | `44906799701` SUCCESS |
| Attestation post-main | `44907764585` SUCCESS |
| Suite local | **1735 tests / 0 fallos / 15 skips** |
| Integridad fuentes | `JAVA_SOURCE_INTEGRITY_PASS files=903` |
| CLI | `--version` → `CATGIS Desktop 1.0.0-beta.2 (beta)` |
| Self-test | `SELFTEST_OK checks=13` |
| E2E headless | `E2E_OK checks=13` (proyecto/CRS, SHP, GeoTIFF, terreno, PNG/PDF/SVG, layout, workspace), re-ejecutado sobre `68a9f96` |
| Versión consistente | `AppVersion`, `build.gradle`, manifiesto y `BETA_README` en `1.0.0-beta.2` |
| Release workflow | `release-beta.yml` publica pre-release con app-images Linux/Windows/macOS |

## Diagnóstico #132 (repetibilidad)

- `ScriptNativeLifecycleContractTest` ejecutado **3 veces localmente** sobre
  `68a9f96`: **3/3 PASS** (sintaxis acotada, salida 300 KB, stdin acotado).
  La anomalía Windows original **no se reprodujo**; no se declara erradicada.

## Matriz de gates

| Gate | Estado | Evidencia / bloqueo |
|---|---|---|
| T0 gobernanza | PASS | ruleset 25 checks, manifiesto, reconciliación |
| T1 datos/proyecto | PASS | crash journal, snapshots, SHP/SpatiaLite/FGB/LAS |
| T2 ciencia | PASS | oráculos numéricos, GeoTIFF reabiertos |
| T3 formatos/proveedores | PARCIAL | #112/#175 congelados por decisión owner; LAZ requiere PDAL |
| T4 seguridad/integración | PASS | PostGIS real, vault OS-backed, WCS, DC-5 `UNSUPPORTED` |
| T5 implementación CATMAP | PASS | gates #95/#96/#98/#99/#102 con tests |
| **A0.7/F3 físico** | **NOT_CERTIFIED** | falta matriz humana interactiva: HiDPI/foco, gestos, impresión física, FPS/memoria, multi-sesión |
| Release final `1.0.0` | **NO** | bloqueada por A0.7/F3 y decisión de programa |

## Qué falta exactamente para la versión final

1. **A0.7/F3 físico** en Windows/Linux/macOS Intel+ARM con sesión humana:
   - HiDPI y cambio de escala en caliente; foco/teclado/gestos.
   - Impresión física y PDF de producción con revisión visual.
   - FPS/memoria con datasets reales y multi-sesión.
   - Snapshots de UI ya generados (`44545814882`) como precondición, no sustituto.
2. **Decisiones de programa** sobre residuales: #132 (anomalía Windows de
   scripting), #114 (performance), #82/#74/#73 (gates físicos/nativos),
   #112/#175 (congelados por owner hasta A0.7).
3. **Aprobación explícita del owner** para publicar `v1.0.0` y ejecutar
   `release-beta.yml`/`gh release create` (el workflow ya está probado con
   pre-release beta).

## Conclusión

El producto está en una **beta corregida, funcional y certificada** con 0 PRs
abiertos y 43 issues del audit cerrados con evidencia. No se declara versión
final ni A0.7/F3 certificado; cualquier afirmación contraria sería
`NOT_VERIFIED`.
