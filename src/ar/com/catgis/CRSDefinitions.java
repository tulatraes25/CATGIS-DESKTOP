package ar.com.catgis;

import java.util.LinkedHashMap;
import java.util.Map;

public class CRSDefinitions {

    public static LinkedHashMap<String, String> createCRSMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        // Geográficas globales
        map.put("EPSG:4326 - WGS 84 (Lat/Long)", "EPSG:4326");
        map.put("EPSG:4258 - ETRS89 geográficas", "EPSG:4258");
        map.put("EPSG:4269 - NAD83 geográficas", "EPSG:4269");

        // Geográficas Argentina
        map.put("EPSG:4674 - POSGAR 2007 geográficas", "EPSG:4674");
        map.put("EPSG:4190 - POSGAR 94 geográficas", "EPSG:4190");
        map.put("EPSG:4221 - Campo Inchauspe geográficas", "EPSG:4221");

        // Web / mundo
        map.put("EPSG:3857 - Web Mercator", "EPSG:3857");
        map.put("EPSG:3395 - WGS 84 / World Mercator", "EPSG:3395");
        map.put("EPSG:4087 - WGS 84 / World Equidistant Cylindrical", "EPSG:4087");

        // UTM Norte frecuentes
        for (int zone = 1; zone <= 60; zone++) {
            String code = "EPSG:" + (32600 + zone);
            map.put(code + " - WGS 84 / UTM " + zone + "N", code);
        }

        // UTM Sur frecuentes
        for (int zone = 1; zone <= 60; zone++) {
            String code = "EPSG:" + (32700 + zone);
            map.put(code + " - WGS 84 / UTM " + zone + "S", code);
        }

        // POSGAR 94 Fajas Argentina 1..7
        for (int i = 1; i <= 7; i++) {
            String code = "EPSG:" + (22180 + i);
            map.put(code + " - POSGAR 94 / Argentina " + i, code);
        }

        // POSGAR 2007 Fajas Argentina 1..7
        for (int i = 1; i <= 7; i++) {
            String code = "EPSG:" + (5342 + i);
            map.put(code + " - POSGAR 2007 / Argentina " + i, code);
        }

        // Campo Inchauspe Fajas Argentina 1..7
        for (int i = 1; i <= 7; i++) {
            String code = "EPSG:" + (22190 + i);
            map.put(code + " - Campo Inchauspe / Argentina " + i, code);
        }

        // Algunos proyectados del mundo útiles
        map.put("EPSG:3035 - ETRS89 / LAEA Europe", "EPSG:3035");
        map.put("EPSG:27700 - OSGB36 / British National Grid", "EPSG:27700");
        map.put("EPSG:2154 - RGF93 / Lambert-93", "EPSG:2154");
        map.put("EPSG:102100 - Web Mercator (compatibilidad)", "EPSG:3857");

        return map;
    }

    public static String getLabelForCode(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        String normalized = normalizeCode(code);

        for (Map.Entry<String, String> entry : createCRSMap().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(normalized)) {
                return entry.getKey();
            }
        }

        return normalized;
    }

    public static String normalizeCode(String text) {
        if (text == null) {
            return "";
        }

        String value = text.trim().toUpperCase();
        if (value.isEmpty()) {
            return "";
        }

        if (value.startsWith("EPSG:")) {
            return value;
        }

        if (value.matches("\\d+")) {
            return "EPSG:" + value;
        }

        return value;
    }

    public static LinkedHashMap<String, String> filter(String searchText) {
        LinkedHashMap<String, String> all = createCRSMap();

        if (searchText == null || searchText.isBlank()) {
            return all;
        }

        String q = searchText.trim().toLowerCase();
        LinkedHashMap<String, String> filtered = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : all.entrySet()) {
            String label = entry.getKey().toLowerCase();
            String code = entry.getValue().toLowerCase();

            if (label.contains(q) || code.contains(q)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        return filtered;
    }
}