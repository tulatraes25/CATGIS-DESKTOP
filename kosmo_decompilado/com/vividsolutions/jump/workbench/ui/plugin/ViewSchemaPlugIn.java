/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.SchemaPanel;
import com.vividsolutions.jump.workbench.ui.SchemaTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.feature.TemporalFeatureDataset;
import org.saig.core.model.relations.Relation;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.util.DialogFactory;

public class ViewSchemaPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(ViewSchemaPlugIn.class);
    public static final String NAME = String.valueOf(I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("Object.gif");
    public static final String KEY = ViewSchemaPlugIn.class + " - FRAME";
    private EditingPlugIn editingPlugIn;
    private WKTReader wktReader;
    private PlugInContext context;

    public ViewSchemaPlugIn(EditingPlugIn editingPlugIn) {
        this.editingPlugIn = editingPlugIn;
        this.wktReader = new WKTReader(geomFac);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ViewSchemaPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.context = context;
        if (this.frame(context) == null) {
            context.getSelectedLayer(0).getBlackboard().put(KEY, new EditSchemaFrame(context.getWorkbenchFrame(), (Layer)context.getSelectedLayer(0), this.editingPlugIn, ((TaskFrameProxy)((Object)context.getActiveInternalFrame())).getTaskFrame()));
        }
        this.frame(context).surface();
        return true;
    }

    protected void applyChanges(final Layer layer, final SchemaPanel panel) throws Exception {
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        progressDialog.setTitle(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Modify-layer-{0}-schema", new Object[]{layer.getTitle(LocaleManager.getActiveLocale())}));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            try {
                                progressDialog.report(String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Applying-changes-to-layer-{0}-schema", new Object[]{layer.getTitle(LocaleManager.getActiveLocale())})) + " ...");
                                ViewSchemaPlugIn.this.applyChanges(layer, panel, progressDialog);
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)e);
                                progressDialog.setExceptionMessage(e.getMessage());
                                progressDialog.setVisible(false);
                                return;
                            }
                        }
                        finally {
                            progressDialog.setVisible(false);
                        }
                    }
                }).start();
            }
        });
        if (this.frame(this.context) != null) {
            GUIUtil.centre(progressDialog, this.frame(this.context));
        } else {
            GUIUtil.centre(progressDialog, JUMPWorkbench.getFrameInstance());
        }
        progressDialog.setVisible(true);
        if (StringUtils.isNotEmpty((String)progressDialog.getExceptionMessage())) {
            throw new ConversionException(progressDialog.getExceptionMessage());
        }
    }

    protected void applyChanges(Layer layer, SchemaPanel panel, TaskMonitorDialog monitor) throws Exception {
        SchemaTableModel.Field field;
        HashMap relationsFields = new HashMap();
        if (!panel.isModified()) {
            return;
        }
        if (panel.validateInput() != null) {
            throw new Exception(panel.validateInput());
        }
        FeatureSchema oldSchema = layer.getFeatureSchema();
        if (!layer.isEditable()) {
            int i = 0;
            while (i < panel.getModel().getRowCount()) {
                SchemaTableModel.Field field2 = panel.getModel().get(i);
                oldSchema.changeTranslations(field2.getName(), field2.getTitleByLang());
                oldSchema.changeVisibility(field2.getName(), new Boolean(field2.getVisibility()));
                ++i;
            }
            layer.fireLayerChanged(LayerEventType.COMMITED);
            return;
        }
        if (oldSchema.hasCalculatedAttributes()) {
            Collection<Relation<?>> oldRelations = oldSchema.recoverRelations();
            for (Relation<?> currentRelation : oldRelations) {
                relationsFields.put(currentRelation, new ArrayList());
            }
        }
        String pkName = oldSchema.getPrimaryKeyName();
        AttributeType attType = oldSchema.getAttributeType(pkName);
        int oldType = oldSchema.getGeometryType();
        int oldPKposition = oldSchema.getPrimaryKeyIndex();
        panel.getModel().removeBlankRows();
        FeatureSchema newSchema = new FeatureSchema();
        newSchema.setGeometryType(oldType);
        int i = 0;
        while (i < panel.getModel().getRowCount()) {
            field = panel.getModel().get(i);
            if (i == oldPKposition) {
                newSchema.addAttribute(pkName, attType, Boolean.TRUE);
            }
            if (!field.isCalculated()) {
                newSchema.addAttribute(field.getName(), field.getPublicName(), field.getVisibility(), field.getType(), Boolean.FALSE, field.getTitleByLang());
            } else {
                Relation<?> relation = ((AttributeCalculate)oldSchema.getAttribute(field.getName())).getRelation();
                List relationFieldList = (List)relationsFields.get(relation);
                relationFieldList.add(field);
                relationsFields.put(relation, relationFieldList);
            }
            ++i;
        }
        if (!newSchema.hasAttribute(pkName)) {
            newSchema.addAttribute(pkName, attType, Boolean.TRUE);
        }
        if (relationsFields.size() > 0) {
            for (Relation currentRelation : relationsFields.keySet()) {
                List relationFieldsList = (List)relationsFields.get(currentRelation);
                for (SchemaTableModel.Field currentField : relationFieldsList) {
                    currentRelation.setRelationFieldNameValues(currentField.getRelationAttributeName(), currentField.getPublicName(), currentField.getVisibility());
                }
                newSchema.addRelation(currentRelation);
            }
        }
        if (newSchema.equals(oldSchema, true) || LayerUtil.checkBD(layer) && newSchema.equals(oldSchema, false)) {
            i = 0;
            while (i < panel.getModel().getRowCount()) {
                field = panel.getModel().get(i);
                oldSchema.changeTranslations(field.getName(), field.getTitleByLang());
                oldSchema.changeVisibility(field.getName(), new Boolean(field.getVisibility()));
                ++i;
            }
            layer.fireLayerChanged(LayerEventType.COMMITED);
            return;
        }
        this.commitChanges(layer, panel, newSchema, monitor);
    }

    protected void commitChanges(Layer layer, SchemaPanel panel, FeatureSchema newSchema, TaskMonitorDialog monitor) throws Exception {
        FeatureCollection fc = layer.getUltimateFeatureCollectionWrapper();
        if (fc instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcOnDemand = (FeatureCollectionOnDemand)fc;
            AbstractDataSource ds = fcOnDemand.getDataAccesor();
            if (ds instanceof ShapeFileDataSource) {
                this.commitChanges(layer, panel, newSchema, monitor, fc);
            } else if (ds instanceof AbstractJDBCDataSource) {
                AbstractJDBCDataSource jdbcDataSource = (AbstractJDBCDataSource)ds;
                this.commitChanges(layer, panel, newSchema, monitor, jdbcDataSource);
            } else {
                this.commitChanges(layer, panel, newSchema, monitor, fc);
            }
        } else {
            this.commitChanges(layer, panel, newSchema, monitor, fc);
        }
    }

    protected void commitChanges(Layer layer, SchemaPanel panel, FeatureSchema newSchema, TaskMonitorDialog monitor, AbstractJDBCDataSource jdbcDataSource) throws Exception {
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Checking-changes-validity")) + "...");
        FeatureIterator it = null;
        try {
            it = jdbcDataSource.getFeaturesIterator();
            int cont = 0;
            int total = layer.getUltimateFeatureCollectionWrapper().size();
            while (it.hasNext()) {
                Feature feature = it.next();
                this.convert(feature, panel, newSchema);
                if (cont++ % 500 != 0) continue;
                monitor.report(cont, total, I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.features-validated"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        try {
            monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Making-schema-changes")) + "...");
            jdbcDataSource.beginTransaction();
            this.executeSchemaChanges(jdbcDataSource, jdbcDataSource.getSchema(), panel);
            layer.getLayerManager().getUndoableEditReceiver().getUndoManager().discardAllEdits();
            jdbcDataSource.endTransaction();
            jdbcDataSource.initialize();
            FeatureSchema updatedSchema = jdbcDataSource.getSchema();
            int i = 0;
            while (i < newSchema.getAttributeCount()) {
                Attribute currentAttr = newSchema.getAttribute(i);
                updatedSchema.changeTranslations(currentAttr.getName(), currentAttr.getTitleByLang());
                updatedSchema.changeVisibility(currentAttr.getName(), currentAttr.isVisibility());
                ++i;
            }
            ((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).setSchema(updatedSchema);
            panel.setModel(new SchemaTableModel(layer));
            layer.setFeatureCollectionModified(false);
            layer.fireLayerChanged(LayerEventType.COMMITED);
            this.context.getLayerViewPanel().getSelectionManager().unselectItems(layer);
            panel.markAsUnmodified();
        }
        catch (Exception ex) {
            jdbcDataSource.rollback(true);
            jdbcDataSource.clearTransaction();
            throw ex;
        }
    }

    private void executeSchemaChanges(AbstractJDBCDataSource jdbcDataSource, FeatureSchema oldSchema, SchemaPanel panel) throws Exception {
        ArrayList<String> sqls = new ArrayList<String>();
        List<String> oldSchemaAttrNames = oldSchema.getAttributeNames();
        oldSchemaAttrNames.remove(oldSchema.getPrimaryKeyName());
        List<SchemaTableModel.Field> fields = panel.getModel().getFields();
        for (SchemaTableModel.Field currentField : fields) {
            if (currentField.getOriginalIndex() == -1) {
                sqls.add(jdbcDataSource.getSQLForAddColumn(currentField.getName(), currentField.getType()));
                continue;
            }
            if (!currentField.getName().equals(currentField.getOldName())) {
                sqls.add(jdbcDataSource.getSQLForAlterColumnName(currentField.getOldName(), currentField.getName(), currentField.getType()));
            }
            if (!oldSchema.getAttributeType(currentField.getOldName()).equals(currentField.getType())) {
                sqls.add(jdbcDataSource.getSQLForAlterColumnType(currentField.getName(), currentField.getType()));
            }
            oldSchemaAttrNames.remove(currentField.getOldName());
        }
        for (String oldAttrName : oldSchemaAttrNames) {
            sqls.add(jdbcDataSource.getSQLForDropColumn(oldAttrName));
        }
        for (String sql : sqls) {
            jdbcDataSource.executeNonFeatureQuery(sql, false);
        }
    }

    protected void commitChanges(Layer layer, SchemaPanel panel, FeatureSchema newSchema, TaskMonitorDialog monitor, FeatureCollection fc) throws Exception {
        ArrayList<Feature> tempFeatures = new ArrayList<Feature>();
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Checking-changes-validity")) + "...");
        FeatureIterator it = null;
        try {
            it = layer.getUltimateFeatureCollectionWrapper().iterator();
            int cont = 0;
            int total = layer.getUltimateFeatureCollectionWrapper().size();
            while (it.hasNext()) {
                Feature feature = it.next();
                tempFeatures.add(this.convert(feature, panel, newSchema));
                if (cont++ % 500 != 0) continue;
                monitor.report(cont, total, I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.features-validated"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Making-schema-changes")) + "...");
        layer.getLayerManager().getUndoableEditReceiver().getUndoManager().discardAllEdits();
        if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcDemmand = (FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper();
            AbstractDataSource dataAccesor = fcDemmand.getDataAccesor();
            if (dataAccesor.getClass().equals(ShapeFileDataSource.class)) {
                ShapeFileDataSource dataSource = (ShapeFileDataSource)fcDemmand.getDataAccesor();
                dataSource.commit(new TemporalFeatureDataset(tempFeatures, newSchema));
                FeatureSchema updatedSchema = dataSource.getSchema();
                int i = 0;
                while (i < newSchema.getAttributeCount()) {
                    Attribute currentAttr = newSchema.getAttribute(i);
                    updatedSchema.changeTranslations(currentAttr.getName(), currentAttr.getTitleByLang());
                    updatedSchema.changeVisibility(currentAttr.getName(), currentAttr.isVisibility());
                    ++i;
                }
                fcDemmand.setSchema(updatedSchema);
            } else {
                FeatureDataset fds = new FeatureDataset(tempFeatures, newSchema);
                fds.set3d(layer.getUltimateFeatureCollectionWrapper().is3d());
                layer.setFeatureCollection(fds);
            }
        } else {
            FeatureDataset fds = new FeatureDataset(tempFeatures, newSchema);
            fds.set3d(layer.getUltimateFeatureCollectionWrapper().is3d());
            layer.setFeatureCollection(fds);
        }
        panel.setModel(new SchemaTableModel(layer));
        if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureDataset) {
            layer.setFeatureCollectionModified(true);
            layer.fireLayerChanged(LayerEventType.METADATA_CHANGED);
        } else {
            layer.setFeatureCollectionModified(false);
        }
        layer.fireLayerChanged(LayerEventType.COMMITED);
        this.context.getLayerViewPanel().getSelectionManager().unselectItems(layer);
        panel.markAsUnmodified();
    }

    private Feature convert(Feature oldFeature, SchemaPanel panel, FeatureSchema newSchema) throws ConversionException {
        Feature newFeature = FeatureUtil.toFeature(oldFeature.getGeometry(), newSchema);
        int i = 0;
        while (i < panel.getModel().getRowCount()) {
            String name;
            if (panel.getModel().get(i).getOriginalIndex() == -1) {
                name = panel.getModel().get(i).getName();
                newFeature.setAttribute(name, (Object)(panel.getModel().get(i).getType() == AttributeType.GEOMETRY ? oldFeature.getGeometry() : null));
            } else {
                name = panel.getModel().get(i).getName();
                String oldName = panel.getModel().get(i).getOldName();
                newFeature.setAttribute(name, this.convert(oldFeature.getAttribute(oldName), oldFeature.getSchema().getAttributeType(oldName), newFeature.getSchema().getAttributeType(name), panel.getModel().get(i).getName(), panel.isForcingInvalidConversionsToNull()));
            }
            ++i;
        }
        return newFeature;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private Object convert(Object oldValue, AttributeType oldType, AttributeType newType, String name, boolean forcingInvalidConversionsToNull) throws ConversionException {
        String newValue = null;
        if (oldType.equals(newType)) {
            return oldValue;
        }
        if (oldValue == null) {
            if (newType != AttributeType.GEOMETRY) return null;
            Point point = geomFac.createPoint(new Coordinate());
            return point;
        }
        if (oldType.equals(AttributeType.GEOMETRY)) {
            if (newType.equals(AttributeType.STRING)) return oldValue.toString();
            if (newType.equals(AttributeType.VARCHAR)) return oldValue.toString();
            if (newType.equals(AttributeType.LONGVARCHAR)) return oldValue.toString();
            if (!newType.equals(AttributeType.TEXT)) throw new ConversionException(String.valueOf(I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.cannot-convert-to-geometry")) + ": \"" + this.limitLength(oldValue.toString()) + "\" (" + name + ")");
            return oldValue.toString();
        }
        if (!newType.equals(AttributeType.GEOMETRY)) return FeatureUtil.getGoodAttribute(newType, oldValue);
        if (!(oldType.equals(AttributeType.STRING) || oldType.equals(AttributeType.VARCHAR) || oldType.equals(AttributeType.LONGVARCHAR))) {
            if (!oldType.equals(AttributeType.TEXT)) throw new ConversionException(String.valueOf(I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.cannot-convert-to-geometry")) + ": \"" + this.limitLength(oldValue.toString()) + "\" (" + name + ")");
        }
        try {
            return this.wktReader.read((String)oldValue);
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new ConversionException(String.valueOf(I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.cannot-convert-to-geometry")) + ": \"" + this.limitLength(oldValue.toString()) + "\" (" + name + ")");
        }
    }

    private String limitLength(String s) {
        return StringUtil.limitLength(s, 30);
    }

    private void commitEditsInProgress(SchemaPanel panel) {
        if (panel.getTable().getEditingRow() != -1) {
            panel.getTable().getCellEditor(panel.getTable().getEditingRow(), panel.getTable().getEditingColumn()).stopCellEditing();
        }
    }

    private EditSchemaFrame frame(PlugInContext context) {
        return (EditSchemaFrame)context.getSelectedLayer(0).getBlackboard().get(KEY);
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAttributeTabLayersMustNotBeHidden());
    }

    private static class ConversionException
    extends Exception {
        private static final long serialVersionUID = 1L;

        public ConversionException(String message) {
            super(message);
        }
    }

    private class EditSchemaFrame
    extends JInternalFrame
    implements LayerNamePanelProxy,
    LayerNamePanel,
    LayerManagerProxy {
        private static final long serialVersionUID = 1L;
        private LayerManager layerManager;
        private Layer layer;
        private WorkbenchFrame workbenchFrame;
        private LayerListener layerListener;

        public EditSchemaFrame(final WorkbenchFrame workbenchFrame, final Layer layer, EditingPlugIn editingPlugIn, final TaskFrame frame) {
            this.layer = layer;
            this.workbenchFrame = workbenchFrame;
            layer.getBlackboard().put(KEY, this);
            final SchemaPanel panel = new SchemaPanel(layer, editingPlugIn, workbenchFrame.getContext());
            this.layerManager = layer.getLayerManager();
            this.addInternalFrameListener(new InternalFrameAdapter(){

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    if (EditSchemaFrame.this.layerManager != null) {
                        EditSchemaFrame.this.layerManager.removeLayerListener(panel.getLayerListener());
                        EditSchemaFrame.this.layerManager.removeLayerListener(EditSchemaFrame.this.layerListener);
                    }
                    if (layer != null && layer.getBlackboard() != null) {
                        layer.getBlackboard().put(KEY, null);
                    }
                }
            });
            this.setResizable(true);
            this.setClosable(true);
            this.setMaximizable(true);
            this.setIconifiable(true);
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add((Component)panel, "Center");
            this.setSize(500, 300);
            this.updateTitle(layer);
            this.layerListener = new LayerListener(){

                @Override
                public void categoryChanged(CategoryEvent e) {
                }

                @Override
                public void featuresChanged(FeatureEvent e) {
                }

                @Override
                public void layerChanged(LayerEvent e) {
                    EditSchemaFrame.this.updateTitle(layer);
                }
            };
            layer.getLayerManager().addLayerListener(this.layerListener);
            this.setDefaultCloseOperation(0);
            panel.add(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        ViewSchemaPlugIn.this.commitEditsInProgress(panel);
                        ViewSchemaPlugIn.this.applyChanges(layer, panel);
                        if (frame != null) {
                            workbenchFrame.activateFrame(frame);
                        }
                        EditSchemaFrame.this.setVisible(false);
                    }
                    catch (Exception ex) {
                        DialogFactory.showWarningDialog(EditSchemaFrame.this, ex.getMessage(), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.error-while-applying-the-changes"));
                    }
                }
            });
            this.addInternalFrameListener(new InternalFrameAdapter(){

                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    ViewSchemaPlugIn.this.commitEditsInProgress(panel);
                    if (!layer.isEditable() || !panel.isModified()) {
                        EditSchemaFrame.this.dispose();
                        if (frame != null) {
                            workbenchFrame.activateFrame(frame);
                        }
                        return;
                    }
                    switch (DialogFactory.showYesNoCancelWarningDialog(EditSchemaFrame.this, I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.apply-changes-to-the-schema"), I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.apply-changes"))) {
                        case 0: {
                            try {
                                ViewSchemaPlugIn.this.applyChanges(layer, panel);
                                if (frame != null) {
                                    workbenchFrame.activateFrame(frame);
                                }
                            }
                            catch (Exception x) {
                                workbenchFrame.handleThrowable(x);
                                return;
                            }
                            EditSchemaFrame.this.dispose();
                            return;
                        }
                        case 1: {
                            EditSchemaFrame.this.dispose();
                            return;
                        }
                        case 2: {
                            return;
                        }
                    }
                    Assert.shouldNeverReachHere();
                }
            });
        }

        private void updateTitle(Layer layer) {
            this.setTitle(String.valueOf(layer.isEditable() ? I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.edit-fields") : I18N.getString("workbench.ui.plugin.ViewSchemaPlugIn.view-fields")) + ": " + layer.getTitle());
        }

        @Override
        public LayerManager getLayerManager() {
            return this.layerManager;
        }

        @Override
        public Layer chooseEditableLayer() {
            return TreeLayerNamePanel.chooseEditableLayer(this);
        }

        public void surface() {
            if (!this.workbenchFrame.hasInternalFrame(this)) {
                this.workbenchFrame.addInternalFrame(this, false, true);
            }
            this.workbenchFrame.activateFrame(this);
            this.setVisible(true);
            this.moveToFront();
            GUIUtil.centreOnScreen(this);
        }

        @Override
        public LayerNamePanel getLayerNamePanel() {
            return this;
        }

        @Override
        public Collection<Category> getSelectedCategories() {
            return new ArrayList<Category>();
        }

        @Override
        public Layerable[] getSelectedLayers() {
            return new Layer[]{this.layer};
        }

        @Override
        public void addListener(LayerNamePanelListener listener) {
        }

        @Override
        public void removeListener(LayerNamePanelListener listener) {
        }

        @Override
        public void saveStatus() {
        }

        @Override
        public void loadStatus() {
        }

        public Collection selectedNodes(Class c) {
            if (!Layerable.class.isAssignableFrom(c)) {
                return new ArrayList();
            }
            return Arrays.asList(this.getSelectedLayers());
        }
    }
}

