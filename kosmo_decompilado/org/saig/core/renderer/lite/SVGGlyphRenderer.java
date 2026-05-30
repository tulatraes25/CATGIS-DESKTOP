/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.batik.transcoder.TranscoderInput
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jump.feature.Feature;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.apache.batik.transcoder.TranscoderInput;
import org.saig.core.renderer.lite.GlyphRenderer;
import org.saig.core.renderer.lite.InternalTranscoder;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Graphic;

public class SVGGlyphRenderer
implements GlyphRenderer {
    private static final List formats = Collections.unmodifiableList(Arrays.asList("image/svg"));
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");

    @Override
    public boolean canRender(String format) {
        return format.toLowerCase().equals("image/svg");
    }

    public List getFormats() {
        return formats;
    }

    @Override
    public BufferedImage render(Graphic graphic, ExternalGraphic eg, Feature feature) {
        try {
            URL svgfile = eg.getLocation();
            InternalTranscoder magic = new InternalTranscoder();
            TranscoderInput in = new TranscoderInput(svgfile.openStream());
            magic.transcode(in, null);
            BufferedImage img = magic.getImage();
            return img;
        }
        catch (IOException mue) {
            LOGGER.warning("Unable to load external svg file, " + mue.getMessage());
            return null;
        }
        catch (Exception te) {
            LOGGER.warning("Unable to render external svg file, " + te.getMessage());
            return null;
        }
    }
}

