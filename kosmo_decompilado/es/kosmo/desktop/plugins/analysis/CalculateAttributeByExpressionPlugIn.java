/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.jeks.JeksExpressionParser
 *  com.eteks.jeks.JeksExpressionSyntax
 *  com.eteks.jeks.JeksFunctionParser
 *  com.eteks.jeks.JeksFunctionSyntax
 *  com.eteks.jeks.JeksInterpreter
 *  com.eteks.jeks.JeksParameter
 *  com.eteks.jeks.JeksTableModel
 *  com.eteks.parser.CompiledExpression
 *  com.eteks.parser.ExpressionParameter
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.analysis;

import com.eteks.jeks.JeksExpressionParser;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.jeks.JeksFunctionParser;
import com.eteks.jeks.JeksFunctionSyntax;
import com.eteks.jeks.JeksInterpreter;
import com.eteks.jeks.JeksParameter;
import com.eteks.jeks.JeksTableModel;
import com.eteks.parser.CompiledExpression;
import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.analysis.CalculateAttributeByExpressionDialog;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.table.TableModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.DummyFeatureIterator;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.util.jeks.userFunctions.ConcatFunction;
import org.saig.jump.util.jeks.userFunctions.IndexOfFunction;
import org.saig.jump.util.jeks.userFunctions.InvFunction;
import org.saig.jump.util.jeks.userFunctions.LengthFunction;
import org.saig.jump.util.jeks.userFunctions.OppFunction;
import org.saig.jump.util.jeks.userFunctions.RoundFunction;
import org.saig.jump.util.jeks.userFunctions.SqrFunction;
import org.saig.jump.util.jeks.userFunctions.SubStringFunction;
import org.saig.jump.util.jeks.userFunctions.TruncFunction;
import org.saig.jump.widgets.util.DialogFactory;

public class CalculateAttributeByExpressionPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final String SPECIAL_QUOTE = "$_quote_$";
    private static final String SPECIAL_SLASH = "$_slash_$";
    public static final Logger LOGGER = Logger.getLogger(CalculateAttributeByExpressionPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Calculator");
    public static final Icon ICON = IconLoader.icon("calculator.png");
    private JeksExpressionParser jeksExpressionParser;
    private JeksInterpreter interpreter;
    protected CalculateAttributeByExpressionDialog optionsDialog;
    private int contErr;
    private static final int nCommit = 1000;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        Collection<Feature> selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
        FeatureSchema fs = editableLayer.getFeatureSchema();
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        int i = 0;
        while (i < fs.getAttributeCount()) {
            Attribute attr;
            if (i != fs.getPrimaryKeyIndex() && (attr = fs.getAttribute(i)).getType() != AttributeType.GEOMETRY) {
                attrs.add(attr);
            }
            ++i;
        }
        this.optionsDialog = new CalculateAttributeByExpressionDialog(JUMPWorkbench.getFrameInstance(), true, attrs, editableLayer.getTitle(), editableLayer.getFeatureCollectionWrapper().size(), selectedFeatures.size(), LayerUtil.checkBD(editableLayer), fs.getPrimaryKey().getPublicName(), fs.getPublicName(fs.getGeometryIndex()), fs.getGeometryType());
        this.optionsDialog.setVisible(true);
        return this.optionsDialog.wasOkPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        this.createParser();
        ArrayList<Attribute> attributesInExpression = new ArrayList<Attribute>();
        boolean generateNew = this.optionsDialog.isNewAttribute();
        boolean useSelectedOnly = this.optionsDialog.useSelectedOnly();
        String attrName = this.optionsDialog.getAttributeName();
        String baseExpression = this.optionsDialog.getExpression();
        baseExpression = CalculateAttributeByExpressionPlugIn.specialCharsPreparse(baseExpression);
        monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Checking-source-layer-{0}", new Object[]{editableLayer.getTitle()})) + "...");
        FeatureSchema fsOrig = editableLayer.getFeatureSchema();
        int i = 0;
        while (i < fsOrig.getAttributeCount()) {
            Attribute attr = fsOrig.getAttribute(i);
            Pattern patron = Pattern.compile(attr.getPublicName());
            Matcher encaja = patron.matcher(baseExpression);
            if (encaja.find()) {
                attributesInExpression.add(attr);
            }
            ++i;
        }
        AttributeType attrType = !generateNew ? fsOrig.getAttributeType(attrName) : this.calculateNewAttributeType(baseExpression);
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Calculating-attribute-{0}", new Object[]{attrName}));
        this.contErr = 0;
        if (generateNew) {
            this.calculateExpressionInNewAttribute(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor);
        } else {
            this.calculateExpressionInExistingAttribute(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor);
        }
    }

    private void calculateExpressionInNewAttribute(boolean useSelectedOnly, Layer editableLayer, String attrName, AttributeType attrType, String baseExpression, List<Attribute> attributesInExpression, PlugInContext context, TaskMonitor monitor) throws Exception {
        FeatureCollection fc = editableLayer.getUltimateFeatureCollectionWrapper();
        if (fc instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcOnDemand = (FeatureCollectionOnDemand)fc;
            AbstractDataSource ds = fcOnDemand.getDataAccesor();
            if (ds instanceof ShapeFileDataSource) {
                this.calculateExpressionInNewAttributeGeneric(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor, fc);
            } else if (ds instanceof AbstractJDBCDataSource) {
                AbstractJDBCDataSource jdbcDataSource = (AbstractJDBCDataSource)ds;
                this.calculateExpressionInNewAttributeForDatabase(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor, jdbcDataSource);
            } else {
                this.calculateExpressionInNewAttributeGeneric(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor, fc);
            }
        } else {
            this.calculateExpressionInNewAttributeGeneric(useSelectedOnly, editableLayer, attrName, attrType, baseExpression, attributesInExpression, context, monitor, fc);
        }
    }

    private void calculateExpressionInNewAttributeForDatabase(boolean useSelectedOnly, Layer editableLayer, String attrName, AttributeType attrType, String baseExpression, List<Attribute> attributesInExpression, PlugInContext context, TaskMonitor monitor, AbstractJDBCDataSource jdbcDataSource) throws Exception {
        int opcion;
        FeatureIterator it = null;
        try {
            it = jdbcDataSource.getFeaturesIterator();
            int cont = 0;
            int total = editableLayer.getUltimateFeatureCollectionWrapper().size();
            while (it.hasNext()) {
                Feature feature = it.next();
                this.calcula(feature, baseExpression, attributesInExpression);
                if (cont++ % 500 != 0) continue;
                monitor.report(cont, total, I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.features-validated"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        if (!monitor.isCancelRequested() && this.contErr != 0 && (opcion = DialogFactory.showYesNoWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.{0}-calculating-errors-were-found", new Object[]{Integer.toString(this.contErr)})) + ". " + I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Do-you-want-to-continue"), NAME)) != 0) {
            this.warnOperationCancelled(context);
            return;
        }
        try {
            monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.Making-schema-changes")) + "...");
            jdbcDataSource.beginTransaction();
            this.addAttribute(jdbcDataSource, attrName, attrType);
            editableLayer.getLayerManager().getUndoableEditReceiver().getUndoManager().discardAllEdits();
            jdbcDataSource.endTransaction();
            jdbcDataSource.initialize();
            jdbcDataSource.beginTransaction();
            Collection<Feature> selectedFeatures = null;
            FeatureCollection sourceFc = null;
            if (useSelectedOnly) {
                selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
                context.getLayerViewPanel().getSelectionManager().unselectItems(editableLayer, selectedFeatures);
            } else {
                sourceFc = editableLayer.getUltimateFeatureCollectionWrapper();
            }
            FeatureIterator itFeatures = null;
            ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
            int cont = 0;
            int totalFeatures = 0;
            try {
                try {
                    if (!useSelectedOnly) {
                        itFeatures = sourceFc.iterator();
                        totalFeatures = sourceFc.size();
                    } else {
                        itFeatures = selectedFeatures.isEmpty() ? new DummyFeatureIterator() : new CollectionIterator(selectedFeatures);
                        totalFeatures = selectedFeatures.size();
                    }
                    while (itFeatures.hasNext() && !monitor.isCancelRequested()) {
                        Feature feat = itFeatures.next();
                        Feature cloneFeature = (Feature)feat.clone();
                        cloneFeature.setSchema(jdbcDataSource.getSchema());
                        Object res = this.calcula(cloneFeature, baseExpression, attributesInExpression);
                        cloneFeature.setAttribute(attrName, FeatureUtil.getGoodAttribute(attrType, res));
                        featsToUpdate.add(cloneFeature);
                        if (++cont % 100 != 0) continue;
                        monitor.report(cont, totalFeatures, I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Processed-features"));
                    }
                    if (!monitor.isCancelRequested()) {
                        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updating-elements-for-layer-{0}", new Object[]{editableLayer.getTitle()}));
                        editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        jdbcDataSource.endTransaction();
                        editableLayer.setFeatureCollectionModified(false);
                        if (useSelectedOnly) {
                            context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(editableLayer, selectedFeatures);
                        }
                        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updated-{0}-elements-from-layer-{1}", new Object[]{Integer.toString(cont), editableLayer.getTitle()}));
                        editableLayer.fireLayerChanged(LayerEventType.COMMITED);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-field-value-{0}-of-multiple-elements-from-layer-{1}", new Object[]{attrName, editableLayer.getTitle()}), I18N.getString("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-values"));
                    if (itFeatures != null) {
                        itFeatures.close();
                    }
                    return;
                }
            }
            finally {
                if (itFeatures != null) {
                    itFeatures.close();
                }
            }
        }
        catch (Exception ex) {
            jdbcDataSource.rollback(true);
            jdbcDataSource.clearTransaction();
            throw ex;
        }
    }

    private void addAttribute(AbstractJDBCDataSource jdbcDataSource, String attrName, AttributeType attrType) throws SQLException {
        String sqlAdd = jdbcDataSource.getSQLForAddColumn(attrName, attrType);
        jdbcDataSource.executeNonFeatureQuery(sqlAdd, false);
    }

    private void calculateExpressionInNewAttributeGeneric(boolean useSelectedOnly, Layer editableLayer, String attrName, AttributeType attrType, String baseExpression, List<Attribute> attributesInExpression, PlugInContext context, TaskMonitor monitor, FeatureCollection fcOrig) throws Exception {
        int opcion;
        FeatureIterator it = null;
        try {
            it = editableLayer.getUltimateFeatureCollectionWrapper().iterator();
            int cont = 0;
            int total = editableLayer.getUltimateFeatureCollectionWrapper().size();
            while (it.hasNext()) {
                Feature feature = it.next();
                this.calcula(feature, baseExpression, attributesInExpression);
                if (cont++ % 500 != 0) continue;
                monitor.report(cont, total, I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn.features-validated"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        if (!monitor.isCancelRequested() && this.contErr != 0 && (opcion = DialogFactory.showYesNoWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.{0}-calculating-errors-were-found", new Object[]{Integer.toString(this.contErr)})) + ". " + I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Do-you-want-to-continue"), NAME)) != 0) {
            this.warnOperationCancelled(context);
            return;
        }
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Calculating-attribute-{0}", new Object[]{attrName}));
        FeatureSchema fs_nuevo = (FeatureSchema)fcOrig.getFeatureSchema().clone();
        fs_nuevo.addAttribute(attrName, attrType);
        Collection<Feature> selectedFeatures = null;
        ArrayList<Feature> tmpFeatures = new ArrayList<Feature>(fcOrig.size());
        if (useSelectedOnly) {
            selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
            context.getLayerViewPanel().getSelectionManager().unselectItems(editableLayer, selectedFeatures);
            tmpFeatures = new ArrayList(selectedFeatures.size());
        } else {
            tmpFeatures = new ArrayList(fcOrig.size());
        }
        FeatureIterator itFeatures = null;
        int contFeat = 0;
        int numElements = fcOrig.size();
        try {
            itFeatures = fcOrig.iterator();
            while (itFeatures.hasNext() && !monitor.isCancelRequested()) {
                Feature feat = itFeatures.next();
                Object res = this.calcula(feat, baseExpression, attributesInExpression);
                Feature feat_temp = FeatureUtil.toFeature((Geometry)feat.getGeometry().clone(), fs_nuevo);
                FeatureUtil.copyAttributes(feat, feat_temp);
                if (!useSelectedOnly || useSelectedOnly && selectedFeatures.contains(feat)) {
                    feat_temp.setAttribute(attrName, FeatureUtil.getGoodAttribute(attrType, res));
                }
                tmpFeatures.add(feat_temp);
                if (contFeat++ % 100 != 0) continue;
                monitor.report(contFeat, numElements, I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                this.warnOperationCancelled(context);
                return;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (fcOrig instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcOnDemand = (FeatureCollectionOnDemand)fcOrig;
            ((ShapeFileDataSource)fcOnDemand.getDataAccesor()).commit(tmpFeatures, fs_nuevo);
        } else {
            FeatureDataset fc_tmp = new FeatureDataset(fs_nuevo);
            fc_tmp.addAll(tmpFeatures);
            editableLayer.setFeatureCollection(fc_tmp);
            fcOrig = null;
        }
        if (useSelectedOnly) {
            context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(editableLayer, selectedFeatures);
        }
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updated-{0}-elements-from-layer-{1}", new Object[]{Integer.toString(contFeat), editableLayer.getTitle()}));
        editableLayer.fireLayerChanged(LayerEventType.COMMITED);
    }

    private void calculateExpressionInExistingAttribute(boolean useSelectedOnly, Layer editableLayer, String attrName, AttributeType attrType, String baseExpression, List<Attribute> attributesInExpression, PlugInContext context, TaskMonitor monitor) {
        Collection<Feature> selectedFeatures = null;
        FeatureCollection sourceFc = null;
        if (useSelectedOnly) {
            selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
            context.getLayerViewPanel().getSelectionManager().unselectItems(editableLayer, selectedFeatures);
        } else {
            sourceFc = editableLayer.getUltimateFeatureCollectionWrapper();
        }
        FeatureIterator itFeatures = null;
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        int cont = 0;
        int totalFeatures = 0;
        try {
            int opcion;
            if (!useSelectedOnly) {
                itFeatures = sourceFc.iterator();
                totalFeatures = sourceFc.size();
            } else {
                itFeatures = selectedFeatures.isEmpty() ? new DummyFeatureIterator() : new CollectionIterator(selectedFeatures);
                totalFeatures = selectedFeatures.size();
            }
            while (itFeatures.hasNext() && !monitor.isCancelRequested()) {
                Feature feat = itFeatures.next();
                Feature cloneFeature = (Feature)feat.clone();
                Object res = this.calcula(cloneFeature, baseExpression, attributesInExpression);
                cloneFeature.setAttribute(attrName, FeatureUtil.getGoodAttribute(attrType, res));
                featsToUpdate.add(cloneFeature);
                if (++cont % 100 != 0) continue;
                monitor.report(cont, totalFeatures, I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Processed-features"));
            }
            if (!monitor.isCancelRequested() && this.contErr != 0 && (opcion = DialogFactory.showYesNoWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.{0}-calculating-errors-were-found", new Object[]{Integer.toString(this.contErr)})) + ". " + I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Do-you-want-to-continue"), NAME)) != 0) {
                this.warnOperationCancelled(context);
                return;
            }
            try {
                if (!monitor.isCancelRequested()) {
                    monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updating-elements-for-layer-{0}", new Object[]{editableLayer.getTitle()}));
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                    editableLayer.setFeatureCollectionModified(true);
                    if (useSelectedOnly) {
                        context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(editableLayer, selectedFeatures);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-field-value-{0}-of-multiple-elements-from-layer-{1}", new Object[]{attrName, editableLayer.getTitle()}), I18N.getString("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-values"));
                return;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updated-{0}-elements-from-layer-{1}", new Object[]{Integer.toString(cont), editableLayer.getTitle()}));
            editableLayer.fireLayerChanged(LayerEventType.COMMITED);
        }
    }

    private AttributeType calculateNewAttributeType(String baseExpression) {
        if (StringUtils.containsIgnoreCase((String)baseExpression, (String)"concat") || StringUtils.containsIgnoreCase((String)baseExpression, (String)"substring")) {
            return AttributeType.STRING;
        }
        return AttributeType.DOUBLE;
    }

    private void createParser() {
        JeksExpressionSyntax syntax = new JeksExpressionSyntax(Locale.ENGLISH);
        this.interpreter = new JeksInterpreter();
        JeksFunctionParser functionParser = new JeksFunctionParser((JeksFunctionSyntax)new JeksExpressionSyntax(Locale.ENGLISH));
        JeksParameter parameter = new JeksParameter(syntax, (Interpreter)this.interpreter, (TableModel)new JeksTableModel());
        this.jeksExpressionParser = new JeksExpressionParser(syntax, (ExpressionParameter)parameter, (Interpreter)this.interpreter, functionParser, null);
        this.jeksExpressionParser.addUserFunction((Function)new ConcatFunction());
        this.jeksExpressionParser.addUserFunction((Function)new SubStringFunction());
        this.jeksExpressionParser.addUserFunction((Function)new LengthFunction());
        this.jeksExpressionParser.addUserFunction((Function)new IndexOfFunction());
        this.jeksExpressionParser.addUserFunction((Function)new SqrFunction());
        this.jeksExpressionParser.addUserFunction((Function)new OppFunction());
        this.jeksExpressionParser.addUserFunction((Function)new InvFunction());
        this.jeksExpressionParser.addUserFunction((Function)new TruncFunction());
        this.jeksExpressionParser.addUserFunction((Function)new RoundFunction());
    }

    public static String specialCharsPreparse(String expresion) {
        expresion = StringUtils.replace((String)expresion, (String)"\\\\", (String)SPECIAL_SLASH);
        expresion = StringUtils.replace((String)expresion, (String)"\\\"", (String)SPECIAL_QUOTE);
        return expresion;
    }

    public static String specialCharsPostparse(String expresion) {
        expresion = StringUtils.replace((String)expresion, (String)SPECIAL_SLASH, (String)"\\");
        expresion = StringUtils.replace((String)expresion, (String)SPECIAL_QUOTE, (String)"\"");
        return expresion;
    }

    private Object calcula(Feature feat, String baseExpression, List<Attribute> attrs) {
        Object resultado;
        String expresionEspecifica = baseExpression;
        int i = 0;
        while (i < attrs.size()) {
            Attribute attr = attrs.get(i);
            String attrName = attr.getName();
            AttributeType type = attr.getType();
            boolean isGeometry = type == AttributeType.GEOMETRY;
            String valorString = "";
            Object attrib = feat.getAttribute(attrName);
            if (AttributeType.isNumeric(type)) {
                valorString = attrib == null ? "0" : feat.getAttribute(attrName).toString();
            }
            if (AttributeType.isString(type)) {
                valorString = attrib == null ? "\"\"" : "\"" + feat.getAttribute(attrName).toString() + "\"";
            }
            if (isGeometry) {
                expresionEspecifica = StringUtils.replace((String)expresionEspecifica, (String)("area(" + attr.getPublicName() + ")"), (String)(" " + feat.getGeometry().getArea() + " "));
                expresionEspecifica = StringUtils.replace((String)expresionEspecifica, (String)("geomLength(" + attr.getPublicName() + ")"), (String)(" " + feat.getGeometry().getLength() + " "));
                expresionEspecifica = StringUtils.replace((String)expresionEspecifica, (String)("coordX(" + attr.getPublicName() + ")"), (String)(" " + feat.getGeometry().getCoordinate().x + " "));
                expresionEspecifica = StringUtils.replace((String)expresionEspecifica, (String)("coordY(" + attr.getPublicName() + ")"), (String)(" " + feat.getGeometry().getCoordinate().y + " "));
            } else {
                expresionEspecifica = StringUtils.replace((String)expresionEspecifica, (String)("[" + attr.getPublicName() + "]"), (String)(" " + valorString + " "));
            }
            ++i;
        }
        try {
            CompiledExpression compiledExpression = this.jeksExpressionParser.compileExpression(expresionEspecifica);
            resultado = compiledExpression.computeExpression((Interpreter)this.interpreter);
            if (resultado instanceof String) {
                resultado = CalculateAttributeByExpressionPlugIn.specialCharsPostparse((String)resultado);
            }
        }
        catch (Exception e) {
            ++this.contErr;
            resultado = null;
        }
        return resultado;
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
        return CalculateAttributeByExpressionPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        return check;
    }
}

