/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Project;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.saig.jump.lang.I18N;

public class LocaleManager {
    public static Locale getActiveLocale() {
        if (JUMPWorkbench.getFrameInstance() != null && JUMPWorkbench.getFrameInstance().getContext().getProject() != null) {
            return JUMPWorkbench.getFrameInstance().getContext().getProject().getActiveLocale();
        }
        return I18N.getLocale();
    }

    public static Set<Locale> getAvailablesLocales() {
        if (JUMPWorkbench.getFrameInstance() != null && JUMPWorkbench.getFrameInstance().getContext().getProject() != null) {
            return JUMPWorkbench.getFrameInstance().getContext().getProject().getAvailablesLocales();
        }
        HashSet<Locale> defaultLocales = new HashSet<Locale>();
        defaultLocales.add(I18N.getLocale());
        return defaultLocales;
    }

    public static boolean hasActiveLocale() {
        return LocaleManager.getActiveLocale() != null;
    }

    public static void setAvailablesLocales(Set<Locale> selectedLocales) {
        if (JUMPWorkbench.getFrameInstance() != null) {
            Project activeProject = JUMPWorkbench.getFrameInstance().getContext().getProject();
            activeProject.refreshLocales(selectedLocales);
        }
    }
}

