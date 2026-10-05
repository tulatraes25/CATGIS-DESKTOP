const http = require('http');
const fs = require('fs');
const path = require('path');
const { URL } = require('url');
const { Pool } = require('pg');

const rootDir = __dirname;
const publicDir = path.join(rootDir, 'public');

const defaultConfig = {
  port: 3080,
  db: {
    host: 'localhost',
    port: 5432,
    database: 'catserver',
    user: 'catserver_viewer_user',
    password: ''
  }
};

const staticMime = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon'
};

const API_KEY = process.env.CATSERVER_WEB_API_KEY || '';

function checkApiKey(req, res) {
  if (!API_KEY) {
    return true;
  }
  const provided = (req.headers['x-api-key'] || '').trim();
  if (provided !== API_KEY) {
    json(res, 401, { error: 'Token de API invalido.' });
    return false;
  }
  return true;
}

const SECURITY_HEADERS = {
  'Content-Security-Policy': "default-src 'self'; script-src 'self' https://unpkg.com; style-src 'self' https://unpkg.com; img-src 'self' data: https://tile.openstreetmap.org; connect-src 'self'",
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=()'
};

const POZOS_LAYERS = [
  { schema: 'ordenamiento_territorial', table: 'ambiente_pozos_petroleros' },
  { schema: 'control_urbano_operativo', table: 'pozos_petroleros_ejido' }
];

const RATE_LIMIT_WINDOW_MS = 60000;
const RATE_LIMIT_MAX_REQUESTS = 120;
const rateLimitStore = new Map();

const cacheState = {
  catalog: {
    municipal: { expiresAt: 0, value: null },
    thematic: { expiresAt: 0, value: null }
  },
  allowedLayers: { expiresAt: 0, value: null },
  pozosFilterOptions: { expiresAt: 0, value: null }
};

const config = loadConfig();

if (!config.db.password) {
  console.error('CATSERVER_DB_PASSWORD no configurada. Definila como variable de entorno o en config/local.json.');
  process.exit(1);
}

const pool = new Pool({
  host: config.db.host,
  port: config.db.port,
  database: config.db.database,
  user: config.db.user,
  password: config.db.password,
  max: 8,
  idleTimeoutMillis: 15000
});

function loadConfig() {
  const configPath = path.join(rootDir, 'config', 'local.json');
  let fileConfig = {};
  if (fs.existsSync(configPath)) {
    fileConfig = JSON.parse(fs.readFileSync(configPath, 'utf8'));
  }

  const merged = {
    ...defaultConfig,
    ...fileConfig,
    db: {
      ...defaultConfig.db,
      ...(fileConfig.db || {})
    }
  };

  if (process.env.CATSERVER_WEB_PORT) {
    merged.port = Number(process.env.CATSERVER_WEB_PORT) || merged.port;
  }
  merged.db.host = process.env.CATSERVER_DB_HOST || merged.db.host;
  merged.db.port = validatePort(process.env.CATSERVER_DB_PORT, merged.db.port);
  merged.db.database = process.env.CATSERVER_DB_NAME || merged.db.database;
  merged.db.user = process.env.CATSERVER_DB_USER || merged.db.user;
  merged.db.password = process.env.CATSERVER_DB_PASSWORD || merged.db.password;

  return merged;
}

function validatePort(raw, fallback) {
  if (raw === undefined || raw === null || raw === '') {
    return fallback;
  }
  const port = Number(raw);
  if (Number.isInteger(port) && port >= 1 && port <= 65535) {
    return port;
  }
  return fallback;
}

function json(res, status, payload) {
  const headers = { ...SECURITY_HEADERS, 'Content-Type': 'application/json; charset=utf-8' };
  res.writeHead(status, headers);
  res.end(JSON.stringify(payload));
}

function quoteIdent(value) {
  return `"${String(value).replace(/"/g, '""')}"`;
}

function qualifiedName(schema, table) {
  return `${quoteIdent(schema)}.${quoteIdent(table)}`;
}

function isPozosLayer(schema, table) {
  return POZOS_LAYERS.some((layer) => layer.schema === schema && layer.table === table);
}

function parseFilterList(raw) {
  if (!raw) {
    return [];
  }
  return Array.from(new Set(
    String(raw)
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
  ));
}

async function runJsonQuery(sql, params, fallback) {
  const result = await pool.query(sql, params);
  if (!result.rows.length) {
    return fallback;
  }

  const firstRow = result.rows[0];
  const firstKey = Object.keys(firstRow)[0];
  const payload = firstRow[firstKey];
  return payload === null || payload === undefined ? fallback : payload;
}

