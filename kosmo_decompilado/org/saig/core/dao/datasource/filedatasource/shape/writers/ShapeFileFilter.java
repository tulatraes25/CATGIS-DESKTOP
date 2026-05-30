/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.FileFilter;

public class ShapeFileFilter
implements FileFilter {
    private String shpPathWithoutExtension;

    public ShapeFileFilter(String shpPath) {
        this.shpPathWithoutExtension = FileUtil.nameWithoutExtension(shpPath);
    }

    @Override
    public boolean accept(File pathname) {
        String strend = FileUtil.nameWithoutExtension(pathname.getAbsolutePath());
        return strend.equals(this.shpPathWithoutExtension);
    }
}

