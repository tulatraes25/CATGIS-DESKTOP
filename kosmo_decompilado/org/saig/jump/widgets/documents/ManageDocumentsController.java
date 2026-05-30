/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.context.documents.DocumentManager;
import org.saig.core.model.data.Record;
import org.saig.jump.widgets.documents.DocumentDetailsController;
import org.saig.jump.widgets.documents.ManageDocumentsPanel;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.FilteringModel;

public class ManageDocumentsController
implements ActionListener,
ListSelectionListener {
    private static final Logger LOGGER = Logger.getLogger(ManageDocumentsController.class);
    private final String documentManagerKey;
    private final ManageDocumentsPanel manageDocumentsPanel;
    private final DocumentDetailsController documentDetailsController;
    private final Object idOrigDoc;
    private final boolean isEditable;

    public ManageDocumentsController(String documentManagerKey, ManageDocumentsPanel manageDocumentsPanel, Object idOrigDoc, boolean allowDuplicates, boolean editable) {
        this.documentManagerKey = documentManagerKey;
        this.manageDocumentsPanel = manageDocumentsPanel;
        this.idOrigDoc = idOrigDoc;
        this.isEditable = editable;
        this.manageDocumentsPanel.getDocumentDetailPanel().getSaveChangesButton().addActionListener(this);
        this.manageDocumentsPanel.getAddDocumentButton().addActionListener(this);
        this.manageDocumentsPanel.getRemoveDocumentButton().addActionListener(this);
        this.manageDocumentsPanel.getDocumentList().getSelectionModel().addListSelectionListener(this);
        this.manageDocumentsPanel.getDocumentList().setCellRenderer(new DocumentListCellRenderer());
        this.documentDetailsController = new DocumentDetailsController(documentManagerKey, manageDocumentsPanel.getDocumentDetailPanel(), idOrigDoc, allowDuplicates, this.isEditable);
        manageDocumentsPanel.getDocumentList().installJTextComponent(manageDocumentsPanel.getSearchTextField());
        this.updateDocumentList();
        this.manageDocumentsPanel.getAddDocumentButton().setEnabled(this.isEditable);
        this.manageDocumentsPanel.getRemoveDocumentButton().setEnabled(this.isEditable);
    }

    private void updateDocumentList() {
        List<Record> docRecsList = DocumentManager.getInstance(this.documentManagerKey).getDocumentRecordsByIdOrig(this.idOrigDoc);
        this.manageDocumentsPanel.getDocumentList().setModel((ListModel)new DocumentFilteringModel((Collection<Record>)docRecsList));
        this.performListSelectionChange();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.manageDocumentsPanel.getAddDocumentButton()) {
            String nombre = (String)DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), "Nombre del nuevo documento", this.manageDocumentsPanel.getAddDocumentButton().getText(), "Nuevo documento");
            if (StringUtils.isNotBlank((String)nombre)) {
                this.performAddDocument(nombre);
            }
        } else if (ae.getSource() == this.manageDocumentsPanel.getRemoveDocumentButton()) {
            this.performRemoveDocument();
        } else if (ae.getSource() == this.manageDocumentsPanel.getDocumentDetailPanel().getSaveChangesButton()) {
            this.manageDocumentsPanel.getDocumentList().repaint();
        }
    }

    private void performRemoveDocument() {
        Object[] selectedValues = this.manageDocumentsPanel.getDocumentList().getSelectedValues();
        int option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), "Se van a borrar " + selectedValues.length + " documentos. " + "Esto afectar\u00e1 a los registros relacionados con ellos. " + "Tambi\u00e9n se borrar\u00e1 su contenido y los documentos temporales no guardados." + "\n\u00bfSeguro que desea continuar?", "Atenci\u00f3n");
        if (option != 0) {
            return;
        }
        ArrayList<Record> recordsToRemove = new ArrayList<Record>(selectedValues.length);
        Object[] objectArray = selectedValues;
        int n = selectedValues.length;
        int n2 = 0;
        while (n2 < n) {
            Object obj = objectArray[n2];
            Record entDocRec = (Record)obj;
            if (entDocRec != null && entDocRec.getPrimaryKey() != null) {
                recordsToRemove.add(entDocRec);
            }
            ++n2;
        }
        DocumentManager.getInstance(this.documentManagerKey).removeDocument(recordsToRemove, true);
        this.updateDocumentList();
    }

    private void performAddDocument(String nombre) {
        Record rec = null;
        try {
            rec = new Record(DocumentManager.getInstance(this.documentManagerKey).getTableDataSource("dm_documents").getSchema());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (rec != null) {
            rec.setAttribute("nombre", (Object)nombre);
            ((FilteringModel)this.manageDocumentsPanel.getDocumentList().getModel()).addElement(rec);
            this.manageDocumentsPanel.getSearchTextField().setText(null);
            this.manageDocumentsPanel.getDocumentList().setSelectedValue(rec, true);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (!lse.getValueIsAdjusting() && lse.getSource() == this.manageDocumentsPanel.getDocumentList().getSelectionModel()) {
            this.performListSelectionChange();
        }
    }

    private void performListSelectionChange() {
        Object[] selectedValues = this.manageDocumentsPanel.getDocumentList().getSelectedValues();
        this.manageDocumentsPanel.getRemoveDocumentButton().setEnabled(selectedValues.length > 0);
        Record selectedDocument = null;
        if (selectedValues.length == 1) {
            selectedDocument = (Record)selectedValues[0];
        }
        this.documentDetailsController.setCurrentDocumentRecord(selectedDocument);
        this.manageDocumentsPanel.getRemoveDocumentButton().setEnabled(this.isEditable && selectedValues.length > 0);
    }

    public int getNumTemporalDocuments() {
        int cont = 0;
        for (Object obj : ((FilteringModel)this.manageDocumentsPanel.getDocumentList().getModel()).getFullList()) {
            Record docRec = (Record)obj;
            if (docRec.getPrimaryKey() != null) continue;
            ++cont;
        }
        return cont;
    }

    private static class DocumentFilteringModel
    extends FilteringModel<Record> {
        private static final long serialVersionUID = 1L;
        private static String[] fieldNamesToCheck = new String[]{"nombre", "observaciones", "ruta_original"};

        public DocumentFilteringModel() {
        }

        public DocumentFilteringModel(Collection<Record> documentRecords) {
            super(documentRecords);
        }

        @Override
        protected boolean elementContainString(Record element, String search) {
            boolean encontrado = false;
            int index = 0;
            while (!encontrado && index < fieldNamesToCheck.length) {
                Object attrObj = element.getAttribute(fieldNamesToCheck[index]);
                if (attrObj != null) {
                    encontrado = StringUtils.contains((String)attrObj.toString().toLowerCase(), (String)search.toLowerCase());
                }
                ++index;
            }
            return encontrado;
        }
    }

    private static class DocumentListCellRenderer
    extends JLabel
    implements ListCellRenderer {
        private static final String UNNAMED_STRING = "(Sin nombre)";

        private DocumentListCellRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Record docRec = (Record)value;
            String nombre = (String)docRec.getAttribute("nombre");
            if (StringUtils.isBlank((String)nombre)) {
                nombre = UNNAMED_STRING;
            }
            this.setText(nombre);
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            if (docRec.getPrimaryKey() != null) {
                this.setFont(list.getFont());
            } else {
                this.setFont(list.getFont().deriveFont(3));
            }
            this.setOpaque(true);
            return this;
        }
    }
}

