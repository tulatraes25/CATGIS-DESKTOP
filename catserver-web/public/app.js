const DEFAULT_VIEW = {
    center: [-67.4964, -45.8587],
    zoom: 11.2,
    maxZoom: 18
};

const DEFAULT_BOUNDS = [
    [-67.675, -45.98],
    [-67.315, -45.735]
];

const MAP_STYLE = {
    version: 8,
    sources: {
        osm: {
            type: 'raster',
            tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
            tileSize: 256,
            attribution: '&copy; OpenStreetMap contributors'
        }
    },
    layers: [
        {
            id: 'osm-base',
            type: 'raster',
            source: 'osm'
        }
    ]
};

const POZOS_LAYER_KEYS = new Set([
    'ordenamiento_territorial.ambiente_pozos_petroleros',
    'control_urbano_operativo.pozos_petroleros_ejido'
]);

const POZOS_ICON_ID = 'pozos-oilwell-marker';
const POZOS_ICON_URL = '/oil-well-marker.svg';

const appState = {
    map: null,
    popup: null,
    mapReady: false,
    catalogMode: 'municipal',
    catalogCache: {
        municipal: null,
        thematic: null
    },
    layerMeta: new Map(),
    layerData: new Map(),
    visibleLayers: new Set(),
    interactiveLayers: new Set(),
    requestTokens: new Map(),
    requestSequence: 0,
    refreshTimer: null,
    sidebarCollapsed: false,
    pozosFilters: {
        tipopozo: new Set(),
        estpozo: new Set(),
        area: new Set(),
        yacimiento: new Set()
    },
    pozosFilterOptions: null,
    pozosVisibleCount: null,
    pozosIconReady: false
};

const dom = {};

document.addEventListener('DOMContentLoaded', init);

async function init() {
    cacheDom();
    bindUi();
    createMap();
    applySidebarState();
    updateVisibleSummary();
    updatePozosFiltersStatus();

    await Promise.all([
        loadPozosFilterOptions(),
        loadCatalog(appState.catalogMode)
    ]);
}

function cacheDom() {
    dom.appShell = document.querySelector('.app-shell');
    dom.sidebar = document.querySelector('.sidebar');
    dom.catalogMode = document.getElementById('catalogMode');
    dom.refreshLayersButton = document.getElementById('refreshLayersButton');
    dom.layersTree = document.getElementById('layersTree');
    dom.searchForm = document.getElementById('searchForm');
    dom.searchInput = document.getElementById('searchInput');
    dom.searchResults = document.getElementById('searchResults');
    dom.featureDetail = document.getElementById('featureDetail');
    dom.visibleSummary = document.getElementById('visibleSummary');
    dom.mapStatus = document.getElementById('mapStatus');
    dom.toggleSidebarButton = document.getElementById('toggleSidebarButton');
    dom.fitAllButton = document.getElementById('fitAllButton');
    dom.pozosFiltersCard = document.getElementById('pozosFiltersCard');
    dom.pozosFiltersStatus = document.getElementById('pozosFiltersStatus');
    dom.clearPozosFiltersButton = document.getElementById('clearPozosFiltersButton');
    dom.tipopozoOptions = document.getElementById('tipopozoOptions');
    dom.estpozoOptions = document.getElementById('estpozoOptions');
    dom.areaOptions = document.getElementById('areaOptions');
    dom.yacimientoOptions = document.getElementById('yacimientoOptions');
    dom.tabButtons = Array.from(document.querySelectorAll('.tab-button'));
    dom.tabPanels = Array.from(document.querySelectorAll('.tab-panel'));
}

function bindUi() {
    dom.catalogMode.addEventListener('change', async (event) => {
        const nextMode = event.target.value === 'thematic' ? 'thematic' : 'municipal';
        if (nextMode === appState.catalogMode) {
            return;
        }
        clearAllVisibleLayers();
        appState.catalogMode = nextMode;
        await loadCatalog(nextMode, { force: true });
    });

    dom.refreshLayersButton.addEventListener('click', async () => {
        await loadCatalog(appState.catalogMode, { force: true });
        scheduleVisibleLayerRefresh();
    });

    dom.fitAllButton.addEventListener('click', () => {
        fitVisibleLayers();
    });

    dom.toggleSidebarButton.addEventListener('click', () => {
        appState.sidebarCollapsed = !appState.sidebarCollapsed;
        try {
            window.localStorage.setItem('catserver-web-sidebar-collapsed', appState.sidebarCollapsed ? '1' : '0');
        } catch (error) {
            if (typeof DEBUG !== 'undefined' && DEBUG) { console.warn(error); }
        }
        applySidebarState();
    });

    dom.tabButtons.forEach((button) => {
        button.addEventListener('click', () => {
            setActiveTab(button.dataset.tab);
        });
    });

    dom.searchForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        await runSearch(dom.searchInput.value.trim());
    });

    dom.clearPozosFiltersButton.addEventListener('click', () => {
        clearPozosFilters();
    });

    try {
        appState.sidebarCollapsed = window.innerWidth > 980 && window.localStorage.getItem('catserver-web-sidebar-collapsed') === '1';
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.warn(error); }
    }
}

