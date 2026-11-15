const { test } = require('node:test');
const assert = require('node:assert/strict');
const http = require('node:http');
const path = require('node:path');
const os = require('node:os');
const fs = require('node:fs');

const {
  createApp,
  createServer,
  loadConfig,
  parseEnvContents,
  parseTrustProxy,
  parseFilterList,
  parseBbox,
  layerLimit,
  searchLimit,
  quoteIdent,
  resolveStaticPath,
  validatePort,
  SECURITY_HEADERS,
  DEFAULT_CONFIG
} = require('../server.js');

const silentLogger = { error() {}, warn() {}, log() {} };

function createFakePool(handler) {
  const calls = [];
  const pool = {
    calls,
    query: async (sql, params) => {
      calls.push({ sql, params });
      return handler(sql, params);
    }
  };
  return pool;
}

function payloadRows(payload) {
  return { rows: [{ payload }] };
}

function startServer(app) {
  const server = createServer(app);
  return new Promise((resolve) => {
    server.listen(0, '127.0.0.1', () => resolve(server));
  });
}

function closeServer(server) {
  return new Promise((resolve) => server.close(resolve));
}

function request(server, options = {}) {
  const address = server.address();
  return new Promise((resolve, reject) => {
    const req = http.request({
      host: '127.0.0.1',
      port: address.port,
      path: options.path || '/',
      method: options.method || 'GET',
      headers: options.headers || {},
      agent: false
    }, (res) => {
      const chunks = [];
      res.on('data', (chunk) => chunks.push(chunk));
      res.on('end', () => {
        const raw = Buffer.concat(chunks).toString('utf8');
        let json = null;
        if (raw) {
          try {
            json = JSON.parse(raw);
          } catch {
            json = null;
          }
        }
        resolve({ status: res.statusCode, headers: res.headers, body: raw, json });
      });
    });
    req.on('error', reject);
    req.end();
  });
}

async function withApp(app, fn) {
  const server = await startServer(app);
  try {
    await fn(server);
  } finally {
    await closeServer(server);
  }
}

function buildCatalogPool(overrides = {}) {
  return createFakePool((sql, params) => {
    if (overrides.handler) {
      return overrides.handler(sql, params);
    }
    if (sql.includes('v_catalogo_capas') && sql.includes('json_agg(row_to_json(t))')) {
      return payloadRows([{ schema_name: 'portal_web', table_name: 'barrios' }]);
    }
    if (sql.includes('v_catserver_layers_tematicas') && sql.includes('SELECT DISTINCT')) {
      return payloadRows([{ schema_name: 'portal_web', table_name: 'barrios' }]);
    }
    return payloadRows([]);
  });
}

test('parseEnvContents ignora comentarios, lineas vacias y comillas', () => {
  const parsed = parseEnvContents([
    '# comentario',
    '',
    'CATSERVER_WEB_PORT=3090',
    'CATSERVER_DB_PASSWORD="secreto con espacios"',
    'SIN_VALOR=',
    'linea sin igual'
  ].join('\n'));

  assert.equal(parsed.CATSERVER_WEB_PORT, '3090');
  assert.equal(parsed.CATSERVER_DB_PASSWORD, 'secreto con espacios');
  assert.equal(parsed.SIN_VALOR, '');
  assert.equal(Object.prototype.hasOwnProperty.call(parsed, 'linea sin igual'), false);
});

test('parseTrustProxy solo acepta valores afirmativos', () => {
  assert.equal(parseTrustProxy(undefined), false);
  assert.equal(parseTrustProxy(''), false);
  assert.equal(parseTrustProxy('false'), false);
  assert.equal(parseTrustProxy('1'), true);
  assert.equal(parseTrustProxy('true'), true);
  assert.equal(parseTrustProxy('SI'), true);
});

test('validatePort respeta rango y cae al valor por defecto', () => {
  assert.equal(validatePort('3080', 1), 3080);
  assert.equal(validatePort('0', 3080), 3080);
  assert.equal(validatePort('70000', 3080), 3080);
  assert.equal(validatePort('abc', 3080), 3080);
  assert.equal(validatePort(undefined, 3080), 3080);
});

