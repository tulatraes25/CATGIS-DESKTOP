/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

public class MultiLineToolTip
extends JToolTip {
    private static final long serialVersionUID = 1L;

    public MultiLineToolTip() {
        this.setUI(new MultiLineToolTipUI());
    }

    public MultiLineToolTip(MetalToolTipUI toolTipUI) {
        this.setUI(toolTipUI);
    }

    private class MultiLineToolTipUI
    extends MetalToolTipUI {
        private String[] strs;

        private MultiLineToolTipUI() {
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            FontMetrics metrics = c.getFontMetrics(c.getFont());
            Dimension size = c.getSize();
            g.setColor(c.getBackground());
            g.fillRect(0, 0, size.width, size.height);
            g.setColor(c.getForeground());
            if (this.strs != null) {
                int length = this.strs.length;
                int i = 0;
                while (i < length) {
                    g.drawString(this.strs[i], 3, metrics.getHeight() * (i + 1));
                    ++i;
                }
            }
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            FontMetrics metrics = c.getFontMetrics(c.getFont());
            String tipText = ((JToolTip)c).getTipText();
            if (tipText == null) {
                tipText = "";
            }
            StringTokenizer st = new StringTokenizer(tipText, "|");
            int maxWidth = 0;
            Vector<String> v = new Vector<String>();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int width = SwingUtilities.computeStringWidth(metrics, token);
                maxWidth = maxWidth < width ? width : maxWidth;
                v.addElement(token);
            }
            int lines = v.size();
            if (lines < 1) {
                this.strs = null;
                lines = 1;
            } else {
                this.strs = new String[lines];
                int i = 0;
                Enumeration e = v.elements();
                while (e.hasMoreElements()) {
                    this.strs[i] = (String)e.nextElement();
                    ++i;
                }
            }
            int height = metrics.getHeight() * lines;
            return new Dimension(maxWidth + 6, height + 4);
        }
    }
}

