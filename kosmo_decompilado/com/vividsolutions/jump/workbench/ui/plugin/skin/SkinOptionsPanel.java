/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin.skin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.TrackedPopupMenu;
import com.vividsolutions.jump.workbench.ui.plugin.skin.LookAndFeelProxy;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.saig.jump.lang.I18N;

public class SkinOptionsPanel
extends OptionsPanel {
    private static final String CURRENT_SKIN_KEY = SkinOptionsPanel.class + " - CURRENT SKIN";
    public static final String SKINS_KEY = SkinOptionsPanel.class + " - SKINS";
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JComboBox comboBox = new JComboBox();
    private JPanel fillerPanel = new JPanel();
    private JLabel label = new JLabel();
    private Blackboard blackboard;
    private Window window;
    private boolean modified;

    public SkinOptionsPanel(Blackboard blackboard, Window window) {
        this.window = window;
        this.blackboard = blackboard;
        try {
            this.jbInit();
            this.comboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SkinOptionsPanel.this.modified = true;
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(this.gridBagLayout1);
        this.label.setText(String.valueOf(I18N.getString("workbench.ui.plugin.skin.SkinOptionsPanel.skin")) + ":");
        this.add((Component)this.comboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(10, 0, 10, 10), 0, 0));
        this.add((Component)this.fillerPanel, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(10, 10, 10, 4), 0, 0));
    }

    @Override
    public void init() {
        this.modified = false;
        DefaultComboBoxModel<LookAndFeelProxy> model = new DefaultComboBoxModel<LookAndFeelProxy>();
        for (LookAndFeelProxy proxy : (Collection)this.blackboard.get(SKINS_KEY)) {
            model.addElement(proxy);
        }
        this.comboBox.setModel(model);
        this.comboBox.setSelectedItem(this.blackboard.get(CURRENT_SKIN_KEY, this.comboBox.getModel().getElementAt(0)));
    }

    @Override
    public void okPressed() {
        if (!this.modified) {
            return;
        }
        this.blackboard.put(CURRENT_SKIN_KEY, this.comboBox.getSelectedItem());
        try {
            UIManager.setLookAndFeel(((LookAndFeelProxy)this.comboBox.getSelectedItem()).getLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException e) {
            Assert.shouldNeverReachHere((String)e.toString());
        }
        this.updateFrames();
        this.updatePopupMenus();
    }

    private void updatePopupMenus() {
        for (JPopupMenu menu : TrackedPopupMenu.trackedPopupMenus()) {
            SwingUtilities.updateComponentTreeUI(menu);
        }
    }

    private void updateFrames() {
        Frame[] frames = Frame.getFrames();
        int i = 0;
        while (i < frames.length) {
            SwingUtilities.updateComponentTreeUI(frames[i]);
            Window[] windows = frames[i].getOwnedWindows();
            int j = 0;
            while (j < windows.length) {
                this.updateWindow(windows[j]);
                ++j;
            }
            ++i;
        }
    }

    private void updateWindow(Window w) {
        SwingUtilities.updateComponentTreeUI(w);
        Window[] children = w.getOwnedWindows();
        int i = 0;
        while (i < children.length) {
            this.updateWindow(children[i]);
            ++i;
        }
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}

