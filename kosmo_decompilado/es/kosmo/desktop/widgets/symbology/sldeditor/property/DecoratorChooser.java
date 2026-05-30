/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property;

import es.kosmo.core.renderer.decorators.IDecorator;
import java.util.List;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class DecoratorChooser
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setDecorators(List<IDecorator> var1);

    public abstract List<IDecorator> getDecorators();
}

