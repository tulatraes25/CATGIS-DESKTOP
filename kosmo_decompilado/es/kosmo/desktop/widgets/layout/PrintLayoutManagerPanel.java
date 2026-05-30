/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.layout;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.layout.SelectTaskDialog;
import es.kosmo.desktop.widgets.task.TaskManagerPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.MyListCellRenderer;

public class PrintLayoutManagerPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JScrollPane printLayoutListScrollPane;
    private JList printLayoutList;
    private JPanel actionPanel;
    private JButton loadPrintLayoutButton;
    private JButton removePrintButton;
    private JButton viewPrintLayoutButton;
    private JButton changeNameButton;
    private JPopupMenu popupMenu;
    private JMenuItem changeNameItem;
    private JMenuItem viewItem;
    private JMenuItem deleteItem;

    public PrintLayoutManagerPanel() {
        super(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getTaskListScrollPane());
        FormUtils.addRowInGBL(this, 2, 0, this.getActionPanel());
        this.getPopupMenu();
        this.refreshActions();
    }

    private JScrollPane getTaskListScrollPane() {
        if (this.printLayoutListScrollPane == null) {
            this.printLayoutListScrollPane = new JScrollPane();
            this.printLayoutListScrollPane.setHorizontalScrollBarPolicy(31);
            this.printLayoutListScrollPane.setMinimumSize(new Dimension(200, 120));
            this.printLayoutListScrollPane.setPreferredSize(new Dimension(200, 120));
            this.printLayoutListScrollPane.setViewportView(this.getPrintLayoutList());
            this.printLayoutListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.printLayoutListScrollPane;
    }

    private JList getPrintLayoutList() {
        this.printLayoutList = new JList();
        this.printLayoutList.setToolTipText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.loaded-map-list"));
        List<PrintLayoutFrame> printLayouts = JUMPWorkbench.getFrameInstance().getContext().getPrintLayoutManager().getPrintLayouts();
        this.printLayoutList.setListData(printLayouts.toArray());
        this.printLayoutList.setCellRenderer(new MyListCellRenderer());
        this.printLayoutList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) && !PrintLayoutManagerPanel.this.printLayoutList.isSelectionEmpty() && PrintLayoutManagerPanel.this.printLayoutList.isSelectedIndex(PrintLayoutManagerPanel.this.printLayoutList.locationToIndex(me.getPoint()))) {
                    PrintLayoutManagerPanel.this.popupMenu.show(PrintLayoutManagerPanel.this.printLayoutList, me.getX(), me.getY());
                }
            }
        });
        this.printLayoutList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    PrintLayoutManagerPanel.this.refreshActions();
                }
            }
        });
        return this.printLayoutList;
    }

    private void refreshActions() {
        this.changeNameButton.setEnabled(this.printLayoutList.getSelectedIndices().length == 1);
        this.viewPrintLayoutButton.setEnabled(this.printLayoutList.getSelectedIndices().length > 0);
        this.removePrintButton.setEnabled(this.printLayoutList.getSelectedIndices().length > 0);
        this.changeNameItem.setEnabled(this.changeNameButton.isEnabled());
        this.viewItem.setEnabled(this.viewPrintLayoutButton.isEnabled());
        this.deleteItem.setEnabled(this.removePrintButton.isEnabled());
    }

    private JPanel getActionPanel() {
        this.actionPanel = new JPanel();
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.actionPanel.setLayout(gridLayout1);
        this.loadPrintLayoutButton = new JButton();
        this.loadPrintLayoutButton.setText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.new"));
        this.loadPrintLayoutButton.setToolTipText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.new-map"));
        this.loadPrintLayoutButton.setIcon(IconLoader.icon("newTask.gif"));
        this.loadPrintLayoutButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.loadPrintLayoutButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                PrintLayoutManager printLayoutManager = JUMPWorkbench.getFrameInstance().getContext().getPrintLayoutManager();
                List<TaskFrame> tasks = JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks();
                TaskFrame taskFrame = null;
                if (tasks.size() == 0) {
                    return;
                }
                if (tasks.size() == 1) {
                    taskFrame = tasks.get(0);
                } else {
                    SelectTaskDialog dialog = new SelectTaskDialog(JUMPWorkbench.getFrameInstance(), true);
                    if (dialog.isOK()) {
                        taskFrame = dialog.getSelectedTask();
                    } else {
                        return;
                    }
                }
                PrintLayoutFrame frame = new PrintLayoutFrame(taskFrame, JUMPWorkbench.getFrameInstance());
                frame.setVisible(true);
                printLayoutManager.addLayout(frame);
                PrintLayoutManagerPanel.this.printLayoutList.removeAll();
                PrintLayoutManagerPanel.this.refresh();
            }
        });
        this.removePrintButton = new JButton();
        this.removePrintButton.setText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.delete"));
        this.removePrintButton.setToolTipText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.delete-maps"));
        this.removePrintButton.setIcon(IconLoader.icon("error_obj.gif"));
        this.removePrintButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.removePrintButton.addActionListener(new DeleteActionListener());
        this.viewPrintLayoutButton = new JButton();
        this.viewPrintLayoutButton.setText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.view"));
        this.viewPrintLayoutButton.setToolTipText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.view-maps"));
        this.viewPrintLayoutButton.setIcon(IconLoader.icon("view.gif"));
        this.viewPrintLayoutButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.viewPrintLayoutButton.addActionListener(new ViewActionListener());
        this.changeNameButton = new JButton();
        this.changeNameButton.setText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.rename"));
        this.changeNameButton.setToolTipText(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.change-selected-layout-title"));
        this.changeNameButton.setIcon(IconLoader.icon("changeName.gif"));
        this.changeNameButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.changeNameButton.addActionListener(new ChangeNameActionListener());
        this.actionPanel.add((Component)this.loadPrintLayoutButton, null);
        this.actionPanel.add((Component)this.changeNameButton, null);
        this.actionPanel.add((Component)this.viewPrintLayoutButton, null);
        this.actionPanel.add((Component)this.removePrintButton, null);
        return this.actionPanel;
    }

    public void refresh() {
        List<PrintLayoutFrame> layouts = JUMPWorkbench.getFrameInstance().getContext().getPrintLayoutManager().getPrintLayouts();
        Collections.sort(layouts);
        this.printLayoutList.setListData(layouts.toArray());
    }

    private JPopupMenu getPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.changeNameItem = new JMenuItem(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.change-name"), IconLoader.icon("changeName.gif"));
        this.changeNameItem.addActionListener(new ChangeNameActionListener());
        this.popupMenu.add(this.changeNameItem);
        this.viewItem = new JMenuItem(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.view"), IconLoader.icon("view.gif"));
        this.viewItem.addActionListener(new ViewActionListener());
        this.popupMenu.add(this.viewItem);
        this.deleteItem = new JMenuItem(I18N.getString("org.saig.core.model.layout.widgets.PrintLayoutManagerPanel.delete"), IconLoader.icon("error_obj.gif"));
        this.deleteItem.addActionListener(new DeleteActionListener());
        this.popupMenu.add(this.deleteItem);
        this.popupMenu.pack();
        return this.popupMenu;
    }

    public void hidePopUpMenu() {
        this.popupMenu.setVisible(false);
    }

    private class ChangeNameActionListener
    implements ActionListener {
        private ChangeNameActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Object newName;
            PrintLayoutManagerPanel.this.popupMenu.setVisible(false);
            PrintLayoutFrame printLayoutFrame = (PrintLayoutFrame)PrintLayoutManagerPanel.this.printLayoutList.getSelectedValue();
            if (printLayoutFrame != null && (newName = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.insert-the-new-window-name"), TaskManagerPanel.NAME, printLayoutFrame.getTitle())) != null) {
                printLayoutFrame.setTitle((String)newName);
                printLayoutFrame.setName((String)newName);
                PrintLayoutManagerPanel.this.refresh();
            }
        }
    }

    private class DeleteActionListener
    implements ActionListener {
        private DeleteActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            PrintLayoutManagerPanel.this.popupMenu.setVisible(false);
            PrintLayoutManager printLayoutManager = JUMPWorkbench.getFrameInstance().getContext().getPrintLayoutManager();
            Object[] selectedValues = PrintLayoutManagerPanel.this.printLayoutList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                PrintLayoutFrame selectedFrame = (PrintLayoutFrame)selectedValues[i];
                printLayoutManager.remove(selectedFrame);
                selectedFrame.dispose();
                ++i;
            }
            PrintLayoutManagerPanel.this.printLayoutList.removeAll();
            PrintLayoutManagerPanel.this.refresh();
        }
    }

    private class ViewActionListener
    implements ActionListener {
        private ViewActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            PrintLayoutManagerPanel.this.popupMenu.setVisible(false);
            Object[] selectedValues = PrintLayoutManagerPanel.this.printLayoutList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                PrintLayoutFrame printLayoutFrame = (PrintLayoutFrame)selectedValues[i];
                printLayoutFrame.setVisible(true);
                ++i;
            }
        }
    }
}

