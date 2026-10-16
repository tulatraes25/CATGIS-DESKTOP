package ar.com.catgis;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.List;

/**
 * Engine for computing spectral indices from multiband raster data.
 * Supports NDVI, NDWI, SAVI, EVI, NBR, MNDWI, BSI, NDMI, and more.
 */
public final class SpectralIndexEngine {

    private SpectralIndexEngine() {}

    public record SpectralIndex(String id, String name, String description,
                                String bandA, String bandB, String formula) {}

    public static List<SpectralIndex> getIndices() {
        return List.of(
            new SpectralIndex("NDVI", "NDVI - Indice de Vegetacion",
                    "Mide la salud y densidad de la vegetacion. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)", "(NIR - Red) / (NIR + Red)"),
            new SpectralIndex("NDWI", "NDWI - Indice de Agua",
                    "Detecta cuerpos de agua y humedad del suelo. Rango: -1 a +1.",
                    "Verde (Banda 2)", "NIR (Banda 4 o 5)", "(Green - NIR) / (Green + NIR)"),
            new SpectralIndex("MNDWI", "MNDWI - NDWI Modificado",
                    "Mejora la discriminacion de agua usando SWIR. Rango: -1 a +1.",
                    "Verde (Banda 2)", "SWIR (Banda 5 o 6)", "(Green - SWIR) / (Green + SWIR)"),
            new SpectralIndex("SAVI", "SAVI - Indice de Vegetacion Ajustado al Suelo",
                    "NDVI corregido para efecto del suelo. L=0.5 por defecto.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)", "((NIR - Red) / (NIR + Red + 0.5)) * 1.5"),
            new SpectralIndex("EVI", "EVI - Indice de Vegetacion Mejorado",
                    "Indice de vegetacion con correccion atmosferica; requiere NIR, rojo y azul.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)",
                    "2.5 * (NIR - Red) / (NIR + 6*Red - 7.5*Blue + 1)"),
            new SpectralIndex("NBR", "NBR - Ratio de Quemadura Normalizado",
                    "Detecta areas quemadas y vigor de vegetacion. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "SWIR (Banda 5 o 6)", "(NIR - SWIR) / (NIR + SWIR)"),
            new SpectralIndex("NDMI", "NDMI - Indice de Humedad Normalizado",
                    "Estima el contenido de humedad de la vegetacion. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "SWIR (Banda 5 o 6)", "(NIR - SWIR) / (NIR + SWIR)"),
            new SpectralIndex("BSI", "BSI - Indice de Suelo Desnudo",
                    "Detecta suelo expuesto; requiere SWIR, rojo, NIR y azul.",
                    "SWIR (Banda 5 o 6)", "Rojo (Banda 3)",
                    "((SWIR + Red) - (NIR + Blue)) / ((SWIR + Red) + (NIR + Blue))"),
            new SpectralIndex("NDRE", "NDRE - NDVI de Borde Rojo",
                    "Sensible a cambios en clorofila. Util para cultivos en crecimiento.",
                    "Red Edge (Banda 5 o 6)", "Rojo (Banda 3)", "(RE - Red) / (RE + Red)"),
            new SpectralIndex("MSAVI2", "MSAVI2 - SAVI Modificado",
                    "SAVI automatico sin necesidad de parametro L.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)",
                    "(2 * NIR + 1 - sqrt((2*NIR+1)^2 - 8*(NIR-Red))) / 2"),
            new SpectralIndex("NDGI", "NDGI - Indice de Diferencia Verde",
                    "Detecta cambios en vegetacion y suelo desnudo.",
                    "Verde (Banda 2)", "NIR (Banda 4 o 5)", "(Green - NIR) / (Green + NIR)"),
            new SpectralIndex("TNDVI", "TNDVI - NDVI Transformado",
                    "NDVI transformado para mejorar contraste en areas con poca variacion.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)",
                    "sqrt(((NIR - Red) / (NIR + Red)) + 0.5)")
        );
    }

    public static int requiredBandCount(String indexId) {
        return switch (indexId) {
            case "EVI" -> 3;
            case "BSI" -> 4;
            case "NDVI", "NDGI", "NDWI", "MNDWI", "NBR", "NDMI", "NDRE",
                    "SAVI", "MSAVI2", "TNDVI" -> 2;
            default -> throw new IllegalArgumentException("Unknown spectral index: " + indexId);
        };
    }

    public static BufferedImage computeIndex(BufferedImage bandA, BufferedImage bandB,
                                             String indexId) {
        if (bandA == null || bandB == null) return null;
        requireArity(indexId, 2);
        int width = bandA.getWidth();
        int height = bandA.getHeight();
        if (width != bandB.getWidth() || height != bandB.getHeight()) return null;

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Raster first = bandA.getRaster();
        Raster second = bandB.getRaster();
        WritableRaster output = result.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = computeRaw2(first.getSampleDouble(x, y, 0),
                        second.getSampleDouble(x, y, 0), indexId);
                output.setSample(x, y, 0, normalizeToByte(value));
            }
        }
        return result;
    }

