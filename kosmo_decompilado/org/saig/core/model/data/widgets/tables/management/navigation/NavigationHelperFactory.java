/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.context.GenericContext;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.FeaturesListNavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.INavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.LayerNavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.NullNavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.TableDataSourceNavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.TableNavigationHelper;
import org.saig.jump.lang.I18N;

public class NavigationHelperFactory {
    public static Logger LOGGER = Logger.getLogger(NavigationHelperFactory.class);

    public static INavigationHelper getNavigationHelper(String sourceName) {
        Table table = null;
        Layer layer = null;
        TableDBRecordDataSource tableDS = null;
        try {
            table = JUMPWorkbench.getTable(sourceName);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            layer = JUMPWorkbench.getLayer(sourceName);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            tableDS = GenericContext.getGenericContext().getTableDataSource(sourceName);
        }
        catch (Exception exception) {
            // empty catch block
        }
        AbstractNavigationHelper navHelper = null;
        if (table != null) {
            navHelper = new TableNavigationHelper(table);
        } else if (layer != null) {
            navHelper = new LayerNavigationHelper(layer);
        } else if (tableDS != null) {
            navHelper = new TableDataSourceNavigationHelper(tableDS);
        } else {
            navHelper = new NullNavigationHelper();
            String name = sourceName != null ? sourceName : "null";
            LOGGER.warn((Object)I18N.getMessage(NavigationHelperFactory.class, "there-is-no-proper-inavigationhelper-to-this-type-of-data-it-will-be-used-nullnavigationhelper-the-name-of-the-requested-data-is-{0}", new Object[]{name}));
        }
        return navHelper;
    }

    public static INavigationHelper getNavigationHelper(List list) {
        if (CollectionUtils.isEmpty((Collection)list)) {
            return new NullNavigationHelper();
        }
        return new FeaturesListNavigationHelper(list);
    }
}

