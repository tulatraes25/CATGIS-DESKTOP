/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.jump.lang.I18N;

public class FontChooser
extends JDialog {
    private String sampleText = I18N.getString("workbench.ui.FontChooser.the-quick-brown-fox-jumped-over-the-lazy-dog");
    String[] styleList = new String[]{I18N.getString("workbench.ui.FontChooser.plain"), I18N.getString("workbench.ui.FontChooser.bold"), I18N.getString("workbench.ui.FontChooser.italic")};
    String[] sizeList = new String[]{"2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "30", "36", "48", "72"};
    String currentFont = null;
    int currentStyle = -1;
    int currentSize = -1;
    public boolean ok = false;
    private JPanel jPanel3;
    private JTextField jFont;
    private JScrollPane jScrollPane1;
    private JList jFontList;
    private JPanel jPanel4;
    private JTextField jStyle;
    private JScrollPane jScrollPane2;
    private JList jStyleList;
    private JPanel jPanel5;
    private JTextField jSize;
    private JScrollPane jScrollPane3;
    private JList jSizeList;
    private JPanel jPanel1;
    private JScrollPane jScrollPane4;
    private JTextArea jSample;
    private JPanel jButtons;
    private JButton jOk;
    private JButton jCancel;
    private JLabel jLabel6;

    private FontChooser(JDialog parent, boolean modal) {
        super((Dialog)parent, modal);
        this.jbInit();
        this.setListValues(this.jFontList, GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        this.setListValues(this.jStyleList, this.styleList);
        this.setListValues(this.jSizeList, this.sizeList);
        this.setCurrentFont(this.jSample.getFont());
        this.pack();
    }

    private FontChooser(JDialog parent, boolean modal, Font font) {
        this(parent, modal);
        this.setCurrentFont(font);
    }

    private void setListValues(JList list, String[] values) {
        if (list.getModel() instanceof DefaultListModel) {
            DefaultListModel model = (DefaultListModel)list.getModel();
            model.removeAllElements();
            int j = 0;
            while (j < values.length) {
                model.addElement(values[j]);
                ++j;
            }
        }
    }

    private void setSampleFont() {
        if (this.currentFont != null && this.currentStyle >= 0 && this.currentSize > 0) {
            this.jSample.setFont(new Font(this.currentFont, this.currentStyle, this.currentSize));
        }
    }

    private String styleToString(int style) {
        String str = "";
        if ((style & 1) == 1) {
            if (str.length() > 0) {
                str = String.valueOf(str) + ",";
            }
            str = String.valueOf(str) + "Bold";
        }
        if ((style & 2) == 2) {
            if (str.length() > 0) {
                str = String.valueOf(str) + ",";
            }
            str = String.valueOf(str) + "Italic";
        }
        if (str.length() <= 0 && !false) {
            str = "Plain";
        }
        return str;
    }

    public Font getCurrentFont() {
        return this.jSample.getFont();
    }

    public void setCurrentFont(Font font) {
        if (font == null) {
            font = this.jSample.getFont();
        }
        this.jFont.setText(font.getName());
        this.jFontActionPerformed(null);
        this.jStyle.setText(this.styleToString(font.getStyle()));
        this.jStyleActionPerformed(null);
        this.jSize.setText(Integer.toString(font.getSize()));
        this.jSizeActionPerformed(null);
    }

    public static Font showDialog(JDialog parent, String title, Font font) {
        FontChooser dialog = new FontChooser(parent, true, font);
        Point p1 = parent.getLocation();
        Dimension d1 = parent.getSize();
        Dimension d2 = dialog.getSize();
        int x = p1.x + (d1.width - d2.width) / 2;
        int y = p1.y + (d1.height - d2.height) / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        Font newfont = null;
        if (dialog.ok) {
            newfont = dialog.getCurrentFont();
        }
        dialog.dispose();
        return newfont;
    }

    private void jbInit() {
        this.jPanel3 = new JPanel();
        this.jFont = new JTextField();
        this.jScrollPane1 = new JScrollPane();
        this.jFontList = new JList();
        this.jPanel4 = new JPanel();
        this.jStyle = new JTextField();
        this.jScrollPane2 = new JScrollPane();
        this.jStyleList = new JList();
        this.jPanel5 = new JPanel();
        this.jSize = new JTextField();
        this.jScrollPane3 = new JScrollPane();
        this.jSizeList = new JList();
        this.jPanel1 = new JPanel();
        this.jScrollPane4 = new JScrollPane();
        this.jScrollPane4.setHorizontalScrollBarPolicy(31);
        this.jScrollPane4.setVerticalScrollBarPolicy(21);
        this.jSample = new JTextArea();
        this.jSample.setEditable(false);
        this.jButtons = new JPanel();
        this.jOk = new JButton();
        this.jCancel = new JButton();
        this.jLabel6 = new JLabel();
        this.getContentPane().setLayout(new GridBagLayout());
        this.setTitle(I18N.getString("workbench.ui.FontChooser.font-chooser"));
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent evt) {
                FontChooser.this.closeDialog(evt);
            }
        });
        this.jPanel3.setLayout(new GridBagLayout());
        this.jPanel3.setBorder(new TitledBorder(new EtchedBorder(), I18N.getString("workbench.ui.FontChooser.font")));
        this.jFont.setEditable(false);
        this.jFont.setColumns(24);
        this.jFont.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                FontChooser.this.jFontActionPerformed(evt);
            }
        });
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = 2;
        gridBagConstraints2.insets = new Insets(0, 3, 0, 3);
        gridBagConstraints2.anchor = 18;
        gridBagConstraints2.weightx = 1.0;
        this.jStyle.setEditable(false);
        this.jPanel3.add((Component)this.jFont, gridBagConstraints2);
        this.jFontList.setModel(new DefaultListModel());
        this.jFontList.setSelectionMode(0);
        this.jFontList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                FontChooser.this.jFontListValueChanged(evt);
            }
        });
        this.jScrollPane1.setViewportView(this.jFontList);
        gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = 1;
        gridBagConstraints2.insets = new Insets(3, 3, 3, 3);
        gridBagConstraints2.anchor = 18;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        this.jPanel3.add((Component)this.jScrollPane1, gridBagConstraints2);
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = 1;
        gridBagConstraints1.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints1.weightx = 0.5;
        gridBagConstraints1.weighty = 1.0;
        this.getContentPane().add((Component)this.jPanel3, gridBagConstraints1);
        this.jPanel4.setLayout(new GridBagLayout());
        this.jPanel4.setBorder(new TitledBorder(new EtchedBorder(), I18N.getString("workbench.ui.FontChooser.style")));
        this.jStyle.setColumns(18);
        this.jStyle.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                FontChooser.this.jStyleActionPerformed(evt);
            }
        });
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = 2;
        gridBagConstraints3.insets = new Insets(0, 3, 0, 3);
        gridBagConstraints3.anchor = 18;
        gridBagConstraints3.weightx = 1.0;
        this.jPanel4.add((Component)this.jStyle, gridBagConstraints3);
        this.jStyleList.setModel(new DefaultListModel());
        this.jStyleList.setVisibleRowCount(4);
        this.jStyleList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                FontChooser.this.jStyleListValueChanged(evt);
            }
        });
        this.jScrollPane2.setViewportView(this.jStyleList);
        gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = 1;
        gridBagConstraints3.insets = new Insets(3, 3, 3, 3);
        gridBagConstraints3.anchor = 18;
        gridBagConstraints3.weightx = 0.5;
        gridBagConstraints3.weighty = 1.0;
        this.jPanel4.add((Component)this.jScrollPane2, gridBagConstraints3);
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = 1;
        gridBagConstraints1.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints1.weightx = 0.375;
        gridBagConstraints1.weighty = 1.0;
        this.getContentPane().add((Component)this.jPanel4, gridBagConstraints1);
        this.jPanel5.setLayout(new GridBagLayout());
        this.jPanel5.setBorder(new TitledBorder(new EtchedBorder(), I18N.getString("workbench.ui.FontChooser.size")));
        this.jSize.setColumns(6);
        this.jSize.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                FontChooser.this.jSizeActionPerformed(evt);
            }
        });
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = 2;
        gridBagConstraints4.insets = new Insets(0, 3, 0, 3);
        gridBagConstraints4.anchor = 18;
        gridBagConstraints4.weightx = 1.0;
        this.jPanel5.add((Component)this.jSize, gridBagConstraints4);
        this.jSizeList.setModel(new DefaultListModel());
        this.jSizeList.setVisibleRowCount(4);
        this.jSizeList.setSelectionMode(0);
        this.jSizeList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                FontChooser.this.jSizeListValueChanged(evt);
            }
        });
        this.jScrollPane3.setViewportView(this.jSizeList);
        gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = 1;
        gridBagConstraints4.insets = new Insets(3, 3, 3, 3);
        gridBagConstraints4.anchor = 18;
        gridBagConstraints4.weightx = 0.25;
        gridBagConstraints4.weighty = 1.0;
        this.jPanel5.add((Component)this.jScrollPane3, gridBagConstraints4);
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = 1;
        gridBagConstraints1.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints1.weightx = 0.125;
        gridBagConstraints1.weighty = 1.0;
        this.jPanel1.setLayout(new GridBagLayout());
        this.jPanel1.setBorder(new TitledBorder(new EtchedBorder(), I18N.getString("workbench.ui.FontChooser.sample")));
        this.jSample.setWrapStyleWord(true);
        this.jSample.setLineWrap(true);
        this.jSample.setColumns(20);
        this.jSample.setRows(3);
        this.jSample.setText(this.sampleText);
        this.jScrollPane4.setViewportView(this.jSample);
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.fill = 1;
        gridBagConstraints5.insets = new Insets(0, 3, 3, 3);
        gridBagConstraints5.weightx = 1.0;
        gridBagConstraints5.weighty = 1.0;
        this.jPanel1.add((Component)this.jScrollPane4, gridBagConstraints5);
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = 1;
        gridBagConstraints1.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints1.anchor = 18;
        gridBagConstraints1.weightx = 1.0;
        this.getContentPane().add((Component)this.jPanel1, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, 10, 1, new Insets(0, 3, 3, 3), 0, 0));
        this.jButtons.setLayout(new GridBagLayout());
        this.jOk.setMnemonic(79);
        this.jOk.setText(I18N.getString("workbench.ui.FontChooser.ok"));
        this.jOk.setRequestFocusEnabled(false);
        this.jOk.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                FontChooser.this.jOkActionPerformed(evt);
            }
        });
        this.jButtons.add((Component)this.jOk, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 0), 0, 0));
        this.jCancel.setMnemonic(67);
        this.jCancel.setText(I18N.getString("workbench.ui.FontChooser.cancel"));
        this.jCancel.setRequestFocusEnabled(false);
        this.jCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                FontChooser.this.jCancelActionPerformed(evt);
            }
        });
        this.jButtons.add((Component)this.jCancel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.anchor = 16;
        gridBagConstraints1.weightx = 1.0;
        this.getContentPane().add((Component)this.jButtons, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, 16, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void jCancelActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    private void jOkActionPerformed(ActionEvent evt) {
        this.ok = true;
        this.setVisible(false);
    }

    private void jSizeActionPerformed(ActionEvent evt) {
        int size = 0;
        try {
            size = Integer.parseInt(this.jSize.getText());
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (size > 0) {
            this.currentSize = size;
            this.setSampleFont();
        }
    }

    private void jStyleActionPerformed(ActionEvent evt) {
        StringTokenizer st = new StringTokenizer(this.jStyle.getText(), ",");
        int style = 0;
        while (st.hasMoreTokens()) {
            String str = st.nextToken().trim();
            if (str.equalsIgnoreCase(I18N.getString("workbench.ui.FontChooser.plain"))) {
                style |= 0;
                continue;
            }
            if (str.equalsIgnoreCase(I18N.getString("workbench.ui.FontChooser.bold"))) {
                style |= 1;
                continue;
            }
            if (!str.equalsIgnoreCase(I18N.getString("workbench.ui.FontChooser.italic"))) continue;
            style |= 2;
        }
        if (style >= 0) {
            this.currentStyle = style;
            this.setSampleFont();
        }
    }

    private void jFontActionPerformed(ActionEvent evt) {
        DefaultListModel model = (DefaultListModel)this.jFontList.getModel();
        if (model.indexOf(this.jFont.getText()) >= 0) {
            this.currentFont = this.jFont.getText();
            this.setSampleFont();
        }
    }

    private void jStyleListValueChanged(ListSelectionEvent evt) {
        String str = "";
        Object[] values = this.jStyleList.getSelectedValues();
        if (values.length > 0) {
            int j = 0;
            while (j < values.length) {
                String s = (String)values[j];
                if (s.equalsIgnoreCase(I18N.getString("workbench.ui.FontChooser.plain"))) {
                    str = I18N.getString("workbench.ui.FontChooser.plain");
                    break;
                }
                if (str.length() > 0) {
                    str = String.valueOf(str) + ",";
                }
                str = String.valueOf(str) + (String)values[j];
                ++j;
            }
        } else {
            str = this.styleToString(this.currentStyle);
        }
        this.jStyle.setText(str);
        this.jStyleActionPerformed(null);
    }

    private void jSizeListValueChanged(ListSelectionEvent evt) {
        String str = (String)this.jSizeList.getSelectedValue();
        if (str == null || str.length() <= 0) {
            str = Integer.toString(this.currentSize);
        }
        this.jSize.setText(str);
        this.jSizeActionPerformed(null);
    }

    private void jFontListValueChanged(ListSelectionEvent evt) {
        String str = (String)this.jFontList.getSelectedValue();
        if (str == null || str.length() <= 0) {
            str = this.currentFont;
        }
        this.jFont.setText(str);
        this.jFontActionPerformed(null);
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
    }
}

