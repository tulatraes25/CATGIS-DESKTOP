package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.ProRasterDerivedService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

public class ProInterpretationToolbar extends JPanel {

    private final JLabel contextLabel;
    private final JButton thematicButton;
    private final JButton qaButton;
    private final JButton cloudsButton;
    private final JButton shadowButton;
    private final JButton snowButton;
    private final JButton waterButton;
    private final JButton compareButton;

    public ProInterpretationToolbar() {
        setOpaque(true);
        setBackground(new Color(245, 249, 255));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 218, 235)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel title = new JLabel("Interpretacion Pro");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        title.setForeground(new Color(34, 68, 112));

        contextLabel = new JLabel("Selecciona un raster Pro Landsat o satelital");
        contextLabel.setFont(contextLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        contextLabel.setForeground(new Color(74, 91, 116));
        contextLabel.setPreferredSize(new Dimension(280, 22));

        thematicButton = createButton("Mapa Pro", "Generar mapa tematico Pro desde la capa raster seleccionada.", AppIcons.imageryIcon());
        thematicButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedProThematic();
            }
        });

        qaButton = createButton("QA Pro", "Generar QA preliminar desde la capa raster Pro seleccionada.", AppIcons.propertiesIcon());
        qaButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedProQa();
            }
        });

        cloudsButton = createButton("Nubes", "Generar mascara Landsat QA_PIXEL para nubes y cirrus.", AppIcons.propertiesIcon());
        cloudsButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS);
            }
        });

        shadowButton = createButton("Sombra", "Generar mascara Landsat QA_PIXEL para sombra de nube.", AppIcons.propertiesIcon());
        shadowButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW);
            }
        });

        snowButton = createButton("Nieve", "Generar mascara Landsat QA_PIXEL para nieve o hielo.", AppIcons.propertiesIcon());
        snowButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW);
            }
        });

        waterButton = createButton("Agua", "Generar mascara Landsat QA_PIXEL para agua.", AppIcons.propertiesIcon());
        waterButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER);
            }
        });

        compareButton = createButton("Comparar", "Comparar la capa Pro seleccionada con otra fecha compatible del proyecto.", AppIcons.attrRefreshIcon());
        compareButton.addActionListener(e -> {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.runSelectedProComparison();
            }
        });

        add(title);
        add(contextLabel);
        add(thematicButton);
        add(qaButton);
        add(cloudsButton);
        add(shadowButton);
        add(snowButton);
        add(waterButton);
        add(compareButton);

        refreshState();
    }

    public void refreshState() {
        LayersPanel layersPanel = CatgisDesktopApp.layersPanel;
        if (layersPanel == null) {
            contextLabel.setText("Selecciona un raster Pro Landsat o satelital");
            thematicButton.setEnabled(false);
            qaButton.setEnabled(false);
            cloudsButton.setEnabled(false);
            shadowButton.setEnabled(false);
            snowButton.setEnabled(false);
            waterButton.setEnabled(false);
            compareButton.setEnabled(false);
            return;
        }

        contextLabel.setText(layersPanel.describeSelectedProInterpretationContext());
        thematicButton.setEnabled(layersPanel.canRunSelectedProThematic());
        qaButton.setEnabled(layersPanel.canRunSelectedProQa());
        cloudsButton.setEnabled(layersPanel.canRunSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS));
        shadowButton.setEnabled(layersPanel.canRunSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW));
        snowButton.setEnabled(layersPanel.canRunSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW));
        waterButton.setEnabled(layersPanel.canRunSelectedLandsatQaMask(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER));
        compareButton.setEnabled(layersPanel.canRunSelectedProComparison());
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
