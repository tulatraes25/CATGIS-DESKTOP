/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.lang;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.collections.CollectionUtils;
import sun.util.ResourceBundleEnumeration;

public class ResourceBundleFamily
extends ResourceBundle {
    protected List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
    protected int mainBundlePosition = 0;

    public void addResourceBundle(ResourceBundle bundle) {
        this.addResourceBundle(bundle, false);
    }

    public void addResourceBundle(ResourceBundle bundle, boolean isMain) {
        this.bundles.add(bundle);
        if (isMain) {
            this.mainBundlePosition = this.bundles.indexOf(bundle);
        }
    }

    public void clearBundles() {
        this.bundles.clear();
    }

    @Override
    public Enumeration<String> getKeys() {
        HashSet<String> keys = new HashSet<String>();
        for (ResourceBundle currentBundle : this.bundles) {
            CollectionUtils.addAll(keys, currentBundle.getKeys());
        }
        ResourceBundleEnumeration enumeration = new ResourceBundleEnumeration(keys, null);
        return enumeration;
    }

    @Override
    protected Object handleGetObject(String key) {
        String val = null;
        ResourceBundle rb = this.bundles.get(this.mainBundlePosition);
        try {
            val = rb.getString(key);
        }
        catch (Exception exception) {
            // empty catch block
        }
        int i = 0;
        while (i < this.bundles.size() && val == null) {
            rb = this.bundles.get(i);
            try {
                val = rb.getString(key);
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++i;
        }
        return val;
    }

    @Override
    public Locale getLocale() {
        ResourceBundle bundle = this.bundles.get(this.mainBundlePosition);
        return bundle.getLocale();
    }
}

