/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.gui.panels;

import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.jump.lang.I18N;

public class TransformationManualPanel
extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(TransformationManualPanel.class);
    private static final long serialVersionUID = 1L;
    private static boolean pressed = true;
    private JLabel x_Translation;
    private JLabel y_Translation;
    private JLabel z_Translation;
    private JLabel x_Rotation;
    private JLabel y_Rotation;
    private JLabel z_Rotation;
    private JLabel scale;
    private JTextField tx_Translation;
    private JTextField ty_Translation;
    private JTextField tz_Translation;
    private JTextField tx_Rotation;
    private JTextField ty_Rotation;
    private JTextField tz_Rotation;
    private JTextField tScale;
    private JLabel domainTranslation;
    private JLabel domainRotation;
    private JLabel domainScale;
    int codeEpsg;
    String targetAbrev = "";
    String sourceAbrev = "";
    String[] targetAuthority;
    private String cadWKT = "";

    public TransformationManualPanel() {
        this.initialize();
    }

    private void initialize() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(7, 3, 10, 10));
        p.add(this.getX_Translation());
        p.add(this.getTx_Translation());
        p.add(this.getDomainTranslation());
        p.add(this.getY_Translation());
        p.add(this.getTy_Translation());
        p.add(this.getDomainTranslation());
        p.add(this.getZ_Translation());
        p.add(this.getTz_Translation());
        p.add(this.getDomainTranslation());
        p.add(this.getX_Rotation());
        p.add(this.getTx_Rotation());
        p.add(this.getDomainRotation());
        p.add(this.getY_Rotation());
        p.add(this.getTy_Rotation());
        p.add(this.getDomainRotation());
        p.add(this.getZ_Rotation());
        p.add(this.getTz_Rotation());
        p.add(this.getDomainRotation());
        p.add(this.getScale());
        p.add(this.getTscale());
        p.add(this.getDomainScale());
        p.setBorder(new EmptyBorder(50, 20, 50, 10));
        this.setLayout(new BorderLayout(1, 30));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.custom-transformation")), BorderFactory.createEmptyBorder(2, 2, 2, 2)), this.getBorder()));
        this.add((Component)p, "Center");
    }

    private JLabel getDomainTranslation() {
        this.domainTranslation = new JLabel("[-1000.0, 1000.0]");
        return this.domainTranslation;
    }

    private JLabel getDomainRotation() {
        this.domainRotation = new JLabel("[-60.0, 60.0]");
        return this.domainRotation;
    }

    private JLabel getDomainScale() {
        this.domainScale = new JLabel("[-20.0, 20.0]");
        return this.domainScale;
    }

    private JLabel getX_Translation() {
        if (this.x_Translation == null) {
            this.x_Translation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.x-translation")) + ":");
        }
        return this.x_Translation;
    }

    private JLabel getY_Translation() {
        if (this.y_Translation == null) {
            this.y_Translation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.y-translation")) + ":");
        }
        return this.y_Translation;
    }

    private JLabel getZ_Translation() {
        if (this.z_Translation == null) {
            this.z_Translation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.z-translation")) + ":");
        }
        return this.z_Translation;
    }

    private JLabel getX_Rotation() {
        if (this.x_Rotation == null) {
            this.x_Rotation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.x-rotation")) + ":");
        }
        return this.x_Rotation;
    }

    private JLabel getY_Rotation() {
        if (this.y_Rotation == null) {
            this.y_Rotation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.y-rotation")) + ":");
        }
        return this.y_Rotation;
    }

    private JLabel getZ_Rotation() {
        if (this.z_Rotation == null) {
            this.z_Rotation = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.z-rotation")) + ":");
        }
        return this.z_Rotation;
    }

    private JLabel getScale() {
        if (this.scale == null) {
            this.scale = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationManualPanel.scale")) + ":");
        }
        return this.scale;
    }

    public JTextField getTx_Translation() {
        if (this.tx_Translation == null) {
            this.tx_Translation = new JTextField();
            this.tx_Translation.setText("0");
            this.tx_Translation.setEditable(true);
        }
        return this.tx_Translation;
    }

    public JTextField getTy_Translation() {
        if (this.ty_Translation == null) {
            this.ty_Translation = new JTextField();
            this.ty_Translation.setText("0");
            this.ty_Translation.setEditable(true);
        }
        return this.ty_Translation;
    }

    public JTextField getTz_Translation() {
        if (this.tz_Translation == null) {
            this.tz_Translation = new JTextField();
            this.tz_Translation.setText("0");
            this.tz_Translation.setEditable(true);
        }
        return this.tz_Translation;
    }

    public JTextField getTx_Rotation() {
        if (this.tx_Rotation == null) {
            this.tx_Rotation = new JTextField();
            this.tx_Rotation.setText("0");
            this.tx_Rotation.setEditable(true);
        }
        return this.tx_Rotation;
    }

    public JTextField getTy_Rotation() {
        if (this.ty_Rotation == null) {
            this.ty_Rotation = new JTextField();
            this.ty_Rotation.setText("0");
            this.ty_Rotation.setEditable(true);
        }
        return this.ty_Rotation;
    }

    public JTextField getTz_Rotation() {
        if (this.tz_Rotation == null) {
            this.tz_Rotation = new JTextField();
            this.tz_Rotation.setText("0");
            this.tz_Rotation.setEditable(true);
        }
        return this.tz_Rotation;
    }

    public JTextField getTscale() {
        if (this.tScale == null) {
            this.tScale = new JTextField();
            this.tScale.setText("0");
            this.tScale.setEditable(true);
        }
        return this.tScale;
    }

    public ICrs getProjection() {
        if (StringUtils.isEmpty((String)this.tx_Translation.getText())) {
            this.tx_Translation.setText("0");
        } else if (StringUtils.isEmpty((String)this.ty_Translation.getText())) {
            this.ty_Translation.setText("0");
        } else if (StringUtils.isEmpty((String)this.tz_Translation.getText())) {
            this.tz_Translation.setText("0");
        } else if (StringUtils.isEmpty((String)this.tx_Rotation.getText())) {
            this.tx_Rotation.setText("0");
        } else if (StringUtils.isEmpty((String)this.ty_Rotation.getText())) {
            this.ty_Rotation.setText("0");
        } else if (StringUtils.isEmpty((String)this.tz_Rotation.getText())) {
            this.tz_Rotation.setText("0");
        } else if (StringUtils.isEmpty((String)this.tScale.getText())) {
            this.tScale.setText("0");
        }
        String param = "+towgs84=" + this.tx_Translation.getText().trim() + "," + this.ty_Translation.getText().trim() + "," + this.tz_Translation.getText().trim() + "," + this.tx_Rotation.getText().trim() + "," + this.ty_Rotation.getText().trim() + "," + this.tz_Rotation.getText().trim() + "," + this.tScale.getText().trim() + " ";
        try {
            String[] sourceAuthority = this.getSourceAbrev().split(":");
            ICrs crs = CrsRepositoryManager.getInstance().getCRS(String.valueOf(sourceAuthority[0]) + ":" + this.getCode());
            crs.setTransformationParams(param, null);
            return crs;
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public void setCode(int cod) {
        this.codeEpsg = cod;
    }

    public int getCode() {
        return this.codeEpsg;
    }

    public void setWKT(String cad) {
        this.cadWKT = cad;
        CrsWkt parser = new CrsWkt(cad);
        this.setSourceAbrev(parser.getAuthority()[0], parser.getAuthority()[1]);
    }

    public void setWKT(ICrs crs) {
        this.cadWKT = crs.getWKT();
        this.setSourceAbrev(crs.getCrsWkt().getAuthority()[0], crs.getCrsWkt().getAuthority()[1]);
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public static boolean isPressed() {
        return pressed;
    }

    public boolean correctJTextField() {
        boolean correct = true;
        if (this.tx_Translation.getText().length() == 0 || this.tx_Translation.getText().length() != this.verify(this.tx_Translation.getText()).length()) {
            this.tx_Translation.setText("0");
            this.tx_Translation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.tx_Translation.setBackground(new Color(255, 255, 255));
        }
        if (this.ty_Translation.getText().length() == 0 || this.ty_Translation.getText().length() != this.verify(this.ty_Translation.getText()).length()) {
            this.ty_Translation.setText("0");
            this.ty_Translation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.ty_Translation.setBackground(new Color(255, 255, 255));
        }
        if (this.tz_Translation.getText().length() == 0 || this.tz_Translation.getText().length() != this.verify(this.tz_Translation.getText()).length()) {
            this.tz_Translation.setText("0");
            this.tz_Translation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.tz_Translation.setBackground(new Color(255, 255, 255));
        }
        if (this.tx_Rotation.getText().length() == 0 || this.tx_Rotation.getText().length() != this.verify(this.tx_Rotation.getText()).length()) {
            this.tx_Rotation.setText("0");
            this.tx_Rotation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.tx_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (this.ty_Rotation.getText().length() == 0 || this.ty_Rotation.getText().length() != this.verify(this.ty_Rotation.getText()).length()) {
            this.ty_Rotation.setText("0");
            this.ty_Rotation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.ty_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (this.tz_Rotation.getText().length() == 0 || this.tz_Rotation.getText().length() != this.verify(this.tz_Rotation.getText()).length()) {
            this.tz_Rotation.setText("0");
            this.tz_Rotation.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.tz_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (this.tScale.getText().length() == 0 || this.tScale.getText().length() != this.verify(this.tScale.getText()).length()) {
            this.tScale.setText("0");
            this.tScale.setBackground(new Color(255, 204, 204));
            correct = false;
        } else {
            this.tScale.setBackground(new Color(255, 255, 255));
        }
        return correct;
    }

    private String verify(String cad) {
        String num_cad = "";
        int pto = 0;
        char[] nums = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        if (cad.startsWith("-")) {
            num_cad = String.valueOf(num_cad) + "-";
        }
        int i = 0;
        while (i < cad.length()) {
            if (cad.charAt(i) == '.') {
                if (!cad.startsWith(".") && !cad.endsWith(".") && pto <= 0) {
                    ++pto;
                    num_cad = String.valueOf(num_cad) + cad.charAt(i);
                }
            } else {
                int j = 0;
                while (j < nums.length) {
                    if (cad.charAt(i) == nums[j]) {
                        num_cad = String.valueOf(num_cad) + cad.charAt(i);
                    }
                    ++j;
                }
            }
            ++i;
        }
        return num_cad;
    }

    public boolean correctDomain() {
        boolean correctDomain = true;
        double tx = Double.parseDouble(this.tx_Translation.getText());
        double ty = Double.parseDouble(this.ty_Translation.getText());
        double tz = Double.parseDouble(this.tz_Translation.getText());
        double rx = Double.parseDouble(this.tx_Rotation.getText());
        double ry = Double.parseDouble(this.ty_Rotation.getText());
        double rz = Double.parseDouble(this.tz_Rotation.getText());
        double sc = Double.parseDouble(this.tScale.getText());
        if (tx < -1000.0 || tx > 1000.0) {
            this.tx_Translation.setText("0");
            this.tx_Translation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.tx_Translation.setBackground(new Color(255, 255, 255));
        }
        if (ty < -1000.0 || ty > 1000.0) {
            this.ty_Translation.setText("0");
            this.ty_Translation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.ty_Translation.setBackground(new Color(255, 255, 255));
        }
        if (tz < -1000.0 || tz > 1000.0) {
            this.tz_Translation.setText("0");
            this.tz_Translation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.tz_Translation.setBackground(new Color(255, 255, 255));
        }
        if (rx < -60.0 || rx > 60.0) {
            this.tx_Rotation.setText("0");
            this.tx_Rotation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.tx_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (ry < -60.0 || ry > 60.0) {
            this.ty_Rotation.setText("0");
            this.ty_Rotation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.ty_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (rz < -60.0 || rz > 60.0) {
            this.tz_Rotation.setText("0");
            this.tz_Rotation.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.tz_Rotation.setBackground(new Color(255, 255, 255));
        }
        if (sc < -20.0 || sc > 20.0) {
            this.tScale.setText("0");
            this.tScale.setBackground(new Color(255, 204, 204));
            correctDomain = false;
        } else {
            this.tScale.setBackground(new Color(255, 255, 255));
        }
        return correctDomain;
    }

    public boolean getStatus() {
        if ((this.tx_Translation.getText().equals("0") || this.tx_Translation.getText().equals("")) && (this.ty_Translation.getText().equals("0") || this.ty_Translation.getText().equals("")) && (this.tz_Translation.getText().equals("0") || this.tz_Translation.getText().equals("")) && (this.tx_Rotation.getText().equals("0") || this.tx_Rotation.getText().equals("")) && (this.ty_Rotation.getText().equals("0") || this.ty_Rotation.getText().equals("")) && (this.tz_Rotation.getText().equals("0") || this.tz_Rotation.getText().equals("")) && (this.tScale.getText().equals("0") || this.tScale.getText().equals(""))) {
            return false;
        }
        return !this.tx_Translation.getText().equals("") && !this.ty_Translation.getText().equals("") && !this.tz_Translation.getText().equals("") && !this.tx_Rotation.getText().equals("") && !this.ty_Rotation.getText().equals("") && !this.tz_Rotation.getText().equals("") && !this.tScale.getText().equals("");
    }

    public void setTargetAuthority(String[] authority) {
        this.targetAuthority = authority;
        this.setTargetAbrev(this.targetAuthority[0], this.targetAuthority[1]);
    }

    public void setTargetAbrev(String fuente, String codigo) {
        this.targetAbrev = String.valueOf(fuente) + ":" + codigo;
    }

    public String getTargetAbrev() {
        return this.targetAbrev;
    }

    public void setSourceAbrev(String fuente, String codigo) {
        this.sourceAbrev = String.valueOf(fuente) + ":" + codigo;
    }

    public String getSourceAbrev() {
        return this.sourceAbrev;
    }

    public String getValues() {
        return "[" + this.tx_Translation.getText() + "," + this.ty_Translation.getText() + "," + this.tz_Translation.getText() + "," + this.tx_Rotation.getText() + "," + this.ty_Rotation.getText() + "," + this.tz_Rotation.getText() + "," + this.tScale.getText() + "]";
    }

    public void fillData(String details) {
        RecentTrsPersistence trPersistence = new RecentTrsPersistence();
        TrData[] crsTrDataArray = trPersistence.getArrayOfTrData();
        int iRow = crsTrDataArray.length - 1;
        while (iRow >= 0) {
            if (details.equals(String.valueOf(crsTrDataArray[iRow].getAuthority()) + ":" + crsTrDataArray[iRow].getCode() + " <--> " + crsTrDataArray[iRow].getDetails()) && crsTrDataArray[iRow].getAuthority().equals("USR")) {
                String data = crsTrDataArray[iRow].getDetails();
                data = data.trim().substring(1, data.length() - 1);
                String[] values = data.split(",");
                this.tx_Translation.setText(values[0]);
                this.ty_Translation.setText(values[1]);
                this.tz_Translation.setText(values[2]);
                this.tx_Rotation.setText(values[3]);
                this.ty_Rotation.setText(values[4]);
                this.tz_Rotation.setText(values[5]);
                this.tScale.setText(values[6]);
                break;
            }
            --iRow;
        }
    }

    public void resetData() {
        this.tx_Translation.setText("0");
        this.ty_Translation.setText("0");
        this.tz_Translation.setText("0");
        this.tx_Rotation.setText("0");
        this.ty_Rotation.setText("0");
        this.tz_Rotation.setText("0");
        this.tScale.setText("0");
    }
}

