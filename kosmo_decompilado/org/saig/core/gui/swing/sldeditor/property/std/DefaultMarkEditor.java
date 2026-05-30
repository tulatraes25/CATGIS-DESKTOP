/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.MarkEditorCellRenderer;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.MarkEditor;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Mark;
import org.saig.jump.lang.I18N;

public class DefaultMarkEditor
extends MarkEditor {
    private static final long serialVersionUID = 1L;
    public static final String LIBRARY_DIRECTORY = "symbol";
    private Mark mark;
    private Vector<String> markNames;
    private JLabel lblMarks;
    private JComboBox cmbMarks;
    private FillEditor fillEditor;
    private StrokeEditor strokeEditor;
    private JTabbedPane tbpGraphicProperties;

    public DefaultMarkEditor(FeatureSchema featureType) {
        this(featureType, styleBuilder.createGraphic().getMarks()[0]);
    }

    public DefaultMarkEditor(FeatureSchema featureType, Mark mark) {
        this.setLayout(new GridBagLayout());
        this.lblMarks = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.well-known-mark"));
        this.markNames = new Vector();
        this.markNames.add("square");
        this.markNames.add("circle");
        this.markNames.add("triangle");
        this.markNames.add("star");
        this.markNames.add("cross");
        this.markNames.add("arrow");
        this.markNames.add("x");
        this.markNames.add("hatch");
        this.loadSymbolsFromLibrary();
        this.cmbMarks = new JComboBox<String>(this.markNames);
        this.cmbMarks.setRenderer(new MarkEditorCellRenderer());
        this.fillEditor = propertyEditorFactory.createFillEditor(featureType);
        this.fillEditor.setBorder(BorderFactory.createEmptyBorder());
        this.strokeEditor = propertyEditorFactory.createStrokeEditor(featureType);
        this.strokeEditor.setBorder(BorderFactory.createEmptyBorder());
        this.tbpGraphicProperties = new JTabbedPane();
        this.tbpGraphicProperties.add(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.fill"), this.fillEditor);
        this.tbpGraphicProperties.add(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.stroke"), this.strokeEditor);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.lblMarks, (JComponent)this.cmbMarks, true);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, (JComponent)this.tbpGraphicProperties, true, true);
        this.setMark(mark);
    }

    @Override
    public void setMark(Mark mark) {
        String markName = (String)mark.getWellKnownName().getValue(null);
        if (markName == null) {
            markName = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.square");
        }
        int i = 0;
        while (i < this.markNames.size()) {
            if (markName.equalsIgnoreCase(this.markNames.get(i))) {
                this.cmbMarks.setSelectedIndex(i);
                break;
            }
            ++i;
        }
        if (this.cmbMarks.getSelectedIndex() == -1) {
            this.cmbMarks.setSelectedIndex(0);
        }
        this.fillEditor.setFill(mark.getFill());
        this.strokeEditor.setStroke(mark.getStroke());
        this.mark = mark;
    }

    @Override
    public Mark getMark() {
        String wellKnown = (String)this.cmbMarks.getSelectedItem();
        this.mark.setWellKnownName(styleBuilder.literalExpression(wellKnown));
        this.mark.setFill(this.fillEditor.getFill());
        this.mark.setStroke(this.strokeEditor.getStroke());
        return this.mark;
    }

    public void loadSymbolsFromLibrary() {
        File libraryDirectory = new File(LIBRARY_DIRECTORY);
        File[] files = libraryDirectory.listFiles();
        if (files != null) {
            int i = 0;
            while (i < files.length) {
                File f = files[i];
                if (!f.isDirectory() && f.canRead()) {
                    this.markNames.add(f.getName());
                }
                ++i;
            }
        }
    }
}

