/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
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
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

public class FinderJDialog
extends JDialog
implements ILayerFinderDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FinderJDialog.class);
    protected List<String> fields;
    protected Layer layer;
    protected JButton goButton;
    protected JButton selectButton;
    protected JButton cancelButton;
    protected JButton flashButton;
    protected JPanel buttonPanel;
    protected HashMap<String, JComboBox> combos;

    public FinderJDialog(Layer layer, boolean modal) {
        super(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.finder.FinderJDialog.Locator-for-the-layer-{0}", new Object[]{layer.getTitle(LocaleManager.getActiveLocale())}), modal);
        this.fields = layer.getFinderFields();
        this.layer = layer;
        this.buildGUI();
    }

    protected void buildGUI() {
        Object jcb;
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.combos = new HashMap();
        ArrayList<String> comboFields = new ArrayList<String>();
        comboFields.addAll(this.fields);
        ArrayList<String> attrs = new ArrayList<String>();
        attrs.addAll(this.fields);
        Collections.reverse(comboFields);
        Object nextCb = null;
        String nextAttr = null;
        FinderComboBoxActionListener nextAL = null;
        for (String field : comboFields) {
            jcb = new JComboBox();
            FinderComboBoxActionListener al = new FinderComboBoxActionListener(this.layer, field, (JComboBox)jcb, attrs.toArray(new String[attrs.size()]), (JComboBox)nextCb, nextAttr, nextAL);
            ((JComboBox)jcb).addActionListener(al);
            attrs.remove(field);
            this.combos.put(field, (JComboBox)jcb);
            nextCb = jcb;
            nextAttr = field;
            nextAL = al;
        }
        int i = 0;
        for (String field : this.fields) {
            JLabel jl = new JLabel(String.valueOf(StringUtils.capitalize((String)this.layer.getFeatureSchema().getAttribute(field).getPublicName())) + ": ");
            FormUtils.addRowInGBL((JComponent)centerPanel, i++, 0, jl, (JComponent)this.combos.get(field));
        }
        String attr = this.fields.get(0);
        jcb = this.combos.get(attr);
        Set<Object> values = this.layer.getFeatureCollectionWrapper().getDistintsValues(attr);
        ((JComboBox)jcb).addItem(FinderComboBoxActionListener.NULL_VALUE);
        for (Object o : values) {
            ((JComboBox)jcb).addItem(o);
        }
        this.buttonPanel = new JPanel(new FlowLayout());
        this.flashButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.FinderJDialog.Flash"), GUIUtil.resize(IconLoader.icon("Flashlight.gif"), 16));
        this.goButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.FinderJDialog.Go-to"), GUIUtil.resize(IconLoader.icon("world_go.png"), 16));
        this.selectButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.FinderJDialog.Select"), GUIUtil.resize(IconLoader.icon("Select.gif"), 16));
        this.cancelButton = new JButton(I18N.getString("org.saig.jump.widgets.finder.FinderJDialog.Close"));
        this.buttonPanel.add(this.flashButton);
        this.buttonPanel.add(this.goButton);
        this.buttonPanel.add(this.selectButton);
        this.buttonPanel.add(this.cancelButton);
        this.flashButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FinderJDialog.this.flash();
            }
        });
        this.goButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FinderJDialog.this.go();
            }
        });
        this.selectButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FinderJDialog.this.select();
            }
        });
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FinderJDialog.this.cancel();
            }
        });
        mainPanel.add((Component)centerPanel, "Center");
        mainPanel.add((Component)this.buttonPanel, "South");
    }

    private void flash() {
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<Object> objs = new ArrayList<Object>();
        for (String field : this.fields) {
            Object o = this.combos.get(field).getSelectedItem();
            if (o == FinderComboBoxActionListener.NULL_VALUE) continue;
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
        try {
            ZoomToSelectedItemsPlugIn.flash(geoms, JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel());
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    protected void go() {
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<Object> objs = new ArrayList<Object>();
        for (String field : this.fields) {
            Object o = this.combos.get(field).getSelectedItem();
            if (o == FinderComboBoxActionListener.NULL_VALUE) continue;
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
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public Map<String, Object> getSelectedFilter() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (String field : this.fields) {
            Object o = this.combos.get(field).getSelectedItem();
            if (o == FinderComboBoxActionListener.NULL_VALUE) continue;
            result.put(field, o);
        }
        return result;
    }

    protected void select() {
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<Object> objs = new ArrayList<Object>();
        for (String field : this.fields) {
            Object o = this.combos.get(field).getSelectedItem();
            if (o == FinderComboBoxActionListener.NULL_VALUE) continue;
            str.add(field);
            objs.add(o);
        }
        List<Feature> features = this.layer.getFeatureCollectionWrapper().getByAttribute(str.toArray(new String[str.size()]), objs.toArray());
        JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().clear();
        JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(this.layer, features);
    }

    private void cancel() {
        this.setVisible(false);
    }
}

