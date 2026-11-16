package ar.com.catgis.layout;

import ar.com.catgis.MapPanel;
import ar.com.catgis.core.model.Project;

/**
 * Host contract for CATMAP layout rendering.
 *
 * <p>Layout elements must not reach into the global {@code AppContext}: the
 * application that embeds the layout (CATGIS or the standalone CATMAP shell)
 * supplies the project and the live map. The headless case may return
 * {@code null} for both.</p>
 */
public interface LayoutRenderHost {

    /** Project being composed; {@code null} when there is no project loaded. */
    Project project();

    /**
     * Live map panel used for extent capture and raster render; {@code null}
     * when running headless (exports then use the static vector renderer).
     */
    MapPanel mapPanel();
}
