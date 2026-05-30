/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.operation.valid.IsValidOp
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EnterWKTDialog;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class WKTPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(WKTPlugIn.class);
    protected Layer layer;

    protected abstract Layer layer(PlugInContext var1);

    private void validate(FeatureCollection c, PlugInContext context) throws WorkbenchException {
        FeatureIterator i = null;
        try {
            try {
                i = c.iterator();
                while (i.hasNext()) {
                    Feature f = i.next();
                    IsValidOp op = new IsValidOp(f.getGeometry());
                    if (op.isValid()) continue;
                    if (this.isRollingBackInvalidEdits()) {
                        throw new WorkbenchException(op.getValidationError().getMessage());
                    }
                    context.getWorkbenchFrame().warnUser(op.getValidationError().getMessage());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (i != null) {
                    i.close();
                }
            }
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.layer = this.layer(context);
        EnterWKTDialog d = this.createDialog(context);
        d.setVisible(true);
        return d.wasOKPressed();
    }

    protected abstract void apply(FeatureCollection var1, PlugInContext var2) throws WorkbenchException, Exception;

    protected EnterWKTDialog createDialog(final PlugInContext context) {
        final EnterWKTDialog d = new EnterWKTDialog(context.getWorkbenchFrame(), I18N.getString("workbench.ui.plugin.WKTPlugIn.enter-well-known-text"), true);
        d.setSize(500, 400);
        d.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (d.wasOKPressed()) {
                        WKTPlugIn.this.apply(d.getText(), context);
                    }
                    d.setVisible(false);
                }
                catch (Exception ex) {
                    LOGGER.error((Object)ex);
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.WKTPlugIn.wkt-expression-is-not-correct")) + "\n" + ex.getMessage(), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.WKTPlugIn.wkt-format-incorrect"));
                }
            }
        });
        GUIUtil.centreOnWindow(d);
        return d;
    }

    protected void apply(String wkt, PlugInContext context) throws Exception {
        StringReader stringReader = new StringReader(wkt);
        try {
            WKTReader wktReader = new WKTReader();
            FeatureCollection c = wktReader.read(stringReader);
            this.validate(c, context);
            this.apply(c, context);
        }
        finally {
            stringReader.close();
        }
    }
}

