# CATGIS / CATMAP — Primera beta usable — Requisitos

Estado: ACTIVE. Base: `main` posterior a `20d811d` (todas las tranches P1–P6
certificadas hasta T16 inclusive). Esta spec define el cierre de la **primera
beta usable** en modo Kiro, sin ampliar el producto más allá de lo ya
inventariado.

## REQ-BETA-1 — Identidad de producto y canal

WHEN el usuario ejecute CATGIS o CATMAP, THE SYSTEM SHALL mostrar una version de
beta explicita y un canal `beta`, y `--version` SHALL imprimirla sin abrir GUI.

Acceptance:

- `AppVersion.VERSION` cumple `X.Y.Z-beta.N` y `CHANNEL == "beta"`.
- `catgis --version` y `catmap --version` salen con codigo 0 mostrando producto,
  version y canal.
- "Acerca de" de CATMAP muestra la version real (sin literales `Version 1.0`).

## REQ-BETA-2 — Autodiagnostico headless

WHEN el usuario ejecute `--self-test`, THE SYSTEM SHALL validar sin GUI que la
version es de beta y que las clases de capacidades clave (terreno, hidrologia,
riesgo, layout, datos, OGC) estan presentes, reportando `SELFTEST_OK` o
`SELFTEST_FAIL` y un codigo de salida verificable.

## REQ-BETA-3 — Splash y launcher CATMAP

WHEN se abra CATMAP standalone, THE SYSTEM SHALL mostrar un splash propio breve
con la marca/version beta y el app-image SHALL exponer el launcher `CATMAP`
(bin/CATMAP en Linux; launcher CATMAP en Windows) con main-class
`ar.com.catgis.catmap.Main`.

Acceptance:

- Splash propio (`catmap/ui/CatmapSplash`) sin bloquear la creacion de la
  ventana; si no hay display, se omite sin fallar.
- `verifyLinuxAppImage` exige `bin/CATMAP`; el empaquetado Windows/macOS no se
  altera.

## REQ-BETA-4 — Analisis accesibles desde CATMAP standalone

WHEN el usuario abra el menu **Analisis** de CATMAP standalone, THE SYSTEM SHALL
ofrecer: curvas de nivel, drenaje, cuencas, inundacion, riesgo, viewshed y
perfil, reutilizando los dialogos existentes con el contexto standalone
instalado.

Acceptance:

- Nuevo menu con los 7 comandos, inventarios y baselines actualizados.
- Cada comando abre el dialogo existente; los dialogos no se duplican.

## REQ-BETA-5 — Documentacion y notas de beta

THE SYSTEM SHALL documentar requisitos, flujo de uso, capacidades y limitaciones
declaradas de la beta (`docs/BETA_README.md`, `docs/release/BETA_NOTES.md`) y el
estado de tareas en `.kiro/specs/catgis-catmap-beta-1/tasks.md`.

## REQ-BETA-6 — Publicacion de pre-release

WHEN se ejecute manualmente el workflow de beta, THE SYSTEM SHALL construir los
app-images de la plataforma, adjuntar los artefactos y crear una **pre-release**
de GitHub con las notas, sin tocar releases estables.

## REQ-BETA-7 — Certificacion

Cada tranche de beta SHALL pasar PR draft → CI/Certification exactos del head →
merge con `expected_head_sha` → workflows post-main → `main-push-attestation`,
sin force-push ni cambios directos a main. A0.5/A0.7/F3 siguen `NOT_CERTIFIED`:
la beta no declara GUI/runtime nativo certificado por compilar.

## No objetivos

- WMTS (se mantiene declarado como no soportado), splash grafico complejo,
  instaladores firmados/notarizados, T2–T4 SQL (decisiones ya registradas, su
  implementacion es otra tranche), telemetria o red obligatoria.
