/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public abstract class SymbolizerChooserDialog
extends JDialog
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public SymbolizerChooserDialog() throws HeadlessException {
    }

    public SymbolizerChooserDialog(Dialog owner) throws HeadlessException {
        super(owner);
    }

    public SymbolizerChooserDialog(Dialog owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    public SymbolizerChooserDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    public SymbolizerChooserDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    public SymbolizerChooserDialog(Dialog owner, String title) throws HeadlessException {
        super(owner, title);
    }

    public SymbolizerChooserDialog(Dialog owner, String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
    }

    public SymbolizerChooserDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title);
    }

    public SymbolizerChooserDialog(Frame owner, String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
    }

    public SymbolizerChooserDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException {
        super(owner, title, modal, gc);
    }

    public SymbolizerChooserDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public abstract SymbolizerType getSelectionCode();

    public Symbolizer getSelectedSymbolizer() {
        Symbolizer s = null;
        switch (this.getSelectionCode()) {
            case POINT: {
                s = styleBuilder.createPointSymbolizer();
                break;
            }
            case LINE: {
                s = styleBuilder.createLineSymbolizer();
                break;
            }
            case POLYGON: {
                s = styleBuilder.createPolygonSymbolizer();
                break;
            }
            case TEXT: {
                s = styleBuilder.createTextSymbolizer();
                break;
            }
            case RASTER: {
                s = styleBuilder.createRasterSymbolizer();
                break;
            }
            default: {
                throw new RuntimeException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog.this-should-not-happen"));
            }
        }
        return s;
    }

    public abstract boolean exitOk();

    protected static enum SymbolizerType {
        POINT,
        POLYGON,
        LINE,
        TEXT,
        RASTER;

    }
}