function createMap() {
    appState.map = new maplibregl.Map({
        container: 'map',
        style: MAP_STYLE,
        center: DEFAULT_VIEW.center,
        zoom: DEFAULT_VIEW.zoom,
        maxZoom: DEFAULT_VIEW.maxZoom
    });

    appState.map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-right');
    appState.popup = new maplibregl.Popup({
        closeButton: false,
        closeOnClick: true,
        offset: 18
    });

    appState.map.on('load', async () => {
        appState.mapReady = true;
        await ensurePozosIcon();
        setMapStatus('Listo');
    });

    appState.map.on('moveend', () => {
        scheduleVisibleLayerRefresh();
    });

    appState.map.on('zoomend', () => {
        scheduleVisibleLayerRefresh();
    });
}

async function ensurePozosIcon() {
    if (!appState.map) {
        return false;
    }
    if (appState.map.hasImage(POZOS_ICON_ID)) {
        appState.pozosIconReady = true;
        return true;
    }

    return new Promise((resolve) => {
        const image = new Image(64, 64);
        image.onload = () => {
            try {
                if (!appState.map.hasImage(POZOS_ICON_ID)) {
                    appState.map.addImage(POZOS_ICON_ID, image, { pixelRatio: 2 });
                }
                appState.pozosIconReady = true;
                resolve(true);
            } catch (error) {
                if (typeof DEBUG !== 'undefined' && DEBUG) { console.warn('No se pudo registrar el icono de pozos.', error); }
                appState.pozosIconReady = false;
                resolve(false);
            }
        };
        image.onerror = () => {
            appState.pozosIconReady = false;
            resolve(false);
        };
        image.src = POZOS_ICON_URL;
    });
}

function applySidebarState() {
    dom.appShell.classList.toggle('sidebar-collapsed', appState.sidebarCollapsed);
    dom.toggleSidebarButton.textContent = appState.sidebarCollapsed ? 'Mostrar panel' : 'Ocultar panel';

    if (appState.map) {
        window.setTimeout(() => {
            appState.map.resize();
        }, 180);
    }
}

function setActiveTab(tabName) {
    dom.tabButtons.forEach((button) => {
        button.classList.toggle('active', button.dataset.tab === tabName);
    });
    dom.tabPanels.forEach((panel) => {
        panel.classList.toggle('active', panel.dataset.panel === tabName);
    });
}

function setMapStatus(message) {
    dom.mapStatus.textContent = message;
}

function updateVisibleSummary() {
    const count = appState.visibleLayers.size;
    dom.visibleSummary.textContent = count === 1 ? '1 activa' : `${count} activas`;
}

function layerKey(layer) {
    return `${layer.schema_name}.${layer.table_name}`;
}

function isPozosLayerTarget(layerOrKey) {
    const key = typeof layerOrKey === 'string' ? layerOrKey : layerKey(layerOrKey);
    return POZOS_LAYER_KEYS.has(key);
}

function isPozosLayerVisible() {
    return Array.from(appState.visibleLayers).some((key) => isPozosLayerTarget(key));
}

function selectedFilterValues(filterName) {
    return Array.from(appState.pozosFilters[filterName] || []);
}

function activePozosFilterCount() {
    return ['tipopozo', 'estpozo', 'area', 'yacimiento']
        .map((filterName) => selectedFilterValues(filterName).length)
        .reduce((sum, count) => sum + count, 0);
}

function updatePozosFiltersStatus(message, isError = false) {
    dom.pozosFiltersCard.classList.toggle('is-error', Boolean(isError));

    if (message) {
        dom.pozosFiltersStatus.textContent = message;
        return;
    }

    const activeFilters = activePozosFilterCount();
    let text = 'Se aplican a la capa Pozos petroleros - ejido cuando la activas.';

    if (activeFilters > 0) {
        text = `${activeFilters} filtro(s) activo(s) sobre Pozos petroleros - ejido.`;
    } else if (isPozosLayerVisible()) {
        text = 'Mostrando todos los pozos de la capa activa.';
    }

    if (isPozosLayerVisible() && appState.pozosVisibleCount !== null) {
        text += ` ${appState.pozosVisibleCount} pozo(s) visibles.`;
    }

    dom.pozosFiltersStatus.textContent = text;
}

function clearPozosFilters() {
    ['tipopozo', 'estpozo', 'area', 'yacimiento'].forEach((filterName) => {
        appState.pozosFilters[filterName].clear();
    });
    appState.pozosVisibleCount = null;
    renderPozosFilterOptions();
    updatePozosFiltersStatus();
    if (isPozosLayerVisible()) {
        scheduleVisibleLayerRefresh();
    }
}

async function loadPozosFilterOptions() {
    try {
        const options = await fetchJson('/api/pozos-filter-options');
        appState.pozosFilterOptions = options;
        renderPozosFilterOptions();
        updatePozosFiltersStatus();
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.error(error); }
        appState.pozosFilterOptions = {
            tipopozo: [],
            estpozo: [],
            area: [],
            yacimiento: []
        };
        renderPozosFilterOptions('Sin datos cargados.');
        updatePozosFiltersStatus('No se pudieron cargar los filtros de pozos.', true);
    }
}

