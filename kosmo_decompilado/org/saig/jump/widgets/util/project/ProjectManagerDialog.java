/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util.project;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.NewTaskPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.project.OpenLastProjectPlugIn;
import org.saig.jump.plugin.utils.project.OpenRecentProjectsPlugIn;
import org.saig.jump.plugin.utils.project.ViewProjectPlugIn;

public class ProjectManagerDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(ProjectManagerDialog.class);
    public static final String TITLE = String.valueOf(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.welcome-to")) + " " + I18N.getString("JUMPWorkbench.app-name");
    private static final int REDUCE_PATH_TO = 50;
    private JPanel newProjectPanel;
    private JPanel openProjectPanel;
    private OKCancelPanel okCancelPanel;
    private ButtonGroup radioButtonGroup = new ButtonGroup();
    private JRadioButton newViewRadioButton;
    private JRadioButton emptyProjectRadioButton;
    private JRadioButton recentProjectRadioButton;
    private JRadioButton anotherProjectRadioButton;
    private PlugInContext context;
    private JFileChooser fileChooser;
    private JList recentProjectsList;
    private LinkedList<String> recentProjectsLinkedList;

    public ProjectManagerDialog(JFrame owner, boolean modal, PlugInContext context) {
        super(owner, TITLE, modal);
        this.context = context;
        this.initialize();
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private void initialize() {
        this.getContentPane().setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getNewProjectPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOpenProjectPanel());
        this.radioButtonGroup.add(this.newViewRadioButton);
        this.radioButtonGroup.add(this.emptyProjectRadioButton);
        this.radioButtonGroup.add(this.recentProjectRadioButton);
        this.radioButtonGroup.add(this.anotherProjectRadioButton);
        ActionListener al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean enabled = ProjectManagerDialog.this.recentProjectRadioButton.isSelected();
                ProjectManagerDialog.this.recentProjectsList.setEnabled(enabled);
                if (enabled) {
                    ProjectManagerDialog.this.recentProjectsList.setBorder(new LineBorder(Color.black));
                } else {
                    ProjectManagerDialog.this.recentProjectsList.setBorder(new LineBorder(Color.gray));
                }
            }
        };
        this.newViewRadioButton.addActionListener(al);
        this.emptyProjectRadioButton.addActionListener(al);
        this.recentProjectRadioButton.addActionListener(al);
        this.anotherProjectRadioButton.addActionListener(al);
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectManagerDialog.this.okCancelPanel_actionPerformed(e);
            }
        });
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                ProjectManagerDialog.this.okCancelPanel.setOKPressed(false);
            }
        });
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                ProjectManagerDialog.this.loadEmptyProject();
            }
        });
        this.getContentPane().add((Component)mainPanel, "Center");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
    }

    protected void okCancelPanel_actionPerformed(ActionEvent e) {
        if (this.okCancelPanel.wasOKPressed()) {
            if (this.newViewRadioButton.isSelected()) {
                this.setVisible(false);
                ProjectManagerDialog.initProjectFrame();
                this.loadNewProjectWithEmptyView();
            } else if (this.emptyProjectRadioButton.isSelected()) {
                this.setVisible(false);
                this.loadEmptyProject();
            } else if (this.recentProjectRadioButton.isSelected()) {
                this.setVisible(false);
                ProjectManagerDialog.initProjectFrame();
                int index = this.recentProjectsList.getSelectedIndex();
                String path = this.recentProjectsLinkedList.get(index);
                PersistentBlackboardPlugIn.get(this.context.getWorkbenchContext()).put("LAST_PROYECT", path);
                if (index == 0) {
                    this.loadLastOpenProject(null);
                } else {
                    File file = new File(path);
                    this.loadLastOpenProject(file.getName());
                }
            } else {
                this.setVisible(false);
                if (!this.loadAnotherProject()) {
                    this.setVisible(true);
                    return;
                }
                ProjectManagerDialog.initProjectFrame();
            }
            this.dispose();
        } else {
            this.setVisible(false);
            this.loadEmptyProject();
            this.dispose();
        }
    }

    private boolean loadAnotherProject() {
        this.fileChooser = new JFileChooser();
        this.fileChooser.setDialogTitle(OpenProjectPlugIn.NAME);
        this.fileChooser.setDialogType(0);
        this.fileChooser.setFileSelectionMode(0);
        this.fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SaveProjectAsPlugIn.SAIG_PROJECT_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SaveProjectAsPlugIn.SAIG_PROJECT_FILE_FILTER);
        if (this.fileChooser.showOpenDialog(this.context.getWorkbenchFrame()) != 0) {
            return false;
        }
        File selectedProjectFile = this.fileChooser.getSelectedFile();
        try {
            PersistentBlackboardPlugIn.get(this.context.getWorkbenchContext()).put("LAST_PROYECT", selectedProjectFile.getAbsolutePath());
            this.loadLastOpenProject(selectedProjectFile.getName());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public void loadLastOpenProject(String projectName) {
        try {
            OpenLastProjectPlugIn open = new OpenLastProjectPlugIn(projectName);
            open.initialize(this.context);
            new TaskMonitorManager().execute(open, this.context);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void initProjectFrame() {
        ProjectManagerDialog.initProjectFrame(true);
    }

    public static void initProjectFrame(boolean visible) {
        ProjectManagerFrame frame = JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame();
        JUMPWorkbench.getFrameInstance().addInternalFrame(frame, visible);
        JUMPWorkbench.getFrameInstance().getDesktopPane().setLayer(frame, 100);
        frame.loadWindowState();
        JUMPWorkbench.getFrameInstance().addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame().saveWindowState();
            }
        });
    }

    private void loadNewProjectWithEmptyView() {
        Project proyecto = new Project();
        proyecto.setName(FirstTaskFramePlugIn.UNTITLED_PROJECT);
        proyecto.setAuthor(System.getProperty("user.name"));
        proyecto.setVersion("3.0 RC1 (20130528)");
        ((JUMPWorkbenchContext)this.context.getWorkbenchContext()).setProject(proyecto);
        NewTaskPlugIn newTaskPlugIn = new NewTaskPlugIn();
        try {
            newTaskPlugIn.execute(this.context);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void loadEmptyProject() {
        Project proyecto = new Project();
        proyecto.setName(FirstTaskFramePlugIn.UNTITLED_PROJECT);
        proyecto.setAuthor(System.getProperty("user.name"));
        proyecto.setVersion("3.0 RC1 (20130528)");
        ((JUMPWorkbenchContext)this.context.getWorkbenchContext()).setProject(proyecto);
        ViewProjectPlugIn viewProyect = new ViewProjectPlugIn();
        ProjectManagerDialog.initProjectFrame();
        try {
            viewProyect.execute(this.context);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private JPanel getNewProjectPanel() {
        if (this.newProjectPanel == null) {
            this.newProjectPanel = new JPanel();
            this.newProjectPanel.setLayout(new GridBagLayout());
            this.newProjectPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.create-a-new-project")));
            this.newViewRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.with-a-new-view"));
            JLabel newViewLabel = new JLabel(IconLoader.icon("newView.png"));
            this.emptyProjectRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.as-an-empty-project"));
            JLabel emptyProjectLabel = new JLabel(IconLoader.icon("document-new.png"));
            FormUtils.addRowInGBL((JComponent)this.newProjectPanel, 0, 0, newViewLabel, (JComponent)this.newViewRadioButton);
            FormUtils.addRowInGBL((JComponent)this.newProjectPanel, 1, 0, emptyProjectLabel, (JComponent)this.emptyProjectRadioButton);
        }
        return this.newProjectPanel;
    }

    private JPanel getOpenProjectPanel() {
        if (this.openProjectPanel == null) {
            this.openProjectPanel = new JPanel();
            this.openProjectPanel.setLayout(new GridBagLayout());
            this.openProjectPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.open-an-existing-project")));
            JLabel lastProjectLabel = new JLabel(IconLoader.icon("document-open.png"));
            this.recentProjectRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.recent-project"));
            JLabel anotherProjectLabel = new JLabel(IconLoader.icon("folder-new.png"));
            this.anotherProjectRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.project.ProjectManagerDialog.another-existing-project"));
            Object[] reducedPaths = null;
            this.recentProjectsLinkedList = null;
            Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(OpenRecentProjectsPlugIn.RECENT_PROJECTS_KEY);
            this.recentProjectsList = new JList();
            if (value == null || !(value instanceof LinkedList) || ((LinkedList)value).isEmpty()) {
                reducedPaths = null;
            } else {
                this.recentProjectsLinkedList = (LinkedList)value;
                int i = 0;
                while (i < this.recentProjectsLinkedList.size()) {
                    File file = new File(this.recentProjectsLinkedList.get(i));
                    if (!file.exists()) {
                        this.recentProjectsLinkedList.remove(i);
                        --i;
                    }
                    ++i;
                }
                reducedPaths = this.recentProjectsLinkedList.toArray();
                i = 0;
                while (i < reducedPaths.length) {
                    reducedPaths[i] = StringUtil.reducePath((String)reducedPaths[i], 50);
                    ++i;
                }
            }
            if (reducedPaths == null || reducedPaths.length < 1) {
                this.newViewRadioButton.setSelected(true);
                this.recentProjectRadioButton.setEnabled(false);
                this.recentProjectsList.setEnabled(false);
            } else {
                this.recentProjectRadioButton.setEnabled(true);
                this.recentProjectRadioButton.setSelected(true);
                this.recentProjectsList = new JList(reducedPaths){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getToolTipText(MouseEvent e) {
                        int row = this.locationToIndex(e.getPoint());
                        return (String)ProjectManagerDialog.this.recentProjectsLinkedList.get(row);
                    }
                };
                this.recentProjectsList.setEnabled(true);
                this.recentProjectsList.setSelectedIndex(0);
                this.recentProjectsList.setSelectionMode(0);
            }
            this.recentProjectsList.setBorder(new LineBorder(Color.black));
            FormUtils.addRowInGBL((JComponent)this.openProjectPanel, 0, 0, lastProjectLabel, (JComponent)this.recentProjectRadioButton);
            FormUtils.addRowInGBL(this.openProjectPanel, 1, 0, this.recentProjectsList);
            FormUtils.addRowInGBL((JComponent)this.openProjectPanel, 2, 0, anotherProjectLabel, (JComponent)this.anotherProjectRadioButton);
        }
        return this.openProjectPanel;
    }
}

