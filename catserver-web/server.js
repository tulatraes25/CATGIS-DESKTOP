const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { URL } = require('url');
const { Pool } = require('pg');

const rootDir = __dirname;
const publicDir = path.join(rootDir, 'public');

const DEFAULT_CONFIG = {
  port: 3080,
  db: {
    host: 'localhost',
    port: 5432,
    database: 'catserver',
    user: 'catserver_viewer_user',
    password: ''
  }
};

const STATIC_MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon'
};

// MapLibre se sirve desde unpkg y crea su worker a partir de un blob local,
// por eso worker-src necesita blob: ademas de 'self'.
const SECURITY_HEADERS = {
  'Content-Security-Policy': [
    "default-src 'self'",
    "script-src 'self' https://unpkg.com",
    "style-src 'self' https://unpkg.com",
    "img-src 'self' data: blob: https://tile.openstreetmap.org https://unpkg.com",
    "connect-src 'self'",
    "worker-src 'self' blob:",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'"
  ].join('; '),
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=()'
};

const POZOS_LAYERS = [
  { schema: 'ordenamiento_territorial', table: 'ambiente_pozos_petroleros' },
  { schema: 'control_urbano_operativo', table: 'pozos_petroleros_ejido' }
];

const DEFAULT_RATE_LIMIT = {
  windowMs: 60000,
  maxRequests: 120,
  maxEntries: 10000
};

const CACHE_TTL_MS = {
  catalog: 15000,
  allowedLayers: 15000,
  pozosFilterOptions: 30000
};

const MAX_SEARCH_QUERY_LENGTH = 200;
const MAX_SEARCH_RESULTS = 50;

class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.name = 'HttpError';
    this.status = status;
  }
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

function parseEnvContents(contents) {
  const parsed = {};
  String(contents).split(/\r?\n/).forEach((line) => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) {
      return;
    }
    const separatorIndex = trimmed.indexOf('=');
    if (separatorIndex <= 0) {
      return;
    }
    const key = trimmed.slice(0, separatorIndex).trim();
    if (!key) {
      return;
    }
    let value = trimmed.slice(separatorIndex + 1).trim();
    if (value.length >= 2) {
      const first = value[0];
      const last = value[value.length - 1];
      if ((first === '"' && last === '"') || (first === '\'' && last === '\'')) {
        value = value.slice(1, -1);
      }
    }
    parsed[key] = value;
  });
  return parsed;
}

function loadEnvFile(filePath, target = process.env) {
  if (!fs.existsSync(filePath)) {
    return {};
  }
  const parsed = parseEnvContents(fs.readFileSync(filePath, 'utf8'));
  Object.keys(parsed).forEach((key) => {
    if (target[key] === undefined || target[key] === '') {
      target[key] = parsed[key];
    }
  });
  return parsed;
}

function loadConfig(options = {}) {
  const env = options.env || process.env;
  const configPath = options.configPath || path.join(rootDir, 'config', 'local.json');

  let fileConfig = {};
  if (fs.existsSync(configPath)) {
    try {
      fileConfig = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    } catch (error) {
      throw new Error(`No se pudo leer la configuracion local en ${configPath}: ${error.message}`, { cause: error });
    }
    if (!fileConfig || typeof fileConfig !== 'object' || Array.isArray(fileConfig)) {
      throw new Error(`La configuracion local en ${configPath} debe ser un objeto JSON.`);
    }
  }

  const merged = {
    ...DEFAULT_CONFIG,
    ...fileConfig,
    db: {
      ...DEFAULT_CONFIG.db,
      ...(fileConfig.db || {})
    }
  };

  merged.port = validatePort(env.CATSERVER_WEB_PORT, merged.port);
  merged.db.host = env.CATSERVER_DB_HOST || merged.db.host;
  merged.db.port = validatePort(env.CATSERVER_DB_PORT, merged.db.port);
  merged.db.database = env.CATSERVER_DB_NAME || merged.db.database;
  merged.db.user = env.CATSERVER_DB_USER || merged.db.user;
  merged.db.password = env.CATSERVER_DB_PASSWORD || merged.db.password;

  return merged;
}

function parseTrustProxy(raw) {
  if (raw === undefined || raw === null) {
    return false;
  }
  const value = String(raw).trim().toLowerCase();
  return ['1', 'true', 'yes', 'si', 'sí'].includes(value);
}

