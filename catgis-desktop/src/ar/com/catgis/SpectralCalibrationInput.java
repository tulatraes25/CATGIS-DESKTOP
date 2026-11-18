package ar.com.catgis;

/**
 * T3/#139: explicit spectral input calibration contract for the generic UI.
 *
 * <p>Generic local rasters carry no validated per-band scale/offset/NoData.
 * The dialog therefore requires the user to declare reflectance calibration
 * explicitly; ambiguous raw DN inputs are rejected instead of producing a
 * mislabeled "professional" index.</p>
 */
public final class SpectralCalibrationInput {

    private SpectralCalibrationInput() {}

    public record Input(SpectralIndexEngine.BandCalibration bandA,
                        SpectralIndexEngine.BandCalibration bandB,
                        String provenance) {}

    public static Input parse(boolean declaredCalibrated,
                              String scaleA, String offsetA, String noDataA,
                              String scaleB, String offsetB, String noDataB,
                              String provenance) {
        if (!declaredCalibrated) {
            throw new IllegalArgumentException(
                    "Declara explicitamente que las bandas ya estan calibradas a reflectancia "
                            + "(scale_factor/add_offset/NoData). CATGIS no calcula indices "
                            + "profesionales sobre DN sin calibracion.");
        }
        SpectralIndexEngine.BandCalibration a = calibration(
                "A", scaleA, offsetA, noDataA, provenance);
        SpectralIndexEngine.BandCalibration b = calibration(
                "B", scaleB, offsetB, noDataB, provenance);
        return new Input(a, b, a.source());
    }

    private static SpectralIndexEngine.BandCalibration calibration(
            String band, String scaleText, String offsetText, String noDataText,
            String provenance) {
        double scale = parseFinite(scaleText, "scale_factor de banda " + band);
        if (scale == 0.0) {
            throw new IllegalArgumentException(
                    "scale_factor de banda " + band + " no puede ser cero.");
        }
        double offset = parseFinite(offsetText, "add_offset de banda " + band);
        Float noData = null;
        if (noDataText != null && !noDataText.isBlank()) {
            float parsed = (float) parseFinite(noDataText, "NoData de banda " + band);
            noData = parsed;
        }
        String source = provenance == null || provenance.isBlank()
                ? "calibracion declarada por el usuario" : provenance.trim();
        return SpectralIndexEngine.BandCalibration.reflectance(scale, offset, noData, source);
    }

    private static double parseFinite(String text, String label) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(label + " es obligatorio y debe ser finito.");
        }
        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException(label + " no es un numero valido: " + text, invalid);
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " debe ser finito.");
        }
        return value;
    }
}
