/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.jump.widgets.documents.ManageDocumentsController;
import org.saig.jump.widgets.documents.SelectDocumentsDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class SelectDocumentsController
implements ListSelectionListener,
ActionListener {
    private static final Logger LOGGER = Logger.getLogger(SelectDocumentsController.class);
    private final String documentManagerKey;
    private final SelectDocumentsDialog selectDocumentsDialog;
    private final ManageDocumentsController manageDocumentsController;
    private Record[] selectedDocuments;
    private final Object idOrigDoc;
    private final boolean isEditable;

    public SelectDocumentsController(String documentManagerKey, SelectDocumentsDialog selectDocumentsDialog, Object idOrigDoc, boolean allowDuplicates, boolean editable) {
        this.documentManagerKey = documentManagerKey;
        this.selectDocumentsDialog = selectDocumentsDialog;
        this.idOrigDoc = idOrigDoc;
        this.isEditable = editable;
        this.selectedDocuments = new Record[0];
        selectDocumentsDialog.setDefaultCloseOperation(0);
        selectDocumentsDialog.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent we) {
                SelectDocumentsController.this.performExit();
            }
        });
        this.selectDocumentsDialog.getManageDocumentsPanel().getDocumentList().getSelectionModel().addListSelectionListener(this);
        this.selectDocumentsDialog.getSelectButton().addActionListener(this);
        this.selectDocumentsDialog.getExitButton().addActionListener(this);
        this.manageDocumentsController = new ManageDocumentsController(documentManagerKey, selectDocumentsDialog.getManageDocumentsPanel(), idOrigDoc, allowDuplicates, this.isEditable);
        this.performListSelectionChange();
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getSource() == this.selectDocumentsDialog.getManageDocumentsPanel().getDocumentList().getSelectionModel()) {
            this.performListSelectionChange();
        }
    }

    private void performListSelectionChange() {
        Object[] selectedValues = this.selectDocumentsDialog.getManageDocumentsPanel().getDocumentList().getSelectedValues();
        this.selectDocumentsDialog.getSelectButton().setEnabled(selectedValues.length > 0);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.selectDocumentsDialog.getSelectButton()) {
            this.performSelection();
        } else if (ae.getSource() == this.selectDocumentsDialog.getExitButton()) {
            this.performExit();
        }
    }

    private void performExit() {
        if (this.continueCheckingTemporalDocuments()) {
            this.selectedDocuments = new Record[0];
            this.selectDocumentsDialog.setVisible(false);
        }
    }

    private boolean continueCheckingTemporalDocuments() {
        boolean continuar = true;
        int numTempDoc = this.manageDocumentsController.getNumTemporalDocuments();
        if (numTempDoc > 0) {
            int option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), "Hay documentos temporales no guardados \u00bfDesea continuar? ", "Atenci\u00f3n");
            continuar = option == 0;
        }
        return continuar;
    }

    private void performSelection() {
        Object[] obj;
        if (this.continueCheckingTemporalDocuments() && (obj = this.selectDocumentsDialog.getManageDocumentsPanel().getDocumentList().getSelectedValues()).length > 0) {
            this.selectedDocuments = new Record[obj.length];
            System.arraycopy(obj, 0, this.selectedDocuments, 0, obj.length);
            this.selectDocumentsDialog.setVisible(false);
        }
    }

    public Record[] getSelectedDocuments() {
        return this.selectedDocuments;
    }
}

