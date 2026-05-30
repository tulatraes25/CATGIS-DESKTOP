/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.saig.jump.lang.I18N;

public class DelegatingCompressedFileHandler
implements JUMPReader {
    private Collection<String> endings;
    private JUMPReader reader;

    public DelegatingCompressedFileHandler(JUMPReader reader, Collection<String> endings) {
        this.reader = reader;
        this.endings = new ArrayList<String>(endings);
    }

    @Override
    public FeatureCollection read(DriverProperties dp) throws Exception {
        this.mangle(dp, "File", I18N.getString("com.vividsolutions.jump.io.datasource.DelegatingCompressedFileHandler.compressedfile"), this.endings);
        return this.reader.read(dp);
    }

    protected void mangle(DriverProperties dp, String fileProperty, String compressedFileProperty, Collection<String> myEndings) throws Exception {
        if (GUIUtil.getExtension(new File(dp.getProperty(fileProperty))).equalsIgnoreCase("zip")) {
            String internalName = null;
            Iterator<String> i = myEndings.iterator();
            while (internalName == null && i.hasNext()) {
                String ending = i.next();
                internalName = CompressedFile.getInternalZipFnameByExtension(ending, dp.getProperty(fileProperty));
            }
            if (internalName == null) {
                throw new Exception("Couldnt find a " + StringUtil.toCommaDelimitedString(myEndings) + " file inside the .zip file: " + dp.getProperty(fileProperty));
            }
            dp.set(compressedFileProperty, dp.getProperty(fileProperty));
            dp.set(fileProperty, internalName);
        } else if (GUIUtil.getExtension(new File(dp.getProperty(fileProperty))).equalsIgnoreCase("gz")) {
            dp.set(compressedFileProperty, dp.getProperty(fileProperty));
        }
    }
}

