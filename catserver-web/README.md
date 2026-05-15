# CATSERVER Web

Visor web base para `CATSERVER`, sin `PHP` y sin frameworks pesados.

## Que trae

- servidor `Node.js` liviano
- frontend `HTML/CSS/JS`
- mapa web con `MapLibre`
- catalogo municipal y catalogo tematico
- busqueda de barrios, parcelas y zonificacion
- lectura directa desde `PostGIS`

## Ejecutar

```powershell
cd C:\CATGIS\catserver-web
npm start
```

Despues abrir:

- `http://localhost:3080`

## Configuracion

Archivo:

- `config/local.json`

Variables opcionales:

- `CATSERVER_WEB_PORT`
- `CATSERVER_DB_HOST`
- `CATSERVER_DB_PORT`
- `CATSERVER_DB_NAME`
- `CATSERVER_DB_USER`
- `CATSERVER_DB_PASSWORD`

## Necesitas algun programa para HTML?

No. Para este proyecto ya alcanza con:

- `Node.js`
- un navegador
- un editor de texto

Si queres un editor mas comodo, lo mas practico es `VS Code`, pero no es obligatorio.
