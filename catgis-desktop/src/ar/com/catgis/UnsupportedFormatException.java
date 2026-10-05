package ar.com.catgis;

/**
 * Thrown when a data file cannot be loaded because the format is
 * unsupported, the file is corrupt, or required extensions are missing.
 */
public final class UnsupportedFormatException extends Exception {

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
