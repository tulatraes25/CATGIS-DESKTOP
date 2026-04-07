# Instalador Windows de CATGIS Desktop

Esta carpeta deja preparada la base de distribucion Windows de `CATGIS Desktop` como version `Review / Beta`, con runtime Java embebido para que el usuario final no tenga que instalar Java manualmente.

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

## Licencia de instalacion

El instalador usa la licencia de revision incluida en:

- `packaging/windows/CATGIS_REVIEW_LICENSE.rtf`

Esa licencia aclara que la distribucion es una version `Review / Beta` no final.
