package ar.com.catgis;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DwgImportSupport {

    private DwgImportSupport() {
    }

    public static ResolvedCadReference resolveDwgReference(File dwgFile, Component parent, boolean showDialogs) {
        if (dwgFile == null || !dwgFile.exists()) {
            return null;
        }

        File sidecarDxf = findSidecarDxf(dwgFile);
        if (sidecarDxf != null) {
            return new ResolvedCadReference(sidecarDxf, true, false, null, buildResolutionMessage(dwgFile, sidecarDxf, true, false, null));
        }

        File converter = detectPreferredCadConverter();
        String autoConversionError = "";
        if (converter != null) {
            try {
                File converted = convertDwgWithConverter(dwgFile, converter);
                if (converted != null && converted.exists()) {
                    return new ResolvedCadReference(
                            converted,
                            false,
                            true,
                            converter,
                            buildResolutionMessage(dwgFile, converted, false, true, converter)
                    );
                }
            } catch (Exception ex) {
                autoConversionError = ex.getMessage() != null ? ex.getMessage().trim() : "";
            }
        }

        if (!showDialogs) {
            return null;
        }

        List<File> converters = findKnownCadConverters();
        StringBuilder message = new StringBuilder();
        message.append("CATGIS no tiene lectura nativa directa de DWG en esta etapa.\n\n");
        if (converter != null) {
            message.append("Se detecto un convertidor CAD oficial, pero la conversion automatica no se pudo completar");
            if (!autoConversionError.isBlank()) {
                message.append(":\n").append(autoConversionError);
            } else {
                message.append(".");
            }
            message.append("\n\n");
        }
        message.append("Para mantener un flujo CAD real, CATGIS puede trabajar con un DXF ASCII convertido desde el DWG.\n\n")
                .append("Archivo DWG:\n")
                .append(dwgFile.getAbsolutePath())
                .append("\n\n")
                .append("No se encontro un DXF gemelo con el mismo nombre en la carpeta.");

        if (!converters.isEmpty()) {
            message.append("\n\nConvertidores detectados en este equipo:");
            for (File detectedConverter : converters) {
                message.append("\n- ").append(detectedConverter.getAbsolutePath());
            }
            message.append("\n\nConverti el DWG a DXF ASCII y seleccionalo ahora.");
        } else {
            message.append("\n\nSi tenes ODA File Converter, Teigha File Converter o una exportacion desde AutoCAD/BricsCAD, converti el DWG a DXF ASCII y seleccionalo ahora.");
        }

        Object[] options = {"Buscar DXF convertido...", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                message.toString(),
                "Importacion asistida DWG",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice != JOptionPane.YES_OPTION) {
            return null;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("dwg-converted-dxf", "Seleccionar DXF convertido");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("CAD DXF (*.dxf)", "dxf"));
        chooser.setMultiSelectionEnabled(false);
        if (dwgFile.getParentFile() != null && dwgFile.getParentFile().isDirectory()) {
            chooser.setCurrentDirectory(dwgFile.getParentFile());
        }
        chooser.setSelectedFile(new File(stripExtension(dwgFile.getName()) + ".dxf"));
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.exists()) {
            return null;
        }
        FileChooserSupport.rememberSelection("dwg-converted-dxf", chooser);
        return new ResolvedCadReference(selected, false, false, null, buildResolutionMessage(dwgFile, selected, false, false, null));
    }

    private static File findSidecarDxf(File dwgFile) {
        if (dwgFile == null) {
            return null;
        }
        File parent = dwgFile.getParentFile();
        if (parent == null || !parent.isDirectory()) {
            return null;
        }
        String baseName = stripExtension(dwgFile.getName());
        File exact = new File(parent, baseName + ".dxf");
        if (exact.exists() && exact.isFile()) {
            return exact;
        }

        File[] files = parent.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String lowerName = file.getName().toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".dxf")) {
                continue;
            }
            if (stripExtension(file.getName()).equalsIgnoreCase(baseName)) {
                return file;
            }
        }
        return null;
    }

    public static File detectPreferredCadConverter() {
        List<File> converters = findKnownCadConverters();
        return converters.isEmpty() ? null : converters.get(0);
    }

    public static List<File> listAvailableCadConverters() {
        return findKnownCadConverters();
    }

    private static List<File> findKnownCadConverters() {
        Set<String> candidates = new LinkedHashSet<>();
        String customPath = CadIntegrationSettings.getCustomConverterPath();
        if (customPath != null && !customPath.isBlank()) {
            candidates.add(customPath.trim());
        }
        String path = System.getenv("PATH");
        if (path != null && !path.isBlank()) {
            String[] entries = path.split(File.pathSeparator);
            for (String entry : entries) {
                if (entry == null || entry.isBlank()) {
                    continue;
                }
                candidates.add(new File(entry, "ODAFileConverter.exe").getAbsolutePath());
                candidates.add(new File(entry, "TeighaFileConverter.exe").getAbsolutePath());
                candidates.add(new File(entry, "DWGCONVERT.exe").getAbsolutePath());
            }
        }

        String[] commonPaths = {
                "C:\\CATGIS\\tools\\oda\\app\\ODAFileConverter.exe",
                "C:\\CATGIS\\tools\\oda\\ODAFileConverter.exe",
                "C:\\Program Files\\ODA\\ODAFileConverter\\ODAFileConverter.exe",
                "C:\\Program Files\\ODA\\ODAFileConverter 25.12.0\\ODAFileConverter.exe",
                "C:\\Program Files\\ODA\\ODAFileConverter 27.1\\ODAFileConverter.exe",
                "C:\\Program Files\\ODA\\ODAFileConverter 25.9.0\\ODAFileConverter.exe",
                "C:\\Program Files\\Teigha File Converter\\TeighaFileConverter.exe",
                "C:\\Program Files\\Autodesk\\DWG TrueView\\DWGCONVERT.exe"
        };
        for (String pathCandidate : commonPaths) {
            candidates.add(pathCandidate);
        }

        List<File> found = new ArrayList<>();
        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists() && file.isFile()) {
                found.add(file);
            }
        }
        return found;
    }

    private static File convertDwgWithConverter(File dwgFile, File converter) throws Exception {
        if (dwgFile == null || converter == null || !dwgFile.exists() || !converter.exists()) {
            return null;
        }
        if (!supportsBatchConversion(converter)) {
            throw new IllegalStateException("El convertidor detectado no tiene una interfaz batch validada por CATGIS.");
        }

        Path outputDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "catgis-dwg-cache", buildCacheFolderName(dwgFile)));
        File outputDxf = outputDir.resolve(stripExtension(dwgFile.getName()) + ".dxf").toFile();
        if (outputDxf.exists() && outputDxf.isFile() && outputDxf.lastModified() >= dwgFile.lastModified()) {
            return outputDxf;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                converter.getAbsolutePath(),
                dwgFile.getParentFile().getAbsolutePath(),
                outputDir.toFile().getAbsolutePath(),
                "ACAD2018",
                "DXF",
                "0",
                "0",
                dwgFile.getName()
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String console;
        try (InputStream stream = process.getInputStream()) {
            console = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        int exitCode = process.waitFor();
        if (exitCode != 0 || !outputDxf.exists()) {
            throw new IllegalStateException("El convertidor CAD no genero el DXF esperado para " + dwgFile.getName()
                    + (console == null || console.isBlank() ? "" : "\n" + console.trim()));
        }
        return outputDxf;
    }

    private static boolean supportsBatchConversion(File converter) {
        if (converter == null) {
            return false;
        }
        String name = converter.getName().toLowerCase(Locale.ROOT);
        return name.contains("odafileconverter") || name.contains("teighafileconverter");
    }

    private static String buildCacheFolderName(File file) {
        String baseName = stripExtension(file.getName()).replaceAll("[^A-Za-z0-9._-]+", "_");
        return baseName + "_" + Math.abs(file.getAbsolutePath().toLowerCase(Locale.ROOT).hashCode());
    }

    private static String buildResolutionMessage(File dwgFile, File dxfFile, boolean sidecar, boolean autoConverted, File converter) {
        if (sidecar) {
            return "DWG asistido usando DXF gemelo: " + dxfFile.getName() + " | origen: " + dwgFile.getName();
        }
        if (autoConverted) {
            String converterName = converter != null ? converter.getName() : "convertidor CAD";
            return "DWG convertido automaticamente a DXF por CATGIS usando " + converterName
                    + ": " + dxfFile.getName() + " | origen: " + dwgFile.getName();
        }
        return "DWG asistido usando DXF convertido: " + dxfFile.getName() + " | origen: " + dwgFile.getName();
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "cad";
        }
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

    public record ResolvedCadReference(File dxfFile,
                                       boolean sidecar,
                                       boolean autoConverted,
                                       File converterFile,
                                       String resolutionMessage) {
    }
}
