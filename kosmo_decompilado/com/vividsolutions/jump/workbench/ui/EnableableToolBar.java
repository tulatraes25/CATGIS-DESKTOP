/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import es.kosmo.desktop.gui.layouts.WrapLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToolBar;

public class EnableableToolBar
extends JToolBar {
    private static final long serialVersionUID = 1L;
    protected Map<AbstractButton, EnableCheck> buttonToEnableCheckMap = new HashMap<AbstractButton, EnableCheck>();

    public EnableableToolBar() {
        this.setLayout(new WrapLayout(0, 0, 0));
    }

    public EnableCheck getEnableCheck(AbstractButton button) {
        return this.buttonToEnableCheckMap.get(button);
    }

    public void setEnableCheck(AbstractButton button, EnableCheck check) {
        this.buttonToEnableCheckMap.put(button, check);
    }

    public void updateEnabledState() {
        Iterator<AbstractButton> itButtons = this.buttonToEnableCheckMap.keySet().iterator();
        while (itButtons.hasNext()) {
            AbstractButton component;
            EnableCheck enableCheck = this.buttonToEnableCheckMap.get(component = itButtons.next());
            String error = enableCheck.check(component);
            component.setEnabled(error == null);
            if (error != null) {
                component.setToolTipText("<HTML><B>" + component.getName() + "</B><BR>" + error + "</HTML>");
                continue;
            }
            component.setToolTipText("<HTML><B>" + component.getName() + "</B></HTML>");
        }
    }

    @Override
    public void addSeparator() {
        JToolBar.Separator separator = new JToolBar.Separator();
        separator.setPreferredSize(new Dimension(10, 20));
        this.add(separator);
    }

    @Override
    public void addSeparator(Dimension size) {
        JToolBar.Separator separator = new JToolBar.Separator(size);
        separator.setPreferredSize(size);
        this.add(separator);
    }

    public void addSpacer() {
        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(5, 5));
        filler.setMinimumSize(new Dimension(5, 5));
        filler.setMaximumSize(new Dimension(5, 5));
        this.add(filler);
    }

    public void add(AbstractButton button, EnableCheck enableCheck) {
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.buttonToEnableCheckMap.put(button, trueCheck);
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EnableableToolBar.this.updateEnabledState();
            }
        });
        this.add(button);
    }

    public void add(AbstractButton button, String tooltip, Icon icon, ActionListener actionListener, EnableCheck enableCheck) {
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.buttonToEnableCheckMap.put(button, trueCheck);
        if (icon != null) {
            button.setIcon(icon);
        } else {
            button.setText(tooltip);
        }
        button.setToolTipText(tooltip);
        button.setName(tooltip);
        button.addActionListener(actionListener);
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EnableableToolBar.this.updateEnabledState();
            }
        });
        if (icon != null) {
            this.add(button);
        } else {
            this.add(button);
        }
    }

    public void remove(AbstractButton button) {
        this.buttonToEnableCheckMap.remove(button);
        this.remove((Component)button);
        this.repaint();
    }
}

