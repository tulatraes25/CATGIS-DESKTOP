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

Para entornos compartidos o accesibles desde otras máquinas se recomienda definir `CATSERVER_WEB_API_KEY` y desplegar detrás de un reverse proxy con TLS.

No publicar directamente archivos `.env`, dumps PostgreSQL ni credenciales de CATSERVER.

Ver también `../SECURITY.md`.

## Desarrollo

```bat
npm ci
npm run lint
npm test
```

Actualmente `npm test` es un placeholder y todavía no constituye una suite real de tests. El lint sí forma parte del CI del repositorio.
