/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.panels;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.saig.jump.lang.I18N;

class FiltroNadgrids
extends FileFilter {
    static final String gsb = "gsb";

    FiltroNadgrids() {
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String s = f.getName();
        int i = s.lastIndexOf(46);
        if (i > 0 && i < s.length() - 1) {
            String extension = s.substring(i + 1).toLowerCase();
            return gsb.equals(extension);
        }
        return false;
    }

    @Override
    public String getDescription() {
        return I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.gsb-files");
    }
}

