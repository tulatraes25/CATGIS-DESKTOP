/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public abstract class BasicMetadataEditor
extends JComponent {
    private static final long serialVersionUID = 1L;
    protected JLabel metadataLabel;
    protected JLabel lblName = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor.name"));
    protected JTextField txtName = new JTextField();
    protected JLabel lblTitle = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor.title"));
    protected JLabel lblAbstract;
    protected JTextArea txaAbstract;
    protected JTextField txtTitle = new JTextField();
    protected JButton langSelecButton;

    public BasicMetadataEditor(boolean hasLangSelection) {
        this.lblAbstract = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor.abstract"));
        this.txaAbstract = new JTextArea(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor.abstract"));
        this.metadataLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor.metadata"));
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.metadataLabel);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, this.lblName, (JComponent)this.txtName);
        if (hasLangSelection) {
            this.langSelecButton = new JButton(IconLoader.icon("country.gif"));
            this.langSelecButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    BasicMetadataEditor.this.langSelecButtonActionListener();
                }
            });
            JPanel langSelectPanel = new JPanel(new BorderLayout());
            langSelectPanel.add((Component)this.txtTitle, "Center");
            langSelectPanel.add((Component)this.langSelecButton, "East");
            FormUtils.addRowInGBL((JComponent)this, 3, 0, this.lblTitle, (JComponent)langSelectPanel);
        } else {
            FormUtils.addRowInGBL((JComponent)this, 3, 0, this.lblTitle, (JComponent)this.txtTitle);
        }
        FormUtils.addRowInGBL((JComponent)this, 4, 0, (JComponent)this.lblAbstract, new JScrollPane(this.txaAbstract), 1.0, true);
    }

    protected abstract void langSelecButtonActionListener();

    public int getLastRow() {
        return 4;
    }

    protected String toText(String s) {
        if (s != null) {
            return s;
        }
        return "";
    }
}

