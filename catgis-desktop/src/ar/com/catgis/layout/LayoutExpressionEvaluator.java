package ar.com.catgis.layout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple expression evaluator for dynamic text in LayoutLabel.
 * Supports: @scale, @date, @datetime, @project, @page, @pagetotal
 */
public final class LayoutExpressionEvaluator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String evaluate(String expression, double scaleDenominator, String projectName,
                                   int currentPage, int totalPages) {
        if (expression == null || expression.isEmpty()) return "";
        String result = expression;
        result = result.replace("@pagetotal", String.valueOf(totalPages));
        result = result.replace("@page", String.valueOf(currentPage + 1));
        result = result.replace("@scale", "1:" + String.format("%,.0f", scaleDenominator));
        result = result.replace("@datetime", LocalDateTime.now().format(DATETIME_FMT));
        result = result.replace("@date", LocalDateTime.now().format(DATE_FMT));
        result = result.replace("@project", projectName != null ? projectName : "CATGIS");
        return result;
    }

    private LayoutExpressionEvaluator() {}
}
