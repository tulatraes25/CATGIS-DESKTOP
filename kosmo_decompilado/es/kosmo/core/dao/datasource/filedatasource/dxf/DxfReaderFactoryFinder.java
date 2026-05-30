/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.factory.FactoryCreator
 *  org.geotools.factory.FactoryRegistry
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import es.kosmo.core.dao.datasource.filedatasource.dxf.Dxf2000Reader;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import javax.imageio.spi.ServiceRegistry;
import org.apache.log4j.Logger;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;

public final class DxfReaderFactoryFinder {
    private static final Logger LOGGER = Logger.getLogger(DxfReaderFactoryFinder.class);
    private static FactoryRegistry registry;

    private DxfReaderFactoryFinder() {
    }

    public static IDxfReader getReader(String version, Reader reader, String encoding) {
        Iterator it = DxfReaderFactoryFinder.getServiceRegistry().getServiceProviders(IDxfReader.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
        while (it.hasNext()) {
            IDxfReader candidate = (IDxfReader)it.next();
            LOGGER.debug((Object)("Evaluating candidate: " + candidate.getDescription()));
            if (!candidate.supportsVersion(version)) continue;
            LOGGER.debug((Object)("Chosen candidate: " + candidate.getDescription()));
            return candidate.newInstance(reader, encoding);
        }
        return null;
    }

    private static FactoryRegistry getServiceRegistry() {
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(IDxfReader.class));
            registry.registerServiceProvider((Object)new Dxf2000Reader(), IDxfReader.class);
        }
        return registry;
    }

    public static synchronized void scanForPlugins() {
        DxfReaderFactoryFinder.getServiceRegistry().scanForPlugins();
    }

    public static String[] getAvailableReaderDescriptions() {
        TreeSet<String> availableReaderVersions = new TreeSet<String>();
        Iterator it = DxfReaderFactoryFinder.getServiceRegistry().getServiceProviders(IDxfReader.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
        while (it.hasNext()) {
            IDxfReader candidate = (IDxfReader)it.next();
            availableReaderVersions.add(candidate.getDescription());
        }
        return availableReaderVersions.toArray(new String[0]);
    }
}

