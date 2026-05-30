/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class GraphicElementTransferHandler
implements Transferable {
    BufferedImage image;
    GraphicElements ge;
    public static DataFlavor GRAPHIC_ELEMENTS_FLAVOR;

    static {
        try {
            GRAPHIC_ELEMENTS_FLAVOR = new DataFlavor("image/x-graphic-element;class=" + GraphicElements.class.getName());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public GraphicElementTransferHandler(BufferedImage transferImage, GraphicElements geToMove) {
        this.image = transferImage;
        this.ge = geToMove;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{GRAPHIC_ELEMENTS_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.isMimeTypeEqual(GRAPHIC_ELEMENTS_FLAVOR.getMimeType());
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(GRAPHIC_ELEMENTS_FLAVOR)) {
            return new Object[]{this.image, this.ge};
        }
        return new UnsupportedFlavorException(flavor);
    }

    public void dispose() {
        this.image = null;
        this.ge = null;
    }
}

