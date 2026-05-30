/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.io.WKTWriter
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.AbstractTransferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CollectionOfFeaturesTransferable
extends AbstractTransferable {
    public static final DataFlavor COLLECTION_OF_FEATURES_FLAVOR = new DataFlavor((Class)Collection.class, "Collection of Features"){

        @Override
        public boolean equals(DataFlavor that) {
            return super.equals(that) && this.getHumanPresentableName().equals(that.getHumanPresentableName());
        }
    };
    private static final DataFlavor[] flavors = new DataFlavor[]{DataFlavor.stringFlavor, COLLECTION_OF_FEATURES_FLAVOR};
    private Collection<Feature> features;
    private WKTWriter writer = new WKTWriter();

    public CollectionOfFeaturesTransferable(Collection<Feature> features) {
        super(flavors);
        this.features = new ArrayList<Feature>(features);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(COLLECTION_OF_FEATURES_FLAVOR)) {
            return Collections.unmodifiableCollection(this.features);
        }
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return this.toString(this.features);
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private String toString(Collection<Feature> features) {
        StringBuffer b = new StringBuffer();
        for (Feature feature : features) {
            b.append(String.valueOf(this.writer.writeFormatted(feature.getGeometry())) + System.getProperty("line.separator") + System.getProperty("line.separator"));
        }
        return b.toString();
    }
}

