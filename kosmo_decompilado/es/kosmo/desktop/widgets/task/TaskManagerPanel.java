/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.task;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.task.TaskManager;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.MyListCellRenderer;

public class TaskManagerPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.Change-name");
    private JScrollPane taskListScrollPane;
    private JList taskList;
    private JPanel actionPanel;
    private JButton newTaskButton;
    private JButton removeTaskButton;
    private JButton viewTaskButton;
    private JButton changeNameButton;
    private JPopupMenu popupMenu;
    private JMenuItem changeNameItem;
    private JMenuItem viewItem;
    private JMenuItem deleteItem;

    public TaskManagerPanel() {
        super(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getTaskListScrollPane());
        FormUtils.addRowInGBL(this, 2, 0, this.getActionPanel());
        this.getPopupMenu();
        this.refreshActions();
    }

    private JScrollPane getTaskListScrollPane() {
        if (this.taskListScrollPane == null) {
            this.taskListScrollPane = new JScrollPane();
            this.taskListScrollPane.setHorizontalScrollBarPolicy(31);
            this.taskListScrollPane.setMinimumSize(new Dimension(200, 120));
            this.taskListScrollPane.setPreferredSize(new Dimension(200, 120));
            this.taskListScrollPane.setViewportView(this.getTaskList());
            this.taskListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.taskListScrollPane;
    }

    private JList getTaskList() {
        this.taskList = new JList();
        this.taskList.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.view-list"));
        List<TaskFrame> taskFrames = JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks();
        this.taskList.setListData(taskFrames.toArray());
        this.taskList.setCellRenderer(new MyListCellRenderer());
        this.taskList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) && !TaskManagerPanel.this.taskList.isSelectionEmpty() && TaskManagerPanel.this.taskList.isSelectedIndex(TaskManagerPanel.this.taskList.locationToIndex(me.getPoint()))) {
                    TaskManagerPanel.this.popupMenu.show(TaskManagerPanel.this.taskList, me.getX(), me.getY());
                }
            }
        });
        this.taskList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    TaskManagerPanel.this.refreshActions();
                }
            }
        });
        return this.taskList;
    }

    private JPanel getActionPanel() {
        this.actionPanel = new JPanel();
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.actionPanel.setLayout(gridLayout1);
        this.newTaskButton = new JButton();
        this.newTaskButton.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.new-view"));
        this.newTaskButton.setIcon(IconLoader.icon("newTask.gif"));
        this.newTaskButton.setText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.new"));
        this.newTaskButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.newTaskButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                TaskManager taskManager = JUMPWorkbench.getFrameInstance().getContext().getTaskManager();
                TaskFrame taskFrame = JUMPWorkbench.getFrameInstance().addTaskFrame();
                Task task = taskFrame.getTask();
                EPSGSelectionDialog csDialog = new EPSGSelectionDialog(JUMPWorkbench.getFrameInstance(), true, true);
                if (csDialog.isOk()) {
                    task.setProjection(csDialog.getProjection());
                } else {
                    task.setProjection(null);
                }
                taskFrame.getLayerViewPanel().setMapLengthUnit(UnitsManager.getLengthUnitFromName(task.getMapLengthUnit()));
                taskFrame.getLayerViewPanel().setUserLengthUnit(UnitsManager.getLengthUnitFromName(task.getUserLengthUnit()));
                taskFrame.getLayerViewPanel().setUserAreaUnit(UnitsManager.getAreaUnitFromName(task.getUserAreaUnit()));
                taskFrame.updateTitle();
                taskManager.addTask(taskFrame);
                TaskManagerPanel.this.taskList.removeAll();
                TaskManagerPanel.this.refresh();
            }
        });
        this.removeTaskButton = new JButton();
        this.removeTaskButton.setText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.delete"));
        this.removeTaskButton.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.delete-view"));
        this.removeTaskButton.setIcon(IconLoader.icon("error_obj.gif"));
        this.removeTaskButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.removeTaskButton.addActionListener(new DeleteActionListener());
        this.viewTaskButton = new JButton();
        this.viewTaskButton.setText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.view"));
        this.viewTaskButton.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.show-view"));
        this.viewTaskButton.setIcon(IconLoader.icon("view.gif"));
        this.viewTaskButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.viewTaskButton.addActionListener(new ViewActionListener());
        this.changeNameButton = new JButton();
        this.changeNameButton.setText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.rename"));
        this.changeNameButton.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.change-selected-view-title"));
        this.changeNameButton.setIcon(IconLoader.icon("changeName.gif"));
        this.changeNameButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.changeNameButton.addActionListener(new ChangeNameActionListener());
        this.actionPanel.add((Component)this.newTaskButton, null);
        this.actionPanel.add((Component)this.changeNameButton, null);
        this.actionPanel.add((Component)this.viewTaskButton, null);
        this.actionPanel.add((Component)this.removeTaskButton, null);
        return this.actionPanel;
    }

    public void refresh() {
        List<TaskFrame> tasks = JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks();
        Collections.sort(tasks);
        this.taskList.setListData(tasks.toArray());
    }

    private JPopupMenu getPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.changeNameItem = new JMenuItem(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.change-name"), IconLoader.icon("changeName.gif"));
        this.changeNameItem.addActionListener(new ChangeNameActionListener());
        this.popupMenu.add(this.changeNameItem);
        this.viewItem = new JMenuItem(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.view"), IconLoader.icon("view.gif"));
        this.viewItem.addActionListener(new ViewActionListener());
        this.popupMenu.add(this.viewItem);
        this.deleteItem = new JMenuItem(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.delete"), IconLoader.icon("error_obj.gif"));
        this.deleteItem.addActionListener(new DeleteActionListener());
        this.popupMenu.add(this.deleteItem);
        this.popupMenu.setSize(new Dimension(100, 100));
        this.popupMenu.pack();
        return this.popupMenu;
    }

    public void hidePopUpMenu() {
        this.popupMenu.setVisible(false);
    }

    private void refreshActions() {
        this.changeNameButton.setEnabled(this.taskList.getSelectedIndices().length == 1);
        this.viewTaskButton.setEnabled(this.taskList.getSelectedIndices().length > 0);
        this.removeTaskButton.setEnabled(this.taskList.getSelectedIndices().length > 0);
        this.changeNameItem.setEnabled(this.changeNameButton.isEnabled());
        this.viewItem.setEnabled(this.viewTaskButton.isEnabled());
        this.deleteItem.setEnabled(this.removeTaskButton.isEnabled());
    }

    public boolean checkRepeated(String selectedName) {
        boolean repeated = false;
        int numberOfTasks = this.taskList.getModel().getSize();
        int i = 0;
        while (i < numberOfTasks && !repeated) {
            TaskFrame selectedTask = (TaskFrame)this.taskList.getModel().getElementAt(i);
            repeated = selectedName.equals(selectedTask.getTask().getTitle(LocaleManager.getActiveLocale()));
            ++i;
        }
        return repeated;
    }

    private class ChangeNameActionListener
    implements ActionListener {
        private ChangeNameActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            TaskManagerPanel.this.popupMenu.setVisible(false);
            TaskFrame taskFrame = (TaskFrame)TaskManagerPanel.this.taskList.getSelectedValue();
            if (taskFrame != null) {
                Object newName = null;
                boolean repeated = false;
                do {
                    if ((newName = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame(), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.insert-the-new-window-name"), NAME, taskFrame.getTask().getTitle(LocaleManager.getActiveLocale()))) == null) continue;
                    String selectedName = (String)newName;
                    if (StringUtils.isEmpty((String)selectedName)) {
                        DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame(), I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.the-name-can-not-be-blank"), I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.error"));
                        continue;
                    }
                    repeated = TaskManagerPanel.this.checkRepeated(selectedName);
                    if (repeated) {
                        DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame(), I18N.getMessage("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.The-selected-task-name-{0}-already-exists", new Object[]{selectedName}), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.Repeated-name"));
                        continue;
                    }
                    taskFrame.getTask().setTitle(selectedName, LocaleManager.getActiveLocale());
                    taskFrame.updateTitle();
                    TaskManagerPanel.this.refresh();
                } while (newName != null && repeated);
            }
        }
    }

    private class DeleteActionListener
    implements ActionListener {
        private DeleteActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            TaskManagerPanel.this.popupMenu.setVisible(false);
            Object[] selectedValues = TaskManagerPanel.this.taskList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                TaskFrame selectedTask = (TaskFrame)selectedValues[i];
                selectedTask.dispose();
                ++i;
            }
            TaskManagerPanel.this.taskList.removeAll();
            TaskManagerPanel.this.refresh();
        }
    }

    private class ViewActionListener
    implements ActionListener {
        private ViewActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            TaskManagerPanel.this.popupMenu.setVisible(false);
            Object[] selectedValues = TaskManagerPanel.this.taskList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                TaskFrame taskFrame = (TaskFrame)selectedValues[i];
                taskFrame.setVisible(true);
                try {
                    taskFrame.setSelected(true);
                }
                catch (PropertyVetoException e1) {
                    e1.printStackTrace();
                }
                ++i;
            }
        }
    }
}

