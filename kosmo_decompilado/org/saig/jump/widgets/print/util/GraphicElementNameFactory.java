/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.util;

import java.util.Iterator;
import java.util.List;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.image.ImageFrame;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.north.NorthFrame;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;
import org.saig.jump.widgets.print.elements.text.GraphicText;

public class GraphicElementNameFactory {
    int legendCont = 1;
    int mapCont = 1;
    int northCont = 1;
    int textCont = 1;
    int imageCont = 1;
    int scaleCont = 1;
    int unknowCont = 1;

    public String generateName(Class<? extends GraphicElements> graphicElementClass, List<GraphicElements> graphicElements) {
        String candidateName = "";
        candidateName = graphicElementClass.equals(LegendFrame.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.legend")) + " - " + this.legendCont++ : (graphicElementClass.equals(MapFrame.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.view")) + " - " + this.mapCont++ : (graphicElementClass.equals(NorthFrame.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.north")) + " - " + this.northCont++ : (graphicElementClass.equals(GraphicText.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.text")) + " - " + this.textCont++ : (graphicElementClass.equals(ImageFrame.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.image")) + " - " + this.imageCont++ : (graphicElementClass.equals(ScaleFrame.class) ? String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.scale")) + " - " + this.imageCont++ : String.valueOf(I18N.getString("org.saig.jump.widgets.print.util.GraphicElementNameFactory.unknow")) + " - " + this.unknowCont++)))));
        return this.uniqueGraphicElementName(candidateName, graphicElements);
    }

    private String uniqueGraphicElementName(String candidateName, List<GraphicElements> graphicElements) {
        String newName;
        if (!this.isExistingName(candidateName, graphicElements)) {
            return candidateName;
        }
        int i = 2;
        do {
            newName = String.valueOf(candidateName.substring(0, candidateName.lastIndexOf("-") + 2)) + i;
            ++i;
        } while (this.isExistingName(newName, graphicElements));
        return newName;
    }

    private boolean isExistingName(String candidateName, List<GraphicElements> graphicElements) {
        boolean solucion = false;
        Iterator<GraphicElements> iter = graphicElements.iterator();
        while (iter.hasNext() && !solucion) {
            GraphicElements element = iter.next();
            if (!element.getName().equals(candidateName)) continue;
            solucion = true;
        }
        return solucion;
    }
}

