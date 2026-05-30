/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class FeatureUnionDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FeatureUnionDialog.class);
    private static final String TITLE = I18N.getString("org.saig.jump.plugin.editing.FeatureUnionDialog.attributes-selection");
    private JList featureSelectionList;
    private JCheckBox dissolveGeometriesCheckBox;
    private JButton okButton;
    private JButton cancelButton;
    private JButton noAttrButton;
    private JButton flashButton;
    private boolean canceled = true;

    public FeatureUnionDialog(List<Feature> features, boolean allowToJoinWithoutAttributes) {
        super(JUMPWorkbench.getFrameInstance(), TITLE, true);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        this.okButton = new JButton(I18N.getString("org.saig.jump.widgets.editing.FeatureUnionDialog.Join-with-attributes"));
        this.okButton.setEnabled(false);
        this.okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                FeatureUnionDialog.this.canceled = false;
                FeatureUnionDialog.this.setVisible(false);
            }
        });
        this.cancelButton = new JButton(I18N.getString("org.saig.jump.plugin.editing.FeatureUnionDialog.cancel"));
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                FeatureUnionDialog.this.canceled = true;
                FeatureUnionDialog.this.setVisible(false);
            }
        });
        this.noAttrButton = new JButton(I18N.getString("org.saig.jump.plugin.editing.FeatureUnionDialog.join-without-attributes"));
        this.noAttrButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                FeatureUnionDialog.this.canceled = false;
                FeatureUnionDialog.this.featureSelectionList.clearSelection();
                FeatureUnionDialog.this.setVisible(false);
            }
        });
        JPanel actionButtonPanel = new JPanel(new FlowLayout());
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder(""));
        this.flashButton = new JButton(IconLoader.icon("Flashlight.gif"));
        this.flashButton.setToolTipText(I18N.getString("org.saig.jump.widgets.editing.FeatureUnionDialog.Flash-the-selected-feature"));
        this.flashButton.addActionListener(this);
        this.flashButton.setEnabled(false);
        actionButtonPanel.add(this.flashButton);
        this.featureSelectionList = new JList<Object>(features.toArray());
        this.featureSelectionList.setSelectionMode(0);
        this.featureSelectionList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Feature selectedFeat = FeatureUnionDialog.this.getSelectedFeature();
                FeatureUnionDialog.this.okButton.setEnabled(selectedFeat != null);
                FeatureUnionDialog.this.flashButton.setEnabled(selectedFeat != null);
            }
        });
        JScrollPane listScrollPane = new JScrollPane(this.featureSelectionList, 22, 30);
        Dimension dim = new Dimension(400, 250);
        listScrollPane.setMinimumSize(dim);
        listScrollPane.setPreferredSize(dim);
        listScrollPane.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.editing.FeatureUnionDialog.choose-what-attributes-to-copy")));
        JPanel south = new JPanel(new FlowLayout());
        if (allowToJoinWithoutAttributes) {
            south.add(this.noAttrButton);
        }
        south.add(this.okButton);
        south.add(this.cancelButton);
        this.dissolveGeometriesCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.editing.FeatureUnionDialog.Dissolve-the-output-geometries"));
        this.dissolveGeometriesCheckBox.setSelected(true);
        FormUtils.addRowInGBL(mainPanel, 0, 0, listScrollPane);
        FormUtils.addRowInGBL(mainPanel, 1, 0, actionButtonPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.dissolveGeometriesCheckBox);
        FormUtils.addRowInGBL(mainPanel, 3, 0, south);
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public Feature getSelectedFeature() {
        return (Feature)this.featureSelectionList.getSelectedValue();
    }

    public boolean dissolveSelectedGeometries() {
        return this.dissolveGeometriesCheckBox.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Feature selectedFeat;
        if (e.getSource().equals(this.flashButton) && (selectedFeat = this.getSelectedFeature()) != null) {
            Geometry[] featds = new Geometry[]{selectedFeat.getGeometry()};
            GeometryCollection gc = new GeometryCollection(featds, new GeometryFactory());
            try {
                JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().flash(gc);
            }
            catch (NoninvertibleTransformException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
        }
    }
}

