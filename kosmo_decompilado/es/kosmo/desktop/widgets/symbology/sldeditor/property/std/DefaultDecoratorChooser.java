/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.DecoratorChooser;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorPropertiesDialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.jump.lang.I18N;

public class DefaultDecoratorChooser
extends DecoratorChooser
implements ListSelectionListener,
ActionListener {
    private static final long serialVersionUID = 1L;
    protected JScrollPane listScrollPane;
    protected JList decoratorsList;
    protected DefaultListModel decoratorsListModel;
    protected JPanel buttonsPanel;
    protected JButton addDecoratorButton;
    protected JButton editSelectedDecoratorButton;
    protected JButton removeSelectedDecoratorsButton;
    protected JButton moveUpButton;
    protected JButton moveDownButton;
    protected FeatureSchema schema;

    public DefaultDecoratorChooser(FeatureSchema fs) {
        this(new ArrayList<IDecorator>(), fs);
    }

    public DefaultDecoratorChooser(List<IDecorator> selectedDecorators, FeatureSchema fs) {
        this.setLayout(new BorderLayout());
        JLabel decoratorsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultDecoratorChooser.Decorators")) + ":");
        this.add((Component)decoratorsLabel, "North");
        this.add((Component)this.getDecoratorsPanel(), "Center");
        this.add((Component)this.getButtonsPanel(), "South");
        this.schema = fs;
        this.refresh();
    }

    protected JScrollPane getDecoratorsPanel() {
        if (this.listScrollPane == null) {
            this.decoratorsListModel = new DefaultListModel();
            this.decoratorsList = new JList(this.decoratorsListModel);
            this.decoratorsList.setCellRenderer(new DecoratorListCellRenderer());
            this.listScrollPane = new JScrollPane(this.decoratorsList, 22, 30);
            this.listScrollPane.setPreferredSize(new Dimension(500, 300));
            this.decoratorsList.addListSelectionListener(this);
        }
        return this.listScrollPane;
    }

    protected JPanel getButtonsPanel() {
        if (this.buttonsPanel == null) {
            this.buttonsPanel = new JPanel(new FlowLayout());
            this.addDecoratorButton = new JButton(DesktopIconLoader.icon("add_16.png"));
            this.addDecoratorButton.addActionListener(this);
            this.editSelectedDecoratorButton = new JButton(DesktopIconLoader.icon("edit_16.png"));
            this.editSelectedDecoratorButton.addActionListener(this);
            this.removeSelectedDecoratorsButton = new JButton(DesktopIconLoader.icon("delete_16.png"));
            this.removeSelectedDecoratorsButton.addActionListener(this);
            this.moveUpButton = new JButton(DesktopIconLoader.icon("up_16.png"));
            this.moveUpButton.addActionListener(this);
            this.moveDownButton = new JButton(DesktopIconLoader.icon("down_16.png"));
            this.moveDownButton.addActionListener(this);
            this.buttonsPanel.add(this.addDecoratorButton);
            this.buttonsPanel.add(this.editSelectedDecoratorButton);
            this.buttonsPanel.add(new JSeparator());
            this.buttonsPanel.add(this.moveUpButton);
            this.buttonsPanel.add(this.moveDownButton);
            this.buttonsPanel.add(new JSeparator());
            this.buttonsPanel.add(this.removeSelectedDecoratorsButton);
        }
        return this.buttonsPanel;
    }

    @Override
    public void setDecorators(List<IDecorator> decorators) {
        this.decoratorsListModel.clear();
        for (IDecorator dec : decorators) {
            this.decoratorsListModel.addElement(dec);
        }
    }

    @Override
    public List<IDecorator> getDecorators() {
        ArrayList<IDecorator> configuredDecorators = new ArrayList<IDecorator>();
        int numDecs = this.decoratorsList.getModel().getSize();
        int i = 0;
        while (i < numDecs) {
            configuredDecorators.add((IDecorator)this.decoratorsList.getModel().getElementAt(i));
            ++i;
        }
        return configuredDecorators;
    }

    protected void refresh() {
        int[] selectedDecorators = this.decoratorsList.getSelectedIndices();
        boolean atLeastOneSelected = selectedDecorators.length >= 1;
        boolean exactlyOneSelected = selectedDecorators.length == 1;
        this.addDecoratorButton.setEnabled(true);
        this.editSelectedDecoratorButton.setEnabled(exactlyOneSelected);
        this.removeSelectedDecoratorsButton.setEnabled(atLeastOneSelected);
        this.moveUpButton.setEnabled(exactlyOneSelected && selectedDecorators[0] != 0);
        this.moveDownButton.setEnabled(exactlyOneSelected && selectedDecorators[0] < this.decoratorsListModel.getSize() - 1);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            this.refresh();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.addDecoratorButton) {
            DecoratorPropertiesDialog dialog = new DecoratorPropertiesDialog(JUMPWorkbench.getFrameInstance(), true, null, this.schema);
            GUIUtil.centreOnScreen(dialog);
            dialog.setVisible(true);
            if (dialog.wasOkPressed()) {
                IDecorator decorator = dialog.getDecorator();
                this.decoratorsListModel.addElement(decorator);
            }
        } else if (e.getSource() == this.editSelectedDecoratorButton) {
            IDecorator selectedDecorator = (IDecorator)this.decoratorsList.getSelectedValue();
            DecoratorPropertiesDialog dialog = new DecoratorPropertiesDialog(JUMPWorkbench.getFrameInstance(), true, selectedDecorator, this.schema);
            GUIUtil.centreOnScreen(dialog);
            dialog.setVisible(true);
            if (dialog.wasOkPressed()) {
                int index = this.decoratorsList.getSelectedIndex();
                IDecorator decorator = dialog.getDecorator();
                this.decoratorsListModel.set(index, decorator);
            }
        } else if (e.getSource() == this.removeSelectedDecoratorsButton) {
            int[] selectedIndexes = this.decoratorsList.getSelectedIndices();
            Arrays.sort(selectedIndexes);
            int i = selectedIndexes.length - 1;
            while (i >= 0) {
                this.decoratorsListModel.remove(i);
                --i;
            }
        } else if (e.getSource() == this.moveUpButton) {
            int selectedIndex = this.decoratorsList.getSelectedIndex();
            int previousIndex = selectedIndex - 1;
            Object selectedItem = this.decoratorsListModel.get(selectedIndex);
            this.decoratorsListModel.set(selectedIndex, this.decoratorsListModel.get(previousIndex));
            this.decoratorsListModel.set(previousIndex, selectedItem);
            this.decoratorsList.setSelectedIndex(previousIndex);
        } else if (e.getSource() == this.moveDownButton) {
            int selectedIndex = this.decoratorsList.getSelectedIndex();
            int nextIndex = selectedIndex + 1;
            Object selectedItem = this.decoratorsListModel.get(selectedIndex);
            this.decoratorsListModel.set(selectedIndex, this.decoratorsListModel.get(nextIndex));
            this.decoratorsListModel.set(nextIndex, selectedItem);
            this.decoratorsList.setSelectedIndex(nextIndex);
        }
    }
}

