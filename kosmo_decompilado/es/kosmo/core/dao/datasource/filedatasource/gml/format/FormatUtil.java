/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.gml.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FormatUtil {
    private static Map<String, String> cache = new HashMap<String, String>();

    public static String getFormat(String filename) {
        String format = cache.get(filename);
        if (format == null) {
            try {
                format = FormatUtil.loadResource(filename);
            }
            catch (IOException e) {
                format = null;
            }
        }
        return format;
    }

    private static String loadResource(String filename) throws IOException {
        String line;
        InputStream is = FormatUtil.class.getResourceAsStream(filename);
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder xml = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            xml.append(line);
        }
        rd.close();
        String format = xml.toString();
        return format;
    }
}

