/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.factory.FactoryCreator
 *  org.geotools.factory.FactoryRegistry
 */
package org.saig.core.renderer.style;

import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;
import org.apache.log4j.Logger;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.saig.core.renderer.style.ExternalGraphicFactory;
import org.saig.core.renderer.style.ImageGraphicFactory;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.ShapeMarkFactory;
import org.saig.core.renderer.style.TTFMarkFactory;
import org.saig.core.renderer.style.WellKnownMarkFactory;

public final class DynamicSymbolFactoryFinder {
    private static final Logger LOGGER = Logger.getLogger(DynamicSymbolFactoryFinder.class);
    private static FactoryRegistry registry;

    private DynamicSymbolFactoryFinder() {
    }

    public static synchronized Iterator<MarkFactory> getMarkFactories() {
        return DynamicSymbolFactoryFinder.getServiceRegistry().getServiceProviders(MarkFactory.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
    }

    public static synchronized Iterator<ExternalGraphicFactory> getExternalGraphicFactories() {
        return DynamicSymbolFactoryFinder.getServiceRegistry().getServiceProviders(ExternalGraphicFactory.class, new ServiceRegistry.Filter(){

            @Override
            public boolean filter(Object provider) {
                return true;
            }
        }, false);
    }

    private static FactoryRegistry getServiceRegistry() {
        assert (Thread.holdsLock(DynamicSymbolFactoryFinder.class));
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(MarkFactory.class, ExternalGraphicFactory.class));
            registry.registerServiceProvider((Object)new WellKnownMarkFactory(), MarkFactory.class);
            registry.registerServiceProvider((Object)new ShapeMarkFactory(), MarkFactory.class);
            registry.registerServiceProvider((Object)new TTFMarkFactory(), MarkFactory.class);
            registry.registerServiceProvider((Object)new ImageGraphicFactory(), ExternalGraphicFactory.class);
        }
        return registry;
    }

    public static synchronized void scanForPlugins() {
        DynamicSymbolFactoryFinder.getServiceRegistry().scanForPlugins();
    }
}

