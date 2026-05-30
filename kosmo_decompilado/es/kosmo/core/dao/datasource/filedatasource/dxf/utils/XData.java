/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf.utils;

import com.vividsolutions.jump.feature.AttributeType;
import java.text.NumberFormat;
import java.util.Locale;

public class XData {
    public static final int XDATE_APPID_VALUE_CODE = 1001;
    public static final int XDATA_INT_VALUE_CODE = 1070;
    public static final int XDATA_LONG_VALUE_CODE = 1071;
    public static final int XDATA_REAL_VALUE_CODE = 1040;
    public static final int XDATA_STRING_VALUE_CODE = 1000;
    public static final int XDATA_CONTROL_VALUE_CODE = 1002;
    private static NumberFormat format = NumberFormat.getInstance(Locale.US);

    static {
        format.setMaximumFractionDigits(12);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(1);
    }

    public static String toDXFValue(int groupCode, Object value) {
        String result = "";
        if (value != null) {
            switch (groupCode) {
                case 1070: {
                    Number integerNumber = (Number)value;
                    result = String.valueOf(result) + integerNumber.intValue();
                    break;
                }
                case 1071: {
                    Number longNumber = (Number)value;
                    result = String.valueOf(result) + longNumber.longValue();
                    break;
                }
                case 1040: {
                    Number realNumber = (Number)value;
                    result = String.valueOf(result) + format.format(realNumber.doubleValue());
                    break;
                }
                default: {
                    result = value.toString();
                }
            }
        }
        return result;
    }

    public static int toDXFGroupCode(AttributeType type) {
        if (type.equals(AttributeType.INTEGER)) {
            return 1070;
        }
        if (type.equals(AttributeType.LONG)) {
            return 1071;
        }
        if (type.equals(AttributeType.DOUBLE) || type.equals(AttributeType.BIGDECIMAL)) {
            return 1040;
        }
        return 1000;
    }
}

