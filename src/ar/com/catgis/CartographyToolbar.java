package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

public class CartographyToolbar extends JPanel {

    public CartographyToolbar() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(true);
        setBackground(new Color(246, 249, 255));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 214, 235)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        add(buildInfoPanel(), BorderLayout.WEST);
        add(buildButtonsPanel(), BorderLayout.CENTER);
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(I18n.t("CATMAP"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(24, 40, 72));

        JLabel subtitle = new JLabel(I18n.t("Composicion, impresion y salida cartografica"));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitle.setForeground(new Color(88, 98, 112));

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JButton compositorButton = createButton(I18n.t("Abrir CATMAP"), AppIcons.projectIcon());
        compositorButton.addActionListener(e -> MapLayoutComposerDialog.open());

        JButton osmButton = createIconButton(I18n.t("Activar OpenStreetMap"), AppIcons.basemapIcon());
        osmButton.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM));

        JButton esriImageryButton = createIconButton(I18n.t("Activar Esri World Imagery"), AppIcons.imageryIcon());
        esriImageryButton.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY));

        JButton esriTopoButton = createIconButton(I18n.t("Activar Esri World Topo"), AppIcons.basemapIcon());
        esriTopoButton.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_TOPO));

        JButton mapsButton = createIconButton(I18n.t("Elegir mapa base online"), AppIcons.openIcon());
        mapsButton.addActionListener(e -> OnlineBaseMapAction.openDialog());

        JButton wmsButton = createIconButton(I18n.t("Agregar servicio WMS"), AppIcons.wmsIcon());
        wmsButton.addActionListener(e -> AddWmsAction.openDialog());

        panel.add(compositorButton);
        panel.add(Box.createHorizontalStrut(6));
        panel.add(osmButton);
        panel.add(esriImageryButton);
        panel.add(esriTopoButton);
        panel.add(mapsButton);
        panel.add(wmsButton);
        return panel;
    }

    private JButton createButton(String text, javax.swing.Icon icon) {
        JButton button = new JButton(text, icon);
        button.setFocusable(false);
        button.setMargin(new Insets(6, 10, 6, 10));
        button.setPreferredSize(new Dimension(Math.max(190, button.getPreferredSize().width), 34));
        return button;
    }

    private JButton createIconButton(String tooltip, javax.swing.Icon icon) {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(6, 6, 6, 6));
        button.setPreferredSize(new Dimension(34, 34));
        return button;
    }
}
