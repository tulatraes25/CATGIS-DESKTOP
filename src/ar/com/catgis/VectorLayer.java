package ar.com.catgis;

public class VectorLayer extends Layer {

    public VectorLayer(String name, String path) {
        super(name, path, "[VECTOR]");
    }
}