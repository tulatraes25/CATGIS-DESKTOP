# Installer Fix 2026-06-03

## Problema real

El instalador de `CATGIS Desktop` estaba mezclando dos caminos de instalación distintos:

- una instalación previa basada en `jpackage/MSI`
- la instalación actual basada en `Inno Setup`

Ambas quedaban registradas para el mismo producto y la misma carpeta:

- `CATGIS Desktop`
- `C:\Program Files\CATGIS Desktop`

Eso generaba conflictos de reinstalación, reparación y actualización.

## Causa raíz

El script de Inno Setup no limpiaba la instalación MSI heredada antes de copiar la app-image nueva.

Resultado:

- podían convivir dos entradas de desinstalación
- podían quedar accesos directos y metadata mezclados
- el instalador podía parecer roto o inconsistente aunque el `.exe` existiera

## Corrección aplicada

Se actualizó:

- `C:\CATGIS\catgis-desktop\packaging\windows\CATGIS_BILINGUAL_SETUP.iss`

Cambios:

- se agregó código `[Code]` en Inno Setup
- el instalador ahora busca una instalación MSI heredada de `CATGIS Desktop`
- si la encuentra, ejecuta:

```text
msiexec /x {PRODUCT-CODE} /qn /norestart
```

- luego continúa con la instalación Inno normal

## Generación del instalador corregido

Como el `.exe` anterior en `build\installer\windows\exe` estaba bloqueado por procesos abiertos, el instalador corregido se compiló directamente con `ISCC.exe` a una carpeta nueva:

- `C:\CATGIS\catgis-desktop\build\installer\windows\exe-fixed`

Archivo resultante:

- `C:\CATGIS\catgis-desktop\build\installer\windows\exe-fixed\CATGIS Desktop Review-1.0.0-fixed.exe`

## Estado

- el script compiló correctamente
- el instalador corregido quedó generado
- la corrección ataca el conflicto MSI/Inno que rompía upgrades/reinstalaciones

## Recomendación

Usar como instalador válido:

- `C:\CATGIS\catgis-desktop\build\installer\windows\exe-fixed\CATGIS Desktop Review-1.0.0-fixed.exe`

Y dejar de distribuir builds anteriores generadas antes de este fix.
