/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.GeneralizedFieldHandler
 */
package org.saig.jump.util.handlers;

import com.vividsolutions.jump.util.StringUtil;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.ArrayList;
import java.util.List;
import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class PageFormatHandler
extends GeneralizedFieldHandler {
    public Object convertUponGet(Object arg) {
        ArrayList<Number> parameters = new ArrayList<Number>();
        PageFormat pf = (PageFormat)arg;
        Paper paper = pf.getPaper();
        parameters.add(new Integer(pf.getOrientation()));
        parameters.add(new Double(paper.getImageableX()));
        parameters.add(new Double(paper.getImageableY()));
        parameters.add(new Double(paper.getImageableWidth()));
        parameters.add(new Double(paper.getImageableHeight()));
        parameters.add(new Double(paper.getWidth()));
        parameters.add(new Double(paper.getHeight()));
        return StringUtil.toCommaDelimitedString(parameters);
    }

    public Object convertUponSet(Object value) {
        List<String> parameters = StringUtil.fromCommaDelimitedString((String)value);
        int orientation = Integer.parseInt(parameters.get(0));
        double imageableX = Double.parseDouble(parameters.get(1));
        double imageableY = Double.parseDouble(parameters.get(2));
        double imageableWidth = Double.parseDouble(parameters.get(3));
        double imageableHeight = Double.parseDouble(parameters.get(4));
        double width = Double.parseDouble(parameters.get(5));
        double height = Double.parseDouble(parameters.get(6));
        PageFormat pf = new PageFormat();
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(imageableX, imageableY, imageableWidth, imageableHeight);
        pf.setOrientation(orientation);
        pf.setPaper(paper);
        return pf;
    }

    public Class<?> getFieldType() {
        return PageFormat.class;
    }
}

