/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.saig.core.dao.coverage;

import java.io.File;
import org.cresques.cts.IProjection;
import org.saig.core.dao.coverage.GvSIGImageDataAccesor;
import org.saig.core.dao.coverage.ImageDataAccesor;

public class ImageDataAccesorFactory {
    public static ImageDataAccesor createDataAccesor(File file, IProjection proj) throws Exception {
        GvSIGImageDataAccesor dataAccesor = new GvSIGImageDataAccesor(file.getAbsolutePath(), proj);
        return dataAccesor;
    }

    public static ImageDataAccesor createDataAccesor(File[] files, IProjection proj) throws Exception {
        String[] filePaths = new String[files.length];
        int i = 0;
        while (i < files.length) {
            filePaths[i] = files[i].getAbsolutePath();
            ++i;
        }
        GvSIGImageDataAccesor dataAccesor = new GvSIGImageDataAccesor(filePaths, proj);
        return dataAccesor;
    }
}

