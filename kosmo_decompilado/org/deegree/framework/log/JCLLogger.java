/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.deegree.framework.log.LoggerService
 */
package org.deegree.framework.log;

import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.framework.log.LoggerService;
import org.saig.jump.lang.I18N;

public class JCLLogger
extends LoggerService {
    Log log;
    private static String LOG4J_PROP_FILE;
    private static String LOG4JLOGGER_CLASS;

    static {
        URL urlToLog4jProps;
        LOG4J_PROP_FILE = "log4j.properties";
        LOG4JLOGGER_CLASS = "org.apache.commons.logging.impl.Log4JLogger";
        Log log = LogFactory.getLog(JCLLogger.class);
        if (log.getClass().getName().equals(LOG4JLOGGER_CLASS) && (urlToLog4jProps = JCLLogger.class.getResource("/" + LOG4J_PROP_FILE)) == null) {
            URL uRL = JCLLogger.class.getResource(LOG4J_PROP_FILE);
        }
    }

    public void bindClass(String name) {
        this.log = LogFactory.getLog((String)name);
    }

    public void bindClass(Class name) {
        this.log = LogFactory.getLog((Class)name);
    }

    public int getLevel() {
        if (this.log.isDebugEnabled() || this.log.isTraceEnabled()) {
            return 0;
        }
        if (this.log.isInfoEnabled()) {
            return 1;
        }
        if (this.log.isWarnEnabled()) {
            return 2;
        }
        return 3;
    }

    public void setLevel(int level) {
        this.log.error((Object)(String.valueOf(I18N.getString("org.deegree.framework.log.JCLLogger.can-not-change-log-level-at-runtime")) + ". " + I18N.getString("org.deegree.framework.log.JCLLogger.use-the-appropriate-properties-file-for-configuration") + "."));
    }

    public boolean isDebug() {
        return this.log.isDebugEnabled();
    }

    public void logDebug(String message) {
        this.log.debug((Object)message);
    }

    public void logDebug(String message, Throwable e) {
        this.log.debug((Object)message, e);
    }

    public void logDebug(String message, Object tracableObject) {
        this.log.debug((Object)(String.valueOf(message) + ": " + tracableObject));
    }

    public void logDebug(String message, Object ... tracableObjects) {
        if (this.log.isDebugEnabled()) {
            this.log.debug((Object)this.stringFromObjects(message, tracableObjects));
        }
    }

    public void logError(String message) {
        this.log.error((Object)message);
    }

    public void logError(String message, Throwable e) {
        this.log.error((Object)message, e);
    }

    public void logInfo(String message) {
        this.log.info((Object)message);
    }

    public void logInfo(String message, Throwable e) {
        this.log.info((Object)message, e);
    }

    public void logInfo(String message, Object tracableObject) {
        this.log.info((Object)(String.valueOf(message) + ": " + tracableObject));
    }

    public void logInfo(String message, Object ... tracableObject) {
        if (this.log.isInfoEnabled()) {
            this.log.info((Object)this.stringFromObjects(message, tracableObject));
        }
    }

    public void logWarning(String message) {
        this.log.warn((Object)message);
    }

    public void logWarning(String message, Throwable e) {
        this.log.warn((Object)message, e);
    }

    public void log(int priority, String message, Throwable ex) {
        this.logDebug(message, ex);
    }

    public void log(int priority, String message, Object source, Throwable ex) {
        this.logDebug(message, ex);
    }

    private String stringFromObjects(String message, Object ... objects) {
        StringBuilder sb = new StringBuilder(message);
        Object[] objectArray = objects;
        int n = objects.length;
        int n2 = 0;
        while (n2 < n) {
            Object part = objectArray[n2];
            sb.append(' ').append(part.toString());
            ++n2;
        }
        return sb.toString();
    }
}

