# Instalador Windows de CATGIS Desktop

Esta carpeta deja preparada la base de distribucion Windows de `CATGIS Desktop` como `beta final funcional`, con runtime Java embebido para que la persona usuaria final no tenga que instalar Java manualmente.

## Tareas Gradle

- `packageWindowsAppImage`
  Genera una `app-image` Windows con runtime embebido.

- `packageWindowsExeInstaller`
  Genera el instalador `.exe` usando `jpackage`.
  Requiere `WiX Toolset 3.x` en el `PATH`.

- `packageWindowsDistribution`
  Ejecuta la preparacion general y deja lista la distribucion Windows.
  Si no hay `WiX`, genera igualmente la `app-image` y deja el proyecto listo para producir el `.exe` cuando esa dependencia este disponible.

## Salidas esperadas

- `build/installer/windows/app-image/`
  Imagen de aplicacion Windows con runtime incluido.

- `build/installer/windows/exe/`
  Instalador `.exe` final, cuando `WiX Toolset` este disponible.

## Requisitos para el `.exe`

Para generar el instalador `.exe` final hace falta:

- `JDK 21` con `jpackage`
- `WiX Toolset 3.x` disponible en el `PATH`

## Firma digital del release

La firma digital no se puede improvisar con certificados locales de prueba si el objetivo es evitar bloqueos o advertencias en equipos externos.

En esta etapa, CATGIS se distribuye como `beta final funcional sin firma digital`. Esto habilita validacion y distribucion controlada, pero puede generar bloqueos o advertencias en equipos con `Smart App Control`, `Windows Application Control` o politicas corporativas restrictivas.

La firma digital queda diferida a una etapa comercial posterior.

CATGIS ahora soporta firma automatica del `launcher` y del instalador durante el packaging si se configura alguno de estos escenarios:

- `CATGIS_SIGN_PFX`
  Ruta a un certificado `.pfx` o `.p12` de code signing.
- `CATGIS_SIGN_PFX_PASSWORD`
  Clave del certificado.
- `CATGIS_SIGN_THUMBPRINT`
  Thumbprint de un certificado de code signing ya instalado en Windows.
- `CATGIS_SIGN_TIMESTAMP_URL`
  URL de timestamp RFC3161. Si no se define, usa `http://timestamp.digicert.com`.
- `CATGIS_SIGNTOOL_EXE`
  Ruta explicita a `signtool.exe` si no esta en Windows SDK ni en el `PATH`.

Tareas utiles:

- `printWindowsSigningStatus`
  Muestra si `signtool` y la configuracion de firma estan disponibles.
- `packageWindowsExeInstaller`
  Genera y firma el launcher y el `.exe` si la firma esta configurada.
- `packageWindowsJpackageExeInstaller`
  Variante del instalador `.exe` con `jpackage`, tambien con firma automatica si corresponde.

## Licencia de instalacion

El instalador usa la licencia de instalacion incluida en:

- `packaging/windows/CATGIS_BETA_LICENSE.rtf`

Esa licencia aclara que la distribucion corresponde a una beta final funcional para validacion y distribucion controlada, sin firma digital en esta etapa.
