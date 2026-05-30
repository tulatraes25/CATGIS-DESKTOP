/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.objectplanet.image.PngEncoder
 */
package org.saig.core.util;

import com.objectplanet.image.PngEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class ImageUtils {
    public static void writeBufferedImageAsJPEG(OutputStream out, float quality, BufferedImage image) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Null 'out' argument.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(quality, true);
        encoder.encode(image, param);
    }

    public static void writeBufferedImageAsPNG(OutputStream out, Image image, boolean transparent, int compressionLevel) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Null 'out' argument.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        if (compressionLevel >= 10) {
            throw new IllegalArgumentException("Non valid 'compressionLevel' argument (1-9).");
        }
        int colorMode = transparent ? 6 : 2;
        PngEncoder pngEncoder = new PngEncoder(colorMode, compressionLevel);
        pngEncoder.encode(image, out);
    }

    public static void writeBufferedImageAsBMP(OutputStream out, BufferedImage image) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Null 'out' argument.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        ImageIO.write((RenderedImage)image, "bmp", out);
    }
}

