/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.warp.BilinearInterpolatedTransform;
import com.vividsolutions.jump.warp.CoordinateTransform;
import com.vividsolutions.jump.warp.DummyTransform;
import com.vividsolutions.jump.warp.TaggedCoordinate;
import com.vividsolutions.jump.warp.Triangle;
import com.vividsolutions.jump.warp.Triangulator;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.CopySelectedLayersToWarpingVectorsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.generate.ShowTriangulationPlugIn;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.warp.DeleteIncrementalWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DeleteWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DrawIncrementalWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DrawWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.IncrementalWarpingVectorLayerFinder;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class WarpingPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WarpingPanel.class);
    public static final String MODIFIED_OUTSIDE_WARP_KEY = String.valueOf(WarpingPanel.class.getName()) + " - MODIFIED_OUTSIDE_WARP";
    public static final String RECONSTRUCTION_VECTORS_KEY = String.valueOf(WarpingPanel.class.getName()) + " - RECONSTRUCTION VECTORS";
    private DummyTaskMonitor dummyMonitor = new DummyTaskMonitor();
    private Triangulator triangulator = new Triangulator();
    private boolean warping = false;
    private boolean initializingSourceLayerComboBox = false;
    private DefaultComboBoxModel sourceLayerComboBoxModel = new DefaultComboBoxModel();
    private ToolboxDialog toolbox;
    private static final String LAST_SOURCE_LAYER_KEY = String.valueOf(WarpingPanel.class.getName()) + " - LAST SOURCE LAYER";
    private JCheckBox autoHideCheckBox = new JCheckBox();
    private JPanel buttonPanel = new JPanel();
    private JButton clearOutputButton = new JButton();
    private JButton copyLayerButton = new JButton();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel layerLabel = new JLabel();
    private JComboBox sourceLayerComboBox = new JComboBox();
    private JCheckBox triangulationCheckBox = new JCheckBox();
    private JButton warpButton = new JButton();
    private JCheckBox warpIncrementallyCheckBox = new JCheckBox();
    private LayerNamePanelListener layerNamePanelListener = new LayerNamePanelListener(){

        @Override
        public void layerSelectionChanged() {
            WarpingPanel.this.updateComponents();
        }
    };
    private LayerNamePanel lastLayerNamePanel = null;

    public WarpingPanel(ToolboxDialog toolbox) {
        this.toolbox = toolbox;
        toolbox.addWindowListener(new WindowAdapter(){

            @Override
            public void windowActivated(WindowEvent e) {
                WarpingPanel.this.updateComponents();
            }
        });
        GUIUtil.addInternalFrameListener(toolbox.getContext().getWorkbench().getFrame().getDesktopPane(), GUIUtil.toInternalFrameListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.updateComponents();
            }
        }));
        this.sourceLayerComboBox.setModel(this.sourceLayerComboBoxModel);
        this.sourceLayerComboBox.setRenderer(new LayerNameRenderer());
        this.warpButton.setIcon(IconLoader.icon("GoalFlag.gif"));
        this.layerLabel.setText(I18N.getString("workbench.ui.warp.WarpingPanel.source-layer"));
        this.setLayout(this.gridBagLayout1);
        this.warpIncrementallyCheckBox.setToolTipText(I18N.getString("workbench.ui.warp.WarpingPanel.warps-relative-to-the-output-layer-as-soon-as-a-vector-is-drawn"));
        this.warpIncrementallyCheckBox.setSelected(false);
        this.warpIncrementallyCheckBox.setText(I18N.getString("workbench.ui.warp.WarpingPanel.warp-incrementally"));
        this.warpIncrementallyCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.warpIncrementallyCheckBox_actionPerformed(e);
            }
        });
        this.sourceLayerComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.sourceComboBox_actionPerformed(e);
            }
        });
        this.buttonPanel.setLayout(this.gridLayout1);
        this.gridLayout1.setColumns(1);
        this.gridLayout1.setRows(2);
        this.warpButton.setText(I18N.getString("workbench.ui.warp.WarpingPanel.warp"));
        this.warpButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.warpButton_actionPerformed(e);
            }
        });
        this.clearOutputButton.setText(I18N.getString("workbench.ui.warp.WarpingPanel.clear-all-vectors"));
        this.clearOutputButton.setToolTipText(I18N.getString("workbench.ui.warp.WarpingPanel.deletes-the-warp-output-layer-and-the-vectors"));
        this.clearOutputButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WarpingPanel.this.clearOutputButton_actionPerformed(e);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        this.autoHideCheckBox.setToolTipText(I18N.getString("workbench.ui.warp.WarpingPanel.auto-hides-the-source-layer-and-the-warping-vectors"));
        this.autoHideCheckBox.setSelected(true);
        this.autoHideCheckBox.setText(I18N.getString("workbench.ui.warp.WarpingPanel.auto-hide-layers"));
        this.triangulationCheckBox.setToolTipText(I18N.getString("workbench.ui.warp.WarpingPanel.shows-the-initial-and-final-triangulation-layers"));
        this.triangulationCheckBox.setText(I18N.getString("workbench.ui.warp.WarpingPanel.display-triangulation"));
        this.triangulationCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.triangulationCheckBox_actionPerformed(e);
            }
        });
        this.copyLayerButton.setToolTipText(I18N.getString("workbench.ui.warp.WarpingPanel.copies-the-features-in-the-selected-layer-not-the-source-layer-above-to-the-warping-vectors-layer"));
        this.copyLayerButton.setText(I18N.getString("workbench.ui.warp.WarpingPanel.copy-layer-to-vectors"));
        this.copyLayerButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WarpingPanel.this.copyLayerButton_actionPerformed(e);
            }
        });
        this.add((Component)this.layerLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.sourceLayerComboBox, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.warpIncrementallyCheckBox, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 4, 0, 4), 0, 0));
        this.add((Component)this.buttonPanel, new GridBagConstraints(0, 8, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.autoHideCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 4, 0, 0), 0, 0));
        this.add((Component)this.triangulationCheckBox, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 4, 0, 4), 0, 0));
        this.add((Component)this.warpButton, new GridBagConstraints(0, 10, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 4, 4, 4), 0, 0));
        this.add((Component)this.clearOutputButton, new GridBagConstraints(0, 11, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 4, 0, 4), 0, 0));
        this.add((Component)this.copyLayerButton, new GridBagConstraints(0, 12, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 4, 4, 4), 0, 0));
    }

    private void addModificationListener(final Layer outputLayer) {
        outputLayer.getLayerManager().addLayerListener(new LayerListener(){

            @Override
            public void categoryChanged(CategoryEvent e) {
            }

            @Override
            public void layerChanged(LayerEvent e) {
            }

            @Override
            public void featuresChanged(FeatureEvent e) {
                if (e.getLayer() != outputLayer) {
                    return;
                }
                if (WarpingPanel.this.warping) {
                    return;
                }
                outputLayer.getBlackboard().put(MODIFIED_OUTSIDE_WARP_KEY, true);
                outputLayer.getBlackboard().put(RECONSTRUCTION_VECTORS_KEY, new ArrayList());
            }
        });
    }

    public UndoableCommand addWarping(final UndoableCommand wrappeeCommand) {
        return new UndoableCommand(wrappeeCommand.getName()){
            private Boolean warping;
            UndoableCommand warpCommand;
            {
                super($anonymous0);
                this.warping = null;
                this.warpCommand = null;
            }

            private boolean warping() throws Exception {
                if (this.warping == null) {
                    this.warping = new Boolean(WarpingPanel.this.isWarpingIncrementally() && WarpingPanel.this.warpConditionsMet());
                    if (this.warping.booleanValue()) {
                        this.warpCommand = WarpingPanel.this.createWarpCommand();
                    }
                }
                return this.warping;
            }

            @Override
            public void execute() throws Exception {
                wrappeeCommand.execute();
                if (this.warping()) {
                    this.warpCommand.execute();
                }
            }

            @Override
            public void unexecute() throws Exception {
                if (this.warping()) {
                    this.warpCommand.unexecute();
                }
                wrappeeCommand.unexecute();
            }
        };
    }

    void clearOutputButton_actionPerformed(ActionEvent e) throws Exception {
        this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
            final Layer sourceLayer = this.currentSourceLayer();
            final Layer outputLayer = this.currentOutputLayer();
            final boolean outputLayerExistedOriginally = this.currentOutputLayer() != null;
            final ArrayList reconstructionVectors = new ArrayList();
            if (outputLayerExistedOriginally) {
                if (outputLayer.getBlackboard().getBoolean(MODIFIED_OUTSIDE_WARP_KEY)) {
                    this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
                } else {
                    reconstructionVectors.addAll((Collection)outputLayer.getBlackboard().get(RECONSTRUCTION_VECTORS_KEY));
                }
            }
            final boolean willShowSourceLayer = this.isAutoHidingLayers() && sourceLayer != null && !sourceLayer.isVisible();
            UndoableCommand command = Layer.addUndo(this.warpingVectorLayerFinder().getLayerName(), this.toolbox.getContext(), Layer.addUndo(this.incrementalWarpingVectorLayerFinder().getLayerName(), this.toolbox.getContext(), ShowTriangulationPlugIn.addUndo(new UndoableCommand(this.clearOutputButton.getText()){

                @Override
                public void execute() throws Exception {
                    if (WarpingPanel.this.warpingVectorLayerFinder().getLayer() != null) {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.warpingVectorLayerFinder().getLayer());
                    }
                    if (WarpingPanel.this.incrementalWarpingVectorLayerFinder().getLayer() != null) {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.incrementalWarpingVectorLayerFinder().getLayer());
                    }
                    if (outputLayerExistedOriginally) {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(outputLayer.getName()));
                    }
                    if (willShowSourceLayer) {
                        sourceLayer.setVisible(true);
                    }
                    if (WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME) != null) {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME));
                    }
                    if (WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME) != null) {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME));
                    }
                }

                @Override
                public void unexecute() throws Exception {
                    try {
                        if (willShowSourceLayer) {
                            sourceLayer.setVisible(false);
                        }
                        if (outputLayerExistedOriginally) {
                            WarpingPanel.this.warp(sourceLayer, reconstructionVectors, false);
                        }
                    }
                    catch (Throwable t) {
                        WarpingPanel.this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                        WarpingPanel.this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
                    }
                }
            }, this.toolbox.getContext())));
            command.execute();
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
        }
        finally {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
    }

    private void clearWarpingFlag() {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                WarpingPanel.this.warping = false;
            }
        });
    }

    private Collection<Feature> clone(Collection<Feature> features) {
        ArrayList<Feature> clone = new ArrayList<Feature>(features.size());
        for (Feature feature : features) {
            clone.add((Feature)feature.clone());
        }
        return clone;
    }

    private Collection<Feature> collapseToTip(Collection<Feature> vectors) {
        ArrayList<Feature> collapsedVectors = new ArrayList<Feature>();
        for (Feature vector : vectors) {
            Feature collapsedVector = (Feature)vector.clone();
            this.tail(collapsedVector).setCoordinate(this.tip(collapsedVector));
            collapsedVector.getGeometry().geometryChanged();
            collapsedVectors.add(collapsedVector);
        }
        return collapsedVectors;
    }

    void copyLayerButton_actionPerformed(ActionEvent e) {
        this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            try {
                new CopySelectedLayersToWarpingVectorsPlugIn().execute(this.toolbox.getContext().createPlugInContext());
            }
            catch (Throwable t) {
                this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
            }
        }
        finally {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
    }

    public UndoableCommand generateWarpingVectorsCommand() throws Exception {
        ArrayList<Feature> reconstructionVectors = this.currentOutputLayer() == null || this.currentOutputLayer().getBlackboard().getBoolean(MODIFIED_OUTSIDE_WARP_KEY) ? new ArrayList<Feature>() : (Collection)this.currentOutputLayer().getBlackboard().get(RECONSTRUCTION_VECTORS_KEY);
        final Collection<Feature> newWarpingVectors = this.toWarpingVectors(this.incrementalWarpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().getFeatures(), reconstructionVectors, this.currentSourceLayer());
        return Layer.addUndo(this.warpingVectorLayerFinder().getLayerName(), this.toolbox.getContext(), new UndoableCommand(I18N.getString("workbench.ui.warp.WarpingPanel.generate-warping-vectors-from-incremental-warping-vectors")){

            @Override
            public void execute() throws Exception {
                try {
                    if (WarpingPanel.this.warpingVectorLayerFinder().getLayer() == null) {
                        WarpingPanel.this.warpingVectorLayerFinder().createLayer();
                    } else {
                        WarpingPanel.this.warpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().clear();
                    }
                    WarpingPanel.this.warpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().addAll(newWarpingVectors);
                }
                catch (Throwable t) {
                    WarpingPanel.this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                    WarpingPanel.this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
                }
            }

            @Override
            public void unexecute() throws Exception {
            }
        });
    }

    private void hideTriangulation() {
        if (!(this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerViewPanelProxy)) {
            return;
        }
        this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            try {
                UndoableCommand command = ShowTriangulationPlugIn.addUndo(new UndoableCommand(I18N.getString("workbench.ui.warp.WarpingPanel.hide-triangulation")){

                    @Override
                    public void execute() {
                        if (WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME) != null) {
                            WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME));
                        }
                        if (WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME) != null) {
                            WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME));
                        }
                    }

                    @Override
                    public void unexecute() {
                    }
                }, this.toolbox.getContext());
                command.execute();
                this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
            }
            catch (Throwable t) {
                this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
            }
        }
        finally {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
    }

    public boolean isAutoHidingLayers() {
        return this.autoHideCheckBox.isSelected();
    }

    private boolean layerViewPanelProxyActive() {
        return this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerViewPanelProxy;
    }

    private Layer outputLayer(String sourceLayerName) {
        Layer outputLayer = this.toolbox.getContext().getLayerManager().getLayer(this.outputLayerName(sourceLayerName));
        if (outputLayer == null) {
            return null;
        }
        if (outputLayer.getBlackboard().get(MODIFIED_OUTSIDE_WARP_KEY) == null) {
            outputLayer.getBlackboard().put(MODIFIED_OUTSIDE_WARP_KEY, true);
            outputLayer.getBlackboard().put(RECONSTRUCTION_VECTORS_KEY, new ArrayList());
            this.addModificationListener(outputLayer);
        }
        return outputLayer;
    }

    private String outputLayerName(String sourceLayerName) {
        return I18N.getMessage("workbench.ui.warp.WarpingPanel.warped-{0}", new Object[]{sourceLayerName});
    }

    private void setWarpingFlag() {
        this.warping = true;
    }

    private void showTriangulation() {
        ShowTriangulationPlugIn showTriangulationPlugIn = new ShowTriangulationPlugIn(this);
        if (showTriangulationPlugIn.createEnableCheck(this.toolbox.getContext()).check(null) != null) {
            return;
        }
        this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            try {
                showTriangulationPlugIn.execute(this.toolbox.getContext().createPlugInContext());
            }
            catch (Throwable t) {
                this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
            }
        }
        finally {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
    }

    private Coordinate tail(Feature vector) {
        return ((LineString)vector.getGeometry()).getCoordinateN(0);
    }

    private Coordinate tip(Feature vector) {
        return ((LineString)vector.getGeometry()).getCoordinateN(1);
    }

    private Collection<Feature> toWarpingVectors(Collection<Feature> incrementalWarpingVectors, Collection<Feature> reconstructionVectors, Layer sourceLayer) {
        Envelope envelope = null;
        try {
            envelope = sourceLayer.getFeatureCollectionWrapper().getEnvelope();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            envelope = new Envelope();
        }
        ArrayList<Feature> warpingVectors = new ArrayList<Feature>();
        CoordinateTransform transform = reconstructionVectors.isEmpty() || sourceLayer == null ? new DummyTransform() : new BilinearInterpolatedTransform(CollectionUtil.inverse(this.triangleMap(envelope, reconstructionVectors, new ArrayList<TaggedCoordinate>(), Triangulator.taggedVectorVertices(false, FeatureUtil.toLineStrings(incrementalWarpingVectors)))), new DummyTaskMonitor());
        List<TaggedCoordinate> reconstructionVectorTips = Triangulator.taggedVectorVertices(true, FeatureUtil.toLineStrings(reconstructionVectors));
        warpingVectors.addAll(reconstructionVectors);
        for (Feature incrementalWarpingVector : incrementalWarpingVectors) {
            Coordinate tip;
            Feature warpingVector = (Feature)incrementalWarpingVector.clone();
            Coordinate tail = ((LineString)warpingVector.getGeometry()).getCoordinateN(0);
            if (tail.equals((Object)(tip = ((LineString)warpingVector.getGeometry()).getCoordinateN(1))) && reconstructionVectorTips.contains(tip)) continue;
            tail.setCoordinate(transform.transform(tail));
            warpingVector.getGeometry().geometryChanged();
            warpingVectors.add(warpingVector);
        }
        return warpingVectors;
    }

    public Map<Triangle, Triangle> triangleMap(Envelope sourceLayerEnvelope, Collection<Feature> vectorFeatures, Collection<TaggedCoordinate> sourceHints, Collection<TaggedCoordinate> destinationHints) {
        List<Geometry> vectorLineStrings = FeatureUtil.toGeometries(CopySelectedLayersToWarpingVectorsPlugIn.removeNonVectorFeaturesAndWarn(vectorFeatures, this.toolbox.getContext().getWorkbench().getFrame()));
        Map<Triangle, Triangle> triangleMap = this.triangulator.triangleMap(sourceLayerEnvelope, vectorLineStrings, sourceHints, destinationHints, this.dummyMonitor);
        Assert.isTrue((boolean)this.triangulator.getIgnoredVectors().isEmpty(), (String)(!this.triangulator.getIgnoredVectors().isEmpty() ? this.triangulator.getIgnoredVectors().iterator().next().toString() : ""));
        return triangleMap;
    }

    void triangulationCheckBox_actionPerformed(ActionEvent e) {
        if (this.triangulationCheckBox.isSelected()) {
            this.showTriangulation();
        } else {
            this.hideTriangulation();
        }
    }

    private void warp() throws Exception {
        this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
            UndoableCommand command = this.createWarpCommand();
            command.execute();
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
        }
        finally {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
    }

    void warpButton_actionPerformed(ActionEvent e) {
        try {
            if (this.warpConditionsMet()) {
                this.warp();
            }
        }
        catch (Throwable t) {
            this.toolbox.getContext().getErrorHandler().handleThrowable(t);
        }
    }

    public boolean warpConditionsMet() {
        return this.layerViewPanelProxyActive() && this.sourceLayerComboBox.getSelectedIndex() > -1;
    }

    private Layer currentOutputLayer() {
        if (this.currentSourceLayer() == null) {
            return null;
        }
        return this.outputLayer(this.currentSourceLayer().getName());
    }

    public Layer currentSourceLayer() {
        return (Layer)this.sourceLayerComboBox.getSelectedItem();
    }

    public UndoableCommand createWarpCommand() throws Exception {
        ArrayList newVectors;
        Assert.isTrue((this.currentSourceLayer() != null ? 1 : 0) != 0);
        Layer outputLayer = this.currentOutputLayer();
        final boolean outputLayerExistedOriginally = outputLayer != null;
        final ArrayList oldVectors = outputLayer != null ? new ArrayList((Collection)outputLayer.getBlackboard().get(RECONSTRUCTION_VECTORS_KEY)) : new ArrayList();
        ArrayList<Object> arrayList = newVectors = this.warpingVectorLayerFinder().getLayer() == null ? new ArrayList() : new ArrayList<Feature>(this.warpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().getFeatures());
        if (outputLayerExistedOriginally && outputLayer.getBlackboard().getBoolean(MODIFIED_OUTSIDE_WARP_KEY)) {
            this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
        }
        final Layer sourceLayer = this.currentSourceLayer();
        final boolean willHideWarpingVectorLayer = this.isAutoHidingLayers() && this.warpingVectorLayerFinder().getLayer() != null && this.warpingVectorLayerFinder().getLayer().isVisible() && this.isWarpingIncrementally();
        final boolean willHideSourceLayer = this.isAutoHidingLayers() && sourceLayer != null && sourceLayer.isVisible();
        final boolean warpingIncrementally = this.isWarpingIncrementally();
        return Layer.addUndo(this.incrementalWarpingVectorLayerFinder().getLayerName(), this.toolbox.getContext(), new ShowTriangulationPlugIn(this).addLayerGeneration(new UndoableCommand(this.warpButton.getText()){

            @Override
            public void execute() throws Exception {
                try {
                    WarpingPanel.this.warp(sourceLayer, newVectors, warpingIncrementally);
                    if (willHideWarpingVectorLayer) {
                        WarpingPanel.this.warpingVectorLayerFinder().getLayer().setVisible(false);
                    }
                    if (willHideSourceLayer) {
                        sourceLayer.setVisible(false);
                    }
                }
                catch (Throwable t) {
                    WarpingPanel.this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                    WarpingPanel.this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
                }
            }

            @Override
            public void unexecute() throws Exception {
                try {
                    if (willHideSourceLayer) {
                        sourceLayer.setVisible(true);
                    }
                    if (willHideWarpingVectorLayer) {
                        WarpingPanel.this.warpingVectorLayerFinder().getLayer().setVisible(true);
                    }
                    if (outputLayerExistedOriginally) {
                        WarpingPanel.this.warp(sourceLayer, oldVectors, false);
                    } else {
                        WarpingPanel.this.toolbox.getContext().getLayerManager().remove(WarpingPanel.this.outputLayer(sourceLayer.getName()));
                    }
                }
                catch (Throwable t) {
                    WarpingPanel.this.toolbox.getContext().getErrorHandler().handleThrowable(t);
                    WarpingPanel.this.toolbox.getContext().getLayerManager().getUndoableEditReceiver().reportIrreversibleChange();
                }
            }
        }, this.toolbox.getContext(), false));
    }

    private void warp(Layer sourceLayer, Collection<Feature> warpingVectors, boolean generateIncrementalWarpingVectors) throws Exception {
        this.setWarpingFlag();
        try {
            Map<Triangle, Triangle> triangleMap = this.triangleMap(sourceLayer.getFeatureCollectionWrapper().getEnvelope(), warpingVectors, new ArrayList<TaggedCoordinate>(), new ArrayList<TaggedCoordinate>());
            BilinearInterpolatedTransform transform = new BilinearInterpolatedTransform(triangleMap, this.dummyMonitor);
            FeatureCollection outputFeatureCollection = transform.transform(sourceLayer.getFeatureCollectionWrapper());
            Layer outputLayer = this.outputLayer(sourceLayer.getName());
            if (outputLayer == null) {
                outputLayer = this.toolbox.getContext().getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, this.outputLayerName(sourceLayer.getName()), outputFeatureCollection);
                outputLayer.setStyles(sourceLayer.cloneStyles());
                this.addModificationListener(outputLayer);
            } else {
                outputLayer.setFeatureCollection(outputFeatureCollection);
            }
            outputLayer.getBlackboard().put(MODIFIED_OUTSIDE_WARP_KEY, false);
            outputLayer.getBlackboard().put(RECONSTRUCTION_VECTORS_KEY, this.clone(warpingVectors));
            if (generateIncrementalWarpingVectors) {
                if (this.incrementalWarpingVectorLayerFinder().getLayer() == null) {
                    this.incrementalWarpingVectorLayerFinder().createLayer();
                }
                this.incrementalWarpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().clear();
                this.incrementalWarpingVectorLayerFinder().getLayer().getFeatureCollectionWrapper().addAll(this.collapseToTip(warpingVectors));
            }
        }
        finally {
            this.clearWarpingFlag();
        }
    }

    public boolean isWarpingIncrementally() {
        return this.warpIncrementallyCheckBox.isEnabled() && this.warpIncrementallyCheckBox.isSelected();
    }

    void sourceComboBox_actionPerformed(ActionEvent e) {
        if (this.initializingSourceLayerComboBox) {
            return;
        }
        if (this.sourceLayerComboBoxModel.getSize() == 0) {
            return;
        }
        ((Layer)this.sourceLayerComboBoxModel.getSelectedItem()).getLayerManager().getBlackboard().put(LAST_SOURCE_LAYER_KEY, this.sourceLayerComboBoxModel.getSelectedItem());
    }

    private IncrementalWarpingVectorLayerFinder incrementalWarpingVectorLayerFinder() {
        return new IncrementalWarpingVectorLayerFinder(this.toolbox.getContext());
    }

    private WarpingVectorLayerFinder warpingVectorLayerFinder() {
        return new WarpingVectorLayerFinder(this.toolbox.getContext());
    }

    private boolean excludingFromLayerList(Layer layer) {
        if (layer == this.warpingVectorLayerFinder().getLayer()) {
            return true;
        }
        if (layer == this.incrementalWarpingVectorLayerFinder().getLayer()) {
            return true;
        }
        if (layer.getName().equals(ShowTriangulationPlugIn.SOURCE_LAYER_NAME)) {
            return true;
        }
        return layer.getName().equals(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME);
    }

    void warpIncrementallyCheckBox_actionPerformed(ActionEvent e) {
        this.updateComponents();
    }

    public void updateComponents() {
        this.toolbox.updateEnabledState();
        this.clearOutputButton.setEnabled(this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame);
        if (this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame) {
            if (this.lastLayerNamePanel != null) {
                this.lastLayerNamePanel.removeListener(this.layerNamePanelListener);
            }
            this.lastLayerNamePanel = ((LayerNamePanelProxy)((Object)this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame())).getLayerNamePanel();
            this.lastLayerNamePanel.addListener(this.layerNamePanelListener);
        }
        this.copyLayerButton.setEnabled(new CopySelectedLayersToWarpingVectorsPlugIn().createEnableCheck(this.toolbox.getContext()).check(null) == null);
        this.triangulationCheckBox.setSelected(this.toolbox.getContext().getLayerViewPanel() != null && this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME) != null && this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.SOURCE_LAYER_NAME).isVisible() && this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME) != null && this.toolbox.getContext().getLayerManager().getLayer(ShowTriangulationPlugIn.DESTINATION_LAYER_NAME).isVisible());
        this.updateSourceLayerComboBox();
        if (this.toolbox.getButton(DrawIncrementalWarpingVectorTool.class).isSelected() && !this.toolbox.getButton(DrawIncrementalWarpingVectorTool.class).isEnabled()) {
            this.toolbox.getButton(DrawWarpingVectorTool.class).doClick();
        }
        if (this.toolbox.getButton(DeleteIncrementalWarpingVectorTool.class).isSelected() && !this.toolbox.getButton(DeleteIncrementalWarpingVectorTool.class).isEnabled()) {
            this.toolbox.getButton(DeleteWarpingVectorTool.class).doClick();
        }
        if (this.toolbox.getButton(DrawWarpingVectorTool.class).isSelected() && !this.toolbox.getButton(DrawWarpingVectorTool.class).isEnabled()) {
            this.toolbox.getButton(DrawIncrementalWarpingVectorTool.class).doClick();
        }
        if (this.toolbox.getButton(DeleteWarpingVectorTool.class).isSelected() && !this.toolbox.getButton(DeleteWarpingVectorTool.class).isEnabled()) {
            this.toolbox.getButton(DeleteIncrementalWarpingVectorTool.class).doClick();
        }
    }

    private void updateSourceLayerComboBox() {
        this.initializingSourceLayerComboBox = true;
        try {
            this.sourceLayerComboBoxModel.removeAllElements();
            if (!(this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerViewPanelProxy)) {
                return;
            }
            LayerViewPanelProxy proxy = (LayerViewPanelProxy)((Object)this.toolbox.getContext().getWorkbench().getFrame().getActiveInternalFrame());
            for (Layer layer : proxy.getLayerViewPanel().getLayerManager().getLayers()) {
                if (this.excludingFromLayerList(layer)) continue;
                this.sourceLayerComboBoxModel.addElement(layer);
            }
            if (this.sourceLayerComboBoxModel.getSize() > 0) {
                Layer lastSourceLayer = (Layer)proxy.getLayerViewPanel().getLayerManager().getBlackboard().get(LAST_SOURCE_LAYER_KEY);
                if (lastSourceLayer == null || !proxy.getLayerViewPanel().getLayerManager().getLayers().contains(lastSourceLayer)) {
                    proxy.getLayerViewPanel().getLayerManager().getBlackboard().put(LAST_SOURCE_LAYER_KEY, this.sourceLayerComboBoxModel.getElementAt(0));
                }
                this.sourceLayerComboBoxModel.setSelectedItem(proxy.getLayerViewPanel().getLayerManager().getBlackboard().get(LAST_SOURCE_LAYER_KEY));
            }
            String listenerAddedKey = String.valueOf(this.getClass().getName()) + " - LISTENER ADDED";
            if (!proxy.getLayerViewPanel().getLayerManager().getBlackboard().get(listenerAddedKey, false)) {
                proxy.getLayerViewPanel().getLayerManager().addLayerListener(new LayerListener(){

                    @Override
                    public void categoryChanged(CategoryEvent e) {
                    }

                    @Override
                    public void layerChanged(LayerEvent e) {
                        WarpingPanel.this.updateSourceLayerComboBox();
                    }

                    @Override
                    public void featuresChanged(FeatureEvent e) {
                    }
                });
                proxy.getLayerViewPanel().getLayerManager().getBlackboard().put(listenerAddedKey, true);
            }
        }
        finally {
            this.initializingSourceLayerComboBox = false;
        }
    }

    public UndoableCommand addWarpingVectorGeneration(final UndoableCommand wrappeeCommand) throws NoninvertibleTransformException {
        return new UndoableCommand(wrappeeCommand.getName()){
            private UndoableCommand generateWarpingVectorsCommand;
            {
                super($anonymous0);
                this.generateWarpingVectorsCommand = null;
            }

            private UndoableCommand generateWarpingVectorsCommand() throws Exception {
                if (this.generateWarpingVectorsCommand == null) {
                    this.generateWarpingVectorsCommand = WarpingPanel.this.generateWarpingVectorsCommand();
                }
                return this.generateWarpingVectorsCommand;
            }

            @Override
            public void execute() throws Exception {
                wrappeeCommand.execute();
                this.generateWarpingVectorsCommand().execute();
            }

            @Override
            public void unexecute() throws Exception {
                this.generateWarpingVectorsCommand().unexecute();
                wrappeeCommand.unexecute();
            }
        };
    }
}

