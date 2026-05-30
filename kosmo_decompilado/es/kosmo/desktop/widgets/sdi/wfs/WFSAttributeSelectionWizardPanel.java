/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.deegree.datatypes.QualifiedName
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.widgets.sdi.wfs.WFSAttributesCheckboxTree;
import es.kosmo.desktop.widgets.sdi.wfs.WFSOptionsWizardPanel;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.lang.ArrayUtils;
import org.deegree.datatypes.QualifiedName;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.CheckBoxNode;
import org.saig.jump.widgets.util.CheckBoxTreeSelectionListener;

public class WFSAttributeSelectionWizardPanel
extends AbstractWizardPanel
implements CheckBoxTreeSelectionListener,
TreeSelectionListener {
    private static final long serialVersionUID = 1L;
    private WFSAttributesCheckboxTree tree;
    private List<WFSFeatureTypeInfo> selectedFeatureTypesInfos;
    private EnableableToolBar toolbar;
    protected EnableCheck featTypeSelectionEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            DefaultMutableTreeNode treeNode;
            Object[] selectionPaths = WFSAttributeSelectionWizardPanel.this.tree.getSelectionPaths();
            if (ArrayUtils.isEmpty((Object[])selectionPaths) || selectionPaths.length > 1) {
                return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.Only-one-feature-type-must-be-selected");
            }
            Object node = ((TreePath)selectionPaths[0]).getLastPathComponent();
            if (node != null && node instanceof DefaultMutableTreeNode && (treeNode = (DefaultMutableTreeNode)node).isLeaf()) {
                return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.Only-one-feature-type-must-be-selected");
            }
            return null;
        }
    };
    protected EnableCheck anyAttributeSelectedEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            DefaultMutableTreeNode treeNode;
            Object node = WFSAttributeSelectionWizardPanel.this.tree.getSelectionPath().getLastPathComponent();
            if (node != null && node instanceof DefaultMutableTreeNode && !(treeNode = (DefaultMutableTreeNode)node).isLeaf()) {
                boolean anySelected = false;
                int i = 0;
                while (i < treeNode.getChildCount() && !anySelected) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)treeNode.getChildAt(i);
                    Object currentUserObject = childNode.getUserObject();
                    if (currentUserObject instanceof CheckBoxNode) {
                        CheckBoxNode checkBoxChildNode = (CheckBoxNode)currentUserObject;
                        anySelected = checkBoxChildNode.isEditable() && checkBoxChildNode.isSelected();
                    }
                    ++i;
                }
                if (!anySelected) {
                    return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.At-least-one-selected-attribute-must-exist");
                }
            }
            return null;
        }
    };
    protected EnableCheck anyAttributeUnselectedEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            DefaultMutableTreeNode treeNode;
            Object node = WFSAttributeSelectionWizardPanel.this.tree.getSelectionPath().getLastPathComponent();
            if (node != null && node instanceof DefaultMutableTreeNode && !(treeNode = (DefaultMutableTreeNode)node).isLeaf()) {
                boolean anyUnselected = false;
                int i = 0;
                while (i < treeNode.getChildCount() && !anyUnselected) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)treeNode.getChildAt(i);
                    Object currentUserObject = childNode.getUserObject();
                    if (currentUserObject instanceof CheckBoxNode) {
                        CheckBoxNode checkBoxChildNode = (CheckBoxNode)currentUserObject;
                        anyUnselected = checkBoxChildNode.isEditable() && !checkBoxChildNode.isSelected();
                    }
                    ++i;
                }
                if (!anyUnselected) {
                    return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.At-least-one-unselected-attribute-must-exist");
                }
            }
            return null;
        }
    };

    public WFSAttributeSelectionWizardPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.tree = new WFSAttributesCheckboxTree();
        this.tree.addCheckBoxTreeSelectionListener(this);
        this.tree.getSelectionModel().addTreeSelectionListener(this);
        JScrollPane pane = new JScrollPane(this.tree);
        FormUtils.addRowInGBL(this, 0, 0, this.getToolbar());
        FormUtils.addRowInGBL(this, 1, 0, pane);
        FormUtils.addFiller(this, 2, 0);
    }

    private EnableableToolBar getToolbar() {
        if (this.toolbar == null) {
            this.toolbar = new EnableableToolBar();
            this.toolbar.setFloatable(false);
            this.toolbar.add(new JButton(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.Select-all-the-attributes-from-the-selected-feature-type"), DesktopIconLoader.icon("attribute_select.png"), new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WFSAttributeSelectionWizardPanel.this.setAttributesSelected(true);
                }
            }, new MultiEnableCheck().add(this.featTypeSelectionEnableCheck).add(this.anyAttributeUnselectedEnableCheck));
            this.toolbar.add(new JButton(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.Unselect-all-the-attributes-from-the-selected-feature-type"), DesktopIconLoader.icon("attribute_unselect.png"), new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WFSAttributeSelectionWizardPanel.this.setAttributesSelected(false);
                }
            }, new MultiEnableCheck().add(this.featTypeSelectionEnableCheck).add(this.anyAttributeSelectedEnableCheck));
        }
        return this.toolbar;
    }

    protected void setAttributesSelected(boolean selected) {
        DefaultMutableTreeNode dmtn;
        Object node = this.tree.getLastSelectedPathComponent();
        if (node != null && node instanceof DefaultMutableTreeNode && !(dmtn = (DefaultMutableTreeNode)node).isLeaf()) {
            int i = 0;
            while (i < dmtn.getChildCount()) {
                CheckBoxNode checkBoxChildNode;
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)dmtn.getChildAt(i);
                Object currentUserObject = childNode.getUserObject();
                if (currentUserObject instanceof CheckBoxNode && (checkBoxChildNode = (CheckBoxNode)currentUserObject).isEditable()) {
                    checkBoxChildNode.setSelected(selected);
                }
                ++i;
            }
            this.tree.fireCheckBoxTreeSelectionChanged();
            this.tree.repaint();
        }
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.selectedFeatureTypesInfos = (List)dataMap.get("SELECTED_FEATURE_TYPES");
        TreeMap featureTypesMap = new TreeMap();
        TreeMap<String, Boolean> featureTypesEnabledMap = new TreeMap<String, Boolean>();
        for (WFSFeatureTypeInfo featTypeInfo : this.selectedFeatureTypesInfos) {
            List<String> attrNames = featTypeInfo.getAvailableAttributes();
            List<QualifiedName> geoProperties = featTypeInfo.getGeometryAttributes();
            Vector<String> attrs = new Vector<String>(attrNames);
            attrs.addAll(geoProperties);
            featureTypesMap.put(featTypeInfo.getPrettyString(), attrs);
            featureTypesEnabledMap.put(featTypeInfo.getPrettyString(), featTypeInfo.isEnabled());
        }
        this.tree.buildTree(featureTypesMap, featureTypesEnabledMap);
        this.toolbar.updateEnabledState();
    }

    @Override
    public void exitingToRight() throws Exception {
        Map<String, List<?>> selectedElements = this.tree.getSelectedElements();
        for (WFSFeatureTypeInfo info : this.selectedFeatureTypesInfos) {
            String infoPrettyStr = info.getPrettyString();
            if (selectedElements.containsKey(infoPrettyStr)) {
                this.updateInfo(infoPrettyStr, selectedElements.get(infoPrettyStr));
                continue;
            }
            info.setEnabled(false);
        }
    }

    private void updateInfo(String featTypeName, List<?> elements) {
        WFSFeatureTypeInfo info = null;
        boolean found = false;
        Iterator<WFSFeatureTypeInfo> iterator = this.selectedFeatureTypesInfos.iterator();
        while (iterator.hasNext() && !found) {
            WFSFeatureTypeInfo currentInfo = iterator.next();
            if (!currentInfo.getPrettyString().equals(featTypeName)) continue;
            info = currentInfo;
            found = true;
        }
        if (info != null) {
            ArrayList<String> selectedAttrs = new ArrayList<String>();
            for (Object currentElement : elements) {
                if (!(currentElement instanceof String)) continue;
                selectedAttrs.add((String)currentElement);
            }
            info.setSelectedAttributes(selectedAttrs);
            if (info.getGeometryAttributes().size() == 1) {
                info.setGeomAttrName(info.getGeometryAttributes().get(0));
            }
            info.setEnabled(true);
        }
    }

    @Override
    public boolean isInputValid() {
        boolean ok = false;
        Map<String, List<?>> selectedElements = this.tree.getSelectedElements();
        Iterator<String> itFeatureTypes = selectedElements.keySet().iterator();
        while (itFeatureTypes.hasNext() && !ok) {
            String featTypeName = itFeatureTypes.next();
            List<?> attrs = selectedElements.get(featTypeName);
            boolean geomAttrSelected = false;
            Iterator<?> itAttr = attrs.iterator();
            while (itAttr.hasNext() && !geomAttrSelected) {
                Object currentAttr = itAttr.next();
                if (!(currentAttr instanceof QualifiedName)) continue;
                geomAttrSelected = true;
            }
            ok = geomAttrSelected;
        }
        return ok;
    }

    @Override
    public boolean isPanelOk() {
        return true;
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        return WFSOptionsWizardPanel.class.getName();
    }

    @Override
    public String getInstructions() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.select-the-attributes-that-you-want-to-load-for-each-feature-type");
    }

    @Override
    public String getTitle() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel.select-attributes");
    }

    @Override
    public void selectionChanged() {
        this.toolbar.updateEnabledState();
        this.inputChangedFirer.fire();
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        this.toolbar.updateEnabledState();
        this.inputChangedFirer.fire();
    }
}

