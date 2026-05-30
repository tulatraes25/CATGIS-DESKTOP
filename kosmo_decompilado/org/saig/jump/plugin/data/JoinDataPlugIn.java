/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.data;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.widgets.JoinDataDialog;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.data.AttributesSelection;

public class JoinDataPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.data.JoinDataPlugIn.name");
    private static final Logger LOGGER = Logger.getLogger(JoinDataPlugIn.class);
    private Layer sourceLayer;
    private Vector fieldsLayer;
    private Table sourceTable;
    private Vector fieldsTable;
    private Object[] fields;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        JoinDataDialog dialog = new JoinDataDialog(context.getWorkbenchFrame(), context.getWorkbenchContext(), true);
        if (dialog.isOk()) {
            this.sourceLayer = dialog.getSourceLayer();
            this.fieldsLayer = dialog.getFieldsLayer();
            this.sourceTable = dialog.getTable();
            this.fieldsTable = dialog.getFieldsTable();
            this.fields = dialog.getFields();
            if (dialog.isAllFieldsSelected()) {
                this.fields = null;
            }
            return true;
        }
        return false;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createAtLeastNTablesMustExistCheck(1)).add(checkFactory.createAtLeastNLayersMustExistCheck(1));
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("joinTable.gif");
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(I18N.getString("org.saig.jump.plugin.data.JoinDataPlugIn.processing-data"));
        Hashtable<AttributesSelection, Record> hashTable = new Hashtable<AttributesSelection, Record>();
        ITableIterator itRecords = null;
        try {
            itRecords = this.sourceTable.getDataSource().getIterator();
            while (itRecords.hasNext()) {
                Record record = itRecords.next();
                Object[] values = new Object[this.fieldsTable.size()];
                int i = 0;
                while (i < this.fieldsTable.size()) {
                    values[i] = record.getAttribute((String)this.fieldsTable.get(i));
                    ++i;
                }
                AttributesSelection selection = new AttributesSelection(values);
                hashTable.put(selection, record);
            }
        }
        finally {
            if (itRecords != null) {
                itRecords.close();
            }
        }
        FeatureCollection layerFC = this.sourceLayer.getFeatureCollectionWrapper().getUltimateWrappee();
        FeatureSchema layerSchema = layerFC.getFeatureSchema();
        HashMap<String, String> fieldTableToFieldLayer = new HashMap<String, String>();
        List<Object> tableNames = null;
        if (this.fields == null) {
            tableNames = this.sourceTable.getSchema().getAttributeNames();
        } else {
            tableNames = new ArrayList();
            int i = 0;
            while (i < this.fields.length) {
                tableNames.add(this.fields[i]);
                ++i;
            }
        }
        Object iter = tableNames.iterator();
        while (iter.hasNext()) {
            String fieldTable;
            String fieldLayer = fieldTable = (String)iter.next();
            if (layerSchema.hasAttribute(fieldTable)) {
                int cont = 1;
                boolean hecho = false;
                while (!hecho) {
                    if (!layerSchema.hasAttribute(String.valueOf(fieldTable) + "_" + cont)) {
                        hecho = true;
                        continue;
                    }
                    ++cont;
                }
                fieldLayer = String.valueOf(fieldTable) + "_" + cont;
            }
            layerSchema.addAttribute(fieldLayer, fieldLayer, Boolean.TRUE, this.sourceTable.getSchema().getAttributeType(fieldTable), Boolean.FALSE);
            fieldTableToFieldLayer.put(fieldTable, fieldLayer);
        }
        iter = null;
        try {
            try {
                iter = this.sourceLayer.getUltimateFeatureCollectionWrapper().iterator();
                int total = this.sourceLayer.getUltimateFeatureCollectionWrapper().size();
                int cont = 1;
                ArrayList<Feature> newFeatures = new ArrayList<Feature>();
                while (iter.hasNext()) {
                    monitor.report(I18N.getMessage("org.saig.jump.plugin.data.JoinDataPlugIn.processing-element-{0}-{1}-{2}", new Object[]{Integer.toString(cont), "/", Integer.toString(total)}));
                    Feature feature = iter.next();
                    Feature featPK = FeatureUtil.toFeature(feature.getGeometry(), layerSchema);
                    Map<String, Object> atributos = feature.getAttributes();
                    for (String fieldName : atributos.keySet()) {
                        featPK.setAttribute(fieldName, atributos.get(fieldName));
                    }
                    Object[] values = new Object[this.fieldsLayer.size()];
                    int i = 0;
                    while (i < this.fieldsLayer.size()) {
                        values[i] = feature.getAttribute((String)this.fieldsLayer.get(i));
                        ++i;
                    }
                    AttributesSelection key = new AttributesSelection(values);
                    if (hashTable.containsKey(key)) {
                        Record record = (Record)hashTable.get(key);
                        Set keys = fieldTableToFieldLayer.keySet();
                        for (String tableFieldKey : keys) {
                            String layerFieldKey = (String)fieldTableToFieldLayer.get(tableFieldKey);
                            Object value = record.getAttribute(tableFieldKey);
                            featPK.setAttribute(layerFieldKey, value);
                        }
                    }
                    newFeatures.add(featPK);
                }
                layerFC.updateAll(newFeatures);
                layerFC.commit();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (iter != null) {
                    iter.close();
                }
            }
        }
        finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }
}

