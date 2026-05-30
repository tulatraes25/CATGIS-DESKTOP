/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import javax.swing.Icon;
import org.saig.core.filter.Expression;

public interface ExternalGraphicFactory {
    public Icon getIcon(Feature var1, Expression var2, String var3, int var4) throws Exception;
}

