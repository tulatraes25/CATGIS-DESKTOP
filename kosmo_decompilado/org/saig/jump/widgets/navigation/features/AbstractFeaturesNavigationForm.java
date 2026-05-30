/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.navigation.features;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.context.documents.DocumentManager;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.features.AbstractFeaturesNavigationPanel;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.IForm;

public abstract class AbstractFeaturesNavigationForm
extends JInternalFrame
implements IForm,
org.saig.jump.widgets.navigation.listener.DataModifiedListener,
IStatusForm,
LayerManagerProxy,
FocusListener,
KeyListener {
    private Logger LOGGER = Logger.getLogger(AbstractFeaturesNavigationForm.class);
    private JPanel buttonPanel;
    protected JButton firstButton;
    protected JButton previousButton;
    protected JButton nextButton;
    protected JButton lastButton;
    protected JButton saveChangesButton;
    private JButton cancelButton;
    private JButton zoomButton;
    private JButton flashButton;
    private JCheckBox zoomAutomaticoCheckBox;
    protected JTextField indexTextField;
    private JLabel indexLabel;
    private JButton selectAllButton;
    private boolean confirmOnExit;
    private JLabel messageLabel;
    private JButton compactButton;
    private JButton helpButton;
    protected int currentPosition = -1;
    protected int lastPosition;
    protected List<Feature> allFeatures;
    protected List<Feature> showingFeatures;
    protected Layer layer;
    protected AbstractFeaturesNavigationPanel attributesPanel;
    protected boolean editable;
    private boolean refreshing;
    protected boolean isDataModified = false;
    private TaskFrame taskFrame;
    private String documentHelpName;
    protected boolean compact;

    public AbstractFeaturesNavigationForm(String title, boolean resizable, boolean closable, boolean maxizable, TaskFrame taskFrame) {
        super(title, resizable, closable, maxizable);
        this.taskFrame = taskFrame;
        this.documentHelpName = DocumentManager.getHelpTag(this.getClass().getName());
        this.confirmOnExit = true;
    }

    public AbstractFeaturesNavigationForm(String title, boolean resizable, boolean closable, boolean maxizable, boolean editable, Collection<Feature> featureList, TaskFrame taskFrame) {
        this(title, resizable, closable, maxizable, editable, featureList, null, taskFrame);
        this.confirmOnExit = true;
    }

    public AbstractFeaturesNavigationForm(String title, boolean resizable, boolean closable, boolean maxizable, boolean editable, Collection<Feature> featureList, Layer layer, TaskFrame taskFrame) {
        super(title, resizable, closable, maxizable);
        this.documentHelpName = DocumentManager.getHelpTag(this.getClass().getName());
        this.taskFrame = taskFrame;
        this.layer = layer;
        this.showingFeatures = new ArrayList<Feature>();
        this.showingFeatures.addAll(featureList);
        this.allFeatures = new ArrayList<Feature>();
        this.allFeatures.addAll(featureList);
        this.attributesPanel = this.getAttributesPanel();
        this.editable = editable;
        this.compact = true;
        this.confirmOnExit = true;
        if (this.attributesPanel == null) {
            return;
        }
        this.attributesPanel.addDataModifiedListener(this);
        this.setTitle(title);
        if (this.showingFeatures == null || this.showingFeatures.size() == 0) {
            this.setTitle(I18N.getString(AbstractFeaturesNavigationForm.class, "no-elements-were-found"));
            this.setContentPane(new JLabel(I18N.getString(AbstractFeaturesNavigationForm.class, "no-elements-to-show-or-edit-were-found")));
            this.setClosable(true);
            return;
        }
        this.lastPosition = this.showingFeatures.size() - 1;
        JPanel navigationButtonsPanel = this.getButtonPanel();
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        this.messageLabel = new JLabel();
        this.messageLabel.setOpaque(true);
        this.messageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.messageLabel.setText(" ");
        statusPanel.add((Component)this.messageLabel, "Center");
        this.currentPosition = 0;
        this.refreshPanel();
        this.refreshNavigationButtons();
        this.setDataModified(false);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.attributesPanel);
        FormUtils.addFiller(mainPanel, 1, 0);
        FormUtils.addRowInGBL(mainPanel, 2, 0, navigationButtonsPanel);
        FormUtils.addRowInGBL((JComponent)mainPanel, 3, 0, (JComponent)statusPanel, true, false);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                AbstractFeaturesNavigationForm.this.rollback();
                AbstractFeaturesNavigationForm.this.onClosing();
            }
        });
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    public void setConfirmOnExit(boolean confirm) {
        this.confirmOnExit = confirm;
    }

    protected void onClosing() {
    }

    public void revalidate(List<Feature> features) {
        this.showingFeatures = features;
        this.lastPosition = this.showingFeatures.size() - 1;
        this.currentPosition = 0;
        this.refreshPanel();
        this.refreshNavigationButtons();
    }

    public JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel(new FlowLayout(1));
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.add(Box.createHorizontalGlue());
            this.zoomButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("ZoomSelected.gif")));
            this.zoomButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "zoom-to-current-element"));
            this.zoomButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractFeaturesNavigationForm.this.zoom();
                }
            });
            this.flashButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Flashlight.gif")));
            this.flashButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "flash-current-element"));
            this.flashButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractFeaturesNavigationForm.this.flash();
                }
            });
            this.firstButton = new JButton(IconLoader.icon("Start.gif"));
            this.firstButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "first"));
            this.firstButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractFeaturesNavigationForm.this.nextButton.requestFocus();
                    AbstractFeaturesNavigationForm.this.goToFirstFeature();
                }
            });
            this.previousButton = new JButton(IconLoader.icon("Prev.gif"));
            this.previousButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "previous"));
            this.previousButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (AbstractFeaturesNavigationForm.this.currentPosition == 1) {
                        AbstractFeaturesNavigationForm.this.nextButton.requestFocus();
                    }
                    AbstractFeaturesNavigationForm.this.goToFeatureAt(AbstractFeaturesNavigationForm.this.currentPosition - 1);
                }
            });
            this.nextButton = new JButton(IconLoader.icon("Next.gif"));
            this.nextButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "next"));
            this.nextButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractFeaturesNavigationForm.this.goToFeatureAt(AbstractFeaturesNavigationForm.this.currentPosition + 1);
                }
            });
            this.lastButton = new JButton(IconLoader.icon("End.gif"));
            this.lastButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "last"));
            this.lastButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractFeaturesNavigationForm.this.goToLastFeature();
                }
            });
            this.saveChangesButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Save.gif")));
            this.saveChangesButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "save-changes"));
            this.saveChangesButton.setEnabled(false);
            this.saveChangesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractFeaturesNavigationForm.this.action_saveChanges();
                }
            });
            this.cancelButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("close.gif")));
            this.cancelButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "cancel"));
            this.cancelButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (AbstractFeaturesNavigationForm.this.confirmOnExit && AbstractFeaturesNavigationForm.this.isDataModified()) {
                        int response = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.navigation.features.AbstractFeaturesNavigationForm.The-form-will-be-close-without-saving-its-changes-Are-you-sure"), I18N.getString("org.saig.jump.widgets.navigation.features.AbstractFeaturesNavigationForm.Close-without-saving"));
                        if (response == 0) {
                            AbstractFeaturesNavigationForm.this.rollback();
                            AbstractFeaturesNavigationForm.this.close();
                        }
                    } else {
                        AbstractFeaturesNavigationForm.this.rollback();
                        AbstractFeaturesNavigationForm.this.close();
                    }
                }
            });
            this.compactButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("restaurar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "compact"));
            this.compactButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractFeaturesNavigationForm.this.compact();
                }
            });
            this.helpButton = new JButton(IconLoader.icon("help.png"));
            this.helpButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "help"));
            this.helpButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        DocumentManager manager = DocumentManager.getInstance("_HELP_DOCUMENT_MANAGER_KEY_");
                        manager.openDocumentByInternalName(AbstractFeaturesNavigationForm.this.taskFrame, AbstractFeaturesNavigationForm.this.documentHelpName);
                    }
                    catch (Exception e1) {
                        AbstractFeaturesNavigationForm.this.LOGGER.error((Object)"", (Throwable)e1);
                        DialogFactory.showErrorDialog(AbstractFeaturesNavigationForm.this.taskFrame, I18N.getString(AbstractFeaturesNavigationForm.class, "an-unexpected-error-occurred-please-check-the-system-log"), I18N.getString(AbstractFeaturesNavigationForm.class, "error-opening-document"));
                    }
                }
            });
            this.zoomAutomaticoCheckBox = new JCheckBox(I18N.getString(AbstractFeaturesNavigationForm.class, "automatic-zoom"));
            this.zoomAutomaticoCheckBox.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "perform-automatic-zoom-to-selected-element"));
            Dimension indexDim = new Dimension(60, 20);
            this.indexLabel = new JLabel();
            this.indexLabel.setMinimumSize(indexDim);
            this.indexLabel.setPreferredSize(indexDim);
            this.indexLabel.setHorizontalAlignment(2);
            this.indexLabel.setVerticalAlignment(0);
            this.indexTextField = new JTextField();
            this.indexTextField.setMinimumSize(indexDim);
            this.indexTextField.setPreferredSize(indexDim);
            this.indexTextField.setHorizontalAlignment(4);
            this.indexTextField.addFocusListener(this);
            this.indexTextField.addKeyListener(this);
            this.selectAllButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("selectAll.png")));
            this.selectAllButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "select-all"));
            this.selectAllButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractFeaturesNavigationForm.this.selectAllFeatures();
                }
            });
            toolBar.add(this.zoomAutomaticoCheckBox);
            toolBar.add(this.zoomButton);
            toolBar.add(this.flashButton);
            toolBar.addSeparator();
            toolBar.add(this.selectAllButton);
            toolBar.addSeparator();
            toolBar.add(this.firstButton);
            toolBar.add(this.previousButton);
            toolBar.add(this.indexTextField);
            toolBar.add(this.indexLabel);
            toolBar.add(this.nextButton);
            toolBar.add(this.lastButton);
            toolBar.addSeparator();
            if (this.editable) {
                toolBar.add(this.saveChangesButton);
            } else {
                this.attributesPanel.disable();
            }
            toolBar.add(this.helpButton);
            toolBar.add(this.cancelButton);
            toolBar.addSeparator();
            toolBar.add(this.compactButton);
            toolBar.add(Box.createHorizontalGlue());
            this.buttonPanel.add(toolBar);
        }
        return this.buttonPanel;
    }

    protected void action_saveChanges() {
        this.clearStatusMessage();
        if (this.isDataModified()) {
            if (!this.beforeSave()) {
                return;
            }
            if (!this.isInputValid()) {
                return;
            }
            try {
                this.update();
            }
            catch (Exception e) {
                this.LOGGER.error((Object)"", (Throwable)e);
                String errorMessage = I18N.getString(AbstractFeaturesNavigationForm.class, "an-error-occurred-while-updating-an-element-state-check-the-log-file");
                String errorTitle = I18N.getString(AbstractFeaturesNavigationForm.class, "error");
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), errorMessage, errorTitle);
                this.rollback();
                this.close();
                return;
            }
            try {
                this.commit();
            }
            catch (Exception e1) {
                this.layer.getUltimateFeatureCollectionWrapper().rollBack();
                String errorMessage = I18N.getString(AbstractFeaturesNavigationForm.class, "an-error-occurred-while-trying-to-save-changes-check-the-log-file-for-more-details");
                String errorTitle = I18N.getString(AbstractFeaturesNavigationForm.class, "error-saving-changes");
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), errorMessage, errorTitle);
                this.LOGGER.error((Object)"", (Throwable)e1);
            }
            this.close();
        }
    }

    protected boolean beforeSave() {
        return this.attributesPanel.beforeSave();
    }

    private void selectAllFeatures() {
        SelectionManager selMgr = this.taskFrame.getLayerViewPanel().getSelectionManager();
        selMgr.unselectItems(this.layer);
        selMgr.getFeatureSelection().selectItems(this.layer, this.showingFeatures);
        this.layer.fireAppearanceChanged();
    }

    public void goToFeatureAt(int position) {
        if (CollectionUtils.isNotEmpty(this.showingFeatures) && position >= 0 && position < this.showingFeatures.size()) {
            if (!this.isInputValid()) {
                return;
            }
            this.clearStatusMessage();
            if (this.isDataModified()) {
                try {
                    this.update();
                }
                catch (Exception e) {
                    this.LOGGER.error((Object)"", (Throwable)e);
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(AbstractFeaturesNavigationForm.class, "error"), I18N.getString(AbstractFeaturesNavigationForm.class, "an-error-occurred-while-updating-an-element-state-check-the-log-file"));
                    this.rollback();
                    this.close();
                    return;
                }
            }
            this.currentPosition = position;
            this.refreshNavigationButtons();
            this.refreshPanel();
            this.zoomIFNecesary();
            this.flash();
        }
    }

    public void goToFirstFeature() {
        this.goToFeatureAt(0);
    }

    public void goToLastFeature() {
        this.goToFeatureAt(this.lastPosition);
    }

    @Override
    public boolean isInputValid() {
        return this.attributesPanel.isInputValid();
    }

    public void refreshPanel() {
        this.setRefreshing(true);
        this.attributesPanel.refresh(this.showingFeatures.get(this.currentPosition));
        this.setRefreshing(false);
    }

    public void refreshNavigationButtons() {
        this.helpButton.setEnabled(this.documentHelpName != null);
        if (this.showingFeatures.size() == 1) {
            this.previousButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.firstButton.setEnabled(false);
            this.lastButton.setEnabled(false);
        } else if (this.currentPosition == 0) {
            this.previousButton.setEnabled(false);
            this.nextButton.setEnabled(true);
            this.firstButton.setEnabled(false);
            this.lastButton.setEnabled(true);
        } else if (this.currentPosition == this.lastPosition) {
            this.previousButton.setEnabled(true);
            this.nextButton.setEnabled(false);
            this.firstButton.setEnabled(true);
            this.lastButton.setEnabled(false);
        } else {
            this.previousButton.setEnabled(true);
            this.nextButton.setEnabled(true);
            this.firstButton.setEnabled(true);
            this.lastButton.setEnabled(true);
        }
        this.indexTextField.setText("" + (this.currentPosition + 1));
        this.indexLabel.setText("/" + (this.lastPosition + 1));
    }

    public void setDataModified(boolean dataModified) {
        if (dataModified && this.isInputValid()) {
            this.saveChangesButton.setEnabled(true);
            this.isDataModified = true;
        } else {
            this.saveChangesButton.setEnabled(false);
        }
    }

    public boolean isDataModified() {
        return this.isDataModified;
    }

    @Override
    public void update() throws Exception {
        this.attributesPanel.update(this.showingFeatures.get(this.currentPosition));
    }

    public void zoom() {
        if (this.layer == null) {
            return;
        }
        Feature selectedFeature = this.showingFeatures.get(this.currentPosition);
        LayerViewPanel layerViewPanel = this.taskFrame.getLayerViewPanel();
        Geometry geomToZoom = selectedFeature.getGeometry();
        ArrayList<Geometry> geomsToZoom = new ArrayList<Geometry>();
        geomsToZoom.add(geomToZoom);
        try {
            new ZoomToSelectedItemsPlugIn().zoom(geomsToZoom, layerViewPanel);
            ArrayList<Feature> features = new ArrayList<Feature>();
            features.add(selectedFeature);
            SelectionManager selectionManager = this.taskFrame.getSelectionManager();
            selectionManager.unselectItems(this.layer);
            selectionManager.getFeatureSelection().selectItems(this.layer, features);
        }
        catch (NoninvertibleTransformException ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(AbstractFeaturesNavigationForm.class, "error"), I18N.getString(AbstractFeaturesNavigationForm.class, "an-error-occurred-while-trying-to-zoom-to-current-element-check-the-log-file"));
        }
    }

    public void flash() {
        if (this.layer == null) {
            return;
        }
        Feature selectedFeature = this.showingFeatures.get(this.currentPosition);
        LayerViewPanel layerViewPanel = this.taskFrame.getLayerViewPanel();
        Geometry geomToFlash = selectedFeature.getGeometry();
        if (geomToFlash == null) {
            return;
        }
        try {
            ArrayList<Geometry> geometries = new ArrayList<Geometry>();
            geometries.add(geomToFlash);
            ZoomToSelectedItemsPlugIn.flash(geometries, layerViewPanel);
        }
        catch (Exception e1) {
            this.LOGGER.error((Object)"", (Throwable)e1);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(AbstractFeaturesNavigationForm.class, "error"), I18N.getString(AbstractFeaturesNavigationForm.class, "an-error-occurred-while-trying-to-flash-the-current-element-check-the-log-file"));
        }
    }

    @Override
    public void rollback() {
        if (this.layer == null) {
            return;
        }
        this.layer.getUltimateFeatureCollectionWrapper().rollBack();
        this.layer.fireAppearanceChanged();
        this.layer.fireLayerChanged(LayerEventType.COMMITED);
        this.attributesPanel.rollback();
    }

    public void close() {
        this.onClosing();
        this.dispose();
    }

    @Override
    public void commit() throws Exception {
        if (this.layer == null) {
            return;
        }
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        fc.updateAll(this.showingFeatures);
        fc.commit();
        this.attributesPanel.commit();
        this.layer.fireAppearanceChanged();
        this.layer.fireLayerChanged(LayerEventType.COMMITED);
    }

    protected void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public boolean isRefreshing() {
        return this.refreshing;
    }

    public abstract AbstractFeaturesNavigationPanel getAttributesPanel();

    @Override
    public void dataModified() {
        if (!this.isRefreshing()) {
            this.setDataModified(true);
        }
    }

    public Feature getCurrentFeature() {
        if (this.currentPosition == -1) {
            return null;
        }
        return this.showingFeatures.get(this.currentPosition);
    }

    public TaskFrame getTaskFrame() {
        return this.taskFrame;
    }

    protected void compact() {
        boolean bl = this.compact = !this.compact;
        if (this.compact) {
            this.compactButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("restaurar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "compact"));
        } else {
            this.compactButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("maximizar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractFeaturesNavigationForm.class, "maximize"));
        }
        this.packAndCompact();
    }

    public void packAndCompact() {
        this.attributesPanel.compact(this.compact);
        this.pack();
    }

    private void zoomIFNecesary() {
        if (this.zoomAutomaticoCheckBox.isSelected()) {
            this.zoom();
        }
    }

    @Override
    public LayerManager getLayerManager() {
        return this.taskFrame.getLayerManager();
    }

    @Override
    public void warnUser(final String warning) {
        new Timer(100, new ActionListener(){
            private int tickCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                ++this.tickCount;
                AbstractFeaturesNavigationForm.this.setStatusBarText(warning);
                AbstractFeaturesNavigationForm.this.setStatusBarTextHighlighted(this.tickCount % 2 == 0);
                if (this.tickCount == 4) {
                    Timer timer = (Timer)e.getSource();
                    timer.stop();
                }
            }
        }).start();
    }

    public void setStatusMessage(String message) {
        this.setStatusBarText(message);
        this.setStatusBarTextHighlighted(false);
    }

    @Override
    public void clearStatusMessage() {
        this.setStatusMessage("");
    }

    private void setStatusBarText(String message) {
        this.messageLabel.setText(StringUtils.isEmpty((String)message) ? " " : message);
        this.messageLabel.setToolTipText(message);
    }

    private void setStatusBarTextHighlighted(boolean highlighted) {
        this.messageLabel.setForeground(highlighted ? Color.black : new JLabel().getForeground());
        this.messageLabel.setBackground(highlighted ? Color.yellow : new JLabel().getBackground());
    }

    public Layer getFormLayer() {
        return this.layer;
    }

    public List<Feature> getAllFeatures() {
        return new ArrayList<Feature>(this.allFeatures);
    }

    public List<Feature> getShowingFeatures() {
        return new ArrayList<Feature>(this.showingFeatures);
    }

    public void addFeature(Feature feature) {
        this.allFeatures.add(feature);
    }

    public void removeFeature(Feature feature) {
        this.allFeatures.remove(feature);
    }

    @Override
    public void focusGained(FocusEvent fe) {
        if (fe.getSource() == this.indexTextField) {
            this.indexTextField.selectAll();
        }
    }

    @Override
    public void focusLost(FocusEvent fe) {
        if (fe.getSource() == this.indexTextField) {
            try {
                int parsedIndex = Integer.parseInt(this.indexTextField.getText());
                if (parsedIndex < 1 || parsedIndex > this.lastPosition + 1) {
                    this.indexTextField.setText("" + (this.currentPosition + 1));
                } else if (this.currentPosition != parsedIndex - 1) {
                    this.goToFeatureAt(parsedIndex - 1);
                }
            }
            catch (NumberFormatException nfe) {
                this.indexTextField.setText("" + (this.currentPosition + 1));
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getSource() == this.indexTextField) {
            if (ke.getKeyCode() == 27) {
                this.indexTextField.setText("" + (this.currentPosition + 1));
                this.nextButton.requestFocus();
            } else if (ke.getKeyCode() == 10) {
                this.nextButton.requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    public static interface DataModifiedListener
    extends EventListener {
        public void dataModified();
    }
}

