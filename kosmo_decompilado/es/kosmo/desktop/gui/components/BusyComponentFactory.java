/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.divxdede.swing.busy.BusyIcon
 *  org.divxdede.swing.busy.BusyModel
 *  org.divxdede.swing.busy.DefaultBusyModel
 *  org.divxdede.swing.busy.JBusyComponent
 *  org.divxdede.swing.busy.icon.RadialBusyIcon
 *  org.divxdede.swing.busy.ui.BasicBusyLayerUI
 *  org.divxdede.swing.busy.ui.BusyLayerUI
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.images.DesktopIconLoader;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.divxdede.swing.busy.BusyIcon;
import org.divxdede.swing.busy.BusyModel;
import org.divxdede.swing.busy.DefaultBusyModel;
import org.divxdede.swing.busy.JBusyComponent;
import org.divxdede.swing.busy.icon.RadialBusyIcon;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
import org.divxdede.swing.busy.ui.BusyLayerUI;

public class BusyComponentFactory<C extends JComponent> {
    private static final Icon DEFAULT_ICON = DesktopIconLoader.icon("search64.png");

    public JBusyComponent<C> createBusyComponent(C view, String description) {
        return this.createBusyComponent(view, description, DEFAULT_ICON);
    }

    public JBusyComponent<C> createBusyComponent(C view, String description, Icon baseIcon) {
        RadialBusyIcon busyIcon = new RadialBusyIcon(baseIcon, new Insets(5, 5, 5, 5));
        BasicBusyLayerUI busyUI = new BasicBusyLayerUI();
        busyUI.setBusyIcon((BusyIcon)busyIcon);
        busyUI.setMillisToDecideToPopup(500);
        busyUI.setMillisToPopup(500);
        DefaultBusyModel model = new DefaultBusyModel();
        model.setDescription(description);
        JBusyComponent comp = new JBusyComponent(view, (BusyLayerUI)busyUI);
        comp.setBusyModel((BusyModel)model);
        return comp;
    }
}

