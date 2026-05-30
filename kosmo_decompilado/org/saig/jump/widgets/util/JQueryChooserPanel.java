/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserDialog;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.CalcFileDataSource;
import org.saig.jump.plugin.datasource.DGNFileDataSource;
import org.saig.jump.plugin.datasource.DWGFileDataSource;
import org.saig.jump.plugin.datasource.DXFFileDataSource;
import org.saig.jump.plugin.datasource.ExcelFileDataSource;
import org.saig.jump.plugin.datasource.IndexedShapeFileDataSource;
import org.saig.jump.plugin.datasource.JumpJDBCDataSource;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.util.AbstractSelectablePanel;
import org.saig.jump.widgets.util.DialogFactory;

public class JQueryChooserPanel
extends AbstractSelectablePanel {
    private static final long serialVersionUID = 1L;
    private static final String TO_MEMORY = I18N.getString(JQueryChooserPanel.class, "in-memory");
    private static final String NO_ASSIGN = I18N.getString(JQueryChooserPanel.class, "not-assigned");
    private static final String CHOOSE_TARGET = I18N.getString(JQueryChooserPanel.class, "choose-target");
    private static final String BUTTON_TITLE = "...";
    private JRadioButton toMemRadioButton;
    private JRadioButton chooseRadioButton;
    private ButtonGroup buttonGroup;
    private JButton chooserButton;
    private DataSourceQuery dataSourceQuery;
    private String chooserDialogTitle;
    private final boolean showMemoryOption;

    public JQueryChooserPanel(String borderTitle, String chooserDialogTitle, boolean isSelectable) {
        this(borderTitle, chooserDialogTitle, isSelectable, true, true);
    }

    public JQueryChooserPanel(String borderTitle, String chooserDialogTitle, boolean isSelectable, boolean isCheckBox, boolean showMemoryOption) {
        super(borderTitle, isSelectable, isCheckBox);
        this.chooserDialogTitle = chooserDialogTitle;
        this.showMemoryOption = showMemoryOption;
        this.initComponents();
    }

    private DataSourceQuery chooseQuery() {
        String format = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(SaveDatasetAsPlugIn.LAST_FORMAT_KEY);
        DataSourceQueryChooserDialog dialog = new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard()).getSaveDataSourceQueryChoosers(), JUMPWorkbench.getFrameInstance(), this.chooserDialogTitle, true);
        if (format != null) {
            dialog.setSelectedFormat(format);
        }
        dialog.refreshPath();
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (dialog.wasOKPressed()) {
            PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(SaveDatasetAsPlugIn.LAST_FORMAT_KEY, dialog.getSelectedFormat());
            return dialog.getCurrentChooser().getDataSourceQueries().iterator().next();
        }
        return null;
    }

    public DataSourceQuery getDataSourceQuery() {
        if (this.isSelected()) {
            if (this.chooseRadioButton.isSelected()) {
                return this.dataSourceQuery;
            }
            if (this.toMemRadioButton.isSelected()) {
                return new DataSourceQuery(new MemoryDataSource(), null, null);
            }
        }
        return null;
    }

    public boolean isInputValid() {
        return this.isInputValid(true);
    }

    public boolean isInputValid(boolean showWarningMessage) {
        if (!this.isSelected()) {
            return true;
        }
        if (this.chooseRadioButton.isSelected() && this.dataSourceQuery == null) {
            if (showWarningMessage) {
                DialogFactory.showWarningDialog(this, I18N.getMessage(this.getClass(), "panel-{0}-is-{1}", new Object[]{this.borderTitle, NO_ASSIGN}), I18N.getString(this.getClass(), "warning"));
            }
            return false;
        }
        return true;
    }

    public String getDataSourceQueryString(DataSourceQuery dataSourceQuery) {
        if (dataSourceQuery == null) {
            return NO_ASSIGN;
        }
        String type = "";
        DataSource ds = dataSourceQuery.getDataSource();
        if (ds instanceof JumpJDBCDataSource) {
            AbstractDataSource da = ((JumpJDBCDataSource)ds).getConnection().getDataSources().iterator().next();
            if (da instanceof AbstractJDBCDataSource) {
                AbstractJDBCDataSource jdbc = (AbstractJDBCDataSource)da;
                type = LayerUtil.getAbstractJDBCDataSourceString(jdbc);
            } else {
                type = I18N.getString("org.saig.jump.widgets.util.JQueryChooserPanel.Unknown");
            }
        }
        if (ds instanceof IndexedShapeFileDataSource) {
            IndexedShapeFileDataSource pshds = (IndexedShapeFileDataSource)ds;
            type = "SHP - " + pshds.getProperties().get("File");
        } else if (ds instanceof DXFFileDataSource) {
            DXFFileDataSource dxfds = (DXFFileDataSource)ds;
            type = "DXF - " + dxfds.getProperties().get("File");
        } else if (ds instanceof DWGFileDataSource) {
            DWGFileDataSource dwgfd = (DWGFileDataSource)ds;
            type = "DWG - " + dwgfd.getProperties().get("File");
        } else if (ds instanceof DGNFileDataSource) {
            DGNFileDataSource dgnfd = (DGNFileDataSource)ds;
            type = "DGN - " + dgnfd.getProperties().get("File");
        } else if (ds instanceof ExcelFileDataSource) {
            ExcelFileDataSource efds = (ExcelFileDataSource)ds;
            type = "Excel - " + efds.getProperties().get("File");
        } else if (ds instanceof CalcFileDataSource) {
            CalcFileDataSource cfds = (CalcFileDataSource)ds;
            type = "Calc - " + cfds.getProperties().get("File");
        }
        return type;
    }

    public String getChooserDialogTitle() {
        return this.chooserDialogTitle;
    }

    public void setChooserDialogTitle(String chooserDialogTitle) {
        this.chooserDialogTitle = chooserDialogTitle;
    }

    @Override
    public void initComponents() {
        this.toMemRadioButton = new JRadioButton(TO_MEMORY);
        this.toMemRadioButton.setSelected(true);
        this.toMemRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        this.chooseRadioButton = new JRadioButton(CHOOSE_TARGET);
        this.chooseRadioButton.setPreferredSize(new Dimension(200, 20));
        this.chooseRadioButton.setSelected(false);
        this.buttonGroup = new ButtonGroup();
        this.buttonGroup.add(this.toMemRadioButton);
        this.buttonGroup.add(this.chooseRadioButton);
        this.chooserButton = new JButton(BUTTON_TITLE);
        this.chooserButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DataSourceQuery tempQuery = JQueryChooserPanel.this.chooseQuery();
                if (tempQuery != null) {
                    JQueryChooserPanel.this.dataSourceQuery = tempQuery;
                    String queryString = JQueryChooserPanel.this.getDataSourceQueryString(JQueryChooserPanel.this.dataSourceQuery);
                    JQueryChooserPanel.this.chooseRadioButton.setSelected(true);
                    JQueryChooserPanel.this.chooseRadioButton.setText(queryString);
                    JQueryChooserPanel.this.chooseRadioButton.setToolTipText(queryString);
                    JQueryChooserPanel.this.firePanelChanged();
                }
            }
        });
        if (this.showMemoryOption) {
            FormUtils.addRowInGBL(this, 1, 0, this.toMemRadioButton);
        } else {
            this.chooseRadioButton.setSelected(true);
        }
        FormUtils.addRowInGBL((JComponent)this, 2, 0, (JComponent)this.chooseRadioButton, true, false, true);
        FormUtils.addRowInGBL((JComponent)this, 2, 2, (JComponent)this.chooserButton, false, false, true);
        FormUtils.addFiller(this, 3, 0);
        if (this.isSelectable) {
            this.refreshComponents(false);
        }
    }

    @Override
    protected void refreshComponents(boolean enable) {
        this.toMemRadioButton.setEnabled(enable);
        this.chooseRadioButton.setEnabled(enable);
        this.chooserButton.setEnabled(enable);
        this.repaint();
    }

    @Override
    protected void selectionStateChanged(boolean enable) {
        this.refreshComponents(enable);
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        if (this.chooseRadioButton != null) {
            this.chooseRadioButton.setOpaque(isOpaque);
        }
        if (this.chooserButton != null) {
            this.chooserButton.setOpaque(isOpaque);
        }
        if (this.toMemRadioButton != null) {
            this.toMemRadioButton.setOpaque(isOpaque);
        }
        if (this.selectionComponent != null) {
            this.selectionComponent.setOpaque(isOpaque);
        }
    }
}