async function getCatalog(mode) {
  const resolvedMode = mode === 'thematic' ? 'thematic' : 'municipal';
  const cache = cacheState.catalog[resolvedMode];
  const now = Date.now();
  if (cache.value && cache.expiresAt > now) {
    return cache.value;
  }

  const sql = resolvedMode === 'thematic'
    ? `
      SELECT COALESCE(json_agg(row_to_json(t)), '[]'::json) AS payload
      FROM (
        SELECT
          load_order,
          display_name,
          schema_name,
          table_name,
          theme AS secretariat,
          ''::text AS subsecretariat,
          geometry_type,
          crs_code,
          load_default,
          writable,
          notes
        FROM public.v_catserver_layers_tematicas
        ORDER BY load_order, schema_name, table_name
      ) t;
    `
    : `
      SELECT COALESCE(json_agg(row_to_json(t)), '[]'::json) AS payload
      FROM (
        SELECT
          load_order,
          display_name,
          schema_name,
          table_name,
          secretariat,
          subsecretariat,
          geometry_type,
          crs_code,
          load_default,
          writable,
          notes
        FROM portal_web.v_catalogo_capas
        ORDER BY load_order, schema_name, table_name
      ) t;
    `;

  const value = await runJsonQuery(sql, [], []);
  cache.value = value;
  cache.expiresAt = now + 15000;
  return value;
}

async function getAllowedLayers() {
  const now = Date.now();
  if (cacheState.allowedLayers.value && cacheState.allowedLayers.expiresAt > now) {
    return cacheState.allowedLayers.value;
  }

  const sql = `
    SELECT COALESCE(json_agg(row_to_json(t)), '[]'::json) AS payload
    FROM (
      SELECT DISTINCT schema_name, table_name
      FROM (
        SELECT schema_name, table_name FROM portal_web.v_catalogo_capas
        UNION ALL
        SELECT schema_name, table_name FROM public.v_catserver_layers_tematicas
      ) x
      ORDER BY schema_name, table_name
    ) t;
  `;

  const value = await runJsonQuery(sql, [], []);
  cacheState.allowedLayers.value = value;
  cacheState.allowedLayers.expiresAt = now + 15000;
  return value;
}

async function isAllowedLayer(schema, table) {
  const layers = await getAllowedLayers();
  return layers.some((entry) => entry.schema_name === schema && entry.table_name === table);
}

function parseBbox(raw) {
  if (!raw) {
    return null;
  }
  const parts = String(raw).split(',').map(Number);
  if (parts.length !== 4 || parts.some((value) => !Number.isFinite(value))) {
    return null;
  }
  return {
    minx: parts[0],
    miny: parts[1],
    maxx: parts[2],
    maxy: parts[3]
  };
}

function layerLimit(schema, table, requestedLimit) {
  const upperBound = isPozosLayer(schema, table) ? 10000 : 2500;
  const safeRequested = Math.max(50, Math.min(Number(requestedLimit) || 800, upperBound));
  if (schema === 'ordenamiento_territorial' && table === 'tierras_parcelas') {
    return Math.min(safeRequested, 1500);
  }
  return safeRequested;
}

function searchLimit(raw) {
  return Math.max(1, Math.min(Number(raw) || 15, 50));
}

async function getPozosFilterOptions() {
  const now = Date.now();
  if (cacheState.pozosFilterOptions.value && cacheState.pozosFilterOptions.expiresAt > now) {
    return cacheState.pozosFilterOptions.value;
  }

  const sql = `
    SELECT json_build_object(
      'tipopozo',
      (
        SELECT COALESCE(json_agg(row_to_json(t) ORDER BY t.count DESC, t.value), '[]'::json)
        FROM (
          SELECT
            tipopozo AS value,
            tipopozo AS label,
            count(*)::integer AS count
          FROM ordenamiento_territorial.ambiente_pozos_petroleros
          WHERE nullif(trim(tipopozo), '') IS NOT NULL
          GROUP BY tipopozo
        ) t
      ),
      'estpozo',
      (
        SELECT COALESCE(json_agg(row_to_json(t) ORDER BY t.count DESC, t.value), '[]'::json)
        FROM (
          SELECT
            estpozo AS value,
            estpozo AS label,
            count(*)::integer AS count
          FROM ordenamiento_territorial.ambiente_pozos_petroleros
          WHERE nullif(trim(estpozo), '') IS NOT NULL
          GROUP BY estpozo
        ) t
      ),
      'area',
      (
        SELECT COALESCE(json_agg(row_to_json(t) ORDER BY t.count DESC, t.value), '[]'::json)
        FROM (
          SELECT
            area AS value,
            area AS label,
            count(*)::integer AS count
          FROM ordenamiento_territorial.ambiente_pozos_petroleros
          WHERE nullif(trim(area), '') IS NOT NULL
          GROUP BY area
        ) t
      ),
      'yacimiento',
      (
        SELECT COALESCE(json_agg(row_to_json(t) ORDER BY t.count DESC, t.value), '[]'::json)
        FROM (
          SELECT
            yacimiento AS value,
            yacimiento AS label,
            count(*)::integer AS count
          FROM ordenamiento_territorial.ambiente_pozos_petroleros
          WHERE nullif(trim(yacimiento), '') IS NOT NULL
          GROUP BY yacimiento
        ) t
      )
    ) AS payload;
  `;

  const value = await runJsonQuery(sql, [], { tipopozo: [], estpozo: [], area: [], yacimiento: [] });
  cacheState.pozosFilterOptions.value = value;
  cacheState.pozosFilterOptions.expiresAt = now + 30000;
  return value;
}

