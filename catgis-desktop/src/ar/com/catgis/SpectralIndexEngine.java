package ar.com.catgis;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
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
                    "NDVI mejorado con correccion atmosferica. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)", "2.5 * (NIR - Red) / (NIR + 6*Red - 7.5*Blue + 1)"),
            new SpectralIndex("NBR", "NBR - Ratio de Quemadura Normalizado",
                    "Detecta areas quemadas y vigor de vegetacion. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "SWIR (Banda 5 o 6)", "(NIR - SWIR) / (NIR + SWIR)"),
            new SpectralIndex("NDMI", "NDMI - Indice de Humedad Normalizado",
                    "Estima el contenido de humedad de la vegetacion. Rango: -1 a +1.",
                    "NIR (Banda 4 o 5)", "SWIR (Banda 5 o 6)", "(NIR - SWIR) / (NIR + SWIR)"),
            new SpectralIndex("BSI", "BSI - Indice de Suelo Desnudo",
                    "Detecta areas de suelo expuesto sin vegetacion.",
                    "SWIR (Banda 5 o 6)", "Rojo (Banda 3)", "((SWIR + Red) - (NIR + Blue)) / ((SWIR + Red) + (NIR + Blue))"),
            new SpectralIndex("NDRE", "NDRE - NDVI de Borde Rojo",
                    "Sensible a cambios en clorofila. Util para cultivos en crecimiento.",
                    "Red Edge (Banda 5 o 6)", "Rojo (Banda 3)", "(RE - Red) / (RE + Red)"),
            new SpectralIndex("MSAVI2", "MSAVI2 - SAVI Modificado",
                    "SAVI automatico sin necesidad de parametro L.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)", "(2 * NIR + 1 - sqrt((2*NIR+1)^2 - 8*(NIR-Red))) / 2"),
            new SpectralIndex("NDGI", "NDGI - Indice de Diferencia Verde",
                    "Detecta cambios en vegetacion y suelo desnudo.",
                    "Verde (Banda 2)", "NIR (Banda 4 o 5)", "(Green - NIR) / (Green + NIR)"),
            new SpectralIndex("TNDVI", "TNDVI - NDVI Transformado",
                    "NDVI transformado para mejorar contraste en areas con poca variacion.",
                    "NIR (Banda 4 o 5)", "Rojo (Banda 3)", "sqrt(((NIR - Red) / (NIR + Red)) + 0.5)")
        );
    }

    /**
     * Compute a spectral index from two single-band images.
     */
    public static BufferedImage computeIndex(BufferedImage bandA, BufferedImage bandB, String indexId) {
        if (bandA == null || bandB == null) return null;
        int w = bandA.getWidth();
        int h = bandA.getHeight();
        if (w != bandB.getWidth() || h != bandB.getHeight()) return null;

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Raster ra = bandA.getRaster();
        Raster rb = bandB.getRaster();
        WritableRaster out = result.getRaster();

        double[] pixelA = new double[ra.getNumBands()];
        double[] pixelB = new double[rb.getNumBands()];
        double[] outPixel = new double[1];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ra.getPixel(x, y, pixelA);
                rb.getPixel(x, y, pixelB);
                double a = pixelA[0];
                double b = pixelB[0];
                double value = computePixel(a, b, indexId);
                outPixel[0] = normalizeToByte(value, indexId);
                out.setPixel(x, y, outPixel);
            }
        }
        return result;
    }

    /**
     * Compute NDVI from Red and NIR bands.
     */
    public static BufferedImage computeNDVI(BufferedImage redBand, BufferedImage nirBand) {
        return computeIndex(nirBand, redBand, "NDVI");
    }

    /**
     * Compute NDWI from Green and NIR bands.
     */
    public static BufferedImage computeNDWI(BufferedImage greenBand, BufferedImage nirBand) {
        return computeIndex(greenBand, nirBand, "NDWI");
    }

    private static double computePixel(double a, double b, String indexId) {
        return switch (indexId) {
            case "NDVI", "NDGI", "NDWI", "MNDWI", "NBR", "NDMI", "NDRE" -> {
                double denom = a + b;
                yield denom == 0 ? 0 : (a - b) / denom;
            }
            case "SAVI" -> {
                double denom = a + b + 0.5;
                yield denom == 0 ? 0 : ((a - b) / denom) * 1.5;
            }
            case "EVI" -> {
                double denom = a + 6 * b + 1;
                yield denom == 0 ? 0 : Math.min(1, Math.max(-1, 2.5 * (a - b) / denom));
            }
            case "BSI" -> {
                double num = a - b;
                double denom = a + b;
                yield denom == 0 ? 0 : Math.min(1, Math.max(-1, num / denom));
            }
            case "MSAVI2" -> {
                double inner = (2 * a + 1) * (2 * a + 1) - 8 * (a - b);
                yield inner < 0 ? 0 : (2 * a + 1 - Math.sqrt(inner)) / 2;
            }
            case "TNDVI" -> {
                double inner = (a - b) / (a + b) + 0.5;
                yield inner < 0 ? 0 : Math.sqrt(inner);
            }
            default -> 0;
        };
    }

    private static double normalizeToByte(double value, String indexId) {
        // Most indices range from -1 to +1
        // Normalize to 0-255 for display
        double normalized = (value + 1.0) / 2.0; // Maps -1..+1 to 0..1
        return Math.max(0, Math.min(255, normalized * 255));
    }

    /**
     * Get the display range for an index (min, max for color ramp).
     */
    public static double[] getIndexRange(String indexId) {
        return switch (indexId) {
            case "NDVI", "NDGI", "NDWI", "MNDWI", "NBR", "NDMI", "NDRE", "SAVI", "EVI", "MSAVI2", "TNDVI" ->
                    new double[]{-1.0, 1.0};
            case "BSI" -> new double[]{-1.0, 1.0};
            default -> new double[]{-1.0, 1.0};
        };
    }

    /**
     * Get a descriptive color ramp name for an index.
     */
    public static String getColorRampName(String indexId) {
        return switch (indexId) {
            case "NDVI", "NDGI", "SAVI", "EVI", "MSAVI2", "TNDVI", "NDRE" -> "Vegetacion (rojo-verde)";
            case "NDWI", "MNDWI" -> "Agua (azul)";
            case "NBR", "NDMI" -> "Quemadura (marron-verde)";
            case "BSI" -> "Suelo (marron)";
            default -> "Escala de grises";
        };
    }
}
