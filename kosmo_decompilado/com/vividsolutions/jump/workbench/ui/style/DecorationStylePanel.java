/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.saig.jump.lang.I18N;

public class DecorationStylePanel
extends JPanel
implements StylePanel {
    private static final long serialVersionUID = 1L;
    private AddRemovePanel<ListItem> addRemovePanel = new AddRemovePanel(false);
    private BorderLayout borderLayout1 = new BorderLayout();
    private Layer layer;
    private Collection<Class<?>> styleClasses;

    public DecorationStylePanel(Layer layer, Collection<Class<?>> styleClasses) {
        try {
            this.layer = layer;
            this.styleClasses = styleClasses;
            ((DefaultAddRemoveListModel)this.addRemovePanel.getLeftList().getModel()).setSorted(true);
            ((DefaultAddRemoveListModel)this.addRemovePanel.getRightList().getModel()).setSorted(true);
            this.populateAddRemovePanel(layer, styleClasses);
            this.setUpRenderer(((DefaultAddRemoveList)this.addRemovePanel.getLeftList()).getList());
            this.setUpRenderer(((DefaultAddRemoveList)this.addRemovePanel.getRightList()).getList());
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return I18N.getString("workbench.ui.style.DecorationStylePanel.title");
    }

    private void setUpRenderer(JList list) {
        list.setCellRenderer(new ListCellRenderer(){
            private DefaultListCellRenderer baseRenderer = new DefaultListCellRenderer();

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                ListItem item = (ListItem)value;
                JLabel component = (JLabel)this.baseRenderer.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
                component.setText(item.style.getName());
                component.setIcon(item.style.getIcon());
                return component;
            }
        });
    }

    private void clearStyles(Layer layer) {
        for (Class<?> styleClass : this.styleClasses) {
            Style style = layer.getStyle(styleClass);
            if (style == null) continue;
            layer.removeStyle(style);
        }
    }

    @Override
    public Style updateStyles() {
        this.clearStyles(this.layer);
        Style style = null;
        for (ListItem item : this.addRemovePanel.getRightItems()) {
            style = item.style;
            this.layer.addStyle(style);
        }
        return style;
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.addRemovePanel.setLeftText(I18N.getString("workbench.ui.style.DecorationStylePanel.available"));
        this.addRemovePanel.setRightText(I18N.getString("workbench.ui.style.DecorationStylePanel.in-use"));
        this.add(this.addRemovePanel, "Center");
    }

    private void populateAddRemovePanel(Layer layer, Collection<Class<?>> styleClasses) {
        ArrayList<ListItem> availableItems = new ArrayList<ListItem>();
        ArrayList<ListItem> inUseItems = new ArrayList<ListItem>();
        for (Class<?> styleClass : styleClasses) {
            Style inUseStyle = layer.getStyle(styleClass);
            if (inUseStyle != null) {
                inUseItems.add(new ListItem(inUseStyle));
                continue;
            }
            try {
                availableItems.add(new ListItem((Style)styleClass.newInstance()));
            }
            catch (IllegalAccessException e) {
                Assert.shouldNeverReachHere();
            }
            catch (InstantiationException e) {
                Assert.shouldNeverReachHere();
            }
        }
        this.addRemovePanel.getLeftList().getModel().setItems(availableItems);
        this.addRemovePanel.getRightList().getModel().setItems(inUseItems);
    }

    @Override
    public String validateInput() {
        return null;
    }

    private class ListItem
    implements Comparable<ListItem> {
        public Style style;

        public ListItem(Style style) {
            this.style = style;
        }

        @Override
        public int compareTo(ListItem o) {
            return this.toString().compareTo(o.toString());
        }

        public String toString() {
            return this.style.getName();
        }
    }
}

