/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.gui.dialog.ImportNewCrsDialog;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class ImportNewCrsDialogListener
implements ActionListener,
ListSelectionListener,
MouseListener {
    ImportNewCrsDialog dialog = null;

    public ImportNewCrsDialogListener(ImportNewCrsDialog d) {
        this.dialog = d;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.dialog.getJButtonAccept()) {
            DialogManager.closeJDialog(this.dialog);
        }
        if (e.getSource() == this.dialog.getJButtonCancel()) {
            this.dialog.setCode(-1);
            DialogManager.closeJDialog(this.dialog);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == this.dialog.getEpsgPanel().getJTable().getSelectionModel()) {
            String[] not_soported = new String[6];
            boolean soported = true;
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (this.dialog.getOption().equals(CRSI18NConstants.IMPORT_PROJECTION_KEY)) {
                not_soported[0] = "engineering";
                not_soported[1] = "vertical";
                not_soported[2] = "compound";
                not_soported[3] = "geocentric";
                not_soported[4] = "geographic 3D";
                not_soported[5] = "geographic 2D";
            } else {
                not_soported[0] = "engineering";
                not_soported[1] = "vertical";
                not_soported[2] = "compound";
                not_soported[3] = "geocentric";
                not_soported[4] = "geographic 3D";
            }
            if (lsm.isSelectionEmpty()) {
                this.dialog.getEpsgPanel().setCodeCRS(-1);
                this.dialog.getJButtonAccept().setEnabled(false);
                this.dialog.getEpsgPanel().getInfoCrs().setEnabled(false);
                this.dialog.setCode(-1);
            } else {
                this.dialog.getEpsgPanel().selectedRowTable = lsm.getMinSelectionIndex();
                String crs_kind = (String)this.dialog.getEpsgPanel().sorter.getValueAt(this.dialog.getEpsgPanel().selectedRowTable, 2);
                int i = 0;
                while (i < not_soported.length) {
                    if (crs_kind.equals(not_soported[i])) {
                        soported = false;
                    }
                    ++i;
                }
                if (soported) {
                    int code = Integer.parseInt((String)this.dialog.getEpsgPanel().sorter.getValueAt(this.dialog.getEpsgPanel().selectedRowTable, 0));
                    this.dialog.setCode(code);
                    this.dialog.getEpsgPanel().setCodeCRS(code);
                    this.dialog.getJButtonAccept().setEnabled(true);
                    this.dialog.getEpsgPanel().getInfoCrs().setEnabled(true);
                } else {
                    if (this.dialog.getOption().equals(CRSI18NConstants.IMPORT_PROJECTION_KEY)) {
                        DialogFactory.showWarningDialog(this.dialog, CRSI18NConstants.CRS_NOT_PROJECTED_KEY, CRSI18NConstants.WARNING_KEY);
                    } else {
                        DialogFactory.showWarningDialog(this.dialog, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                    }
                    this.dialog.setCode(-1);
                    this.dialog.getEpsgPanel().setCodeCRS(-1);
                    this.dialog.getJButtonAccept().setEnabled(false);
                    this.dialog.getEpsgPanel().getInfoCrs().setEnabled(false);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.dialog.getEpsgPanel().getJTable() && e.getClickCount() == 2) {
            DialogManager.closeJDialog(this.dialog);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

