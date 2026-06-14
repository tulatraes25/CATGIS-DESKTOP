package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;

public interface MapPointCaptureHandler {
    void onPointCaptured(Coordinate coordinate, String sourceCrs);

    void onCaptureCanceled();
}
