/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.JList;

public class JListTypeAheadKeyListener
extends KeyAdapter {
    private JList list;
    private String buffer = "";
    private Date bufferUpdateTime = new Date();
    private int bufferLifetimeInMilliseconds = 1000;

    public JListTypeAheadKeyListener(JList list) {
        this.list = list;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        this.updateBuffer(e.getKeyChar());
        int i = 0;
        while (i < this.list.getModel().getSize()) {
            if (this.list.getModel().getElementAt(i).toString().toUpperCase().indexOf(this.buffer.toUpperCase()) == 0) {
                this.list.setSelectedValue(this.list.getModel().getElementAt(i), true);
                break;
            }
            ++i;
        }
    }

    private void updateBuffer(char c) {
        Date newBufferUpdateTime = new Date();
        if (newBufferUpdateTime.getTime() - this.bufferUpdateTime.getTime() > (long)this.bufferLifetimeInMilliseconds) {
            this.buffer = "";
        }
        this.bufferUpdateTime = newBufferUpdateTime;
        if (c != '\uffff') {
            this.buffer = String.valueOf(this.buffer) + c;
        }
    }
}

