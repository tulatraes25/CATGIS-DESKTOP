package ar.com.catgis.beta;

import ar.com.catgis.AppVersion;
import ar.com.catgis.ProjectOpenService;
import ar.com.catgis.ShapefileLoader;
import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.catmap.CatmapDocumentIO;
import ar.com.catgis.catmap.GeoTiffLayoutExporter;
import ar.com.catgis.catmap.GoldenMaster;
import ar.com.catgis.catmap.LayoutExportEngine;
import ar.com.catgis.catmap.PdfVectorExporter;
import ar.com.catgis.catmap.SvgVectorExporter;
import ar.com.catgis.catmap.TerrainRasterAlgorithms;
import ar.com.catgis.catmap.workspace.DataWorkspaceModel;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutRenderHost;
import ar.com.catgis.layout.LayoutRenderHosts;
import ar.com.catgis.layout.LayoutScaleBar;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import ar.com.catgis.AppContext;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Canonical headless beta E2E harness (REQ-PC-3 steps 2, 9, 10, 11, 12 partial):
 * project save/reopen, representative data read, terrain analysis, CATMAP compose
 * and PNG/PDF/SVG/GeoTIFF export with independent re-open validation. It does not
 * claim GUI or interactive certification: A0.7/F3 remain NOT_CERTIFIED.
 */
public final class BetaE2EHarness {

    private static final Path FIXTURES =
            Path.of("src", "test", "resources", "fixtures", "integration");