test('loadConfig aplica variables de entorno sobre los valores por defecto', () => {
  const config = loadConfig({
    env: {
      CATSERVER_WEB_PORT: '3091',
      CATSERVER_DB_PORT: '5433',
      CATSERVER_DB_HOST: 'db.local',
      CATSERVER_DB_NAME: 'otra',
      CATSERVER_DB_USER: 'usuario',
      CATSERVER_DB_PASSWORD: 'clave'
    },
    configPath: path.join(os.tmpdir(), 'catserver-web-config-inexistente.json')
  });

  assert.equal(config.port, 3091);
  assert.equal(config.db.port, 5433);
  assert.equal(config.db.host, 'db.local');
  assert.equal(config.db.database, 'otra');
  assert.equal(config.db.user, 'usuario');
  assert.equal(config.db.password, 'clave');
});

test('loadConfig informa error claro si config/local.json es invalido', () => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'catserver-web-'));
  const configPath = path.join(directory, 'local.json');
  fs.writeFileSync(configPath, '{ invalido', 'utf8');

  try {
    assert.throws(
      () => loadConfig({ env: {}, configPath }),
      /No se pudo leer la configuracion local/
    );
  } finally {
    fs.rmSync(directory, { recursive: true, force: true });
  }
});

test('parseBbox normaliza el orden de las coordenadas', () => {
  const bbox = parseBbox('-67.5,-45.9,-67.4,-45.8');
  assert.deepEqual(bbox, { minx: -67.5, miny: -45.9, maxx: -67.4, maxy: -45.8 });

  const inverted = parseBbox('-67.4,-45.8,-67.5,-45.9');
  assert.deepEqual(inverted, { minx: -67.5, miny: -45.9, maxx: -67.4, maxy: -45.8 });

  assert.equal(parseBbox('1,2,3'), null);
  assert.equal(parseBbox('a,b,c,d'), null);
  assert.equal(parseBbox(''), null);
});

test('parseFilterList deduplica y limpia valores', () => {
  assert.deepEqual(parseFilterList('a, b ,a,,c'), ['a', 'b', 'c']);
  assert.deepEqual(parseFilterList(''), []);
  assert.deepEqual(parseFilterList(null), []);
});

test('layerLimit acota el limite pedido por el cliente', () => {
  assert.equal(layerLimit('portal_web', 'barrios', '99999'), 2500);
  assert.equal(layerLimit('portal_web', 'barrios', '1'), 50);
  assert.equal(layerLimit('portal_web', 'barrios', undefined), 800);
  assert.equal(layerLimit('ordenamiento_territorial', 'ambiente_pozos_petroleros', '99999'), 10000);
  assert.equal(layerLimit('ordenamiento_territorial', 'tierras_parcelas', '99999'), 1500);
});

test('searchLimit acota la cantidad de resultados', () => {
  assert.equal(searchLimit(undefined), 15);
  assert.equal(searchLimit('0'), 1);
  assert.equal(searchLimit('9999'), 50);
  assert.equal(searchLimit('abc'), 15);
});

test('quoteIdent escapa comillas dobles', () => {
  assert.equal(quoteIdent('barrios'), '"barrios"');
  assert.equal(quoteIdent('mal"nombre'), '"mal""nombre"');
});

test('resolveStaticPath bloquea traversal y acepta rutas internas', () => {
  const publicRoot = path.resolve(__dirname, '..', 'public');
  assert.equal(resolveStaticPath('/app.js'), path.join(publicRoot, 'app.js'));

  for (const attempt of [
    '/../server.js',
    '/..%2fserver.js',
    '/%2e%2e/server.js',
    '/../../package.json',
    '/..\\..\\server.js',
    '/%2e%2e%2f%2e%2e%2fserver.js'
  ]) {
    let resolved = null;
    try {
      resolved = resolveStaticPath(attempt);
    } catch (error) {
      assert.ok(error instanceof Error, attempt);
    }
    if (resolved !== null) {
      const relative = path.relative(publicRoot, resolved);
      assert.equal(relative.startsWith('..'), false, `${attempt} escapo de public: ${resolved}`);
      assert.equal(path.isAbsolute(relative), false, attempt);
    }
  }

  assert.throws(() => resolveStaticPath('/%'), /Ruta invalida/);
});

test('las cabeceras de seguridad cubren el worker blob de MapLibre', () => {
  const csp = SECURITY_HEADERS['Content-Security-Policy'];
  assert.match(csp, /worker-src 'self' blob:/);
  assert.match(csp, /frame-ancestors 'none'/);
  assert.equal(SECURITY_HEADERS['X-Content-Type-Options'], 'nosniff');
  assert.equal(SECURITY_HEADERS['X-Frame-Options'], 'DENY');
});

