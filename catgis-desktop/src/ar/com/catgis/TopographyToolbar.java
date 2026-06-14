package ar.com.catgis;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopographyToolbar extends JPanel {

    public TopographyToolbar() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));

        add(flat("Imagen DEM", AppIcons.demIcon(), "Descargar o incorporar un DEM al proyecto", OnlineDemDownloadDialog::open));
        add(flat("Cargar DEM", AppIcons.openIcon(), "Cargar datos DEM desde archivos locales", DemLocalLoadAction::openDialog));
        add(flat("Suelos online", AppIcons.basemapIcon(), "Descargar mapas de suelos globales", OnlineSoilDownloadDialog::open));
        add(flat("Recortar DEM", AppIcons.clipIcon(), "Recortar un DEM por vista o mascara", () -> chkRaster(DemClipDialog::open)));
        add(flat("Curvas de nivel", AppIcons.contourIcon(), "Generar curvas de nivel desde un DEM", () -> chkRaster(ContourGenerationDialog::open)));
        add(flat("Escorrentias", AppIcons.drainageIcon(), "Calcular red de drenaje desde un DEM", () -> chkRaster(DrainageExtractionDialog::open)));
        add(flat("Analisis hidro", AppIcons.hydrologyIcon(), "Hillshade, pendiente, aspecto, flujo, cuencas", () -> chkRaster(TerrainHydrologyAnalysisDialog::open)));
        add(flat("Cuenca outlet", AppIcons.pointIcon(), "Delimitar una cuenca desde un outlet", () -> chkRaster(BasinFromOutletDialog::open)));
        add(flat("Inundacion", AppIcons.floodIcon(), "Escenario preliminar de anegamiento", () -> chkRaster(FloodScenarioDialog::open)));
        add(flat("Riesgo booleano", AppIcons.riskIcon(), "Combinar DEM y suelos con reglas booleanas", () -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().size() < 2) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null, I18n.t("Necesitas al menos un DEM y un raster de suelos cargados.")); return;
            }
            BooleanRiskDialog.open();
        }));
        add(flat("Perfil topografico", AppIcons.profileIcon(), "Generar un perfil topografico desde un DEM", () -> chkRaster(TopographicProfileDialog::open)));
    }

    private void chkRaster(Runnable action) {
        if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
            TopographyWorkflowSupport.showNoRasterMessage();
            return;
        }
        action.run();
    }

    private JButton flat(String text, javax.swing.Icon icon, String tip, Runnable action) {
        return OnlineConnectionsToolbar.flatButton(text, icon, tip, action);
    }
}
