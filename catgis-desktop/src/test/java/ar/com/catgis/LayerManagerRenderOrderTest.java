package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.online.OnlineWmsLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayerManagerRenderOrderTest {

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void basemapLayersRenderBeforeThematicLayersWhilePreservingRelativeOrder() {
        Project project = new Project("Render order test");
        AppContext.setCurrentProject(project);

        MapPanel panel = new MapPanel();

        Layer thematicTop = new Layer("Thematic top", "", "VECTOR");
        OnlineTileLayer basemapMid = new OnlineTileLayer("Basemap mid");
        Layer thematicBottom = new Layer("Thematic bottom", "", "VECTOR");
        OnlineWmsLayer basemapBottom = new OnlineWmsLayer("Basemap bottom");

        project.addLayer(thematicTop);
        project.addLayer(basemapMid);
        project.addLayer(thematicBottom);
        project.addLayer(basemapBottom);

        panel.shapefileLayers.put(thematicTop, null);
        panel.onlineTileLayers.put(basemapMid, null);
        panel.shapefileLayers.put(thematicBottom, null);
        panel.onlineWmsLayers.put(basemapBottom, null);

        List<Layer> renderOrder = panel.layerManager.getRenderOrderLayers();

        assertEquals(List.of(basemapBottom, basemapMid, thematicBottom, thematicTop), renderOrder);
    }
}