async function getLayerGeoJson(schema, table, bbox, requestedLimit, filters = {}) {
  if (!(await isAllowedLayer(schema, table))) {
    throw new Error('La capa solicitada no esta publicada para CATSERVER Web.');
  }

  const limit = layerLimit(schema, table, requestedLimit);
  const params = [];
  const whereClauses = [];

  if (bbox) {
    params.push(bbox.minx, bbox.miny, bbox.maxx, bbox.maxy);
    whereClauses.push('src.geom && ST_MakeEnvelope($1, $2, $3, $4, 4326)');
  }

  if (isPozosLayer(schema, table)) {
    if (filters.tipopozo && filters.tipopozo.length) {
      params.push(filters.tipopozo);
      whereClauses.push(`coalesce(src.tipopozo, '') = ANY($${params.length}::text[])`);
    }
    if (filters.estpozo && filters.estpozo.length) {
      params.push(filters.estpozo);
      whereClauses.push(`coalesce(src.estpozo, '') = ANY($${params.length}::text[])`);
    }
    if (filters.area && filters.area.length) {
      params.push(filters.area);
      whereClauses.push(`coalesce(src.area, '') = ANY($${params.length}::text[])`);
    }
    if (filters.yacimiento && filters.yacimiento.length) {
      params.push(filters.yacimiento);
      whereClauses.push(`coalesce(src.yacimiento, '') = ANY($${params.length}::text[])`);
    }
  }

  const whereClause = whereClauses.length ? `WHERE ${whereClauses.join(' AND ')}` : '';

  const sql = `
    SELECT json_build_object(
      'type', 'FeatureCollection',
      'features', COALESCE(json_agg(feature), '[]'::json)
    ) AS payload
    FROM (
      SELECT json_build_object(
        'type', 'Feature',
        'id', f.gid,
        'geometry', ST_AsGeoJSON(f.geom, 6)::json,
        'properties', to_jsonb(f) - 'geom'
      ) AS feature
      FROM (
        SELECT *
        FROM ${qualifiedName(schema, table)} AS src
        ${whereClause}
        ORDER BY src.gid
        LIMIT ${limit}
      ) AS f
    ) AS features;
  `;

  return runJsonQuery(sql, params, { type: 'FeatureCollection', features: [] });
}

async function getFeatureById(schema, table, gid) {
  if (!(await isAllowedLayer(schema, table))) {
    throw new Error('La capa solicitada no esta publicada para CATSERVER Web.');
  }

  const numericId = Number(gid);
  if (!Number.isFinite(numericId)) {
    throw new Error('Identificador invalido.');
  }

  const sql = `
    SELECT json_build_object(
      'type', 'Feature',
      'id', f.gid,
      'geometry', ST_AsGeoJSON(f.geom, 6)::json,
      'properties', to_jsonb(f) - 'geom'
    ) AS payload
    FROM ${qualifiedName(schema, table)} AS f
    WHERE f.gid = $1
    LIMIT 1;
  `;

  return runJsonQuery(sql, [numericId], null);
}

async function getSearchResults(q, limit) {
  const sql = `
    SELECT COALESCE(json_agg(row_to_json(t)), '[]'::json) AS payload
    FROM (
      SELECT *
      FROM portal_web.buscar_general($1, $2)
    ) t;
  `;
  return runJsonQuery(sql, [q, searchLimit(limit)], []);
}

function serveFile(filePath, res) {
  if (!fs.existsSync(filePath)) {
    json(res, 404, { error: 'No encontrado' });
    return;
  }
  const ext = path.extname(filePath).toLowerCase();
  const mime = staticMime[ext] || 'application/octet-stream';
  const headers = { ...SECURITY_HEADERS, 'Content-Type': mime };
  res.writeHead(200, headers);
  fs.createReadStream(filePath).pipe(res);
}

