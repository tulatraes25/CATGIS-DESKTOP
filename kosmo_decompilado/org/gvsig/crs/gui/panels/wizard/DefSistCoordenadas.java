/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.units.ConversionException
 *  javax.units.Unit
 *  org.apache.commons.lang.StringUtils
 *  org.geotools.referencing.crs.DefaultProjectedCRS
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 */
package org.gvsig.crs.gui.panels.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.units.ConversionException;
import javax.units.Unit;
import org.apache.commons.lang.StringUtils;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Proj4;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.dialog.ImportNewCrsDialog;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.saig.core.util.DialogManager;

public class DefSistCoordenadas
extends JPanel
implements ActionListener,
ItemListener {
    private static final long serialVersionUID = 1L;
    private JPanel top;
    private JPanel proyectadoPanel;
    private JPanel cardPanel;
    private JRadioButton rbGeografico;
    private JRadioButton rbProyectado;
    private ButtonGroup coordGroup;
    private JButton btnImportar = null;
    private JLabel lblProyeccion;
    private JLabel lblNombreProy;
    private JTextField txtNombreProy;
    private JComboBox cbProyeccion;
    private JComboBox cbUnits;
    private JTable tableParametros;
    private JScrollPane scrollTable;
    private DefaultTableModel model = null;
    private int theigth = 140;
    private int twidth = 300;
    static final String PROYECTADOPANEL = "Proyectado";
    private String sourceUnit = null;
    private List<String> projections = null;
    private List<String> trueParametersNames = null;
    ICrs crs;
    Proj4 proj4 = null;
    private int pos;
    int divider = 10000;
    private static final double EPS = 1.0E-8;

    public DefSistCoordenadas() {
        BorderLayout bl = new BorderLayout();
        try {
            this.proj4 = new Proj4();
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        bl.setVgap(5);
        bl.setHgap(5);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(bl);
        this.add((Component)this.getTop(), "North");
        this.add((Component)this.getCardPanel(), "Center");
    }

    public JPanel getCardPanel() {
        if (this.cardPanel == null) {
            this.cardPanel = new JPanel();
            this.cardPanel.setLayout(new CardLayout());
            this.cardPanel.add(PROYECTADOPANEL, this.getProyectadoPanel());
        }
        return this.cardPanel;
    }

    public JRadioButton getRbGeografico() {
        if (this.rbGeografico == null) {
            this.rbGeografico = new JRadioButton(CRSI18NConstants.CRS_GEOGRAPHIC_2D_KEY);
            this.rbGeografico.addActionListener(this);
        }
        return this.rbGeografico;
    }

    public JRadioButton getRbProyectado() {
        if (this.rbProyectado == null) {
            this.rbProyectado = new JRadioButton(CRSI18NConstants.CRS_PROJECTED_KEY);
            this.rbProyectado.addActionListener(this);
        }
        return this.rbProyectado;
    }

    public JPanel getProyectadoPanel() {
        if (this.proyectadoPanel == null) {
            this.proyectadoPanel = new JPanel(new BorderLayout(5, 5));
            this.proyectadoPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
            JPanel p1 = new JPanel(new FlowLayout(0, 3, 5));
            p1.add(this.getLblNombreProy());
            p1.add(this.getTxtNombreProy());
            p1.add(this.getBtnImportar());
            JPanel p2 = new JPanel(new FlowLayout(2, 3, 5));
            p2.add(this.getLblProyeccion());
            p2.add(this.getCbProyeccion());
            JPanel pNorth = new JPanel(new GridLayout(2, 1));
            pNorth.add(p1);
            pNorth.add(p2);
            this.proyectadoPanel.add((Component)pNorth, "North");
            this.proyectadoPanel.add((Component)this.getScrollTable(), "Center");
        }
        return this.proyectadoPanel;
    }

    public JPanel getTop() {
        if (this.top == null) {
            this.top = new JPanel();
            this.top.add(this.getRbGeografico());
            this.top.add(this.getRbProyectado());
            this.agruparRadioButtons();
            this.top.setBorder(new TitledBorder(CRSI18NConstants.CRS_KEY));
        }
        return this.top;
    }

    private void agruparRadioButtons() {
        if (this.coordGroup == null) {
            this.coordGroup = new ButtonGroup();
            this.coordGroup.add(this.getRbProyectado());
            this.coordGroup.add(this.getRbGeografico());
            this.getRbProyectado().setSelected(true);
        }
    }

    public JLabel getLblProyeccion() {
        if (this.lblProyeccion == null) {
            this.lblProyeccion = new JLabel();
            this.lblProyeccion.setText(CRSI18NConstants.PROJECTION_KEY);
        }
        return this.lblProyeccion;
    }

    public JTable getTableParametros() {
        if (this.tableParametros == null) {
            this.tableParametros = new JTable();
            this.tableParametros.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            this.model = (DefaultTableModel)this.tableParametros.getModel();
            Object[][] data = new Object[][]{{"", "", "Metros"}, {"", "", "Metros"}, {"", "", "Metros"}, {"", "", "Metros"}, {"", "", "Metros"}, {"", "", "Metros"}, {"", "", "Metros"}};
            String col1 = CRSI18NConstants.PARAMETER_KEY;
            String col2 = CRSI18NConstants.VALUE_KEY;
            String col3 = CRSI18NConstants.UNITS_KEY;
            Object[] headers = new Object[]{col1, col2, col3};
            List<String> units = this.obtenerItemsUnidades();
            String[] items = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                items[i] = units.get(i);
                ++i;
            }
            this.model = new DefaultTableModel(data, headers){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0 && column != 2;
                }
            };
            this.tableParametros.setModel(this.model);
            this.tableParametros.setPreferredScrollableViewportSize(new Dimension(this.twidth, this.theigth));
            this.tableParametros.getTableHeader().setReorderingAllowed(false);
            this.ajustarTamanoTabla();
            this.fillTable(this.getProjection(0));
        }
        return this.tableParametros;
    }

    public JComboBox getCbUnits() {
        if (this.cbUnits == null) {
            List<String> units = this.obtenerItemsUnidades();
            String[] items = new String[units.size()];
            int i = 0;
            while (i < units.size()) {
                items[i] = units.get(i);
                ++i;
            }
            this.cbUnits = new JComboBox<String>(items);
            this.cbUnits.setEditable(false);
            this.cbUnits.setToolTipText(CRSI18NConstants.UNITS_KEY);
            this.sourceUnit = (String)this.cbUnits.getItemAt(0);
            this.cbUnits.addItemListener(this);
        }
        return this.cbUnits;
    }

    private List<String> obtenerItemsUnidades() {
        ArrayList<String> items = new ArrayList<String>();
        String sentence = "SELECT unit_of_meas_name FROM epsg_unitofmeasure ";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            while (result.next()) {
                String item = result.getString("unit_of_meas_name");
                items.add(item);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private List<String> obtenerItemsUnidadesAngular() {
        ArrayList<String> items = new ArrayList<String>();
        String sentence = "SELECT unit_of_meas_name FROM epsg_unitofmeasure WHERE unit_of_meas_type = 'angle'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            while (result.next()) {
                String item = result.getString("unit_of_meas_name");
                items.add(item);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private List<String> obtenerItemsUnidadesScale() {
        ArrayList<String> items = new ArrayList<String>();
        String sentence = "SELECT unit_of_meas_name FROM epsg_unitofmeasure WHERE unit_of_meas_type = 'scale'";
        CRSRepositoryConnection connect = new CRSRepositoryConnection();
        connect.setConnectionEPSG();
        ResultSet result = Query.select(sentence, connect.getConnection());
        try {
            while (result.next()) {
                String item = result.getString("unit_of_meas_name");
                items.add(item);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public JScrollPane getScrollTable() {
        if (this.scrollTable == null) {
            this.scrollTable = new JScrollPane(this.getTableParametros());
            this.scrollTable.setHorizontalScrollBarPolicy(30);
        }
        return this.scrollTable;
    }

    public JComboBox getCbProyeccion() {
        if (this.cbProyeccion == null) {
            List<String> projections = this.obtainProjections();
            String[] items = new String[projections.size()];
            int i = 0;
            while (i < projections.size()) {
                items[i] = projections.get(i);
                ++i;
            }
            this.cbProyeccion = new JComboBox<String>(items);
            this.cbProyeccion.setEditable(false);
            this.cbProyeccion.setToolTipText(CRSI18NConstants.PROJECTED_KEY);
            this.cbProyeccion.addItemListener(this);
        }
        return this.cbProyeccion;
    }

    public JLabel getLblNombreProy() {
        if (this.lblNombreProy == null) {
            this.lblNombreProy = new JLabel(String.valueOf(CRSI18NConstants.PROJECTION_NAME_KEY) + ": ");
        }
        return this.lblNombreProy;
    }

    public JTextField getTxtNombreProy() {
        if (this.txtNombreProy == null) {
            this.txtNombreProy = new JTextField();
            Dimension d = new Dimension(320, 20);
            this.txtNombreProy.setPreferredSize(d);
            this.txtNombreProy.setMinimumSize(d);
        }
        return this.txtNombreProy;
    }

    private List<String> obtainProjections() {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> util = new ArrayList<String>();
        int i = 0;
        while (i < this.proj4.getProjectionNameList().size()) {
            try {
                items.add(i, this.proj4.getProj4ProjectionName(i));
                util.add(i, this.proj4.getProj4ProjectionName(i));
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
            ++i;
        }
        int i2 = 0;
        while (i2 < items.size() - 1) {
            int j = i2 + 1;
            while (j < items.size()) {
                String tempItem1 = (String)items.get(i2);
                String tempItem2 = (String)items.get(j);
                String tempUtil1 = (String)util.get(i2);
                String tempUtil2 = (String)util.get(j);
                if (((String)items.get(j)).compareTo(tempItem1) < 0) {
                    items.remove(i2);
                    items.add(i2, tempItem2);
                    items.remove(j);
                    items.add(j, tempItem1);
                    util.remove(i2);
                    util.add(i2, tempUtil2);
                    util.remove(j);
                    util.add(j, tempUtil1);
                }
                ++j;
            }
            ++i2;
        }
        this.setProjection(util);
        return items;
    }

    public void ajustarTamanoTabla() {
        TableColumn column = null;
        this.getTableParametros().setRowHeight(20);
        column = this.getTableParametros().getColumnModel().getColumn(0);
        column.setPreferredWidth(30);
        column = this.getTableParametros().getColumnModel().getColumn(1);
        column.setPreferredWidth(90);
        column = this.getTableParametros().getColumnModel().getColumn(2);
        column.setPreferredWidth(120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CardLayout cl = (CardLayout)this.cardPanel.getLayout();
        if (e.getSource().equals(this.getRbProyectado())) {
            this.habilitarControles();
        } else if (e.getSource().equals(this.getRbGeografico())) {
            this.deshabilitarControles();
        } else if (e.getSource().equals(this.getBtnImportar())) {
            ImportNewCrsDialog newCrs = new ImportNewCrsDialog(CRSI18NConstants.IMPORT_PROJECTION_KEY);
            DialogManager.addJDialog(newCrs, CRSI18NConstants.IMPORT_PROJECTION_KEY);
            if (newCrs.getCode() != -1) {
                this.setCrs(newCrs.getCode());
                this.fillData(this.getCrs());
            }
        }
    }

    public void fillData(ICrs crs) {
        if (!crs.getCrsWkt().getProjcs().equals("")) {
            this.getRbProyectado().setSelected(true);
            int index = this.proj4.findProjection(crs.getCrsWkt().getProjection());
            String proj2Compare = "";
            try {
                proj2Compare = this.proj4.getProj4ProjectionName(index);
            }
            catch (CrsException e1) {
                e1.printStackTrace();
            }
            int i = 0;
            while (i < this.getCbProyeccion().getItemCount()) {
                if (this.getProjection(i).equals(proj2Compare)) {
                    this.getCbProyeccion().setSelectedIndex(i);
                    break;
                }
                ++i;
            }
            this.getTxtNombreProy().setText(crs.getCrsWkt().getProjcs());
            this.setPos(this.proj4.findProjection(crs.getCrsWkt().getProjection()));
            int numRow = this.model.getRowCount();
            while (numRow != 0) {
                this.model.removeRow(--numRow);
            }
            Object[] data = new Object[3];
            data[2] = "Metros";
            ArrayList<String> trueParametersNames = new ArrayList<String>();
            try {
                int i2 = 0;
                while (i2 < crs.getCrsWkt().getParam_name().length) {
                    String param = StringUtils.trim((String)crs.getCrsWkt().getParam_name()[i2]);
                    if (!param.equals("semi_major") && !param.equals("semi_minor")) {
                        trueParametersNames.add(param);
                        data[0] = param;
                        data[1] = crs.getCrsWkt().getParam_value()[i2];
                        int pos = this.proj4.findProjectionParameter(param);
                        String unit = this.proj4.getProjectionParameterUnitList(pos);
                        if (unit.equals("Angular")) {
                            data[2] = "Degree";
                        } else if (unit.equals("Unitless")) {
                            data[2] = "Unitless";
                        } else if (unit.equals("Linear")) {
                            data[2] = "Meters";
                        }
                        this.model.addRow(data);
                    }
                    ++i2;
                }
                this.setTrueParametersNames(trueParametersNames);
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        } else {
            this.getRbGeografico().setSelected(true);
            CardLayout cl = (CardLayout)this.cardPanel.getLayout();
            this.deshabilitarControles();
        }
    }

    public void fillData(CoordinateReferenceSystem crs) {
        if (crs instanceof DefaultProjectedCRS) {
            DefaultProjectedCRS sour = (DefaultProjectedCRS)crs;
            String[] val = sour.getName().toString().split(":");
            if (val.length < 2) {
                this.getTxtNombreProy().setText(val[0]);
            } else {
                this.getTxtNombreProy().setText(val[1]);
            }
            val = sour.getConversionFromBase().getMethod().getName().toString().split(":");
            String proj = val.length < 2 ? val[0] : val[1];
            int index = this.proj4.findProjection(proj);
            String proj2Compare = "";
            try {
                proj2Compare = this.proj4.getProj4ProjectionName(index);
            }
            catch (CrsException e1) {
                e1.printStackTrace();
            }
            int i = 0;
            while (i < this.getCbProyeccion().getItemCount()) {
                if (this.getProjection(i).equals(proj2Compare)) {
                    this.getCbProyeccion().setSelectedIndex(i);
                    break;
                }
                ++i;
            }
            String[] param_name = new String[sour.getConversionFromBase().getParameterValues().values().size()];
            String[] param_value = new String[sour.getConversionFromBase().getParameterValues().values().size()];
            int i2 = 0;
            while (i2 < sour.getConversionFromBase().getParameterValues().values().size()) {
                String str = sour.getConversionFromBase().getParameterValues().values().get(i2).toString();
                Unit u = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).getUnit();
                double value = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).doubleValue();
                value = this.convert(value, u.toString());
                param_name[i2] = str.split("=")[0];
                param_value[i2] = String.valueOf(value);
                ++i2;
            }
            this.setPos(this.proj4.findProjection(proj));
            int numRow = this.model.getRowCount();
            while (numRow != 0) {
                this.model.removeRow(--numRow);
            }
            Object[] data = new Object[3];
            ArrayList<String> trueParametersNames = new ArrayList<String>();
            try {
                int i3 = 0;
                while (i3 < param_name.length) {
                    String param = param_name[i3].trim();
                    if (!param.equals("semi_major") && !param.equals("semi_minor")) {
                        trueParametersNames.add(param);
                        data[0] = param;
                        data[1] = param_value[i3];
                        int pos = this.proj4.findProjectionParameter(param);
                        String unit = this.proj4.getProjectionParameterUnitList(pos);
                        if (unit.equals("Angular")) {
                            data[2] = "Degree";
                        } else if (unit.equals("Unitless")) {
                            data[2] = "Unitless";
                        } else if (unit.equals("Linear")) {
                            data[2] = "Meters";
                        }
                        this.model.addRow(data);
                    }
                    ++i3;
                }
                this.setTrueParametersNames(trueParametersNames);
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        } else {
            this.getRbGeografico().setSelected(true);
            CardLayout cl = (CardLayout)this.cardPanel.getLayout();
            this.deshabilitarControles();
        }
    }

    public void fillTable(String projection) {
        try {
            int index = this.proj4.findProjection(projection);
            List<String> params = this.proj4.getProj4ProjectionParameters(index);
            List<String> defaultValuesParams = this.proj4.getProj4ProjectionParameterDefaultValues(index);
            this.setPos(index);
            String[] parameters = new String[params.size()];
            String[] defaultValues = new String[params.size()];
            String[] units = new String[params.size()];
            int i = 0;
            while (i < params.size()) {
                parameters[i] = params.get(i).toString();
                int pos = this.proj4.findProjectionParameter(parameters[i]);
                defaultValues[i] = defaultValuesParams.get(i).toString();
                units[i] = this.proj4.getProjectionParameterUnitList(pos);
                ++i;
            }
            int numRow = this.model.getRowCount();
            while (numRow != 0) {
                this.model.removeRow(--numRow);
            }
            Object[] data = new Object[3];
            data[1] = "0";
            data[2] = "Metros";
            String[] items = null;
            List<String> unit = this.obtenerItemsUnidades();
            items = new String[unit.size()];
            int j = 0;
            while (j < unit.size()) {
                items[j] = unit.get(j);
                ++j;
            }
            ArrayList<String> trueParametersNames = new ArrayList<String>();
            int i2 = 0;
            while (i2 < parameters.length) {
                trueParametersNames.add(i2, parameters[i2]);
                data[0] = parameters[i2];
                data[1] = defaultValues[i2];
                data[2] = units[i2].equals("Angular") ? "Degree" : (units[i2].equals("Unitless") ? "Unitless" : "Meters");
                this.model.addRow(data);
                ++i2;
            }
            this.setTrueParametersNames(trueParametersNames);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getItemSelectable().equals(this.cbProyeccion)) {
            String op = (String)e.getItem();
            int option = ((JComboBox)e.getSource()).getSelectedIndex();
            this.fillTable(this.getProjection(option));
        }
        if (e.getItemSelectable().equals(this.cbUnits)) {
            double value = Double.valueOf((String)this.getTableParametros().getValueAt(0, 1));
            value = this.convertFromMeters(this.sourceUnit, (String)e.getItem(), value);
            this.sourceUnit = (String)e.getItem();
            String val = String.valueOf(value);
            this.getTableParametros().setValueAt(val, 0, 1);
        }
    }

    public void cleanData() {
        this.getTxtNombreProy().setText("");
        this.getCbProyeccion().setSelectedIndex(0);
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

    public double convert2Unitless(String unit, double value) {
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
        value = value * factor_b / factor_c;
        return value * 180.0 / 200.0;
    }

    public double convertFromMeters(String unitSource, String unitTarget, double value) {
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

    public double convertFromUnitless(String unitSource, String unitTarget, double value) {
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

    public Proj4 getProj4() {
        return this.proj4;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return this.pos;
    }

    private void deshabilitarControles() {
        this.getProyectadoPanel().setEnabled(false);
        this.getTableParametros().setEnabled(false);
        this.getTableParametros().getTableHeader().setEnabled(false);
        this.getTxtNombreProy().setEnabled(false);
        this.getLblNombreProy().setEnabled(false);
        this.getLblProyeccion().setEnabled(false);
        this.getCbProyeccion().setEnabled(false);
        this.getBtnImportar().setEnabled(false);
    }

    private void habilitarControles() {
        this.getProyectadoPanel().setEnabled(true);
        this.getTableParametros().setEnabled(true);
        this.getTableParametros().getTableHeader().setEnabled(true);
        this.getTxtNombreProy().setEnabled(true);
        this.getLblNombreProy().setEnabled(true);
        this.getLblProyeccion().setEnabled(true);
        this.getCbProyeccion().setEnabled(true);
        this.getBtnImportar().setEnabled(true);
    }

    public JButton getBtnImportar() {
        if (this.btnImportar == null) {
            this.btnImportar = new JButton("...");
            this.btnImportar.addActionListener(this);
        }
        return this.btnImportar;
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

    public void setProjection(List<String> proj) {
        this.projections = proj;
    }

    public String getProjection(int indice) {
        return this.projections.get(indice);
    }

    public void setTrueParametersNames(List<String> names) {
        this.trueParametersNames = names;
    }

    public String getTrueParametersNames(int indice) {
        return this.trueParametersNames.get(indice);
    }
}

