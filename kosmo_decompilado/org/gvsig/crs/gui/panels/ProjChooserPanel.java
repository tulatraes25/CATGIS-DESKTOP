/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 *  org.gvsig.gui.beans.swing.JButton
 */
package org.gvsig.crs.gui.panels;

import com.iver.cit.gvsig.gui.panels.CRSSelectPanel;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Task;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.dialog.CSSelectionDialog;
import org.gvsig.crs.gui.dialog.TRSelectionDialog;
import org.gvsig.gui.beans.swing.JButton;
import org.saig.core.util.DialogManager;
import org.saig.jump.lang.I18N;

public class ProjChooserPanel
extends CRSSelectPanel {
    private static final long serialVersionUID = 1L;
    public ICrs curProj = null;
    private JLabel jLblProj = null;
    private JLabel jLblProjName = null;
    private JButton jBtnChangeProj = null;
    private boolean okPressed = false;
    private String abrev;
    boolean panel = false;
    boolean targetNad = false;
    String dataSource = "EPSG";
    private ActionListener actionListener = null;

    public ProjChooserPanel(IProjection proj) {
        super(proj);
        IProjection pr = proj;
        Task activeTask = JUMPWorkbench.getFrameInstance().getContext().getTask();
        pr = activeTask.getProjection();
        this.setCurProj(pr);
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridLayout(3, 1));
        this.setLayout(new FlowLayout(0, 15, 0));
        this.setPreferredSize(new Dimension(330, 35));
        this.setSize(new Dimension(330, 23));
        this.add((Component)this.getJLblProjName(), null);
        this.add((Component)this.getJLblProj(), null);
        this.add((Component)this.getJBtnChangeProj(), null);
        if (!this.curProj.getAbrev().equals("EPSG:23030")) {
            this.dataSource = this.curProj.getCrsWkt().getAuthority()[0];
            this.jLblProj.setText(String.valueOf(this.dataSource) + ":" + String.valueOf(this.curProj.getCode()));
        } else {
            this.jLblProj.setText("EPSG:23030");
        }
        this.initBtnChangeProj();
    }

    private void initBtnChangeProj() {
        this.getJBtnChangeProj().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                CSSelectionDialog csSelect = null;
                TRSelectionDialog trSelect = null;
                if (!ProjChooserPanel.this.isTransPanelActive()) {
                    ProjChooserPanel.this.okPressed = false;
                    csSelect = new CSSelectionDialog(ProjChooserPanel.this.curProj);
                    csSelect.setProjection(ProjChooserPanel.this.curProj);
                    csSelect.initRecents(ProjChooserPanel.this.curProj);
                    DialogManager.addJDialog(csSelect, I18N.getString("org.gvsig.crs.gui.panels.ProjChooserPanel.spatial-reference-system-selection"));
                    ProjChooserPanel.this.abrev = String.valueOf(ProjChooserPanel.this.dataSource) + ":" + String.valueOf(ProjChooserPanel.this.curProj.getCode());
                    if (csSelect.isOkPressed()) {
                        ProjChooserPanel.this.curProj = (ICrs)csSelect.getProjection();
                        ProjChooserPanel.this.dataSource = ProjChooserPanel.this.curProj.getCrsWkt().getAuthority()[0];
                        ProjChooserPanel.this.abrev = String.valueOf(ProjChooserPanel.this.dataSource) + ":" + String.valueOf(ProjChooserPanel.this.curProj.getCode());
                        ProjChooserPanel.this.jLblProj.setText(String.valueOf(ProjChooserPanel.this.dataSource) + ":" + String.valueOf(ProjChooserPanel.this.curProj.getCode()));
                        ProjChooserPanel.this.okPressed = true;
                        if (ProjChooserPanel.this.actionListener != null) {
                            ProjChooserPanel.this.actionListener.actionPerformed(e);
                        }
                    } else {
                        ProjChooserPanel.this.jLblProj.setText(ProjChooserPanel.this.abrev);
                    }
                } else {
                    ProjChooserPanel.this.okPressed = false;
                    Task activeTask = JUMPWorkbench.getFrameInstance().getContext().getTask();
                    int targetProjCode = ((ICrs)activeTask.getProjection()).getCode();
                    CrsWkt crsWktTarget = ((ICrs)activeTask.getProjection()).getCrsWkt();
                    ProjChooserPanel.this.okPressed = false;
                    trSelect = new TRSelectionDialog(ProjChooserPanel.this.curProj, targetProjCode, crsWktTarget);
                    trSelect.setProjection(ProjChooserPanel.this.curProj);
                    trSelect.setLayout(new GridLayout(0, 1));
                    DialogManager.addJDialog(csSelect, I18N.getString("org.gvsig.crs.gui.panels.ProjChooserPanel.transformation-system-selection"));
                    ProjChooserPanel.this.setTargetNad(trSelect.getTargetNad());
                    if (trSelect.getProjection() != ProjChooserPanel.this.curProj) {
                        ProjChooserPanel.this.curProj = (ICrs)trSelect.getProjection();
                        ProjChooserPanel.this.dataSource = ProjChooserPanel.this.curProj.getCrsWkt().getAuthority()[0];
                        ProjChooserPanel.this.jLblProj.setText(String.valueOf(ProjChooserPanel.this.dataSource) + ":" + String.valueOf(ProjChooserPanel.this.curProj.getCode()));
                        ProjChooserPanel.this.okPressed = true;
                        if (ProjChooserPanel.this.actionListener != null) {
                            ProjChooserPanel.this.actionListener.actionPerformed(e);
                        }
                    } else {
                        ProjChooserPanel.this.jLblProj.setText(String.valueOf(ProjChooserPanel.this.dataSource) + ":" + String.valueOf(ProjChooserPanel.this.curProj.getCode()));
                    }
                }
            }
        });
    }

    public JLabel getJLblProjName() {
        if (this.jLblProjName == null) {
            this.jLblProjName = new JLabel(I18N.getString("org.gvsig.crs.gui.panels.ProjChooserPanel.current-projection"));
            this.jLblProjName.setText("Proyecci\u00f3n actual");
        }
        return this.jLblProjName;
    }

    @Override
    public JLabel getJLabel() {
        return this.getJLblProjName();
    }

    public JLabel getJLblProj() {
        if (this.jLblProj == null) {
            this.jLblProj = new JLabel();
            this.jLblProj.setText(this.curProj.getAbrev());
        }
        return this.jLblProj;
    }

    public void addBtnChangeProjActionListener(ActionListener al) {
        this.jBtnChangeProj.addActionListener(al);
    }

    @Override
    public JButton getJBtnChangeProj() {
        if (this.jBtnChangeProj == null) {
            this.jBtnChangeProj = new JButton();
            this.jBtnChangeProj.setText("...");
            this.jBtnChangeProj.setPreferredSize(new Dimension(50, 23));
        }
        return this.jBtnChangeProj;
    }

    public void setTargetNad(boolean tarNad) {
        this.targetNad = tarNad;
    }

    public boolean getTargetNad() {
        return this.targetNad;
    }

    @Override
    public IProjection getCurProj() {
        return this.curProj;
    }

    public void setCurProj(IProjection curProj) {
        this.curProj = (ICrs)curProj;
    }

    @Override
    public boolean isOkPressed() {
        return this.okPressed;
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}

