/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.RenderingHints;
import java.util.Map;
import org.saig.jump.widgets.config.ConfigRenderOptionsPanel;

public class RenderingHintsManager {
    private static Map<Object, Object> renderingHints;

    static {
        RenderingHintsManager.refreshRenderingHints();
    }

    public static Map<Object, Object> getRenderingHints() {
        return renderingHints;
    }

    public static void refreshRenderingHints() {
        boolean lineAntialiasingOn = false;
        boolean textAntialiasingOn = true;
        boolean quality = false;
        if (JUMPWorkbench.getFrameInstance() != null && JUMPWorkbench.getFrameInstance().getContext() != null && JUMPWorkbench.getBlackboard() != null) {
            lineAntialiasingOn = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigRenderOptionsPanel.KEY_LINE_ANTIALIASING_ON, false);
            textAntialiasingOn = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigRenderOptionsPanel.KEY_TEXT_ANTIALIASING_ON, true);
            quality = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigRenderOptionsPanel.KEY_QUALITY, false);
        }
        renderingHints = lineAntialiasingOn ? new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) : new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        if (quality) {
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        } else {
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        if (textAntialiasingOn) {
            renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }
}

