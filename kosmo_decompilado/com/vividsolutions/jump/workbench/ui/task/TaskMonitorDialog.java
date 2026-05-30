/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.task;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.ui.AnimatedClockPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class TaskMonitorDialog
extends JDialog
implements TaskMonitor {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(TaskMonitorDialog.class);
    protected String exceptionMessage;
    JPanel mainPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel labelPanel = new JPanel();
    private JButton cancelButton = new JButton();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private ErrorHandler errorHandler;
    private boolean cancelled;
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private JLabel taskProgressLabel = new JLabel();
    private JLabel subtaskProgressLabel = new JLabel();
    private String taskProgress;
    private String subtaskProgress;
    private Timer timer = new Timer(500, new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskMonitorDialog.this.updateLabels();
        }
    });
    private AnimatedClockPanel clockPanel = new AnimatedClockPanel();

    public TaskMonitorDialog(JDialog owner, ErrorHandler errorHandler) {
        super(owner, I18N.getString("workbench.ui.task.TaskMonitorDialog.busy"), true);
        this.errorHandler = errorHandler;
        this.initialize();
    }

    public TaskMonitorDialog(Frame frame, ErrorHandler errorHandler) {
        super(frame, I18N.getString("workbench.ui.task.TaskMonitorDialog.busy"), true);
        this.initialize();
    }

    private void initialize() {
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.setSize(400, 100);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowOpened(WindowEvent e) {
                TaskMonitorDialog.this.cancelButton.setEnabled(true);
            }

            @Override
            public void windowClosing(WindowEvent evt) {
                if (TaskMonitorDialog.this.cancelButton.isVisible()) {
                    TaskMonitorDialog.this.cancelButton.doClick();
                }
            }
        });
        this.setDefaultCloseOperation(0);
    }

    private void jbInit() throws Exception {
        this.mainPanel.setLayout(this.gridBagLayout1);
        this.cancelButton.setText(I18N.getString("workbench.ui.task.TaskMonitorDialog.cancel"));
        this.cancelButton.setVisible(false);
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                TaskMonitorDialog.this.cancelButton_actionPerformed(e);
            }
        });
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                TaskMonitorDialog.this.this_componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                TaskMonitorDialog.this.this_componentHidden(e);
            }
        });
        this.getContentPane().setLayout(this.gridBagLayout2);
        this.labelPanel.setLayout(this.gridBagLayout3);
        this.subtaskProgressLabel.setText("");
        this.taskProgressLabel.setText("");
        this.getContentPane().add((Component)this.mainPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, 10, 1, new Insets(15, 0, 15, 15), 0, 0));
        this.mainPanel.add((Component)this.labelPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 17, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.labelPanel.add((Component)this.taskProgressLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.labelPanel.add((Component)this.subtaskProgressLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.mainPanel.add((Component)this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add((Component)this.clockPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(4, 4, 4, 4), 0, 0));
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        this.cancelButton.setEnabled(false);
        this.cancelled = true;
    }

    void this_componentHidden(ComponentEvent e) {
        this.clockPanel.stop();
        this.timer.stop();
    }

    void this_componentShown(ComponentEvent e) {
        this.cancelled = false;
        this.taskProgress = "";
        this.subtaskProgress = "";
        this.updateLabels();
        this.cancelButton.setVisible(false);
        this.timer.start();
        this.clockPanel.start();
    }

    private void updateLabels() {
        this.taskProgressLabel.setText(this.taskProgress);
        this.subtaskProgressLabel.setText(this.subtaskProgress);
    }

    public void setRefreshRate(int millisecondDelay) {
        this.timer.setDelay(millisecondDelay);
    }

    @Override
    public void report(String description) {
        this.taskProgress = description;
        this.subtaskProgress = "";
    }

    @Override
    public void report(int subtasksDone, int totalSubtasks, String subtaskDescription) {
        this.subtaskProgress = "";
        this.subtaskProgress = String.valueOf(this.subtaskProgress) + subtasksDone;
        if (totalSubtasks != -1) {
            this.subtaskProgress = String.valueOf(this.subtaskProgress) + " / " + totalSubtasks;
        }
        this.subtaskProgress = String.valueOf(this.subtaskProgress) + " " + subtaskDescription;
    }

    @Override
    public void allowCancellationRequests() {
        this.cancelButton.setVisible(true);
    }

    @Override
    public void report(Exception exception) {
        this.errorHandler.handleThrowable(exception);
    }

    @Override
    public boolean isCancelRequested() {
        return this.cancelled;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public JButton getCancelButton() {
        return this.cancelButton;
    }
}

