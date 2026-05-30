/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class WorkbenchFileFilter
extends FileFilter {
    private String description;

    public WorkbenchFileFilter(String fileType) {
        this.description = fileType;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String extension = GUIUtil.getExtension(file);
        if (extension != null) {
            if (this.description.equals("JCS GML")) {
                return extension.equals("jml") || extension.equals("zip") || extension.equals("gz");
            }
            if (this.description.equals("XML")) {
                return extension.equals("xml") || extension.equals("zip") || extension.equals("gz");
            }
            if (this.description.equals("ESRI Shapefile")) {
                return extension.equals("shp") || extension.equals("zip");
            }
            if (this.description.equals("SHX")) {
                return extension.equals("shx");
            }
            if (this.description.equals("DBF")) {
                return extension.equals("dbf");
            }
            if (this.description.equals("GML")) {
                return extension.equals("gml") || extension.equals("fme") || extension.equals("zip") || extension.equals("gz");
            }
            if (this.description.equals(GUIUtil.wktDesc)) {
                return extension.equals("wkt") || extension.equals("zip") || extension.equals("gz");
            }
            if (this.description.equals("FME GML")) {
                return extension.equals("xml") || extension.equals("fme") || extension.equals("zip") || extension.equals("gz");
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}

