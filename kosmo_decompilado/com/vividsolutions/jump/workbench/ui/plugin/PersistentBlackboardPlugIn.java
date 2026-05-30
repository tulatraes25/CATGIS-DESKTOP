/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class PersistentBlackboardPlugIn
extends AbstractPlugIn {
    private static String persistenceDirectory = ".";
    private static String fileName = "workbench-state.xml";
    private static final String BLACKBOARD_KEY = String.valueOf(PersistentBlackboardPlugIn.class.getName()) + " - BLACKBOARD";
    public static final Logger LOGGER = Logger.getLogger(PersistentBlackboardPlugIn.class);

    public static Blackboard get(WorkbenchContext context) {
        Blackboard blackboard = context.getBlackboard();
        return PersistentBlackboardPlugIn.get(blackboard);
    }

    public static Blackboard getPersistentBlackboard() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static Blackboard get(Blackboard blackboard) {
        if (blackboard.get(BLACKBOARD_KEY) == null) {
            blackboard.put(BLACKBOARD_KEY, new Blackboard());
        }
        return (Blackboard)blackboard.get(BLACKBOARD_KEY);
    }

    public static void setPersistenceDirectory(String value) {
        persistenceDirectory = value;
    }

    public static void setFileName(String value) {
        fileName = value;
    }

    public String getFilePath() {
        return String.valueOf(persistenceDirectory) + "/" + fileName;
    }

    @Override
    public void initialize(final PlugInContext context) throws Exception {
        this.restoreState(context.getWorkbenchContext());
        context.getWorkbenchFrame().addComponentListener(new ComponentAdapter(){

            @Override
            public void componentHidden(ComponentEvent e) {
                PersistentBlackboardPlugIn.this.saveState(context.getWorkbenchContext());
            }
        });
    }

    private void restoreState(WorkbenchContext workbenchContext) {
        if (!new File(this.getFilePath()).exists()) {
            return;
        }
        try {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(this.getFilePath());
                InputStreamReader inputReader = new InputStreamReader((InputStream)stream, Charset.forName("UTF-8"));
                PersistentBlackboardPlugIn.get(workbenchContext).putAll(((Blackboard)new XML2Java(workbenchContext.getWorkbench().getPlugInManager().getClassLoader()).read(inputReader, Blackboard.class)).getProperties());
                inputReader.close();
            }
            finally {
                if (stream != null) {
                    ((InputStream)stream).close();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void saveState(WorkbenchContext workbenchContext) {
        File tempConfigurationFile = FileUtil.createTemporalFile("workbench-state-temp", "xml");
        try {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(tempConfigurationFile);
                OutputStreamWriter outsw = new OutputStreamWriter((OutputStream)out, Charset.forName("UTF-8"));
                new Java2XML().write((Object)PersistentBlackboardPlugIn.get(workbenchContext), "workbench-state", outsw);
            }
            finally {
                if (out != null) {
                    ((OutputStream)out).close();
                }
            }
            FileUtil.copy(tempConfigurationFile, new File(this.getFilePath()));
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn.The-config-file-could-not-be-saved"), (Throwable)e);
        }
    }
}

