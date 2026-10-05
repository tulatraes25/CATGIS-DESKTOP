package ar.com.catgis;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OnlineConnectionsToolbar extends JPanel {

    public OnlineConnectionsToolbar() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));

        add(flatButton("OSM", AppIcons.osmIcon(), I18n.t("Activar OpenStreetMap"), () -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM)));
        add(flatButton("Esri", AppIcons.esriIcon(), I18n.t("Activar Esri World Imagery"), () -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY)));
        add(flatButton("WFS", AppIcons.wfsIcon(), I18n.t("Agregar capa WFS"), AddWfsAction::openDialog));
    }

    static JButton flatButton(String text, javax.swing.Icon icon, String tip, Runnable action) {
        JButton btn = new JButton(text, icon);
        btn.setToolTipText(tip);
        btn.setFocusable(false);
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setOpaque(true); btn.setBackground(new Color(0xE0E0E0)); }
            public void mouseExited(MouseEvent e) { btn.setOpaque(false); btn.repaint(); }
        });
        return btn;
    }
}
