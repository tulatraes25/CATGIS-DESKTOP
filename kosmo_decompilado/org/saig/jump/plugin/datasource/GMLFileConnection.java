/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 *  org.deegree.framework.util.CharsetUtils
 *  org.deegree.model.feature.FeatureCollection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import de.latlon.deejump.wfs.data.JUMPFeatureFactory2;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.styling.Style;

public class GMLFileConnection
implements Connection {
    private List<AbstractDataSource> dataAccesors = new ArrayList<AbstractDataSource>();

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromSelection(query, new HashMap<String, Object>());
    }

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) throws Exception {
        return this.createFeatureCollectionFromSelection(query, new HashMap<String, Object>());
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        return this.createFeatureCollectionFromSelection(query, new HashMap<String, Object>());
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromSelection(query, properties);
    }

    private void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, IProjection proj) throws Exception {
        query = FileUtil.addValidExtension(query, "gml");
        org.deegree.model.feature.FeatureCollection deegreeFC = JUMPFeatureFactory2.createFromJUMPFeatureCollection(featureCollection, proj);
        FileOutputStream fos = new FileOutputStream(query);
        new GMLFeatureAdapter().exportFC(deegreeFC, fos);
        fos.close();
    }

    public void executeUpdate(String query, FeatureCollection featureCollection, long maxNumberOfFeatures) throws Exception {
        query = FileUtil.addValidExtension(query, "gml");
        org.deegree.model.feature.FeatureCollection deegreeFC = JUMPFeatureFactory2.createFromJUMPFeatureCollection(featureCollection, maxNumberOfFeatures, null);
        FileOutputStream fos = new FileOutputStream(query);
        new GMLFeatureAdapter().exportFC(deegreeFC, fos);
        fos.close();
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes, (IProjection)null);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes, proj);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style clone, IProjection proj, TaskMonitor monitor) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes, proj);
    }

    @Override
    public void close() {
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        return this.dataAccesors;
    }

    private FeatureCollection[] createFeatureCollectionFromSelection(String selectedFile, Map<String, Object> properties) throws Exception {
        org.deegree.model.feature.FeatureCollection deegreeFC = null;
        GMLFeatureCollectionDocument gmlDoc = new GMLFeatureCollectionDocument();
        gmlDoc.load(new FileInputStream(selectedFile), "file:///" + selectedFile);
        deegreeFC = gmlDoc.parse();
        return JUMPFeatureFactory2.createFromDeegreeFC(deegreeFC, null, null);
    }

    protected String readEncoding(PushbackInputStream pbis) throws IOException {
        int p;
        byte[] b = new byte[80];
        int rd = pbis.read(b);
        String s = new String(b).toLowerCase();
        String encoding = CharsetUtils.getSystemCharset();
        if (s.indexOf("?>") > -1 && (p = s.indexOf("encoding=")) > -1) {
            StringBuffer sb = new StringBuffer();
            int k = p + 1 + "encoding=".length();
            while (s.charAt(k) != '\"' && s.charAt(k) != '\'') {
                sb.append(s.charAt(k++));
            }
            encoding = sb.toString();
        }
        pbis.unread(b, 0, rd);
        return encoding;
    }
}