test('GET /api/health responde JSON con cabeceras de seguridad', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/health' });
    assert.equal(response.status, 200);
    assert.equal(response.json.ok, true);
    assert.equal(response.json.app, 'catserver-web');
    assert.equal(response.headers['x-content-type-options'], 'nosniff');
    assert.match(response.headers['content-security-policy'], /worker-src 'self' blob:/);
    assert.equal(response.headers['cache-control'], 'no-store');
  });
});

test('HEAD /api/health devuelve cabeceras sin cuerpo', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/health', method: 'HEAD' });
    assert.equal(response.status, 200);
    assert.equal(response.body, '');
    assert.ok(Number(response.headers['content-length']) > 0);
  });
});

test('un metodo no soportado responde 405 con Allow', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/health', method: 'POST' });
    assert.equal(response.status, 405);
    assert.equal(response.headers.allow, 'GET, HEAD');
  });
});

test('sirve index.html y app.js como estaticos', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const index = await request(server, { path: '/' });
    assert.equal(index.status, 200);
    assert.match(index.headers['content-type'], /text\/html/);
    assert.match(index.body, /app\.js/);
    assert.match(index.body, /<\/script>/);

    const script = await request(server, { path: '/app.js' });
    assert.equal(script.status, 200);
    assert.match(script.headers['content-type'], /javascript/);
  });
});

test('index.html cierra todas las etiquetas script', () => {
  const html = fs.readFileSync(path.join(__dirname, '..', 'public', 'index.html'), 'utf8');
  const openings = (html.match(/<script\b/g) || []).length;
  const closings = (html.match(/<\/script>/g) || []).length;
  assert.equal(openings, closings, 'Cada <script> debe tener su </script>');
});

test('no expone archivos fuera de public ni rompe con traversal', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    for (const target of ['/../server.js', '/..%2fserver.js', '/%2e%2e/package.json']) {
      const response = await request(server, { path: target });
      assert.ok([400, 404].includes(response.status), `${target} devolvio ${response.status}`);
      assert.equal(response.body.includes('createApp'), false);
    }
  });
});

test('exige API key cuando esta configurada', async () => {
  const app = createApp({
    pool: createFakePool(() => payloadRows([])),
    logger: silentLogger,
    apiKey: 'clave-secreta'
  });
  await withApp(app, async (server) => {
    const missing = await request(server, { path: '/api/health' });
    assert.equal(missing.status, 401);

    const wrong = await request(server, { path: '/api/health', headers: { 'x-api-key': 'otra' } });
    assert.equal(wrong.status, 401);

    const ok = await request(server, { path: '/api/health', headers: { 'x-api-key': 'clave-secreta' } });
    assert.equal(ok.status, 200);
  });
});

test('aplica rate limit por IP', async () => {
  const app = createApp({
    pool: createFakePool(() => payloadRows([])),
    logger: silentLogger,
    rateLimit: { maxRequests: 2, windowMs: 60000 }
  });
  await withApp(app, async (server) => {
    assert.equal((await request(server, { path: '/api/health' })).status, 200);
    assert.equal((await request(server, { path: '/api/health' })).status, 200);
    const limited = await request(server, { path: '/api/health' });
    assert.equal(limited.status, 429);
    assert.equal(limited.headers['retry-after'], '60');
  });
});

test('ignora X-Forwarded-For si no se confia en el proxy', async () => {
  const app = createApp({
    pool: createFakePool(() => payloadRows([])),
    logger: silentLogger,
    rateLimit: { maxRequests: 1, windowMs: 60000 }
  });
  await withApp(app, async (server) => {
    const first = await request(server, {
      path: '/api/health',
      headers: { 'x-forwarded-for': '10.0.0.1' }
    });
    assert.equal(first.status, 200);

    const second = await request(server, {
      path: '/api/health',
      headers: { 'x-forwarded-for': '10.0.0.2' }
    });
    assert.equal(second.status, 429);
  });
});

test('el catalogo se cachea y respeta el modo pedido', async () => {
  const pool = buildCatalogPool();
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const first = await request(server, { path: '/api/catalog?mode=municipal' });
    assert.equal(first.status, 200);
    assert.deepEqual(first.json, [{ schema_name: 'portal_web', table_name: 'barrios' }]);

    const second = await request(server, { path: '/api/catalog?mode=municipal' });
    assert.equal(second.status, 200);
    assert.equal(pool.calls.length, 1);

    const thematic = await request(server, { path: '/api/catalog?mode=thematic' });
    assert.equal(thematic.status, 200);
    assert.ok(pool.calls[1].sql.includes('v_catserver_layers_tematicas'));
  });
});

