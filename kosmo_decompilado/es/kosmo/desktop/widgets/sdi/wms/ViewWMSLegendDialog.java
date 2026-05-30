/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.sdi.wms;

import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.MapStyle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class ViewWMSLegendDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ViewWMSLegendDialog.class);
    private WMSLayer layer;

    public ViewWMSLegendDialog(JFrame parent, boolean modal, WMSLayer layer) {
        super((Frame)parent, modal);
        this.layer = layer;
        try {
            this.setTitle(I18N.getMessage("org.saig.jump.widgets.wms.ViewWMSLegendDialog.WMS-service-legend-{0}", new Object[]{layer.getService().getCapabilities().getTitle()}));
        }
        catch (IOException e1) {
            LOGGER.error((Object)e1);
        }
        try {
            this.getContentPane().add(this.getLegendPanel());
        }
        catch (IOException e) {
            LOGGER.error((Object)e);
        }
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JScrollPane getLegendPanel() throws IOException {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, 1));
        JScrollPane scrollPane = new JScrollPane(mainPanel, 22, 32);
        List<String> names = this.layer.getLayerNames();
        for (String layerName : names) {
            MapLayer mapLayer = this.layer.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
            MapStyle style = mapLayer.getSelectedStyle();
            if (style == null || style.getLegendIcon() == null) continue;
            String layerTitle = StringUtils.isNotEmpty((String)mapLayer.getTitle()) ? mapLayer.getTitle() : layerName;
            JLabel nameLabel = new JLabel(I18N.getMessage("org.saig.jump.widgets.wms.ViewWMSLegendDialog.Layer-{0}", new Object[]{layerTitle}));
            nameLabel.setHorizontalAlignment(0);
            Font labelFont = nameLabel.getFont().deriveFont(1);
            nameLabel.setFont(labelFont);
            mainPanel.add(nameLabel);
            JLabel labelIcon = new JLabel(style.getLegendIcon(), 0);
            mainPanel.add(labelIcon);
        }
        scrollPane.setSize(new Dimension(300, 400));
        scrollPane.setMinimumSize(new Dimension(300, 400));
        scrollPane.setPreferredSize(new Dimension(300, 400));
        return scrollPane;
    }
}

