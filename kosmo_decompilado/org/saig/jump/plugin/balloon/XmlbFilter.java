/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.StringUtil;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class XmlbFilter
extends FileFilter {
    @Override
    public boolean accept(File f) {
        String extension = FileUtil.getExtension(f);
        if (!StringUtil.isEmpty(extension)) {
            return extension.toUpperCase().equals("XMLB");
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "*.xmlb - Kosmo Balloon Files";
    }
}

