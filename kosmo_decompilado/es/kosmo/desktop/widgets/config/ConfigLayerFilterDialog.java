/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.config;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.desktop.plugins.config.ConfigLayerFilterPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.filter.Filter;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.FilterConfigPanel;

public class ConfigLayerFilterDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private boolean ok;
    private FilterConfigPanel filterPanel;

    public ConfigLayerFilterDialog(JFrame parent, boolean modal, Layer layer) {
        super((Frame)parent, modal);
        this.setTitle(String.valueOf(ConfigLayerFilterPlugIn.NAME) + " - " + I18N.getMessage("es.kosmo.desktop.widgets.config.ConfigLayerFilterDialog.Layer-{0}", new Object[]{layer.getTitle()}));
        this.setContentPane(this.getMainPanel(layer));
        this.setMinimumSize(new Dimension(300, 200));
        this.setPreferredSize(new Dimension(300, 200));
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel(Layer layer) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.filterPanel = new FilterConfigPanel(layer.getName(), true, layer.getLayerFilter(), true);
        mainPanel.add((Component)this.filterPanel, "Center");
        mainPanel.add((Component)this.getOKCancelPanel(), "South");
        return mainPanel;
    }

    private OKCancelPanel getOKCancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                ConfigLayerFilterDialog.this.ok = okCancelPanel.wasOKPressed();
                ConfigLayerFilterDialog.this.setVisible(false);
                ConfigLayerFilterDialog.this.dispose();
            }
        });
        return okCancelPanel;
    }

    public Filter getFilter() {
        return this.filterPanel.getFilter();
    }

    public boolean isOk() {
        return this.ok;
    }
}