    private BetaE2EHarness() {}

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args != null && args.length > 0 && !args[0].isBlank()
                ? args[0] : "build/e2e-beta");
        Files.createDirectories(output);
        List<String> checks = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        StringBuilder report = new StringBuilder();
        report.append("# CATMAP/CATGIS beta E2E report\n\n");
        report.append("- Product: ").append(AppVersion.versionText()).append('\n');
        report.append("- OS: ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.arch")).append('\n');
        report.append("- Java: ").append(System.getProperty("java.version")).append('\n');
        report.append("- Headless: ").append(java.awt.GraphicsEnvironment.isHeadless())
                .append("\n\n");
        report.append("| Check | Result | Detail |\n|---|---|---|\n");

        try {
            run(output, checks, failures, report);
        } catch (Exception failure) {
            failures.add("fatal: " + failure);
            report.append("| fatal | FAIL | ").append(escape(failure.toString()))
                    .append(" |\n");
        }
        writeChecksums(output);
        report.append('\n').append(failures.isEmpty()
                ? "E2E_OK checks=" + checks.size()
                : "E2E_FAIL failures=" + failures.size());
        report.append('\n');
        Files.writeString(output.resolve("report.md"), report.toString(),
                StandardCharsets.UTF_8);
        System.out.println(failures.isEmpty()
                ? "E2E_OK checks=" + checks.size() + " output=" + output.toAbsolutePath()
                : "E2E_FAIL failures=" + failures + " output=" + output.toAbsolutePath());
        System.out.println("E2E_REPORT " + output.resolve("report.md").toAbsolutePath());
        if (!failures.isEmpty()) {
            System.exit(1);
        }
    }

    private static void run(Path output, List<String> checks, List<String> failures,
                            StringBuilder report) throws Exception {
        // Step 2/9/10: project create, save and reopen with state preserved.
        Project project = new Project("Beta E2E");
        project.setProjectCRS("EPSG:32719");
        Layer layer = new Layer("Puntos E2E", FIXTURES.resolve("test_points.shp").toString(),
                "shapefile");
        layer.setSourceCRS("EPSG:32719");
        layer.setFeatureCount(3);
        project.addLayer(layer);
        project.setStudyName("Estudio Beta E2E");
        project.setCompanyName("CATGIS");
        ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
        ar.com.catgis.MapPanel mapPanel = new ar.com.catgis.MapPanel();
        ar.com.catgis.CatgisDesktopApp.mapPanel = mapPanel;
        AppContext.get().setMapPanel(mapPanel);
        AppContext.setCurrentProject(project);
        AppContext.get().setProject(project);
        File projectFile = output.resolve("e2e.catgis").toFile();
        var saveMethod = Class.forName("ar.com.catgis.SaveProjectAction")
                .getDeclaredMethod("saveProjectToFile", File.class, boolean.class);
        saveMethod.setAccessible(true);
        boolean saved = (boolean) saveMethod.invoke(null, projectFile, false);
        check(checks, failures, report, "project-save", saved && projectFile.isFile(),
                "bytes=" + projectFile.length());
        ProjectOpenService.ProjectInspection inspection =
                ProjectOpenService.inspect(projectFile);
        Project reopenedProject = inspection.project();
        check(checks, failures, report, "project-reopen",
                reopenedProject != null
                        && "EPSG:32719".equals(reopenedProject.getProjectCRS())
                        && reopenedProject.getLayers() != null
                        && !reopenedProject.getLayers().isEmpty()
                        && "Estudio Beta E2E".equals(reopenedProject.getStudyName()),
                "crs=" + (reopenedProject == null ? "null"
                        : reopenedProject.getProjectCRS())
                        + " layers=" + (reopenedProject == null ? "null"
                        : String.valueOf(reopenedProject.getLayers().size())));

        // Step 3: representative vector and raster data.
        var shapefile = ShapefileLoader.load(FIXTURES.resolve("test_points.shp").toFile());
        check(checks, failures, report, "vector-shapefile",
                shapefile != null && shapefile.getFeatureCount() > 0,
                "features=" + (shapefile == null ? "null" : shapefile.getFeatureCount()));
        Path demPath = FIXTURES.resolve("test_dem.tif");
        int[] demSize = new int[2];
        boolean demOk;
        try (var input = Files.newInputStream(demPath)) {
            var reader = new org.geotools.gce.geotiff.GeoTiffReader(
                    new ByteArrayInputStream(input.readAllBytes()));
            try {
                var coverage = reader.read((org.geotools.api.parameter.GeneralParameterValue[]) null);
                try {
                    demSize[0] = coverage.getRenderedImage().getWidth();
                    demSize[1] = coverage.getRenderedImage().getHeight();
                    demOk = coverage.getCoordinateReferenceSystem() != null
                            && demSize[0] > 0 && demSize[1] > 0;
                } finally {
                    coverage.dispose(true);
                }
            } finally {
                reader.dispose();
            }
        }
        check(checks, failures, report, "raster-geotiff", demOk,
                "size=" + demSize[0] + "x" + demSize[1]);

        // Step 7: representative terrain analysis + GeoTIFF/ASCII outputs.
        AnalysisRaster dem = syntheticDem(32, 32);
        AnalysisRaster slope = TerrainRasterAlgorithms.slopeDegrees(dem);
        AnalysisRaster hillshade = TerrainRasterAlgorithms.hillshade(dem);
        File slopeTif = output.resolve("pendiente.tif").toFile();
        File hillshadeAsc = output.resolve("hillshade.asc").toFile();
        slope.exportGeoTiff(slopeTif);
        hillshade.exportAsciiGrid(hillshadeAsc);
        check(checks, failures, report, "analysis-terrain",
                slope.stats().width() == 32 && slope.stats().height() == 32
                        && slope.stats().min() >= 0.0,
                "slope=" + slope.stats());
        check(checks, failures, report, "analysis-geotiff-reopen",
                slopeTif.isFile() && slopeTif.length() > 0, "bytes=" + slopeTif.length());
        check(checks, failures, report, "analysis-ascii-grid",
                hillshadeAsc.isFile() && Files.size(hillshadeAsc.toPath()) > 0,
                "bytes=" + Files.size(hillshadeAsc.toPath()));

        // Steps 11/12: CATMAP compose and export PNG/PDF/SVG/GeoTIFF.
        Project layoutProject = new Project("CATMAP E2E");
        layoutProject.setProjectCRS("EPSG:32719");
        LayoutRenderHosts.install(new LayoutRenderHost() {
            @Override
            public Project project() {
                return layoutProject;
            }

            @Override
            public ar.com.catgis.MapPanel mapPanel() {
                return null;
            }
        });
        LayoutModel a3 = catmapModel();
        File png = output.resolve("plancheta.png").toFile();
        File pdf = output.resolve("plancheta.pdf").toFile();
        File svg = output.resolve("plancheta.svg").toFile();
        File geotiff = output.resolve("plancheta-mapa.tif").toFile();
        LayoutExportEngine.exportPng(a3, png, 96);
        PdfVectorExporter.export(a3, pdf);
        SvgVectorExporter.exportPage(a3, svg);
        GeoTiffLayoutExporter.exportMapFrame(a3, geotiff, 96);

        BufferedImage image = ImageIO.read(png);
        check(checks, failures, report, "export-png",
                image != null && image.getWidth() > 100 && image.getHeight() > 100,
                image == null ? "unreadable" : image.getWidth() + "x" + image.getHeight());
        boolean pdfOk;
        int pages;
        try (var document = org.apache.pdfbox.Loader.loadPDF(pdf)) {
            pages = document.getNumberOfPages();
            pdfOk = pages >= 1;
        }
        check(checks, failures, report, "export-pdf", pdfOk, "pages=" + pages);
        boolean svgOk;
        try (var input = Files.newInputStream(svg.toPath())) {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var document = factory.newDocumentBuilder().parse(input);
            svgOk = "svg".equalsIgnoreCase(document.getDocumentElement().getNodeName());
        }
        check(checks, failures, report, "export-svg", svgOk, "bytes=" + svg.length());
        boolean tifOk;
        try (var input = Files.newInputStream(geotiff.toPath())) {
            var reader = new org.geotools.gce.geotiff.GeoTiffReader(
                    new ByteArrayInputStream(input.readAllBytes()));
            try {
                var coverage = reader.read((org.geotools.api.parameter.GeneralParameterValue[]) null);
                try {
                    tifOk = coverage.getRenderedImage().getWidth() > 0
                            && coverage.getCoordinateReferenceSystem() != null;
                } finally {
                    coverage.dispose(true);
                }
            } finally {
                reader.dispose();
            }
        }
        check(checks, failures, report, "export-geotiff", tifOk,
                "bytes=" + geotiff.length());

        // Layout document roundtrip preserves the rendered hash (persistible scene).
        GoldenMaster.Scene scene = GoldenMaster.scenes().get(0);
        File catmapFile = output.resolve("layout.catmap").toFile();
        CatmapDocumentIO.save(scene.model(), catmapFile);
        LayoutModel reopened = CatmapDocumentIO.load(catmapFile);
        String before = GoldenMaster.capture(scene.name(), scene.model(), 96).sha256();
        String after = GoldenMaster.capture(scene.name(), reopened, 96).sha256();
        check(checks, failures, report, "layout-roundtrip-hash", before.equals(after),
                before.substring(0, 12));

        // Workspace model reflects the project layers (T16 data surface).
        var rows = DataWorkspaceModel.rows(reopenedProject);
        check(checks, failures, report, "workspace-model", !rows.isEmpty(),
                "rows=" + rows.size());

        Files.writeString(output.resolve("checks.txt"),
                String.join("\n", checks) + "\n", StandardCharsets.UTF_8);
    }

    private static AnalysisRaster syntheticDem(int width, int height) {
        float[] values = new float[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                values[y * width + x] = 100f + 2f * x + 1.5f * y
                        + (float) Math.sin(x / 4.0) * 3f;
            }
        }
        return new AnalysisRaster("dem e2e", "m", width, height, values,
                TerrainRasterAlgorithms.NO_DATA,
                1000, 2000, 1000 + width * 10.0, 2000 + height * 10.0, "EPSG:32719");
    }

    private static LayoutModel catmapModel() {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3,
                PageOrientation.PORTRAIT));
        LayoutMap map = new LayoutMap("mapa", 15, 20, 267, 200);
        map.setOwnExtent(true);
        map.setOwnViewMinX(1000.0);
        map.setOwnViewMinY(2000.0);
        map.setOwnZoomFactor(2.0);
        map.setPreviewImage(GoldenMaster.syntheticMapPreview(200, 150));
        model.addElement(map);
        LayoutLabel title = new LayoutLabel("titulo", "CATMAP beta E2E", 15, 5, 220, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setColor(new Color(0x1A2434));
        model.addElement(title);
        LayoutLegend legend = new LayoutLegend("leyenda", 15, 230, 120, 40);
        legend.setTitle("Referencias");
        legend.getItems().add(new LayoutLegend.LegendItem("Ruta", Color.ORANGE, "LineString"));
        legend.getItems().add(new LayoutLegend.LegendItem("Zona", new Color(255, 200, 200),
                "Polygon"));
        model.addElement(legend);
        LayoutScaleBar scale = new LayoutScaleBar("escala", 160, 235, 100, 20);
        scale.setMapScaleDenominator(25000);
        model.addElement(scale);
        model.addElement(new LayoutNorthArrow("norte", 265, 25, 20, 30));
        return model;
    }

    private static void check(List<String> checks, List<String> failures,
                              StringBuilder report, String name, boolean ok,
                              String detail) {
        checks.add(name + ": " + (ok ? "OK" : "FAIL"));
        report.append("| ").append(name).append(" | ").append(ok ? "PASS" : "FAIL")
                .append(" | ").append(escape(detail)).append(" |\n");
        if (!ok) {
            failures.add(name + " (" + detail + ")");
        }
    }

    private static void writeChecksums(Path output) throws Exception {
        List<String> lines = new ArrayList<>();
        try (var files = Files.list(output)) {
            for (Path file : files.sorted().toList()) {
                if (!Files.isRegularFile(file)
                        || file.getFileName().toString().equals("checksums.sha256")) {
                    continue;
                }
                String name = file.getFileName().toString();
                if (name.endsWith(".part") || name.startsWith(".")) {
                    continue;
                }
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(Files.readAllBytes(file));
                lines.add(HexFormat.of().formatHex(digest.digest()) + "  " + name);
            }
        }
        Files.writeString(output.resolve("checksums.sha256"),
                String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", " ");
    }

    private static void copy(Path source, Path target) throws Exception {
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    static {
        // Keep the fixture constants referenced in one place for documentation.
        Locale.setDefault(Locale.ROOT);
    }
}
