/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.RecordPanel;
import com.vividsolutions.jump.workbench.ui.RecordPanelModel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class HTMLFrame
extends JInternalFrame
implements RecordPanelModel {
    private static final Logger LOGGER = Logger.getLogger(HTMLFrame.class);
    private JScrollPane scrollPane = new JScrollPane();
    private JEditorPane editorPane = new JEditorPane();
    private boolean focusFlag = true;
    private WorkbenchFrame workbenchFrame;
    private int currentIndex = -1;
    private ArrayList history = new ArrayList();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel southPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private RecordPanel recordPanel = new RecordPanel(this);
    private JPanel fillerPanel = new JPanel();
    protected boolean alwaysOnTop = false;
    protected JButton okButton = new JButton();
    private boolean notifyingUser = false;
    private JButton button = null;

    public HTMLFrame() {
    }

    public HTMLFrame(final WorkbenchFrame workbenchFrame) {
        this.workbenchFrame = workbenchFrame;
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.setDefaultCloseOperation(0);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                try {
                    workbenchFrame.removeInternalFrame(HTMLFrame.this);
                }
                catch (Exception x) {
                    workbenchFrame.handleThrowable(x);
                }
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                HTMLFrame.this.recordPanel.updateAppearance();
            }
        });
        this.setSize(500, 300);
        this.okButton.setVisible(false);
    }

    @Override
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        this.setEditorPaneText();
    }

    @Override
    public void setTitle(String s) {
        super.setTitle(s);
    }

    public void setBackgroundColor(Color color) {
        this.editorPane.setBackground(color);
    }

    public Color getBackgroundColor() {
        return this.editorPane.getBackground();
    }

    public void setFocusOn(boolean flag) {
        this.focusFlag = flag;
    }

    @Override
    public int getRecordCount() {
        return this.history.size();
    }

    @Override
    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public void createNewDocument() {
        this.history.add("");
        this.goToLatestDocument();
        this.recordPanel.updateAppearance();
    }

    public void setRecordNavigationControlVisible(boolean visible) {
        this.southPanel.setVisible(visible);
    }

    public void clear() {
        this.createNewDocument();
    }

    public void surface() {
        JInternalFrame activeFrame = this.workbenchFrame.getActiveInternalFrame();
        if (!this.workbenchFrame.hasInternalFrame(this)) {
            this.workbenchFrame.addInternalFrame(this, this.alwaysOnTop, true);
            GUIUtil.centreOnScreen(this);
        }
        if (this.focusFlag) {
            this.workbenchFrame.activateFrame(this);
        } else if (activeFrame != null) {
            this.workbenchFrame.activateFrame(activeFrame);
        }
        if (this.isIcon()) {
            try {
                this.setIcon(false);
            }
            catch (PropertyVetoException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        this.moveToFront();
    }

    public void addHeader(int level, String text) {
        this.append("<H" + level + "> " + GUIUtil.escapeHTML(text, false) + " </H" + level + ">\n");
    }

    public void addField(String label, String value) {
        this.addField(label, value, "");
    }

    public void addField(String label, String value, String units) {
        this.append("<B> " + label + " </B>" + value + " " + units + " <BR>\n");
    }

    public void addText(String text) {
        this.append(String.valueOf(GUIUtil.escapeHTML(text, false)) + " <BR>\n");
    }

    public void append(String html) {
        this.setLatestDocument(String.valueOf(this.getLatestDocument()) + html);
        this.goToLatestDocument();
    }

    private void setLatestDocument(String document) {
        this.history.set(this.history.size() - 1, document);
    }

    public void setButton(JButton button) {
        this.button = button;
    }

    private void setButtonHighlighted(boolean highlighted) {
        if (this.button == null) {
            return;
        }
        this.button.setIcon(highlighted ? IconLoader.icon("Frame2.gif") : IconLoader.icon("Frame.gif"));
    }

    private void notifyUser() {
        if (this.button == null) {
            return;
        }
        if (this.notifyingUser) {
            return;
        }
        this.notifyingUser = true;
        new Timer(500, new ActionListener(){
            private int tickCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                ++this.tickCount;
                HTMLFrame.this.setButtonHighlighted(this.tickCount % 2 == 0);
                if (this.tickCount == 8) {
                    Timer timer = (Timer)e.getSource();
                    timer.stop();
                    HTMLFrame.this.notifyingUser = false;
                    HTMLFrame.this.setButtonHighlighted(!HTMLFrame.this.isSelected());
                }
            }
        }).start();
    }

    private void setEditorPaneText() {
        final String document = (String)this.history.get(this.currentIndex);
        try {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    HTMLFrame.this.editorPane.setText("<HTML>" + document + "</HTML>");
                }
            });
            this.notifyUser();
            this.scrollToTop();
        }
        catch (Throwable t) {
            this.workbenchFrame.handleThrowable(t);
        }
    }

    private String getLatestDocument() {
        return (String)this.history.get(this.history.size() - 1);
    }

    private void jbInit() throws Exception {
        this.setTitle(I18N.getString("workbench.ui.HTMLFrame.output"));
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                HTMLFrame.this.this_internalFrameActivated(e);
            }
        });
        this.getContentPane().setLayout(this.borderLayout1);
        this.editorPane.setEditable(false);
        this.editorPane.setContentType("text/html");
        this.southPanel.setLayout(this.gridBagLayout1);
        this.scrollPane.setHorizontalScrollBarPolicy(31);
        this.okButton.setText(I18N.getString("workbench.ui.HTMLFrame.ok"));
        this.okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                HTMLFrame.this.okButton_actionPerformed(e);
            }
        });
        this.getContentPane().add((Component)this.scrollPane, "Center");
        this.getContentPane().add((Component)this.southPanel, "South");
        this.southPanel.add((Component)this.fillerPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.southPanel.add((Component)this.recordPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.southPanel.add((Component)this.okButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        JButton saveButton = new JButton(I18N.getString("com.vividsolutions.jump.workbench.ui.HTMLFrame.Save-stadistics"));
        saveButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                HTMLFrame.this.saveButton_actionPerformed(e);
            }
        });
        this.southPanel.add((Component)saveButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.scrollPane.getViewport().add((Component)this.editorPane, null);
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
    }

    protected void saveButton_actionPerformed(ActionEvent e) {
        File archivo = null;
        JFileChooser chooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(GUIUtil.createFileFilter(I18N.getString("com.vividsolutions.jump.workbench.ui.HTMLFrame.Save-in-HTML"), new String[]{"htm"}));
        int returned = chooser.showSaveDialog(JUMPWorkbench.getFrameInstance());
        if (returned == 0) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            chooser.getSelectedFile().delete();
            archivo = new File(path);
            archivo = FileUtil.addExtensionIfNone(archivo, "htm");
            try {
                String texto = this.history.get(this.currentIndex).toString();
                FileUtil.setContents(archivo.getAbsolutePath(), texto);
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("com.vividsolutions.jump.workbench.ui.HTMLFrame.An-error-has-been-produced-while-writing-the-file"), I18N.getString("com.vividsolutions.jump.workbench.ui.HTMLFrame.Error"));
            }
        }
    }

    private void goToLatestDocument() {
        this.setCurrentIndex(this.history.size() - 1);
    }

    public void scrollToTop() {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                HTMLFrame.this.editorPane.setCaretPosition(0);
            }
        });
    }

    void okButton_actionPerformed(ActionEvent e) {
        this.doDefaultCloseAction();
    }

    void this_internalFrameActivated(InternalFrameEvent e) {
        this.setButtonHighlighted(false);
    }
}

