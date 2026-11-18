package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

/**
 * T3/#176: explicit LiDAR preview policy.
 *
 * <p>Uncompressed LAS up to {@link #MAX_PREVIEW_POINTS} is loaded completely.
 * Larger datasets yield an explicit, spatially stratified sample (stride over the
 * declared point range), marked PARTIAL_PREVIEW so analyses/exports never treat a
 * biased prefix or sample as the complete dataset.</p>
 */
public final class LasPreviewPolicy {

    public static final int MAX_PREVIEW_POINTS = 50_000;

    public record Plan(boolean preview, int samplePoints, long totalPoints,
                       long stride, String message) {}

    private LasPreviewPolicy() {}

    /**
     * T3/#176: a LiDAR PARTIAL_PREVIEW must never be exported or analysed as a
     * complete dataset. Callers surface the reason to the user.
     */
    public static void requireCompleteForExport(Layer layer) {
        requireComplete(layer, "exportar");
    }

    /** Blocks professional analysis on a sampled LiDAR preview. */
    public static void requireCompleteForAnalysis(Layer layer, String operation) {
        requireComplete(layer, operation == null || operation.isBlank()
                ? "analizar" : operation);
    }

    private static void requireComplete(Layer layer, String operation) {
        if (layer != null && layer.isPartialPreview()) {
            throw new IllegalStateException("La capa LiDAR es una vista previa ("
                    + layer.getLoadedSourceFeatures() + " de " + layer.getTotalSourceFeatures()
                    + " puntos); no se puede " + operation
                    + " como dataset completo. Carga el dataset completo con un lector "
                    + "PDAL/indexado.");
        }
    }

    /** User-visible persistent badge for a preview layer ("" when complete). */
    public static String statusLabel(Layer layer) {
        if (layer == null || !layer.isPartialPreview()) {
            return "";
        }
        return "PREVIEW - " + layer.getLoadedSourceFeatures() + " de "
                + layer.getTotalSourceFeatures() + " puntos (muestra estratificada)";
    }

    public static Plan plan(long totalPoints, int maxPoints) {
        if (totalPoints < 0) {
            throw new IllegalArgumentException("LiDAR: conteo de puntos invalido: " + totalPoints);
        }
        if (maxPoints <= 0) {
            throw new IllegalArgumentException("LiDAR: limite de preview invalido: " + maxPoints);
        }
        if (totalPoints <= maxPoints) {
            return new Plan(false, (int) totalPoints, totalPoints, 1L,
                    "LiDAR completo: " + totalPoints + " puntos verificados");
        }
        long stride = (totalPoints + maxPoints - 1L) / maxPoints;
        long sampled = (totalPoints + stride - 1L) / stride;
        int samplePoints = (int) Math.min(maxPoints, sampled);
        return new Plan(true, samplePoints, totalPoints, stride,
                "PARTIAL_PREVIEW: muestra estratificada de " + samplePoints
                        + " de " + totalPoints + " puntos (paso " + stride
                        + "); no es el dataset completo");
    }
}
