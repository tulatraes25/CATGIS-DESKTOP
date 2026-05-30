/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.task.TaskMonitorFilter;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Configuration;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.core.plugins.ToolInstanceManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class PlugInManager {
    private TaskMonitor monitor;
    private WorkbenchContext context;
    public static File plugInDirectory;
    private ClassLoader classLoader;
    private Collection<Extension> configurations = new ArrayList<Extension>();
    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(PlugInManager.class);
    }

    public PlugInManager(WorkbenchContext context, File directory, TaskMonitor monitor) throws Exception {
        this.monitor = monitor;
        this.context = context;
        plugInDirectory = directory;
        if (plugInDirectory != null && plugInDirectory.exists() && plugInDirectory.canRead() && plugInDirectory.isDirectory()) {
            this.classLoader = plugInDirectory != null ? new URLClassLoader(this.toURLs(this.findJarFilesRecursively(plugInDirectory).toArray(new File[0])), this.getClass().getClassLoader()) : this.getClass().getClassLoader();
            this.configurations.addAll(plugInDirectory != null ? this.findConfigurations(plugInDirectory) : new ArrayList());
            this.configurations.addAll(this.findConfigurations(context.getWorkbench().getProperties().getConfigurationClasses()));
        } else if (plugInDirectory != null) {
            DialogFactory.showErrorDialog(null, I18N.getString("workbench.plugin.PlugInManager.the-extensions-directory-does-not-exist-can-not-be-read-or-is-not-a-directory"), I18N.getString("workbench.plugin.PlugInManager.error-loading-extensions"));
        }
    }

    private Collection<File> findJarFilesRecursively(File directory) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : Arrays.asList(directory.listFiles())) {
            if (file.isDirectory()) {
                files.addAll(this.findJarFilesRecursively(file));
            }
            if (!file.isFile() || !this.isJarFile(file)) continue;
            files.add(file);
        }
        return files;
    }

    private boolean isJarFile(File file) {
        String extension = FileUtil.getExtension(file);
        return StringUtils.isNotEmpty((String)extension) && (extension.equalsIgnoreCase("jar") || extension.equalsIgnoreCase("zip"));
    }

    public void load() throws Exception {
        this.loadPlugInClasses(this.context.getWorkbench().getProperties().getPlugInClasses());
    }

    public static String name(Configuration configuration) {
        if (configuration instanceof Extension) {
            return ((Extension)configuration).getName();
        }
        return String.valueOf(StringUtil.toFriendlyName(configuration.getClass().getName(), I18N.getString("workbench.plugin.PlugInManager.configuration"))) + " (" + configuration.getClass().getPackage().getName() + ")";
    }

    public static String version(Configuration configuration) {
        if (configuration instanceof Extension) {
            return ((Extension)configuration).getVersion();
        }
        return "";
    }

    private Collection<Extension> findConfigurations(List<Class<?>> classes) throws Exception {
        ArrayList<Extension> configurations = new ArrayList<Extension>();
        for (Class<?> c : classes) {
            if (!Extension.class.isAssignableFrom(c)) continue;
            try {
                I18N.setPlugInRessource(c.getName(), this.classLoader);
            }
            catch (MissingResourceException mre) {
                LOGGER.debug((Object)(String.valueOf(I18N.getMessage("workbench.plugin.PlugInManager.can-not-load-the-language-file-for-the-extension-{0}", new Object[]{c.getName()})) + " - " + mre.getMessage()));
            }
            try {
                Extension configuration = (Extension)c.newInstance();
                configurations.add(configuration);
                TaskMonitorFilter.get(this.monitor).report(I18N.getMessage("workbench.plugin.PlugInManager.loading-{0}-{1}", new Object[]{PlugInManager.name(configuration), PlugInManager.version(configuration)}));
            }
            catch (Exception e) {
                LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.plugin.PlugInManager.error-loading-extension-{0}", new Object[]{c.getName()})) + " - " + e.getMessage()));
            }
        }
        return configurations;
    }

    private void loadPlugInClasses(List<Class<?>> plugInClasses) throws Exception {
        for (Class<?> plugInClass : plugInClasses) {
            PlugIn plugIn = (PlugIn)plugInClass.newInstance();
            plugIn.initialize(new PlugInContext(this.context, null, null, null, null));
            ToolInstanceManager.instance().registerPlugIn(plugIn);
        }
    }

    private Collection<Extension> findConfigurations(File plugInDirectory) throws Exception {
        this.monitor.report(String.valueOf(I18N.getString("workbench.plugin.PlugInManager.reading-extension-directory")) + "...");
        ArrayList<Extension> configurations = new ArrayList<Extension>();
        for (File file : Arrays.asList(plugInDirectory.listFiles())) {
            if (!file.isFile() || !this.isJarFile(file)) continue;
            try {
                configurations.addAll(this.findConfigurations(this.classes(new ZipFile(file), this.classLoader)));
            }
            catch (ZipException zipException) {
                // empty catch block
            }
        }
        return configurations;
    }

    private URL[] toURLs(File[] files) {
        URL[] urls = new URL[files.length];
        int i = 0;
        while (i < files.length) {
            try {
                urls[i] = new URL("jar:file:" + files[i].getPath() + "!/");
            }
            catch (MalformedURLException e) {
                Assert.shouldNeverReachHere((String)e.toString());
            }
            ++i;
        }
        return urls;
    }

    private List<Class<?>> classes(ZipFile zipFile, ClassLoader classLoader) {
        ArrayList classes = new ArrayList();
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            Class<?> c = this.toClass(entry, classLoader);
            if (c == null) continue;
            classes.add(c);
        }
        return classes;
    }

    private Class<?> toClass(ZipEntry entry, ClassLoader classLoader) {
        Class<?> candidate;
        if (entry.isDirectory()) {
            return null;
        }
        if (!entry.getName().endsWith(".class")) {
            return null;
        }
        if (entry.getName().indexOf("$") != -1) {
            return null;
        }
        String className = entry.getName();
        className = className.substring(0, className.length() - ".class".length());
        className = StringUtil.replaceAll(className, "/", ".");
        try {
            candidate = classLoader.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            Assert.shouldNeverReachHere((String)I18N.getMessage("workbench.plugin.PlugInManager.class-not-found-{0}-refine-class-name-algorithm", new Object[]{className}));
            return null;
        }
        catch (Throwable t) {
            t.printStackTrace(System.out);
            return null;
        }
        return candidate;
    }

    public Collection<Extension> getConfigurations() {
        return Collections.unmodifiableCollection(this.configurations);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void loadExtensionByName(String extensionClassName) throws Exception {
        for (Extension ext : this.configurations) {
            if (!ext.getClass().getName().equals(extensionClassName)) continue;
            ext.install(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
            ext.setActive(true);
        }
    }

    public static File getPlugInDirectory() {
        return plugInDirectory;
    }
}

