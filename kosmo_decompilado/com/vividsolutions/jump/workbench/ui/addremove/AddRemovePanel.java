/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.saig.jump.lang.I18N;

public class AddRemovePanel<T>
extends JPanel {
    private static final long serialVersionUID = 1L;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanel1 = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton removeButton = new JButton();
    JButton removeAllButton = new JButton();
    JButton addButton = new JButton();
    JButton moveUpButton = new JButton();
    JButton moveDownButton = new JButton();
    JButton addAllButton = new JButton();
    Border border1;
    Border border2;
    private JComponent rightLabel = new JLabel();
    private JComponent leftLabel = new JLabel();
    JScrollPane rightScrollPane = new JScrollPane();
    JScrollPane leftScrollPane = new JScrollPane();
    private AddRemoveList<T> leftList = new DefaultAddRemoveList();
    private AddRemoveList<T> rightList = new DefaultAddRemoveList();
    private InputChangedFirer inputChangedFirer = new InputChangedFirer();

    public AddRemovePanel(boolean showingUpDownButtons) {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!showingUpDownButtons) {
            this.jPanel1.remove(this.moveUpButton);
            this.jPanel1.remove(this.moveDownButton);
        }
        this.setLeftList(this.leftList);
        this.setRightList(this.rightList);
    }

    public AddRemovePanel(boolean showingUpDownButtons, boolean showingAllToRightLeftButtons) {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!showingUpDownButtons) {
            this.jPanel1.remove(this.moveUpButton);
            this.jPanel1.remove(this.moveDownButton);
        }
        if (!showingAllToRightLeftButtons) {
            this.jPanel1.remove(this.addAllButton);
            this.jPanel1.remove(this.removeAllButton);
        }
        this.setLeftList(this.leftList);
        this.setRightList(this.rightList);
    }

    public AddRemovePanel(boolean showingUpButton, boolean showingDownButton, boolean showingAddAllButton, boolean showingRemoveAllButton) {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!showingUpButton) {
            this.jPanel1.remove(this.moveUpButton);
        }
        if (!showingDownButton) {
            this.jPanel1.remove(this.moveDownButton);
        }
        if (!showingAddAllButton) {
            this.jPanel1.remove(this.addAllButton);
        }
        if (!showingRemoveAllButton) {
            this.jPanel1.remove(this.removeAllButton);
        }
        this.setLeftList(this.leftList);
        this.setRightList(this.rightList);
    }

    private void jbInit() throws Exception {
        this.border1 = new EtchedBorder(0, new Color(0, 0, 51), new Color(0, 0, 25));
        this.border2 = new EtchedBorder(0, new Color(0, 0, 51), new Color(0, 0, 25));
        this.setLayout(this.gridBagLayout1);
        this.jPanel1.setLayout(this.gridBagLayout2);
        this.removeButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.remove"));
        this.removeAllButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.remove-all"));
        this.removeButton.setMargin(new Insets(0, 0, 0, 0));
        this.removeAllButton.setMargin(new Insets(0, 0, 0, 0));
        this.removeButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRBack.gif")));
        this.removeAllButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRRewind.gif")));
        this.removeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.removeButton_actionPerformed(e);
            }
        });
        this.removeAllButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.removeAllButton_actionPerformed(e);
            }
        });
        this.addButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.add"));
        this.moveUpButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.move-up"));
        this.moveDownButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.move-down"));
        this.addAllButton.setToolTipText(I18N.getString("workbench.ui.addremove.AddRemovePanel.add-all"));
        this.addButton.setMargin(new Insets(0, 0, 0, 0));
        this.moveUpButton.setMargin(new Insets(0, 0, 0, 0));
        this.moveDownButton.setMargin(new Insets(0, 0, 0, 0));
        this.addAllButton.setMargin(new Insets(0, 0, 0, 0));
        this.addButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRForward.gif")));
        this.moveUpButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRUp.gif")));
        this.moveDownButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRDown.gif")));
        this.addAllButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("VCRFastForward.gif")));
        this.addButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.addButton_actionPerformed(e);
            }
        });
        this.moveUpButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.moveUpButton_actionPerformed(e);
            }
        });
        this.moveDownButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.moveDownButton_actionPerformed(e);
            }
        });
        this.addAllButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddRemovePanel.this.addAllButton_actionPerformed(e);
            }
        });
        this.jPanel1.setMaximumSize(new Dimension(31, Integer.MAX_VALUE));
        this.add((Component)this.rightScrollPane, new GridBagConstraints(34, 12, 1, 1, 1.0, 1.0, 10, 1, new Insets(4, 4, 4, 4), 0, 0));
        this.rightScrollPane.getViewport().add((Component)((JComponent)((Object)this.leftList)), null);
        this.add((Component)this.jPanel1, new GridBagConstraints(23, 10, 1, 5, 0.0, 1.0, 10, 3, new Insets(0, 4, 0, 4), 0, 0));
        this.jPanel1.add((Component)this.removeAllButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 17, 0), 0, 0));
        this.jPanel1.add((Component)this.removeButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 5, 0), 0, 0));
        this.jPanel1.add((Component)this.addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 5, 0), 0, 0));
        this.jPanel1.add((Component)this.moveUpButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 5, 0), 0, 0));
        this.jPanel1.add((Component)this.moveDownButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 11, 0), 0, 0));
        this.jPanel1.add((Component)this.addAllButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 11, 0), 0, 0));
        this.add((Component)this.leftScrollPane, new GridBagConstraints(12, 12, 2, 1, 1.0, 1.0, 10, 1, new Insets(4, 4, 4, 4), 0, 0));
        this.leftScrollPane.getViewport().add((Component)((JComponent)((Object)this.leftList)), null);
        this.rightScrollPane.getViewport().add((Component)((JComponent)((Object)this.rightList)), null);
        this.setRightLabel(this.rightLabel);
        this.setLeftLabel(this.leftLabel);
    }

    public void add(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    public void updateEnabled() {
        this.addButton.setEnabled(!this.leftList.getSelectedItems().isEmpty());
        this.addAllButton.setEnabled(!this.leftList.getModel().getItems().isEmpty());
        this.removeButton.setEnabled(!this.rightList.getSelectedItems().isEmpty());
        this.removeAllButton.setEnabled(!this.rightList.getModel().getItems().isEmpty());
        this.moveUpButton.setEnabled(!this.itemsToMoveUp().isEmpty());
        this.moveDownButton.setEnabled(!this.itemsToMoveDown().isEmpty());
        this.inputChangedFirer.fire();
    }

    void addAllButton_actionPerformed(ActionEvent e) {
        this.leftList.firePreMoveElementsBetweenListsAction(true);
        for (T item : this.leftList.getModel().getItems()) {
            this.rightList.getModel().add(item);
            this.leftList.getModel().remove(item);
        }
        this.leftList.firePostMoveElementsBetweenListsAction();
        this.updateEnabled();
    }

    void removeAllButton_actionPerformed(ActionEvent e) {
        this.rightList.firePreMoveElementsBetweenListsAction(true);
        for (T item : this.rightList.getModel().getItems()) {
            this.rightList.getModel().remove(item);
            this.leftList.getModel().add(item);
        }
        this.rightList.firePostMoveElementsBetweenListsAction();
        this.updateEnabled();
    }

    void addButton_actionPerformed(ActionEvent e) {
        this.leftList.firePreMoveElementsBetweenListsAction();
        this.addSelected();
        this.leftList.firePostMoveElementsBetweenListsAction();
    }

    private void addSelected() {
        for (T selectedItem : this.leftList.getSelectedItems()) {
            this.rightList.getModel().add(selectedItem);
            this.leftList.getModel().remove(selectedItem);
        }
        this.updateEnabled();
    }

    void moveUpButton_actionPerformed(ActionEvent e) {
        this.move(this.itemsToMoveUp(), -1);
    }

    private void move(Collection<T> itemsToMove, int displacement) {
        List<T> selectedItems = this.rightList.getSelectedItems();
        ArrayList<T> items = new ArrayList<T>(this.rightList.getModel().getItems());
        for (T item : itemsToMove) {
            int index = items.indexOf(item);
            items.remove(item);
            items.add(index + displacement, item);
        }
        this.rightList.getModel().setItems(items);
        this.rightList.setSelectedItems(selectedItems);
        this.updateEnabled();
    }

    void moveDownButton_actionPerformed(ActionEvent e) {
        this.move(this.itemsToMoveDown(), 1);
    }

    private Collection<T> itemsToMoveUp() {
        return CollectionUtil.itemsToMoveUp(this.rightList.getModel().getItems(), this.rightList.getSelectedItems());
    }

    private Collection<T> itemsToMoveDown() {
        return CollectionUtil.itemsToMoveDown(this.rightList.getModel().getItems(), this.rightList.getSelectedItems());
    }

    void removeButton_actionPerformed(ActionEvent e) {
        this.rightList.firePreMoveElementsBetweenListsAction();
        this.removeSelected();
        this.rightList.firePostMoveElementsBetweenListsAction();
    }

    private void removeSelected() {
        for (T selectedItem : this.rightList.getSelectedItems()) {
            this.rightList.getModel().remove(selectedItem);
            this.leftList.getModel().add(selectedItem);
        }
        if (this.leftList.getModel().isSorted()) {
            this.leftList.getModel().sort();
        }
        this.updateEnabled();
    }

    public void setLeftText(String newLeftText) {
        if (this.leftLabel instanceof JLabel) {
            ((JLabel)this.leftLabel).setText(newLeftText);
        } else {
            Assert.shouldNeverReachHere();
        }
    }

    public void setRightLabel(JComponent rightLabel) {
        this.remove(rightLabel);
        this.rightLabel = rightLabel;
        this.add((Component)rightLabel, new GridBagConstraints(34, 10, 1, 1, 0.0, 0.0, 17, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.initLabelSizes();
    }

    public void setLeftLabel(JComponent leftLabel) {
        this.remove(leftLabel);
        this.leftLabel = leftLabel;
        this.add((Component)leftLabel, new GridBagConstraints(12, 10, 2, 1, 0.0, 0.0, 17, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.initLabelSizes();
    }

    private void initLabelSizes() {
        Dimension d = new Dimension((int)Math.max(this.leftLabel.getPreferredSize().getWidth(), this.rightLabel.getPreferredSize().getWidth()), (int)Math.max(this.leftLabel.getPreferredSize().getHeight(), this.rightLabel.getPreferredSize().getHeight()));
        this.leftLabel.setPreferredSize(d);
        this.rightLabel.setPreferredSize(d);
    }

    public void setRightList(AddRemoveList<T> rightList) {
        this.rightScrollPane.getViewport().remove((JComponent)((Object)this.rightList));
        this.rightList = rightList;
        this.rightScrollPane.getViewport().add((Component)((JComponent)((Object)rightList)), null);
        this.init(rightList, this.rightScrollPane);
        rightList.add(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    AddRemovePanel.this.getRightList().firePreMoveElementsBetweenListsAction();
                    AddRemovePanel.this.removeSelected();
                    AddRemovePanel.this.getRightList().firePostMoveElementsBetweenListsAction();
                }
            }
        });
    }

    private void init(AddRemoveList<T> list, JScrollPane scrollPane) {
        list.add(new InputChangedListener(){

            @Override
            public void inputChanged() {
                AddRemovePanel.this.updateEnabled();
            }
        });
        scrollPane.setPreferredSize(new Dimension(10, 10));
        this.updateEnabled();
    }

    public void setLeftList(AddRemoveList<T> leftList) {
        this.leftScrollPane.getViewport().remove((JComponent)((Object)this.leftList));
        this.leftList = leftList;
        this.leftScrollPane.getViewport().add((Component)((JComponent)((Object)leftList)), null);
        this.init(leftList, this.leftScrollPane);
        leftList.add(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    AddRemovePanel.this.getLeftList().firePreMoveElementsBetweenListsAction();
                    AddRemovePanel.this.addSelected();
                    AddRemovePanel.this.getLeftList().firePostMoveElementsBetweenListsAction();
                }
            }
        });
    }

    public void setRightText(String newRightText) {
        if (this.rightLabel instanceof JLabel) {
            ((JLabel)this.rightLabel).setText(newRightText);
        } else {
            Assert.shouldNeverReachHere();
        }
    }

    public List<T> getLeftItems() {
        return this.leftList.getModel().getItems();
    }

    public List<T> getRightItems() {
        return this.rightList.getModel().getItems();
    }

    public void sortAll() {
        this.rightList.getModel().sort();
        this.leftList.getModel().sort();
    }

    public AddRemoveList<T> getLeftList() {
        return this.leftList;
    }

    public AddRemoveList<T> getRightList() {
        return this.rightList;
    }

    public JScrollPane getRightScrollPane() {
        return this.rightScrollPane;
    }

    public JScrollPane getLeftScrollPane() {
        return this.leftScrollPane;
    }
}

