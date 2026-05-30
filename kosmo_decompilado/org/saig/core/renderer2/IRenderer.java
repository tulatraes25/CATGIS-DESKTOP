/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer2;

import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;

public interface IRenderer {
    public void render(ThreadSafeImage var1, Layerable var2, Viewport var3, FeatureCollectionRenderer var4, boolean var5);
}

