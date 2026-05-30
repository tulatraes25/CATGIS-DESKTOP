/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public abstract class AbstractTransferable
implements Transferable {
    private final DataFlavor[] flavors;

    public AbstractTransferable(DataFlavor[] flavors) {
        this.flavors = flavors;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[])this.flavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        int i = 0;
        while (i < this.flavors.length) {
            if (flavor.equals(this.flavors[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public abstract Object getTransferData(DataFlavor var1) throws UnsupportedFlavorException, IOException;
}

