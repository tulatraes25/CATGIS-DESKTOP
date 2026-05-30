/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.geotools.util.SoftValueHashMap;
import org.saig.core.filter.Expression;
import org.saig.core.renderer.style.ExternalGraphicFactory;

public class ImageGraphicFactory
implements ExternalGraphicFactory {
    private static final Logger LOGGER = Logger.getLogger(ImageGraphicFactory.class);
    static Map<URL, BufferedImage> imageCache = Collections.synchronizedMap(new SoftValueHashMap());
    static Set<String> supportedGraphicFormats = new HashSet<String>(Arrays.asList(ImageIO.getReaderMIMETypes()));

    @Override
    public Icon getIcon(Feature feature, Expression url, String format, int size) throws Exception {
        if (!supportedGraphicFormats.contains(format.toLowerCase())) {
            return null;
        }
        URL location = this.toURL(url.getValue(feature));
        if (location == null) {
            throw new IllegalArgumentException("The provided expression cannot be evaluated to a URL");
        }
        BufferedImage image = imageCache.get(location);
        if (image == null) {
            image = ImageIO.read(location);
            imageCache.put(location, image);
        }
        if (size > 0 && image.getHeight() != size) {
            double scaleY;
            double dsize = size;
            double scaleX = scaleY = dsize / (double)image.getHeight();
            AffineTransform scaleTx = AffineTransform.getScaleInstance(scaleX, scaleY);
            AffineTransformOp ato = new AffineTransformOp(scaleTx, 2);
            image = ato.filter(image, null);
        }
        return new ImageIcon(image);
    }

    private URL toURL(Object source) {
        String s = (String)source;
        try {
            return new URL(s);
        }
        catch (MalformedURLException e1) {
            File f = new File(s);
            try {
                return f.toURI().toURL();
            }
            catch (MalformedURLException malformedURLException) {
                return null;
            }
        }
    }

    public Set<String> getSupportedMimeTypes() {
        return Collections.unmodifiableSet(supportedGraphicFormats);
    }

    public static void resetCache() {
        imageCache.clear();
    }
}

