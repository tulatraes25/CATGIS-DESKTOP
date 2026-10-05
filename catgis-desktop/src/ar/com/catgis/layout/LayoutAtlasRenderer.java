package ar.com.catgis.layout;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders atlas pages using an export function.
 */
public final class LayoutAtlasRenderer {

    public interface PageRenderer {
        BufferedImage renderPage(String pageName, int pageIndex, LayoutRenderContext ctx);
    }

    public static int renderAllPages(LayoutAtlas atlas, PageRenderer renderer,
                                      LayoutRenderContext ctx, java.io.File outputDir,
                                      String baseFileName) throws Exception {
        if (!atlas.isEnabled() || atlas.getPageCount() == 0) return 0;
        int rendered = 0;
        for (int i = 0; i < atlas.getPageCount(); i++) {
            atlas.setCurrentPage(i);
            BufferedImage page = renderer.renderPage(atlas.getCurrentPageName(), i, ctx);
            if (page != null) {
                java.io.File out = new java.io.File(outputDir, baseFileName + "_" + (i + 1) + ".png");
                javax.imageio.ImageIO.write(page, "PNG", out);
                rendered++;
            }
        }
        return rendered;
    }

    private LayoutAtlasRenderer() {}
}
