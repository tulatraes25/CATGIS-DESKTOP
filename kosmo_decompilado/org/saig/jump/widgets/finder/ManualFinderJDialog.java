/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.jump.widgets.finder;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.collections.CollectionUtils;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;
import org.saig.jump.widgets.finder.FinderComboBoxActionListener;
import org.saig.jump.widgets.finder.ILayerFinderDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class ManualFinderJDialog
extends JDialog
implements ILayerFinderDialog {
    private static final long serialVersionUID = 1L;
    private List<String> fields;
    private HashMap<String, JTextField> combos;
    private Layer layer;
    private JButton goButton;
    private JButton selectButton;
    private JButton cancelButton;

    public ManualFinderJDialog(Layer layer, boolean modal) {
        super(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.finder.FinderJDialog.Locator-for-the-layer-{0}", new Object[]{layer.getTitle(LocaleManager.getActiveLocale())}), modal);
        this.fields = layer.getFinderFields();
        this.layer = layer;
        this.buildGUI();
    }

    protected void buildGUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        this.goButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.Go-to"), GUIUtil.resize(IconLoader.icon("world_go.png"), 16));
        this.selectButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.Select"), GUIUtil.resize(IconLoader.icon("Select.gif"), 16));
        this.cancelButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.Close"));
        buttonPanel.add(this.goButton);
        buttonPanel.add(this.selectButton);
        buttonPanel.add(this.cancelButton);
        this.goButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ManualFinderJDialog.this.go();
            }
        });
        this.selectButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ManualFinderJDialog.this.select();
            }
        });
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ManualFinderJDialog.this.cancel();
            }
        });
        centerPanel.setLayout(new GridBagLayout());
        this.combos = new HashMap();
        int i = 0;
        for (String field : this.fields) {
            JLabel jl = new JLabel(this.layer.getFeatureSchema().getAttribute(field).getPublicName());
            JTextField jtf = new JTextField();
            jtf.setPreferredSize(new Dimension(100, jtf.getPreferredSize().height));
            this.combos.put(field, jtf);
            FormUtils.addRowInGBL((JComponent)centerPanel, i++, 0, jl, (JComponent)jtf);
        }
        mainPanel.add((Component)centerPanel, "Center");
        mainPanel.add((Component)buttonPanel, "South");
    }

    private void go() {
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<String> objs = new ArrayList<String>();
        for (String field : this.fields) {
            String o = this.combos.get(field).getText().trim();
            if (o.isEmpty()) continue;
            str.add(field);
            objs.add(o);
        }
        List<Feature> features = this.layer.getFeatureCollectionWrapper().getByAttribute(str.toArray(new String[str.size()]), objs.toArray());
        if (CollectionUtils.isEmpty(features)) {
            DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.No-results-were-found"), I18N.getMessage("org.saig.jump.widgets.finder.FinderJDialog.Locator-for-the-layer-{0}", new Object[]{this.layer.getTitle(LocaleManager.getActiveLocale())}));
            return;
        }
        ICoordTrans coordTrans = this.layer.getCoordTrans();
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        if (coordTrans != null) {
            for (Feature feat : features) {
                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry());
                pathGeom.reProject(coordTrans);
                geoms.add(ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp()));
            }
        } else {
            for (Feature feat : features) {
                geoms.add(feat.getGeometry());
            }
        }
        Envelope env = null;
        for (Geometry geom : geoms) {
            if (env == null) {
                env = geom.getEnvelopeInternal();
                continue;
            }
            env.expandToInclude(geom.getEnvelopeInternal());
        }
        try {
            Envelope proposedEnvelope = EnvelopeUtil.bufferByFraction(env, ConfigZoomPanel.getExtentFraction());
            JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getViewport().zoom(proposedEnvelope);
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    private void select() {
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<String> objs = new ArrayList<String>();
        for (String field : this.fields) {
            String o = this.combos.get(field).getText().trim();
            if (o.isEmpty()) continue;
            str.add(field);
            objs.add(o);
        }
        List<Feature> features = this.layer.getFeatureCollectionWrapper().getByAttribute(str.toArray(new String[str.size()]), objs.toArray());
        JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().clear();
        if (CollectionUtils.isEmpty(features)) {
            DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.Search-results"), I18N.getString("org.saig.jump.widgets.finder.ManualFinderJDialog.No-results-were-found"));
        } else {
            JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(this.layer, features);
        }
    }

    private void cancel() {
        this.setVisible(false);
    }

    @Override
    public Map<String, Object> getSelectedFilter() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (String field : this.fields) {
            String o = this.combos.get(field).getText().trim();
            if (o == FinderComboBoxActionListener.NULL_VALUE) continue;
            result.put(field, o);
        }
        return result;
    }
}

