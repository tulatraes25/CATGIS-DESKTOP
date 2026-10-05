package ar.com.catgis;

public interface CadPlacementDragHandler {
    void onDragApplied(double offsetX, double offsetY);

    void onDragCanceled();
}
