package ar.com.catgis.catmap;

import ar.com.catgis.CRSDefinitions;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutRenderHosts;
import ar.com.catgis.layout.MapFrameRenderer;
import ar.com.catgis.layout.MapFrameViewport;
import ar.com.catgis.core.model.Project;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Exports the active layout page's map frame as a georeferenced GeoTIFF: the
 * raster is rendered with the frame's viewport and carries the project CRS and
 * world extent, so it can be opened in any GIS with correct coordinates.
 */
public final class GeoTiffLayoutExporter {

    public static final int RENDER_DPI = 96;

    private GeoTiffLayoutExporter() {}

    /** Export the first visible map frame of the active page. */
    public static File exportMapFrame(LayoutModel model, File target, int dpi) throws Exception {
        Objects.requireNonNull(model, "CATMAP layout model");
        Objects.requireNonNull(target, "CATMAP GeoTIFF destination");
        LayoutMap map = firstVisibleMap(model);
        if (map == null) {
            throw new IOException("CATMAP: no hay un marco de mapa visible en la pagina activa.");
        }
        CoordinateReferenceSystem crs = projectCrs();
        Rectangle2D.Double bounds = map.getBoundsMm();
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw new IOException("CATMAP: el marco de mapa no tiene dimensiones validas.");
        }
        int[] dimensions = CatmapAtomicExport.rasterSize(dpi, bounds.width, bounds.height);
        int widthPx = dimensions[0];
        int heightPx = dimensions[1];
        MapFrameViewport viewport = map.viewportForFrame(widthPx, heightPx);
        if (!(viewport.getWidth() > 0) || !(viewport.getHeight() > 0)) {
            throw new IOException("CATMAP: el marco de mapa no tiene una vista valida.");
        }
        BufferedImage image = new MapFrameRenderer(viewport).render(widthPx, heightPx, RENDER_DPI);
        if (image == null) {
            throw new IOException("CATMAP: no se pudo renderizar el mapa para el GeoTIFF.");
        }
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                viewport.getMinX(), viewport.getMaxX(),
                viewport.getMinY(), viewport.getMaxY(), crs);
        GridCoverage2D coverage = new GridCoverageFactory().create(
                "CATMAP", image, envelope);
        try {
            CatmapAtomicExport.publish(target,
                    staged -> writeGeoTiff(coverage, staged.toFile()),
                    staged -> verifyGeoTiff(staged.toFile(), crs, envelope,
                            widthPx, heightPx));
        } finally {
            coverage.dispose(true);
            image.flush();
        }
        return target;
    }

    private static LayoutMap firstVisibleMap(LayoutModel model) {
        for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
            if (element instanceof LayoutMap map) {
                return map;
            }
        }
        return null;
    }

    private static CoordinateReferenceSystem projectCrs() throws IOException {
        Project project = LayoutRenderHosts.get().project();
        String code = project != null ? project.getProjectCRS() : null;
        if (code == null || code.isBlank()) {
            throw new IOException("CATMAP: el proyecto no tiene CRS; no se puede georreferenciar.");
        }
        try {
            return CRSDefinitions.decode(CRSDefinitions.normalizeCode(code), true);
        } catch (Exception invalid) {
            throw new IOException("CATMAP: no se pudo interpretar el CRS del proyecto: " + code,
                    invalid);
        }
    }

    private static void writeGeoTiff(GridCoverage2D coverage, File file) throws IOException {
        GeoTiffWriter writer = new GeoTiffWriter(file);
        try {
            writer.write(coverage, (GeneralParameterValue[]) null);
        } catch (IOException io) {
            throw io;
        } catch (Exception failed) {
            throw new IOException("CATMAP: no se pudo escribir el GeoTIFF.", failed);
        } finally {
            writer.dispose();
        }
    }

    /** Independent re-open of the staged file: CRS, envelope and raster size. */
    private static void verifyGeoTiff(File file, CoordinateReferenceSystem expectedCrs,
                                      ReferencedEnvelope expectedEnvelope,
                                      int expectedWidth, int expectedHeight) throws IOException {
        GeoTiffReader reader = null;
        GridCoverage2D coverage = null;
        java.io.InputStream input = null;
        try {
            // Read through a stream: the staged file has a neutral ".part"
            // suffix and Windows keeps File-backed readers locked after dispose.
            input = java.nio.file.Files.newInputStream(file.toPath());
            reader = new GeoTiffReader(input);
            coverage = reader.read(null);
            if (coverage == null) {
                throw new IOException("CATMAP: el GeoTIFF generado no se puede leer.");
            }
            if (!org.geotools.referencing.CRS.equalsIgnoreMetadata(
                    reader.getCoordinateReferenceSystem(), expectedCrs)) {
                throw new IOException("CATMAP: el GeoTIFF no conserva el CRS del proyecto.");
            }
            ReferencedEnvelope actual = new ReferencedEnvelope(
                    coverage.getEnvelope2D(), reader.getCoordinateReferenceSystem());
            double toleranceX = Math.max(1.0e-6, Math.abs(expectedEnvelope.getWidth()) * 1.0e-6);
            double toleranceY = Math.max(1.0e-6, Math.abs(expectedEnvelope.getHeight()) * 1.0e-6);
            if (Math.abs(actual.getMinX() - expectedEnvelope.getMinX()) > toleranceX
                    || Math.abs(actual.getMaxX() - expectedEnvelope.getMaxX()) > toleranceX
                    || Math.abs(actual.getMinY() - expectedEnvelope.getMinY()) > toleranceY
                    || Math.abs(actual.getMaxY() - expectedEnvelope.getMaxY()) > toleranceY) {
                throw new IOException("CATMAP: el GeoTIFF no conserva la extension del mapa.");
            }
            BufferedImage rendered = coverage.getRenderedImage() instanceof BufferedImage buffered
                    ? buffered : null;
            int width = rendered != null ? rendered.getWidth()
                    : coverage.getRenderedImage().getWidth();
            int height = rendered != null ? rendered.getHeight()
                    : coverage.getRenderedImage().getHeight();
            if (width != expectedWidth || height != expectedHeight) {
                throw new IOException("CATMAP: el GeoTIFF tiene dimensiones inesperadas ("
                        + width + "x" + height + " de " + expectedWidth + "x" + expectedHeight + ").");
            }
        } catch (IOException io) {
            throw io;
        } catch (Exception failed) {
            throw new IOException("CATMAP: el GeoTIFF generado no supera la validacion.", failed);
        } finally {
            if (coverage != null) {
                coverage.dispose(true);
            }
            if (reader != null) {
                reader.dispose();
            }
            if (input != null) {
                input.close();
            }
        }
    }
}
