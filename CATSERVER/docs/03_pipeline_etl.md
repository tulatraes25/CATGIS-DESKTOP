# Pipeline ETL

## 1. Inventario

Antes de importar, registrar todo en `admin.source_asset`.

Fuentes ya identificadas:

- carpeta de DWG por barrios:
  - `C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\2 - DCCION. GRAL. DE CATASTRO ↔ Barrios DWG - A Areas MCR - Enero 2025`
- carpeta de KMZ:
  - `C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ`
- DWG principal del ejido:
  - `C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg`

## 2. Flujo recomendado para KMZ

### Hechos observados

- Los KMZ inspeccionados tienen `doc.kml` interno.
- El `doc.kml` de `Parcelas.kmz` es muy grande.
- Las cabeceras inspeccionadas muestran exportacion desde Google Earth Pro.

### Supuesto operativo

KML/KMZ se trata inicialmente como WGS84 geograficas.

### Paso a paso

1. Importar a `raw`.
2. Validar cantidad de registros, tipos de geometria y campos.
3. Corregir geometria y nombres de atributos en `staging`.
4. Cargar a esquema tematico final.
5. Publicar por vista en `public`.

### Ejemplo `ogr2ogr`

```powershell
ogr2ogr `
  -f "PostgreSQL" `
  "PG:host=localhost port=5432 dbname=CATSERVER user=catserver_etl password=CAMBIAR" `
  "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Parcelas.kmz" `
  -nln raw.kmz_parcelas_20260413 `
  -lco GEOMETRY_NAME=geom `
  -lco FID=gid `
  -nlt PROMOTE_TO_MULTI `
  -overwrite
```

## 3. Flujo recomendado para DWG

### Riesgo principal

El problema no es solo importar el DWG; el problema real es asegurar el CRS correcto y separar capas utiles de entidades CAD auxiliares.

### Paso a paso

1. Abrir el DWG en QGIS o software CAD.
2. Revisar nombres de layers CAD.
3. Confirmar CRS con un punto de control municipal.
4. Exportar capas utiles a GPKG o DXF si hace falta.
5. Importar a `raw`.
6. Limpiar geometria, disolver, multipartes y nombres en `staging`.
7. Cargar a tabla final en esquema tematico.

### Capas DWG prioritarias

- `PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg`
- `Z01 y 16 - Centro y Centro Civico Gral.Solari.dwg`
- `Z11 - Gral. E.Mosconi, 25 de Mayo y Divina Providencia.dwg`
- `Z24 - Don Bosco, Std. Sur, Gdor. Fontana, Std.Norte + Restinga Ali.dwg`
- `Z32 - Centenario, Gesta de Malvinas, Dr. R.G. Favaloro + Aeropuerto y C. Chacabuco.dwg`

## 4. Regla de paso entre esquemas

Una capa solo pasa de `raw` a `staging` si:

- tiene geometria legible
- tiene CRS identificado o hipotesis documentada
- se conoce el origen exacto del archivo

Una capa solo pasa de `staging` a esquema final si:

- la geometria es valida
- el nombre de la capa y atributos ya estan normalizados
- existe registro en `admin.import_batch`
- ya tiene destino tematico definido

## 5. Controles minimos

Ejemplos de SQL utiles:

```sql
SELECT COUNT(*) FROM raw.kmz_parcelas_20260413;

SELECT COUNT(*)
FROM raw.kmz_parcelas_20260413
WHERE NOT ST_IsValid(geom);

SELECT GeometryType(geom), COUNT(*)
FROM raw.kmz_parcelas_20260413
GROUP BY GeometryType(geom);
```

## 6. Estrategia concreta de arranque

- Primero KMZ:
  - `Parcelas.kmz`
  - `Limite de Barrios.kmz`
  - `Circunscripciones - Sectores.kmz`
  - `Zonificacion por Ordenanza.kmz`
  - `Drenaje Comodoro Rivadavia.kmz`
- Despues el ejido PAC en DWG
- Al final el lote completo de DWG por zonas

Eso te deja un primer `CATSERVER` util mas rapido, mientras resolvemos el CRS y la limpieza fina del CAD.

