/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public class CADToolsOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private JLabel bufferRatio = new JLabel(I18N.getMessage("org.saig.jump.widgets.config.ExtendLineOptions.Tolerance-radius-{0}", new Object[]{" (m):"}));
    private NumberSpinner ratioSpinner;
    private JCheckBox unionCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.config.ExtendLineOptions.Break-lines-create-new-vertices-in-crosses"));
    public static final String EXTEND_LINE_BUFFER_KEY = String.valueOf(CADToolsOptionsPanel.class.getName()) + " - EXTEND LINE BUFFER";
    public static final String EXTEND_LINE_UNION_KEY = String.valueOf(CADToolsOptionsPanel.class.getName()) + " - EXTEND LINE UNION";
    private JPanel extentShortenOptionsPanel;
    private JCheckBox preguntarAntesDeCortarCheckBox;
    private JPanel moveOptionsPanel;

    public CADToolsOptionsPanel() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getExtentShortenOptionsPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    public JPanel getExtentShortenOptionsPanel() {
        if (this.extentShortenOptionsPanel == null) {
            this.extentShortenOptionsPanel = new JPanel(new GridBagLayout());
            this.extentShortenOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ExtendLineOptions.extent-shorten-line")));
            this.ratioSpinner = new NumberSpinner(0.0, 0.0, Double.MAX_VALUE, 0.1);
            this.ratioSpinner.setPreferredSize(new Dimension(80, this.ratioSpinner.getPreferredSize().height));
            FormUtils.addRowInGBL((JComponent)this.extentShortenOptionsPanel, 0, 0, this.bufferRatio, (JComponent)this.ratioSpinner);
            FormUtils.addRowInGBL(this.extentShortenOptionsPanel, 1, 0, this.unionCheckbox);
        }
        return this.extentShortenOptionsPanel;
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(EXTEND_LINE_BUFFER_KEY, this.ratioSpinner.getDoubleValue());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(EXTEND_LINE_UNION_KEY, this.unionCheckbox.isSelected());
    }

    @Override
    public void init() {
        this.ratioSpinner.setValue(CADToolsOptionsPanel.getExtendShortLineBuffer());
        this.unionCheckbox.setSelected(CADToolsOptionsPanel.getExtendShortLineUnion());
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return I18N.getString("org.saig.jump.widgets.config.CADToolsOptionsPanel.CAD");
    }

    public static Double getExtendShortLineBuffer() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(EXTEND_LINE_BUFFER_KEY, 1000.0);
    }

    public static boolean getExtendShortLineUnion() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(EXTEND_LINE_UNION_KEY, true);
    }
}