function sendJson(res, status, payload, options = {}) {
  const method = options.method || 'GET';
  const body = Buffer.from(JSON.stringify(payload), 'utf8');
  const headers = {
    ...SECURITY_HEADERS,
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': String(body.length),
    'Cache-Control': 'no-store',
    ...(options.headers || {})
  };
  res.writeHead(status, headers);
  if (method === 'HEAD') {
    res.end();
    return;
  }
  res.end(body);
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

function parseBbox(raw) {
  if (!raw) {
    return null;
  }
  const parts = String(raw).split(',').map(Number);
  if (parts.length !== 4 || parts.some((value) => !Number.isFinite(value))) {
    return null;
  }
  return {
    minx: Math.min(parts[0], parts[2]),
    miny: Math.min(parts[1], parts[3]),
    maxx: Math.max(parts[0], parts[2]),
    maxy: Math.max(parts[1], parts[3])
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
  const requested = Number(raw);
  if (!Number.isFinite(requested)) {
    return 15;
  }
  return Math.max(1, Math.min(Math.trunc(requested), MAX_SEARCH_RESULTS));
}

function timingSafeEquals(provided, expected) {
  const providedDigest = crypto.createHash('sha256').update(String(provided), 'utf8').digest();
  const expectedDigest = crypto.createHash('sha256').update(String(expected), 'utf8').digest();
  return crypto.timingSafeEqual(providedDigest, expectedDigest);
}

function resolveStaticPath(rawPath) {
  let decoded;
  try {
    decoded = decodeURIComponent(rawPath);
  } catch {
    throw new HttpError(400, 'Ruta invalida.');
  }
  if (decoded.includes('\0')) {
    throw new HttpError(400, 'Ruta invalida.');
  }
  const normalized = path.posix.normalize(decoded.startsWith('/') ? decoded : `/${decoded}`);
  const candidate = path.resolve(publicDir, `.${normalized}`);
  const relative = path.relative(publicDir, candidate);
  if (!relative || relative.startsWith('..') || path.isAbsolute(relative)) {
    throw new HttpError(404, 'No encontrado');
  }
  return candidate;
}

function createApp(options = {}) {
  const pool = options.pool;
  if (!pool || typeof pool.query !== 'function') {
    throw new Error('createApp requiere un pool con query(sql, params).');
  }
  const logger = options.logger || console;
  const config = options.config || DEFAULT_CONFIG;
  const apiKey = options.apiKey === undefined ? '' : String(options.apiKey);
  const trustProxy = Boolean(options.trustProxy);
  const rateLimit = { ...DEFAULT_RATE_LIMIT, ...(options.rateLimit || {}) };

  const rateLimitStore = new Map();
  const cacheState = {
    catalog: {
      municipal: { expiresAt: 0, value: null },
      thematic: { expiresAt: 0, value: null }
    },
    allowedLayers: { expiresAt: 0, value: null },
    pozosFilterOptions: { expiresAt: 0, value: null }
  };

  function checkApiKey(req, res, method) {
    if (!apiKey) {
      return true;
    }
    const provided = String(req.headers['x-api-key'] || '').trim();
    if (!provided || !timingSafeEquals(provided, apiKey)) {
      sendJson(res, 401, { error: 'Token de API invalido.' }, { method });
      return false;
    }
    return true;
  }

  async function runJsonQuery(sql, params, fallback) {
    const result = await pool.query(sql, params);
    if (!result.rows || !result.rows.length) {
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
    cache.expiresAt = now + CACHE_TTL_MS.catalog;
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
    cacheState.allowedLayers.expiresAt = now + CACHE_TTL_MS.allowedLayers;
    return value;
  }

  async function isAllowedLayer(schema, table) {
    const layers = await getAllowedLayers();
    return Array.isArray(layers) && layers.some(
      (entry) => entry.schema_name === schema && entry.table_name === table
    );
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
    cacheState.pozosFilterOptions.expiresAt = now + CACHE_TTL_MS.pozosFilterOptions;
    return value;
  }

  async function getLayerGeoJson(schema, table, bbox, requestedLimit, filters = {}) {
    if (!(await isAllowedLayer(schema, table))) {
      throw new HttpError(403, 'La capa solicitada no esta publicada para CATSERVER Web.');
    }

    const limit = layerLimit(schema, table, requestedLimit);
    const params = [];
    const whereClauses = [];

    if (bbox) {
      params.push(bbox.minx, bbox.miny, bbox.maxx, bbox.maxy);
      whereClauses.push('src.geom && ST_MakeEnvelope($1, $2, $3, $4, 4326)');
    }

    if (isPozosLayer(schema, table)) {
      ['tipopozo', 'estpozo', 'area', 'yacimiento'].forEach((filterName) => {
        const values = filters[filterName];
        if (values && values.length) {
          params.push(values);
          whereClauses.push(`coalesce(src.${filterName}, '') = ANY($${params.length}::text[])`);
        }
      });
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
      throw new HttpError(403, 'La capa solicitada no esta publicada para CATSERVER Web.');
    }

    const numericId = Number(gid);
    if (!Number.isInteger(numericId)) {
      throw new HttpError(400, 'Identificador invalido.');
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

  function checkRateLimit(clientIp) {
    const now = Date.now();
    let entry = rateLimitStore.get(clientIp);

    if (!entry || (now - entry.windowStart) > rateLimit.windowMs) {
      entry = { windowStart: now, count: 0 };
      rateLimitStore.set(clientIp, entry);
    }

    entry.count += 1;

    if (rateLimitStore.size > rateLimit.maxEntries) {
      const cutoff = now - rateLimit.windowMs * 2;
      rateLimitStore.forEach((value, key) => {
        if ((now - value.windowStart) > cutoff) {
          rateLimitStore.delete(key);
        }
      });
    }

    return entry.count <= rateLimit.maxRequests;
  }

  function getClientIp(req) {
    if (trustProxy) {
      const forwarded = String(req.headers['x-forwarded-for'] || '').split(',')[0].trim();
      if (forwarded) {
        return forwarded;
      }
    }
    return (req.socket && req.socket.remoteAddress) || '127.0.0.1';
  }

  async function serveFile(res, filePath, method) {
    let stats;
    try {
      stats = await fs.promises.stat(filePath);
    } catch {
      throw new HttpError(404, 'No encontrado');
    }
    if (!stats.isFile()) {
      throw new HttpError(404, 'No encontrado');
    }

    const ext = path.extname(filePath).toLowerCase();
    const mime = STATIC_MIME[ext] || 'application/octet-stream';
    const headers = {
      ...SECURITY_HEADERS,
      'Content-Type': mime,
      'Content-Length': String(stats.size),
      'Cache-Control': 'no-cache'
    };

    res.writeHead(200, headers);
    if (method === 'HEAD') {
      res.end();
      return;
    }

    await new Promise((resolve, reject) => {
      const stream = fs.createReadStream(filePath);
      stream.on('error', reject);
      res.on('close', resolve);
      stream.pipe(res);
      stream.on('end', resolve);
    });
  }

  async function handleApi(req, res, urlObj, method) {
    if (urlObj.pathname === '/api/health') {
      sendJson(res, 200, {
        ok: true,
        app: 'catserver-web',
        modeDefault: 'municipal'
      }, { method });
      return;
    }

    if (urlObj.pathname === '/api/catalog') {
      const mode = urlObj.searchParams.get('mode') || 'municipal';
      sendJson(res, 200, await getCatalog(mode), { method });
      return;
    }

    if (urlObj.pathname === '/api/search') {
      const q = (urlObj.searchParams.get('q') || '').trim().slice(0, MAX_SEARCH_QUERY_LENGTH);
      if (!q) {
        sendJson(res, 200, [], { method });
        return;
      }
      sendJson(res, 200, await getSearchResults(q, urlObj.searchParams.get('limit')), { method });
      return;
    }

    if (urlObj.pathname === '/api/pozos-filter-options') {
      sendJson(res, 200, await getPozosFilterOptions(), { method });
      return;
    }

    if (urlObj.pathname === '/api/layer-data') {
      const schema = (urlObj.searchParams.get('schema') || '').trim();
      const table = (urlObj.searchParams.get('table') || '').trim();
      if (!schema || !table) {
        sendJson(res, 400, { error: 'Faltan schema o table.' }, { method });
        return;
      }
      const bbox = parseBbox(urlObj.searchParams.get('bbox'));
      const filters = {
        tipopozo: parseFilterList(urlObj.searchParams.get('tipopozo')),
        estpozo: parseFilterList(urlObj.searchParams.get('estpozo')),
        area: parseFilterList(urlObj.searchParams.get('area')),
        yacimiento: parseFilterList(urlObj.searchParams.get('yacimiento'))
      };
      const payload = await getLayerGeoJson(
        schema,
        table,
        bbox,
        urlObj.searchParams.get('limit'),
        filters
      );
      sendJson(res, 200, payload, { method });
      return;
    }

    if (urlObj.pathname === '/api/feature') {
      const schema = (urlObj.searchParams.get('schema') || '').trim();
      const table = (urlObj.searchParams.get('table') || '').trim();
      const gid = (urlObj.searchParams.get('gid') || '').trim();
      if (!schema || !table || !gid) {
        sendJson(res, 400, { error: 'Faltan schema, table o gid.' }, { method });
        return;
      }
      const feature = await getFeatureById(schema, table, gid);
      if (!feature) {
        sendJson(res, 404, { error: 'No encontrado' }, { method });
        return;
      }
      sendJson(res, 200, feature, { method });
      return;
    }

    sendJson(res, 404, { error: 'No encontrado' }, { method });
  }

  async function handleRequest(req, res) {
    const method = (req.method || 'GET').toUpperCase();
    try {
      if (method !== 'GET' && method !== 'HEAD') {
        sendJson(res, 405, { error: 'Metodo no permitido.' }, {
          method,
          headers: { Allow: 'GET, HEAD' }
        });
        return;
      }

      const clientIp = getClientIp(req);
      if (!checkRateLimit(clientIp)) {
        sendJson(res, 429, { error: 'Demasiadas solicitudes. Reintenta en un minuto.' }, {
          method,
          headers: { 'Retry-After': String(Math.ceil(rateLimit.windowMs / 1000)) }
        });
        return;
      }

      let urlObj;
      try {
        urlObj = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
      } catch {
        throw new HttpError(400, 'Solicitud invalida.');
      }

      if (urlObj.pathname.startsWith('/api/')) {
        if (!checkApiKey(req, res, method)) {
          return;
        }
        await handleApi(req, res, urlObj, method);
        return;
      }

      if (urlObj.pathname === '/' || urlObj.pathname === '/index.html') {
        await serveFile(res, path.join(publicDir, 'index.html'), method);
        return;
      }

      await serveFile(res, resolveStaticPath(urlObj.pathname), method);
    } catch (error) {
      if (res.headersSent) {
        if (!res.writableEnded) {
          res.end();
        }
        return;
      }
      if (error instanceof HttpError) {
        sendJson(res, error.status, { error: error.message }, { method });
        return;
      }
      logger.error('Error interno del servidor:', error && error.message ? error.message : error);
      sendJson(res, 500, { error: 'Error interno del servidor.' }, { method });
    }
  }

  return {
    handleRequest,
    getCatalog,
    getAllowedLayers,
    getLayerGeoJson,
    getFeatureById,
    getSearchResults,
    getPozosFilterOptions,
    config
  };
}

function createServer(app) {
  const server = http.createServer(app.handleRequest);
  server.requestTimeout = 30000;
  server.headersTimeout = 35000;
  return server;
}

function start() {
  loadEnvFile(path.join(rootDir, '.env'));

  let config;
  try {
    config = loadConfig();
  } catch (error) {
    console.error(error.message);
    process.exit(1);
    return;
  }

  if (!config.db.password) {
    console.error('CATSERVER_DB_PASSWORD no configurada. Definila como variable de entorno o en .env / config/local.json.');
    process.exit(1);
    return;
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

  pool.on('error', (error) => {
    console.error('Error inesperado en el pool de PostgreSQL:', error.message);
  });

  const app = createApp({
    pool,
    config,
    apiKey: process.env.CATSERVER_WEB_API_KEY || '',
    trustProxy: parseTrustProxy(process.env.CATSERVER_WEB_TRUST_PROXY)
  });

  const server = createServer(app);

  server.on('error', (error) => {
    if (error && error.code === 'EADDRINUSE') {
      console.error(`El puerto ${config.port} ya esta en uso. Definí CATSERVER_WEB_PORT con otro valor.`);
    } else {
      console.error('Error del servidor HTTP:', error && error.message ? error.message : error);
    }
    process.exit(1);
  });

  server.listen(config.port, () => {
    console.log(`CATSERVER Web escuchando en http://localhost:${config.port}`);
  });

  let shuttingDown = false;
  async function shutdown(signal) {
    if (shuttingDown) {
      return;
    }
    shuttingDown = true;
    console.log(`Cerrando CATSERVER Web (${signal})...`);
    server.close();
    await pool.end().catch(() => {});
    process.exit(0);
  }

  process.on('SIGINT', () => { shutdown('SIGINT'); });
  process.on('SIGTERM', () => { shutdown('SIGTERM'); });

  return server;
}

if (require.main === module) {
  start();
}

module.exports = {
  createApp,
  createServer,
  start,
  loadConfig,
  loadEnvFile,
  parseEnvContents,
  parseTrustProxy,
  parseFilterList,
  parseBbox,
  layerLimit,
  searchLimit,
  quoteIdent,
  qualifiedName,
  resolveStaticPath,
  validatePort,
  HttpError,
  SECURITY_HEADERS,
  STATIC_MIME,
  DEFAULT_CONFIG
};
