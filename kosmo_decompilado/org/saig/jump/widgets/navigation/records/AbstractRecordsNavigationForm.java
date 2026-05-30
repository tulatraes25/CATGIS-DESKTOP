/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.navigation.records;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.context.documents.DocumentManager;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.records.AbstractRecordsNavigationPanel;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.IForm;

public abstract class AbstractRecordsNavigationForm
extends JInternalFrame
implements IForm,
IStatusForm,
org.saig.jump.widgets.navigation.listener.DataModifiedListener,
VetoableChangeListener,
LayerManagerProxy,
FocusListener,
KeyListener {
    private Logger LOGGER = Logger.getLogger(AbstractRecordsNavigationForm.class);
    private JPanel buttonPanel;
    protected JButton firstButton;
    protected JButton previousButton;
    protected JButton nextButton;
    protected JButton lastButton;
    protected JButton saveChangesButton;
    private JButton cancelButton;
    private JButton compactButton;
    private JButton helpButton;
    private boolean confirmOnExit;
    protected JTextField indexTextField;
    private JLabel indexLabel;
    protected JLabel messageLabel;
    protected int currentPosition = -1;
    protected int lastPosition;
    protected List<Record> allRecords;
    protected List<Record> showingRecords;
    protected TableDBRecordDataSource table;
    protected AbstractRecordsNavigationPanel attributesPanel;
    protected boolean editable;
    protected boolean compact;
    private boolean refreshing;
    private boolean isDataModified = false;
    private TaskFrame taskFrame;
    private String documentHelpName = DocumentManager.getHelpTag(this.getClass().getName());

    public AbstractRecordsNavigationForm(String title, boolean resizable, boolean closable, boolean maxizable, TaskFrame taskFrame) {
        super(title, resizable, closable, maxizable);
        this.taskFrame = taskFrame;
        this.compact = true;
        this.confirmOnExit = true;
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                AbstractRecordsNavigationForm.this.LOGGER.warn((Object)I18N.getString(AbstractRecordsNavigationForm.class, "closing"));
                AbstractRecordsNavigationForm.this.rollback();
                AbstractRecordsNavigationForm.this.onClosing();
            }
        });
        this.addVetoableChangeListener(this);
    }

    public AbstractRecordsNavigationForm(String title, boolean resizable, boolean closable, boolean maxizable, boolean editable, List<Record> recordsList, TableDBRecordDataSource table, TaskFrame taskFrame) {
        super(title, resizable, closable, maxizable);
        this.taskFrame = taskFrame;
        this.compact = true;
        this.table = table;
        this.allRecords = new ArrayList<Record>();
        this.allRecords.addAll(recordsList);
        this.showingRecords = new ArrayList<Record>();
        this.showingRecords.addAll(recordsList);
        this.attributesPanel = this.getAttributesPanel();
        this.editable = editable;
        this.confirmOnExit = true;
        if (this.attributesPanel == null) {
            return;
        }
        this.attributesPanel.addDataModifiedListener(this);
        if (CollectionUtils.isEmpty(recordsList)) {
            this.lastPosition = -1;
            this.currentPosition = -1;
        } else {
            this.lastPosition = recordsList.size() - 1;
            this.currentPosition = 0;
        }
        JPanel navigationButtonsPanel = this.getButtonPanel();
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        this.messageLabel = new JLabel();
        this.messageLabel.setOpaque(true);
        this.messageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.messageLabel.setText(" ");
        statusPanel.add((Component)this.messageLabel, "Center");
        this.refreshPanel();
        this.refreshNavigationButtons();
        this.setDataModified(false);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.attributesPanel);
        FormUtils.addFiller(mainPanel, 1, 0);
        FormUtils.addRowInGBL(mainPanel, 2, 0, navigationButtonsPanel);
        FormUtils.addRowInGBL((JComponent)mainPanel, 3, 0, (JComponent)statusPanel, true, false);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                AbstractRecordsNavigationForm.this.LOGGER.warn((Object)I18N.getString(AbstractRecordsNavigationForm.class, "closing"));
                AbstractRecordsNavigationForm.this.rollback();
                AbstractRecordsNavigationForm.this.onClosing();
            }
        });
        this.addVetoableChangeListener(this);
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    public void setConfirmOnExit(boolean confirm) {
        this.confirmOnExit = confirm;
    }

    public void revalidate(List<Record> records) {
        this.showingRecords = records;
        if (this.showingRecords == null || this.showingRecords.size() == 0) {
            this.lastPosition = -1;
            this.currentPosition = -1;
        } else {
            this.lastPosition = this.showingRecords.size() - 1;
            this.currentPosition = 0;
        }
        this.refreshPanel();
        this.refreshNavigationButtons();
    }

    public JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel(new FlowLayout(1));
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.setLayout(new FlowLayout(1, 0, 0));
            toolBar.add(Box.createHorizontalGlue());
            this.firstButton = new JButton(IconLoader.icon("Start.gif"));
            this.firstButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "first"));
            this.firstButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractRecordsNavigationForm.this.goToFirstRecord();
                }
            });
            this.previousButton = new JButton(IconLoader.icon("Prev.gif"));
            this.previousButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "previous"));
            this.previousButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractRecordsNavigationForm.this.goToRecordAt(AbstractRecordsNavigationForm.this.currentPosition - 1);
                }
            });
            this.nextButton = new JButton(IconLoader.icon("Next.gif"));
            this.nextButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "next"));
            this.nextButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractRecordsNavigationForm.this.goToRecordAt(AbstractRecordsNavigationForm.this.currentPosition + 1);
                }
            });
            this.lastButton = new JButton(IconLoader.icon("End.gif"));
            this.lastButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "last"));
            this.lastButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractRecordsNavigationForm.this.goToLastRecord();
                }
            });
            this.saveChangesButton = new JButton(IconLoader.icon("Save.gif"));
            this.saveChangesButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "save-changes"));
            this.saveChangesButton.setEnabled(false);
            this.saveChangesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    AbstractRecordsNavigationForm.this.action_saveChanges();
                }
            });
            this.helpButton = new JButton(IconLoader.icon("help.png"));
            this.helpButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        DocumentManager manager = DocumentManager.getInstance("_HELP_DOCUMENT_MANAGER_KEY_");
                        manager.openDocumentByInternalName(AbstractRecordsNavigationForm.this.taskFrame, AbstractRecordsNavigationForm.this.documentHelpName);
                    }
                    catch (Exception e1) {
                        AbstractRecordsNavigationForm.this.LOGGER.error((Object)"", (Throwable)e1);
                        DialogFactory.showErrorDialog(AbstractRecordsNavigationForm.this.taskFrame, I18N.getString(AbstractRecordsNavigationForm.class, "an-unexpected-error-occurred-please-check-the-system-log"), I18N.getString(AbstractRecordsNavigationForm.class, "error-opening-document"));
                    }
                }
            });
            this.cancelButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("close.gif")));
            this.cancelButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (AbstractRecordsNavigationForm.this.confirmOnExit && AbstractRecordsNavigationForm.this.isDataModified()) {
                        int response = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.navigation.records.AbstractRecordsNavigationForm.The-form-will-be-close-without-saving-its-changes-Are-you-sure"), I18N.getString("org.saig.jump.widgets.navigation.records.AbstractRecordsNavigationForm.Close-without-saving"));
                        if (response == 0) {
                            AbstractRecordsNavigationForm.this.rollback();
                            AbstractRecordsNavigationForm.this.close();
                        }
                    } else {
                        AbstractRecordsNavigationForm.this.rollback();
                        AbstractRecordsNavigationForm.this.close();
                    }
                }
            });
            this.compactButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("restaurar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "compatc"));
            this.compactButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractRecordsNavigationForm.this.compact();
                }
            });
            Dimension indexDim = new Dimension(60, 20);
            this.indexLabel = new JLabel();
            this.indexLabel.setMinimumSize(indexDim);
            this.indexLabel.setPreferredSize(indexDim);
            this.indexLabel.setHorizontalAlignment(2);
            this.indexLabel.setVerticalAlignment(0);
            this.indexTextField = new JTextField();
            this.indexTextField.setMinimumSize(indexDim);
            this.indexTextField.setPreferredSize(indexDim);
            this.indexTextField.setHorizontalAlignment(4);
            this.indexTextField.addFocusListener(this);
            this.indexTextField.addKeyListener(this);
            toolBar.add(this.firstButton);
            toolBar.add(this.previousButton);
            toolBar.add(this.indexTextField);
            toolBar.add(this.indexLabel);
            toolBar.add(this.nextButton);
            toolBar.add(this.lastButton);
            toolBar.addSeparator();
            if (this.editable) {
                toolBar.add(this.saveChangesButton);
            } else {
                this.attributesPanel.disable();
            }
            toolBar.add(this.helpButton);
            toolBar.add(this.cancelButton);
            toolBar.addSeparator();
            toolBar.add(this.compactButton);
            toolBar.add(Box.createHorizontalGlue());
            this.buttonPanel.add(toolBar);
        }
        return this.buttonPanel;
    }

    protected void action_saveChanges() {
        this.clearStatusMessage();
        if (this.isDataModified()) {
            if (!this.beforeSave()) {
                return;
            }
            if (!this.isInputValid()) {
                return;
            }
            try {
                this.update();
            }
            catch (Exception e) {
                this.LOGGER.error((Object)e);
                String errorMsg = I18N.getString(AbstractRecordsNavigationForm.class, "an-error-occurred-updating-an-element-state-check-the-log-file");
                String errorTitle = I18N.getString(AbstractRecordsNavigationForm.class, "error");
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), errorMsg, errorTitle);
                this.rollback();
                this.close();
                return;
            }
            try {
                this.commit();
            }
            catch (Exception e1) {
                this.LOGGER.error((Object)"", (Throwable)e1);
                this.table.rollback();
                String errorMsg = I18N.getString(AbstractRecordsNavigationForm.class, "an-error-occurred-while-trying-to-save-the-changes-check-the-log-file-for-mor-details");
                String errorTitle = I18N.getString(AbstractRecordsNavigationForm.class, "error-saving-changes");
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), errorMsg, errorTitle);
            }
            this.close();
        }
    }

    protected boolean beforeSave() {
        return this.attributesPanel.beforeSave();
    }

    public void goToRecordAt(int position) {
        if (CollectionUtils.isNotEmpty(this.showingRecords) && position >= 0 && position < this.showingRecords.size()) {
            if (!this.isInputValid()) {
                return;
            }
            this.clearStatusMessage();
            if (this.isDataModified()) {
                try {
                    this.update();
                }
                catch (Exception e) {
                    this.LOGGER.error((Object)e);
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(AbstractRecordsNavigationForm.class, "error"), I18N.getString(AbstractRecordsNavigationForm.class, "an-error-occurred-updating-an-element-state-check-the-log-file"));
                    this.rollback();
                    this.close();
                    return;
                }
            }
            this.currentPosition = position;
            this.refreshNavigationButtons();
            this.refreshPanel();
        }
    }

    public void goToFirstRecord() {
        this.goToRecordAt(0);
    }

    public void goToLastRecord() {
        this.goToRecordAt(this.lastPosition);
    }

    @Override
    public boolean isInputValid() {
        return this.attributesPanel.isInputValid();
    }

    public void refreshPanel() {
        this.setRefreshing(true);
        if (this.currentPosition != -1) {
            this.attributesPanel.refresh(this.showingRecords.get(this.currentPosition));
        }
        this.setRefreshing(false);
    }

    public void refreshNavigationButtons() {
        this.helpButton.setEnabled(this.documentHelpName != null);
        if (this.showingRecords.size() == 0) {
            this.previousButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.firstButton.setEnabled(false);
            this.lastButton.setEnabled(false);
        } else if (this.showingRecords.size() == 1) {
            this.previousButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.firstButton.setEnabled(false);
            this.lastButton.setEnabled(false);
        } else if (this.currentPosition == 0) {
            this.previousButton.setEnabled(false);
            this.nextButton.setEnabled(true);
            this.firstButton.setEnabled(false);
            this.lastButton.setEnabled(true);
        } else if (this.currentPosition == this.lastPosition) {
            this.previousButton.setEnabled(true);
            this.nextButton.setEnabled(false);
            this.firstButton.setEnabled(true);
            this.lastButton.setEnabled(false);
        } else {
            this.previousButton.setEnabled(true);
            this.nextButton.setEnabled(true);
            this.firstButton.setEnabled(true);
            this.lastButton.setEnabled(true);
        }
        this.indexTextField.setText("" + (this.currentPosition + 1));
        this.indexLabel.setText("/" + (this.lastPosition + 1));
    }

    public void setDataModified(boolean dataModified) {
        if (dataModified && this.isInputValid()) {
            this.saveChangesButton.setEnabled(true);
            this.isDataModified = true;
        } else {
            this.saveChangesButton.setEnabled(false);
        }
    }

    public boolean isDataModified() {
        return this.isDataModified;
    }

    @Override
    public void update() throws Exception {
        this.attributesPanel.update(this.showingRecords.get(this.currentPosition));
    }

    @Override
    public void rollback() {
        this.table.rollback();
        this.attributesPanel.rollback();
    }

    public void close() {
        this.onClosing();
        this.dispose();
    }

    @Override
    public void commit() throws Exception {
        this.table.updateAll(this.showingRecords);
        this.table.commit();
        this.attributesPanel.commit();
    }

    protected void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public boolean isRefreshing() {
        return this.refreshing;
    }

    public abstract AbstractRecordsNavigationPanel getAttributesPanel();

    @Override
    public void dataModified() {
        if (!this.isRefreshing()) {
            this.setDataModified(true);
        }
    }

    public Record getCurrentRecord() {
        if (this.currentPosition == -1) {
            return null;
        }
        return this.showingRecords.get(this.currentPosition);
    }

    public int recordsSize() {
        return this.showingRecords.size();
    }

    public List<Record> getRecords() {
        return new ArrayList<Record>(this.showingRecords);
    }

    public List<Record> getAllRecords() {
        return new ArrayList<Record>(this.allRecords);
    }

    public void addRecord(Record record) {
        this.allRecords.add(record);
    }

    public void removeRecord(Record record) {
        this.allRecords.remove(record);
    }

    public TaskFrame getTaskFrame() {
        return this.taskFrame;
    }

    protected void compact() {
        boolean bl = this.compact = !this.compact;
        if (this.compact) {
            this.compactButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("restaurar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "compact"));
        } else {
            this.compactButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("maximizar.png")));
            this.compactButton.setToolTipText(I18N.getString(AbstractRecordsNavigationForm.class, "maximize"));
        }
        this.attributesPanel.compact(this.compact);
        this.pack();
    }

    @Override
    public void warnUser(final String warning) {
        new Timer(100, new ActionListener(){
            private int tickCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                ++this.tickCount;
                AbstractRecordsNavigationForm.this.setStatusBarText(warning);
                AbstractRecordsNavigationForm.this.setStatusBarTextHighlighted(this.tickCount % 2 == 0);
                if (this.tickCount == 4) {
                    Timer timer = (Timer)e.getSource();
                    timer.stop();
                }
            }
        }).start();
    }

    public void setStatusMessage(String message) {
        this.setStatusBarText(message);
        this.setStatusBarTextHighlighted(false);
    }

    public void setStatusMessage(String message, boolean highlighted) {
        this.setStatusBarText(message);
        this.setStatusBarTextHighlighted(highlighted);
    }

    @Override
    public void clearStatusMessage() {
        this.setStatusMessage("");
    }

    private void setStatusBarText(String message) {
        this.messageLabel.setText(StringUtils.isEmpty((String)message) ? " " : message);
        this.messageLabel.setToolTipText(message);
    }

    private void setStatusBarTextHighlighted(boolean highlighted) {
        this.messageLabel.setForeground(highlighted ? Color.black : new JLabel().getForeground());
        this.messageLabel.setBackground(highlighted ? Color.yellow : new JLabel().getBackground());
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (evt.getPropertyName().equals("closed") && evt.getNewValue().equals(Boolean.TRUE) && !this.attributesPanel.isInputValid()) {
            throw new PropertyVetoException("", evt);
        }
    }

    protected void onClosing() {
        this.LOGGER.warn((Object)"Closing");
    }

    @Override
    public LayerManager getLayerManager() {
        return this.taskFrame.getLayerManager();
    }

    @Override
    public void focusGained(FocusEvent fe) {
        if (fe.getSource() == this.indexTextField) {
            this.indexTextField.selectAll();
        }
    }

    @Override
    public void focusLost(FocusEvent fe) {
        if (fe.getSource() == this.indexTextField) {
            try {
                int parsedIndex = Integer.parseInt(this.indexTextField.getText());
                if (parsedIndex < 1 || parsedIndex > this.lastPosition + 1) {
                    this.indexTextField.setText("" + (this.currentPosition + 1));
                } else {
                    this.goToRecordAt(parsedIndex - 1);
                }
            }
            catch (NumberFormatException nfe) {
                this.indexTextField.setText("" + (this.currentPosition + 1));
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getSource() == this.indexTextField) {
            if (ke.getKeyCode() == 27) {
                this.indexTextField.setText("" + (this.currentPosition + 1));
                this.nextButton.requestFocus();
            } else if (ke.getKeyCode() == 10) {
                this.nextButton.requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    public static interface DataModifiedListener
    extends EventListener {
        public void dataModified();
    }
}

