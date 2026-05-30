/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.wms;

import com.vividsolutions.wms.MapLayer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

public class MapStyle {
    private static final Logger LOGGER = Logger.getLogger(MapStyle.class);
    private String name;
    private String title;
    private String urlLegend;
    private String formatLegend;
    private boolean selected;
    private Icon legendIcon;
    private MapLayer layer;
    private boolean loadedIcon;

    public MapStyle(String name, String title, String urlLegend, String formatLegend) {
        this.name = name;
        this.title = title;
        this.setUrlLegend(urlLegend);
        this.formatLegend = formatLegend;
        this.selected = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlLegend() {
        return this.urlLegend;
    }

    public void setUrlLegend(String newURLLegend) {
        this.urlLegend = newURLLegend;
    }

    public String getFormatLegend() {
        return this.formatLegend;
    }

    public void setFormatLegend(String formatLegend) {
        this.formatLegend = formatLegend;
    }

    public String toString() {
        return this.name;
    }

    public Icon getLegendIcon() {
        if (!this.loadedIcon) {
            this.loadIconFromLegendURL();
        }
        return this.legendIcon;
    }

    private void loadIconFromLegendURL() {
        URL selectedUrl = null;
        try {
            selectedUrl = new URL(this.urlLegend);
        }
        catch (MalformedURLException e) {
            LOGGER.error((Object)e.getMessage());
        }
        if (selectedUrl != null) {
            try {
                BufferedImage image = ImageIO.read(selectedUrl);
                this.legendIcon = new ImageIcon(image);
                this.loadedIcon = true;
            }
            catch (IOException e) {
                LOGGER.error((Object)e.getMessage());
            }
        } else {
            this.loadedIcon = false;
        }
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected, boolean check) {
        if (check) {
            if (this.selected && !selected && this.layer.getStyles().size() == 1) {
                return;
            }
            if (this.selected && !selected) {
                for (MapStyle element : this.layer.getStyles()) {
                    if (element.equals(this)) continue;
                    element.setSelected(true, false);
                }
            }
        }
        this.selected = selected;
    }

    public void fireStyleChanged() {
        this.layer.setSelectedStyle(this);
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MapStyle)) {
            return false;
        }
        return this.getName().equals(((MapStyle)other).getName());
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }
}