function renderPozosFilterOptions(emptyMessage) {
    renderFilterGroup(dom.tipopozoOptions, 'tipopozo', appState.pozosFilterOptions?.tipopozo || [], emptyMessage);
    renderFilterGroup(dom.estpozoOptions, 'estpozo', appState.pozosFilterOptions?.estpozo || [], emptyMessage);
    renderFilterGroup(dom.areaOptions, 'area', appState.pozosFilterOptions?.area || [], emptyMessage);
    renderFilterGroup(dom.yacimientoOptions, 'yacimiento', appState.pozosFilterOptions?.yacimiento || [], emptyMessage);
}

function renderFilterGroup(container, filterName, options, emptyMessage) {
    container.innerHTML = '';

    if (!options.length) {
        const paragraph = document.createElement('p');
        paragraph.className = 'filter-empty-state';
        paragraph.textContent = emptyMessage || 'Sin datos cargados.';
        container.appendChild(paragraph);
        return;
    }

    const fragment = document.createDocumentFragment();

    options.forEach((option) => {
        const label = document.createElement('label');
        label.className = 'filter-option';

        const input = document.createElement('input');
        input.type = 'checkbox';
        input.checked = appState.pozosFilters[filterName].has(option.value);
        input.addEventListener('change', () => {
            if (input.checked) {
                appState.pozosFilters[filterName].add(option.value);
            } else {
                appState.pozosFilters[filterName].delete(option.value);
            }
            appState.pozosVisibleCount = null;
            updatePozosFiltersStatus();
            if (isPozosLayerVisible()) {
                scheduleVisibleLayerRefresh();
            }
        });

        const text = document.createElement('span');
        text.textContent = `${option.label} (${option.count})`;

        label.appendChild(input);
        label.appendChild(text);
        fragment.appendChild(label);
    });

    container.appendChild(fragment);
}

async function loadCatalog(mode, options = {}) {
    const resolvedMode = mode === 'thematic' ? 'thematic' : 'municipal';
    const shouldForce = Boolean(options.force);

    if (!shouldForce && appState.catalogCache[resolvedMode]) {
        setCatalogEntries(appState.catalogCache[resolvedMode]);
        return;
    }

    setMapStatus('Cargando catalogo...');
    try {
        const entries = await fetchJson(`/api/catalog?mode=${resolvedMode}`);
        appState.catalogCache[resolvedMode] = entries;
        setCatalogEntries(entries);
        setMapStatus('Listo');
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.error(error); }
        dom.layersTree.innerHTML = '<p class="helper-text">No se pudo cargar el catalogo de capas.</p>';
        setMapStatus('Error');
    }
}

function setCatalogEntries(entries) {
    appState.layerMeta = new Map(entries.map((layer) => [layerKey(layer), layer]));
    renderLayersTree(entries);
    updateVisibleSummary();
}

function renderLayersTree(entries) {
    dom.layersTree.innerHTML = '';

    if (!entries.length) {
        dom.layersTree.innerHTML = '<p class="helper-text">No hay capas publicadas para este modo.</p>';
        return;
    }

    const grouped = new Map();

    entries.forEach((layer) => {
        const groupName = layer.secretariat || layer.schema_name || 'Sin grupo';
        const subgroupName = layer.subsecretariat || '';
        if (!grouped.has(groupName)) {
            grouped.set(groupName, new Map());
        }
        if (!grouped.get(groupName).has(subgroupName)) {
            grouped.get(groupName).set(subgroupName, []);
        }
        grouped.get(groupName).get(subgroupName).push(layer);
    });

    grouped.forEach((subgroups, groupName) => {
        const groupDetails = document.createElement('details');
        groupDetails.className = 'group-card';
        groupDetails.open = true;

        const groupSummary = document.createElement('summary');
        groupSummary.textContent = groupName;
        groupDetails.appendChild(groupSummary);

        const groupContent = document.createElement('div');
        groupContent.className = 'group-content';

        subgroups.forEach((layers, subgroupName) => {
            if (!subgroupName) {
                layers.forEach((layer) => {
                    groupContent.appendChild(buildLayerRow(layer));
                });
                return;
            }

            const subgroupDetails = document.createElement('details');
            subgroupDetails.className = 'subgroup-card';
            subgroupDetails.open = true;

            const subgroupSummary = document.createElement('summary');
            subgroupSummary.textContent = subgroupName;
            subgroupDetails.appendChild(subgroupSummary);

            const subgroupContent = document.createElement('div');
            subgroupContent.className = 'subgroup-content';
            layers.forEach((layer) => {
                subgroupContent.appendChild(buildLayerRow(layer));
            });

            subgroupDetails.appendChild(subgroupContent);
            groupContent.appendChild(subgroupDetails);
        });

        groupDetails.appendChild(groupContent);
        dom.layersTree.appendChild(groupDetails);
    });
}

function buildLayerRow(layer) {
    const row = document.createElement('label');
    row.className = 'layer-row';

    const input = document.createElement('input');
    input.type = 'checkbox';
    input.checked = appState.visibleLayers.has(layerKey(layer));
    input.addEventListener('change', async () => {
        await setLayerVisibility(layer, input.checked);
    });

    const info = document.createElement('div');

    const title = document.createElement('div');
    title.className = 'layer-title';
    title.textContent = layer.display_name;

    const meta = document.createElement('div');
    meta.className = 'layer-meta';

    const metaParts = [
        `${layer.schema_name}.${layer.table_name}`,
        layer.geometry_type
    ];
    if (layer.load_default) {
        metaParts.push('sugerida');
    }
    meta.textContent = metaParts.join(' | ');

    info.appendChild(title);
    info.appendChild(meta);

    const notes = document.createElement('div');
    notes.className = 'layer-meta';
    notes.textContent = layer.writable ? 'Editable' : 'Solo lectura';

    row.appendChild(input);
    row.appendChild(info);
    row.appendChild(notes);
    return row;
}

