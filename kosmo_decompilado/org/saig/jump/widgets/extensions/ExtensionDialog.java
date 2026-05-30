/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.extensions;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInManager;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.extensions.ExtensionsListCellRenderer;
import org.saig.jump.widgets.util.ThreeStateCheckBox;

public class ExtensionDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.extension-manager");
    private JPanel toolsPanel;
    private JScrollPane extensionScrollPane;
    private JList extensionsList;
    private ThreeStateCheckBox selectAllCheckbox;
    private JCheckBox markAsDefaultCheckbox;
    private JPanel extensionDescriptionPanel;
    private JTextArea descriptionTextArea;
    private OKCancelPanel okCancelPanel;
    private Map<String, Boolean> activeExtensions = new HashMap<String, Boolean>();

    public ExtensionDialog(JFrame parent, boolean owner) {
        super(parent, TITLE, owner);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(centerPanel, 0, 0, this.getExtensionScrollPane());
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.getToolsPanel());
        FormUtils.addRowInGBL(centerPanel, 2, 0, this.getExtensionDescriptionPanel());
        mainPanel.add((Component)centerPanel, "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    private boolean isInputValid() {
        return true;
    }

    private JTextArea getDescriptionTextArea() {
        if (this.descriptionTextArea == null) {
            this.descriptionTextArea = new JTextArea();
            this.descriptionTextArea.setFont(new JLabel().getFont());
            this.descriptionTextArea.setLineWrap(true);
            this.descriptionTextArea.setWrapStyleWord(true);
            this.descriptionTextArea.setEditable(false);
        }
        return this.descriptionTextArea;
    }

    private JScrollPane getExtensionScrollPane() {
        if (this.extensionScrollPane == null) {
            this.extensionScrollPane = new JScrollPane(this.getExtensionsList(), 22, 31);
            this.extensionScrollPane.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.available-extensions")));
            this.extensionScrollPane.setMinimumSize(new Dimension(500, 200));
            this.extensionScrollPane.setPreferredSize(new Dimension(500, 200));
        }
        return this.extensionScrollPane;
    }

    public JPanel getExtensionDescriptionPanel() {
        if (this.extensionDescriptionPanel == null) {
            this.extensionDescriptionPanel = new JPanel(new GridBagLayout());
            this.extensionDescriptionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.extension-description")));
            JScrollPane descriptionScrollPane = new JScrollPane(this.getDescriptionTextArea(), 22, 31);
            descriptionScrollPane.setMinimumSize(new Dimension(400, 150));
            descriptionScrollPane.setPreferredSize(new Dimension(400, 150));
            FormUtils.addRowInGBL((JComponent)this.extensionDescriptionPanel, 0, 0, (JComponent)descriptionScrollPane, true, true);
        }
        return this.extensionDescriptionPanel;
    }

    public void initializeData(PlugInManager plugInManager) {
        this.reset();
        boolean anyActive = false;
        boolean allActive = true;
        Object[] extensions = plugInManager.getConfigurations().toArray();
        ArrayList<Extension> ext_ord = new ArrayList<Extension>();
        int i = 0;
        while (i < extensions.length) {
            Extension ext = (Extension)extensions[i];
            ext_ord.add(ext);
            if (ext.isActive()) {
                this.activeExtensions.put(ext.getName(), true);
                anyActive = true;
            } else {
                allActive = false;
            }
            ++i;
        }
        Collections.sort(ext_ord, new MyExtensionsComparator());
        this.extensionsList.setListData(ext_ord.toArray());
        if (allActive) {
            this.selectAllCheckbox.setSelected(true);
            this.selectAllCheckbox.setState(ThreeStateCheckBox.SELECTED);
        } else if (anyActive) {
            this.selectAllCheckbox.setState(ThreeStateCheckBox.DONT_CARE);
        } else {
            this.selectAllCheckbox.setState(ThreeStateCheckBox.NOT_SELECTED);
        }
    }

    private void reset() {
        this.extensionsList.removeAll();
        this.descriptionTextArea.setText("");
    }

    private JList getExtensionsList() {
        if (this.extensionsList == null) {
            this.extensionsList = new JList();
            this.extensionsList.setCellRenderer(new ExtensionsListCellRenderer());
            this.extensionsList.addMouseListener(new ExtensionListListener());
            this.extensionsList.addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent ev) {
                    Extension selectedExtension = (Extension)ExtensionDialog.this.extensionsList.getSelectedValue();
                    ExtensionDialog.this.descriptionTextArea.setText(selectedExtension.getDescription());
                }
            });
        }
        return this.extensionsList;
    }

    public JPanel getToolsPanel() {
        if (this.toolsPanel == null) {
            this.toolsPanel = new JPanel(new FlowLayout());
            this.selectAllCheckbox = new ThreeStateCheckBox(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.select-all"));
            this.selectAllCheckbox.setToolTipText(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.select-unselect-all-the-extensions-in-the-list"));
            this.selectAllCheckbox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ev) {
                    boolean selected = ExtensionDialog.this.selectAllCheckbox.isSelected();
                    int i = 0;
                    while (i < ExtensionDialog.this.extensionsList.getModel().getSize()) {
                        Extension ext = (Extension)ExtensionDialog.this.extensionsList.getModel().getElementAt(i);
                        ext.setActive(selected);
                        ++i;
                    }
                    ExtensionDialog.this.extensionsList.repaint();
                }
            });
            this.markAsDefaultCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.mark-as-default"));
            this.markAsDefaultCheckbox.setToolTipText(I18N.getString("org.saig.jump.widgets.extensions.ExtensionDialog.if-it-is-selected-the-current-extensions-configuration-will-be-the-default-configuration-for-all-the-projects"));
            this.toolsPanel.add(this.selectAllCheckbox);
            this.toolsPanel.add(this.markAsDefaultCheckbox);
        }
        return this.toolsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okCancelPanel) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        }
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public List<Extension> getExtensions() {
        ArrayList<Extension> extensions = new ArrayList<Extension>();
        int i = 0;
        while (i < this.extensionsList.getModel().getSize()) {
            extensions.add((Extension)this.extensionsList.getModel().getElementAt(i));
            ++i;
        }
        return extensions;
    }

    public Map<String, Boolean> getExtensionActiveMap() {
        return this.activeExtensions;
    }

    public boolean isMarkAsDefault() {
        return this.markAsDefaultCheckbox.isSelected();
    }

    private class ExtensionListListener
    implements MouseListener {
        private ExtensionListListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getX() < 20) {
                this.doCheck(e.getPoint());
                ExtensionDialog.this.extensionsList.repaint();
            }
        }

        private void doCheck(Point point) {
            Extension ext;
            ext.setActive(!(ext = (Extension)ExtensionDialog.this.extensionsList.getModel().getElementAt(ExtensionDialog.this.extensionsList.locationToIndex(point))).isActive());
            this.refreshSelectAllCheckbox();
        }

        private void refreshSelectAllCheckbox() {
            boolean allActive = true;
            boolean anyActive = false;
            int size = ExtensionDialog.this.extensionsList.getModel().getSize();
            int i = 0;
            while (i < size) {
                Extension ext = (Extension)ExtensionDialog.this.extensionsList.getModel().getElementAt(i);
                anyActive = anyActive || ext.isActive();
                allActive = allActive && ext.isActive();
                ++i;
            }
            if (allActive) {
                ExtensionDialog.this.selectAllCheckbox.setSelected(true);
                ExtensionDialog.this.selectAllCheckbox.setState(ThreeStateCheckBox.SELECTED);
            } else if (anyActive) {
                ExtensionDialog.this.selectAllCheckbox.setState(ThreeStateCheckBox.DONT_CARE);
            } else {
                ExtensionDialog.this.selectAllCheckbox.setState(ThreeStateCheckBox.NOT_SELECTED);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    private class MyExtensionsComparator
    implements Comparator {
        private MyExtensionsComparator() {
        }

        public int compare(Object o1, Object o2) {
            String one = ((Extension)o1).getName();
            String two = ((Extension)o2).getName();
            return one.compareTo(two);
        }
    }
}

