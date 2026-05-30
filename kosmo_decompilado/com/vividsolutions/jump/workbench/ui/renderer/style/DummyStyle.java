/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Graphics2D;
import javax.swing.Icon;
import org.saig.core.util.I18NUnsupportedOperationException;

public class DummyStyle
implements Style {
    private static DummyStyle instance = new DummyStyle();

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
    }

    @Override
    public void initialize(Layer layer) {
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    private DummyStyle() {
    }

    public static DummyStyle instance() {
        return instance;
    }
}

