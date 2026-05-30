/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.swing.JComboBox
 *  javax.units.ConversionException
 *  javax.units.Unit
 *  org.geotools.referencing.crs.AbstractSingleCRS
 *  org.geotools.referencing.datum.DefaultEllipsoid
 *  org.geotools.referencing.datum.DefaultGeodeticDatum
 *  org.geotools.referencing.datum.DefaultPrimeMeridian
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 */
package org.gvsig.crs.gui.panels.wizard;

import com.iver.utiles.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.units.ConversionException;
import javax.units.Unit;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.dialog.ImportNewCrsDialog;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class DefinirDatum
extends JPanel
implements ActionListener,
FocusListener,
ItemListener,
KeyListener {
    private JTextField txtDatum;
    private JTextField txtElipsoide;
    private JTextField txtSemMay;
    private JTextField txtSemMen;
    private JTextField txtInvF;
    private JTextField txtMeridian;
    private JTextField txtLong;
    private JTextField txtNombreCrs;
    private JTextField txtCodigoCrs;
    private JLabel lblDatum;
    private JLabel lblElipsoide;
    private JLabel lblMeridian;
    private JLabel lblLong;
    private JLabel lblNombreCrs;
    private JLabel lblCodigoCrs;
    private JLabel lblSemiejeMayor;
    private JLabel lblSemiejeMenor;
    private JLabel lblInverseFlat;
    private JLabel lblDefinir;
    private JButton btnImportDatum;
    private JButton btnImportElipsoide;
    private JButton btnImportMeridian;
    private JComboBox cbSemMay;
    private JComboBox cbSemMen;
    private JComboBox cbInvF;
    private JComboBox cbLong;
    private JRadioButton rbA_Inv;
    private JRadioButton rbA_B;
    private ButtonGroup groupRadioButton;
    private Dimension bigSize;
    private Dimension smallSize;
    private Dimension dimLabels = new Dimension(110, 15);
    private static final long serialVersionUID = 1L;
    ICrs crs = null;
    private String sourceUnitLong = null;
    private String sourceUnitSemMay = null;
    private String sourceUnitSemMen = null;
    List<String> lengthUnits = null;
    List<String> angularUnits = null;
    int divider = 10000;
    boolean primera = true;
    private static final double EPS = 1.0E-8;

    public DefinirDatum() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.bigSize = new Dimension();
        this.bigSize.width = 350;
        this.smallSize = new Dimension();
        this.smallSize.width = 130;
        this.inicializarNuevo();
    }

    private void inicializarNuevo() {
        JPanel pMain = new JPanel();
        JPanel pDatum = new JPanel();
        JPanel pElipsoide = new JPanel();
        JPanel pMeridian = new JPanel();
        JPanel pNorthis = new JPanel(new FlowLayout(0, 10, 5));
        pMain.setLayout(new BorderLayout());
        pMain.setBorder(BorderFactory.createTitledBorder(CRSI18NConstants.DATUM_KEY));
        pElipsoide.setBorder(BorderFactory.createTitledBorder(CRSI18NConstants.ELLIPSOID_KEY));
        pMeridian.setBorder(BorderFactory.createTitledBorder(CRSI18NConstants.MERIDIAN_KEY));
        pDatum.setLayout(new GridLayout(2, 1));
        JPanel pD1 = new JPanel(new FlowLayout(0, 10, 0));
        pD1.add(this.getLblDatum());
        JPanel pD2 = new JPanel(new FlowLayout(0, 10, 0));
        pD2.add(this.getTxtDatum());
        pD2.add(this.getBtnImportDatum());
        pDatum.add(pD1);
        pDatum.add(pD2);
        pElipsoide.setLayout(new GridLayout(6, 1));
        JPanel pE1 = new JPanel(new FlowLayout(0, 10, 2));
        pE1.add(this.getLblElipsoide());
        JPanel pE2 = new JPanel(new FlowLayout(0, 10, 2));
        pE2.add(this.getTxtElipsoide());
        pE2.add(this.getBtnImportElipsoide());
        JPanel pE3 = new JPanel(new FlowLayout(0, 10, 2));
        pE3.add(this.getLblDefinir());
        pE3.add(this.getRbA_Inv());
        pE3.add(this.getRbA_B());
        JPanel pE4 = new JPanel(new FlowLayout(0, 10, 2));
        pE4.add(this.getLblSemiejeMayor());
        pE4.add(this.getTxtSemMay());
        pE4.add((Component)this.getCbSemMay());
        JPanel pE5 = new JPanel(new FlowLayout(0, 10, 2));
        pE5.add(this.getLblInverseFlat());
        pE5.add(this.getTxtInvF());
        JPanel pE6 = new JPanel(new FlowLayout(0, 10, 2));
        pE6.add(this.getLblSemiejeMenor());
        pE6.add(this.getTxtSemMen());
        pE6.add((Component)this.getCbSemMen());
        pElipsoide.add(pE1);
        pElipsoide.add(pE2);
        pElipsoide.add(pE3);
        pElipsoide.add(pE4);
        pElipsoide.add(pE5);
        pElipsoide.add(pE6);
        this.agruparRadioButtons();
        pMeridian.setLayout(new GridLayout(2, 1));
        JPanel pM1 = new JPanel(new FlowLayout(0, 10, 3));
        pM1.add(this.getLblMeridian());
        pM1.add(this.getTxtMeridian());
        pM1.add(this.getBtnImportMeridian());
        JPanel pM2 = new JPanel(new FlowLayout(0, 10, 3));
        pM2.add(this.getLblLong());
        pM2.add(this.getTxtLong());
        pM2.add((Component)this.getCbLong());
        pMeridian.add(pM1);
        pMeridian.add(pM2);
        pMain.add((Component)pDatum, "North");
        pMain.add((Component)pElipsoide, "Center");
        pMain.add((Component)pMeridian, "South");
        this.add((Component)pMain, "Center");
        pNorthis.add(this.getLblNombreCrs());
        pNorthis.add(this.getTxtNombreCrs());
        pNorthis.add(this.getLblCodigoCrs());
        pNorthis.add(this.getTxtCodigoCrs());
        this.add((Component)pNorthis, "North");
    }

    public JButton getBtnImportDatum() {
        if (this.btnImportDatum == null) {
            this.btnImportDatum = new JButton();
            this.btnImportDatum.setText("...");
            this.btnImportDatum.addActionListener(this);
        }
        return this.btnImportDatum;
    }

    public JButton getBtnImportElipsoide() {
        if (this.btnImportElipsoide == null) {
            this.btnImportElipsoide = new JButton();
            this.btnImportElipsoide.setText("...");
            this.btnImportElipsoide.addActionListener(this);
        }
        return this.btnImportElipsoide;
    }

    public JButton getBtnImportMeridian() {
        if (this.btnImportMeridian == null) {
            this.btnImportMeridian = new JButton();
            this.btnImportMeridian.setText("...");
            this.btnImportMeridian.addActionListener(this);
        }
        return this.btnImportMeridian;
    }

    public JComboBox getCbInvF() {
        if (this.cbInvF == null) {
            List<String> units = this.obtenerItemsUnidades();
            Object[] items = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                items[i] = units.get(i);
                ++i;
            }
            this.cbInvF = new JComboBox(items);
            this.cbInvF.setSelectedIndex(0);
            this.cbInvF.addItemListener((ItemListener)this);
        }
        return this.cbInvF;
    }

    public JComboBox getCbLong() {
        if (this.cbLong == null) {
            List<String> units = this.obtenerItemsUnidadesAngle();
            Object[] items = new String[units.size()];
            String[] tooltips = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                tooltips[i] = units.get(i);
                items[i] = tooltips[i];
                ++i;
            }
            this.cbLong = new JComboBox(items);
            this.cbLong.setRenderer((ListCellRenderer)new ComboBoxTootipRenderer(tooltips));
            this.cbLong.setPreferredSize(new Dimension(230, 25));
            this.cbLong.setSelectedIndex(1);
            this.cbLong.addItemListener((ItemListener)this);
            this.sourceUnitLong = this.getAngularUnit(this.cbLong.getSelectedIndex());
        }
        return this.cbLong;
    }

    public JComboBox getCbSemMay() {
        if (this.cbSemMay == null) {
            List<String> units = this.obtenerItemsUnidades();
            Object[] items = new String[units.size()];
            String[] tooltips = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                tooltips[i] = units.get(i);
                items[i] = tooltips[i];
                ++i;
            }
            this.cbSemMay = new JComboBox(items);
            this.cbSemMay.setRenderer((ListCellRenderer)new ComboBoxTootipRenderer(tooltips));
            this.cbSemMay.setPreferredSize(new Dimension(230, 25));
            this.cbSemMay.setSelectedIndex(0);
            this.cbSemMay.addItemListener((ItemListener)this);
            this.sourceUnitSemMay = this.getLengthUnit(this.cbSemMay.getSelectedIndex());
        }
        return this.cbSemMay;
    }

    public JComboBox getCbSemMen() {
        if (this.cbSemMen == null) {
            List<String> units = this.obtenerItemsUnidades();
            Object[] items = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                items[i] = units.get(i);
                ++i;
            }
            this.cbSemMen = new JComboBox(items);
            this.cbSemMen.setSelectedIndex(0);
            this.cbSemMen.addItemListener((ItemListener)this);
            this.sourceUnitSemMen = this.getLengthUnit(this.cbSemMen.getSelectedIndex());
        }
        return this.cbSemMen;
    }

    private List<String> obtenerItemsUnidades() {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> lengthUnits = new ArrayList<String>();
        String sentence = "SELECT unit_of_meas_name FROM epsg_unitofmeasure WHERE unit_of_meas_type = 'length'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        int in = 0;
        try {
            while (result.next()) {
                String item = result.getString("unit_of_meas_name");
                items.add(in, item.replaceAll(" ", "_"));
                lengthUnits.add(in, item);
                ++in;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        this.setLengthUnit(lengthUnits);
        return items;
    }

    private List<String> obtenerItemsUnidadesAngle() {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> angularUnit = new ArrayList<String>();
        String sentence = "SELECT unit_of_meas_name FROM epsg_unitofmeasure WHERE unit_of_meas_type = 'angle' and factor_b is not null and factor_c is not null";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        int in = 0;
        try {
            while (result.next()) {
                String item = result.getString("unit_of_meas_name");
                items.add(in, item.replaceAll(" ", "_"));
                angularUnit.add(in, item);
                ++in;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        this.setAngularUnit(angularUnit);
        return items;
    }

    public JLabel getLblDatum() {
        if (this.lblDatum == null) {
            this.lblDatum = new JLabel(String.valueOf(CRSI18NConstants.DATUM_NAME_KEY) + ":");
        }
        return this.lblDatum;
    }

    public JLabel getLblElipsoide() {
        if (this.lblElipsoide == null) {
            this.lblElipsoide = new JLabel(String.valueOf(CRSI18NConstants.ELLIPSOID_NAME_KEY) + ":");
        }
        return this.lblElipsoide;
    }

    public JLabel getLblLong() {
        if (this.lblLong == null) {
            this.lblLong = new JLabel(String.valueOf(CRSI18NConstants.LONGITUDE_KEY) + ":");
            this.lblLong.setMinimumSize(this.dimLabels);
            this.lblLong.setPreferredSize(this.dimLabels);
        }
        return this.lblLong;
    }

    public JLabel getLblMeridian() {
        if (this.lblMeridian == null) {
            this.lblMeridian = new JLabel(String.valueOf(CRSI18NConstants.MERIDIAN_NAME_KEY) + ":");
            this.lblMeridian.setMinimumSize(this.dimLabels);
            this.lblMeridian.setPreferredSize(this.dimLabels);
        }
        return this.lblMeridian;
    }

    public JLabel getLblCodigoCrs() {
        if (this.lblCodigoCrs == null) {
            this.lblCodigoCrs = new JLabel(String.valueOf(CRSI18NConstants.CODE_KEY) + ":");
        }
        return this.lblCodigoCrs;
    }

    public JLabel getLblNombreCrs() {
        if (this.lblNombreCrs == null) {
            this.lblNombreCrs = new JLabel(String.valueOf(CRSI18NConstants.NAME_KEY) + ":");
        }
        return this.lblNombreCrs;
    }

    public JLabel getLblInverseFlat() {
        if (this.lblInverseFlat == null) {
            this.lblInverseFlat = new JLabel(String.valueOf(CRSI18NConstants.INVERSE_FLATNESS_KEY) + " (inv_f)");
            this.lblInverseFlat.setMinimumSize(this.dimLabels);
            this.lblInverseFlat.setPreferredSize(this.dimLabels);
        }
        return this.lblInverseFlat;
    }

    public JLabel getLblSemiejeMayor() {
        if (this.lblSemiejeMayor == null) {
            this.lblSemiejeMayor = new JLabel(String.valueOf(CRSI18NConstants.MAJOR_SEMI_AXIS_KEY) + " (a)");
            this.lblSemiejeMayor.setMinimumSize(this.dimLabels);
            this.lblSemiejeMayor.setPreferredSize(this.dimLabels);
        }
        return this.lblSemiejeMayor;
    }

    public JLabel getLblSemiejeMenor() {
        if (this.lblSemiejeMenor == null) {
            this.lblSemiejeMenor = new JLabel(String.valueOf(CRSI18NConstants.MINOR_SEMI_AXIS_KEY) + " (b)");
            this.lblSemiejeMenor.setMinimumSize(this.dimLabels);
            this.lblSemiejeMenor.setPreferredSize(this.dimLabels);
        }
        return this.lblSemiejeMenor;
    }

    public JLabel getLblDefinir() {
        if (this.lblDefinir == null) {
            this.lblDefinir = new JLabel(String.valueOf(CRSI18NConstants.DEFINE_BY_KEY) + ": ");
            Dimension d = new Dimension(200, 15);
            this.lblDefinir.setMinimumSize(d);
            this.lblDefinir.setPreferredSize(d);
        }
        return this.lblDefinir;
    }

    public JRadioButton getRbA_B() {
        if (this.rbA_B == null) {
            this.rbA_B = new JRadioButton("a, b");
            this.rbA_B.addActionListener(this);
        }
        return this.rbA_B;
    }

    public JRadioButton getRbA_Inv() {
        if (this.rbA_Inv == null) {
            this.rbA_Inv = new JRadioButton("a, inv_f");
            Dimension d = new Dimension(100, 15);
            this.rbA_Inv.setMinimumSize(d);
            this.rbA_Inv.setPreferredSize(d);
            this.rbA_Inv.addActionListener(this);
        }
        return this.rbA_Inv;
    }

    private void agruparRadioButtons() {
        if (this.groupRadioButton == null) {
            this.groupRadioButton = new ButtonGroup();
            this.groupRadioButton.add(this.getRbA_B());
            this.groupRadioButton.add(this.getRbA_Inv());
            this.getRbA_Inv().setSelected(true);
            this.getTxtSemMen().setEditable(false);
            this.getCbSemMen().setEnabled(false);
        }
    }

    public JTextField getTxtDatum() {
        if (this.txtDatum == null) {
            this.txtDatum = new JTextField();
            this.bigSize.height = this.txtDatum.getPreferredSize().height;
            this.txtDatum.setPreferredSize(this.bigSize);
            this.txtDatum.addActionListener(this);
        }
        return this.txtDatum;
    }

    public JTextField getTxtElipsoide() {
        if (this.txtElipsoide == null) {
            this.txtElipsoide = new JTextField();
            this.bigSize.height = this.txtElipsoide.getPreferredSize().height;
            this.txtElipsoide.setPreferredSize(this.bigSize);
            this.txtElipsoide.addActionListener(this);
        }
        return this.txtElipsoide;
    }

    public JTextField getTxtInvF() {
        if (this.txtInvF == null) {
            this.txtInvF = new JTextField();
            this.smallSize.height = this.txtInvF.getPreferredSize().height;
            this.txtInvF.setPreferredSize(this.smallSize);
            this.txtInvF.setMinimumSize(this.smallSize);
            this.txtInvF.addActionListener(this);
            this.txtInvF.addKeyListener(this);
            this.txtInvF.addFocusListener(this);
        }
        return this.txtInvF;
    }

    public JTextField getTxtLong() {
        if (this.txtLong == null) {
            this.txtLong = new JTextField();
            this.smallSize.height = this.txtLong.getPreferredSize().height;
            this.txtLong.setPreferredSize(this.smallSize);
            this.txtLong.addActionListener(this);
            this.txtLong.addKeyListener(this);
            this.txtLong.addFocusListener(this);
        }
        return this.txtLong;
    }

    public JTextField getTxtMeridian() {
        if (this.txtMeridian == null) {
            this.txtMeridian = new JTextField();
            this.bigSize.height = this.txtMeridian.getPreferredSize().height;
            Dimension d = new Dimension(280, 15);
            d.height = this.txtMeridian.getPreferredSize().height;
            this.txtMeridian.setPreferredSize(d);
            this.txtMeridian.addActionListener(this);
        }
        return this.txtMeridian;
    }

    public JTextField getTxtSemMay() {
        if (this.txtSemMay == null) {
            this.txtSemMay = new JTextField();
            this.smallSize.height = this.txtSemMay.getPreferredSize().height;
            this.txtSemMay.setPreferredSize(this.smallSize);
            this.txtSemMay.setMinimumSize(this.smallSize);
            this.txtSemMay.addActionListener(this);
            this.txtSemMay.addKeyListener(this);
            this.txtSemMay.addFocusListener(this);
        }
        return this.txtSemMay;
    }

    public JTextField getTxtSemMen() {
        if (this.txtSemMen == null) {
            this.txtSemMen = new JTextField();
            this.smallSize.height = this.txtSemMen.getPreferredSize().height;
            this.txtSemMen.setPreferredSize(this.smallSize);
            this.txtSemMen.setMinimumSize(this.smallSize);
            this.txtSemMen.addActionListener(this);
            this.txtSemMen.addKeyListener(this);
            this.txtSemMen.addFocusListener(this);
        }
        return this.txtSemMen;
    }

    public JTextField getTxtCodigoCrs() {
        if (this.txtCodigoCrs == null) {
            this.txtCodigoCrs = new JTextField();
            this.smallSize.height = this.txtCodigoCrs.getPreferredSize().height;
            this.txtCodigoCrs.setPreferredSize(this.smallSize);
            this.txtCodigoCrs.addActionListener(this);
            this.txtCodigoCrs.addFocusListener(this);
        }
        return this.txtCodigoCrs;
    }

    public JTextField getTxtNombreCrs() {
        if (this.txtNombreCrs == null) {
            this.txtNombreCrs = new JTextField();
            this.smallSize.height = this.txtNombreCrs.getPreferredSize().height;
            this.txtNombreCrs.setPreferredSize(this.smallSize);
            this.txtNombreCrs.addActionListener(this);
        }
        return this.txtNombreCrs;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.getTxtDatum())) {
            System.out.println("txt Datum");
        } else if (e.getSource().equals(this.getTxtElipsoide())) {
            System.out.println("txt Elipsoide");
        } else if (e.getSource().equals(this.getTxtInvF())) {
            System.out.println("txt Inverse Flat");
        } else if (e.getSource().equals(this.getTxtSemMay())) {
            System.out.println("txt Semieje Mayor");
        } else if (e.getSource().equals(this.getTxtSemMen())) {
            System.out.println("txt Semieje Menor");
        } else if (e.getSource().equals(this.getTxtLong())) {
            System.out.println("txt Longitud");
        } else if (e.getSource().equals(this.getTxtMeridian())) {
            System.out.println("txt Meridiano");
        } else if (e.getSource().equals(this.getCbInvF())) {
            System.out.println("combo box Inverse Flat");
        } else if (e.getSource().equals(this.getCbLong())) {
            System.out.println("combo box Longitud");
        } else if (e.getSource().equals(this.getCbSemMay())) {
            System.out.println("combo box Semieje Mayor");
        } else if (e.getSource().equals(this.getCbSemMen())) {
            System.out.println("combo box Semieje Menor");
        } else if (e.getSource().equals(this.getRbA_B())) {
            this.getTxtSemMay().setEditable(true);
            this.getTxtSemMen().setEditable(true);
            this.getTxtInvF().setEditable(false);
            this.getCbSemMen().setEnabled(true);
        } else if (e.getSource().equals(this.getRbA_Inv())) {
            this.getTxtSemMay().setEditable(true);
            this.getTxtSemMen().setEditable(false);
            this.getTxtInvF().setEditable(true);
            this.getCbSemMen().setEnabled(false);
        } else if (e.getSource().equals(this.getBtnImportDatum())) {
            ImportNewCrsDialog newCrs = new ImportNewCrsDialog(CRSI18NConstants.IMPORT_DATUM_KEY);
            DialogManager.addJDialog(newCrs, CRSI18NConstants.IMPORT_DATUM_KEY);
            if (newCrs.getCode() != -1) {
                this.setCrs(newCrs.getCode());
                this.fillDatum(this.getCrs());
            }
        } else if (e.getSource().equals(this.getBtnImportElipsoide())) {
            ImportNewCrsDialog newCrs = new ImportNewCrsDialog(CRSI18NConstants.IMPORT_ELLIPSOID_KEY);
            DialogManager.addJDialog(newCrs, CRSI18NConstants.IMPORT_ELLIPSOID_KEY);
            if (newCrs.getCode() != -1) {
                this.setCrs(newCrs.getCode());
                this.fillEllipsoid(this.getCrs());
            }
        } else if (e.getSource().equals(this.getBtnImportMeridian())) {
            ImportNewCrsDialog newCrs = new ImportNewCrsDialog(CRSI18NConstants.IMPORT_MERIDIAN_KEY);
            DialogManager.addJDialog(newCrs, CRSI18NConstants.IMPORT_MERIDIAN_KEY);
            if (newCrs.getCode() != -1) {
                this.setCrs(newCrs.getCode());
                this.fillPrimeMeridian(this.getCrs());
            } else if (!e.getSource().equals(this.getRbA_Inv())) {
                e.getSource().equals(this.getRbA_B());
            }
        }
    }

    public void fillData(ICrs crs) {
        this.getTxtNombreCrs().setText(crs.getCrsWkt().getGeogcs());
        this.getTxtCodigoCrs().setText(this.getMaxCode());
        this.fillDatum(crs);
    }

    public void fillData(ICrs crs, boolean editing) {
        this.getTxtNombreCrs().setText(crs.getCrsWkt().getGeogcs());
        this.getTxtCodigoCrs().setText("" + crs.getCode());
        this.fillDatum(crs);
    }

    public void fillDatum(ICrs crs) {
        this.getTxtDatum().setText(crs.getCrsWkt().getDatumName());
        this.fillEllipsoid(crs);
        this.fillPrimeMeridian(crs);
    }

    public void fillData(CoordinateReferenceSystem crsGT) {
        AbstractSingleCRS crs = (AbstractSingleCRS)crsGT;
        String authority = crs.getName().toString().split(":")[0];
        this.getTxtNombreCrs().setText(authority);
        this.getTxtCodigoCrs().setText(this.getMaxCode());
        DefaultGeodeticDatum d = (DefaultGeodeticDatum)crs.getDatum();
        String[] val = d.getName().toString().split(":");
        if (val.length < 2) {
            this.getTxtDatum().setText(d.getName().toString().split(":")[0]);
        } else {
            this.getTxtDatum().setText(d.getName().toString().split(":")[1]);
        }
        DefaultEllipsoid ellips = (DefaultEllipsoid)d.getEllipsoid();
        Unit u = ellips.getAxisUnit();
        double semi_major = this.convert(ellips.getSemiMajorAxis(), u.toString());
        double inv_f = ellips.getInverseFlattening();
        val = ellips.getName().toString().split(":");
        if (val.length < 2) {
            this.getTxtElipsoide().setText(val[0]);
        } else {
            this.getTxtElipsoide().setText(val[1]);
        }
        this.getTxtSemMay().setText(String.valueOf(semi_major));
        this.getTxtInvF().setText(String.valueOf(inv_f));
        this.getTxtSemMen().setText(String.valueOf(this.calcularSemMen()));
        DefaultPrimeMeridian primem = (DefaultPrimeMeridian)d.getPrimeMeridian();
        u = primem.getAngularUnit();
        double longitude = this.convert(primem.getGreenwichLongitude(), u.toString());
        val = primem.getName().toString().split(":");
        if (val.length < 2) {
            this.getTxtMeridian().setText(val[0]);
        } else {
            this.getTxtMeridian().setText(val[1]);
        }
        this.getTxtLong().setText("" + longitude);
    }

    public void fillEllipsoid(ICrs crs) {
        this.getTxtElipsoide().setText(crs.getCrsWkt().getSpheroid()[0]);
        String semMay = crs.getCrsWkt().getSpheroid()[1];
        String invF = crs.getCrsWkt().getSpheroid()[2];
        this.getTxtSemMay().setText(semMay);
        this.getTxtInvF().setText(invF);
        double semMen = this.calcularSemMen();
        this.getTxtSemMen().setText(String.valueOf(semMen));
    }

    public void fillPrimeMeridian(ICrs crs) {
        this.getTxtMeridian().setText(crs.getCrsWkt().getPrimen()[0]);
        this.getTxtLong().setText(crs.getCrsWkt().getPrimen()[1]);
    }

    private double calcularSemMen() {
        String cadenaNumerica = this.getTxtSemMay().getText().replaceAll("[^0-9.E]", "");
        if (this.getTxtSemMay().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.MAJOR_SEMI_AXIS_KEY, CRSI18NConstants.WARNING_KEY);
            return 0.0;
        }
        if (this.getTxtInvF().getText().equals("Infinity")) {
            this.getRbA_B().setSelected(true);
            this.getTxtSemMay().setEditable(true);
            this.getTxtSemMen().setEditable(true);
            this.getTxtInvF().setEditable(false);
            this.getCbSemMen().setEnabled(true);
            return Double.parseDouble(this.getTxtSemMay().getText());
        }
        cadenaNumerica = this.getTxtInvF().getText().replaceAll("[^0-9.E-]", "");
        if (this.getTxtInvF().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.INVERSE_FLATNESS_KEY, CRSI18NConstants.WARNING_KEY);
            return 0.0;
        }
        double semMay = Double.parseDouble(this.getTxtSemMay().getText());
        double invF = Double.parseDouble(this.getTxtInvF().getText());
        if (invF != 0.0) {
            return semMay - semMay / invF;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private double calcularInvF() {
        double semMen;
        String cadenaNumerica = this.getTxtSemMay().getText().replaceAll("[^0-9.E]", "");
        if (this.getTxtSemMay().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.MAJOR_SEMI_AXIS_KEY, CRSI18NConstants.WARNING_KEY);
            return 0.0;
        }
        cadenaNumerica = this.getTxtSemMen().getText().replaceAll("[^0-9.E-]", "");
        if (this.getTxtSemMen().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.MINOR_SEMI_AXIS_KEY, CRSI18NConstants.WARNING_KEY);
            return 0.0;
        }
        double semMay = Double.parseDouble(this.getTxtSemMay().getText());
        if (semMay - (semMen = Double.parseDouble(this.getTxtSemMen().getText())) != 0.0) {
            return semMay / (semMay - semMen);
        }
        return Double.POSITIVE_INFINITY;
    }

    public void setCrs(int code) {
        try {
            this.crs = new CrsFactory().getCRS("EPSG:" + code);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
    }

    public ICrs getCrs() {
        return this.crs;
    }

    public void cleanData() {
        String max = this.getMaxCode();
        this.getTxtCodigoCrs().setText(max);
        this.getTxtNombreCrs().setText("");
        this.getTxtDatum().setText("");
        this.getTxtElipsoide().setText("GRS 1980");
        this.getTxtInvF().setText("298.257222101");
        this.getTxtLong().setText("0.0");
        this.getTxtMeridian().setText("Greenwich");
        this.getTxtSemMay().setText("6378137.0");
        double semMen = this.calcularSemMen();
        this.getTxtSemMen().setText("" + semMen);
    }

    private String getMaxCode() {
        String sentence = "SELECT usr_code FROM USR ORDER BY usr_code ASC";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionUsr();
        ResultSet result = Query.select(sentence, connect.getConnection());
        int max = 0;
        int lastValue = 0;
        int goodValue = 1;
        boolean firstAccess = true;
        try {
            while (result.next()) {
                max = result.getInt("usr_code");
                if (firstAccess && max != 1) {
                    return "" + goodValue;
                }
                firstAccess = false;
                if (max - lastValue == 1) {
                    lastValue = max;
                    continue;
                }
                goodValue = lastValue + 1;
                break;
            }
            if (goodValue == 1) {
                goodValue = max + 1;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "" + goodValue;
    }

    public double convert2Meters(String unit, double value) {
        double factor_b = 0.0;
        double factor_c = 0.0;
        String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unit + "'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return value * factor_b / factor_c;
    }

    public double convert2Degree(String unit, double value) {
        double factor_b = 0.0;
        double factor_c = 0.0;
        String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unit + "'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (Math.abs(factor_b / factor_c - Math.PI / 180) < 1.0E-12) {
            return value;
        }
        value = value * factor_b / factor_c;
        return value * 180.0 / Math.PI;
    }

    public double convert(double value, String measure) throws ConversionException {
        if (measure.equals("D.MS")) {
            int deg = (int)((value *= (double)this.divider) / 10000.0);
            int min = (int)((value -= (double)(10000 * deg)) / 100.0);
            value -= (double)(100 * min);
            if (min <= -60 || min >= 60) {
                if ((double)Math.abs(Math.abs(min) - 100) <= 1.0E-8) {
                    deg = min >= 0 ? ++deg : --deg;
                    min = 0;
                } else {
                    throw new ConversionException(String.valueOf(CRSI18NConstants.INVALID_MINUTES_VALUE_KEY) + ": " + min);
                }
            }
            if (value <= -60.0 || value >= 60.0) {
                if (Math.abs(Math.abs(value) - 100.0) <= 1.0E-8) {
                    min = value >= 0.0 ? ++min : --min;
                    value = 0.0;
                } else {
                    throw new ConversionException(String.valueOf(CRSI18NConstants.INVALID_SECONDS_VALUE_KEY) + ": " + value);
                }
            }
            value = (value / 60.0 + (double)min) / 60.0 + (double)deg;
            return value;
        }
        if (measure.equals("grad") || measure.equals("grade")) {
            return value * 180.0 / 200.0;
        }
        if (measure.equals("\u00b0")) {
            return value;
        }
        if (measure.equals("DMS")) {
            return value;
        }
        if (measure.equals("m")) {
            return value;
        }
        if (measure.equals("")) {
            return value;
        }
        throw new ConversionException(String.valueOf(CRSI18NConstants.CONVERSION_NOT_CONSIDERED) + ": " + measure);
    }

    public double convertFromDegree(String unitSource, String unitTarget, double value) {
        double factor_b = 0.0;
        double factor_c = 0.0;
        String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unitSource + "'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        value = value * factor_b / factor_c;
        sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unitTarget + "'";
        connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return value * factor_c / factor_b;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        String val;
        double value;
        String unit;
        if (e.getItemSelectable().equals(this.cbLong)) {
            unit = this.getAngularUnit(((JComboBox)e.getSource()).getSelectedIndex());
            value = Double.valueOf(this.getTxtLong().getText());
            value = this.convertFromDegree(this.sourceUnitLong, unit, value);
            this.sourceUnitLong = unit;
            val = String.valueOf(value);
            this.getTxtLong().setText(val);
        }
        if (e.getItemSelectable().equals(this.cbSemMay)) {
            unit = this.getLengthUnit(((JComboBox)e.getSource()).getSelectedIndex());
            value = Double.valueOf(this.getTxtSemMay().getText());
            value = this.convertFromMeters(this.sourceUnitSemMay, unit, value);
            this.sourceUnitSemMay = unit;
            val = String.valueOf(value);
            this.getTxtSemMay().setText(val);
        }
        if (e.getItemSelectable().equals(this.cbSemMen)) {
            unit = this.getLengthUnit(((JComboBox)e.getSource()).getSelectedIndex());
            value = Double.valueOf(this.getTxtSemMen().getText());
            value = this.convertFromMeters(this.sourceUnitSemMen, unit, value);
            this.sourceUnitSemMen = unit;
            val = String.valueOf(value);
            this.getTxtSemMen().setText(val);
        }
    }

    private boolean notANumber(String cadenaNumerica) {
        int puntos = 0;
        int signos = 0;
        int letras = 0;
        int i = 0;
        while (i < cadenaNumerica.length()) {
            if (cadenaNumerica.charAt(i) == '.') {
                ++puntos;
            } else if (cadenaNumerica.charAt(i) == '-') {
                if (i == 0) {
                    ++signos;
                } else if (i != 0 && cadenaNumerica.charAt(i - 1) != 'E') {
                    signos = 2;
                }
            } else if (cadenaNumerica.charAt(i) == 'E') {
                letras = i == 0 ? 2 : ++letras;
            }
            ++i;
        }
        return letras > 1 || signos > 1 || puntos > 1;
    }

    public double convertFromMeters(String unitSource, String unitTarget, double value) {
        double factor_b = 0.0;
        double factor_c = 0.0;
        String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unitSource.replace("'", "'+char(39)+'") + "'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        value = value * factor_b / factor_c;
        sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE unit_of_meas_name = '" + unitTarget.replace("'", "'+char(39)+'") + "'";
        connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        result = Query.select(sentence, connect.getConnection());
        try {
            result.next();
            factor_b = result.getDouble("factor_b");
            factor_c = result.getDouble("factor_c");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return value * factor_c / factor_b;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.primera = true;
        if (e.getSource() == this.getTxtSemMay() && e.getKeyCode() == 10) {
            this.primera = false;
            if (this.getRbA_Inv().isSelected()) {
                this.getTxtSemMen().setText(String.valueOf(this.calcularSemMen()));
            } else if (this.getRbA_B().isSelected()) {
                this.getTxtInvF().setText(String.valueOf(this.calcularInvF()));
            }
        }
        if (e.getSource() == this.getTxtSemMen() && e.getKeyCode() == 10) {
            this.primera = false;
            this.getTxtInvF().setText(String.valueOf(this.calcularInvF()));
        }
        if (e.getSource() == this.getTxtInvF() && e.getKeyCode() == 10) {
            this.primera = false;
            this.getTxtSemMen().setText(String.valueOf(this.calcularSemMen()));
        }
        if (e.getSource() == this.getTxtLong() && e.getKeyCode() == 10) {
            this.primera = false;
            String cadenaNumerica = this.getTxtLong().getText().replaceAll("[^0-9.E]", "");
            if (this.getTxtLong().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
                DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.LONGITUDE_KEY, CRSI18NConstants.WARNING_KEY);
                this.primera = true;
                return;
            }
        }
        this.primera = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void setLengthUnit(List<String> units) {
        this.lengthUnits = units;
    }

    public void setAngularUnit(List<String> units) {
        this.angularUnits = units;
    }

    public String getLengthUnit(int indice) {
        return this.lengthUnits.get(indice);
    }

    public String getAngularUnit(int indice) {
        return this.angularUnits.get(indice);
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.primera) {
            this.primera = false;
            if (e.getSource() == this.getTxtSemMay()) {
                if (this.getRbA_Inv().isSelected()) {
                    this.getTxtSemMen().setText(String.valueOf(this.calcularSemMen()));
                } else if (this.getRbA_B().isSelected()) {
                    this.getTxtInvF().setText(String.valueOf(this.calcularInvF()));
                }
            }
            if (e.getSource() == this.getTxtSemMen()) {
                this.getTxtInvF().setText(String.valueOf(this.calcularInvF()));
            }
            if (e.getSource() == this.getTxtInvF()) {
                this.getTxtSemMen().setText(String.valueOf(this.calcularSemMen()));
            }
            if (e.getSource() == this.getTxtLong()) {
                String cadenaNumerica = this.getTxtLong().getText().replaceAll("[^0-9.E]", "");
                if (this.getTxtLong().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.LONGITUDE_KEY, CRSI18NConstants.WARNING_KEY);
                    return;
                }
            }
        }
    }

    class ComboBoxTootipRenderer
    extends BasicComboBoxRenderer {
        private static final long serialVersionUID = 1L;
        private String[] tooltips = null;

        public ComboBoxTootipRenderer(String[] tooltips) {
            this.tooltips = tooltips;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
                if (-1 < index) {
                    list.setToolTipText(this.tooltips[index]);
                }
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setFont(list.getFont());
            this.setText(value == null ? "" : value.toString());
            return this;
        }
    }
}

