/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.AbstractTransferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionOfLayerablesTransferable
extends AbstractTransferable {
    public static final DataFlavor COLLECTION_OF_LAYERABLES_FLAVOR = new DataFlavor((Class)Collection.class, "Collection of Layerables"){

        @Override
        public boolean equals(DataFlavor that) {
            return super.equals(that) && this.getHumanPresentableName().equals(that.getHumanPresentableName());
        }
    };
    private static final DataFlavor[] flavors = new DataFlavor[]{DataFlavor.stringFlavor, COLLECTION_OF_LAYERABLES_FLAVOR};
    private Collection<Layerable> layerables;

    public CollectionOfLayerablesTransferable(Collection<Layerable> layerables) {
        super(flavors);
        this.layerables = new ArrayList<Layerable>(layerables);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(COLLECTION_OF_LAYERABLES_FLAVOR)) {
            return Collections.unmodifiableCollection(this.layerables);
        }
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return this.toString(new ArrayList<Layerable>(this.layerables));
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private String toString(List<Layerable> layerables) {
        StringBuffer b = new StringBuffer();
        int i = 0;
        while (i < layerables.size()) {
            Layerable layerable = layerables.get(i);
            if (i != 0) {
                b.append(", ");
            }
            b.append(layerable.getName());
            ++i;
        }
        return b.toString();
    }
}

