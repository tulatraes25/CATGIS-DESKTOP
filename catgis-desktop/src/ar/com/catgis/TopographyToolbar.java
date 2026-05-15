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

        JButton soilButton = createButton(I18n.t("Suelos online"), I18n.t("Descargar mapas de suelos globales y agregarlos al proyecto"), AppIcons.basemapIcon());
        soilButton.addActionListener(e -> OnlineSoilDownloadDialog.open());

        JButton clipDemButton = createButton(I18n.t("Recortar DEM"), I18n.t("Recortar un DEM por vista o mascara poligonal"), AppIcons.cutIcon());
        clipDemButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DemClipDialog.open();
        });

        JButton contourButton = createButton(I18n.t("Curvas de nivel"), I18n.t("Generar curvas de nivel desde un raster DEM"), AppIcons.lineIcon());
        contourButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            ContourGenerationDialog.open();
        });

        JButton drainageButton = createButton(I18n.t("Escorrentias"), I18n.t("Calcular escorrentias y red de drenaje desde un DEM"), AppIcons.drainageIcon());
        drainageButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DrainageExtractionDialog.open();
        });

        JButton terrainAnalysisButton = createButton(I18n.t("Analisis hidro"), I18n.t("Generar hillshade, pendiente, aspecto, flujo, cuencas y flechas desde un DEM"), AppIcons.terrainAnalysisIcon());
        terrainAnalysisButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JButton basinOutletButton = createButton(I18n.t("Cuenca outlet"), I18n.t("Delimitar una cuenca desde un outlet o pour point"), AppIcons.pointIcon());
        basinOutletButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            BasinFromOutletDialog.open();
        });

        JButton floodButton = createButton(I18n.t("Inundacion"), I18n.t("Generar un escenario preliminar de anegamiento por lluvia"), AppIcons.areaIcon());
        floodButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            FloodScenarioDialog.open();
        });

        JButton booleanRiskButton = createButton(I18n.t("Riesgo booleano"), I18n.t("Combinar DEM y suelos con reglas booleanas preliminares"), AppIcons.terrainAnalysisIcon());
        booleanRiskButton.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().size() < 2) {
                javax.swing.JOptionPane.showMessageDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        I18n.t("Necesitas al menos un DEM y un raster de suelos cargados para generar riesgo booleano preliminar.")
                );
                return;
            }
            BooleanRiskDialog.open();
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
        add(soilButton);
        add(clipDemButton);
        add(contourButton);
        add(drainageButton);
        add(terrainAnalysisButton);
        add(basinOutletButton);
        add(floodButton);
        add(booleanRiskButton);
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
