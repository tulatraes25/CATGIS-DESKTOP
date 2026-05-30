/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jump.feature.Feature;
import java.awt.image.BufferedImage;
import java.util.List;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Graphic;

public interface GlyphRenderer {
    public boolean canRender(String var1);

    public List<String> getFormats();

    public BufferedImage render(Graphic var1, ExternalGraphic var2, Feature var3);
}

