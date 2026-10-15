package ar.com.catgis;

import java.math.BigInteger;
import java.util.Locale;

/**
 * WCS form dimensions: never turn an explicit invalid request into 512x512.
 * The user-facing preflight is informative, not an arbitrary hard pixel cap.
 */
final class WcsDimensionsPreflight {

    private static final BigInteger FOUR_BYTES = BigInteger.valueOf(4L);
    private static final BigInteger MEBIBYTE = BigInteger.valueOf(1024L * 1024L);

    private WcsDimensionsPreflight() {}

    record Checked(int width, int height, long pixels, BigInteger roughBytes) {}

    static Checked validate(String widthInput, String heightInput) {
        int width = positiveInt(widthInput, "ancho");
        int height = positiveInt(heightInput, "alto");
        long pixels = Math.multiplyExact((long) width, (long) height);
        // A deliberately rough ONE-BAND 32-bit estimate; real service band
        // count, TIFF metadata, compression and response byte totals vary.
        BigInteger bytes = BigInteger.valueOf(pixels).multiply(FOUR_BYTES);
        return new Checked(width, height, pixels, bytes);
    }

    private static int positiveInt(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(
                    "El " + field + " WCS debe ser un entero positivo.");
        }
        String value = raw.trim();
        if (!value.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "El " + field + " WCS debe ser un entero positivo.");
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(
                        "El " + field + " WCS debe ser mayor que cero.");
            }
            return parsed;
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException(
                    "El " + field + " WCS excede el rango entero permitido.", invalid);
        }
    }

    static boolean requiresConfirmation(Checked dimensions, long responseLimit) {
        if (responseLimit <= 0L) {
            throw new IllegalArgumentException("El presupuesto WCS debe ser positivo.");
        }
        return dimensions.roughBytes().compareTo(BigInteger.valueOf(responseLimit)) > 0;
    }

    static String warning(Checked dimensions, long responseLimit) {
        if (responseLimit <= 0L) {
            throw new IllegalArgumentException("El presupuesto WCS debe ser positivo.");
        }
        BigInteger approxMiB = dimensions.roughBytes().add(MEBIBYTE.subtract(BigInteger.ONE))
                .divide(MEBIBYTE);
        long limitMiB = responseLimit / (1024L * 1024L);
        return String.format(Locale.ROOT,
                "Solicitaste %d x %d pixeles (%d en total).\n"
                + "Estimacion ilustrativa para UNA banda de 32 bits sin compresion: "
                + "aproximadamente %s MiB.\n"
                + "Presupuesto maximo configurado de transferencia WCS: %d MiB.\n\n"
                + "La cantidad real de bandas, compresion y metadatos puede "
                + "cambiar mucho el tamano descargado.\n"
                + "CATGIS conserva el limite de transferencia y su cancelacion "
                + "aunque elijas continuar.\n"
                + "Deseas continuar con estas dimensiones?",
                dimensions.width(), dimensions.height(), dimensions.pixels(),
                approxMiB, limitMiB);
    }
}