async function setLayerVisibility(layer, visible) {
    const key = layerKey(layer);

    if (visible) {
        appState.visibleLayers.add(key);
        await updateLayerData(layer);
    } else {
        appState.visibleLayers.delete(key);
        clearLayerFromMap(layer);
    }

    updateVisibleSummary();
    updatePozosFiltersStatus();
}

function clearAllVisibleLayers() {
    Array.from(appState.visibleLayers).forEach((key) => {
        const layer = appState.layerMeta.get(key);
        if (layer) {
            clearLayerFromMap(layer);
        }
    });
    appState.visibleLayers.clear();
    updateVisibleSummary();
    updatePozosFiltersStatus();
}

function nextRequestToken(key) {
    const token = `${Date.now()}-${++appState.requestSequence}`;
    appState.requestTokens.set(key, token);
    return token;
}

function scheduleVisibleLayerRefresh() {
    if (!appState.mapReady || !appState.visibleLayers.size) {
        return;
    }
    window.clearTimeout(appState.refreshTimer);
    appState.refreshTimer = window.setTimeout(() => {
        refreshVisibleLayers();
    }, 220);
}

async function refreshVisibleLayers() {
    if (!appState.mapReady) {
        return;
    }

    const layers = Array.from(appState.visibleLayers)
        .map((key) => appState.layerMeta.get(key))
        .filter(Boolean);

    if (!layers.length) {
        return;
    }

    setMapStatus('Actualizando...');
    await Promise.all(layers.map((layer) => updateLayerData(layer, { silentStatus: true })));
    setMapStatus('Listo');
}

function requestedLimitForLayer(layer) {
    const key = layerKey(layer);
    if (isPozosLayerTarget(key)) {
        return 10000;
    }
    if (key.includes('parcelas')) {
        return 1500;
    }
    if (key.includes('cad_barrios_rotulos') || key.includes('cad_barrios_control')) {
        return 2500;
    }
    return 2000;
}

function buildLayerDataUrl(layer) {
    const params = new URLSearchParams();
    params.set('schema', layer.schema_name);
    params.set('table', layer.table_name);
    params.set('limit', String(requestedLimitForLayer(layer)));

    if (appState.mapReady) {
        const bounds = appState.map.getBounds();
        params.set(
            'bbox',
            [
                bounds.getWest().toFixed(6),
                bounds.getSouth().toFixed(6),
                bounds.getEast().toFixed(6),
                bounds.getNorth().toFixed(6)
            ].join(',')
        );
    }

    if (isPozosLayerTarget(layer)) {
        ['tipopozo', 'estpozo', 'area', 'yacimiento'].forEach((filterName) => {
            const values = selectedFilterValues(filterName);
            if (values.length) {
                params.set(filterName, values.join(','));
            }
        });
    }

    return `/api/layer-data?${params.toString()}`;
}

async function updateLayerData(layer, options = {}) {
    if (!appState.mapReady) {
        return;
    }

    const key = layerKey(layer);
    const token = nextRequestToken(key);

    if (!options.silentStatus) {
        setMapStatus(`Cargando ${layer.display_name}...`);
    }

    try {
        if (isPozosLayerTarget(key)) {
            await ensurePozosIcon();
        }

        const geojson = await fetchJson(buildLayerDataUrl(layer));

        if (appState.requestTokens.get(key) !== token || !appState.visibleLayers.has(key)) {
            return;
        }

        appState.layerData.set(key, geojson);
        ensureMapLayer(layer, geojson);

        if (isPozosLayerTarget(key)) {
            appState.pozosVisibleCount = geojson.features.length;
            updatePozosFiltersStatus();
        }

        if (!options.silentStatus) {
            setMapStatus('Listo');
        }
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.error(error); }
        if (!options.silentStatus) {
            setMapStatus('Error');
        }
        if (isPozosLayerTarget(key)) {
            appState.pozosVisibleCount = 0;
            updatePozosFiltersStatus('No se pudieron actualizar los pozos con el filtro actual.', true);
        }
    }
}

function clearLayerFromMap(layer) {
    const key = layerKey(layer);
    nextRequestToken(key);
    const ids = getLayerIds(layer);

    [ids.fill, ids.outline, ids.line, ids.point].forEach((id) => {
        if (appState.map.getLayer(id)) {
            appState.map.removeLayer(id);
        }
    });

    if (appState.map.getSource(ids.source)) {
        appState.map.removeSource(ids.source);
    }

    appState.layerData.delete(key);

    if (isPozosLayerTarget(key)) {
        appState.pozosVisibleCount = 0;
        updatePozosFiltersStatus();
    }
}

