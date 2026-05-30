/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.gui.GUIUtil
 *  org.jdesktop.swingx.JXLabel
 */
package es.kosmo.desktop.gui.dialogs;

import com.iver.cit.gvsig.gui.GUIUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.ui.AnimatedClockPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.components.GradientPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class TaskListProgressMonitor
extends JDialog
implements TaskMonitor {
    private static final long serialVersionUID = 1L;
    protected static final Icon DONE_ICON = IconLoader.icon("apply_co.gif");
    protected static final Icon PENDING_ICON = GUIUtil.resize((ImageIcon)IconLoader.icon("blank.png"), (int)16);
    private AnimatedClockPanel clockPanel;
    protected ErrorHandler errorHandler;
    protected List<String> taskList;
    protected List<JXLabel> taskLabelList;
    protected String exceptionMessage;

    public TaskListProgressMonitor(Frame owner, ErrorHandler errorHandler, List<String> taskNameList) {
        super(owner, I18N.getString("workbench.ui.task.TaskMonitorDialog.busy"), true);
        this.errorHandler = errorHandler;
        this.taskList = taskNameList;
        this.taskLabelList = new ArrayList<JXLabel>(this.taskList.size());
        this.initialize();
    }

    public TaskListProgressMonitor(JDialog owner, ErrorHandler errorHandler, List<String> taskNameList) {
        super(owner, I18N.getString("workbench.ui.task.TaskMonitorDialog.busy"), true);
        this.errorHandler = errorHandler;
        this.taskList = taskNameList;
        this.taskLabelList = new ArrayList<JXLabel>(this.taskList.size());
        this.initialize();
    }

    private void initialize() {
        this.setUndecorated(true);
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        int innerGap = 15;
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(innerGap, innerGap, innerGap, innerGap + 15)));
        mainPanel.setOpaque(false);
        this.setContentPane((Container)((Object)mainPanel));
        this.clockPanel = new AnimatedClockPanel();
        this.clockPanel.setOpaque(false);
        this.clockPanel.setBorder(BorderFactory.createEmptyBorder(innerGap, 0, innerGap, innerGap));
        mainPanel.add(this.clockPanel, "West");
        mainPanel.add(this.getTaskPanel(), "Center");
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                TaskListProgressMonitor.this.this_componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                TaskListProgressMonitor.this.this_componentHidden(e);
            }
        });
        this.pack();
    }

    private JPanel getTaskPanel() {
        JPanel taskPanel = new JPanel(new GridBagLayout());
        taskPanel.setOpaque(false);
        int cont = 0;
        for (String taskName : this.taskList) {
            JXLabel taskLabel = new JXLabel(taskName, PENDING_ICON, 2);
            taskLabel.setFont(taskLabel.getFont().deriveFont(2));
            taskLabel.setForeground(Color.GRAY);
            taskLabel.setOpaque(false);
            this.taskLabelList.add(taskLabel);
            FormUtils.addRowInGBL(taskPanel, cont++, 0, (JComponent)taskLabel);
        }
        return taskPanel;
    }

    @Override
    public void report(String description) {
        int index = this.taskList.indexOf(description);
        if (index != -1) {
            JXLabel label = this.taskLabelList.get(index);
            label.setIcon(DONE_ICON);
            label.setFont(label.getFont().deriveFont(1));
            label.setForeground(Color.BLACK);
            this.pack();
        }
    }

    @Override
    public void report(int itemsDone, int totalItems, String itemDescription) {
    }

    @Override
    public void report(Exception exception) {
        this.errorHandler.handleThrowable(exception);
    }

    @Override
    public void allowCancellationRequests() {
    }

    @Override
    public boolean isCancelRequested() {
        return false;
    }

    void this_componentHidden(ComponentEvent e) {
        this.clockPanel.stop();
        this.taskLabelList.clear();
        this.taskList.clear();
        this.taskLabelList = null;
        this.taskList = null;
    }

    void this_componentShown(ComponentEvent e) {
        this.updateLabels();
        this.clockPanel.start();
    }

    private void updateLabels() {
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}

