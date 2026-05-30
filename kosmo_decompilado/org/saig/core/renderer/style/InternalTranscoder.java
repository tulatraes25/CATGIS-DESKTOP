/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.batik.transcoder.TranscoderException
 *  org.apache.batik.transcoder.TranscoderOutput
 *  org.apache.batik.transcoder.image.ImageTranscoder
 */
package org.saig.core.renderer.style;

import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.Document;

final class InternalTranscoder
extends ImageTranscoder {
    private BufferedImage result;
    private Document doc;

    protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
        super.transcode(document, uri, output);
        this.doc = document;
    }

    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, 2);
    }

    public void writeImage(BufferedImage img, TranscoderOutput output) {
        this.result = img;
    }

    public BufferedImage getImage() {
        return this.result;
    }

    public Document getDocument() {
        return this.doc;
    }
}

