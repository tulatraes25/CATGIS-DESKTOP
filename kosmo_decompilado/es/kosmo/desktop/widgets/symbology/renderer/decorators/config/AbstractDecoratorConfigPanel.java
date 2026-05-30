/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.IDecoratorConfigPanel;
import javax.swing.JPanel;

public abstract class AbstractDecoratorConfigPanel
extends JPanel
implements IDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    protected FeatureSchema schema;

    public void setSchema(FeatureSchema layerSchema) {
        this.schema = layerSchema;
    }

    @Override
    public String validateInput() {
        return null;
    }
}

