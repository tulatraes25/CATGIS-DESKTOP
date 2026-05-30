/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.model.LayerManager;
import java.awt.Component;
import javax.swing.Icon;

public interface ILeftTabTaskFrameComponent {
    public String getName();

    public Icon getIcon();

    public Component getComponent(LayerManager var1);
}

