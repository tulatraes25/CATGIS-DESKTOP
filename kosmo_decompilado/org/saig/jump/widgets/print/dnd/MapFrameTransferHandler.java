/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.saig.jump.widgets.print.elements.map.MapFrame;

public class MapFrameTransferHandler
implements Transferable {
    BufferedImage image;
    public static DataFlavor MAP_FRAME_FLAVOR;

    static {
        try {
            MAP_FRAME_FLAVOR = new DataFlavor("image/x-map-frame;class=" + MapFrame.class.getName());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public MapFrameTransferHandler(BufferedImage transferImage) {
        this.image = transferImage;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{MAP_FRAME_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.isMimeTypeEqual(MAP_FRAME_FLAVOR.getMimeType());
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(MAP_FRAME_FLAVOR)) {
            return this.image;
        }
        return new UnsupportedFlavorException(flavor);
    }
}

