/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.MarkEditor;
import org.saig.core.gui.swing.sldeditor.property.SymbolEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Mark;
import org.saig.core.styling.Symbol;
import org.saig.jump.lang.I18N;

public class DefaultSymbolEditor
extends SymbolEditor {
    private static final long serialVersionUID = 1L;
    private Symbol symbol;
    private static String MARK_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultSymbolEditor.mark");
    private static String EXTERNAL_GRAPHIC_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultSymbolEditor.external-graphic");
    private JLabel lblSymbolType;
    private JComboBox cmbSymbolType;
    private JPanel editorPanel;
    private MarkEditor markEditor;
    private ExternalGraphicEditor egxEditor;

    public DefaultSymbolEditor(FeatureSchema featureType) {
        this(featureType, styleBuilder.createGraphic().getSymbols()[0]);
    }

    public DefaultSymbolEditor(FeatureSchema featureType, Symbol symbol) {
        this.setLayout(new GridBagLayout());
        this.lblSymbolType = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultSymbolEditor.symbol-type"));
        this.cmbSymbolType = new JComboBox<String>(new String[]{MARK_KEY, EXTERNAL_GRAPHIC_KEY});
        this.cmbSymbolType.setSelectedIndex(0);
        this.cmbSymbolType.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                DefaultSymbolEditor.this.switchEditor();
            }
        });
        this.cmbSymbolType.setMinimumSize(new Dimension(300, 20));
        this.cmbSymbolType.setPreferredSize(new Dimension(300, 20));
        this.markEditor = propertyEditorFactory.createMarkEditor(featureType);
        this.egxEditor = propertyEditorFactory.createExternalGraphicEditor();
        this.editorPanel = new JPanel(new CardLayout());
        this.editorPanel.add((Component)this.markEditor, MARK_KEY);
        this.editorPanel.add((Component)this.egxEditor, EXTERNAL_GRAPHIC_KEY);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.lblSymbolType, (JComponent)this.cmbSymbolType);
        FormUtils.addRowInGBL(this, 1, 0, this.editorPanel);
        FormUtils.addFiller(this, 3, 0);
        this.setSymbol(symbol);
    }

    private void switchEditor() {
        ((CardLayout)this.editorPanel.getLayout()).show(this.editorPanel, (String)this.cmbSymbolType.getSelectedItem());
    }

    @Override
    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
        if (this.symbol instanceof Mark) {
            this.markEditor.setMark((Mark)symbol);
            this.cmbSymbolType.setSelectedItem(MARK_KEY);
        } else if (this.symbol instanceof ExternalGraphic) {
            this.egxEditor.setExternalGraphic((ExternalGraphic)symbol);
            this.cmbSymbolType.setSelectedItem(EXTERNAL_GRAPHIC_KEY);
        }
    }

    @Override
    public Symbol getSymbol() {
        if (this.cmbSymbolType.getSelectedItem().equals(MARK_KEY)) {
            return this.markEditor.getMark();
        }
        return this.egxEditor.getExternalGraphic();
    }
}

