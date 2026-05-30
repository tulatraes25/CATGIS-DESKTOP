/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import es.kosmo.core.renderer.decorators.IDecorator;

public interface IDecoratorConfigPanel {
    public void setDecorator(IDecorator var1);

    public IDecorator getDecorator(IDecorator var1);

    public String validateInput();
}

