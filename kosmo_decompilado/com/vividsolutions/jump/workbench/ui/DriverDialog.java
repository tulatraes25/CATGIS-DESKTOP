/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.driver.AbstractDriver;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.DriverPanelCache;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class DriverDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    JPanel centrePanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    private List<AbstractDriver> drivers;
    private AbstractDriverPanel dummyDriverPanel;
    protected AbstractDriverPanel currentDriverPanel = this.dummyDriverPanel = new AbstractDriverPanel(){
        private static final long serialVersionUID = 1L;

        @Override
        public boolean wasOKPressed() {
            return false;
        }

        @Override
        public String getValidationError() {
            return null;
        }

        @Override
        public void addActionListener(ActionListener l) {
        }

        @Override
        public void removeActionListener(ActionListener l) {
        }
    };
    JPanel northPanel = new JPanel();
    JPanel innerNorthPanel = new JPanel();
    JLabel driverLabel = new JLabel();
    JComboBox driverComboBox = new JComboBox();
    FlowLayout flowLayout1 = new FlowLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    private boolean closeInitiatedByPanelButton;
    private boolean okPressed;
    private WeakHashMap<Layer, DriverPanelCache> layerToDriverPanelCacheMap = new WeakHashMap();
    private Layer layer;

    public DriverDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DriverDialog() {
        this((Frame)null, "", false);
    }

    public void initialize(List<AbstractDriver> drivers) {
        this.drivers = drivers;
    }

    @Override
    public void show() {
        this.driverComboBox.removeAllItems();
        for (AbstractDriver driver : this.drivers) {
            this.driverComboBox.addItem(driver);
            this.centrePanel.setPreferredSize(this.merge(this.centrePanel.getPreferredSize(), driver.getPanel().getPreferredSize()));
        }
        this.pack();
        super.show();
    }

    private Dimension merge(Dimension envelopeA, Dimension envelopeB) {
        return new Dimension((int)Math.max(envelopeA.getWidth(), envelopeB.getWidth()), (int)Math.max(envelopeA.getHeight(), envelopeB.getHeight()));
    }

    void jbInit() throws Exception {
        this.centrePanel.setLayout(this.borderLayout1);
        this.getContentPane().setLayout(this.borderLayout2);
        this.innerNorthPanel.setLayout(this.flowLayout1);
        this.driverLabel.setText(I18N.getString("workbench.ui.DriverDialog.format"));
        this.driverComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                DriverDialog.this.driverComboBox_itemStateChanged(e);
            }
        });
        this.northPanel.setLayout(this.gridBagLayout1);
        this.northPanel.setBorder(BorderFactory.createEtchedBorder());
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                DriverDialog.this.this_componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                DriverDialog.this.this_componentHidden(e);
            }
        });
        this.getContentPane().add((Component)this.centrePanel, "Center");
        this.getContentPane().add((Component)this.northPanel, "North");
        this.northPanel.add((Component)this.innerNorthPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.innerNorthPanel.add((Component)this.driverLabel, null);
        this.innerNorthPanel.add((Component)this.driverComboBox, null);
        this.centrePanel.add((Component)this.currentDriverPanel, "Center");
    }

    public AbstractDriver getCurrentDriver() {
        return (AbstractDriver)this.driverComboBox.getSelectedItem();
    }

    public boolean wasOKPressed() {
        if (!this.closeInitiatedByPanelButton) {
            return false;
        }
        return this.okPressed;
    }

    void driverComboBox_itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != 1) {
            return;
        }
        this.updateCentrePanel(this.getCurrentDriver().getPanel());
    }

    private void updateCentrePanel(AbstractDriverPanel newDriverPanel) {
        this.currentDriverPanel.removeActionListener(this);
        this.centrePanel.remove(this.currentDriverPanel);
        this.currentDriverPanel = newDriverPanel;
        this.centrePanel.add((Component)this.currentDriverPanel, "Center");
        this.currentDriverPanel.addActionListener(this);
        this.validateTree();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.okPressed = this.currentDriverPanel.wasOKPressed();
        if (!this.currentDriverPanel.wasOKPressed() || this.currentDriverPanel.isInputValid()) {
            this.closeInitiatedByPanelButton = true;
            this.setVisible(false);
            if (this.currentDriverPanel.wasOKPressed()) {
                this.updateDriverPanelCache();
            }
            return;
        }
        this.reportValidationError(this.currentDriverPanel.getValidationError());
    }

    private void reportValidationError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, I18N.getString("workbench.ui.DriverDialog.error"), 0);
    }

    void this_componentShown(ComponentEvent e) {
        this.closeInitiatedByPanelButton = false;
        AbstractDriver cachedDriver = (AbstractDriver)this.driverPanelCache(this.layer).get("DRIVER");
        if (cachedDriver != null) {
            this.driverComboBox.setSelectedItem(cachedDriver);
        }
        this.applyDriverPanelCache();
        this.updateCentrePanel(this.getCurrentDriver().getPanel());
    }

    void this_componentHidden(ComponentEvent e) {
        this.updateCentrePanel(this.dummyDriverPanel);
    }

    private void applyDriverPanelCache() {
        int i = 0;
        while (i < this.driverComboBox.getItemCount()) {
            AbstractDriver driver = (AbstractDriver)this.driverComboBox.getItemAt(i);
            driver.getPanel().setCache(this.driverPanelCache(this.layer));
            ++i;
        }
    }

    private void updateDriverPanelCache() {
        this.driverPanelCache(this.layer).addAll(this.currentDriverPanel.getCache());
        this.driverPanelCache(this.layer).put("DRIVER", this.driverComboBox.getSelectedItem());
    }

    private DriverPanelCache driverPanelCache(Layer layer) {
        if (layer == null) {
            return new DriverPanelCache();
        }
        DriverPanelCache cache = this.layerToDriverPanelCacheMap.get(layer);
        if (cache == null) {
            cache = new DriverPanelCache();
            cache.put("DRIVER", this.driverComboBox.getSelectedItem());
            this.layerToDriverPanelCacheMap.put(layer, cache);
        }
        return cache;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
}

