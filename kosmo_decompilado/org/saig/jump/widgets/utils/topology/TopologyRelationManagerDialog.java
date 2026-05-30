/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.TopologyRelationsRepository;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn;
import org.saig.jump.widgets.query.LayerQueryWizardDialog;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.MyListCellRenderer;

public class TopologyRelationManagerDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TopologyRelationManagerDialog.class);
    private String layerName;
    private JScrollPane topologyRelationsListScrollPane;
    private JList topologyRelationsList;
    private JPanel actionPanel;
    private JButton addTopologyRelationButton;
    private JButton removeTopologyRelationButton;
    private JButton moveUpTopologyRelationButton;
    private JButton moveDownTopologyRelationButton;
    private JButton changeTopologyRelationButton;
    private int listLength = 0;
    private JButton closeButton;

    public TopologyRelationManagerDialog(JFrame parent, boolean modal, String layerName) {
        super((Frame)parent, modal);
        this.layerName = layerName;
        this.setTitle(I18N.getString(this.getClass(), "configure-topological-relations"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getTopologyRelationsListScrollPane());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getActionPanel());
        this.refreshActions();
        return mainPanel;
    }

    private JScrollPane getTopologyRelationsListScrollPane() {
        if (this.topologyRelationsListScrollPane == null) {
            this.topologyRelationsListScrollPane = new JScrollPane();
            this.topologyRelationsListScrollPane.setHorizontalScrollBarPolicy(31);
            this.topologyRelationsListScrollPane.setMinimumSize(new Dimension(300, 300));
            this.topologyRelationsListScrollPane.setPreferredSize(new Dimension(300, 300));
            this.topologyRelationsListScrollPane.setViewportView(this.getTopologyRelationsList());
            this.topologyRelationsListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.topologyRelationsListScrollPane;
    }

    private JList getTopologyRelationsList() {
        this.topologyRelationsList = new JList();
        this.topologyRelationsList.setToolTipText(I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.view-list"));
        Layer layer = JUMPWorkbench.getLayer(this.layerName);
        List<ITopologyRelation> topologyRelations = layer.getTopologyRelations();
        this.topologyRelationsList.setListData(topologyRelations.toArray());
        this.listLength = topologyRelations.size();
        this.topologyRelationsList.setCellRenderer(new MyListCellRenderer());
        this.topologyRelationsList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) && !TopologyRelationManagerDialog.this.topologyRelationsList.isSelectionEmpty()) {
                    TopologyRelationManagerDialog.this.topologyRelationsList.isSelectedIndex(TopologyRelationManagerDialog.this.topologyRelationsList.locationToIndex(me.getPoint()));
                }
            }
        });
        this.topologyRelationsList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    TopologyRelationManagerDialog.this.refreshActions();
                }
            }
        });
        return this.topologyRelationsList;
    }

    private JPanel getActionPanel() {
        this.actionPanel = new JPanel();
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.actionPanel.setLayout(gridLayout1);
        this.addTopologyRelationButton = new JButton();
        this.addTopologyRelationButton.setToolTipText(I18N.getString(this.getClass(), "add-new-topological-relation-related-to-the-layer"));
        this.addTopologyRelationButton.setIcon(IconLoader.icon("newTask.gif"));
        this.addTopologyRelationButton.setText(I18N.getString(this.getClass(), "new"));
        this.addTopologyRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.addTopologyRelationButton.addActionListener(new AddActionListener());
        this.removeTopologyRelationButton = new JButton();
        this.removeTopologyRelationButton.setText(I18N.getString(this.getClass(), "remove"));
        this.removeTopologyRelationButton.setToolTipText(I18N.getString(this.getClass(), "remove-selected-topological-relation"));
        this.removeTopologyRelationButton.setIcon(IconLoader.icon("error_obj.gif"));
        this.removeTopologyRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.removeTopologyRelationButton.addActionListener(new DeleteActionListener());
        this.moveUpTopologyRelationButton = new JButton();
        this.moveUpTopologyRelationButton.setIcon(IconLoader.icon("SelectionUp.gif"));
        this.moveUpTopologyRelationButton.setToolTipText(I18N.getString(this.getClass(), "move-one-position-upwards"));
        this.moveUpTopologyRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.moveUpTopologyRelationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Layer layer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                List<ITopologyRelation> topologyRelations = layer.getTopologyRelations();
                int selectedIndex = TopologyRelationManagerDialog.this.topologyRelationsList.getSelectedIndex();
                ITopologyRelation selectedRel = topologyRelations.get(selectedIndex);
                ITopologyRelation previusRel = topologyRelations.get(selectedIndex - 1);
                topologyRelations.set(selectedIndex - 1, selectedRel);
                topologyRelations.set(selectedIndex, previusRel);
                TopologyRelationManagerDialog.this.refresh(selectedIndex - 1);
            }
        });
        this.moveDownTopologyRelationButton = new JButton();
        this.moveDownTopologyRelationButton.setIcon(IconLoader.icon("SelectionDown.gif"));
        this.moveDownTopologyRelationButton.setToolTipText(I18N.getString(this.getClass(), "move-one-position-downwards"));
        this.moveDownTopologyRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.moveDownTopologyRelationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Layer layer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                List<ITopologyRelation> topologyRelations = layer.getTopologyRelations();
                int selectedIndex = TopologyRelationManagerDialog.this.topologyRelationsList.getSelectedIndex();
                ITopologyRelation selectedRel = topologyRelations.get(selectedIndex);
                ITopologyRelation nextRel = topologyRelations.get(selectedIndex + 1);
                topologyRelations.set(selectedIndex + 1, selectedRel);
                topologyRelations.set(selectedIndex, nextRel);
                TopologyRelationManagerDialog.this.refresh(selectedIndex + 1);
            }
        });
        this.changeTopologyRelationButton = new JButton();
        this.changeTopologyRelationButton.setText("Modificar");
        this.changeTopologyRelationButton.setToolTipText(I18N.getString(this.getClass(), "modify-selected-topological-relation"));
        this.changeTopologyRelationButton.setIcon(IconLoader.icon("view.gif"));
        this.changeTopologyRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.changeTopologyRelationButton.addActionListener(new ChangeTopologyRelationListener());
        this.closeButton = new JButton();
        this.closeButton.setText(I18N.getString(this.getClass(), "close"));
        this.closeButton.setToolTipText(I18N.getString(this.getClass(), "close-dialog"));
        this.closeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                TopologyRelationManagerDialog.this.dispose();
            }
        });
        this.actionPanel.add((Component)this.addTopologyRelationButton, null);
        this.actionPanel.add((Component)this.changeTopologyRelationButton, null);
        this.actionPanel.add((Component)this.removeTopologyRelationButton, null);
        this.actionPanel.add((Component)this.moveUpTopologyRelationButton, null);
        this.actionPanel.add((Component)this.moveDownTopologyRelationButton, null);
        this.actionPanel.add((Component)this.closeButton, null);
        return this.actionPanel;
    }

    private void refreshActions() {
        this.changeTopologyRelationButton.setEnabled(this.topologyRelationsList.getSelectedIndices().length == 1);
        this.removeTopologyRelationButton.setEnabled(this.topologyRelationsList.getSelectedIndices().length > 0);
        if (this.topologyRelationsList.getSelectedIndices().length == 1) {
            int selectedIndex = this.topologyRelationsList.getSelectedIndex();
            this.moveDownTopologyRelationButton.setEnabled(true);
            this.moveUpTopologyRelationButton.setEnabled(true);
            if (this.listLength <= 1) {
                this.moveDownTopologyRelationButton.setEnabled(false);
                this.moveUpTopologyRelationButton.setEnabled(false);
            } else if (selectedIndex == 0) {
                this.moveUpTopologyRelationButton.setEnabled(false);
            } else if (selectedIndex == this.listLength - 1) {
                this.moveDownTopologyRelationButton.setEnabled(false);
            }
        } else {
            this.moveDownTopologyRelationButton.setEnabled(false);
            this.moveUpTopologyRelationButton.setEnabled(false);
        }
    }

    public void refresh(int selectedIndex) {
        Layer layer = JUMPWorkbench.getLayer(this.layerName);
        List<ITopologyRelation> topologyRelations = layer.getTopologyRelations();
        this.listLength = topologyRelations.size();
        this.topologyRelationsList.setListData(topologyRelations.toArray());
        if (selectedIndex != -1) {
            this.topologyRelationsList.setSelectedIndex(selectedIndex);
        }
    }

    private class AddActionListener
    implements ActionListener {
        private AddActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            TopologyRelationDialog topologyRelationDialog = new TopologyRelationDialog(JUMPWorkbench.getFrameInstance());
            if (topologyRelationDialog.isOK()) {
                ITopologyRelation newRelation = topologyRelationDialog.getTopologyRelation();
                Layer layer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                layer.addTopologyRelation(newRelation);
                TopologyRelationManagerDialog.this.topologyRelationsList.removeAll();
                TopologyRelationManagerDialog.this.refresh(-1);
            }
        }
    }

    private class ChangeTopologyRelationListener
    implements ActionListener {
        private ChangeTopologyRelationListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            ITopologyRelation topologyRelation = (ITopologyRelation)TopologyRelationManagerDialog.this.topologyRelationsList.getSelectedValue();
            int selectedIndex = TopologyRelationManagerDialog.this.topologyRelationsList.getSelectedIndex();
            TopologyRelationDialog topologyRelationDialog = new TopologyRelationDialog(JUMPWorkbench.getFrameInstance(), topologyRelation);
            if (topologyRelationDialog.isOK()) {
                ITopologyRelation newRelation = topologyRelationDialog.getTopologyRelation();
                Layer layer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                List<ITopologyRelation> topologyRelations = layer.getTopologyRelations();
                topologyRelations.set(selectedIndex, newRelation);
                TopologyRelationManagerDialog.this.topologyRelationsList.removeAll();
                TopologyRelationManagerDialog.this.refresh(-1);
            }
        }
    }

    private class DeleteActionListener
    implements ActionListener {
        private DeleteActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Layer layer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
            Object[] selectedValues = TopologyRelationManagerDialog.this.topologyRelationsList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                layer.removeTopologyRelation((ITopologyRelation)selectedValues[i]);
                ++i;
            }
            TopologyRelationManagerDialog.this.topologyRelationsList.removeAll();
            TopologyRelationManagerDialog.this.refresh(-1);
        }
    }

    private class TopologyRelationDialog
    extends JDialog {
        private static final long serialVersionUID = -1184916981800281189L;
        private ITopologyRelation topologyRelation;
        private Filter sourceLayerFilter;
        private Filter topologyFilter;
        private Filter targetLayerFilter;
        private JComboBox topologyComoBox;
        private JComboBox targetLayersComboBox;
        private JButton addConditionToSourceLayerButton;
        private JButton addEntryFilterToSourceLayerButton;
        private JButton addEntryFilterToTargetLayerButton;
        private boolean exitOk;
        private JScrollPane topologyRelationsDecriptionScrollPane;
        private JTextArea topologyRelationDescriptionTextArea;

        public TopologyRelationDialog(JFrame parent) {
            super((Frame)parent, true);
            this.setTitle(I18N.getString(this.getClass(), "add-or-modify-a-topological-relation"));
            this.setContentPane(this.getMainPanel());
            this.pack();
            GUIUtil.centreOnScreen(this);
            this.setVisible(true);
        }

        public TopologyRelationDialog(JFrame parent, ITopologyRelation topologyRelation) {
            super((Frame)parent, true);
            this.setTitle(I18N.getString(this.getClass(), "add-or-modify-a-topological-relation"));
            this.setContentPane(this.getMainPanel());
            this.setTopologyRelation(topologyRelation);
            this.pack();
            GUIUtil.centreOnScreen(this);
            this.setVisible(true);
        }

        public boolean isOK() {
            return this.exitOk;
        }

        public ITopologyRelation getTopologyRelation() {
            this.topologyRelation.setSourceLayerName(TopologyRelationManagerDialog.this.layerName);
            this.topologyRelation.setEntrySourceFilter(this.sourceLayerFilter);
            this.topologyRelation.setAlphanumericFilter(this.topologyFilter);
            if (this.topologyRelation instanceof ITopologyBinaryRelation) {
                ITopologyBinaryRelation binaryRel = (ITopologyBinaryRelation)this.topologyRelation;
                binaryRel.setTargetLayerName(((Layer)this.targetLayersComboBox.getSelectedItem()).getName());
                binaryRel.setEntryTargetFilter(this.targetLayerFilter);
            }
            return this.topologyRelation;
        }

        private JPanel getMainPanel() {
            JPanel mainPanel = new JPanel(new GridBagLayout());
            List<String> topologyRelations = TopologyRelationsRepository.getTopologyRelations();
            Object[] topologyRelationsNames = new String[topologyRelations.size()];
            topologyRelations.toArray(topologyRelationsNames);
            Arrays.sort(topologyRelationsNames);
            if (topologyRelations.size() > 0) {
                try {
                    this.topologyRelation = TopologyRelationsRepository.getTopologyRelation((String)topologyRelationsNames[0]);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
            }
            this.topologyComoBox = new JComboBox<Object>(topologyRelationsNames);
            this.topologyComoBox.addActionListener(new SelectTopologyRelationActionListener());
            FormUtils.addRowInGBL((JComponent)mainPanel, 0, 0, I18N.getString(this.getClass(), "topological-relation"), (JComponent)this.topologyComoBox);
            List<Layer> layers = JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getLayers();
            Vector<Layer> layersToShow = new Vector<Layer>();
            if (layers.size() > 1) {
                for (Layer layer : layers) {
                    if (layer.isRaster() || layer.getName().equals(TopologyRelationManagerDialog.this.layerName)) continue;
                    layersToShow.add(layer);
                }
            }
            this.targetLayersComboBox = new JComboBox(layersToShow);
            this.targetLayersComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    TopologyRelationDialog.this.targetLayerFilter = null;
                }
            });
            FormUtils.addRowInGBL((JComponent)mainPanel, 1, 0, I18N.getString(this.getClass(), "layer-to-cross-with"), (JComponent)this.targetLayersComboBox);
            FormUtils.addRowInGBL(mainPanel, 2, 0, this.createActionPanel());
            FormUtils.addRowInGBL(mainPanel, 3, 0, this.getTopologyRelationsDescriptionScrollPane());
            FormUtils.addRowInGBL(mainPanel, 4, 0, this.createOKcancelPanel());
            return mainPanel;
        }

        private JScrollPane getTopologyRelationsDescriptionScrollPane() {
            if (this.topologyRelationsDecriptionScrollPane == null) {
                this.topologyRelationDescriptionTextArea = new JTextArea(2, 10);
                this.topologyRelationDescriptionTextArea.setFont(new JLabel().getFont());
                this.topologyRelationDescriptionTextArea.setEditable(false);
                this.topologyRelationDescriptionTextArea.setLineWrap(true);
                this.topologyRelationDescriptionTextArea.setWrapStyleWord(true);
                this.topologyRelationDescriptionTextArea.setColumns(40);
                this.topologyRelationDescriptionTextArea.setRows(3);
                this.topologyRelationDescriptionTextArea.setText(this.topologyRelation.getDescription());
                this.topologyRelationsDecriptionScrollPane = new JScrollPane();
                this.topologyRelationsDecriptionScrollPane.setHorizontalScrollBarPolicy(31);
                this.topologyRelationsDecriptionScrollPane.setMinimumSize(new Dimension(200, 100));
                this.topologyRelationsDecriptionScrollPane.setPreferredSize(new Dimension(200, 100));
                this.topologyRelationsDecriptionScrollPane.setViewportView(this.topologyRelationDescriptionTextArea);
                this.topologyRelationsDecriptionScrollPane.setVerticalScrollBarPolicy(22);
            }
            return this.topologyRelationsDecriptionScrollPane;
        }

        private OKCancelPanel createOKcancelPanel() {
            final OKCancelPanel okCancelPanel = new OKCancelPanel();
            GridBagLayout gbPaneOKCancel = new GridBagLayout();
            okCancelPanel.setLayout(gbPaneOKCancel);
            okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (okCancelPanel.wasOKPressed()) {
                        TopologyRelationDialog.this.exitOk = true;
                        ITopologyRelation topologyRelation = TopologyRelationDialog.this.getTopologyRelation();
                        int option = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "do-you-want-to-check-the-whole-layer"), I18N.getString(this.getClass(), "check-layer"));
                        if (option == 0 && !topologyRelation.checkAll()) {
                            option = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "topological-errors-were-detected-do-you-want-to-create-an-errors-layer"), I18N.getString(this.getClass(), "topological-errors"));
                            if (option == 0) {
                                Layer selectedLayer = JUMPWorkbench.getLayer(topologyRelation.getSourceLayerName());
                                try {
                                    CheckTopologyRelationsPlugIn.loadIncidentCategory(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), topologyRelation.obtainErrors(), topologyRelation.getSourceLayerName(), selectedLayer.getGeometryType());
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)"", (Throwable)e);
                                    JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(this.getClass(), "an-error-occurred-while-creating-the-errors-layer"));
                                }
                            }
                            if ((option = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "do-you-want-to-include-the-topological-rule"), I18N.getString(this.getClass(), "topological-errors"))) == 1) {
                                TopologyRelationDialog.this.exitOk = false;
                            }
                        }
                    } else {
                        TopologyRelationDialog.this.exitOk = false;
                    }
                    TopologyRelationDialog.this.setVisible(false);
                }
            });
            return okCancelPanel;
        }

        private JPanel createActionPanel() {
            JPanel actionPanel = new JPanel();
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setVgap(5);
            gridLayout1.setHgap(5);
            actionPanel.setLayout(gridLayout1);
            this.addEntryFilterToSourceLayerButton = new JButton();
            this.addEntryFilterToSourceLayerButton.setToolTipText(I18N.getString(this.getClass(), "restrict-input-layer-elements"));
            this.addEntryFilterToSourceLayerButton.setText(I18N.getString(this.getClass(), "input-layer-filter"));
            this.addEntryFilterToSourceLayerButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
            this.addEntryFilterToSourceLayerButton.addActionListener(new ConfigureSourceLayerTopologyRelationFilterActionListener());
            this.addConditionToSourceLayerButton = new JButton();
            this.addConditionToSourceLayerButton.setToolTipText(I18N.getString(this.getClass(), "add-filter-to-topological-rule"));
            this.addConditionToSourceLayerButton.setText(I18N.getString(this.getClass(), "filter-to-topological-rule"));
            this.addConditionToSourceLayerButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
            this.addConditionToSourceLayerButton.addActionListener(new ConfigureTopologyFilterActionListener());
            this.addEntryFilterToTargetLayerButton = new JButton();
            this.addEntryFilterToTargetLayerButton.setToolTipText(I18N.getString(this.getClass(), "restrict-crossing-layer-element"));
            this.addEntryFilterToTargetLayerButton.setText(I18N.getString(this.getClass(), "crossing-layer-filter"));
            this.addEntryFilterToTargetLayerButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
            this.addEntryFilterToTargetLayerButton.addActionListener(new ConfigureTargetLayerTopologyRelationFilterActionListener());
            actionPanel.add((Component)this.addEntryFilterToSourceLayerButton, null);
            actionPanel.add((Component)this.addConditionToSourceLayerButton, null);
            actionPanel.add((Component)this.addEntryFilterToTargetLayerButton, null);
            return actionPanel;
        }

        public void setTopologyRelation(ITopologyRelation topologyRelation) {
            this.topologyRelation = topologyRelation;
            this.sourceLayerFilter = topologyRelation.getEntrySourceFilter();
            this.topologyFilter = topologyRelation.getAlphanumericFilter();
            this.topologyComoBox.setSelectedItem(topologyRelation.getName());
            if (topologyRelation instanceof ITopologyBinaryRelation) {
                this.targetLayersComboBox.setSelectedItem(JUMPWorkbench.getLayer(((ITopologyBinaryRelation)topologyRelation).getTargetLayerName()));
                this.targetLayerFilter = ((ITopologyBinaryRelation)topologyRelation).getEntryTargetFilter();
            }
        }

        private class ConfigureSourceLayerTopologyRelationFilterActionListener
        implements ActionListener {
            private ConfigureSourceLayerTopologyRelationFilterActionListener() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Layer sourceLayer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                LayerQueryWizardDialog queryWizardDialog = new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), sourceLayer);
                queryWizardDialog.setVisible(true);
                if (queryWizardDialog.exitOk()) {
                    TopologyRelationDialog.this.sourceLayerFilter = queryWizardDialog.getFilter();
                }
            }
        }

        private class ConfigureTargetLayerTopologyRelationFilterActionListener
        implements ActionListener {
            private ConfigureTargetLayerTopologyRelationFilterActionListener() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Layer targetLayer = (Layer)TopologyRelationDialog.this.targetLayersComboBox.getSelectedItem();
                LayerQueryWizardDialog queryWizardDialog = new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), targetLayer);
                queryWizardDialog.setVisible(true);
                if (queryWizardDialog.exitOk()) {
                    TopologyRelationDialog.this.targetLayerFilter = queryWizardDialog.getFilter();
                }
            }
        }

        private class ConfigureTopologyFilterActionListener
        implements ActionListener {
            private ConfigureTopologyFilterActionListener() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Layer sourceLayer = JUMPWorkbench.getLayer(TopologyRelationManagerDialog.this.layerName);
                LayerQueryWizardDialog queryWizardDialog = new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), sourceLayer);
                queryWizardDialog.setVisible(true);
                if (queryWizardDialog.exitOk()) {
                    TopologyRelationDialog.this.topologyFilter = queryWizardDialog.getFilter();
                }
            }
        }

        private class SelectTopologyRelationActionListener
        implements ActionListener {
            private SelectTopologyRelationActionListener() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String relationName = (String)TopologyRelationDialog.this.topologyComoBox.getSelectedItem();
                try {
                    TopologyRelationDialog.this.topologyRelation = TopologyRelationsRepository.getTopologyRelation(relationName);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
                TopologyRelationDialog.this.topologyRelationDescriptionTextArea.setText(TopologyRelationDialog.this.topologyRelation.getDescription());
                if (TopologyRelationDialog.this.topologyRelation instanceof ITopologyBinaryRelation) {
                    TopologyRelationDialog.this.targetLayersComboBox.setEnabled(true);
                    TopologyRelationDialog.this.addEntryFilterToTargetLayerButton.setEnabled(true);
                } else {
                    TopologyRelationDialog.this.targetLayersComboBox.setEnabled(false);
                    TopologyRelationDialog.this.addEntryFilterToTargetLayerButton.setEnabled(false);
                }
            }
        }
    }
}

