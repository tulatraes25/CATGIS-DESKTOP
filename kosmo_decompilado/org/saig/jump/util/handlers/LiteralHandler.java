/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.GeneralizedFieldHandler
 */
package org.saig.jump.util.handlers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.util.StringUtil;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.GeneralizedFieldHandler;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.util.DateFormatManager;

public class LiteralHandler
extends GeneralizedFieldHandler {
    private static final Logger LOGGER = Logger.getLogger(LiteralHandler.class);

    public Object convertUponGet(Object value) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        if (value instanceof Geometry) {
            parameters.add(new Short(104));
            parameters.add(((Geometry)value).toText());
        } else if (value instanceof Double) {
            parameters.add(new Short(101));
            parameters.add(value.toString());
        } else if (value instanceof Integer) {
            parameters.add(new Short(102));
            parameters.add(value.toString());
        } else if (value instanceof Long) {
            parameters.add(new Short(99));
            parameters.add(value.toString());
        } else if (value instanceof Timestamp || value instanceof Time) {
            parameters.add(new Short(103));
            parameters.add(DateFormatManager.getDateTimeFormat().format(value));
        } else if (value instanceof Date) {
            parameters.add(new Short(103));
            parameters.add(DateFormatManager.getDateFormat().format(value));
        } else if (value instanceof Boolean) {
            parameters.add(new Short(103));
            parameters.add(value.toString());
        } else if (value instanceof String) {
            parameters.add(new Short(103));
            parameters.add(value.toString());
        } else if (value instanceof BigDecimal) {
            parameters.add(new Short(101));
            parameters.add(value.toString());
        } else if (value instanceof Short) {
            parameters.add(new Short(102));
            parameters.add(value.toString());
        } else if (value instanceof Float) {
            parameters.add(new Short(101));
            parameters.add(value.toString());
        } else {
            LOGGER.warn((Object)("Unknown type " + value.getClass().getName()));
        }
        return StringUtil.toPercentDelimitedString(parameters);
    }

    public Object convertUponSet(Object value) {
        List<String> parameters = StringUtil.fromPercentDelimitedString((String)value);
        if (CollectionUtils.isEmpty(parameters)) {
            return value;
        }
        Object solucion = null;
        short expressionType = Short.parseShort(parameters.get(0));
        String literalValue = parameters.get(1);
        switch (expressionType) {
            case 104: {
                try {
                    solucion = new WKTReader().read(literalValue);
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                break;
            }
            case 101: {
                solucion = Double.valueOf(literalValue);
                break;
            }
            case 102: {
                solucion = Integer.valueOf(literalValue);
                break;
            }
            case 99: {
                solucion = Long.valueOf(literalValue);
                break;
            }
            case 103: {
                solucion = literalValue;
            }
        }
        return solucion;
    }

    public Class<?> getFieldType() {
        return LiteralExpressionImpl.class;
    }
}

