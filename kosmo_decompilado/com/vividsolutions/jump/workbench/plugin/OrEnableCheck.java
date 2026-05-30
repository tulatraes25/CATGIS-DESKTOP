/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

public class OrEnableCheck
implements EnableCheck {
    private List<EnableCheck> enableChecks = new ArrayList<EnableCheck>();
    private CursorTool cursorTool;

    public OrEnableCheck() {
    }

    public OrEnableCheck(CursorTool cursorTool) {
        this.cursorTool = cursorTool;
    }

    @Override
    public String check(JComponent component) {
        String errorMessage = null;
        boolean anyCheckOk = false;
        Iterator<EnableCheck> i = this.enableChecks.iterator();
        while (i.hasNext() && !anyCheckOk) {
            EnableCheck enableCheck = i.next();
            if (errorMessage == null) {
                errorMessage = enableCheck.check(component);
            }
            boolean bl = anyCheckOk = enableCheck.check(component) == null;
        }
        if (!anyCheckOk && errorMessage != null) {
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

    public OrEnableCheck add(EnableCheck enableCheck) {
        this.enableChecks.add(enableCheck);
        return this;
    }
}

