package ar.com.catgis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CatgisLogger {

    private static final Logger LOG = LogManager.getLogger("ar.com.catgis");

    private CatgisLogger() {
    }

    public static void error(String context, Throwable ex) {
        if (context != null && !context.isBlank()) {
            LOG.error(context, ex);
        } else if (ex != null) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static void warn(String context, Throwable ex) {
        if (context != null && !context.isBlank()) {
            LOG.warn(context, ex);
        } else if (ex != null) {
            LOG.warn(ex.getMessage(), ex);
        } else {
            LOG.warn(context);
        }
    }

    public static void info(String message) {
        LOG.info(message);
    }

    public static void debug(String message) {
        LOG.debug(message);
    }
}
