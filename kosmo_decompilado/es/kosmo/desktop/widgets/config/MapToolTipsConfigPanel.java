/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.MapToolTipsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class MapToolTipsConfigPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final String NAME = MapToolTipsPlugIn.NAME;
    private Blackboard blackboard;
    private JCheckBox showAreaAndLengthAttributesCheckBox;

    public MapToolTipsConfigPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.config.MapToolTipsConfigPanel.Options")));
        this.showAreaAndLengthAttributesCheckBox = new JCheckBox(I18N.getString("es.kosmo.desktop.widgets.config.MapToolTipsConfigPanel.Show-feature-area-and-perimeter"));
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.showAreaAndLengthAttributesCheckBox);
        FormUtils.addRowInGBL(this, 0, 0, mainPanel);
        FormUtils.addFiller(this, 1, 0);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void init() {
        boolean showAreaAndLength = PersistentBlackboardPlugIn.get(this.blackboard).get(MapToolTipsPlugIn.SHOW_AREA_AND_LENGTH_KEY, true);
        this.showAreaAndLengthAttributesCheckBox.setSelected(showAreaAndLength);
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(MapToolTipsPlugIn.SHOW_AREA_AND_LENGTH_KEY, this.showAreaAndLengthAttributesCheckBox.isSelected());
    }
}

