/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.project;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.io.File;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class OpenLastProjectPlugIn
extends ThreadedBasePlugIn {
    public static final String LAST_PROJECT_KEY = "LAST_PROYECT";
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.project.OpenLastProjectPlugIn.recover-last-open-project");
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.plugin.utils.project.OpenLastProjectPlugIn");
    private String projectName;

    public OpenLastProjectPlugIn() {
        this.projectName = null;
    }

    public OpenLastProjectPlugIn(String project) {
        this.projectName = project;
    }

    @Override
    public String getName() {
        if (this.projectName != null) {
            return I18N.getMessage("org.saig.jump.plugin.utils.project.OpenLastProjectPlugIn.recover-project-{0}", new Object[]{this.projectName});
        }
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Object value = PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(LAST_PROJECT_KEY);
        File file = new File((String)value);
        OpenProjectPlugIn openProject = new OpenProjectPlugIn();
        openProject.open(file, context, monitor);
    }
}