function ensureMapLayer(layer, geojson) {
    const ids = getLayerIds(layer);
    const geometryKind = normalizeGeometryKind(layer.geometry_type);
    const style = layerStyle(layer);

    if (appState.map.getSource(ids.source)) {
        appState.map.getSource(ids.source).setData(geojson);
    } else {
        appState.map.addSource(ids.source, {
            type: 'geojson',
            data: geojson
        });
    }

    if (geometryKind === 'polygon') {
        ensurePolygonLayers(layer, ids, style);
        return;
    }

    if (geometryKind === 'line') {
        ensureLineLayer(layer, ids, style);
        return;
    }

    ensurePointLayer(layer, ids, style);
}

function ensurePolygonLayers(layer, ids, style) {
    if (!appState.map.getLayer(ids.fill)) {
        appState.map.addLayer({
            id: ids.fill,
            type: 'fill',
            source: ids.source,
            paint: {
                'fill-color': style.fillColor,
                'fill-opacity': style.fillOpacity
            }
        });
        attachLayerInteractions(layer, ids.fill);
    }

    if (!appState.map.getLayer(ids.outline)) {
        appState.map.addLayer({
            id: ids.outline,
            type: 'line',
            source: ids.source,
            paint: {
                'line-color': style.outlineColor,
                'line-opacity': style.lineOpacity ?? 1,
                'line-width': style.lineWidth,
                'line-dasharray': style.lineDasharray || [1, 0]
            }
        });
        attachLayerInteractions(layer, ids.outline);
    }
}

function ensureLineLayer(layer, ids, style) {
    if (!appState.map.getLayer(ids.line)) {
        appState.map.addLayer({
            id: ids.line,
            type: 'line',
            source: ids.source,
            paint: {
                'line-color': style.lineColor,
                'line-opacity': style.lineOpacity ?? 0.85,
                'line-width': style.lineWidth
            }
        });
        attachLayerInteractions(layer, ids.line);
    }
}

function ensurePointLayer(layer, ids, style) {
    const wantsIcon = Boolean(style.useSymbolIcon && appState.pozosIconReady);
    const existingLayer = appState.map.getLayer(ids.point);

    if (existingLayer && existingLayer.type !== (wantsIcon ? 'symbol' : 'circle')) {
        appState.map.removeLayer(ids.point);
        appState.interactiveLayers.delete(ids.point);
    }

    if (!appState.map.getLayer(ids.point)) {
        if (wantsIcon) {
            appState.map.addLayer({
                id: ids.point,
                type: 'symbol',
                source: ids.source,
                layout: {
                    'icon-image': style.iconImage || POZOS_ICON_ID,
                    'icon-size': style.iconSize || 0.52,
                    'icon-anchor': style.iconAnchor || 'bottom',
                    'icon-allow-overlap': true,
                    'icon-ignore-placement': true
                },
                paint: {
                    'icon-opacity': style.iconOpacity ?? 0.98
                }
            });
        } else {
            appState.map.addLayer({
                id: ids.point,
                type: 'circle',
                source: ids.source,
                paint: {
                    'circle-color': style.pointColor,
                    'circle-radius': style.pointRadius,
                    'circle-stroke-color': style.pointStrokeColor,
                    'circle-stroke-width': style.pointStrokeWidth,
                    'circle-opacity': style.pointOpacity ?? 0.95
                }
            });
        }
        attachLayerInteractions(layer, ids.point);
    }
}

function attachLayerInteractions(layer, layerId) {
    if (appState.interactiveLayers.has(layerId)) {
        return;
    }

    appState.map.on('click', layerId, async (event) => {
        const feature = event.features && event.features[0];
        if (!feature) {
            return;
        }
        showFeaturePopup(layer, feature, event.lngLat);
        await updateFeatureDetail(layer, feature.properties?.gid || feature.id, feature);
    });

    appState.map.on('mouseenter', layerId, () => {
        appState.map.getCanvas().style.cursor = 'pointer';
    });

    appState.map.on('mouseleave', layerId, () => {
        appState.map.getCanvas().style.cursor = '';
    });

    appState.interactiveLayers.add(layerId);
}

function getLayerIds(layer) {
    const safe = layerKey(layer).replace(/[^a-z0-9]+/gi, '-').replace(/^-+|-+$/g, '').toLowerCase();
    return {
        source: `src-${safe}`,
        fill: `fill-${safe}`,
        outline: `outline-${safe}`,
        line: `line-${safe}`,
        point: `point-${safe}`
    };
}

function normalizeGeometryKind(geometryType) {
    const value = String(geometryType || '').toUpperCase();
    if (value.includes('POLYGON')) {
        return 'polygon';
    }
    if (value.includes('LINE')) {
        return 'line';
    }
    return 'point';
}

function infrastructureLineColorExpression() {
    const field = ['coalesce', ['get', 'nombre'], ['get', 'name'], ''];
    return [
        'match',
        field,
        'LMT 13200 V - SCPL', '#b91c1c',
        'LMT 10400 V - SCPL', '#2563eb',
        'LMT 1100 V - SCPL', '#ea580c',
        'LMT 33000 V - SCPL', '#4338ca',
        'LMT 500 Kv', '#111827',
        'LMT 35 Kv', '#047857',
        'Lineas Electricas', '#7c3aed',
        'Franja 100 m I.P.A.', '#a16207',
        '#7b4f2f'
    ];
}

