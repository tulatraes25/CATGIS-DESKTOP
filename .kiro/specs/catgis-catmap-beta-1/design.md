# CATGIS / CATMAP — Primera beta usable — Design

## Arquitectura del cambio

La beta no reescribe modulos: agrega una capa fina de identidad y entrada, y
expone en CATMAP los analisis ya certificados (T17–T25) mediante su menu.

```
catgis/Main ──┬─ BetaCli.parse(args)
              ├─ --version  → AppVersion.versionText()
              ├─ --self-test → BetaCli.runSelfTest()  (headless)
              └─ --catmap-standalone → catmap/Main.main(resto)
catmap/Main ──┬─ BetaCli.parse(args) (mismo contrato)
              ├─ CatmapSplash (breve, tolerante a headless)
              ├─ menu Analisis → dialogos existentes (ContourGenerationDialog, ...)
              └─ Acerca de → AppVersion
```

## Decisiones

- **Version**: `AppVersion.VERSION = "1.0.0-beta.1"`, canal `beta`; `build.gradle`
  `version = '1.0.0-beta.1'` mantiene `installerVersion = '1.0.0'` para jpackage
  (formato numerico) y diferencia la beta en producto/CLI/docs.
- **CLI**: parser propio y testeable (`BetaCli.parse`) en vez de cadenas
  dispersas; `--self-test` no instancia Swing (`Class.forName` de capacidades).
- **Splash**: `CatmapSplash` es un `JWindow` con degradado y textos; se cierra a
  los ~700 ms o cuando la ventana esta lista, y en headless no se crea.
- **Analisis**: se reutilizan los 7 dialogos existentes (sin duplicar logica).
  El contexto standalone ya instala `AppContext`/mapPanel (T1b), por lo que las
  acciones existentes funcionan; si un dialogo requiere raster, muestra su
  propio aviso de "sin raster".
- **Inventarios**: los nuevos comandos visibles actualizan
  `SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv` y mapas/matriz semantica en el
  mismo PR, con baselines exactos.
- **Release beta**: workflow manual `release-beta.yml` con `contents: write`,
  matriz Linux/Windows/macOS que empaqueta app-images, las adjunta y crea una
  pre-release con `gh release create --prerelease`. Nunca toca releases estables.

## Pruebas

- `AppVersionContractTest`: formato, canal y descripciones.
- `BetaCliContractTest`: parseo, versionText, `runSelfTest()` = 0 con >=10
  checks y sin dependencia de display.
- `CatmapSplashContractTest`: tolerante a headless, no lanza excepciones.
- Inventarios: tests de arquitectura existentes.
- Smoke de beta: `--self-test` en Linux/Windows/macOS se ejecuta en CI como paso
  del job de certificacion (opcional, dentro de esta spec).

## Riesgos

- jpackage puede rechazar app-version no numerica: se conserva `1.0.0`.
- Los dialogos de analisis requieren capas raster: en una sesion vacia muestran
  su aviso; no se simula exito.
- La GUI no se declara verificada: el snapshot Xvfb sigue siendo evidencia sin
  claim (A0.7/F3 `NOT_CERTIFIED`).
