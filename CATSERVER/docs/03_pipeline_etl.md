# Pipeline ETL

## 1. Inventario local

Antes de importar, registrar las fuentes en el entorno operativo. Las rutas, tamaños, fechas y conteos reales de una instalación no deben versionarse en el repositorio público.

Usar un directorio externo al repositorio, por ejemplo:

```text
<CATSERVER_SOURCE_ROOT>/
├── CAD/
├── KMZ/
└── otras_fuentes/
```

Generar el inventario local con:

```powershell
.\scripts\02_inventory_sources.ps1 -SourceRoot "<CATSERVER_SOURCE_ROOT>"
```

Los CSV resultantes quedan bajo `CATSERVER/inventory/` y están ignorados por Git. El repositorio conserva sólo la documentación y los scripts que permiten regenerarlos.

## 2. Flujo recomendado para KML/KMZ

KML/KMZ se trata inicialmente como WGS84 salvo evidencia en contrario.

1. Importar a `raw`.
2. Validar registros, geometría y atributos.
3. Corregir y normalizar en `staging`.
4. Cargar al esquema temático final.
5. Publicar mediante vistas o servicios definidos.

Ejemplo:

```powershell
.\scripts\04_import_kmz_examples.ps1 `
  -SourceRoot "<CATSERVER_SOURCE_ROOT>\KMZ" `
  -Host "localhost" `
  -Database "CATSERVER" `
  -User "catserver_etl"
```

Las credenciales se suministran en tiempo de ejecución o mediante mecanismos locales seguros. No se guardan en Git ni en connection strings versionados.

## 3. Flujo recomendado para CAD

El punto crítico es validar CRS y separar entidades CAD útiles de contenido auxiliar.

1. Conservar el archivo fuente fuera del repositorio.
2. Revisar layers CAD.
3. Confirmar CRS con puntos de control confiables.
4. Generar DXF/GPKG u otro formato intermedio cuando corresponda.
5. Importar a `raw`.
6. Corregir geometría y atributos en `staging`.
7. Cargar a la tabla temática final.

## 4. Regla de paso entre esquemas

Una capa pasa de `raw` a `staging` cuando:

- tiene geometría legible;
- tiene CRS identificado o hipótesis documentada;
- se conoce el origen del archivo;
- existe trazabilidad del lote de importación.

Una capa pasa de `staging` a esquema final cuando:

- la geometría es válida;
- nombres y atributos están normalizados;
- tiene destino temático definido;
- la validación necesaria quedó documentada.

## 5. Controles mínimos

```sql
SELECT COUNT(*) FROM raw.<tabla>;

SELECT COUNT(*)
FROM raw.<tabla>
WHERE NOT ST_IsValid(geom);

SELECT GeometryType(geom), COUNT(*)
FROM raw.<tabla>
GROUP BY GeometryType(geom);
```

## 6. Principio de publicación

El repositorio contiene código, SQL, documentación y plantillas.

Quedan fuera de Git:

- datasets municipales;
- inventarios de workstation;
- dumps;
- rutas reales;
- credenciales;
- entregas generadas.

## 7. Nombre físico de la base

El repositorio conserva material de distintas etapas: algunos scripts históricos usan la base quoted `"CATSERVER"` y componentes posteriores usan `catserver`.

No renombrar una instalación existente como parte de la sanitización documental. Cualquier normalización del nombre físico de la base debe tratarse como una migración separada y verificada.
