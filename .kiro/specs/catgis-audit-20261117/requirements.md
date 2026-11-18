# CATGIS — Remediación audit #263 — Requisitos

Base: `main` `c0484a1b5da48b28146e88941f14122e1865c64f` (2026-11-17), release
`v1.0.0-beta.2`. Origen: issue **#263** "SPEC/Kiro AUDITORÍA INTEGRAL
2026-11-17" y sus issues hijos #144–#262.

## Objetivo

Convertir el backlog integral de auditoría en tranches pequenos, certificados
GitHub-first (RED → fix → PR → CI/Certification/Runtime/GeoParquet → merge con
expected head → post-main → attestation), sin declarar A0.7/F3 cerrados ni
capacidades no verificadas.

## REQ-AUD-0 — Gobernanza y reconciliación (T0)

- Ledger y documentación deben citar la auditoría y no contradecir `main`.
- Un **manifiesto único** de versión y capacidad (`docs/CAPABILITY_MANIFEST.md`)
  con estados SUPPORTED_VERIFIED / BETA_LIMITED / REQUIRES_DEPENDENCY /
  INSPECTION_ONLY / UNSUPPORTED.
- Ruleset activo debe exigir certificación nativa y atestación, no sólo 3 checks.

## REQ-AUD-1 — No perder trabajo del usuario (T1)

Fallos cerrados y atómicos en abrir/guardar proyectos; sin éxito parcial; sin
colisiones de nombres; recuperación tras crash; codec reversible. Issues
#144, #146, #155, #260, #148, #149, #153, #151, #150, #195, #193, #175, #176.

## REQ-AUD-2 — Ciencia básica fiable (T2)

Raster/vector con CRS, NoData, unidades y signos correctos; no cuantización
silenciosa; oráculos numéricos y GeoTIFF reabiertos. Issues #256, #257, #258,
#259, #262, #162, #139, #163, #128, #161.

## REQ-AUD-3 — Datos, proveedores e integración (T3/T4)

Descargas atómicas con sidecar, CF/NetCDF sin relabel, PostGIS
reemplazo transaccional, seguridad de credenciales, OGC/WCS/WMTS y DC-5.
Issues #156, #261, #162, #161, #128, #174, #112, #150, #152.

## REQ-AUD-4 — A0.7/F3 y release (T5)

Sólo cuando se cumplan los gates de arquitectura y una sesión GUI interactiva
certificada; el E2E headless y los snapshots no sustituyen F3.2/F3.3/F3.4.

## Criterios de aceptación por tranche

1. Caso RED reproducido (test/oráculo) en el PR.
2. Fix mínimo, sin ruptura de compatibilidad y con migración versionada.
3. CI exacto: CI + Certification (15 jobs) + Runtime Identity (8) + GeoParquet.
4. Merge con `expected_head_sha`; post-main y `main-push-attestation` PASS.
5. Documentación/manifiesto actualizado; issue vinculado y estado real.
