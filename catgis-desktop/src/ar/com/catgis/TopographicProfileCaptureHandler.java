package ar.com.catgis;

import org.locationtech.jts.geom.LineString;

public interface TopographicProfileCaptureHandler {
    void onLineCaptured(LineString line, String sourceCrs);

    void onCaptureCanceled();
}
