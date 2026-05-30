/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.factory.FactoryCreator
 *  org.geotools.factory.FactoryRegistry
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import es.kosmo.core.dao.datasource.filedatasource.dxf.Dxf2000Writer;
import es.kosmo.core.dao.datasource.filedatasource.dxf.DxfRel14Writer;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import javax.imageio.spi.ServiceRegistry;
import org.apache.log4j.Logger;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;

public final class DxfWriterFactoryFinder {
    private static final Logger LOGGER = Logger.getLogger(DxfWriterFactoryFinder.class);
    private static FactoryRegistry registry;

    private DxfWriterFactoryFinder() {
    }

    public static IDxfWriter getWriter(String version, Writer writer, String encoding) {
        Iterator it = DxfWriterFactoryFinder.getServiceRegistry().getServiceProviders(IDxfWriter.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
        while (it.hasNext()) {
            IDxfWriter candidate = (IDxfWriter)it.next();
            LOGGER.debug((Object)("Evaluating candidate: " + candidate.getDescription()));
            if (!candidate.supportsVersion(version)) continue;
            LOGGER.debug((Object)("Chosen candidate: " + candidate.getDescription()));
            return candidate.newInstance(writer, encoding);
        }
        return null;
    }

    private static FactoryRegistry getServiceRegistry() {
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(IDxfWriter.class));
            registry.registerServiceProvider((Object)new DxfRel14Writer(), IDxfWriter.class);
            registry.registerServiceProvider((Object)new Dxf2000Writer(), IDxfWriter.class);
        }
        return registry;
    }

    public static synchronized void scanForPlugins() {
        DxfWriterFactoryFinder.getServiceRegistry().scanForPlugins();
    }

    public static String[] getAvailableWriterDescriptions() {
        TreeSet<String> availableWriterVersions = new TreeSet<String>();
        Iterator it = DxfWriterFactoryFinder.getServiceRegistry().getServiceProviders(IDxfWriter.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
        while (it.hasNext()) {
            IDxfWriter candidate = (IDxfWriter)it.next();
            availableWriterVersions.add(candidate.getDescription());
        }
        return availableWriterVersions.toArray(new String[0]);
    }
}

