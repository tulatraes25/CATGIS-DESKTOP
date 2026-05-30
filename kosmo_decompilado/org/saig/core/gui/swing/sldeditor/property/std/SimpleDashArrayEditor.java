/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.apache.commons.lang.ArrayUtils;
import org.saig.core.gui.swing.sldeditor.property.DashArrayEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

public class SimpleDashArrayEditor
extends DashArrayEditor {
    private static final long serialVersionUID = 1L;
    private List<float[]> dashArray;
    private JComboBox cboDash;
    private String[] linePatterns = new String[]{"1", "2", "3", "4", "4,2", "5", "5,1", "6", "6,2", "6,6", "7", "7,12", "9", "9,2", "15,6", "20,3"};

    public SimpleDashArrayEditor() {
        this.setLayout(new GridBagLayout());
        this.cboDash = new JComboBox();
        this.cboDash.setRenderer(new ListCellRenderer(){
            private JLabel label = new JLabel(" ");
            private JPanel panel = new JPanel(new BorderLayout()){
                private static final long serialVersionUID = 1L;
                {
                    label.setPreferredSize(new Dimension(150, (int)label.getPreferredSize().getHeight()));
                    this.add((Component)label, "Center");
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int offset = label.getFontMetrics(label.getFont()).stringWidth(label.getText()) + 5;
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setStroke(new BasicStroke(lineWidth, 0, 2, 1.0f, BasicStyle.toArray(linePattern, lineWidth), 0.0f));
                    g2.draw(new Line2D.Double(offset, (double)panel.getHeight() / 2.0, panel.getWidth(), (double)panel.getHeight() / 2.0));
                }
            };
            private String linePattern;
            private int lineWidth = 2;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                this.linePattern = (String)value;
                this.label.setText(ArrayUtils.toString((Object)this.linePattern));
                this.panel.setForeground(UIManager.getColor(isSelected ? "ComboBox.selectionForeground" : "ComboBox.foreground"));
                this.panel.setBackground(UIManager.getColor(isSelected ? "ComboBox.selectionBackground" : "ComboBox.background"));
                return this.panel;
            }
        });
        this.dashArray = new ArrayList<float[]>();
        int i = 0;
        while (i < this.linePatterns.length) {
            String currentLinePattern = this.linePatterns[i];
            List<String> strings = StringUtil.fromCommaDelimitedString(currentLinePattern);
            float[] array = new float[strings.size()];
            int j = 0;
            while (j < strings.size()) {
                String string = strings.get(j);
                array[j] = Float.parseFloat(string);
                ++j;
            }
            this.dashArray.add(array);
            ++i;
        }
        this.initDashIcon();
        FormUtils.addRowInGBL(this, 0, 0, this.cboDash);
    }

    @Override
    public void setDashArray(float[] dash) {
        int i = 0;
        while (i < this.dashArray.size()) {
            float[] currDash = this.dashArray.get(i);
            if (Arrays.equals(dash, currDash)) {
                this.cboDash.setSelectedIndex(i);
                return;
            }
            ++i;
        }
        this.dashArray.add(dash);
        this.initDashIcon();
        this.cboDash.setSelectedIndex(this.dashArray.size() - 1);
    }

    @Override
    public float[] getDashArray() {
        return this.dashArray.get(this.cboDash.getSelectedIndex());
    }

    private void initDashIcon() {
        if (this.dashArray != null) {
            this.cboDash.removeAllItems();
            for (float[] currentDash : this.dashArray) {
                String dash = ArrayUtils.toString((Object)currentDash);
                if (dash.length() >= 2) {
                    dash = dash.substring(1, dash.length() - 1);
                }
                this.cboDash.addItem(dash);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.cboDash.setEnabled(enabled);
    }
}

