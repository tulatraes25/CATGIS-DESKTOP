/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jgoodies.looks.LookUtils
 *  com.jgoodies.looks.plastic.PlasticXPLookAndFeel
 *  org.apache.commons.lang.StringUtils
 *  org.apache.contrib.jimmoore.LoggingOutputStream
 *  org.apache.log4j.Category
 *  org.apache.log4j.Level
 *  org.apache.log4j.Logger
 *  org.apache.log4j.Priority
 */
package com.vividsolutions.jump.workbench;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.util.commandline.CommandLine;
import com.vividsolutions.jump.util.commandline.OptionSpec;
import com.vividsolutions.jump.util.commandline.ParseException;
import com.vividsolutions.jump.workbench.JUMPConfiguration;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.WorkbenchProperties;
import com.vividsolutions.jump.workbench.WorkbenchPropertiesFile;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInManager;
import com.vividsolutions.jump.workbench.ui.SplashPanel;
import com.vividsolutions.jump.workbench.ui.SplashWindow;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.apache.commons.lang.StringUtils;
import org.apache.contrib.jimmoore.LoggingOutputStream;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.gvsig.crs.ICrs;
import org.saig.core.filter.FilterFactory;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.task.TaskManager;
import org.saig.core.renderer.Renderer;
import org.saig.core.styling.StyleFactory;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.IniFileLoader;
import org.saig.jump.widgets.config.ConfigViewDataPanel;
import org.saig.jump.widgets.config.ProxyAuth;
import org.saig.jump.widgets.util.DialogFactory;

public class JUMPWorkbench {
    public static final Logger LOGGER = Logger.getLogger(JUMPWorkbench.class);
    public static Image SPLASH_IMAGE = null;
    public static Image ABOUT_IMAGE = null;
    public static Image DESKTOP_IMAGE = null;
    public static final String VERSION_TEXT = "3.0 RC1 (20130528)";
    public static String PROPERTIES_OPTION = null;
    public static String PLUG_IN_DIRECTORY_OPTION = null;
    public static String I18N_FILE_OPTION = null;
    public static String CONFIG_FILE_OPTION = null;
    public static ImageIcon APP_ICON = null;
    public static String I18N_SETLOCALE = "";
    public static String ROOT_LOG_LEVEL = "";
    public static final String USER_DIR = System.getProperty("user.dir");
    public static String PROJECT_DIRECT_LOAD_OPTION = null;
    public static String PROJECT_DIRECT_LOAD_PATH = null;
    public static String APPLICATION_DIRECT_LOAD_OPTION = null;
    public static String APPLICATION_DIRECT_LOAD_ID = null;
    private static final boolean VERBOSE_SPLASH_PROGRESS_REPORTING = true;
    private Properties configProperties = new Properties();
    private static Class<? extends ProgressMonitor> progressMonitorClass = SingleLineProgressMonitor.class;
    private static CommandLine commandLine;
    private WorkbenchContext context = new JUMPWorkbenchContext(this);
    private DataManager dataManager;
    private TaskManager taskManager;
    private PrintLayoutManager printLayoutManager;
    private ProjectManagerFrame projectManagerFrame;
    private static WorkbenchFrame frame;
    private DriverManager driverManager = new DriverManager(frame);
    private WorkbenchProperties dummyProperties;
    private WorkbenchProperties properties = this.dummyProperties = new WorkbenchProperties(){

        @Override
        public List<Class<?>> getPlugInClasses() {
            return new ArrayList();
        }

        @Override
        public List<Class<?>> getInputDriverClasses() {
            return new ArrayList();
        }

        @Override
        public List<Class<?>> getOutputDriverClasses() {
            return new ArrayList();
        }

        @Override
        public List<Class<?>> getConfigurationClasses() {
            return new ArrayList();
        }
    };
    private PlugInManager plugInManager;
    private static Blackboard blackboard;
    private static List<EnableCheck> previousGenericEnableChecks;
    private static List<EnableCheck> postGenericEnableChecks;

