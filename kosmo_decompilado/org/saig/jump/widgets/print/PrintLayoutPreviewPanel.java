/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutFrame;

public class PrintLayoutPreviewPanel
extends JScrollPane
implements ItemListener,
MouseListener {
    private static final long serialVersionUID = 1L;
    private PrintLayoutFrame parent;
    private Rule columnView;
    private Rule rowView;
    private PreviewPanel preview;
    private JToggleButton btnUnits;

    public PrintLayoutPreviewPanel(PrintLayoutFrame parent) {
        this.parent = parent;
        this.setPreferredSize(new Dimension(875, 675));
        this.setSize(this.getPreferredSize());
        this.getViewport().setPreferredSize(new Dimension(800, 600));
        this.getViewport().setSize(this.getViewport().getPreferredSize());
        this.columnView = new Rule(0, true);
        this.rowView = new Rule(1, true);
        this.columnView.setPreferredWidth(Math.round((float)this.getViewport().getSize().getWidth()));
        this.rowView.setPreferredHeight(Math.round((float)this.getViewport().getSize().getWidth()));
        JPanel buttonCorner = new JPanel();
        this.btnUnits = new JToggleButton("cm", true);
        this.btnUnits.setFont(new Font("SansSerif", 0, 11));
        this.btnUnits.setPreferredSize(new Dimension(25, 25));
        this.btnUnits.setSize(this.btnUnits.getPreferredSize());
        this.btnUnits.setMargin(new Insets(2, 2, 2, 2));
        this.btnUnits.addItemListener(this);
        buttonCorner.add(this.btnUnits);
        this.setColumnHeaderView(this.columnView);
        this.setRowHeaderView(this.rowView);
        this.setViewportBorder(BorderFactory.createLineBorder(Color.BLUE));
        this.setCorner("UPPER_LEFT_CORNER", buttonCorner);
        this.setCorner("LOWER_LEFT_CORNER", new Corner());
        this.setCorner("UPPER_RIGHT_CORNER", new Corner());
        this.addMouseListener(this);
    }

    public void dispose() {
        this.parent = null;
        if (this.preview != null) {
            this.preview.dispose();
            this.preview = null;
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Rectangle pageBounds = this.preview.getPage().getPageDrawOnScreen().getBounds();
        int ruleStartWidth = pageBounds.x;
        int ruleStartHeight = pageBounds.y;
        int ruleEndWidth = pageBounds.x + pageBounds.width;
        int ruleEndHeight = pageBounds.y + pageBounds.height;
        this.columnView.setPreferredWidth(ruleEndWidth);
        this.rowView.setPreferredHeight(ruleEndHeight);
        this.rowView.setRuleStart(ruleStartHeight);
        this.rowView.setRuleEnd(ruleEndHeight);
        this.columnView.setRuleStart(ruleStartWidth);
        this.columnView.setRuleEnd(ruleEndWidth);
        if (e.getStateChange() == 1) {
            this.rowView.setIsMetric(true, this.preview.getCmUnit());
            this.columnView.setIsMetric(true, this.preview.getCmUnit());
            this.btnUnits.setText("cm");
        } else {
            this.rowView.setIsMetric(false, this.preview.getInchUnit());
            this.columnView.setIsMetric(false, this.preview.getInchUnit());
            this.btnUnits.setText("in");
        }
    }

    public void repaintRules() {
        if (this.preview != null && this.preview.getPage() != null && this.preview.getPage().getPageDrawOnScreen() != null) {
            Rectangle pageBounds = this.preview.getPage().getPageDrawOnScreen().getBounds();
            int ruleStartWidth = pageBounds.x;
            int ruleStartHeight = pageBounds.y;
            int ruleEndWidth = pageBounds.x + pageBounds.width;
            int ruleEndHeight = pageBounds.y + pageBounds.height;
            this.columnView.setPreferredWidth(ruleEndWidth);
            this.rowView.setPreferredHeight(ruleEndHeight);
            this.rowView.setRuleStart(ruleStartHeight);
            this.rowView.setRuleEnd(ruleEndHeight);
            this.columnView.setRuleStart(ruleStartWidth);
            this.columnView.setRuleEnd(ruleEndWidth);
            this.columnView.repaint();
            this.rowView.repaint();
        }
    }

    public void setPreview(PreviewPanel preview) {
        this.preview = preview;
        this.setViewportView(preview);
        Rectangle pageBounds = preview.getPage().getPageDrawOnScreen().getBounds();
        int ruleStartWidth = pageBounds.x;
        int ruleStartHeight = pageBounds.y;
        int ruleEndWidth = pageBounds.x + pageBounds.width;
        int ruleEndHeight = pageBounds.y + pageBounds.height;
        this.columnView.setPreferredWidth(ruleEndWidth);
        this.rowView.setPreferredHeight(ruleEndHeight);
        this.rowView.setRuleStart(ruleStartHeight);
        this.rowView.setRuleEnd(ruleEndHeight);
        this.columnView.setRuleStart(ruleStartWidth);
        this.columnView.setRuleEnd(ruleEndWidth);
        if (this.rowView.isMetric()) {
            this.rowView.setIsMetric(true, preview.getCmUnit());
            this.columnView.setIsMetric(true, preview.getCmUnit());
            this.btnUnits.setText("cm");
        } else {
            this.rowView.setIsMetric(false, preview.getInchUnit());
            this.columnView.setIsMetric(false, preview.getInchUnit());
            this.btnUnits.setText("in");
        }
    }

    public PreviewPanel getPreviewPanel() {
        return this.preview;
    }

    public boolean isMetric() {
        return this.rowView.isMetric();
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (this.parent.getSelectedComponent() != null) {
            this.parent.getSelectedComponent().setSelected(false);
        }
        this.parent.setSelectedComponent(null);
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    public void setParent(PrintLayoutFrame parent) {
        this.parent = parent;
    }

    private class Corner
    extends JComponent {
        private static final long serialVersionUID = 1L;

        private Corner() {
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    private class Rule
    extends JComponent {
        private static final long serialVersionUID = 1L;
        protected static final int HORIZONTAL = 0;
        protected static final int VERTICAL = 1;
        private final int INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        private final int CM = Conversion.inch_To_cm(this.INCH);
        private static final int SIZE = 35;
        private int orientation;
        private boolean isMetric;
        private int increment;
        private int units;
        private int ruleStart;
        private int ruleEnd;

        public Rule(int o, boolean m) {
            this.orientation = o;
            this.isMetric = m;
            this.setIncrementAndUnits();
        }

        public void setIsMetric(boolean isMetric, int unit) {
            this.isMetric = isMetric;
            this.setIncrementAndUnits(unit);
            this.repaint();
        }

        private void setIncrementAndUnits() {
            if (this.isMetric) {
                this.units = this.CM;
                this.increment = this.units / 2;
            } else {
                this.units = this.INCH;
                this.increment = this.units / 2;
            }
        }

        private void setIncrementAndUnits(int unit) {
            if (this.isMetric) {
                this.increment = this.units = unit;
            } else {
                this.units = unit;
                this.increment = this.units / 2;
            }
        }

        public boolean isMetric() {
            return this.isMetric;
        }

        public void setPreferredHeight(int ph) {
            this.setMinimumSize(new Dimension(35, ph));
            this.setPreferredSize(new Dimension(35, ph));
        }

        public void setPreferredWidth(int pw) {
            this.setMinimumSize(new Dimension(pw, 35));
            this.setPreferredSize(new Dimension(pw, 35));
        }

        public void setRuleStart(int pixelStart) {
            this.ruleStart = pixelStart;
        }

        public void setRuleEnd(int pixelEnd) {
            this.ruleEnd = pixelEnd;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Rectangle drawHere = g.getClipBounds();
            g.setColor(Color.WHITE);
            if (this.orientation == 0) {
                g.fillRect(this.ruleStart, drawHere.y, this.getPreferredSize().width - this.ruleStart, this.getPreferredSize().height);
            } else {
                g.fillRect(drawHere.x, this.ruleStart, this.getPreferredSize().width, this.getPreferredSize().height - this.ruleStart);
            }
            g.setFont(new Font("SansSerif", 0, 10));
            g.setColor(Color.black);
            int end = 0;
            int start = 0;
            int tickLength = 0;
            String text = null;
            end = this.ruleEnd;
            start = this.ruleStart;
            if (start == this.ruleStart) {
                text = Integer.toString(0);
                tickLength = 10;
                if (this.orientation == 0) {
                    g.drawLine(start, 34, start, 35 - tickLength - 1);
                    g.drawString(text, start - 3, 21);
                } else {
                    g.drawLine(34, start, 35 - tickLength - 1, start);
                    g.drawString(text, 9, start + 3);
                }
                text = null;
            }
            int i = start + this.increment;
            while (i < end + this.increment) {
                if ((i - this.ruleStart) % (5 * this.units) == 0) {
                    tickLength = 10;
                    text = Integer.toString((i - this.ruleStart) / this.units);
                } else {
                    tickLength = 7;
                    text = null;
                }
                if (tickLength != 0) {
                    if (this.orientation == 0) {
                        g.drawLine(i, 34, i, 35 - tickLength - 1);
                        if (text != null) {
                            g.drawString(text, i - 3, 21);
                        }
                    } else {
                        g.drawLine(34, i, 35 - tickLength - 1, i);
                        if (text != null) {
                            g.drawString(text, 9, i + 3);
                        }
                    }
                }
                i += this.increment;
            }
        }
    }
}