test('rechaza capas no publicadas con 403', async () => {
  const pool = createFakePool((sql) => {
    if (sql.includes('SELECT DISTINCT')) {
      return payloadRows([]);
    }
    return payloadRows([]);
  });
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, {
      path: '/api/layer-data?schema=public&table=pg_catalog&limit=10'
    });
    assert.equal(response.status, 403);
    assert.match(response.json.error, /no esta publicada/);
  });
});

test('layer-data arma SQL parametrizado y acota el limite', async () => {
  const pool = createFakePool((sql) => {
    if (sql.includes('SELECT DISTINCT')) {
      return payloadRows([{
        schema_name: 'control_urbano_operativo',
        table_name: 'pozos_petroleros_ejido'
      }]);
    }
    return payloadRows({ type: 'FeatureCollection', features: [] });
  });

  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, {
      path: '/api/layer-data?schema=control_urbano_operativo&table=pozos_petroleros_ejido'
        + '&limit=99999&bbox=-67.5,-45.9,-67.4,-45.8&tipopozo=A,B&estpozo=Activo'
    });
    assert.equal(response.status, 200);
    assert.deepEqual(response.json, { type: 'FeatureCollection', features: [] });

    const layerCall = pool.calls[pool.calls.length - 1];
    assert.match(layerCall.sql, /ST_MakeEnvelope\(\$1, \$2, \$3, \$4, 4326\)/);
    assert.match(layerCall.sql, /ANY\(\$5::text\[\]\)/);
    assert.match(layerCall.sql, /ANY\(\$6::text\[\]\)/);
    assert.match(layerCall.sql, /LIMIT 10000/);
    assert.deepEqual(layerCall.params[0], -67.5);
    assert.deepEqual(layerCall.params[1], -45.9);
    assert.deepEqual(layerCall.params[4], ['A', 'B']);
    assert.deepEqual(layerCall.params[5], ['Activo']);
    assert.equal(layerCall.sql.includes("'A,B'"), false);
  });
});

test('layer-data exige schema y table', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/layer-data?schema=portal_web' });
    assert.equal(response.status, 400);
  });
});

test('feature valida el gid y responde 404 si no existe', async () => {
  const pool = createFakePool((sql) => {
    if (sql.includes('SELECT DISTINCT')) {
      return payloadRows([{ schema_name: 'portal_web', table_name: 'barrios' }]);
    }
    return { rows: [] };
  });
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const invalid = await request(server, {
      path: '/api/feature?schema=portal_web&table=barrios&gid=abc'
    });
    assert.equal(invalid.status, 400);

    const missing = await request(server, {
      path: '/api/feature?schema=portal_web&table=barrios&gid=42'
    });
    assert.equal(missing.status, 404);

    const notInteger = await request(server, {
      path: '/api/feature?schema=portal_web&table=barrios&gid=4.2'
    });
    assert.equal(notInteger.status, 400);
  });
});

test('search sin termino no consulta la base', async () => {
  const pool = createFakePool(() => payloadRows([]));
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/search?q=' });
    assert.equal(response.status, 200);
    assert.deepEqual(response.json, []);
    assert.equal(pool.calls.length, 0);
  });
});

test('search recorta terminos largos y acota el limite', async () => {
  const pool = createFakePool((sql) => {
    if (sql.includes('buscar_general')) {
      return payloadRows([]);
    }
    return payloadRows([]);
  });
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, {
      path: `/api/search?q=${'x'.repeat(500)}&limit=9999`
    });
    assert.equal(response.status, 200);
    const call = pool.calls[0];
    assert.equal(call.params[0].length, 200);
    assert.equal(call.params[1], 50);
  });
});

test('una ruta de API desconocida responde 404', async () => {
  const app = createApp({ pool: createFakePool(() => payloadRows([])), logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/desconocido' });
    assert.equal(response.status, 404);
  });
});

test('un error de base de datos se traduce en 500 generico', async () => {
  const pool = createFakePool(() => {
    throw new Error('detalle interno sensible');
  });
  const app = createApp({ pool, logger: silentLogger });
  await withApp(app, async (server) => {
    const response = await request(server, { path: '/api/catalog' });
    assert.equal(response.status, 500);
    assert.deepEqual(response.json, { error: 'Error interno del servidor.' });
    assert.equal(response.body.includes('detalle interno sensible'), false);
  });
});

test('los valores por defecto de configuracion siguen siendo compatibles', () => {
  assert.equal(DEFAULT_CONFIG.port, 3080);
  assert.equal(DEFAULT_CONFIG.db.port, 5432);
  assert.equal(DEFAULT_CONFIG.db.database, 'catserver');
});