function layerStyle(layer) {
    const key = layerKey(layer);

    if (isPozosLayerTarget(key)) {
        return {
            useSymbolIcon: true,
            iconImage: POZOS_ICON_ID,
            iconSize: ['interpolate', ['linear'], ['zoom'], 10.5, 0.34, 12.5, 0.46, 15, 0.62, 17, 0.76],
            iconAnchor: 'bottom',
            iconOpacity: 0.98,
            pointColor: '#111827',
            pointRadius: 4.5,
            pointStrokeColor: '#f59e0b',
            pointStrokeWidth: 1.4
        };
    }

    if (key.includes('barrios') && normalizeGeometryKind(layer.geometry_type) === 'polygon') {
        return {
            fillColor: '#2f7a74',
            fillOpacity: 0.1,
            outlineColor: '#235a56',
            lineWidth: 1.35
        };
    }

    if (key.includes('parcelas')) {
        return {
            fillColor: '#64748b',
            fillOpacity: 0.03,
            outlineColor: '#475569',
            lineWidth: 0.7
        };
    }

    if (key.includes('circunscripcion') || key.includes('circunscripcion_sector') || key.includes('circsect')) {
        return {
            fillColor: '#64748b',
            fillOpacity: 0.025,
            outlineColor: '#334155',
            lineWidth: 1
        };
    }

    if (key.includes('zonificacion') || key.includes('zonif')) {
        return {
            fillColor: '#d16b2c',
            fillOpacity: 0.14,
            outlineColor: '#aa561f',
            lineWidth: 1.05
        };
    }

    if (key.includes('ejido')) {
        return {
            fillColor: '#0f172a',
            fillOpacity: 0.02,
            outlineColor: '#1f2937',
            lineWidth: 1.75,
            lineDasharray: [2, 2]
        };
    }

    if (key.includes('cad_barrios_lineas')) {
        return {
            lineColor: '#7c3f5d',
            lineOpacity: 0.82,
            lineWidth: ['interpolate', ['linear'], ['zoom'], 10, 0.45, 14, 1.1, 17, 1.55]
        };
    }

    if (key.includes('cad_barrios_rotulos')) {
        return {
            pointColor: '#7c3f5d',
            pointRadius: ['interpolate', ['linear'], ['zoom'], 11, 2.6, 15, 4.3],
            pointStrokeColor: '#fff7ed',
            pointStrokeWidth: 1
        };
    }

    if (key.includes('cad_barrios_control')) {
        return {
            pointColor: '#0f766e',
            pointRadius: ['interpolate', ['linear'], ['zoom'], 11, 2.8, 15, 4.6],
            pointStrokeColor: '#ffffff',
            pointStrokeWidth: 1.2
        };
    }

    if (key.includes('redes_servicios_publicos_lineas') || key === 'ingenieria.lineas') {
        return {
            lineColor: infrastructureLineColorExpression(),
            lineOpacity: 0.82,
            lineWidth: ['interpolate', ['linear'], ['zoom'], 10.5, 1.2, 14, 2.4, 17, 3.1]
        };
    }

    if (key.includes('reservorios_poligonos')) {
        return {
            fillColor: '#177d88',
            fillOpacity: 0.18,
            outlineColor: '#0f5660',
            lineWidth: 1.2
        };
    }

    if (key.includes('reservorios_barreras')) {
        return {
            fillColor: '#7a4b2a',
            fillOpacity: 0.15,
            outlineColor: '#5b371e',
            lineWidth: 1.1
        };
    }

    if (key.includes('reservorios_nombres')) {
        return {
            pointColor: '#177d88',
            pointRadius: 4,
            pointStrokeColor: '#ffffff',
            pointStrokeWidth: 1
        };
    }

    if (key.includes('drenaje_cuencas_principales')) {
        return {
            fillColor: '#2a6fb8',
            fillOpacity: 0.11,
            outlineColor: '#1f5a92',
            lineWidth: 1.15
        };
    }

    if (key.includes('drenaje_cuencas_regionales')) {
        return {
            fillColor: '#6aa7de',
            fillOpacity: 0.08,
            outlineColor: '#3d7fb2',
            lineWidth: 1
        };
    }

    if (key.includes('drenaje_lineas')) {
        return {
            lineColor: '#2563eb',
            lineOpacity: 0.9,
            lineWidth: ['interpolate', ['linear'], ['zoom'], 10.5, 1, 14, 2.1, 17, 2.8]
        };
    }

    if (key.includes('drenaje_puentes')) {
        return {
            pointColor: '#0f52ba',
            pointRadius: 4.6,
            pointStrokeColor: '#ffffff',
            pointStrokeWidth: 1.1
        };
    }

    if (key.includes('drenaje_etiquetas')) {
        return {
            pointColor: '#0d7d84',
            pointRadius: 3.5,
            pointStrokeColor: '#ffffff',
            pointStrokeWidth: 1
        };
    }

    return {
        fillColor: '#2f7a74',
        fillOpacity: 0.08,
        outlineColor: '#235a56',
        lineWidth: 1.1,
        lineColor: '#235a56',
        lineOpacity: 0.85,
        pointColor: '#155e63',
        pointRadius: 4,
        pointStrokeColor: '#ffffff',
        pointStrokeWidth: 1
    };
}

