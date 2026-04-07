package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

public class TopographyToolbar extends JPanel {

    public TopographyToolbar() {
        setOpaque(true);
        setBackground(new Color(246, 251, 247));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 224, 214)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel title = new JLabel(I18n.t("Topografia"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        title.setForeground(new Color(41, 74, 48));

        JButton demButton = createButton(I18n.t("Imagen DEM"), I18n.t("Descargar o incorporar un DEM al proyecto"), AppIcons.imageryIcon());
        demButton.addActionListener(e -> OnlineDemDownloadDialog.open());

        JButton localDemButton = createButton(I18n.t("Cargar DEM"), I18n.t("Cargar datos DEM desde archivos locales"), AppIcons.openIcon());
        localDemButton.addActionListener(e -> DemLocalLoadAction.openDialog());

        JButton contourButton = createButton(I18n.t("Curvas de nivel"), I18n.t("Generar curvas de nivel desde un raster DEM"), AppIcons.lineIcon());
        contourButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            ContourGenerationDialog.open();
        });

        JButton profileButton = createButton(I18n.t("Perfil topografico"), I18n.t("Generar un perfil topografico desde un DEM"), AppIcons.distanceIcon());
        profileButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TopographicProfileDialog.open();
        });

        add(title);
        add(demButton);
        add(localDemButton);
        add(contourButton);
        add(profileButton);
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
