/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.rotate;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

public class NorthLayerViewPanelListener
implements LayerViewPanelListener {
    private static final Logger LOGGER = Logger.getLogger(NorthLayerViewPanelListener.class);
    private LayerViewPanel lvp;
    private Image north;
    private double px;
    private double py;
    private double angle;
    private String northPath;

    public NorthLayerViewPanelListener() {
    }

    public NorthLayerViewPanelListener(LayerViewPanel lvp, String northPath, double px, double py, double angle) {
        this.lvp = lvp;
        this.northPath = northPath;
        this.px = px;
        this.py = py;
        this.angle = angle;
        this.loadNorthImage();
    }

    private void loadNorthImage() {
        File northFile;
        if (this.northPath != null && (northFile = new File(this.northPath)).exists()) {
            try {
                this.north = ImageIO.read(northFile);
            }
            catch (IOException e) {
                LOGGER.info((Object)"", (Throwable)e);
            }
        }
    }

    public void setLayerViewPanel(LayerViewPanel lvp) {
        this.lvp = lvp;
    }

    @Override
    public void cursorPositionChanged(String x, String y) {
    }

    @Override
    public void painted(Graphics graphics) {
        if (this.north != null) {
            int x = (int)((double)this.lvp.getWidth() * this.px - (double)(this.north.getWidth(null) / 2));
            int y = (int)((double)this.lvp.getHeight() * this.py - (double)(this.north.getHeight(null) / 2));
            Graphics2D g = (Graphics2D)graphics;
            g.rotate(-this.angle, x, y);
            g.drawImage(this.north, x - this.north.getWidth(null) / 2, y - this.north.getWidth(null) / 2, null);
        }
    }

    @Override
    public void renderingFinished() {
    }

    @Override
    public void renderingStarted() {
    }

    @Override
    public void selectionChanged() {
    }

    public double getPx() {
        return this.px;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public double getPy() {
        return this.py;
    }

    public void setPy(double py) {
        this.py = py;
    }

    public double getAngle() {
        return this.angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public String getNorthPath() {
        return this.northPath;
    }

    public void setNorthPath(String northPath) {
        this.northPath = northPath;
        this.loadNorthImage();
    }

    public Image northImage() {
        return this.north;
    }
}

