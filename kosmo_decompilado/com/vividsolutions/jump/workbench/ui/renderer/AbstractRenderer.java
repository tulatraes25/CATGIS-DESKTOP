/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public abstract class AbstractRenderer {
    protected static final Logger LOGGER = Logger.getLogger(AbstractRenderer.class);
    public volatile boolean cancelled = false;
    private Object contentID;
    protected volatile ThreadSafeImage image = null;
    protected LayerViewPanel panel;
    protected volatile boolean rendering = false;
    protected Iterator<Feature> featureIterator;
    protected double factor;

    public AbstractRenderer() {
    }

    public AbstractRenderer(Object contentID, LayerViewPanel panel, double factor) {
        this.contentID = contentID;
        this.panel = panel;
        this.factor = factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public void clearImageCache() {
        this.image = null;
    }

    public boolean isRendering() {
        return this.rendering;
    }

    public Object getContentID() {
        return this.contentID;
    }

    public ThreadSafeImage getImage() {
        return this.image;
    }

    public Runnable createRunnable() {
        if (this.image != null) {
            return null;
        }
        this.rendering = true;
        this.cancelled = false;
        return new Runnable(){

            @Override
            public void run() {
                try {
                    if (AbstractRenderer.this.cancelled) {
                        return;
                    }
                    if (AbstractRenderer.this.panel == null) {
                        LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer.null-panel"));
                    }
                    AbstractRenderer.this.image = new ThreadSafeImage(AbstractRenderer.this.panel);
                    try {
                        AbstractRenderer.this.renderHook(AbstractRenderer.this.image);
                    }
                    catch (Throwable t) {
                        LOGGER.error((Object)"", t);
                        AbstractRenderer.this.panel.getContext().handleThrowable(t);
                    }
                    SwingUtilities.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            (this).AbstractRenderer.this.panel.superRepaint();
                        }
                    });
                }
                finally {
                    AbstractRenderer.this.rendering = false;
                }
            }
        };
    }

    protected abstract void renderHook(ThreadSafeImage var1) throws Exception;

    public void cancel() {
        this.cancelled = true;
        if (this.featureIterator != null && this.featureIterator instanceof FeatureIterator) {
            ((FeatureIterator)((Object)this.featureIterator)).close();
        }
    }

    public static interface Factory {
        public AbstractRenderer create();
    }
}

