/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.wizard;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardContext;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import org.saig.jump.lang.I18N;

public class WizardDialog
extends JDialog
implements WizardContext,
InputChangedListener {
    private static final long serialVersionUID = 1L;
    private JPanel panel1 = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private List<WizardPanel> completedWizardPanels;
    private JPanel buttonPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JButton cancelButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton backButton = new JButton();
    private JPanel fillerPanel = new JPanel();
    private Border border3;
    private JPanel outerCenterPanel = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JPanel centerPanel = new JPanel();
    private JLabel titleLabel = new JLabel();
    private CardLayout cardLayout1 = new CardLayout();
    private WizardPanel currentWizardPanel;
    private List<WizardPanel> allWizardPanels;
    private ErrorHandler errorHandler;
    private JTextArea instructionTextArea = new JTextArea();
    private boolean finishPressed = false;
    private Map<String, Object> dataMap = new HashMap<String, Object>();
    private Border border6;
    private Border border7;

    public WizardDialog(Frame frame, String title, ErrorHandler errorHandler) {
        super(frame, title, true);
        this.errorHandler = errorHandler;
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                WizardDialog.this.cancel();
            }
        });
    }

    private void checkIDs(Collection<WizardPanel> wizardPanels) {
        ArrayList<String> ids = new ArrayList<String>();
        for (WizardPanel panel : wizardPanels) {
            ids.add(panel.getID());
        }
        for (WizardPanel panel : wizardPanels) {
            if (panel.getNextID() == null) continue;
            Assert.isTrue((boolean)ids.contains(panel.getNextID()), (String)(String.valueOf(I18N.getString("workbench.ui.wizard.WizardDialog.required-panel-missing")) + ":" + panel.getNextID()));
        }
    }

    private void setCurrentWizardPanel(WizardPanel wizardPanel) {
        if (this.currentWizardPanel != null) {
            this.currentWizardPanel.remove(this);
        }
        this.titleLabel.setText(wizardPanel.getTitle());
        this.cardLayout1.show(this.centerPanel, wizardPanel.getID());
        this.currentWizardPanel = wizardPanel;
        this.updateButtons();
        this.currentWizardPanel.add(this);
        this.instructionTextArea.setText(this.currentWizardPanel.getInstructions());
    }

    private WizardPanel getCurrentWizardPanel() {
        return this.currentWizardPanel;
    }

    private void updateButtons() {
        this.backButton.setEnabled(!this.completedWizardPanels.isEmpty());
        this.nextButton.setEnabled(this.getCurrentWizardPanel().isInputValid());
        this.nextButton.setText(this.getCurrentWizardPanel().getNextID() == null ? I18N.getString("workbench.ui.wizard.WizardDialog.finish") : String.valueOf(I18N.getString("workbench.ui.wizard.WizardDialog.next")) + " >");
    }

    @Override
    public void inputChanged() {
        this.updateButtons();
    }

    public void init(WizardPanel[] wizardPanels) {
        this.finishPressed = false;
        this.allWizardPanels = Arrays.asList(wizardPanels);
        this.checkIDs(this.allWizardPanels);
        int i = 0;
        while (i < wizardPanels.length) {
            this.centerPanel.add((Component)((Object)wizardPanels[i]), wizardPanels[i].getID());
            ++i;
        }
        this.completedWizardPanels = new ArrayList<WizardPanel>();
        wizardPanels[0].enteredFromLeft(this.dataMap);
        this.setCurrentWizardPanel(wizardPanels[0]);
        this.pack();
    }

    private void jbInit() throws Exception {
        this.border7 = BorderFactory.createEmptyBorder(20, 20, 20, 10);
        this.instructionTextArea.setEnabled(false);
        this.instructionTextArea.setFont(new JLabel().getFont());
        this.instructionTextArea.setOpaque(false);
        this.instructionTextArea.setToolTipText("");
        this.instructionTextArea.setDisabledTextColor(this.instructionTextArea.getForeground());
        this.instructionTextArea.setEditable(false);
        this.instructionTextArea.setLineWrap(true);
        this.instructionTextArea.setWrapStyleWord(true);
        this.border6 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), BorderFactory.createEmptyBorder(20, 30, 20, 10));
        this.centerPanel.setLayout(this.cardLayout1);
        this.border3 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        this.panel1.setLayout(this.borderLayout1);
        this.buttonPanel.setLayout(this.gridBagLayout1);
        this.cancelButton.setText(I18N.getString("workbench.ui.wizard.WizardDialog.cancel"));
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WizardDialog.this.cancelButton_actionPerformed(e);
            }
        });
        this.nextButton.setText(String.valueOf(I18N.getString("workbench.ui.wizard.WizardDialog.next")) + " >");
        this.nextButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WizardDialog.this.nextButton_actionPerformed(e);
            }
        });
        this.backButton.setText("< " + I18N.getString("workbench.ui.wizard.WizardDialog.back"));
        this.backButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WizardDialog.this.backButton_actionPerformed(e);
            }
        });
        this.buttonPanel.setBorder(this.border3);
        this.outerCenterPanel.setLayout(this.gridBagLayout2);
        this.titleLabel.setBackground(Color.white);
        this.titleLabel.setFont(new Font("Dialog", 1, 12));
        this.titleLabel.setBorder(this.border7);
        this.titleLabel.setOpaque(true);
        this.titleLabel.setText(I18N.getString("workbench.ui.wizard.WizardDialog.title"));
        this.outerCenterPanel.setBorder(this.border6);
        this.setDefaultCloseOperation(0);
        this.instructionTextArea.setText("instructionTextArea");
        this.getContentPane().add(this.panel1);
        this.panel1.add((Component)this.buttonPanel, "South");
        this.buttonPanel.add((Component)this.cancelButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 20, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.nextButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.backButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.fillerPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.panel1.add((Component)this.outerCenterPanel, "Center");
        this.outerCenterPanel.add((Component)this.centerPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.outerCenterPanel.add((Component)this.instructionTextArea, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 20, 0), 0, 0));
        this.getContentPane().add((Component)this.titleLabel, "North");
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        this.cancel();
    }

    private void cancel() {
        this.setVisible(false);
    }

    void nextButton_actionPerformed(ActionEvent e) {
        try {
            if (this.getCurrentWizardPanel().isPanelOk()) {
                this.getCurrentWizardPanel().exitingToRight();
                if (this.getCurrentWizardPanel().getNextID() == null) {
                    this.finishPressed = true;
                    this.setVisible(false);
                    return;
                }
                this.completedWizardPanels.add(this.getCurrentWizardPanel());
                WizardPanel nextWizardPanel = this.find(this.getCurrentWizardPanel().getNextID());
                nextWizardPanel.enteredFromLeft(this.dataMap);
                this.setCurrentWizardPanel(nextWizardPanel);
            }
        }
        catch (Throwable x) {
            this.errorHandler.handleThrowable(x);
        }
    }

    private WizardPanel find(String id) {
        for (WizardPanel wizardPanel : this.allWizardPanels) {
            if (!wizardPanel.getID().equals(id)) continue;
            return wizardPanel;
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public boolean wasFinishPressed() {
        return this.finishPressed;
    }

    void backButton_actionPerformed(ActionEvent e) {
        WizardPanel prevPanel = this.completedWizardPanels.remove(this.completedWizardPanels.size() - 1);
        this.setCurrentWizardPanel(prevPanel);
    }

    @Override
    public void setData(String name, Object value) {
        this.dataMap.put(name, value);
    }

    @Override
    public Object getData(String name) {
        return this.dataMap.get(name);
    }

    public Map<String, Object> getDataMap() {
        return this.dataMap;
    }

    public List<WizardPanel> getAllWizardPanels() {
        return this.allWizardPanels;
    }
}

