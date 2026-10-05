# CATGIS

CATGIS es un sistema GIS de escritorio orientado a trabajo técnico, municipal y ambiental. El repositorio reúne tres componentes principales:

- **CATGIS Desktop** — aplicación Java/Swing con GeoTools, JTS, GDAL, PostGIS, herramientas vectoriales/raster, CATMAP y análisis GIS.
- **CATSERVER** — modelo y scripts para una infraestructura geoespacial PostgreSQL/PostGIS.
- **CATSERVER Web** — visor web para publicación y consulta de capas CATSERVER.

## Fuente canónica

La única rama de desarrollo activa y canónica es **`main`**.

Después del cierre de saneamiento, `main` es la **única rama remota persistente**. Las ramas históricas y de saneamiento fueron auditadas y eliminadas administrativamente; los desarrollos futuros deben usar ramas cortas creadas desde el HEAD vigente de `main`.

Ver: [inventario y cierre de ramas](docs/repository/BRANCH_INVENTORY_2026-10-04.md).

## Estructura

```text
.
├── catgis-desktop/   # aplicación GIS de escritorio
├── CATSERVER/        # SQL, ETL y documentación PostGIS
├── catserver-web/    # visor web
├── docs/             # documentación técnica y archivo histórico
├── .github/          # CI
├── SECURITY.md       # política de secretos y respuesta a exposición
└── run.bat           # launcher de desarrollo en Windows
```

## Desarrollo

CATGIS Desktop usa Gradle y Java 17 como baseline del repositorio.

En Windows:

```bat
cd catgis-desktop
gradlew.bat test
gradlew.bat build
```

El visor web:

```bat
cd catserver-web
npm ci
npm run lint
npm test
```

> El script `npm test` debe evolucionar hacia una suite real de tests; actualmente el visor tiene principalmente validación de lint.

## Flujo Git

Para cambios nuevos:

1. crear una rama desde el HEAD actual de `main`;
2. hacer cambios atómicos;
3. abrir Pull Request;
4. ejecutar CI;
5. revisar el diff;
6. fusionar a `main`.

No realizar nuevos desarrollos directamente sobre ramas históricas.

## Seguridad

Este repositorio es público. No deben versionarse contraseñas, tokens, claves privadas, archivos `.env` reales, dumps de bases productivas ni datos municipales sensibles.

Ver [SECURITY.md](SECURITY.md).

## Licencia

CATGIS es software **propietario / source-available**, no software open source. La publicación del código en GitHub no concede permiso para usarlo, copiarlo, modificarlo, redistribuirlo, desplegarlo ni crear obras derivadas salvo autorización escrita del titular o derechos que no puedan excluirse por ley.

Ver [LICENSE](LICENSE). Los componentes de terceros conservan sus propias licencias.

## Material histórico y de terceros

El árbol activo de `main` ya no contiene el directorio de fuente decompilada de terceros ni el archivo CATSERVER que expuso credenciales. Las antiguas ramas públicas también fueron neutralizadas para apuntar al estado canónico saneado.

Esto no elimina todavía los objetos de commits históricos. La purga final del historial se ejecuta por separado y está documentada en [HISTORY_SANITIZATION_RUNBOOK.md](docs/repository/HISTORY_SANITIZATION_RUNBOOK.md).

No reutilizar ni redistribuir material histórico/de terceros sin una revisión expresa de licencia y procedencia.

## Estado de saneamiento

La fase de saneamiento del **árbol activo** quedó completada en octubre de 2026:

- secretos conocidos retirados del HEAD público;
- material decompilado de terceros retirado del árbol activo;
- inventarios operativos CATSERVER retirados del repositorio y convertidos a generación local;
- rutas personales/workstation reemplazadas por configuración portable;
- documentación y scripts temporales reorganizados;
- `main` consolidada como única fuente canónica;
- ramas públicas heredadas neutralizadas contra el estado saneado;
- CI Java/Checkstyle y Web lint estabilizados.

Estado administrativo/histórico actual:

- reescritura histórica publicada y certificada;
- `main` protegido por ruleset y única rama remota persistente;
- checkout Windows activo reemplazado por un clon post-rewrite certificado;
- licencia propietaria declarada;
- **pendiente de seguridad:** rotar/revocar credenciales CATSERVER históricamente expuestas;
- **pendiente externo:** GitHub Support debe purgar objetos/vistas pre-rewrite todavía accesibles por SHA;
- pendiente confirmar privacidad/noreply a nivel de cuenta GitHub.