async function runSearch(query) {
    if (!query) {
        dom.searchResults.innerHTML = '<p class="helper-text">Escribi un termino para buscar.</p>';
        return;
    }

    setMapStatus('Buscando...');
    try {
        const results = await fetchJson(`/api/search?q=${encodeURIComponent(query)}&limit=20`);
        renderSearchResults(results);
        setActiveTab('search');
        setMapStatus('Listo');
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.error(error); }
        dom.searchResults.innerHTML = '<p class="helper-text">No se pudo completar la busqueda.</p>';
        setMapStatus('Error');
    }
}

function renderSearchResults(results) {
    dom.searchResults.innerHTML = '';

    if (!results.length) {
        dom.searchResults.innerHTML = '<p class="helper-text">No hubo coincidencias para esa busqueda.</p>';
        return;
    }

    const fragment = document.createDocumentFragment();

    results.forEach((result) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'result-card';
        button.addEventListener('click', async () => {
            await focusSearchResult(result);
        });

        const chip = document.createElement('span');
        chip.className = 'result-chip';
        chip.textContent = result.tipo || 'resultado';

        const title = document.createElement('h3');
        title.textContent = result.titulo || 'Sin titulo';

        const detail = document.createElement('p');
        detail.className = 'result-detail';
        detail.textContent = result.detalle || `${result.schema_name}.${result.table_name}`;

        button.appendChild(chip);
        button.appendChild(title);
        button.appendChild(detail);
        fragment.appendChild(button);
    });

    dom.searchResults.appendChild(fragment);
}

async function focusSearchResult(result) {
    if (Number.isFinite(result.minx) && Number.isFinite(result.miny) && Number.isFinite(result.maxx) && Number.isFinite(result.maxy)) {
        fitBounds([result.minx, result.miny, result.maxx, result.maxy], { maxZoom: 16 });
    } else if (Number.isFinite(result.center_lon) && Number.isFinite(result.center_lat)) {
        appState.map.flyTo({
            center: [result.center_lon, result.center_lat],
            zoom: 15,
            essential: true
        });
    }

    if (result.schema_name && result.table_name && result.gid) {
        await updateFeatureDetail({
            schema_name: result.schema_name,
            table_name: result.table_name,
            display_name: result.titulo || `${result.schema_name}.${result.table_name}`
        }, result.gid, null);
    } else {
        renderSearchResultDetail(result);
    }

    setActiveTab('detail');
}

async function updateFeatureDetail(layer, gid, fallbackFeature) {
    const schemaName = layer.schema_name;
    const tableName = layer.table_name;

    if (!schemaName || !tableName || gid === undefined || gid === null) {
        if (fallbackFeature) {
            renderFeatureDetail(fallbackFeature, layer.display_name || `${schemaName}.${tableName}`);
        }
        return;
    }

    try {
        const feature = await fetchJson(
            `/api/feature?schema=${encodeURIComponent(schemaName)}&table=${encodeURIComponent(tableName)}&gid=${encodeURIComponent(gid)}`
        );
        renderFeatureDetail(feature, layer.display_name || `${schemaName}.${tableName}`);
    } catch (error) {
        if (typeof DEBUG !== 'undefined' && DEBUG) { console.error(error); }
        if (fallbackFeature) {
            renderFeatureDetail(fallbackFeature, layer.display_name || `${schemaName}.${tableName}`);
        } else {
            dom.featureDetail.classList.remove('empty');
            dom.featureDetail.innerHTML = '<p class="helper-text">No se pudo cargar la ficha del elemento seleccionado.</p>';
        }
    }
}

function renderSearchResultDetail(result) {
    dom.featureDetail.classList.remove('empty');
    dom.featureDetail.innerHTML = `
        <div class="result-chip">${escapeHtml(result.tipo || 'resultado')}</div>
        <h3>${escapeHtml(result.titulo || 'Sin titulo')}</h3>
        <p class="feature-detail">${escapeHtml(result.detalle || '')}</p>
    `;
}

function renderFeatureDetail(feature, layerLabel) {
    const properties = feature?.properties || {};
    const title = buildFeatureTitle(properties, layerLabel);
    const subtitle = buildFeatureSubtitle(properties, layerLabel);
    const rows = orderedPropertyEntries(properties)
        .map(([key, value]) => `
            <tr>
                <td>${escapeHtml(humanizeKey(key))}</td>
                <td>${escapeHtml(formatValue(value))}</td>
            </tr>
        `)
        .join('');

    dom.featureDetail.classList.remove('empty');
    dom.featureDetail.innerHTML = `
        <div class="result-chip">${escapeHtml(layerLabel || 'Capa')}</div>
        <h3>${escapeHtml(title)}</h3>
        ${subtitle ? `<p class="feature-detail">${escapeHtml(subtitle)}</p>` : ''}
        <table class="feature-table">
            <tbody>${rows}</tbody>
        </table>
    `;
}

function showFeaturePopup(layer, feature, lngLat) {
    const properties = feature.properties || {};
    const title = buildFeatureTitle(properties, layer.display_name);
    const detail = buildFeatureSubtitle(properties, layer.display_name) || `${layer.schema_name}.${layer.table_name}`;
    appState.popup
        .setLngLat(lngLat)
        .setHTML(`
            <p class="popup-title">${escapeHtml(title)}</p>
            <p class="popup-detail">${escapeHtml(detail)}</p>
        `)
        .addTo(appState.map);
}

function buildFeatureTitle(properties, fallback) {
    return (
        firstNonEmpty(
            properties.sigla,
            properties.nompropio,
            properties.barrio,
            properties.nombre,
            properties.name,
            properties.zona,
            properties.etiq
        ) || fallback || 'Elemento'
    );
}

