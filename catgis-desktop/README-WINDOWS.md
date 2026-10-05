# CATGIS Desktop - ejecucion local en Windows

## Requisitos

- JDK 17 activo
- Gradle en PATH

## Validacion rapida

Abrir PowerShell o hacer doble clic en:

- `probar-catgis.bat` para correr pruebas y build
- `generar-instalador-app-image.bat` para generar la app-image Windows
- `generar-instalador-exe.bat` para intentar el instalador `.exe` con Inno Setup

## Comandos equivalentes

```powershell
gradle clean test build
gradle packageWindowsAppImage
gradle packageWindowsExeInstaller
```

## Reportes

Los scripts guardan logs en la carpeta `reportes`.

## Nota

Para generar el instalador `.exe` completo se necesita:

- Inno Setup 6 o WiX, segun la tarea que uses
- los recursos bajo `packaging/windows`
