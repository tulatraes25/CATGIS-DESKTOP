# CATSERVER Web

Visor web base para `CATSERVER`, sin PHP ni frameworks pesados.

## Componentes

- servidor Node.js liviano;
- frontend HTML/CSS/JS;
- mapa web con MapLibre;
- catálogo municipal y temático;
- búsqueda de barrios, parcelas y zonificación;
- lectura directa desde PostgreSQL/PostGIS.

## Requisitos

- Node.js;
- acceso a una instancia PostgreSQL/PostGIS;
- un navegador web.

## Configuración

La configuración local se realiza mediante **variables de entorno**. No se deben versionar credenciales reales.

El servidor lee automáticamente un archivo `.env` si existe en `catserver-web/` (las variables ya definidas en el sistema tienen prioridad). También admite, de forma opcional, un archivo `config/local.json` con la misma estructura; ambos están excluidos por `.gitignore`.

Plantilla pública:

`.env.example`

Variables admitidas:

- `CATSERVER_WEB_PORT`
- `CATSERVER_DB_HOST`
- `CATSERVER_DB_PORT`
- `CATSERVER_DB_NAME`
- `CATSERVER_DB_USER`
- `CATSERVER_DB_PASSWORD`
- `CATSERVER_WEB_API_KEY`
- `CATSERVER_WEB_TRUST_PROXY`

El archivo `.env.example` contiene sólo valores de ejemplo. Las contraseñas, tokens y configuraciones específicas de una máquina deben permanecer fuera de Git.

## Ejecutar

Desde PowerShell o CMD:

```bat
cd catserver-web
npm ci
npm start
```

Por defecto el servidor usa el puerto `3080`.

En Windows también puede utilizarse el launcher portable:

```bat
INICIAR_CATSERVER_WEB.cmd
```

o indicar un puerto:

```bat
INICIAR_CATSERVER_WEB.cmd 3091
```

El launcher usa rutas relativas al propio proyecto y no depende de `C:\CATGIS`.

## Seguridad

Para entornos compartidos o accesibles desde otras máquinas se recomienda definir `CATSERVER_WEB_API_KEY` y desplegar detrás de un reverse proxy con TLS. El visor aplica cabeceras de seguridad (CSP, `X-Frame-Options`, `nosniff`), límite de tasa por IP y validación de capas publicadas antes de consultar la base.

`CATSERVER_WEB_TRUST_PROXY` debe activarse **sólo** si el visor está detrás de un reverse proxy propio: habilita el uso de `X-Forwarded-For` para el límite de tasa. Si está desactivado (valor por defecto), esa cabecera se ignora.

No publicar directamente archivos `.env`, dumps PostgreSQL ni credenciales de CATSERVER.

Ver también `../SECURITY.md`.

## Desarrollo

```bat
npm ci
npm run lint
npm test
```

`npm test` ejecuta una suite real con el runner integrado de Node.js (`node --test`) sobre `test/server.test.js`: cubre configuración, validación de parámetros, catálogo, capas, búsqueda, límites, API key, rate limit, archivos estáticos y rutas de error. Lint y tests forman parte del CI del repositorio.
