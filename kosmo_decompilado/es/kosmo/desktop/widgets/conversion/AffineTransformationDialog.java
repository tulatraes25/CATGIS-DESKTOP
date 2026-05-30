/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.plugins.conversion.AffineTransformationPlugIn;
import es.kosmo.desktop.widgets.conversion.TransRotScaleBuilder;
import es.kosmo.desktop.widgets.conversion.TriPointTransRotScaleBuilder;
import es.kosmo.desktop.widgets.conversion.TwoPointTransRotScaleBuilder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;
import org.saig.jump.widgets.util.JQueryChooserPanel;
import org.saig.jump.widgets.util.ToolTargetSelectorPanel;
import org.saig.jump.widgets.util.validating.NumericTextFieldValidator;

public class AffineTransformationDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AffineTransformationDialog.class);
    private static final String TOOL_TARGET_PANEL_TITLE = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.process");
    private static final String QUERY_CHOOSER_PANEL_TITLE = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.results-in-new-layer");
    private JTextField originXField;
    private JTextField originYField;
    private JTextField transXField;
    private JTextField transYField;
    private JTextField scaleXField;
    private JTextField scaleYField;
    private JTextField shearXField;
    private JTextField shearYField;
    private JTextField rotateAngleField;
    private JButton buttonSetToLowerLeft;
    private JButton buttonSetToMidpoint;
    private JButton setIdentityButton;
    private JAvailableLayersComboBox srcBaseLayerComboBox;
    private JAvailableLayersComboBox destBaseLayerComboBox;
    private ToolTargetSelectorPanel toolTargetSelectorPanel;
    private JQueryChooserPanel resultQueryChooserPanel;
    private OKCancelPanel okCancelPanel;
    protected boolean exitOK;
    private final Layer layer;
    private final SelectionManager sm;
    private final LayerManager lm;

    public AffineTransformationDialog(JFrame parent, boolean modal, Layer layer, SelectionManager sm, LayerManager lm) {
        super((Frame)parent, modal);
        this.layer = layer;
        this.sm = sm;
        this.lm = lm;
        this.exitOK = false;
        this.setTitle(I18N.getMessage("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.{0}-in-layer-{1}", new Object[]{AffineTransformationPlugIn.NAME, layer.getName()}));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((Component)this.createImagePanel(), "West");
        mainPanel.add((Component)this.createOptionsCenterPanel(), "Center");
        mainPanel.add((Component)this.createOptionsEastPanel(), "East");
        mainPanel.add((Component)this.createOKCancelPanel(), "South");
        return mainPanel;
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new GridBagLayout());
        JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/AffineTransformation.png"));
        JTextArea descriptionTextArea = new JTextArea(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.affine-transformation-is-specified-by-a-combination-of-scales-rotations-and-traslations"));
        descriptionTextArea.setOpaque(false);
        descriptionTextArea.setEnabled(false);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFont(imageLabel.getFont());
        descriptionTextArea.setDisabledTextColor(imageLabel.getForeground());
        FormUtils.addRowInGBL(imagePanel, 0, 0, imageLabel);
        FormUtils.addRowInGBL(imagePanel, 1, 0, descriptionTextArea);
        return imagePanel;
    }

    private JPanel createOptionsCenterPanel() {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        this.toolTargetSelectorPanel = new ToolTargetSelectorPanel(TOOL_TARGET_PANEL_TITLE, this.layer, this.sm);
        this.resultQueryChooserPanel = new JQueryChooserPanel(QUERY_CHOOSER_PANEL_TITLE, I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.save-results"), false);
        FormUtils.addRowInGBL(optionsPanel, 0, 0, this.toolTargetSelectorPanel);
        FormUtils.addRowInGBL(optionsPanel, 1, 0, this.resultQueryChooserPanel);
        FormUtils.addRowInGBL(optionsPanel, 2, 0, this.createPuntoAnclajePanel());
        FormUtils.addFiller(optionsPanel, 10, 0);
        return optionsPanel;
    }

    private JPanel createPuntoAnclajePanel() {
        JPanel puntoAnclajePanel = new JPanel(new GridBagLayout());
        puntoAnclajePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.anchor-point")));
        this.originXField = new JTextField("0.0");
        this.originXField.setHorizontalAlignment(4);
        this.originXField.setInputVerifier(new NumericTextFieldValidator(this, this.originXField));
        this.originYField = new JTextField("0.0");
        this.originYField.setHorizontalAlignment(4);
        this.originYField.setInputVerifier(new NumericTextFieldValidator(this, this.originYField));
        this.buttonSetToLowerLeft = new JButton(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.left-below-point"));
        this.buttonSetToLowerLeft.addActionListener(new OriginLLListener(true));
        this.buttonSetToMidpoint = new JButton(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.central-point"));
        this.buttonSetToMidpoint.addActionListener(new OriginLLListener(false));
        FormUtils.addRowInGBL((JComponent)puntoAnclajePanel, 1, 0, new JLabel("X"), (JComponent)this.originXField);
        FormUtils.addRowInGBL((JComponent)puntoAnclajePanel, 2, 0, new JLabel("Y"), (JComponent)this.originYField);
        FormUtils.addRowInGBL(puntoAnclajePanel, 3, 0, this.buttonSetToLowerLeft);
        FormUtils.addRowInGBL(puntoAnclajePanel, 4, 0, this.buttonSetToMidpoint);
        return puntoAnclajePanel;
    }

    private JPanel createOptionsEastPanel() {
        JPanel eastPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(eastPanel, 0, 0, this.createTransformacionPanel());
        FormUtils.addRowInGBL(eastPanel, 1, 0, this.createVectoresDirectoresPanel());
        FormUtils.addFiller(eastPanel, 2, 0);
        return eastPanel;
    }

    private JPanel createVectoresDirectoresPanel() {
        JPanel vectoresDirectoresPanel = new JPanel(new GridBagLayout());
        vectoresDirectoresPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.directional-vectors")));
        this.srcBaseLayerComboBox = new JAvailableLayersComboBox(this.lm, false, false, true);
        this.destBaseLayerComboBox = new JAvailableLayersComboBox(this.lm, false, false, true);
        JButton buttonParam = new JButton(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.calculate-parameters"));
        buttonParam.addActionListener(new UpdateParamListener());
        FormUtils.addRowInGBL((JComponent)vectoresDirectoresPanel, 1, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.source-layer")), (JComponent)this.srcBaseLayerComboBox);
        FormUtils.addRowInGBL((JComponent)vectoresDirectoresPanel, 2, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.target-layer")), (JComponent)this.destBaseLayerComboBox);
        FormUtils.addRowInGBL(vectoresDirectoresPanel, 3, 0, buttonParam);
        return vectoresDirectoresPanel;
    }

    private JPanel createTransformacionPanel() {
        JPanel transformacionPanel = new JPanel(new GridBagLayout());
        transformacionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.transformation")));
        this.scaleXField = new JTextField("1.0");
        this.scaleXField.setHorizontalAlignment(4);
        this.scaleXField.setInputVerifier(new NumericTextFieldValidator(this, this.scaleXField));
        this.scaleYField = new JTextField("1.0");
        this.scaleYField.setHorizontalAlignment(4);
        this.scaleYField.setInputVerifier(new NumericTextFieldValidator(this, this.scaleYField));
        this.rotateAngleField = new JTextField("0.0");
        this.rotateAngleField.setHorizontalAlignment(4);
        this.rotateAngleField.setInputVerifier(new NumericTextFieldValidator(this, this.rotateAngleField));
        this.shearXField = new JTextField("0.0");
        this.shearXField.setHorizontalAlignment(4);
        this.shearXField.setInputVerifier(new NumericTextFieldValidator(this, this.shearXField));
        this.shearYField = new JTextField("0.0");
        this.shearYField.setHorizontalAlignment(4);
        this.shearYField.setInputVerifier(new NumericTextFieldValidator(this, this.shearYField));
        this.transXField = new JTextField("0.0");
        this.transXField.setHorizontalAlignment(4);
        this.transXField.setInputVerifier(new NumericTextFieldValidator(this, this.transXField));
        this.transYField = new JTextField("0.0");
        this.transYField.setHorizontalAlignment(4);
        this.transYField.setInputVerifier(new NumericTextFieldValidator(this, this.transYField));
        this.setIdentityButton = new JButton(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.identity-transformation"));
        this.setIdentityButton.addActionListener(new SetIdentityListener());
        FormUtils.addRowInGBL(transformacionPanel, 0, 0, new JLabel("<HTML><B>" + I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.scale") + "</B></HTML>"));
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 1, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.factor-x")), (JComponent)this.scaleXField);
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 2, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.factor-y")), (JComponent)this.scaleYField);
        FormUtils.addRowInGBL(transformacionPanel, 3, 0, new JLabel("<HTML><B>" + I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.rotation") + "</B></HTML>"));
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 4, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.angle")), (JComponent)this.rotateAngleField);
        FormUtils.addRowInGBL(transformacionPanel, 5, 0, new JLabel("<HTML><B>" + I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.shearing") + "</B></HTML>"));
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 6, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.shearing-x")), (JComponent)this.shearXField);
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 7, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.shearing-y")), (JComponent)this.shearYField);
        FormUtils.addRowInGBL(transformacionPanel, 8, 0, new JLabel("<HTML><B>" + I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.traslation") + "</B></HTML>"));
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 9, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.traslation-x")), (JComponent)this.transXField);
        FormUtils.addRowInGBL((JComponent)transformacionPanel, 10, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.traslation-y")), (JComponent)this.transYField);
        FormUtils.addRowInGBL(transformacionPanel, 11, 0, this.setIdentityButton);
        return transformacionPanel;
    }

    private JPanel createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){
            boolean error = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AffineTransformationDialog.this.okCancelPanel.wasOKPressed()) {
                    if (AffineTransformationDialog.this.isInputValid()) {
                        if (AffineTransformationDialog.this.layer.isEditable() && AffineTransformationDialog.this.resultQueryChooserPanel.getDataSourceQuery() == null) {
                            int option = DialogFactory.showYesNoCancelWarningDialog(AffineTransformationDialog.this, I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.you-selected-to-modify-the-layer-changes-will-be-irreversibles-do-you-want-to-continue"), I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.warning"));
                            if (option == 0) {
                                AffineTransformationDialog.this.exitOK = true;
                                this.error = false;
                            } else {
                                AffineTransformationDialog.this.exitOK = false;
                                this.error = true;
                            }
                        } else {
                            AffineTransformationDialog.this.exitOK = true;
                            this.error = false;
                        }
                    } else {
                        AffineTransformationDialog.this.exitOK = false;
                        this.error = true;
                    }
                } else {
                    AffineTransformationDialog.this.exitOK = false;
                    this.error = false;
                }
                if (!this.error) {
                    AffineTransformationDialog.this.setVisible(false);
                    AffineTransformationDialog.this.dispose();
                }
            }
        });
        return this.okCancelPanel;
    }

    private boolean isInputValid() {
        if (!this.resultQueryChooserPanel.isInputValid()) {
            return false;
        }
        if (!this.originXField.getInputVerifier().verify(this.originXField)) {
            return false;
        }
        if (!this.originYField.getInputVerifier().verify(this.originYField)) {
            return false;
        }
        if (!this.transXField.getInputVerifier().verify(this.transXField)) {
            return false;
        }
        if (!this.transYField.getInputVerifier().verify(this.transYField)) {
            return false;
        }
        if (!this.scaleXField.getInputVerifier().verify(this.scaleXField)) {
            return false;
        }
        if (!this.scaleYField.getInputVerifier().verify(this.scaleYField)) {
            return false;
        }
        if (!this.shearXField.getInputVerifier().verify(this.shearXField)) {
            return false;
        }
        if (!this.shearYField.getInputVerifier().verify(this.shearYField)) {
            return false;
        }
        return this.rotateAngleField.getInputVerifier().verify(this.rotateAngleField);
    }

    public boolean isExitOk() {
        return this.exitOK;
    }

    public FeatureIterator getFeaturesToProcess(PlugInContext context) throws Exception {
        return this.toolTargetSelectorPanel.getFeaturesToProcess(context);
    }

    public int getSelectedTargetOption() {
        return this.toolTargetSelectorPanel.getSelectedOption();
    }

    private double getDouble(JTextField textField) {
        return Double.parseDouble(textField.getText().trim());
    }

    private void updateOriginLL(boolean isLowerLeft) {
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        Envelope env = null;
        try {
            env = fc.getEnvelope();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            env = new Envelope();
        }
        double x = env.getMinX();
        double y = env.getMinY();
        if (!isLowerLeft) {
            x = (env.getMinX() + env.getMaxX()) / 2.0;
            y = (env.getMinY() + env.getMaxY()) / 2.0;
        }
        this.originXField.setText(String.valueOf(x));
        this.originYField.setText(String.valueOf(y));
    }

    private void setToIdentity() {
        this.scaleXField.setText("1.0");
        this.scaleYField.setText("1.0");
        this.shearXField.setText("0.0");
        this.shearYField.setText("0.0");
        this.transXField.setText("0.0");
        this.transYField.setText("0.0");
        this.rotateAngleField.setText("0.0");
    }

    private String updateParams() {
        Layer layerSrc = this.srcBaseLayerComboBox.getSelectedLayer();
        Layer layerDest = this.destBaseLayerComboBox.getSelectedLayer();
        FeatureCollection fcSrc = layerSrc.getUltimateFeatureCollectionWrapper();
        FeatureCollection fcDest = layerDest.getUltimateFeatureCollectionWrapper();
        AffineTransControlPointExtracter controlPtExtracter = new AffineTransControlPointExtracter(fcSrc, fcDest);
        String parseErrMsg = null;
        if (controlPtExtracter.getInputType() == 0) {
            parseErrMsg = controlPtExtracter.getParseErrorMessage();
            return parseErrMsg;
        }
        Coordinate[] srcPts = controlPtExtracter.getSrcControlPoints();
        Coordinate[] destPts = controlPtExtracter.getDestControlPoints();
        TransRotScaleBuilder trsBuilder = null;
        switch (srcPts.length) {
            case 2: {
                trsBuilder = new TwoPointTransRotScaleBuilder(srcPts, destPts);
                break;
            }
            case 3: {
                trsBuilder = new TriPointTransRotScaleBuilder(srcPts, destPts);
            }
        }
        if (trsBuilder != null) {
            this.updateParams(trsBuilder);
        }
        return null;
    }

    private void updateParams(TransRotScaleBuilder trsBuilder) {
        this.originXField.setText(String.valueOf(trsBuilder.getOriginX()));
        this.originYField.setText(String.valueOf(trsBuilder.getOriginY()));
        this.scaleXField.setText(String.valueOf(trsBuilder.getScaleX()));
        this.scaleYField.setText(String.valueOf(trsBuilder.getScaleY()));
        this.transXField.setText(String.valueOf(trsBuilder.getTranslateX()));
        this.transYField.setText(String.valueOf(trsBuilder.getTranslateY()));
        this.rotateAngleField.setText(String.valueOf(trsBuilder.getRotationAngle()));
    }

    public double getDoubleOriginX() {
        return this.getDouble(this.originXField);
    }

    public double getDoubleOriginY() {
        return this.getDouble(this.originYField);
    }

    public double getDoubleTransX() {
        return this.getDouble(this.transXField);
    }

    public double getDoubleTransY() {
        return this.getDouble(this.transYField);
    }

    public double getDoubleScaleX() {
        return this.getDouble(this.scaleXField);
    }

    public double getDoubleScaleY() {
        return this.getDouble(this.scaleYField);
    }

    public double getDoubleShearX() {
        return this.getDouble(this.shearXField);
    }

    public double getDoubleShearY() {
        return this.getDouble(this.shearYField);
    }

    public double getDoubleRotationAngle() {
        return this.getDouble(this.rotateAngleField);
    }

    public DataSourceQuery getResultQuery() {
        return this.resultQueryChooserPanel.getDataSourceQuery();
    }

    public boolean hasFeaturesToProcess(PlugInContext context) throws Exception {
        return this.toolTargetSelectorPanel.hasFeaturesToProcess(context);
    }

    private class AffineTransControlPointExtracter {
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_VECTOR = 1;
        public static final int TYPE_LINE_3 = 2;
        private FeatureCollection fcSrc;
        private FeatureCollection fcDest;
        private int inputType = 0;
        private String parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-geometry-not-recognized");
        private Geometry[] geomSrc = new Geometry[3];
        private Geometry[] geomDest = new Geometry[3];
        private Coordinate[] controlPtSrc;
        private Coordinate[] controlPtDest;

        public AffineTransControlPointExtracter(FeatureCollection fcSrc, FeatureCollection fcDest) {
            this.fcSrc = fcSrc;
            this.fcDest = fcDest;
            this.init();
        }

        public int getInputType() {
            return this.inputType;
        }

        public String getParseErrorMessage() {
            return this.parseErrMsg;
        }

        public Coordinate[] getSrcControlPoints() {
            return this.controlPtSrc;
        }

        public Coordinate[] getDestControlPoints() {
            return this.controlPtDest;
        }

        private void init() {
            this.parseInput();
        }

        private void parseInput() {
            int fcDestSize;
            int fcSrcSize;
            this.inputType = 0;
            try {
                fcSrcSize = this.fcSrc.size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                fcSrcSize = 0;
            }
            try {
                fcDestSize = this.fcDest.size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                fcDestSize = 0;
            }
            if (fcSrcSize != fcDestSize) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-collections-must-have-same-size");
                return;
            }
            if (fcSrcSize != 1) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-must-have-only-one-geometry");
                return;
            }
            this.geomSrc[0] = this.fcSrc.getFeaturesSamples(1).iterator().next().getGeometry();
            this.geomDest[0] = this.fcDest.getFeaturesSamples(1).iterator().next().getGeometry();
            if (this.geomSrc[0].getClass() != this.geomDest[0].getClass()) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-must-be-linestring-type");
                return;
            }
            if (!(this.geomSrc[0] instanceof LineString)) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-must-be-linestring-type");
                return;
            }
            this.parseLines();
        }

        private void parseLines() {
            this.controlPtSrc = this.geomSrc[0].getCoordinates();
            this.controlPtDest = this.geomDest[0].getCoordinates();
            if (this.controlPtSrc.length != this.controlPtDest.length) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.control-points-have-different-longitudes");
                return;
            }
            if (this.controlPtSrc.length < 2) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.unique-control-points-are-not-supported");
            }
            if (this.controlPtSrc.length > 3) {
                this.parseErrMsg = I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.linestring-with-too-much-points");
            }
            this.inputType = 1;
        }
    }

    private class OriginLLListener
    implements ActionListener {
        private boolean isLowerLeft;

        OriginLLListener(boolean isLowerLeft) {
            this.isLowerLeft = isLowerLeft;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AffineTransformationDialog.this.updateOriginLL(this.isLowerLeft);
        }
    }

    private class SetIdentityListener
    implements ActionListener {
        private SetIdentityListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AffineTransformationDialog.this.setToIdentity();
        }
    }

    private class UpdateParamListener
    implements ActionListener {
        private UpdateParamListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String errMsg = AffineTransformationDialog.this.updateParams();
            if (errMsg != null) {
                DialogFactory.showWarningDialog(AffineTransformationDialog.this, errMsg, I18N.getString("org.saig.jump.widgets.utils.conversion.AffineTransformationDialog.error-with-control-point"));
            }
        }
    }
}