function checkRateLimit(clientIp) {
  const now = Date.now();
  let entry = rateLimitStore.get(clientIp);

  if (!entry || (now - entry.windowStart) > RATE_LIMIT_WINDOW_MS) {
    entry = { windowStart: now, count: 0 };
    rateLimitStore.set(clientIp, entry);
  }

  entry.count++;

  if (rateLimitStore.size > 10000) {
    const cutoff = now - RATE_LIMIT_WINDOW_MS * 2;
    rateLimitStore.forEach((value, key) => {
      if ((now - value.windowStart) > cutoff) {
        rateLimitStore.delete(key);
      }
    });
  }

  return entry.count <= RATE_LIMIT_MAX_REQUESTS;
}

function getClientIp(req) {
  return (req.headers['x-forwarded-for'] || '').split(',')[0].trim() || req.socket.remoteAddress || '127.0.0.1';
}

async function handleApi(res, urlObj) {
  if (urlObj.pathname === '/api/health') {
    json(res, 200, {
      ok: true,
      app: 'catserver-web',
      modeDefault: 'municipal'
    });
    return;
  }

  if (urlObj.pathname === '/api/catalog') {
    const mode = urlObj.searchParams.get('mode') || 'municipal';
    json(res, 200, await getCatalog(mode));
    return;
  }

  if (urlObj.pathname === '/api/search') {
    const q = (urlObj.searchParams.get('q') || '').trim();
    if (!q) {
      json(res, 200, []);
      return;
    }
    json(res, 200, await getSearchResults(q, urlObj.searchParams.get('limit')));
    return;
  }

  if (urlObj.pathname === '/api/pozos-filter-options') {
    json(res, 200, await getPozosFilterOptions());
    return;
  }

  if (urlObj.pathname === '/api/layer-data') {
    const schema = (urlObj.searchParams.get('schema') || '').trim();
    const table = (urlObj.searchParams.get('table') || '').trim();
    if (!schema || !table) {
      json(res, 400, { error: 'Faltan schema o table.' });
      return;
    }
    const bbox = parseBbox(urlObj.searchParams.get('bbox'));
    const filters = {
      tipopozo: parseFilterList(urlObj.searchParams.get('tipopozo')),
      estpozo: parseFilterList(urlObj.searchParams.get('estpozo')),
      area: parseFilterList(urlObj.searchParams.get('area')),
      yacimiento: parseFilterList(urlObj.searchParams.get('yacimiento'))
    };
    json(res, 200, await getLayerGeoJson(schema, table, bbox, urlObj.searchParams.get('limit'), filters));
    return;
  }

  if (urlObj.pathname === '/api/feature') {
    const schema = (urlObj.searchParams.get('schema') || '').trim();
    const table = (urlObj.searchParams.get('table') || '').trim();
    const gid = (urlObj.searchParams.get('gid') || '').trim();
    if (!schema || !table || !gid) {
      json(res, 400, { error: 'Faltan schema, table o gid.' });
      return;
    }
    const feature = await getFeatureById(schema, table, gid);
    if (!feature) {
      json(res, 404, { error: 'No encontrado' });
      return;
    }
    json(res, 200, feature);
    return;
  }

  json(res, 404, { error: 'No encontrado' });
}

const server = http.createServer(async (req, res) => {
  try {
    const clientIp = getClientIp(req);

    if (!checkRateLimit(clientIp)) {
      json(res, 429, { error: 'Demasiadas solicitudes. Reintenta en un minuto.' });
      return;
    }

    const urlObj = new URL(req.url, `http://${req.headers.host}`);

    if (urlObj.pathname.startsWith('/api/')) {
      if (!checkApiKey(req, res)) {
        return;
      }
      await handleApi(res, urlObj);
      return;
    }

    if (urlObj.pathname === '/' || urlObj.pathname === '/index.html') {
      serveFile(path.join(publicDir, 'index.html'), res);
      return;
    }

    const safePath = path.normalize(urlObj.pathname).replace(/^(\.\.[/\\])+/, '');
    serveFile(path.join(publicDir, safePath), res);
  } catch (error) {
    console.error('Error interno del servidor:', error.message);
    json(res, 500, { error: 'Error interno del servidor.' });
  }
});

async function shutdown() {
  server.close(() => {});
  await pool.end().catch(() => {});
}

process.on('SIGINT', async () => {
  await shutdown();
  process.exit(0);
});

process.on('SIGTERM', async () => {
  await shutdown();
  process.exit(0);
});

server.listen(config.port, () => {
  console.log(`CATSERVER Web escuchando en http://localhost:${config.port}`);
});
