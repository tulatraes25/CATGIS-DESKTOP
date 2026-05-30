/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.gui.components;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class AbstractSearchablePanel
extends JPanel
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(AbstractSearchablePanel.class);
    protected String borderTitle;
    protected FeatureSchema schema;
    protected String searchableAttrName;
    protected JLabel searchLabel;
    protected JTextField searchTextField;
    protected JButton searchButton;
    protected JLabel attributeSelectionLabel;
    protected JComboBox attributeSelectionComboBox;
    protected JList candidatesFoundList;
    protected DefaultListModel candidatesFoundListModel;
    protected FilterFactory factory = FilterFactory.createFilterFactory();

    protected void initializeGUI() {
        this.setBorder(BorderFactory.createTitledBorder(this.borderTitle));
        this.setLayout(new GridBagLayout());
        this.searchLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Search-text")) + ": ");
        this.searchTextField = new JTextField();
        Dimension dim = new Dimension(200, 20);
        this.searchTextField.setMinimumSize(dim);
        this.searchTextField.setPreferredSize(dim);
        this.searchButton = new JButton(GUIUtil.resize(IconLoader.icon("Magnify.gif"), 16));
        this.searchButton.setToolTipText(I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Search-elements-by-using-the-inserted-string"));
        this.searchButton.addActionListener(this);
        if (this.schema != null) {
            this.attributeSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Select-search-attribute")) + ": ");
            this.attributeSelectionComboBox = new JComboBox<String>(this.getAttributeNames());
        }
        this.candidatesFoundListModel = new DefaultListModel();
        this.candidatesFoundList = new JList(this.candidatesFoundListModel);
        this.candidatesFoundList.setCellRenderer(this.getCellRenderer());
        JScrollPane candidatesScrollPane = new JScrollPane(this.candidatesFoundList, 22, 31);
        dim = new Dimension(300, 100);
        candidatesScrollPane.setMinimumSize(dim);
        candidatesScrollPane.setPreferredSize(dim);
        if (this.schema != null) {
            FormUtils.addRowInGBL((JComponent)this, 0, 0, this.attributeSelectionLabel, (JComponent)this.attributeSelectionComboBox, true);
        }
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.searchLabel, (JComponent)this.searchTextField, false);
        FormUtils.addRowInGBL((JComponent)this, 1, 30, (JComponent)this.searchButton, false, false);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, (JComponent)candidatesScrollPane, true, true);
        FormUtils.addFiller(this, 3, 0);
    }

    private Vector<String> getAttributeNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Attribute attr : this.schema.getAttributes().values()) {
            if (attr.isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY)) continue;
            names.add(attr.getName());
        }
        Vector<String> attrNames = new Vector<String>(names);
        Collections.sort(attrNames, Collator.getInstance(LocaleManager.getActiveLocale()));
        return attrNames;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.searchButton)) {
            String selectedText = this.searchTextField.getText().trim();
            if (selectedText.length() < 3) {
                DialogFactory.showWarningDialog(this, I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.The-search-string-length-must-be-greater-than-3-characters"), I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Invalid-search-string"));
                return;
            }
            String selectedAttrName = null;
            selectedAttrName = this.schema != null ? (String)this.attributeSelectionComboBox.getSelectedItem() : this.searchableAttrName;
            try {
                AttributeExpressionImpl2 attribute = new AttributeExpressionImpl2(selectedAttrName);
                final LikeFilter likeFilter = this.factory.createLikeFilter();
                likeFilter.setPattern(new LiteralExpressionImpl("*" + selectedText + "*"), "*", "?", "\\");
                likeFilter.setValue(attribute);
                final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
                final String attrName = selectedAttrName;
                progressDialog.setTitle(I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Search"));
                progressDialog.addComponentListener(new ComponentAdapter(){

                    @Override
                    public void componentShown(ComponentEvent e) {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    try {
                                        progressDialog.report(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.AbstractSearchablePanel.Executing-the-selected-search")) + "...");
                                        AbstractSearchablePanel.this.searchElements(attrName, likeFilter);
                                    }
                                    catch (Exception e) {
                                        LOGGER.error((Object)"", (Throwable)e);
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
                GUIUtil.centre(progressDialog, this.getRootPane().getParent());
                progressDialog.setVisible(true);
            }
            catch (IllegalFilterException illegalFilterException) {
                // empty catch block
            }
        }
    }

    protected abstract void searchElements(String var1, Filter var2);

    protected ListCellRenderer getCellRenderer() {
        return new DefaultListCellRenderer();
    }

    public void setListSelectionRenderer(ListSelectionListener listener) {
        this.candidatesFoundList.addListSelectionListener(listener);
    }

    public Object[] getSelectedElements() {
        return this.candidatesFoundList.getSelectedValues();
    }
}

