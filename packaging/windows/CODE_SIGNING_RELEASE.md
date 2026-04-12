# CATGIS Desktop - Firma Digital de Release

Objetivo: firmar el `launcher` de Windows y el instalador `.exe` con un certificado real de code signing para reducir bloqueos de seguridad y dejar trazabilidad de release.

## Lo que hace CATGIS ahora

El pipeline de packaging soporta firma automatica durante:

- `packageWindowsExeInstaller`
- `packageWindowsJpackageExeInstaller`

Si la firma esta configurada, se firma:

1. `build/installer/windows/app-image/CATGIS Desktop/CATGIS Desktop.exe`
2. `build/installer/windows/exe/CATGIS Desktop-1.0.0.exe`

## Requisitos reales

- `signtool.exe` disponible en Windows SDK o indicado manualmente
- certificado de `code signing` real
- timestamp activo

Advertencia:
- un certificado autofirmado sirve para pruebas internas, pero no evita advertencias ni bloqueos en equipos externos
- para distribucion seria conviene certificado OV o EV de code signing

## Variables de entorno soportadas

- `CATGIS_SIGN_PFX`
  Ruta al certificado `.pfx` o `.p12`
- `CATGIS_SIGN_PFX_PASSWORD`
  Clave del certificado
- `CATGIS_SIGN_THUMBPRINT`
  Thumbprint del certificado instalado en Windows
- `CATGIS_SIGN_TIMESTAMP_URL`
  URL del servidor de timestamp
- `CATGIS_SIGNTOOL_EXE`
  Ruta directa a `signtool.exe`

## Flujo recomendado con PFX

```powershell
$env:CATGIS_SIGN_PFX='C:\ruta\certificado-codesign.pfx'
$env:CATGIS_SIGN_PFX_PASSWORD='clave-segura'
$env:CATGIS_SIGN_TIMESTAMP_URL='http://timestamp.digicert.com'
```

Opcional si `signtool` no esta detectable:

```powershell
$env:CATGIS_SIGNTOOL_EXE='C:\Program Files (x86)\Windows Kits\10\bin\10.0.22621.0\x64\signtool.exe'
```

## Verificacion previa

```powershell
gradle printWindowsSigningStatus
```

## Generacion firmada

```powershell
gradle packageWindowsExeInstaller
```

## Verificacion posterior

Comprobar estado de firma:

```powershell
Get-AuthenticodeSignature 'build\installer\windows\exe\CATGIS Desktop-1.0.0.exe'
Get-AuthenticodeSignature 'build\installer\windows\app-image\CATGIS Desktop\CATGIS Desktop.exe'
```

Se espera `Status = Valid`.

## Criterio de cierre

El bloque de firma digital puede considerarse cerrado cuando:

- `signtool` esta disponible
- el certificado real de code signing esta configurado
- launcher e instalador quedan con `Status = Valid`
- el instalador abre y se ejecuta en una maquina que antes bloqueaba builds no firmadas