    static {
        blackboard = new Blackboard();
        previousGenericEnableChecks = new ArrayList<EnableCheck>();
        postGenericEnableChecks = new ArrayList<EnableCheck>();
    }

    public JUMPWorkbench(String title, ImageIcon icon, final SplashWindow s, TaskMonitor monitor) throws Exception {
        if (commandLine.hasOption(PROJECT_DIRECT_LOAD_OPTION)) {
            PROJECT_DIRECT_LOAD_PATH = commandLine.getOption(PROJECT_DIRECT_LOAD_OPTION).getArg(0);
        }
        if (commandLine.hasOption(APPLICATION_DIRECT_LOAD_OPTION)) {
            APPLICATION_DIRECT_LOAD_ID = commandLine.getOption(APPLICATION_DIRECT_LOAD_OPTION).getArg(0);
        }
        StyleFactory.createStyleFactory();
        FilterFactory.createFilterFactory();
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            Renderer.getUniqueInstance();
        }
        catch (Error e) {
            LOGGER.error((Object)"", (Throwable)e);
            s.setVisible(false);
            DialogFactory.showErrorDialog(null, I18N.getString("JUMPWorkbench.the-java-virtual-machine-that-executes-the-application-does-not-have-JAI-installed-the-program-can-not-start"), I18N.getString("JUMPWorkbench.error-starting-the-application"));
            System.exit(-1);
        }
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.loading-spatial-reference-systems-libraries")) + "...");
        LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.loading-spatial-reference-systems-libraries")) + "..."));
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS("EPSG:4326");
            crs.isProjected();
            LOGGER.info((Object)I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.the-spatial-reference-systems-libraries-have-been-successfully-loaded"));
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            s.setVisible(false);
            DialogFactory.showErrorDialog(null, String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.spatial-reference-systems-libraries-couldnt-be-correctly-loaded")) + ":\n" + e.getMessage(), I18N.getString("JUMPWorkbench.error-starting-the-application"));
            System.exit(-1);
        }
        monitor.report(I18N.getString("JUMPWorkbench.creating-workbench-frame"));
        title = I18N.getString("JUMPWorkbench.app-name");
        title = String.valueOf(title) + " - v.3.0 RC1 (20130528) ";
        frame = new WorkbenchFrame(title, icon, this.context);
        frame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowOpened(WindowEvent e) {
                s.setVisible(false);
            }
        });
        if (commandLine.hasOption(PROPERTIES_OPTION)) {
            monitor.report(I18N.getString("JUMPWorkbench.reading-properties-file"));
            File propertiesFile = new File(commandLine.getOption(PROPERTIES_OPTION).getArg(0));
            if (!propertiesFile.canRead()) {
                LOGGER.info((Object)I18N.getMessage("JUMPWorkbench.no-properties-file-{0}", new Object[]{propertiesFile}));
            } else {
                this.properties = new WorkbenchPropertiesFile(propertiesFile, frame);
            }
        }
        monitor.report(I18N.getString("JUMPWorkbench.loading-config-file"));
        if (commandLine.hasOption(CONFIG_FILE_OPTION)) {
            File configFile = new File(commandLine.getOption(CONFIG_FILE_OPTION).getArg(0));
            if (!configFile.canRead()) {
                LOGGER.info((Object)I18N.getMessage("JUMPWorkbench.no-config-file-{0}", new Object[]{configFile}));
            } else {
                FileInputStream stream = null;
                try {
                    stream = new FileInputStream(configFile);
                    this.configProperties.load(stream);
                }
                finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        }
        this.plugInManager = new PlugInManager(this.context, commandLine.hasOption(PLUG_IN_DIRECTORY_OPTION) ? new File(commandLine.getOption(PLUG_IN_DIRECTORY_OPTION).getArg(0)) : null, monitor);
        this.driverManager.loadDrivers(this.properties);
        this.initializeManagers();
    }

    public static void main(String[] args) throws Exception {
        IniFileLoader.loadIniFile();
        JUMPWorkbench.parseCommandLine(args);
        if (StringUtils.isNotEmpty((String)I18N_SETLOCALE)) {
            I18N.loadLanguageFile(I18N_SETLOCALE);
            Locale.setDefault(I18N.getLocale());
        } else if (commandLine.hasOption(I18N_FILE_OPTION) && StringUtils.isEmpty((String)I18N_SETLOCALE)) {
            I18N.loadLanguageFile(commandLine.getOption(I18N_FILE_OPTION).getArg(0));
            I18N_SETLOCALE = commandLine.getOption(I18N_FILE_OPTION).getArg(0);
            Locale.setDefault(I18N.getLocale());
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String appName = I18N.getString("JUMPWorkbench.app-name");
        LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.starting-application-{0}", new Object[]{String.valueOf(appName) + " " + VERSION_TEXT + " - " + dateFormat.format(new Date(System.currentTimeMillis()))}));
        LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.Working-directory")) + ": " + new File("").getCanonicalPath()));
        LOGGER.info((Object)(String.valueOf(I18N.getString("workbench.ui.AboutDialog.java-version")) + " : " + System.getProperty("java.version")));
        LOGGER.info((Object)(String.valueOf(I18N.getString("workbench.ui.AboutDialog.os")) + " : " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")"));
        JUMPWorkbench.initLookAndFeel();
        PrintStream prt = new PrintStream((OutputStream)new LoggingOutputStream(Category.getRoot(), Priority.DEBUG), true);
        System.setOut(prt);
        PrintStream prtError = new PrintStream((OutputStream)new LoggingOutputStream(Category.getRoot(), Priority.ERROR), true);
        System.setErr(prtError);
        JUMPWorkbench.stablishLoggerLevel();
        JUMPWorkbench.stablishNetworkProperties();
        SplashPanel splashPanel = new SplashPanel(new ImageIcon(SPLASH_IMAGE), VERSION_TEXT);
        ProgressMonitor progressMonitor = progressMonitorClass.newInstance();
        splashPanel.add((Component)progressMonitor, new GridBagConstraints(0, 10, 1, 1, 1.0, 0.0, 18, 2, new Insets(0, 0, 0, 10), 0, 0));
        SplashWindow s = new SplashWindow(splashPanel);
        s.setVisible(true);
        ProgressMonitor monitor = progressMonitor;
        JUMPWorkbench workbench = new JUMPWorkbench("", APP_ICON, s, monitor);
        monitor.report(I18N.getString("JUMPWorkbench.building-workbench"));
        new JUMPConfiguration().configure(workbench.context, monitor);
        monitor.report(I18N.getString("JUMPWorkbench.displaying-workbench"));
        JUMPWorkbench.loadMainFrameStatus(workbench);
        workbench.getFrame().setVisible(true);
    }

    private static void stablishLoggerLevel() {
        if (StringUtils.isNotEmpty((String)ROOT_LOG_LEVEL)) {
            Level level = null;
            if (ROOT_LOG_LEVEL.equalsIgnoreCase(Level.DEBUG.toString())) {
                level = Level.DEBUG;
            } else if (ROOT_LOG_LEVEL.equalsIgnoreCase(Level.INFO.toString())) {
                level = Level.INFO;
            } else if (ROOT_LOG_LEVEL.equalsIgnoreCase(Level.WARN.toString())) {
                level = Level.WARN;
            } else if (ROOT_LOG_LEVEL.equalsIgnoreCase(Level.ERROR.toString())) {
                level = Level.ERROR;
            }
            if (level != null) {
                LOGGER.info((Object)I18N.getMessage(JUMPWorkbench.class, "setting-log-level-to-{0}", new Object[]{level.toString()}));
                Logger.getRootLogger().setLevel(level);
            }
        }
    }

    private static void stablishNetworkProperties() {
        if (System.getProperty("http.proxyUser") != null) {
            String userName = System.getProperty("http.proxyUser");
            String password = System.getProperty("http.proxyPassword");
            LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.JUMPWorkbench.setting-proxy-user-and-password-for-user")) + " " + userName));
            Authenticator.setDefault(new ProxyAuth(userName, password));
        }
        System.setProperty("http.agent", "Kosmo Desktop 3.0 RC1 (20130528)");
    }

    private void initializeManagers() {
        this.dataManager = new DataManager();
        this.taskManager = new TaskManager();
        this.printLayoutManager = new PrintLayoutManager();
        this.projectManagerFrame = new ProjectManagerFrame();
    }

    private static void loadMainFrameStatus(JUMPWorkbench workbench) {
        Blackboard blackboard = workbench.getContext().getBlackboard();
        int width = 900;
        int height = 675;
        int locationX = 0;
        int locationY = 0;
        int extendedState = 6;
        boolean alwaysOnTop = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_ALWAYS_ON_TOP_ENABLED, false);
        if (PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_REMENBER_WINDOW_STATUS_ON_CLOSE_ENABLED, false)) {
            width = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_MAIN_FRAME_WIDTH, 900);
            height = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_MAIN_FRAME_HEIGTH, 675);
            locationX = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_MAIN_FRAME_LOCATION_X, 0);
            locationY = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_MAIN_FRAME_LOCATION_Y, 0);
            extendedState = PersistentBlackboardPlugIn.get(blackboard).get(ConfigViewDataPanel.KEY_MAIN_FRAME_EXTANDED_STATE, 6);
        }
        workbench.getFrame().setSize(width, height);
        workbench.getFrame().setLocation(locationX, locationY);
        workbench.getFrame().setExtendedState(extendedState);
        workbench.getFrame().setAlwaysOnTop(alwaysOnTop);
    }

    public static void ampliaTamEscritorio(JUMPWorkbench workbench) {
        workbench.getFrame().setExtendedState(6);
    }

    private static void initLookAndFeel() throws Exception {
        try {
            if (UIManager.getLookAndFeel() != null && UIManager.getLookAndFeel().getClass().getName().equals(UIManager.getSystemLookAndFeelClassName())) {
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{UIManager.getSystemLookAndFeelClassName()}));
                return;
            }
            String laf = System.getProperty("swing.defaultlaf");
            if (laf != null) {
                UIManager.setLookAndFeel(laf);
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{laf}));
            } else {
                JUMPWorkbench.loadDefaultLookAndFeel();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{UIManager.getSystemLookAndFeelClassName()}));
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)e);
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{UIManager.getCrossPlatformLookAndFeelClassName()}));
            }
        }
    }

    private static void loadDefaultLookAndFeel() throws Exception {
        if (!LookUtils.IS_OS_WINDOWS) {
            PlasticXPLookAndFeel laf = new PlasticXPLookAndFeel();
            UIManager.setLookAndFeel((LookAndFeel)laf);
            LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{laf.getClass().getName()}));
        } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.JUMPWorkbench.using-look-and-feel-{0}", new Object[]{UIManager.getSystemLookAndFeelClassName()}));
        }
    }

    public DriverManager getDriverManager() {
        return this.driverManager;
    }

    public WorkbenchProperties getProperties() {
        return this.properties;
    }

    public WorkbenchFrame getFrame() {
        return frame;
    }

    public WorkbenchContext getContext() {
        return this.context;
    }

    private static void parseCommandLine(String[] args) throws WorkbenchException {
        commandLine = new CommandLine('-');
        commandLine.addOptionSpec(new OptionSpec(PROPERTIES_OPTION, 1));
        commandLine.addOptionSpec(new OptionSpec(PLUG_IN_DIRECTORY_OPTION, 1));
        commandLine.addOptionSpec(new OptionSpec(I18N_FILE_OPTION, 1));
        commandLine.addOptionSpec(new OptionSpec(PROJECT_DIRECT_LOAD_OPTION, 1));
        commandLine.addOptionSpec(new OptionSpec(APPLICATION_DIRECT_LOAD_OPTION, 1));
        try {
            commandLine.parse(args);
        }
        catch (ParseException e) {
            throw new WorkbenchException(I18N.getMessage("JUMPWorkbench.problem-parsing-command-line-{0}", new Object[]{e.toString()}));
        }
    }

    public PlugInManager getPlugInManager() {
        return this.plugInManager;
    }

    public static Blackboard getBlackboard() {
        return blackboard;
    }

    public Properties getConfiguration() {
        return this.configProperties;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    public PrintLayoutManager getPrintLayoutManager() {
        return this.printLayoutManager;
    }

    public ProjectManagerFrame getProjectManagerFrame() {
        return this.projectManagerFrame;
    }

    public static WorkbenchFrame getFrameInstance() {
        return frame;
    }

    public static Layer getLayer(String name) {
        if (frame.getContext().getLayerManager() == null) {
            return null;
        }
        return frame.getContext().getLayerManager().getLayer(name);
    }

    public static Layer getHiddenLayer(String name) {
        if (frame.getContext().getLayerManager() == null) {
            return null;
        }
        return frame.getContext().getLayerManager().getHideLayer(name);
    }

    public static Table getTable(String name) {
        return frame.getContext().getDataManager().getTable(name);
    }

    public static EnableCheck getPreGenericCheck() {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (EnableCheck currentCheck : previousGenericEnableChecks) {
                    String errorMessage = currentCheck.check(component);
                    if (!StringUtils.isNotEmpty((String)errorMessage)) continue;
                    return errorMessage;
                }
                return null;
            }
        };
        return check;
    }

    public static EnableCheck getPostGenericCheck() {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (EnableCheck currentCheck : postGenericEnableChecks) {
                    String errorMessage = currentCheck.check(component);
                    if (!StringUtils.isNotEmpty((String)errorMessage)) continue;
                    return errorMessage;
                }
                return null;
            }
        };
        return check;
    }

    public static void addGenericCheck(EnableCheck check, boolean previousCheck) {
        if (previousCheck) {
            if (check != null && !previousGenericEnableChecks.contains(check)) {
                previousGenericEnableChecks.add(check);
            }
        } else if (check != null && !postGenericEnableChecks.contains(check)) {
            postGenericEnableChecks.add(check);
        }
    }

    public static void removeGenericCheck(EnableCheck check) {
        previousGenericEnableChecks.remove(check);
        postGenericEnableChecks.remove(check);
    }

    public static void clearGenericChecks() {
        previousGenericEnableChecks.clear();
        postGenericEnableChecks.clear();
    }

    private static abstract class ProgressMonitor
    extends JPanel
    implements TaskMonitor {
        private static final long serialVersionUID = 1L;
        private Component component;

        public ProgressMonitor(Component component) {
            this.component = component;
            this.setLayout(new BorderLayout());
            this.add(component, "Center");
            this.setOpaque(false);
        }

        protected Component getComponent() {
            return this.component;
        }

        protected abstract void addText(String var1);

        @Override
        public void report(String description) {
            this.addText(description);
        }

        @Override
        public void report(int itemsDone, int totalItems, String itemDescription) {
            this.addText(String.valueOf(itemsDone) + " / " + totalItems + " " + itemDescription);
        }

        @Override
        public void report(Exception exception) {
            this.addText(StringUtil.toFriendlyName(exception.getClass().getName()));
        }

        @Override
        public void allowCancellationRequests() {
        }

        @Override
        public boolean isCancelRequested() {
            return false;
        }
    }

    private static class SingleLineProgressMonitor
    extends ProgressMonitor {
        private static final long serialVersionUID = 1L;

        public SingleLineProgressMonitor() {
            super(new JLabel(" "));
            ((JLabel)this.getComponent()).setFont(((JLabel)this.getComponent()).getFont().deriveFont(1));
            ((JLabel)this.getComponent()).setHorizontalAlignment(2);
        }

        @Override
        protected void addText(String s) {
            ((JLabel)this.getComponent()).setText(s);
        }
    }
}

