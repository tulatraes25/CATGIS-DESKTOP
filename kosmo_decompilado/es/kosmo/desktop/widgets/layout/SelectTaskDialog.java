/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.layout;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.task.TaskManager;
import org.saig.jump.lang.I18N;

public class SelectTaskDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JComboBox taskComboBox;
    private boolean exitOk = false;

    public SelectTaskDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.core.model.layout.widgets.SelectTaskDialog.select-associated-view"));
        this.setContentPane(this.getContentPanel());
        this.setModal(true);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        this.taskComboBox = new JComboBox();
        this.taskComboBox.setAutoscrolls(true);
        TaskManager taskManager = JUMPWorkbench.getFrameInstance().getContext().getTaskManager();
        Iterator<TaskFrame> iter = taskManager.getTasks().iterator();
        while (iter.hasNext()) {
            this.taskComboBox.addItem(iter.next());
        }
        JLabel etiqueta = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.layout.widgets.SelectTaskDialog.associated-view")) + ":");
        FormUtils.addRowInGBL((JComponent)panel, 0, 0, etiqueta, (JComponent)this.taskComboBox);
        FormUtils.addRowInGBL(panel, 1, 0, this.createOKcancelPanel());
        return panel;
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed()) {
                    SelectTaskDialog.this.exitOk = true;
                } else {
                    SelectTaskDialog.this.exitOk = false;
                }
                SelectTaskDialog.this.setVisible(false);
            }
        });
        return okCancelPanel;
    }

    public boolean isOK() {
        return this.exitOk;
    }

    public TaskFrame getSelectedTask() {
        return (TaskFrame)this.taskComboBox.getSelectedItem();
    }
}

