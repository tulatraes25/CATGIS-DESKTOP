/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.cts;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.TransformationEpsgPanel;
import org.gvsig.crs.ogr.TransEPSG;
import org.saig.jump.lang.I18N;

public class EPSGTransformationSelectionDialog
extends JDialog
implements ListSelectionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EPSGTransformationSelectionDialog.class);
    private IProjection sourceProj;
    private int targetEPSGCode;
    private TransformationEpsgPanel tranformationSelectionPanel;
    private OKCancelPanel okCancelPanel;

    public EPSGTransformationSelectionDialog(JFrame owner, boolean modal, IProjection proj, int epsgCode) {
        super((Frame)owner, modal);
        this.setTitle(I18N.getString(this.getClass(), "select-transformation-to-be-applied"));
        this.setBounds(0, 0, 600, 500);
        this.sourceProj = proj;
        this.targetEPSGCode = epsgCode;
        this.initialize();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        ICrs crs = (ICrs)this.sourceProj;
        int code = crs.getCode();
        String crs_target = "EPSG:" + this.targetEPSGCode;
        this.tranformationSelectionPanel = new TransformationEpsgPanel(crs_target);
        this.tranformationSelectionPanel.setSource("EPSG", code);
        this.tranformationSelectionPanel.setWKT(crs.getWKT());
        this.tranformationSelectionPanel.getJTable().getSelectionModel().addListSelectionListener(this);
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EPSGTransformationSelectionDialog.this.setVisible(false);
            }
        });
        mainPanel.add((Component)this.tranformationSelectionPanel, "Center");
        mainPanel.add((Component)this.okCancelPanel, "South");
        this.setContentPane(mainPanel);
    }

    public IProjection getProjection() {
        TransEPSG epsgParams = new TransEPSG(this.tranformationSelectionPanel.getTransformation_code(), this.tranformationSelectionPanel.connect, this.tranformationSelectionPanel.inverseTranformation);
        this.tranformationSelectionPanel.setValues(epsgParams.getParamValue());
        return this.tranformationSelectionPanel.getProjection();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            this.tranformationSelectionPanel.selectedRowTable = -1;
            this.okCancelPanel.getAcceptButton().setEnabled(false);
        } else {
            this.tranformationSelectionPanel.selectedRowTable = lsm.getMinSelectionIndex();
            this.tranformationSelectionPanel.setTrasformation_code(Integer.parseInt((String)this.tranformationSelectionPanel.sorter.getValueAt(this.tranformationSelectionPanel.selectedRowTable, 0)));
            String sentence = "SELECT target_crs_code FROM epsg_coordoperation WHERE coord_op_code = " + this.tranformationSelectionPanel.getTransformation_code();
            ResultSet result = Query.select(sentence, this.tranformationSelectionPanel.connect.getConnection());
            try {
                result.next();
                int tar = result.getInt("target_crs_code");
                this.tranformationSelectionPanel.inverseTranformation = tar != this.tranformationSelectionPanel.crs_target;
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            this.okCancelPanel.getAcceptButton().setEnabled(true);
        }
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

