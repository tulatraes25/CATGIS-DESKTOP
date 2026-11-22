package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.swing.Icon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The primary toolbar icon set must load at the intended size and be complete. */
class AppIconsContractTest {

    @Test
    void primaryToolbarIconsLoadAt24Pixels() {
        Icon[] icons = {
                AppIcons.openIcon(), AppIcons.addLayerIcon(), AppIcons.saveIcon(),
                AppIcons.zoomInIcon(), AppIcons.zoomOutIcon(), AppIcons.zoomAllIcon(),
                AppIcons.panIcon(), AppIcons.identifyIcon(),
                AppIcons.pointIcon(), AppIcons.lineIcon(), AppIcons.polygonIcon(),
                AppIcons.catmapIcon(),
                AppIcons.osmIcon(), AppIcons.esriIcon(), AppIcons.wfsIcon(),
                AppIcons.demIcon(), AppIcons.exportIcon(), AppIcons.attrEditIcon()
        };
        for (Icon icon : icons) {
            assertNotNull(icon, "icono nulo en el set principal");
            assertEquals(24, icon.getIconWidth(), "ancho de icono inconsistente");
            assertEquals(24, icon.getIconHeight(), "alto de icono inconsistente");
        }
    }

    @Test
    void catmapHasItsOwnLayoutIcon() {
        Icon catmap = AppIcons.catmapIcon();
        assertNotNull(catmap);
        assertTrue(catmap.getIconWidth() > 0);
    }
}
