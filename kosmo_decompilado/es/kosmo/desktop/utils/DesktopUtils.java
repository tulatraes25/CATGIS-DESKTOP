/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class DesktopUtils {
    private static final Logger LOGGER = Logger.getLogger(DesktopUtils.class);
    private static final String OS_WINDOWS = "Windows";
    private static final String[] UNIX_BROWSE_CMDS = new String[]{"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "w3m", "lynx", "www-browser"};
    private static final String[] UNIX_OPEN_CMDS = new String[]{"run-mailcap", "pager", "less", "more"};

    public static void open(File file) throws Exception {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            String osName = System.getProperty("os.name");
            LOGGER.debug((Object)("Opening " + file + " for OS " + osName));
            if (StringUtils.startsWithIgnoreCase((String)osName, (String)OS_WINDOWS)) {
                DesktopUtils.openWindows(file);
            } else {
                DesktopUtils.openLinux(file);
            }
        }
    }

    public static void browse(URL url) throws Exception {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url.toURI());
        } else {
            String osName = System.getProperty("os.name");
            LOGGER.debug((Object)("Launching " + url + " for OS " + osName));
            if (StringUtils.startsWithIgnoreCase((String)osName, (String)OS_WINDOWS)) {
                DesktopUtils.browseWindows(url);
            } else {
                DesktopUtils.browseLinux(url);
            }
        }
    }

    private static void browseWindows(URL url) throws IOException {
        LOGGER.debug((Object)"Windows - Invoking rundll32");
        Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()});
    }

    private static void browseLinux(URL url) throws IOException {
        String[] stringArray = UNIX_BROWSE_CMDS;
        int n = UNIX_BROWSE_CMDS.length;
        int n2 = 0;
        while (n2 < n) {
            String cmd = stringArray[n2];
            LOGGER.debug((Object)("Linux -  Looking for " + cmd));
            if (DesktopUtils.unixCommandExists(cmd)) {
                LOGGER.debug((Object)("Linux - Found " + cmd));
                Runtime.getRuntime().exec(new String[]{cmd, url.toString()});
                return;
            }
            ++n2;
        }
        throw new IOException("Could not find a suitable web browser");
    }

    private static boolean unixCommandExists(String cmd) throws IOException {
        Process whichProcess = Runtime.getRuntime().exec(new String[]{"which", cmd});
        boolean finished = false;
        do {
            try {
                whichProcess.waitFor();
                finished = true;
            }
            catch (InterruptedException e) {
                LOGGER.warn((Object)"Interrupted waiting for which to complete", (Throwable)e);
            }
        } while (!finished);
        return whichProcess.exitValue() == 0;
    }

    private static void openWindows(File file) throws IOException {
        LOGGER.debug((Object)"Windows invoking rundll32");
        Runtime.getRuntime().exec(new String[]{"rundll32", "shell32.dll,ShellExec_RunDLL", file.getAbsolutePath()});
    }

    private static void openLinux(File file) throws IOException {
        String[] stringArray = UNIX_OPEN_CMDS;
        int n = UNIX_OPEN_CMDS.length;
        int n2 = 0;
        while (n2 < n) {
            String cmd = stringArray[n2];
            LOGGER.debug((Object)("Linux - Looking for " + cmd));
            if (DesktopUtils.unixCommandExists(cmd)) {
                LOGGER.debug((Object)("Linux - Found " + cmd));
                Runtime.getRuntime().exec(new String[]{cmd, file.getAbsolutePath()});
                return;
            }
            ++n2;
        }
        throw new IOException("Could not find a suitable viewer");
    }
}

