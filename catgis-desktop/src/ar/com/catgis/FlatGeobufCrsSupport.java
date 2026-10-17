package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Header;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/** Truthful CRS extraction from the FlatGeobuf header. */
final class FlatGeobufCrsSupport {

    record Resolution(CoordinateReferenceSystem coordinateReferenceSystem,
                      String sourceCode,
                      String provenance) {
        static Resolution unknown() {
            return new Resolution(null, "", "UNKNOWN");
        }
    }

    private FlatGeobufCrsSupport() {}

    static Resolution resolve(ByteBuffer headerBuffer) {
        if (headerBuffer == null) {
            return Resolution.unknown();
        }
        try {
            ByteBuffer raw = headerBuffer.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
            return resolve(Header.getRootAsHeader(raw));
        } catch (Exception ex) {
            CatgisLogger.warn("FlatGeobuf: no se pudo interpretar el CRS del header", ex);
            return Resolution.unknown();
        }
    }

    /** Read CRS from an already structurally verified FlatGeobuf header. */
    static Resolution resolve(Header header) {
        try {
            Crs crs = header != null ? header.crs() : null;
            if (crs == null) {
                return Resolution.unknown();
            }
            return resolveMetadata(
                    crs.org(), crs.code(), optionalString(crs, "codeString"), crs.wkt());
        } catch (RuntimeException malformed) {
            CatgisLogger.warn("FlatGeobuf: CRS inválido en encabezado", malformed);
            return Resolution.unknown();
        }
    }

    static Resolution resolveMetadata(String organization,
                                      int numericCode,
                                      String stringCode,
                                      String wkt) {
        String org = organization != null ? organization.trim() : "";
        if (org.isBlank()) {
            org = "EPSG";
        }

        String codePart = numericCode > 0
                ? Integer.toString(numericCode)
                : stringCode != null ? stringCode.trim() : "";
        if (!codePart.isBlank() && org.matches("[A-Za-z][A-Za-z0-9_.-]{0,31}")
                && codePart.matches("[A-Za-z0-9_.-]{1,64}")) {
            String authorityCode = org.toUpperCase(Locale.ROOT) + ":" + codePart;
            try {
                CoordinateReferenceSystem decoded = CRS.decode(authorityCode, true);
                String normalized = sourceCode(decoded);
                return new Resolution(
                        decoded,
                        !normalized.isBlank() ? normalized : authorityCode,
                        "AUTHORITY");
            } catch (Exception ex) {
                CatgisLogger.warn(
                        "FlatGeobuf: autoridad CRS no resoluble: " + authorityCode,
                        ex);
            }
        }

        if (wkt != null && !wkt.isBlank()) {
            try {
                CoordinateReferenceSystem decoded = CRS.parseWKT(wkt);
                String normalized = sourceCode(decoded);
                return new Resolution(
                        decoded,
                        !normalized.isBlank() ? normalized : wkt.trim(),
                        "WKT");
            } catch (Exception ex) {
                CatgisLogger.warn("FlatGeobuf: WKT CRS no resoluble", ex);
            }
        }

        return Resolution.unknown();
    }

    static String schemaCode(SimpleFeatureType schema) {
        if (schema == null || schema.getCoordinateReferenceSystem() == null) {
            return "";
        }
        return sourceCode(schema.getCoordinateReferenceSystem());
    }

    private static String sourceCode(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return "";
        }
        try {
            String code = CRS.toSRS(crs, true);
            if (code != null && !code.isBlank()) {
                return CRSDefinitions.normalizeCode(code);
            }
        } catch (Exception ignored) {
            // A WKT-only CRS can intentionally have no authority identifier.
        }
        try {
            String wkt = crs.toWKT();
            return wkt != null ? wkt.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String optionalString(Crs crs, String methodName) {
        try {
            Method method = crs.getClass().getMethod(methodName);
            Object value = method.invoke(crs);
            return value != null ? value.toString() : "";
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }
}