function buildFeatureSubtitle(properties, fallback) {
    if (properties.tipopozo || properties.estpozo || properties.area || properties.yacimiento) {
        return [properties.tipopozo, properties.estpozo, properties.area, properties.yacimiento]
            .filter(Boolean)
            .join(' | ');
    }

    if (properties.pob || properties.sup_ha || properties.riesgo) {
        const parts = [];
        if (properties.zona) {
            parts.push(properties.zona);
        }
        if (properties.pob) {
            parts.push(`Pob. ${properties.pob}`);
        }
        if (properties.sup_ha) {
            parts.push(`Sup. ${properties.sup_ha} ha`);
        }
        if (properties.riesgo) {
            parts.push(String(properties.riesgo));
        }
        return parts.join(' | ');
    }

    return firstNonEmpty(properties.observ, properties.fuente, fallback);
}

function orderedPropertyEntries(properties) {
    const preferredOrder = [
        'gid',
        'sigla',
        'nompropio',
        'tipopozo',
        'estpozo',
        'area',
        'yacimiento',
        'barrio',
        'zona',
        'pob',
        'sup_ha',
        'riesgo',
        'nombre',
        'name',
        'etiq',
        'observ',
        'fuente',
        'id_src'
    ];

    const preferredSet = new Set(preferredOrder);
    const ordered = [];

    preferredOrder.forEach((key) => {
        if (Object.prototype.hasOwnProperty.call(properties, key) && properties[key] !== null && properties[key] !== '') {
            ordered.push([key, properties[key]]);
        }
    });

    Object.keys(properties)
        .filter((key) => !preferredSet.has(key))
        .sort((a, b) => a.localeCompare(b))
        .forEach((key) => {
            const value = properties[key];
            if (value !== null && value !== '') {
                ordered.push([key, value]);
            }
        });

    return ordered;
}

function fitVisibleLayers() {
    const combined = {
        minx: Infinity,
        miny: Infinity,
        maxx: -Infinity,
        maxy: -Infinity
    };

    let hasBounds = false;

    appState.layerData.forEach((geojson) => {
        const bounds = featureCollectionBounds(geojson);
        if (!bounds) {
            return;
        }
        hasBounds = true;
        combined.minx = Math.min(combined.minx, bounds[0]);
        combined.miny = Math.min(combined.miny, bounds[1]);
        combined.maxx = Math.max(combined.maxx, bounds[2]);
        combined.maxy = Math.max(combined.maxy, bounds[3]);
    });

    if (hasBounds) {
        fitBounds([combined.minx, combined.miny, combined.maxx, combined.maxy], { maxZoom: 16 });
        return;
    }

    fitBounds([DEFAULT_BOUNDS[0][0], DEFAULT_BOUNDS[0][1], DEFAULT_BOUNDS[1][0], DEFAULT_BOUNDS[1][1]], { maxZoom: 12.8 });
}

function fitBounds(bbox, options = {}) {
    appState.map.fitBounds(
        [
            [bbox[0], bbox[1]],
            [bbox[2], bbox[3]]
        ],
        {
            padding: 52,
            duration: 700,
            maxZoom: options.maxZoom || 16
        }
    );
}

function featureCollectionBounds(geojson) {
    if (!geojson || !Array.isArray(geojson.features) || !geojson.features.length) {
        return null;
    }

    const bounds = {
        minx: Infinity,
        miny: Infinity,
        maxx: -Infinity,
        maxy: -Infinity
    };

    geojson.features.forEach((feature) => {
        extendBounds(bounds, feature.geometry?.coordinates);
    });

    if (!Number.isFinite(bounds.minx)) {
        return null;
    }

    return [bounds.minx, bounds.miny, bounds.maxx, bounds.maxy];
}

function extendBounds(bounds, coordinates) {
    if (!Array.isArray(coordinates)) {
        return;
    }

    if (coordinates.length >= 2 && typeof coordinates[0] === 'number' && typeof coordinates[1] === 'number') {
        bounds.minx = Math.min(bounds.minx, coordinates[0]);
        bounds.miny = Math.min(bounds.miny, coordinates[1]);
        bounds.maxx = Math.max(bounds.maxx, coordinates[0]);
        bounds.maxy = Math.max(bounds.maxy, coordinates[1]);
        return;
    }

    coordinates.forEach((child) => {
        extendBounds(bounds, child);
    });
}

async function fetchJson(url) {
    const response = await fetch(url, { cache: 'no-store' });
    const payload = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(payload.error || payload.detail || `HTTP ${response.status}`);
    }

    return payload;
}

function firstNonEmpty(...values) {
    return values.find((value) => value !== null && value !== undefined && String(value).trim() !== '');
}

function humanizeKey(value) {
    return String(value)
        .replace(/_/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
        .replace(/\b\w/g, (char) => char.toUpperCase());
}

function formatValue(value) {
    if (value === null || value === undefined || value === '') {
        return '-';
    }
    if (typeof value === 'number') {
        return Number.isInteger(value) ? String(value) : value.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    }
    if (Array.isArray(value)) {
        return value.join(', ');
    }
    if (typeof value === 'object') {
        return JSON.stringify(value);
    }
    return String(value);
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
