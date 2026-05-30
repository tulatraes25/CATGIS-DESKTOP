/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.geotools.referencing.wkt.Parser
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 */
package org.gvsig.crs.gui.panels.wizard;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import org.geotools.referencing.wkt.Parser;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.wizard.DefCrsUsr;
import org.gvsig.crs.gui.panels.wizard.DefSistCoordenadas;
import org.gvsig.crs.gui.panels.wizard.DefinirDatum;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class MainPanel
extends JPanel
implements ActionListener,
ChangeListener,
IWindow {
    private static final long serialVersionUID = 1L;
    private JTabbedPane pCard;
    private JPanel pSouth;
    private JButton btnCancelar;
    private JButton btnSiguiente;
    private JButton btnAnterior;
    private JButton btnFinalizar;
    private DefCrsUsr pCrsUsr;
    private DefinirDatum pDatum;
    private DefSistCoordenadas pSistCoord;
    private ICrs currentCrs;
    private String cadWkt = "";
    private int newCrsCode = -1;
    private boolean edit = false;

    public MainPanel(ICrs crs) {
        this.setCrs(crs);
        this.setLayout(new BorderLayout());
        this.add((Component)this.getPCard(), "Center");
        this.add((Component)this.getPSouth(), "South");
        this.getPCrsUsr().getRbCadenaWkt().addActionListener(this);
        this.getPCrsUsr().getRbCrsExistente().addActionListener(this);
        this.getPCrsUsr().getRbNuevoCrs().addActionListener(this);
    }

    public JButton getBtnAnterior() {
        if (this.btnAnterior == null) {
            this.btnAnterior = new JButton();
            this.btnAnterior.setText(CRSI18NConstants.PREVIOUS_KEY);
            this.btnAnterior.addActionListener(this);
        }
        return this.btnAnterior;
    }

    public JButton getBtnCancelar() {
        if (this.btnCancelar == null) {
            this.btnCancelar = new JButton();
            this.btnCancelar.setText(CRSI18NConstants.CANCEL_KEY);
            this.btnCancelar.addActionListener(this);
        }
        return this.btnCancelar;
    }

    public JButton getBtnFinalizar() {
        if (this.btnFinalizar == null) {
            this.btnFinalizar = new JButton();
            this.btnFinalizar.setText(CRSI18NConstants.FINISH_KEY);
            this.btnFinalizar.addActionListener(this);
        }
        return this.btnFinalizar;
    }

    public JButton getBtnSiguiente() {
        if (this.btnSiguiente == null) {
            this.btnSiguiente = new JButton();
            this.btnSiguiente.setText(CRSI18NConstants.NEXT_KEY);
            this.btnSiguiente.addActionListener(this);
        }
        return this.btnSiguiente;
    }

    public JTabbedPane getPCard() {
        if (this.pCard == null) {
            this.pCard = new JTabbedPane();
            this.pCard.addTab(CRSI18NConstants.USER_CRS_KEY, this.getPCrsUsr());
            this.pCard.addTab(CRSI18NConstants.DATUM_KEY, this.getPDatum());
            this.pCard.addTab(CRSI18NConstants.CRS_KEY, this.getPSistCoord());
            this.pCard.setSelectedIndex(0);
            this.pCard.addChangeListener(this);
        }
        return this.pCard;
    }

    public JPanel getPSouth() {
        if (this.pSouth == null) {
            this.pSouth = new JPanel();
            this.pSouth.setLayout(new FlowLayout(2, 5, 5));
            this.pSouth.add(this.getBtnCancelar());
            this.pSouth.add(this.getBtnAnterior());
            this.pSouth.add(this.getBtnSiguiente());
            this.pSouth.add(this.getBtnFinalizar());
            this.getBtnFinalizar().setVisible(false);
            this.getBtnAnterior().setVisible(false);
        }
        return this.pSouth;
    }

    public DefCrsUsr getPCrsUsr() {
        if (this.pCrsUsr == null) {
            this.pCrsUsr = new DefCrsUsr(this.getCrs());
        }
        return this.pCrsUsr;
    }

    public DefinirDatum getPDatum() {
        if (this.pDatum == null) {
            this.pDatum = new DefinirDatum();
        }
        return this.pDatum;
    }

    public DefSistCoordenadas getPSistCoord() {
        if (this.pSistCoord == null) {
            this.pSistCoord = new DefSistCoordenadas();
        }
        return this.pSistCoord;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.getBtnAnterior())) {
            this.getBtnFinalizar().setVisible(false);
            this.getBtnSiguiente().setVisible(true);
            if (this.getPSistCoord().isShowing() && this.isEditing()) {
                this.getBtnAnterior().setVisible(false);
            }
            if (this.getPDatum().isShowing()) {
                this.getBtnAnterior().setVisible(false);
                if (this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                    this.getBtnFinalizar().setVisible(true);
                }
                this.getPCard().setSelectedComponent(this.getPCrsUsr());
            } else if (this.getPSistCoord().isShowing()) {
                this.getPCard().setSelectedComponent(this.getPDatum());
            }
        } else if (e.getSource().equals(this.getBtnSiguiente())) {
            if (this.getPCrsUsr().isShowing() && this.getPCrsUsr().getRbCrsExistente().isSelected()) {
                this.getBtnFinalizar().setVisible(false);
                ICrs crs = this.getPCrsUsr().getCrs();
                if (crs != null) {
                    this.fillData(crs);
                } else {
                    this.fillData(this.getCrs());
                }
            } else if (this.getPCrsUsr().isShowing() && this.getPCrsUsr().getRbNuevoCrs().isSelected()) {
                this.getBtnFinalizar().setVisible(false);
                this.cleanData();
            } else if (this.getPCrsUsr().isShowing() && this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                if (StringUtils.isEmpty((String)this.getPCrsUsr().getTxtAreaWkt().getText())) {
                    DialogFactory.showWarningDialog(this, CRSI18NConstants.WKT_EMPTY_KEY, CRSI18NConstants.WARNING_KEY);
                    return;
                }
                CoordinateReferenceSystem crs = null;
                String wkt = this.getPCrsUsr().getTxtAreaWkt().getText().trim();
                Parser parser = new Parser();
                try {
                    crs = parser.parseCoordinateReferenceSystem(wkt);
                }
                catch (ParseException e1) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e1.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                    return;
                }
                catch (UnsupportedOperationException e2) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e2.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                    return;
                }
                this.fillData(crs);
            }
            if (this.getPCrsUsr().isShowing()) {
                this.getPCard().setSelectedComponent(this.getPDatum());
            } else if (this.getPDatum().isShowing()) {
                this.getPCard().setSelectedComponent(this.getPSistCoord());
            }
            this.getBtnAnterior().setVisible(true);
            if (this.getPDatum().isShowing()) {
                this.getBtnFinalizar().setVisible(true);
                this.getBtnSiguiente().setVisible(false);
                this.getBtnAnterior().setVisible(true);
            }
        } else if (e.getSource().equals(this.getBtnFinalizar())) {
            if (this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                CoordinateReferenceSystem crs = null;
                String wkt = this.getPCrsUsr().getTxtAreaWkt().getText();
                Parser parser = new Parser();
                if (StringUtils.isEmpty((String)this.getPCrsUsr().getTxtAreaWkt().getText())) {
                    DialogFactory.showWarningDialog(this, CRSI18NConstants.WKT_EMPTY_KEY, CRSI18NConstants.WARNING_KEY);
                    return;
                }
                try {
                    crs = parser.parseCoordinateReferenceSystem(wkt);
                }
                catch (ParseException e1) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e1.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                    return;
                }
                catch (UnsupportedOperationException e2) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e2.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                    return;
                }
                this.fillData(crs);
            }
            this.getDataAndUpdate();
        } else if (e.getSource().equals(this.getBtnCancelar())) {
            DialogManager.closeJDialog(this);
        } else if (e.getSource().equals(this.getPCrsUsr().getRbCadenaWkt())) {
            this.getPCrsUsr().habilitarExistente(false);
            this.getPCrsUsr().habilitarWkt(true);
            this.getBtnFinalizar().setVisible(true);
        } else if (e.getSource().equals(this.getPCrsUsr().getRbCrsExistente())) {
            this.getPCrsUsr().habilitarExistente(true);
            this.getPCrsUsr().habilitarWkt(false);
            this.getBtnFinalizar().setVisible(false);
        } else if (e.getSource().equals(this.getPCrsUsr().getRbNuevoCrs())) {
            this.getPCrsUsr().habilitarExistente(false);
            this.getPCrsUsr().habilitarWkt(false);
            this.getBtnFinalizar().setVisible(false);
        }
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle("wz_titulo");
        m_viewinfo.setWidth(560);
        m_viewinfo.setHeight(400);
        return m_viewinfo;
    }

    public ICrs getCrs() {
        return this.currentCrs;
    }

    public void setCrs(ICrs crs) {
        this.currentCrs = crs;
    }

    public void fillData(ICrs crs) {
        this.getPDatum().fillData(crs);
        this.getPSistCoord().fillData(crs);
    }

    public void fillData(ICrs crs, boolean editing) {
        this.getPDatum().fillData(crs, editing);
        this.getPSistCoord().fillData(crs);
    }

    public void cleanData() {
        this.getPDatum().cleanData();
        this.getPSistCoord().cleanData();
    }

    public void fillData(CoordinateReferenceSystem crs) {
        this.getPDatum().fillData(crs);
        this.getPSistCoord().fillData(crs);
    }

    private void getDataAndUpdate() {
        String name;
        String datum;
        ResultSet result;
        String sentence;
        CRSRepositoryConnection conn = new CRSRepositoryConnection();
        conn.setConnectionUsr();
        String codeCrs = "";
        if (StringUtils.isEmpty((String)this.getPDatum().getTxtSemMay().getText())) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.WKT_EMPTY_KEY) + ": " + CRSI18NConstants.MAJOR_SEMI_AXIS_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        if (StringUtils.isEmpty((String)this.getPDatum().getTxtLong().getText())) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.WKT_EMPTY_KEY) + ": " + CRSI18NConstants.LONGITUDE_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        if (StringUtils.isEmpty((String)this.getPDatum().getTxtCodigoCrs().getText())) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.WKT_EMPTY_KEY) + ": " + CRSI18NConstants.CODE_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        String cadenaNumerica = this.getPDatum().getTxtSemMay().getText().replaceAll("[^0-9.E-]", "");
        if (this.getPDatum().getTxtSemMay().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.MAJOR_SEMI_AXIS_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        cadenaNumerica = this.getPDatum().getTxtLong().getText().replaceAll("[^0-9.E-]", "");
        if (this.getPDatum().getTxtLong().getText().length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.WKT_EMPTY_KEY) + ": " + CRSI18NConstants.LONGITUDE_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        codeCrs = this.getPDatum().getTxtCodigoCrs().getText().replaceAll("[^0-9]", "");
        if (this.getPDatum().getTxtCodigoCrs().getText().length() != codeCrs.length()) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.CODE_KEY, CRSI18NConstants.WARNING_KEY);
            return;
        }
        double value = Double.valueOf(this.getPDatum().getTxtSemMay().getText());
        String unit = this.getPDatum().getLengthUnit(this.getPDatum().getCbSemMay().getSelectedIndex());
        double semMay = this.getPDatum().convert2Meters(unit, value);
        value = Double.valueOf(this.getPDatum().getTxtLong().getText());
        unit = this.getPDatum().getAngularUnit(this.getPDatum().getCbLong().getSelectedIndex());
        double longitude = this.getPDatum().convert2Degree(unit, value);
        String spheroidName = this.getPDatum().getTxtElipsoide().getText();
        if (StringUtils.isEmpty((String)spheroidName)) {
            spheroidName = "no_name";
        }
        String[] spheroid = new String[]{spheroidName, "" + semMay, this.getPDatum().getTxtInvF().getText()};
        String meridianName = this.getPDatum().getTxtMeridian().getText();
        if (StringUtils.isEmpty((String)meridianName)) {
            meridianName = "no_name";
        }
        String[] primem = new String[]{meridianName, "" + longitude};
        String[] authority = new String[]{"\"USR\"", codeCrs};
        if (!this.isEditing()) {
            sentence = "SELECT usr_code FROM USR WHERE usr_code = " + authority[1];
            result = Query.select(sentence, conn.getConnection());
            try {
                if (result.next()) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.CRS_REPEATED_KEY) + ": " + authority[1], CRSI18NConstants.WARNING_KEY);
                    return;
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        if (StringUtils.isEmpty((String)(datum = this.getPDatum().getTxtDatum().getText()))) {
            datum = "no_name";
        }
        if (this.getPSistCoord().getRbGeografico().isSelected()) {
            name = this.getPDatum().getTxtNombreCrs().getText();
            if (StringUtils.isEmpty((String)name)) {
                name = "no_name";
            }
            this.cadWkt = "GEOGCS[\"" + name + "\", DATUM[\"" + datum + "\", SPHEROID[\"" + spheroid[0] + "\", " + spheroid[1] + ", " + spheroid[2] + "]], " + "PRIMEM[\"" + primem[0] + "\", " + primem[1] + "], UNIT[\"Degree\", " + Math.PI / 180 + "]]";
            sentence = !this.isEditing() ? "INSERT INTO USR VALUES(" + authority[1] + ",'" + this.cadWkt + "','','" + name + "','" + datum + "')" : "UPDATE USR SET usr_wkt='" + this.cadWkt + "'," + "usr_proj='',usr_geog='" + this.getPDatum().getTxtNombreCrs().getText() + "'," + "usr_datum='" + datum + "' " + "WHERE usr_code = " + authority[1];
        } else {
            String projection;
            name = this.getPDatum().getTxtNombreCrs().getText();
            if (StringUtils.isEmpty((String)name)) {
                name = "no_name";
            }
            if (StringUtils.isEmpty((String)(projection = this.getPSistCoord().getTxtNombreProy().getText()))) {
                projection = "no_name";
            }
            this.cadWkt = "PROJCS[\"" + projection + "\", GEOGCS[\"" + name + "\", DATUM[\"" + datum + "\", SPHEROID[\"" + spheroid[0] + "\", " + spheroid[1] + ", " + spheroid[2] + "]], " + "PRIMEM[\"" + primem[0] + "\", " + primem[1] + "], UNIT[\"Degree\", " + Math.PI / 180 + "]], PROJECTION[\"" + this.getPSistCoord().getProjection(this.getPSistCoord().getCbProyeccion().getSelectedIndex()) + "\"], ";
            List<String> maxValues = null;
            List<String> minValues = null;
            int paramPos = 0;
            try {
                maxValues = this.getPSistCoord().getProj4().getProj4ProjectionParameterMaxValues(this.getPSistCoord().getPos());
                minValues = this.getPSistCoord().getProj4().getProj4ProjectionParameterMinValues(this.getPSistCoord().getPos());
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
            int j = 0;
            int i = 0;
            while (i < this.getPSistCoord().getTableParametros().getRowCount()) {
                if (((String)this.getPSistCoord().getTableParametros().getValueAt(i, 1)).equals("")) {
                    this.getPSistCoord().getTableParametros().setValueAt("0", i, 1);
                }
                cadenaNumerica = ((String)this.getPSistCoord().getTableParametros().getValueAt(i, 1)).replaceAll("[^0-9.E-]", "");
                if (((String)this.getPSistCoord().getTableParametros().getValueAt(i, 1)).length() != cadenaNumerica.length() || this.notANumber(cadenaNumerica)) {
                    DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY) + ": " + CRSI18NConstants.PARAMETER_KEY + " " + (String)this.getPSistCoord().getTableParametros().getValueAt(i, 0), CRSI18NConstants.WARNING_KEY);
                    return;
                }
                String condition = StringUtils.trim((String)((String)this.getPSistCoord().getTableParametros().getValueAt(i, 0)));
                String param = StringUtils.trim((String)this.getPSistCoord().getTrueParametersNames(j));
                paramPos = this.findPositionParameter(param);
                if (paramPos != -1) {
                    value = Double.parseDouble(cadenaNumerica);
                    if (!condition.equals("semi_major") && !condition.equals("semi_minor")) {
                        if (!param.equals("semi_major") && !param.equals("semi_minor")) {
                            double maxValue = Double.parseDouble(maxValues.get(paramPos));
                            double minValue = Double.parseDouble(minValues.get(paramPos));
                            if (value > maxValue || value < minValue) {
                                DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.INVALID_DOMAIN_KEY) + ": " + CRSI18NConstants.PARAMETER_KEY + " " + condition, CRSI18NConstants.WARNING_KEY);
                                return;
                            }
                            ++j;
                        }
                        value = 0.0;
                        unit = "";
                        String type = (String)this.getPSistCoord().getTableParametros().getValueAt(i, 2);
                        value = Double.parseDouble((String)this.getPSistCoord().getTableParametros().getValueAt(i, 1));
                        this.cadWkt = String.valueOf(this.cadWkt) + "PARAMETER[\"" + this.getPSistCoord().getTrueParametersNames(i) + "\", " + value + "], ";
                    }
                } else {
                    ++j;
                }
                ++i;
            }
            this.cadWkt = String.valueOf(this.cadWkt) + "UNIT[\"Meters\", 1.0]]";
            sentence = !this.isEditing() ? "INSERT INTO USR VALUES(" + authority[1] + ",'" + this.cadWkt + "','" + projection + "','" + name + "','" + datum + "')" : "UPDATE USR SET usr_wkt='" + this.cadWkt + "'," + "usr_proj='" + projection + "'," + "usr_geog='" + name + "'," + "usr_datum='" + datum + "' " + "WHERE usr_code = " + authority[1];
        }
        try {
            conn.update(sentence);
            conn.shutdown();
        }
        catch (SQLException e) {
            DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.ERROR_UPDATING_DB_KEY) + ":\n" + e.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
            return;
        }
        this.setNewCrsCode(Integer.parseInt(authority[1]));
        try {
            ICrs crs = new CrsFactory().getCRS("USR:" + authority[1]);
            crs.getProj4String();
        }
        catch (CrsException e1) {
            DialogFactory.showWarningDialog(this, e1.getMessage(), CRSI18NConstants.WARNING_KEY);
            conn.setConnectionUsr();
            sentence = "DELETE FROM USR WHERE usr_code =" + authority[1];
            result = Query.select(sentence, conn.getConnection());
            try {
                conn.shutdown();
            }
            catch (SQLException arg0) {
                arg0.printStackTrace();
            }
            return;
        }
        DialogManager.closeJDialog(this);
    }

    private int findPositionParameter(String param) {
        List<Object> parameters = new ArrayList();
        try {
            parameters = this.getPSistCoord().getProj4().getProj4ProjectionParameters(this.getPSistCoord().getPos());
            int pos = -1;
            int i = 0;
            do {
                pos = this.getPSistCoord().getProj4().findProjectionParameters(param, ((String)parameters.get(i)).toString());
            } while (++i != parameters.size() && pos == -1);
            if (pos != -1) {
                param = this.getPSistCoord().getProj4().getProj4ProjectionParameterName(pos);
            }
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        int i = 0;
        while (i < parameters.size()) {
            String actualParam = ((String)parameters.get(i)).trim();
            if (actualParam.equals(param)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public void setNewCrsCode(int code) {
        this.newCrsCode = code;
    }

    public int getNewCrsCode() {
        return this.newCrsCode;
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

    public void setEditing(boolean edit) {
        this.edit = edit;
    }

    public boolean isEditing() {
        return this.edit;
    }

    public void setEditingPanel() {
        this.pCard.setSelectedComponent(this.getPDatum());
        this.pCard.setEnabledAt(0, false);
        this.fillData(this.getCrs(), true);
        this.getPDatum().getTxtCodigoCrs().setEnabled(false);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.getPCard()) {
            int i;
            if (this.getPCrsUsr().getHasChanged()) {
                ICrs crs;
                if (this.getPCrsUsr().getRbCrsExistente().isSelected()) {
                    crs = this.getPCrsUsr().getCrs();
                    if (crs != null) {
                        this.fillData(crs);
                    } else {
                        this.fillData(this.getCrs());
                    }
                    this.getPCrsUsr().setHasChange(false);
                } else if (this.getPCrsUsr().getRbNuevoCrs().isSelected()) {
                    this.cleanData();
                    this.getPCrsUsr().setHasChange(false);
                } else if (this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                    this.cleanData();
                    if (StringUtils.isEmpty((String)this.getPCrsUsr().getTxtAreaWkt().getText())) {
                        DialogFactory.showWarningDialog(this, CRSI18NConstants.WKT_EMPTY_KEY, CRSI18NConstants.WARNING_KEY);
                        this.getPCrsUsr().setHasChange(false);
                        this.getPCard().setSelectedComponent(this.getPCrsUsr());
                        return;
                    }
                    crs = null;
                    String wkt = StringUtils.trim((String)this.getPCrsUsr().getTxtAreaWkt().getText());
                    Parser parser = new Parser();
                    try {
                        crs = parser.parseCoordinateReferenceSystem(wkt);
                    }
                    catch (ParseException e1) {
                        DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e1.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                        this.getPCrsUsr().setHasChange(false);
                        this.getPCard().setSelectedComponent(this.getPCrsUsr());
                        return;
                    }
                    catch (UnsupportedOperationException e2) {
                        DialogFactory.showWarningDialog(this, String.valueOf(CRSI18NConstants.PROBLEM_WITH_WKT_STRING_KEY) + ":\n" + e2.getLocalizedMessage(), CRSI18NConstants.WARNING_KEY);
                        this.getPCrsUsr().setHasChange(false);
                        this.getPCard().setSelectedComponent(this.getPCrsUsr());
                        return;
                    }
                    this.fillData((CoordinateReferenceSystem)crs);
                    this.getPCrsUsr().setHasChange(false);
                }
            }
            if ((i = ((JTabbedPane)e.getSource()).getSelectedIndex()) == 0) {
                this.getBtnAnterior().setVisible(false);
                this.getBtnSiguiente().setVisible(true);
                if (this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                    this.getPCrsUsr().setHasChange(true);
                    this.getBtnFinalizar().setVisible(true);
                } else {
                    this.getBtnFinalizar().setVisible(false);
                }
            } else if (i == 1 && !this.isEditing()) {
                this.getBtnAnterior().setVisible(true);
                this.getBtnSiguiente().setVisible(true);
                this.getBtnFinalizar().setVisible(false);
                if (this.getPCrsUsr().getRbCadenaWkt().isSelected()) {
                    this.getBtnFinalizar().setVisible(true);
                }
            } else if (i == 1 && this.isEditing()) {
                this.getBtnAnterior().setVisible(false);
                this.getBtnSiguiente().setVisible(true);
                this.getBtnFinalizar().setVisible(false);
            } else if (i == 2) {
                this.getBtnAnterior().setVisible(true);
                this.getBtnSiguiente().setVisible(false);
                this.getBtnFinalizar().setVisible(true);
            }
        }
    }

    public Object getWindowProfile() {
        return null;
    }
}

