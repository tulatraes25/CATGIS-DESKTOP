package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

public class VectorLayer extends Layer {

    public VectorLayer(String name, String path) {
        super(name, path, "[VECTOR]");
    }
}