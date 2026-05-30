/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.print.wms;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import org.apache.log4j.Logger;
import org.saig.core.renderer.Renderer;
import org.saig.jump.widgets.print.actions.PrintOptions;
import org.saig.jump.widgets.print.util.PrintWaitDialog;

public class WMSPrintUtils {
    private static final Logger LOGGER = Logger.getLogger(WMSPrintUtils.class);
    private static Renderer staticRenderer = Renderer.getUniqueInstance();

    public static void printWMSLayer(WMSLayer wmsLayer, Rectangle rIntersecction, Envelope envelope, Graphics2D g) throws Exception {
        Rectangle wmsRectangle = new Rectangle(0, 0, (int)PrintWaitDialog.wmsWidthQuality, (int)PrintWaitDialog.wmsHeightQuality);
        Rectangle[] tiles = WMSPrintUtils.getTiles(wmsRectangle);
        int i = 0;
        while (i < tiles.length) {
            Rectangle view = tiles[i];
            WMSPrintUtils.drawPart(view.x, view.y, view.x + view.width, view.y + view.height, view.width, view.height, envelope, g, rIntersecction, wmsLayer, wmsRectangle);
            ++i;
        }
    }

    public static void printWMSLayer(WMSLayer wmsLayer, int width, int height, Rectangle rIntersecction, Envelope envelope, Graphics2D g) throws Exception {
        Rectangle wmsRectangle = new Rectangle(0, 0, width, height);
        Rectangle[] tiles = WMSPrintUtils.getTiles(wmsRectangle);
        int i = 0;
        while (i < tiles.length) {
            Rectangle view = tiles[i];
            WMSPrintUtils.drawPart(view.x, view.y, view.x + view.width, view.y + view.height, view.width, view.height, envelope, g, rIntersecction, wmsLayer, wmsRectangle);
            ++i;
        }
    }

    private static Rectangle[] getTiles(Rectangle r) {
        int tileMaxWidth = WMSPrintUtils.getMaxTile(r.width, 1000);
        int tileMaxHeight = WMSPrintUtils.getMaxTile(r.height, PrintOptions.wmsGrid);
        int numCols = 1 + r.width / tileMaxWidth;
        int numRows = 1 + r.height / tileMaxHeight;
        double[][] srcPts = new double[numCols * numRows][8];
        Rectangle[] tile = new Rectangle[numCols * numRows];
        int yProv = r.y;
        int stepY = 0;
        while (stepY < numRows) {
            int altoAux = (double)(yProv + tileMaxHeight) > r.getMaxY() ? (int)r.getMaxY() - yProv : tileMaxHeight;
            int xProv = r.x;
            int stepX = 0;
            while (stepX < numCols) {
                int anchoAux = (double)(xProv + tileMaxWidth) > r.getMaxX() ? (int)r.getMaxX() - xProv : tileMaxWidth;
                int tileCnt = stepY * numCols + stepX;
                srcPts[tileCnt][0] = xProv;
                srcPts[tileCnt][1] = yProv;
                srcPts[tileCnt][2] = xProv + anchoAux + 1;
                srcPts[tileCnt][3] = yProv;
                srcPts[tileCnt][4] = xProv + anchoAux + 1;
                srcPts[tileCnt][5] = yProv + altoAux + 1;
                srcPts[tileCnt][6] = xProv;
                srcPts[tileCnt][7] = yProv + altoAux + 1;
                tile[tileCnt] = new Rectangle(xProv, yProv, anchoAux + 1, altoAux + 1);
                LOGGER.debug((Object)("Tile " + tileCnt + "->" + xProv + "," + yProv + "," + (anchoAux + 1) + "," + (altoAux + 1)));
                xProv += tileMaxWidth;
                ++stepX;
            }
            yProv += tileMaxHeight;
            ++stepY;
        }
        LOGGER.debug((Object)("Tiles:" + tile.length));
        return tile;
    }

    private static int getMaxTile(int sizePx, int maxTile) {
        int nTiles = sizePx / maxTile;
        if (nTiles == 0) {
            return sizePx;
        }
        int resto = sizePx - nTiles * maxTile;
        if (resto >= 150) {
            return maxTile;
        }
        int dec = (150 - resto) / nTiles;
        return maxTile - dec;
    }

    private static void drawPart(int x, int y, int x1, int y1, int incX, int incY, Envelope envelope, Graphics2D outputGraphics, Rectangle vistaGraficaImpresion, WMSLayer wmsLayer, Rectangle wmsRectangle) throws Exception {
        Image partImage;
        Envelope newEnvelope = new Envelope(staticRenderer.pixelToWorld(x, y, envelope, wmsRectangle), staticRenderer.pixelToWorld(x1, y1, envelope, wmsRectangle));
        if (incX == 0 || incY == 0) {
            return;
        }
        try {
            partImage = wmsLayer.createMapRequest(newEnvelope, incX, incY).getImage();
        }
        catch (Exception e) {
            return;
        }
        double scaleX = vistaGraficaImpresion.getWidth() / wmsRectangle.getWidth();
        double scaleY = vistaGraficaImpresion.getHeight() / wmsRectangle.getHeight();
        int newX = (int)((double)x * scaleX + vistaGraficaImpresion.getX());
        int newY = (int)((double)y * scaleY + vistaGraficaImpresion.getY());
        int newX1 = (int)((double)x1 * scaleX + vistaGraficaImpresion.getX());
        int newY1 = (int)((double)y1 * scaleY + vistaGraficaImpresion.getY());
        outputGraphics.drawImage(partImage, newX, newY, newX1 - newX, newY1 - newY, null);
    }
}

