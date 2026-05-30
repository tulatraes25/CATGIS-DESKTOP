/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.batik.transcoder.TranscoderOutput
 *  org.apache.batik.transcoder.image.ImageTranscoder
 */
package org.saig.core.renderer.lite;

import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class InternalTranscoder
extends ImageTranscoder {
    private BufferedImage result;

    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, 2);
    }

    public void writeImage(BufferedImage img, TranscoderOutput output) {
        this.result = img;
    }

    public BufferedImage getImage() {
        return this.result;
    }
}

