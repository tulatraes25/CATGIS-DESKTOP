/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigViewDataPanel;

public class NumberFormatManager {
    private static final int MAX_FRACTION_DIGITS = 15;
    private static NumberFormat localeNumberFormat = NumberFormat.getInstance(I18N.getLocale());

    static {
        if (localeNumberFormat.getMaximumFractionDigits() < 15) {
            localeNumberFormat.setMaximumFractionDigits(15);
        }
    }

    private NumberFormatManager() {
    }

    public static NumberFormat getDefaultNumberFormat() {
        DecimalFormat formatter = (DecimalFormat)localeNumberFormat;
        if (PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigViewDataPanel.KEY_NUMBER_OF_DECIMALS_ENABLED, false)) {
            String pattern = "##0";
            int numbersOfDecimal = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get("KEY_NUMBER_OF_DECIMALS", 2);
            int i = 0;
            while (i < numbersOfDecimal) {
                pattern = i == 0 ? String.valueOf(pattern) + ".#" : String.valueOf(pattern) + "#";
                ++i;
            }
            formatter.applyPattern(pattern);
        }
        return formatter;
    }

    public static String getFormattedValue(Number value) {
        if (value == null) {
            return "";
        }
        NumberFormat formatter = NumberFormatManager.getDefaultNumberFormat();
        if (!(value instanceof Integer || value instanceof Long || value instanceof BigDecimal || value instanceof Short || !PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigViewDataPanel.KEY_NUMBER_OF_DECIMALS_ENABLED, false))) {
            int numbersOfDecimal = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get("KEY_NUMBER_OF_DECIMALS", 2);
            formatter.setMinimumFractionDigits(numbersOfDecimal);
        } else {
            formatter.setMinimumFractionDigits(0);
        }
        return formatter.format(value.doubleValue());
    }

    public static NumberFormat getMinNumDigitsNumberFormat(int minNumDigits) {
        DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(I18N.getLocale());
        String pattern = "";
        int i = 0;
        while (i < minNumDigits) {
            pattern = String.valueOf(pattern) + "0";
            ++i;
        }
        formatter.applyPattern(pattern);
        return formatter;
    }
}

