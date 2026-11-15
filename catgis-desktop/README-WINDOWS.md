# CATGIS Desktop - ejecucion local en Windows

## Requisitos

- JDK 21 (Gradle lo resuelve via toolchain segun `gradle.properties`)
- No hace falta instalar Gradle: el repositorio incluye el wrapper (`gradlew.bat`)
- Opcional: OSGeo4W con GDAL para las funciones raster/GDAL

## Validacion rapida

Abrir PowerShell o hacer doble clic en:

- `probar-catgis.bat` para correr pruebas y build
- `generar-instalador-app-image.bat` para generar la app-image Windows
- `generar-instalador-exe.bat` para intentar el instalador `.exe` (requiere WiX Toolset 3.x)

## Comandos equivalentes

```powershell
.\gradlew.bat clean test build
.\gradlew.bat packageWindowsAppImage
.\gradlew.bat packageWindowsJpackageExeInstaller
```

## GDAL (opcional)

La aplicacion detecta GDAL en `C:\OSGeo4W64\bin`, `C:\OSGeo4W\bin` o en la ruta indicada
por la variable de entorno `CATGIS_OSGEO4W` (por ejemplo `CATGIS_OSGEO4W=C:\OSGeo4W`).
Si no se encuentra, la aplicacion arranca igual pero sin funciones GDAL.

## Firma digital (opcional)

El empaquetado firma los binarios solo si se configura:

- `CATGIS_SIGN_PFX` (o `CATGIS_SIGN_CERT_FILE`) y `CATGIS_SIGN_PFX_PASSWORD`
- o `CATGIS_SIGN_THUMBPRINT`
- opcionalmente `CATGIS_SIGN_TIMESTAMP_URL` y `CATGIS_SIGNTOOL_EXE`

## Reportes

Los scripts guardan logs en la carpeta `reportes`.

## Nota

Para generar el instalador `.exe` completo se necesita WiX Toolset 3.x
(`candle.exe` y `light.exe` en el PATH o en `tools\wix314\bin`) y los recursos bajo
`packaging/windows`.
