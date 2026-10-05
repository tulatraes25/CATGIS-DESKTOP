# Inventarios locales de CATSERVER

Este directorio puede contener inventarios y resúmenes generados durante procesos ETL o de control.

Los CSV reales no se versionan porque pueden incluir nombres de fuentes municipales, conteos operativos, tamaños, fechas o rutas propias de una instalación.

## Generación

El inventario principal puede generarse con:

```powershell
.\scripts\02_inventory_sources.ps1 -SourceRoot "<CATSERVER_SOURCE_ROOT>"
```

Los archivos `*.csv` creados en este directorio están ignorados por Git.

## Uso en entregas

`scripts/16_build_shape_delivery_package.ps1` copia automáticamente los CSV locales existentes hacia `99_control/inventarios` cuando se arma una entrega. Por lo tanto, los inventarios siguen formando parte del flujo operativo sin tener que publicarlos en el repositorio.

## Qué sí se versiona

- scripts que generan inventarios;
- documentación del flujo;
- estructura y convenciones;
- ejemplos sin información operativa real.

No guardar aquí credenciales, dumps de bases productivas ni datos municipales sensibles.
