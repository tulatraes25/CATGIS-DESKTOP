package ar.com.catgis;

public interface ReadOnlyVectorLayerSource {

    boolean isReadOnly();

    String getReadOnlyReason();
}
