package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

/**
 * Layer subclass for FlatGeobuf data sources.
 * FlatGeobuf is a binary format for encoding vector data with spatial indexing.
 */
public class FlatGeobufLayer extends Layer {

    public FlatGeobufLayer(String name, String path) {
        super(name, path, "FLATGEOBUF");
    }
}
