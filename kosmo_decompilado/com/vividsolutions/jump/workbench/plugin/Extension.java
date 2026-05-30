/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.plugin.Configuration;
import org.saig.jump.lang.I18N;

public abstract class Extension
implements Configuration {
    protected boolean active = false;

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public String getName() {
        return String.valueOf(StringUtil.toFriendlyName(this.getClass().getName(), I18N.getString("workbench.plugin.Extension.extension"))) + (this.getClass().getPackage() == null ? "" : " (" + this.getClass().getPackage().getName() + ")");
    }

    public String getDescription() {
        return "";
    }

    public String getVersion() {
        return "";
    }
}

