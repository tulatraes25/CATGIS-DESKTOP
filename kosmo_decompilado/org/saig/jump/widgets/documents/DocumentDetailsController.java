/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.context.documents.DocumentManager;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.util.RecordUtil;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.documents.DocumentDetailsPanel;
import org.saig.jump.widgets.navigation.changecontrol.ChangeControl;
import org.saig.jump.widgets.navigation.changecontrol.ChangeControllable;
import org.saig.jump.widgets.util.DialogFactory;

public class DocumentDetailsController
implements ActionListener,
DocumentListener,
ChangeControllable {
    private static final Logger LOGGER = Logger.getLogger(DocumentDetailsController.class);
    private final String documentManagerKey;
    private final DocumentDetailsPanel documentDetailsPanel;
    private Record currentDocumentRecord;
    protected boolean dataModified;
    protected boolean refreshing;
    protected ChangeControl changeControl;
    private Object idOrigDoc;
    private final boolean allowDuplicates;
    private final boolean isEditable;

    public DocumentDetailsController(String documentManagerKey, DocumentDetailsPanel documentDetailsPanel, Object idOrigDoc, boolean editable) {
        this(documentManagerKey, documentDetailsPanel, idOrigDoc, false, editable);
    }

    public DocumentDetailsController(String documentManagerKey, DocumentDetailsPanel documentDetailsPanel, Object idOrigDoc, boolean allowDuplicates, boolean editable) {
        this.documentManagerKey = documentManagerKey;
        this.documentDetailsPanel = documentDetailsPanel;
        this.allowDuplicates = allowDuplicates;
        this.isEditable = editable;
        this.setIdOrigDoc(idOrigDoc);
        this.documentDetailsPanel.getSelectButton().addActionListener(this);
        this.documentDetailsPanel.getSaveChangesButton().addActionListener(this);
        this.documentDetailsPanel.getReloadButton().addActionListener(this);
        this.documentDetailsPanel.getSaveToDBCheckBox().addActionListener(this);
        this.documentDetailsPanel.getRutaTextField().getDocument().addDocumentListener(this);
        this.setDataModified(false);
        this.changeControl = new ChangeControl(this, this.documentDetailsPanel);
        this.documentDetailsPanel.getSaveChangesButton().setEnabled(this.isEditable);
        this.documentDetailsPanel.getReloadButton().setEnabled(this.isEditable);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.documentDetailsPanel.getSelectButton()) {
            this.performSelect();
        } else if (ae.getSource() == this.documentDetailsPanel.getSaveChangesButton()) {
            this.performSave();
        } else if (ae.getSource() == this.documentDetailsPanel.getReloadButton()) {
            this.performReload();
        } else if (ae.getSource() == this.documentDetailsPanel.getSaveToDBCheckBox()) {
            this.performSaveToDBCheckBoxChange();
        }
    }

    private void performSaveToDBCheckBoxChange() {
        String sizeText = null;
        if (this.documentDetailsPanel.getSaveToDBCheckBox().isSelected()) {
            sizeText = String.valueOf(NumberFormat.getIntegerInstance(I18N.getLocale()).format(new File(this.documentDetailsPanel.getRutaTextField().getText().trim()).length())) + " B";
        }
        this.documentDetailsPanel.getSizeTextField().setText(sizeText);
    }

    private void performReload() {
        this.refreshComponentFromCurrentRecord();
        this.setDataModified(false);
    }

    private void performSave() {
        int option;
        boolean nuevoRegistro;
        if (this.currentDocumentRecord == null) {
            return;
        }
        boolean bl = nuevoRegistro = this.currentDocumentRecord.getAttribute("id_doc") == null;
        if (!nuevoRegistro && (option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), "Los cambios realizados afectar\u00e1n a todos los elementos relacionados con este documento \u00bfDesea continuar?", "Atenci\u00f3n")) != 0) {
            return;
        }
        TableDBRecordDataSource documDS = null;
        try {
            documDS = DocumentManager.getInstance(this.documentManagerKey).getTableDataSource("dm_documents");
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        String ruta = this.documentDetailsPanel.getRutaTextField().getText().trim();
        if (ruta.isEmpty()) {
            ruta = null;
        }
        boolean rutaCambiada = !StringUtils.equals((String)((String)this.currentDocumentRecord.getAttribute("ruta_original")), (String)ruta);
        DocumentManager.StoreAction storeAction = DocumentManager.StoreAction.NONE;
        boolean recorFileExist = false;
        try {
            recorFileExist = DocumentManager.getInstance(this.documentManagerKey).hasFile(this.currentDocumentRecord.getPrimaryKey());
        }
        catch (Exception e) {
            LOGGER.error((Object)"No se ha podido comprobar si existe contenido", (Throwable)e);
        }
        boolean rutaExiste = this.isFileCorrect(ruta);
        if (this.documentDetailsPanel.getSaveToDBCheckBox().isSelected()) {
            if (recorFileExist) {
                boolean actualizar = true;
                if (!rutaCambiada && rutaExiste) {
                    int option2 = DialogFactory.showYesNoCancelDialog(JUMPWorkbench.getFrameInstance(), "\u00bfDesea resubir el contenido del documento a la BD?", "Atenci\u00f3n");
                    boolean bl2 = actualizar = option2 == 0;
                    if (option2 != 0 && option2 != 1) {
                        return;
                    }
                }
                if (actualizar && rutaExiste) {
                    storeAction = DocumentManager.StoreAction.UPDATE;
                }
            } else {
                storeAction = DocumentManager.StoreAction.CREATE;
            }
        } else if (recorFileExist) {
            storeAction = DocumentManager.StoreAction.DELETE;
        }
        String md5 = null;
        if (storeAction == DocumentManager.StoreAction.CREATE || storeAction == DocumentManager.StoreAction.UPDATE) {
            if (!rutaExiste) {
                LOGGER.error((Object)("No existe el documento " + ruta));
                DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), "No existe el documento " + ruta, "Atenci\u00f3n");
                return;
            }
            TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
            progressDialog.setTitle("Analizando documento");
            MD5ComponentAdapterExtension md5ComponentAdapter = new MD5ComponentAdapterExtension(progressDialog, ruta);
            progressDialog.addComponentListener(md5ComponentAdapter);
            GUIUtil.centre(progressDialog, JUMPWorkbench.getFrameInstance());
            progressDialog.setVisible(true);
            md5 = md5ComponentAdapter.getMd5();
            if (md5 == null) {
                DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), "Proceso interrumpido", "Atenci\u00f3n");
                return;
            }
            List<Record> sameDocumRecords = documDS.getByAttribute(new String[]{"md5", "id_origen_doc"}, new Object[]{md5, this.idOrigDoc}, "nombre");
            if (!sameDocumRecords.isEmpty() && !this.allowDuplicates) {
                String msg = "El documento ya existe en la BD:";
                boolean distinto = false;
                for (Record rec : sameDocumRecords) {
                    if (rec.getPrimaryKey().equals(this.currentDocumentRecord.getPrimaryKey())) continue;
                    distinto = true;
                    String nombre = (String)rec.getAttribute("nombre");
                    String rutaStr = (String)rec.getAttribute("ruta_original");
                    msg = StringUtils.isBlank((String)nombre) ? String.valueOf(msg) + "\n - [Sin nombre]: " + rutaStr : String.valueOf(msg) + "\n - " + nombre + ": " + rutaStr;
                }
                if (distinto) {
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), msg, "Atenci\u00f3n");
                    return;
                }
            }
        }
        Record copyRecord = new Record(this.currentDocumentRecord.getSchema());
        RecordUtil.copyRecord(this.currentDocumentRecord.getSchema(), this.currentDocumentRecord, copyRecord);
        String nombre = this.documentDetailsPanel.getNombreTextField().getText().trim();
        if (nombre.isEmpty()) {
            nombre = null;
        }
        this.currentDocumentRecord.setAttribute("nombre", (Object)nombre);
        String observ = this.documentDetailsPanel.getObservacionesTextArea().getText().trim();
        if (observ.isEmpty()) {
            observ = null;
        }
        this.currentDocumentRecord.setAttribute("observaciones", (Object)observ);
        this.currentDocumentRecord.setAttribute("ruta_original", (Object)ruta);
        this.currentDocumentRecord.setAttribute("id_origen_doc", this.idOrigDoc);
        if (storeAction != DocumentManager.StoreAction.NONE) {
            this.currentDocumentRecord.setAttribute("md5", (Object)md5);
        }
        TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        progressDialog.setTitle("Salvando documento");
        SaveDocumentComponentAdapterExtension saveComponentAdapter = new SaveDocumentComponentAdapterExtension(progressDialog, this.documentManagerKey, this.currentDocumentRecord, storeAction, this.allowDuplicates);
        progressDialog.addComponentListener(saveComponentAdapter);
        GUIUtil.centre(progressDialog, JUMPWorkbench.getFrameInstance());
        progressDialog.setVisible(true);
        if (!saveComponentAdapter.isSaved()) {
            RecordUtil.copyRecord(this.currentDocumentRecord.getSchema(), copyRecord, this.currentDocumentRecord);
            DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), "Proceso de salvado interrumpido.", "Atenci\u00f3n");
        }
        this.refreshComponentFromCurrentRecord();
        this.setDataModified(false);
    }

    private void performSelect() {
        String ruta = this.documentDetailsPanel.getRutaTextField().getText().trim();
        File file = new File(ruta);
        JFileChooser jfc = new JFileChooser(ruta);
        if (!file.isDirectory()) {
            jfc = new JFileChooser(file.getParent());
        }
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showDialog(JUMPWorkbench.getFrameInstance(), "Selecciona documento");
        if (returnVal == 0) {
            this.documentDetailsPanel.getRutaTextField().setText(jfc.getSelectedFile().getAbsolutePath());
        }
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        if (de.getDocument() == this.documentDetailsPanel.getRutaTextField().getDocument()) {
            this.refreshSaveToDBCheckBox();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        if (de.getDocument() == this.documentDetailsPanel.getRutaTextField().getDocument()) {
            this.refreshSaveToDBCheckBox();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if (de.getDocument() == this.documentDetailsPanel.getRutaTextField().getDocument()) {
            this.refreshSaveToDBCheckBox();
        }
    }

    private void refreshSaveToDBCheckBox() {
        boolean correct = this.isFileCorrect(this.documentDetailsPanel.getRutaTextField().getText().trim());
        this.documentDetailsPanel.getSaveToDBCheckBox().setEnabled(this.isEditable && correct);
        this.documentDetailsPanel.getSaveToDBCheckBox().setSelected(correct);
    }

    private boolean isFileCorrect(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        return file.isFile() && file.canRead() && file.exists();
    }

    private void refreshComponentFromCurrentRecord() {
        this.clearComponents();
        boolean enabled = this.currentDocumentRecord != null;
        this.documentDetailsPanel.getNombreTextField().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getObservacionesTextArea().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getObservacionesTextArea().setEditable(this.isEditable && enabled);
        this.documentDetailsPanel.getObservacionesScrollPane().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getRutaTextField().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getSelectButton().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getSaveChangesButton().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getReloadButton().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getSizeTextField().setEnabled(this.isEditable && enabled);
        this.documentDetailsPanel.getFechaAltaTextField().setEnabled(this.isEditable && enabled);
        if (enabled) {
            Object id;
            Object name = this.currentDocumentRecord.getAttribute("nombre");
            if (name != null) {
                this.documentDetailsPanel.getNombreTextField().setText(name.toString());
            }
            this.documentDetailsPanel.getObservacionesScrollPane().setRecord(this.currentDocumentRecord);
            Object ruta = this.currentDocumentRecord.getAttribute("ruta_original");
            if (ruta != null) {
                this.documentDetailsPanel.getRutaTextField().setText(ruta.toString());
            }
            if ((id = this.currentDocumentRecord.getPrimaryKey()) != null) {
                boolean hasFile = false;
                try {
                    hasFile = DocumentManager.getInstance(this.documentManagerKey).hasFile(id);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                this.refreshSaveToDBCheckBox();
                this.documentDetailsPanel.getSaveToDBCheckBox().setSelected(hasFile);
            }
            String sizeText = null;
            Number size = (Number)this.currentDocumentRecord.getAttribute("size");
            if (this.documentDetailsPanel.getSaveToDBCheckBox().isSelected() && size != null) {
                sizeText = String.valueOf(NumberFormat.getIntegerInstance(I18N.getLocale()).format(size)) + " B";
            }
            this.documentDetailsPanel.getSizeTextField().setText(sizeText);
            Object fecha = this.currentDocumentRecord.getAttribute("f_alta");
            if (fecha != null) {
                this.documentDetailsPanel.getFechaAltaTextField().setText(SimpleDateFormat.getDateTimeInstance().format(fecha));
            }
        } else {
            this.documentDetailsPanel.getSaveToDBCheckBox().setEnabled(this.isEditable && enabled);
        }
    }

    private void clearComponents() {
        this.documentDetailsPanel.getNombreTextField().setText(null);
        this.documentDetailsPanel.getObservacionesTextArea().setText(null);
        this.documentDetailsPanel.getRutaTextField().setText(null);
        this.documentDetailsPanel.getSaveToDBCheckBox().setEnabled(false);
        this.documentDetailsPanel.getSaveToDBCheckBox().setSelected(false);
        this.documentDetailsPanel.getFechaAltaTextField().setText(null);
        this.documentDetailsPanel.getSizeTextField().setText(null);
    }

    public DocumentDetailsPanel getDocumentDetailsPanel() {
        return this.documentDetailsPanel;
    }

    public Record getCurrentDocumentRecord() {
        return this.currentDocumentRecord;
    }

    public void setCurrentDocumentRecord(Record currentDocumentRecord) {
        this.changeControl.addChangeControl();
        this.refreshing = true;
        this.currentDocumentRecord = currentDocumentRecord;
        this.refreshComponentFromCurrentRecord();
        this.setDataModified(false);
        this.refreshing = false;
    }

    public String getDocumentManagerKey() {
        return this.documentManagerKey;
    }

    @Override
    public boolean isRefreshing() {
        return this.refreshing;
    }

    @Override
    public void setDataModified(boolean dataModified) {
        this.dataModified = dataModified;
        this.documentDetailsPanel.getSaveChangesButton().setEnabled(this.isEditable && (dataModified || this.currentDocumentRecord != null && this.currentDocumentRecord.getPrimaryKey() == null));
        this.documentDetailsPanel.getReloadButton().setEnabled(this.isEditable && dataModified);
    }

    public Object getIdOrigDoc() {
        return this.idOrigDoc;
    }

    public void setIdOrigDoc(Object idOrigDoc) {
        this.idOrigDoc = idOrigDoc;
    }

    private static final class MD5ComponentAdapterExtension
    extends ComponentAdapter {
        private final TaskMonitorDialog progressDialog;
        private final String path;
        private String md5;

        private MD5ComponentAdapterExtension(TaskMonitorDialog progressDialog, String path) {
            this.progressDialog = progressDialog;
            this.path = path;
        }

        @Override
        public void componentShown(ComponentEvent e) {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    if (MD5ComponentAdapterExtension.this.progressDialog != null) {
                        MD5ComponentAdapterExtension.this.progressDialog.allowCancellationRequests();
                    }
                    MD5ComponentAdapterExtension.this.md5 = FileUtil.calculateMd5(MD5ComponentAdapterExtension.this.path, MD5ComponentAdapterExtension.this.progressDialog);
                }
            }).start();
        }

        public String getMd5() {
            return this.md5;
        }
    }

    private static final class SaveDocumentComponentAdapterExtension
    extends ComponentAdapter {
        private final TaskMonitorDialog progressDialog;
        private final Record documentRecord;
        private final DocumentManager.StoreAction storeAction;
        private final String docManagerKey;
        private final boolean allowDuplicated;
        private boolean saved = false;

        private SaveDocumentComponentAdapterExtension(TaskMonitorDialog progressDialog, String docManagerKey, Record documentRecord, DocumentManager.StoreAction storeAction, boolean allowDuplicated) {
            this.progressDialog = progressDialog;
            this.docManagerKey = docManagerKey;
            this.documentRecord = documentRecord;
            this.storeAction = storeAction;
            this.allowDuplicated = allowDuplicated;
        }

        @Override
        public void componentShown(ComponentEvent e) {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        DocumentManager.getInstance(SaveDocumentComponentAdapterExtension.this.docManagerKey).saveDocumentRecord(SaveDocumentComponentAdapterExtension.this.documentRecord, SaveDocumentComponentAdapterExtension.this.storeAction, SaveDocumentComponentAdapterExtension.this.allowDuplicated, SaveDocumentComponentAdapterExtension.this.progressDialog);
                        SaveDocumentComponentAdapterExtension.this.saved = true;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        SaveDocumentComponentAdapterExtension.this.saved = false;
                    }
                    if (SaveDocumentComponentAdapterExtension.this.progressDialog != null) {
                        SaveDocumentComponentAdapterExtension.this.progressDialog.setVisible(false);
                    }
                }
            }).start();
        }

        public boolean isSaved() {
            return this.saved;
        }
    }
}

