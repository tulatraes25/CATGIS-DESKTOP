package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

public class OnlineConnectionsToolbar extends JPanel {

    public OnlineConnectionsToolbar() {
        setOpaque(true);
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 228, 236)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel title = new JLabel(I18n.t("Conexiones online"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        title.setForeground(new Color(41, 54, 75));

        JButton osmButton = createButton("OSM", I18n.t("Activar OpenStreetMap"), AppIcons.basemapIcon());
        osmButton.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM));

        JButton esriButton = createButton("Esri", I18n.t("Activar Esri World Imagery"), AppIcons.imageryIcon());
        esriButton.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY));

        JButton mapsButton = createButton(I18n.t("Mapas..."), I18n.t("Elegir mapa base online"), AppIcons.basemapIcon());
        mapsButton.addActionListener(e -> OnlineBaseMapAction.openDialog());

        JButton wmsButton = createButton("WMS", I18n.t("Agregar servicio WMS"), AppIcons.wmsIcon());
        wmsButton.addActionListener(e -> AddWmsAction.openDialog());

        add(title);
        add(osmButton);
        add(esriButton);
        add(mapsButton);
        add(wmsButton);
    }

    private JButton createButton(String text, String tooltip, javax.swing.Icon icon) {
        JButton button = new JButton(text, icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(4, 8, 4, 8));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
        return button;
    }
}
