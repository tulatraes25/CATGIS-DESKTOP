/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
 *  com.l2fprod.common.beans.editor.ComboBoxPropertyEditor$Value
 *  com.l2fprod.common.beans.editor.JCalendarDatePropertyEditor
 *  com.l2fprod.common.beans.editor.LongPropertyEditor
 *  com.l2fprod.common.propertysheet.DefaultProperty
 *  com.l2fprod.common.propertysheet.Property
 *  com.l2fprod.common.propertysheet.PropertyEditorFactory
 *  com.l2fprod.common.propertysheet.PropertyEditorRegistry
 *  com.l2fprod.common.propertysheet.PropertyRendererRegistry
 *  com.l2fprod.common.propertysheet.PropertySheetPanel
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.editing;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import com.l2fprod.common.beans.editor.JCalendarDatePropertyEditor;
import com.l2fprod.common.beans.editor.LongPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorFactory;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.editing.ConstrainedCellRenderer;

public class EditSelectedFeatureAttributesFrame
extends JInternalFrame
implements LayerViewPanelListener {
    protected static final long serialVersionUID = 1L;
    public static final String TITLE = "";
    public static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame");
    protected JPanel mainPanel;
    protected PropertySheetPanel fieldPanel;
    protected OKCancelPanel okCancelPanel;
    protected Layer editableLayer;
    protected String findKey;
    protected Feature selectedFeature;
    protected Map layerToAttributeValuesMap = new HashMap();
    protected Map layerToAttributeDescripcionMap = new HashMap();

    public EditSelectedFeatureAttributesFrame() {
        super(TITLE);
        this.setResizable(false);
        this.setClosable(true);
        this.setMaximizable(false);
        this.setIconifiable(true);
        this.setDefaultCloseOperation(1);
        this.initialize();
        this.pack();
    }

    protected void initialize() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel(new GridBagLayout());
        }
        this.setContentPane(this.mainPanel);
        FormUtils.addRowInGBL(this.mainPanel, 0, 0, (JComponent)this.getFieldPanel());
        FormUtils.addFiller(this.mainPanel, 1, 0, this.getOkCancelPanel());
    }

    public void reset() {
        this.layerToAttributeValuesMap = new HashMap();
        this.layerToAttributeDescripcionMap = new HashMap();
    }

    public PropertySheetPanel getFieldPanel() {
        if (this.fieldPanel == null) {
            this.fieldPanel = new PropertySheetPanel();
            this.fieldPanel.setMinimumSize(new Dimension(350, 400));
            this.fieldPanel.setPreferredSize(new Dimension(350, 400));
            this.fieldPanel.setBorder((Border)BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.Fields-to-edit")));
            this.fieldPanel.setMode(0);
            this.fieldPanel.setDescriptionVisible(true);
            this.fieldPanel.setSortingCategories(false);
            this.fieldPanel.setSorting(true);
            this.fieldPanel.setToolBarVisible(true);
            PropertyEditorRegistry propertyEditors = new PropertyEditorRegistry();
            try {
                propertyEditors.registerEditor(Date.class, Class.forName("com.l2fprod.common.beans.editor.JCalendarDatePropertyEditor"));
            }
            catch (ClassNotFoundException e) {
                LOGGER.error((Object)TITLE, (Throwable)e);
            }
            this.fieldPanel.setEditorFactory((PropertyEditorFactory)propertyEditors);
        }
        return this.fieldPanel;
    }

    public void loadEditableLayerAttributes(Layer layer) {
        this.editableLayer = layer;
        if (this.editableLayer.isDataBaseDataSource()) {
            FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)this.editableLayer.getUltimateFeatureCollectionWrapper();
            this.findKey = ((AbstractJDBCDataSource)fcd.getDataAccesor()).getTableName();
        } else {
            this.findKey = this.editableLayer.getName();
        }
        this.fieldPanel.setProperties(new Property[0]);
        FeatureSchema layerSchema = this.editableLayer.getFeatureSchema();
        int i = 0;
        while (i < layerSchema.getAttributeCount()) {
            Attribute currentAttribute;
            if (i != layerSchema.getGeometryIndex() && (currentAttribute = layerSchema.getAttribute(i)).isVisibility()) {
                Property currentProperty = this.createPropertyFromAttribute(currentAttribute);
                this.fieldPanel.addProperty(currentProperty);
                ((PropertyEditorRegistry)this.fieldPanel.getEditorFactory()).registerEditor(currentProperty, this.createPropertyEditor(currentAttribute));
                ((PropertyRendererRegistry)this.fieldPanel.getRendererFactory()).registerRenderer(currentProperty, this.createPropertyRenderer(currentAttribute));
            }
            ++i;
        }
    }

    protected Property createPropertyFromAttribute(Attribute attr) {
        DefaultProperty myProperty = new DefaultProperty();
        myProperty.setName(attr.getName());
        myProperty.setDisplayName(attr.getName());
        myProperty.setShortDescription(this.getDescriptionForAttribute(attr));
        myProperty.setEditable(!attr.isPrimaryKey() && !attr.isCalculated() && !attr.getType().equals(AttributeType.OBJECT));
        myProperty.setType(attr.getType().toJavaClass());
        return myProperty;
    }

    protected String getDescriptionForAttribute(Attribute attr) {
        List attributeProperties;
        String description = attr.getPublicName();
        Map attributeMap = (Map)this.layerToAttributeDescripcionMap.get(this.findKey.toLowerCase());
        if (attributeMap != null && (attributeProperties = (List)attributeMap.get(attr.getName())) != null) {
            description = ((String)attributeProperties.get(0)).trim();
        }
        return description;
    }

    public PropertyEditor createPropertyEditor(Attribute attr) {
        if (this.isConstrained(attr)) {
            Object[] values = this.getAllowedValuesForAttribute(attr);
            return this.getComboBoxPropertyEditor(values);
        }
        if (attr.getType().equals(AttributeType.BIGDECIMAL)) {
            try {
                return (PropertyEditor)LongPropertyEditor.class.newInstance();
            }
            catch (Exception e) {
                return null;
            }
        }
        if (attr.getType().equals(AttributeType.TIMESTAMP) || attr.getType().equals(AttributeType.TIME)) {
            try {
                Class<?> dateClass = Class.forName("com.l2fprod.common.beans.editor.JCalendarDatePropertyEditor");
                if (dateClass != null) {
                    JCalendarDatePropertyEditor pe = (JCalendarDatePropertyEditor)dateClass.newInstance();
                    pe.setLocale(I18N.getLocale());
                    return pe;
                }
                return null;
            }
            catch (Exception e) {
                return null;
            }
        }
        PropertyEditorRegistry propertyEditor = (PropertyEditorRegistry)this.fieldPanel.getEditorFactory();
        return propertyEditor.getEditor(attr.getType().toJavaClass());
    }

    public TableCellRenderer createPropertyRenderer(Attribute attr) {
        if (this.isConstrained(attr)) {
            return new ConstrainedCellRenderer();
        }
        return ((PropertyRendererRegistry)this.fieldPanel.getRendererFactory()).getRenderer(attr.getType().toJavaClass());
    }

    protected Object[] getAllowedValuesForAttribute(Attribute attr) {
        Map attributeMap = (Map)this.layerToAttributeValuesMap.get(this.findKey.toLowerCase());
        if (attributeMap != null) {
            List valueList = (List)attributeMap.get(attr.getName());
            return valueList.toArray();
        }
        return null;
    }

    protected boolean isConstrained(Attribute attr) {
        List attributeProperties;
        boolean isConstrained = false;
        Map attributeMap = (Map)this.layerToAttributeDescripcionMap.get(this.findKey.toLowerCase());
        if (attributeMap != null && (attributeProperties = (List)attributeMap.get(attr.getName())) != null) {
            isConstrained = ((Number)attributeProperties.get(1)).intValue() != 0;
        }
        return isConstrained;
    }

    public void loadSelectedFeature(Feature feature) {
        this.selectedFeature = feature;
        this.modifyFrameTitle();
        Property[] properties = this.fieldPanel.getProperties();
        int i = 0;
        while (i < this.fieldPanel.getPropertyCount()) {
            Property currentProperty = properties[i];
            if (this.selectedFeature != null) {
                Object propertyValue = this.selectedFeature.getAttribute(currentProperty.getName());
                if (propertyValue == null && (propertyValue = this.getDefaultValue(currentProperty.getName())) != null) {
                    propertyValue = FeatureUtil.getGoodAttribute(this.selectedFeature.getSchema().getAttributeType(currentProperty.getName()), propertyValue);
                }
                currentProperty.setValue(propertyValue);
            } else {
                currentProperty.setValue(null);
            }
            ++i;
        }
    }

    protected Object getDefaultValue(String attrName) {
        List attributeProperties;
        Object defaultValue = null;
        Map attributeMap = (Map)this.layerToAttributeDescripcionMap.get(this.findKey.toLowerCase());
        if (attributeMap != null && (attributeProperties = (List)attributeMap.get(attrName)) != null) {
            defaultValue = attributeProperties.get(2);
        }
        return defaultValue;
    }

    protected void modifyFrameTitle() {
        if (this.selectedFeature != null) {
            String newTitle = I18N.getMessage("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.Layer-{0}", new Object[]{this.editableLayer.getName()});
            newTitle = this.selectedFeature.isUnsaved() ? String.valueOf(newTitle) + " - " + I18N.getString("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.New-feature") : String.valueOf(newTitle) + " - " + I18N.getMessage("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.Feature-{0}", new Object[]{this.selectedFeature.getPrimaryKey()});
            this.setTitle(newTitle);
        } else {
            this.setTitle(I18N.getMessage("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.Layer-{0}-{1}-NULL-feature", new Object[]{this.editableLayer.getName(), " - "}));
        }
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame.Save-changes"));
            this.okCancelPanel.setCancelVisible(false);
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (EditSelectedFeatureAttributesFrame.this.okCancelPanel.wasOKPressed()) {
                        EditSelectedFeatureAttributesFrame.this.saveChanges();
                        EditSelectedFeatureAttributesFrame.this.editableLayer.fireAppearanceChanged();
                    } else {
                        EditSelectedFeatureAttributesFrame.this.setVisible(false);
                    }
                }
            });
        }
        return this.okCancelPanel;
    }

    protected void saveChanges() {
        this.modifyFeature(this.selectedFeature);
        this.saveChanges(this.selectedFeature);
    }

    protected void modifyFeature(Feature selectedFeature) {
        if (selectedFeature == null) {
            return;
        }
        Property[] properties = this.fieldPanel.getProperties();
        int i = 0;
        while (i < properties.length) {
            Property property = properties[i];
            String propertyName = property.getName();
            if (property.isEditable()) {
                Object value = property.getValue();
                if (value != null) {
                    selectedFeature.setAttribute(propertyName, FeatureUtil.getGoodAttribute(selectedFeature.getSchema().getAttributeType(property.getName()), value));
                } else {
                    selectedFeature.setAttribute(propertyName, value);
                }
            }
            ++i;
        }
    }

    protected void saveChanges(Feature selectedFeature) {
        if (selectedFeature == null) {
            return;
        }
        try {
            this.editableLayer.getFeatureCollectionWrapper().update(selectedFeature);
            this.editableLayer.getFeatureCollectionWrapper().commit();
            this.editableLayer.fireLayerChanged(LayerEventType.COMMITED);
            this.editableLayer.setFeatureCollectionModified(false);
            this.loadSelectedFeature(selectedFeature);
        }
        catch (Exception e) {
            LOGGER.error((Object)TITLE, (Throwable)e);
        }
    }

    public PropertyEditor getComboBoxPropertyEditor(Object[] values) {
        ComboBoxPropertyEditor cmbPropertyEditor = new ComboBoxPropertyEditor();
        cmbPropertyEditor.setAvailableValues(values);
        return cmbPropertyEditor;
    }

    @Override
    public void selectionChanged() {
        int numSelectedFeatures = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(this.editableLayer);
        if (numSelectedFeatures == 0) {
            this.clear();
        } else {
            Collection<Feature> selectedFeatures = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(this.editableLayer);
            this.loadSelectedFeature(selectedFeatures.iterator().next());
        }
    }

    public void clear() {
        this.selectedFeature = null;
        this.loadSelectedFeature(null);
    }

    @Override
    public void cursorPositionChanged(String x, String y) {
    }

    @Override
    public void painted(Graphics graphics) {
    }

    @Override
    public void renderingFinished() {
    }

    @Override
    public void renderingStarted() {
    }

    public void loadAttributeMaps(Table[] attributeTables) {
        this.loadAttributeAttributeValueMap(attributeTables[0]);
        this.loadAttributeDescriptionMap(attributeTables[1]);
    }

    public void constructAttributeMaps(List<Record> values, List<Record> descriptor) {
        this.loadAttributeAttributeValueMap(values);
        this.loadAttributeDescriptionMap(descriptor);
    }

    protected void loadAttributeDescriptionMap(List<Record> records) {
        for (Record currentRecord : records) {
            if (currentRecord == null) continue;
            Map attributeMap = null;
            String layerName = ((String)currentRecord.getAttribute("FEATURE_CODE")).toLowerCase();
            attributeMap = this.layerToAttributeDescripcionMap.containsKey(layerName) ? (Map)this.layerToAttributeDescripcionMap.get(layerName) : new HashMap();
            ArrayList<Object> attributeValues = new ArrayList<Object>();
            attributeValues.add(currentRecord.getAttribute("ATT_NAME"));
            attributeValues.add(currentRecord.getAttribute("MULTIPLE"));
            attributeValues.add(currentRecord.getAttribute("DEFAULT_VALUE"));
            attributeMap.put(currentRecord.getAttribute("ATT_CODE"), attributeValues);
            this.layerToAttributeDescripcionMap.put(layerName, attributeMap);
        }
    }

    protected void loadAttributeDescriptionMap(Table table) {
        if (table == null) {
            return;
        }
        List<Record> records = table.getRecords("FEATURE_CODE");
        this.loadAttributeDescriptionMap(records);
    }

    protected void loadAttributeAttributeValueMap(List<Record> records) {
        for (Record currentRecord : records) {
            if (currentRecord == null) continue;
            Map attributeMap = null;
            List<ComboBoxPropertyEditor.Value> attributeValues = null;
            String layerName = ((String)currentRecord.getAttribute("FEATURE_CODE")).toLowerCase();
            if (this.layerToAttributeValuesMap.containsKey(layerName)) {
                attributeMap = (Map)this.layerToAttributeValuesMap.get(layerName);
                attributeValues = (List)attributeMap.get(currentRecord.getAttribute("ATT_CODE"));
                if (attributeValues == null) {
                    attributeValues = new ArrayList();
                }
            } else {
                attributeMap = new HashMap();
                attributeValues = new ArrayList();
            }
            Object featureValue = currentRecord.getAttribute("ATT_VALUE");
            Object featureValueName = currentRecord.getAttribute("ATT_VALUE_NAME");
            attributeValues.add(new ComboBoxPropertyEditor.Value(featureValue, featureValueName));
            attributeMap.put(currentRecord.getAttribute("ATT_CODE"), attributeValues);
            this.layerToAttributeValuesMap.put(layerName, attributeMap);
        }
    }

    protected void loadAttributeAttributeValueMap(Table table) {
        if (table == null) {
            return;
        }
        List<Record> records = table.getRecords("FEATURE_CODE");
        this.loadAttributeAttributeValueMap(records);
    }
}

