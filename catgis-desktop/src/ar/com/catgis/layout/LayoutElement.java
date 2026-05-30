package ar.com.catgis.layout;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface LayoutElement {

    String getId();
    String getName();
    void setName(String name);
    java.awt.geom.Rectangle2D.Double getBoundsMm();
    void setBoundsMm(double x, double y, double w, double h);
    int getZOrder();
    void setZOrder(int z);
    boolean isVisible();
    void setVisible(boolean v);
    boolean isLocked();
    void setLocked(boolean locked);
    boolean isSelected();
    void setSelected(boolean sel);

    void render(Graphics2D g2, LayoutRenderContext ctx);
    boolean containsMm(double xMm, double yMm);
}
