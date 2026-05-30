/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.dao.datasource.filedatasource.dxf.DxfWriterFactoryFinder;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfWriter;
import es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.filedatasource.dxf.DXFDataAccesor;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser;

public class DXFFileConnection
implements Connection {
    private DXFDataAccesor dataAccesor;

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) throws Exception {
        return this.createFeatureCollectionFromSelection(query, null);
    }

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromSelection(query, null);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        query = FileUtil.addValidExtension(query, "dxf");
        return this.createFeatureCollectionFromSelection(query, null);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromSelection(query, properties);
    }

    private void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes) throws Exception {
        block19: {
            query = FileUtil.addValidExtension(query, "dxf");
            String version = "AC1014";
            ArrayList<FeatureCollection> fcList = new ArrayList<FeatureCollection>(1);
            fcList.add(featureCollection);
            boolean geometryAsBlocks = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(DXFFileSaveQueryChooser.SAVE_GEOMETRIES_AS_BLOCKS_KEY, false);
            boolean writeFeatureAttributesAsXData = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(DXFFileSaveQueryChooser.WRITE_FEATURE_ATTRIBUTES_AS_XDATA_KEY, false);
            boolean writePointFcsAsInsertsWithAttrs = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(DXFFileSaveQueryChooser.WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS_KEY, false);
            String encoding = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, null);
            FileOutputStream stream = null;
            OutputStreamWriter outWriter = null;
            File dxfFile = new File(query);
            Writer out = null;
            boolean fileAlreadyExisted = dxfFile.exists();
            try {
                try {
                    stream = new FileOutputStream(dxfFile);
                    outWriter = encoding != null ? new OutputStreamWriter((OutputStream)stream, Charset.forName(encoding)) : new OutputStreamWriter(stream);
                    out = new BufferedWriter(outWriter);
                    IDxfWriter writer = DxfWriterFactoryFinder.getWriter(version, out, encoding);
                    if (writer != null) {
                        writer.setOption("GEOMETRY_AS_BLOCK", geometryAsBlocks);
                        writer.setOption("WRITE_FEATURE_ATTRS_AS_XDATA", writeFeatureAttributesAsXData);
                        writer.setOption("WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS", writePointFcsAsInsertsWithAttrs);
                        writer.setOption("DXF_INSUNITS_VALUE", new Integer(4));
                        writer.write(fcList, version);
                        out.flush();
                        break block19;
                    }
                    throw new Exception(I18N.getMessage("org.saig.jump.plugin.datasource.DXFFileConnection.DXF-version-{0}-not-supported", new Object[]{version}));
                }
                catch (Exception ex) {
                    try {
                        if (!fileAlreadyExisted && dxfFile != null && !dxfFile.isDirectory()) {
                            dxfFile.delete();
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    throw ex;
                }
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                }
                catch (IOException iOException) {}
                try {
                    if (stream != null) {
                        ((OutputStream)stream).close();
                    }
                }
                catch (IOException iOException) {}
            }
        }
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style clone, IProjection proj, TaskMonitor monitor) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void close() {
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        ArrayList<AbstractDataSource> list = new ArrayList<AbstractDataSource>();
        list.add(this.dataAccesor);
        return list;
    }

    private FeatureCollection[] createFeatureCollectionFromSelection(String selectedFile, Map<String, Object> properties) throws Exception {
        boolean ignoreBlocks = false;
        if (MapUtils.isNotEmpty(properties) && properties.containsKey("Ignore blocks")) {
            ignoreBlocks = (Boolean)properties.get("Ignore blocks");
        }
        this.updateDataAccesor(selectedFile, ignoreBlocks);
        FeatureCollectionOnDemand fC = new FeatureCollectionOnDemand();
        fC.setDataAccesor(this.dataAccesor);
        fC.setSchema(this.dataAccesor.getSchema());
        fC.setName(FileUtil.nameWithoutExtension(this.dataAccesor.getFile().getName()));
        return new FeatureCollection[]{fC};
    }

    private void updateDataAccesor(String query, boolean ignoreBlocks) throws Exception {
        this.dataAccesor = new DXFDataAccesor(new File(query), ignoreBlocks);
    }
}

