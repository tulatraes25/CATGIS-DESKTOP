package ar.com.catgis;

import java.io.File;

public class LayerLoader {

    public static Layer loadLayer(File archivo) {
        String nombreArchivo = archivo.getName().toLowerCase();

        if (nombreArchivo.endsWith(".tif") || nombreArchivo.endsWith(".tiff")) {
            return new RasterLayer(archivo.getName(), archivo.getAbsolutePath());
        }

        return new VectorLayer(archivo.getName(), archivo.getAbsolutePath());
    }
}