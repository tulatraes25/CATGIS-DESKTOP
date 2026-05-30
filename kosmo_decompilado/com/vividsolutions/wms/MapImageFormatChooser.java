/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.wms;

import java.util.Arrays;

public class MapImageFormatChooser {
    private boolean transparencyRequired = false;
    private boolean useLossy = false;
    public static final String[][] IMAGE_FORMATS = new String[][]{{"GIF", "PNG", "JPEG"}, {"image/gif", "image/png", "image/jpeg"}};
    private String[] imageFormats = IMAGE_FORMATS[0];

    public MapImageFormatChooser() {
        this("1.0.0");
    }

    public MapImageFormatChooser(String wmsVersion) {
        if ("1.1.1".equals(wmsVersion) || "1.1.0".equals(wmsVersion) || wmsVersion.startsWith("1.3")) {
            this.imageFormats = IMAGE_FORMATS[1];
        }
    }

    public static boolean isKnownFormat(String format) {
        int i = 0;
        while (i < IMAGE_FORMATS.length) {
            int j = 0;
            while (j < IMAGE_FORMATS[i].length) {
                if (format.equals(IMAGE_FORMATS[i][j])) {
                    return true;
                }
                ++j;
            }
            ++i;
        }
        return false;
    }

    public void setTransparencyRequired(boolean transparencyRequired) {
        this.transparencyRequired = transparencyRequired;
    }

    public void setPreferLossyCompression(boolean useLossy) {
        this.useLossy = useLossy;
    }

    public String chooseFormat(String[] formats) {
        if (formats.length == 0) {
            throw new IllegalArgumentException();
        }
        String[] order = new String[3];
        if (this.transparencyRequired) {
            order[0] = this.imageFormats[1];
            order[1] = this.imageFormats[0];
            order[2] = this.imageFormats[2];
        } else if (this.useLossy) {
            order[0] = this.imageFormats[2];
            order[1] = this.imageFormats[1];
            order[2] = this.imageFormats[0];
        } else {
            order[0] = this.imageFormats[1];
            order[1] = this.imageFormats[2];
            order[2] = this.imageFormats[0];
        }
        Arrays.sort(formats);
        int i = 0;
        while (i < order.length) {
            if (Arrays.binarySearch(formats, order[i]) >= 0) {
                return order[i];
            }
            ++i;
        }
        return null;
    }
}

