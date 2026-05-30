/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Color;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.saig.jump.widgets.print.PrintLayoutFrame;

public interface GraphicElements {
    public static final Border selectedBorder = BorderFactory.createLineBorder(Color.RED, 1);

    public boolean isSelected();

    public void setSelected(boolean var1);

    public JComponent getGraphicElementsOnScreen();

    public JComponent getGraphicElementsForPrint();

    public void initCornerPoint();

    public void setBorder(Border var1);

    public Point[] getCornerPoint();

    public void fixerDimensions(int var1, int var2, int var3, int var4, int var5, int var6);

    public void repaint();

    public void refreshForPrintBounds();

    public void resize(int var1, int var2, int var3, int var4, int var5, int var6);

    public void zoom(int var1, int var2, int var3, int var4, int var5, int var6);

    public void zoom(float var1);

    public String getName();

    public void setName(String var1);

    public void setParent(PrintLayoutFrame var1);

    public PrintLayoutFrame getParent();

    public void setGraphicAttributes();

    public void initGraphicAttributes(PrintLayoutFrame var1);

    public void refresh();

    public Icon getIcon();

    public void dispose();

    public int getPrintX();

    public int getPrintY();

    public int getPrintWidth();

    public int getPrintHeight();

    public void setResizing(boolean var1);
}

