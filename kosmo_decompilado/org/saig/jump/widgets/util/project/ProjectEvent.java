/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.jump.widgets.util.project;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Project;
import org.saig.jump.widgets.util.project.ProjectEventType;

public class ProjectEvent {
    private Project project;
    private String oldProjectPath = "";
    private ProjectEventType type;

    public ProjectEvent(Project project, ProjectEventType type) {
        Assert.isTrue((type != null ? 1 : 0) != 0);
        this.project = project;
        this.type = type;
    }

    public ProjectEvent(Project project, String oldProjectPath, ProjectEventType type) {
        Assert.isTrue((type != null ? 1 : 0) != 0);
        this.project = project;
        this.type = type;
        this.oldProjectPath = oldProjectPath;
    }

    public ProjectEventType getType() {
        return this.type;
    }

    public Project getProject() {
        return this.project;
    }

    public String getOldProjectPath() {
        return this.oldProjectPath;
    }
}

