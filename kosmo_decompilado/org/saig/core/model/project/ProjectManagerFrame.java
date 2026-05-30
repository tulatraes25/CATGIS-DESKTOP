/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.model.project;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.widgets.locale.LanguageRenderer;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigAvailablesLanguageDialog;
import org.saig.jump.widgets.util.project.ProjectManagerDialog;

public class ProjectManagerFrame
extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    public static final String X_KEY = String.valueOf(ProjectManagerDialog.class.getName()) + " - PROJECT_MANAGER_DIALOG_X_KEY";
    public static final String Y_KEY = String.valueOf(ProjectManagerDialog.class.getName()) + " - PROJECT_MANAGER_DIALOG_Y_KEY";
    public static final String VISIBLE_KEY = String.valueOf(ProjectManagerDialog.class.getName()) + " - PROJECT_MANAGER_DIALOG_VISIBLE_KEY";
    public static final Insets BUTTON_INSETS = new Insets(2, 2, 2, 2);
    protected JPanel metadataPanel;
    protected JTextField projectNameTextField;
    protected JTextField projectAuthorTextField;
    protected JTextField projectKosmoVersionTextField;
    protected JTextField projectCreationDateTextField;
    protected JTextField projectLastModificationDateTextField;
    protected JTextField projectPathTextField;
    protected JTextArea projectDescriptionTextArea;
    protected JComboBox defaultLanguageComboBox;
    protected JButton applyLanguageButton;
    protected JButton selectAvailablesLanguagesButton;
    protected ComboBoxModel languageModel;

    public ProjectManagerFrame() {
        this.setResizable(false);
        this.setClosable(true);
        this.setIconifiable(true);
        this.setSize(300, 300);
        try {
            this.initialize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.setTitle(I18N.getString("org.saig.core.model.proyect.ProyectManagerFrame.untitled"));
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                ProjectManagerFrame.this.hideAllPopups();
            }
        });
    }

    public void loadWindowState() {
        WorkbenchContext workbenchContext = JUMPWorkbench.getFrameInstance().getContext();
        int x = new Integer((String)PersistentBlackboardPlugIn.get(workbenchContext).get(X_KEY, "0"));
        int y = new Integer((String)PersistentBlackboardPlugIn.get(workbenchContext).get(Y_KEY, "0"));
        boolean visible = new Boolean((String)PersistentBlackboardPlugIn.get(workbenchContext).get(VISIBLE_KEY, "false"));
        this.setLocation(x, y);
        this.setVisible(visible);
    }

    public void saveWindowState() {
        WorkbenchContext wc = JUMPWorkbench.getFrameInstance().getContext();
        PersistentBlackboardPlugIn.get(wc).put(X_KEY, new Integer(this.getX()).toString());
        PersistentBlackboardPlugIn.get(wc).put(Y_KEY, new Integer(this.getY()).toString());
        PersistentBlackboardPlugIn.get(wc).put(VISIBLE_KEY, new Boolean(this.isVisible()).toString());
    }

    private void initialize() throws Exception {
        this.setDefaultCloseOperation(1);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getMetadataPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getTabPanel());
        this.setContentPane(mainPanel);
        this.pack();
    }

    private JPanel getMetadataPanel() throws Exception {
        if (this.metadataPanel == null) {
            this.metadataPanel = new JPanel(new GridBagLayout());
            this.metadataPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "project-data")));
            JLabel projectNameLabel = new JLabel(I18N.getString(this.getClass(), "name"));
            this.projectNameTextField = new JTextField();
            JLabel projectAuthorLabel = new JLabel(I18N.getString(this.getClass(), "author"));
            this.projectAuthorTextField = new JTextField();
            JLabel projectKosmoVersionLabel = new JLabel(I18N.getString(this.getClass(), "kosmo-version"));
            this.projectKosmoVersionTextField = new JTextField();
            this.projectKosmoVersionTextField.setEditable(false);
            JLabel projectPathLabel = new JLabel(I18N.getString(this.getClass(), "file-path"));
            this.projectPathTextField = new JTextField();
            this.projectPathTextField.setEditable(false);
            Dimension dim = new Dimension(150, 20);
            JLabel projectCreationDateLabel = new JLabel(I18N.getString(this.getClass(), "creation-date"));
            this.projectCreationDateTextField = new JTextField();
            this.projectCreationDateTextField.setMinimumSize(dim);
            this.projectCreationDateTextField.setPreferredSize(dim);
            this.projectCreationDateTextField.setEditable(false);
            JLabel projectLastModificationDate = new JLabel(I18N.getString(this.getClass(), "last-modification-date"));
            this.projectLastModificationDateTextField = new JTextField();
            this.projectLastModificationDateTextField.setMinimumSize(dim);
            this.projectLastModificationDateTextField.setPreferredSize(dim);
            this.projectLastModificationDateTextField.setEditable(false);
            JLabel projectDescriptionLabel = new JLabel(String.valueOf(I18N.getString(this.getClass(), "description")) + ":");
            JScrollPane descriptionScrollPane = new JScrollPane(22, 31);
            this.projectDescriptionTextArea = new JTextArea();
            this.projectDescriptionTextArea.setLineWrap(true);
            this.projectDescriptionTextArea.setWrapStyleWord(true);
            this.projectDescriptionTextArea.setRows(2);
            this.projectDescriptionTextArea.setFont(projectDescriptionLabel.getFont());
            descriptionScrollPane.setViewportView(this.projectDescriptionTextArea);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 0, 0, projectNameLabel, (JComponent)this.projectNameTextField, true);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 1, 0, projectAuthorLabel, (JComponent)this.projectAuthorTextField, true);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 2, 0, projectKosmoVersionLabel, (JComponent)this.projectKosmoVersionTextField, true);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 3, 0, projectPathLabel, (JComponent)this.projectPathTextField, true);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 4, 0, projectCreationDateLabel, (JComponent)this.projectCreationDateTextField, false);
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 4, 50, projectLastModificationDate, (JComponent)this.projectLastModificationDateTextField);
            PersistentBlackboardPlugIn persistentBlackboardPlugIn = new PersistentBlackboardPlugIn();
            persistentBlackboardPlugIn.initialize(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
            ArrayList<Locale> availablesLanguages = new ArrayList<Locale>();
            availablesLanguages.add(I18N.getLocale());
            this.languageModel = new DefaultComboBoxModel<Object>(availablesLanguages.toArray());
            this.defaultLanguageComboBox = new JComboBox(this.languageModel);
            this.defaultLanguageComboBox.setRenderer(new LanguageRenderer());
            FormUtils.addRowInGBL((JComponent)this.metadataPanel, 5, 0, String.valueOf(I18N.getString("org.saig.core.model.project.ProjectManagerFrame.Language")) + ":", (JComponent)this.defaultLanguageComboBox);
            JPanel buttonsPanel = new JPanel(new FlowLayout());
            this.applyLanguageButton = new JButton(I18N.getString("org.saig.core.model.project.ProjectManagerFrame.Change-language"));
            this.applyLanguageButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    Locale language = (Locale)ProjectManagerFrame.this.defaultLanguageComboBox.getSelectedItem();
                    Project prj = JUMPWorkbench.getFrameInstance().getContext().getProject();
                    if (prj != null) {
                        prj.setActiveLocale(language);
                    }
                    List layerables = JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
                    for (AbstractLayerable layer : layerables) {
                        layer.fireLayerChanged(LayerEventType.APPEARANCE_CHANGED);
                    }
                }
            });
            buttonsPanel.add(this.applyLanguageButton);
            this.selectAvailablesLanguagesButton = new JButton(String.valueOf(I18N.getString("org.saig.core.model.project.ProjectManagerFrame.Select-available-languages")) + "...");
            this.selectAvailablesLanguagesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    new ConfigAvailablesLanguageDialog(JUMPWorkbench.getFrameInstance(), true);
                    ProjectManagerFrame.this.refreshAvailableLanguageComboBox();
                }
            });
            buttonsPanel.add(this.selectAvailablesLanguagesButton);
            FormUtils.addRowInGBL(this.metadataPanel, 5, 40, buttonsPanel);
            FormUtils.addRowInGBL(this.metadataPanel, 6, 0, projectDescriptionLabel);
            FormUtils.addRowInGBL(this.metadataPanel, 7, 0, descriptionScrollPane);
        }
        return this.metadataPanel;
    }

    private void refreshAvailableLanguageComboBox() {
        Project prj = JUMPWorkbench.getFrameInstance().getContext().getProject();
        this.languageModel = new DefaultComboBoxModel<Object>(prj.getOrderedAvailablesLocales().toArray());
        this.languageModel.setSelectedItem(prj.getActiveLocale());
        this.defaultLanguageComboBox.setModel(this.languageModel);
    }

    private JTabbedPane getTabPanel() {
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.setOpaque(true);
        jTabbedPane.addTab(I18N.getString("org.saig.core.model.proyect.ProyectManagerFrame.views"), IconLoader.icon("world.png"), JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTaskManagerPanel());
        jTabbedPane.addTab(I18N.getString("org.saig.core.model.proyect.ProyectManagerFrame.tables"), IconLoader.icon("properties.gif"), JUMPWorkbench.getFrameInstance().getContext().getDataManager().getDataManagerPanel());
        jTabbedPane.addTab(I18N.getString("org.saig.core.model.proyect.ProyectManagerFrame.maps"), IconLoader.icon("ver_cartografia_no_edit.gif"), JUMPWorkbench.getFrameInstance().getContext().getPrintLayoutManager().getPrintLayoutManagerFrame());
        jTabbedPane.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent evt) {
                ProjectManagerFrame.this.hideAllPopups();
            }
        });
        return jTabbedPane;
    }

    @Override
    public void setTitle(String newTitle) {
        String title = String.valueOf(I18N.getString("org.saig.core.model.proyect.ProyectManagerFrame.project-manager")) + " - [" + newTitle + "]";
        super.setTitle(title);
    }

    public void hideAllPopups() {
        WorkbenchContext wc = JUMPWorkbench.getFrameInstance().getContext();
        wc.getTaskManager().getTaskManagerPanel().hidePopUpMenu();
        wc.getDataManager().getDataManagerPanel().hidePopUpMenu();
        wc.getPrintLayoutManager().getPrintLayoutManagerFrame().hidePopUpMenu();
    }

    public String getProjectName() {
        return StringUtils.trim((String)this.projectNameTextField.getText());
    }

    public void setProject(Project project) {
        if (project != null) {
            this.projectNameTextField.setText(project.getName());
            this.projectAuthorTextField.setText(project.getAuthor());
            this.projectKosmoVersionTextField.setText(project.getVersion());
            this.projectDescriptionTextArea.setText(project.getDescription());
            this.projectCreationDateTextField.setText(DateFormatManager.getDateTimeFormat().format(project.getCreationDate()));
            this.projectLastModificationDateTextField.setText(DateFormatManager.getDateTimeFormat().format(project.getLastModificationDate()));
            if (project.getProjectFile() != null) {
                this.projectPathTextField.setText(project.getProjectFile().getAbsolutePath());
            }
            if (project.getAvailablesLocales().isEmpty()) {
                Locale locale = I18N.getLocale();
                project.addLocale(locale);
                project.setActiveLocale(locale);
            }
            this.refreshAvailableLanguageComboBox();
            this.languageModel.setSelectedItem(project.getActiveLocale());
        } else {
            this.projectNameTextField.setText("");
            this.projectAuthorTextField.setText("");
            this.projectDescriptionTextArea.setText("");
            this.projectKosmoVersionTextField.setText("");
            this.projectCreationDateTextField.setText("");
            this.projectLastModificationDateTextField.setText("");
            this.projectPathTextField.setText("");
            this.languageModel.setSelectedItem(I18N.getLocale());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.setProject(JUMPWorkbench.getFrameInstance().getContext().getProject());
        }
        super.setVisible(visible);
    }

    public String getProjectAuthor() {
        return StringUtils.trim((String)this.projectAuthorTextField.getText());
    }

    public String getProjectDescription() {
        return StringUtils.trim((String)this.projectDescriptionTextArea.getText());
    }
}

