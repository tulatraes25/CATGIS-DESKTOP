/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

public class MultiEnableCheck
implements EnableCheck {
    private List<EnableCheck> enableChecks = new ArrayList<EnableCheck>();
    private CursorTool cursorTool;

    public MultiEnableCheck() {
    }

    public MultiEnableCheck(CursorTool cursorTool) {
        this.cursorTool = cursorTool;
    }

    public MultiEnableCheck(EnableCheck[] checks) {
        if (checks == null) {
            return;
        }
        EnableCheck[] enableCheckArray = checks;
        int n = checks.length;
        int n2 = 0;
        while (n2 < n) {
            EnableCheck check = enableCheckArray[n2];
            this.enableChecks.add(check);
            ++n2;
        }
    }

    @Override
    public String check(JComponent component) {
        for (EnableCheck enableCheck : this.enableChecks) {
            String errorMessage = enableCheck.check(component);
            if (errorMessage == null) continue;
            if (this.cursorTool != null) {
                this.cursorTool.setActivate(false);
            }
            return errorMessage;
        }
        if (this.cursorTool != null) {
            this.cursorTool.setActivate(true);
        }
        return null;
    }

    public MultiEnableCheck add(EnableCheck enableCheck) {
        this.enableChecks.add(enableCheck);
        return this;
    }
}

