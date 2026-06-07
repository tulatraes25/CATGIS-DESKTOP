package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;

final class ReleaseVectorInteropTestHelper {

    private ReleaseVectorInteropTestHelper() {
    }

    static Object buildKmlOptions(boolean exportLabels, String labelField, boolean includeDescription) throws Exception {
        Class<?> optionsClass = Class.forName("ar.com.catgis.ExportVectorLayerAction$KmlExportOptions");
        Constructor<?> constructor = optionsClass.getDeclaredConstructor(boolean.class, String.class, boolean.class);
        constructor.setAccessible(true);
        return constructor.newInstance(exportLabels, labelField, includeDescription);
    }

    static void exportKmlReflective(Layer layer, ShapefileData data, Path file, Object options) throws Exception {
        Class<?> optionsClass = Class.forName("ar.com.catgis.ExportVectorLayerAction$KmlExportOptions");
        Method method = ExportVectorLayerAction.class.getDeclaredMethod(
                "exportToKml",
                Layer.class,
                ShapefileData.class,
                File.class,
                optionsClass
        );
        method.setAccessible(true);
        method.invoke(null, layer, data, file.toFile(), options);
    }
}