    public static BufferedImage computeIndex3(BufferedImage bandA, BufferedImage bandB,
                                               BufferedImage bandC, String indexId) {
        if (bandA == null || bandB == null || bandC == null) return null;
        requireArity(indexId, 3);
        if (!sameDimensions(bandA, bandB) || !sameDimensions(bandA, bandC)) return null;
        int width = bandA.getWidth();
        int height = bandA.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = computeRaw3(
                        bandA.getRaster().getSampleDouble(x, y, 0),
                        bandB.getRaster().getSampleDouble(x, y, 0),
                        bandC.getRaster().getSampleDouble(x, y, 0), indexId);
                result.getRaster().setSample(x, y, 0, normalizeToByte(value));
            }
        }
        return result;
    }

    public static BufferedImage computeIndex4(BufferedImage bandA, BufferedImage bandB,
                                               BufferedImage bandC, BufferedImage bandD,
                                               String indexId) {
        if (bandA == null || bandB == null || bandC == null || bandD == null) return null;
        requireArity(indexId, 4);
        if (!sameDimensions(bandA, bandB) || !sameDimensions(bandA, bandC)
                || !sameDimensions(bandA, bandD)) return null;
        int width = bandA.getWidth();
        int height = bandA.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = computeRaw4(
                        bandA.getRaster().getSampleDouble(x, y, 0),
                        bandB.getRaster().getSampleDouble(x, y, 0),
                        bandC.getRaster().getSampleDouble(x, y, 0),
                        bandD.getRaster().getSampleDouble(x, y, 0), indexId);
                result.getRaster().setSample(x, y, 0, normalizeToByte(value));
            }
        }
        return result;
    }

    public static BufferedImage computeNDVI(BufferedImage redBand, BufferedImage nirBand) {
        return computeIndex(nirBand, redBand, "NDVI");
    }

    public static BufferedImage computeNDWI(BufferedImage greenBand, BufferedImage nirBand) {
        return computeIndex(greenBand, nirBand, "NDWI");
    }

    static double computeRaw2(double a, double b, String indexId) {
        requireArity(indexId, 2);
        return switch (indexId) {
            case "NDVI", "NDGI", "NDWI", "MNDWI", "NBR", "NDMI", "NDRE" ->
                    normalizedDifference(a, b);
            case "SAVI" -> {
                double denominator = a + b + 0.5;
                yield denominator == 0.0 ? 0.0 : ((a - b) / denominator) * 1.5;
            }
            case "MSAVI2" -> {
                double inner = (2 * a + 1) * (2 * a + 1) - 8 * (a - b);
                yield inner < 0 ? 0 : (2 * a + 1 - Math.sqrt(inner)) / 2;
            }
            case "TNDVI" -> {
                double denominator = a + b;
                if (denominator == 0.0) yield 0.0;
                double inner = (a - b) / denominator + 0.5;
                yield inner < 0 ? 0 : Math.sqrt(inner);
            }
            default -> throw new IllegalArgumentException(
                    "Index is not a two-band formula: " + indexId);
        };
    }

    static double computeRaw3(double nir, double red, double blue, String indexId) {
        requireArity(indexId, 3);
        double denominator = nir + 6 * red - 7.5 * blue + 1;
        if (denominator == 0.0) return 0.0;
        return clampUnit(2.5 * (nir - red) / denominator);
    }

    static double computeRaw4(double swir, double red, double nir, double blue,
                              String indexId) {
        requireArity(indexId, 4);
        double numerator = (swir + red) - (nir + blue);
        double denominator = (swir + red) + (nir + blue);
        return denominator == 0.0 ? 0.0 : clampUnit(numerator / denominator);
    }

    private static double normalizedDifference(double a, double b) {
        double denominator = a + b;
        return denominator == 0.0 ? 0.0 : (a - b) / denominator;
    }

    private static double clampUnit(double value) {
        return Math.min(1.0, Math.max(-1.0, value));
    }

    private static boolean sameDimensions(BufferedImage first, BufferedImage second) {
        return first.getWidth() == second.getWidth()
                && first.getHeight() == second.getHeight();
    }

    private static void requireArity(String indexId, int actualBands) {
        int required = requiredBandCount(indexId);
        if (required != actualBands) {
            throw new IllegalArgumentException("Spectral index " + indexId + " requires "
                    + required + " bands, not " + actualBands + ".");
        }
    }

    private static double normalizeToByte(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Spectral index produced a non-finite value.");
        }
        double normalized = (value + 1.0) / 2.0;
        return Math.max(0, Math.min(255, normalized * 255));
    }

    public static double[] getIndexRange(String indexId) {
        requiredBandCount(indexId);
        return new double[]{-1.0, 1.0};
    }

    public static String getColorRampName(String indexId) {
        requiredBandCount(indexId);
        return switch (indexId) {
            case "NDVI", "NDGI", "SAVI", "EVI", "MSAVI2", "TNDVI", "NDRE" ->
                    "Vegetacion (rojo-verde)";
            case "NDWI", "MNDWI" -> "Agua (azul)";
            case "NBR", "NDMI" -> "Quemadura (marron-verde)";
            case "BSI" -> "Suelo (marron)";
            default -> throw new IllegalArgumentException("Unknown spectral index: " + indexId);
        };
    }
}
