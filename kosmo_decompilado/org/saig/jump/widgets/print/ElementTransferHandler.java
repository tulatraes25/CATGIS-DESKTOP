/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import org.saig.jump.widgets.print.DragableJList;
import org.saig.jump.widgets.print.ElementsListModel;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class ElementTransferHandler
extends TransferHandler {
    private static final long serialVersionUID = 1L;
    DataFlavor localArrayListFlavor;
    DataFlavor serialArrayListFlavor;
    String localArrayListType = "application/x-java-jvm-local-objectref;class=org.saig.jump.widgets.print.elements.GraphicElements";
    DragableJList source = null;
    int[] indices = null;
    int addIndex = -1;
    int addCount = 0;
    int rmindex = -1;

    public ElementTransferHandler() {
        try {
            this.localArrayListFlavor = new DataFlavor(this.localArrayListType);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.serialArrayListFlavor = new DataFlavor(GraphicElements.class, "GraphicElement");
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {
        GraphicElements element;
        JList target;
        block8: {
            target = null;
            element = null;
            if (!this.canImport(c, t.getTransferDataFlavors())) {
                return false;
            }
            try {
                target = (JList)c;
                if (this.hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
                    element = (GraphicElements)t.getTransferData(this.localArrayListFlavor);
                    break block8;
                }
                if (this.hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
                    element = (GraphicElements)t.getTransferData(this.serialArrayListFlavor);
                    break block8;
                }
                return false;
            }
            catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                return false;
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        int index = target.getSelectedIndex();
        if (index == -1) {
            return false;
        }
        ElementsListModel listModel = (ElementsListModel)target.getModel();
        listModel.removeElement(this.rmindex);
        if (index > this.rmindex) {
            --index;
        }
        listModel.addElementAt(index, element);
        return true;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        DragableJList list = (DragableJList)c;
        list.orderChanged();
    }

    private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
        if (this.localArrayListFlavor == null) {
            return false;
        }
        int i = 0;
        while (i < flavors.length) {
            if (flavors[i].equals(this.localArrayListFlavor)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
        if (this.serialArrayListFlavor == null) {
            return false;
        }
        int i = 0;
        while (i < flavors.length) {
            if (flavors[i].equals(this.serialArrayListFlavor)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (this.hasLocalArrayListFlavor(flavors)) {
            return true;
        }
        return this.hasSerialArrayListFlavor(flavors);
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof DragableJList) {
            this.source = (DragableJList)c;
            int index = this.source.getPressedIndex();
            if (index == -1 || index == this.source.getModel().getSize() - 1) {
                return null;
            }
            GraphicElements e = (GraphicElements)this.source.getModel().getElementAt(index);
            this.rmindex = index;
            return new ElementTransferable(e);
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return 3;
    }

    public class ElementTransferable
    implements Transferable {
        GraphicElements data;

        public ElementTransferable(GraphicElements e) {
            this.data = e;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!this.isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return this.data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ElementTransferHandler.this.localArrayListFlavor, ElementTransferHandler.this.serialArrayListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (ElementTransferHandler.this.localArrayListFlavor.equals(flavor)) {
                return true;
            }
            return ElementTransferHandler.this.serialArrayListFlavor.equals(flavor);
        }
    }
}

