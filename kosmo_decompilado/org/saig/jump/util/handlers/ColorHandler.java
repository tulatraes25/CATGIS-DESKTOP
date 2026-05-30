/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.GeneralizedFieldHandler
 */
package org.saig.jump.util.handlers;

import com.vividsolutions.jump.util.StringUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class ColorHandler
extends GeneralizedFieldHandler {
    public Object convertUponGet(Object arg) {
        Color color = (Color)arg;
        if (color != null) {
            ArrayList<Integer> parameters = new ArrayList<Integer>();
            parameters.add(new Integer(color.getRed()));
            parameters.add(new Integer(color.getGreen()));
            parameters.add(new Integer(color.getBlue()));
            parameters.add(new Integer(color.getAlpha()));
            return StringUtil.toCommaDelimitedString(parameters);
        }
        return null;
    }

    public Object convertUponSet(Object arg) {
        List<String> parameters = StringUtil.fromCommaDelimitedString((String)arg);
        return new Color(Integer.parseInt(parameters.get(0)), Integer.parseInt(parameters.get(1)), Integer.parseInt(parameters.get(2)), Integer.parseInt(parameters.get(3)));
    }

    public Class<?> getFieldType() {
        return Color.class;
    }
}

