/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;
import org.saig.core.renderer.print.PrintRenderer;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.util.PrintWaitDialog;

public class MyPrintable
implements Printable {
    private List<GraphicElements> graphicsElements;

    public MyPrintable(List<GraphicElements> graphicsElements) {
        this.graphicsElements = graphicsElements;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0 || this.graphicsElements.size() == 0) {
            return 1;
        }
        if (!PrintWaitDialog.canceled) {
            Graphics2D g2d = (Graphics2D)graphics;
            System.out.println(g2d.getClipBounds());
            PrintRenderer print = new PrintRenderer();
            print.print(g2d, this.graphicsElements);
            return 0;
        }
        return 1;
    }
}

