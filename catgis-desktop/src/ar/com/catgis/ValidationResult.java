package ar.com.catgis;

/**
 * Result of {@code validateFile(File)} checks.
 *
 * @param isValid  true if the file passes all format-specific validation
 * @param message  human-readable status or error description
 */
public record ValidationResult(boolean isValid, String message) {

    public static ValidationResult valid(String message) {
        return new ValidationResult(true, message);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }
}
