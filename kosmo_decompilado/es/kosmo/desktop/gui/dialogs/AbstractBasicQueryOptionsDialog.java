/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.dialogs;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public abstract class AbstractBasicQueryOptionsDialog
extends AbstractOptionsDialog {
    private static final long serialVersionUID = 1L;
    protected JQueryChooserPanel queryChooser;

    public AbstractBasicQueryOptionsDialog(JFrame parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super(parent, modal, toolName, toolDescription, toolImagePath);
    }

    @Override
    protected JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        this.queryChooser = new JQueryChooserPanel(I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Results-layer"), I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Save-results-layer"), false);
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.queryChooser);
        FormUtils.addFiller(centerPanel, 2, 0);
        return centerPanel;
    }

    @Override
    public boolean isInputValid() {
        return this.queryChooser.isInputValid();
    }

    public DataSourceQuery getResultsQuery() {
        return this.queryChooser.getDataSourceQuery();
    }
}

