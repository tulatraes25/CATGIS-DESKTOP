package ar.com.catgis.layout;

import ar.com.catgis.AppContext;
import ar.com.catgis.MapPanel;
import ar.com.catgis.core.model.Project;

import java.util.Objects;

/**
 * Installs the active {@link LayoutRenderHost}.
 *
 * <p>The default adapter delegates to the legacy CATGIS {@link AppContext}
 * statics, so migrating a call site to this abstraction cannot change
 * behaviour. Tests and the standalone shell may install a different host and
 * must restore the default with {@link #reset()}.</p>
 */
public final class LayoutRenderHosts {

    private static final LayoutRenderHost CATGIS_HOST = new CatgisRenderHost();
    private static volatile LayoutRenderHost current = CATGIS_HOST;

    private LayoutRenderHosts() {}

    public static LayoutRenderHost get() {
        return current;
    }

    public static void install(LayoutRenderHost host) {
        current = Objects.requireNonNull(host, "CATMAP layout render host");
    }

    public static void reset() {
        current = CATGIS_HOST;
    }

    private static final class CatgisRenderHost implements LayoutRenderHost {
        @Override
        public Project project() {
            return AppContext.project();
        }

        @Override
        public MapPanel mapPanel() {
            return AppContext.mapPanel();
        }
    }
}
