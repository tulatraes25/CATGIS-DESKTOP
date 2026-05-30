/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.Assert
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.symbolizer.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.components.JRadioButtonWithIcon;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class DefaultSymbolizerChooserDialog
extends SymbolizerChooserDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DefaultSymbolizerChooserDialog.class);
    protected FeatureSchema schema;
    protected JPanel symbolizerSelectionPanel;
    protected JLabel instructionsLabel;
    protected JRadioButtonWithIcon rbtPoint;
    protected JRadioButtonWithIcon rbtPolygon;
    protected JRadioButtonWithIcon rbtLine;
    protected JRadioButtonWithIcon rbtText;
    protected JRadioButtonWithIcon rbtRaster;
    protected OKCancelPanel okCancelPanel;

    public DefaultSymbolizerChooserDialog(Component parent, FeatureSchema ft) {
        super(JOptionPane.getFrameForComponent(parent), true);
        this.schema = ft;
        this.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.symbolizer-list"));
        this.initialize();
        this.refreshSymbolizerOptions();
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void refreshSymbolizerOptions() {
        int geomType = this.schema.getGeometryType();
        this.rbtPoint.setEnabled(geomType == 1 || geomType == 8 || geomType == 3 || geomType == 2);
        this.rbtLine.setEnabled(geomType == 3 || geomType == 2);
        this.rbtPolygon.setEnabled(geomType == 5 || geomType == 4);
        this.rbtText.setEnabled(geomType != 0);
        this.rbtRaster.setEnabled(geomType == 0);
        this.setDefaultSelection();
        this.setResizable(false);
    }

    private void setDefaultSelection() {
        Symbolizer symbolizer = SymbolizerUtils.getDefaultSymbolizer(this.schema);
        if (symbolizer instanceof LineSymbolizer) {
            this.rbtLine.setSelected(true);
        } else if (symbolizer instanceof PointSymbolizer) {
            this.rbtPoint.setSelected(true);
        } else if (symbolizer instanceof PolygonSymbolizer) {
            this.rbtPolygon.setSelected(true);
        } else if (symbolizer instanceof TextSymbolizer) {
            this.rbtText.setSelected(true);
        } else if (symbolizer instanceof RasterSymbolizer) {
            this.rbtRaster.setSelected(true);
        }
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getSymbolizerSelectionPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOkCancelPanel());
    }

    private JPanel getSymbolizerSelectionPanel() {
        if (this.symbolizerSelectionPanel == null) {
            this.symbolizerSelectionPanel = new JPanel(new GridBagLayout());
            this.instructionsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.choose-a-symbolizer-type")) + ": ");
            this.rbtPoint = new JRadioButtonWithIcon(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.point-symbolizer"), IconLoader.icon("point.gif"));
            this.rbtLine = new JRadioButtonWithIcon(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.line-symbolizer"), IconLoader.icon("lineal.gif"));
            this.rbtPolygon = new JRadioButtonWithIcon(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.polygon-symbolizer"), IconLoader.icon("polygon.gif"));
            this.rbtText = new JRadioButtonWithIcon(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.text-symbolizer"), IconLoader.icon("texto.png"));
            this.rbtRaster = new JRadioButtonWithIcon(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog.raster-symbolizer"), IconLoader.icon("raster.png"));
            ButtonGroup buttonGroup = new ButtonGroup();
            this.rbtLine.addToButtonGroup(buttonGroup);
            this.rbtPoint.addToButtonGroup(buttonGroup);
            this.rbtPolygon.addToButtonGroup(buttonGroup);
            this.rbtText.addToButtonGroup(buttonGroup);
            this.rbtRaster.addToButtonGroup(buttonGroup);
            FormUtils.addRowInGBL((JComponent)this.symbolizerSelectionPanel, 0, 0, (JComponent)this.instructionsLabel, true, true);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 1, 0, this.rbtPoint);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 1, 30, this.rbtPoint);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 2, 0, this.rbtLine);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 3, 0, this.rbtPolygon);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 3, 30, this.rbtPolygon);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 4, 0, this.rbtText);
            FormUtils.addRowInGBL(this.symbolizerSelectionPanel, 5, 0, this.rbtRaster);
        }
        return this.symbolizerSelectionPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    @Override
    public SymbolizerChooserDialog.SymbolizerType getSelectionCode() {
        if (this.rbtPoint.isSelected()) {
            return SymbolizerChooserDialog.SymbolizerType.POINT;
        }
        if (this.rbtLine.isSelected()) {
            return SymbolizerChooserDialog.SymbolizerType.LINE;
        }
        if (this.rbtPolygon.isSelected()) {
            return SymbolizerChooserDialog.SymbolizerType.POLYGON;
        }
        if (this.rbtText.isSelected()) {
            return SymbolizerChooserDialog.SymbolizerType.TEXT;
        }
        return SymbolizerChooserDialog.SymbolizerType.RASTER;
    }

    @Override
    public boolean exitOk() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            this.setVisible(false);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    @Override
    public Symbolizer getSelectedSymbolizer() {
        Symbolizer s = null;
        switch (this.getSelectionCode()) {
            case POINT: {
                s = styleBuilder.createPointSymbolizer();
                break;
            }
            case LINE: {
                s = styleBuilder.createLineSymbolizer();
                break;
            }
            case POLYGON: {
                s = styleBuilder.createPolygonSymbolizer();
                break;
            }
            case TEXT: {
                try {
                    s = styleBuilder.createTextSymbolizer(this.schema);
                }
                catch (IllegalFilterException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                break;
            }
            case RASTER: {
                s = styleBuilder.createRasterSymbolizer();
                break;
            }
            default: {
                Assert.fail((String)I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog.this-should-not-happen"));
            }
        }
        return s;
    }
}

