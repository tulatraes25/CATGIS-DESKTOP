/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.filechooser.FileFilter;
import org.saig.jump.lang.I18N;

public class ImageFilter
extends FileFilter {
    private static final Set suffixes = ImageFilter.getSuffixes();

    private static Set getSuffixes() {
        HashSet<String> suffixes = new HashSet<String>();
        Iterator<ImageReaderSpi> it = IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, false);
        while (it.hasNext()) {
            ImageReaderSpi spi = it.next();
            String[] spiSuffixes = spi.getFileSuffixes();
            int i = 0;
            while (i < spiSuffixes.length) {
                suffixes.add(spiSuffixes[i]);
                ++i;
            }
        }
        return suffixes;
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }
        String extension = ImageFilter.getExtension(pathname);
        if (extension == null) {
            return false;
        }
        return suffixes.contains(extension.toLowerCase());
    }

    @Override
    public String getDescription() {
        return I18N.getString("org.saig.core.gui.swing.sldeditor.util.ImageFilter.all-supported-images");
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf(46);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}

