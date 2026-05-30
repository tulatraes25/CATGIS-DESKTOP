/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Insets;
import javax.swing.JButton;

public class ColorButton
extends JButton {
    private static final long serialVersionUID = 1L;
    private final Insets margins = new Insets(0, 0, 0, 0);

    public ColorButton(String text) {
        this.setIcon(IconLoader.icon("color_wheel.png"));
        this.setActionCommand(text);
        this.setToolTipText(text);
        this.setMargin(this.margins);
        this.setVerticalTextPosition(3);
        this.setHorizontalTextPosition(0);
    }
}

