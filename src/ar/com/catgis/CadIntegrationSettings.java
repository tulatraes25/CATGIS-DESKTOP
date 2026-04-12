package ar.com.catgis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CadIntegrationSettings {

    private static final String KEY_CUSTOM_CONVERTER = "custom.converter.path";

    private CadIntegrationSettings() {
    }

    public static String getCustomConverterPath() {
        Properties properties = loadProperties();
        return properties.getProperty(KEY_CUSTOM_CONVERTER, "").trim();
    }

    public static void setCustomConverterPath(String path) {
        Properties properties = loadProperties();
        if (path == null || path.isBlank()) {
            properties.remove(KEY_CUSTOM_CONVERTER);
        } else {
            properties.setProperty(KEY_CUSTOM_CONVERTER, path.trim());
        }
        saveProperties(properties);
    }

    public static Path getSettingsFile() {
        String appData = System.getenv("APPDATA");
        Path base = (appData != null && !appData.isBlank())
                ? Path.of(appData, "CATGIS")
                : Path.of(System.getProperty("user.home"), ".catgis");
        return base.resolve("cad-tools.properties");
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path file = getSettingsFile();
        if (!Files.exists(file)) {
            return properties;
        }
        try (InputStream inputStream = Files.newInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static void saveProperties(Properties properties) {
        Path file = getSettingsFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream outputStream = Files.newOutputStream(file)) {
                properties.store(outputStream, "CATGIS CAD integration");
            }
        } catch (IOException ignored) {
        }
    }
}
