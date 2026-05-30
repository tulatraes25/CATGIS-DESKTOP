/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.apache.log4j.Logger;

public class AnimatedClockPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AnimatedClockPanel.class);
    private List<Icon> queue = new ArrayList<Icon>();
    private Timer timer = new Timer(250, new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
            AnimatedClockPanel.this.nextImage();
        }
    });
    private JLabel label = new JLabel();
    private BorderLayout borderLayout1 = new BorderLayout();

    public AnimatedClockPanel() {
        this.add("ClockN.gif");
        this.add("ClockNE.gif");
        this.add("ClockE.gif");
        this.add("ClockSE.gif");
        this.add("ClockS.gif");
        this.add("ClockSW.gif");
        this.add("ClockW.gif");
        this.add("ClockNW.gif");
        try {
            this.jbInit();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    private void add(String icon) {
        this.queue.add(IconLoader.icon(icon));
    }

    public void start() {
        this.timer.start();
    }

    public void stop() {
        this.timer.stop();
    }

    private void nextImage() {
        Icon icon = this.queue.remove(0);
        this.queue.add(icon);
        this.label.setIcon(icon);
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.add((Component)this.label, "Center");
        this.label.setIcon(IconLoader.icon("ClockN.gif"));
    }
}

