package ar.com.catgis.integration;

import ar.com.catgis.AppContext;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.LayersPanel;
import ar.com.catgis.MapPanel;
import ar.com.catgis.ModuleRegistry;
import ar.com.catgis.StatusBar;
import ar.com.catgis.core.model.Project;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import javax.swing.SwingUtilities;
import java.io.File;
import java.lang.reflect.Method;

final class IntegrationTestSupport {

    static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private IntegrationTestSupport() {
    }

    static SimpleFeatureType createFeatureType(String name,
                                               String crsCode,
                                               Class<? extends Geometry> geometryClass,
                                               Object[][] attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(name);
        if (crsCode != null && !crsCode.isBlank()) {
            if ("EPSG:4326".equalsIgnoreCase(crsCode.trim())) {
                builder.setCRS(DefaultGeographicCRS.WGS84);
            } else {
                builder.setCRS(CRSDefinitions.decode(crsCode, false));
            }
        }
        builder.add("the_geom", geometryClass);
        if (attributes != null) {
            for (Object[] attribute : attributes) {
                builder.add(String.valueOf(attribute[0]), (Class<?>) attribute[1]);
            }
        }
        return builder.buildFeatureType();
    }

    static SimpleFeature buildFeature(SimpleFeatureType type, String id, Object... values) {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        for (Object value : values) {
            featureBuilder.add(value);
        }
        return featureBuilder.buildFeature(id);
    }

    static void initializeAppContext(String projectName) {
        ModuleRegistry.initializeDefaults();
        AppContext.setCurrentProject(new Project(projectName));
        AppContext.get().setMapPanel(new MapPanel());
        CatgisDesktopApp.layersPanel = new LayersPanel();
        CatgisDesktopApp.statusBar = new StatusBar();
    }

    static void clearAppContext() {
        AppContext.setCurrentProject(null);
        AppContext.get().setMapPanel(null);
        CatgisDesktopApp.layersPanel = null;
        CatgisDesktopApp.statusBar = null;
    }

    static void runOnEdt(ThrowingRunnable runnable) throws Exception {
        final Exception[] failure = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                failure[0] = ex;
            }
        });
        if (failure[0] != null) {
            throw failure[0];
        }
    }

    static boolean saveProject(File file, boolean allowRasterPrompts) throws Exception {
        Method method = ar.com.catgis.SaveProjectAction.class.getDeclaredMethod("saveProjectToFile", File.class, boolean.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, file, allowRasterPrompts);
    }

    static boolean loadProject(File file, boolean fromRecent) throws Exception {
        Method method = ar.com.catgis.LoadProjectAction.class.getDeclaredMethod("loadProjectFile", File.class, boolean.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, file, fromRecent);
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
