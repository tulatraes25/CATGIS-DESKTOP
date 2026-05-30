/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  au.com.objectix.jgridshift.GridShiftFile
 *  au.com.objectix.jgridshift.SubGrid
 *  com.iver.utiles.XMLEntity
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.panels;

import au.com.objectix.jgridshift.GridShiftFile;
import au.com.objectix.jgridshift.SubGrid;
import com.iver.andami.ConfigurationException;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.utiles.XMLEntity;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.panels.FiltroNadgrids;
import org.gvsig.crs.gui.panels.TreePanel;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.core.util.XMLUtils;
import org.saig.jump.lang.I18N;

public class TransformationNadgridsPanel
extends JPanel
implements IWindow,
ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TransformationNadgridsPanel.class);
    private JPanel groupRadioButton = null;
    private JLabel jLabelChooser = null;
    private JRadioButton jRadioButtonSource = null;
    private JRadioButton jRadioButtonTarget = null;
    private IProjection firstProj;
    private String nadFile = null;
    String[] targetAuthority;
    String targetAbrev = "";
    String sourceAbrev = "";
    private String cadWKT = "";
    private String dataPath = "./crs/data/";
    private int codeEpsg;
    private JPanel jPanelOpen;
    private JPanel jPanelSelectNad;
    private JLabel jLabelOpenGsb;
    private JFileChooser openFileChooser;
    private JButton jButtonOpen;
    private TreePanel treePanel = null;
    private JComboBox jComboNadFile = null;
    private JLabel jLabelSelectNad = null;
    private XMLEntity xml = null;
    boolean targetNad = false;
    boolean setRadioButtons = false;
    private static final String XMLCRS = "crs.xml";

    public TransformationNadgridsPanel(boolean optional) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.setRadioButtons = optional;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(CRSI18NConstants.GRIDS_KEY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel pNorth = new JPanel();
        pNorth.setBorder(new EmptyBorder(1, 10, 1, 10));
        pNorth.setLayout(new GridLayout(4, 1));
        pNorth.add(this.getJPanelOpen());
        pNorth.add(this.getJPanelSelectNad());
        JPanel lbl = new JPanel(new FlowLayout(0, 0, 10));
        lbl.add(this.getJLabelChooser());
        if (this.setRadioButtons) {
            pNorth.add(lbl);
            pNorth.add(this.getGroupRadioButton());
        }
        this.add((Component)pNorth, "North");
        this.add((Component)this.getTreePanel(), "Center");
    }

    private JPanel getGroupRadioButton() {
        if (this.groupRadioButton == null) {
            this.groupRadioButton = new JPanel();
            this.groupRadioButton.setLayout(new GridLayout(1, 0));
            this.groupRadioButton.add(this.getJRadioButtonSource());
            this.groupRadioButton.add(this.getJRadioButtonTarget());
        }
        return this.groupRadioButton;
    }

    private JRadioButton getJRadioButtonSource() {
        if (this.jRadioButtonSource == null) {
            this.jRadioButtonSource = new JRadioButton();
            this.jRadioButtonSource.setSelected(true);
            this.jRadioButtonSource.addActionListener(this);
        }
        this.jRadioButtonSource.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.layer-coordinate-reference-system")) + "(" + this.getSourceAbrev() + ")");
        return this.jRadioButtonSource;
    }

    private JRadioButton getJRadioButtonTarget() {
        if (this.jRadioButtonTarget == null) {
            this.jRadioButtonTarget = new JRadioButton();
            this.jRadioButtonTarget.setSelected(false);
            this.jRadioButtonTarget.addActionListener(this);
        }
        this.jRadioButtonTarget.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.view-coordinate-reference-system")) + "(" + this.getTargetAbrev() + ")");
        return this.jRadioButtonTarget;
    }

    private JLabel getJLabelChooser() {
        this.jLabelChooser = new JLabel();
        this.jLabelChooser.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.grid-file-calculated-in")) + ":");
        return this.jLabelChooser;
    }

    private JPanel getJPanelOpen() {
        if (this.jPanelOpen == null) {
            this.jPanelOpen = new JPanel();
            this.jPanelOpen.setLayout(new FlowLayout(0, 5, 5));
            this.jPanelOpen.add((Component)this.getJLabelOpenGsb(), null);
            this.jPanelOpen.add((Component)this.getJButtonOpen(), null);
        }
        return this.jPanelOpen;
    }

    private JLabel getJLabelOpenGsb() {
        if (this.jLabelOpenGsb == null) {
            this.jLabelOpenGsb = new JLabel();
            this.jLabelOpenGsb.setPreferredSize(new Dimension(130, 20));
            this.jLabelOpenGsb.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.gsb-import")) + ":");
        }
        return this.jLabelOpenGsb;
    }

    private JFileChooser getOpenFileChooser() {
        if (this.openFileChooser == null) {
            this.openFileChooser = new JFileChooser();
            this.openFileChooser.setEnabled(false);
            this.openFileChooser.addChoosableFileFilter(new FiltroNadgrids());
        }
        return this.openFileChooser;
    }

    private JButton getJButtonOpen() {
        if (this.jButtonOpen == null) {
            this.jButtonOpen = new JButton();
            this.jButtonOpen.setText("");
            this.jButtonOpen.setPreferredSize(new Dimension(20, 20));
            this.jButtonOpen.setText("...");
            this.jButtonOpen.addActionListener(this);
        }
        return this.jButtonOpen;
    }

    public ICrs getProjection() {
        if (this.jRadioButtonSource.isSelected()) {
            try {
                this.setNad(false);
                ICrs crs = CrsRepositoryManager.getInstance().getCRS(this.getSourceAbrev());
                crs.setTransformationParams("+nadgrids=" + this.getNadFile(), null);
                return crs;
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
                return null;
            }
        }
        this.setNad(true);
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS(this.getSourceAbrev());
            crs.setTransformationParams(null, "+nadgrids=" + this.getNadFile());
            return crs;
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public void setProjection(IProjection proj) {
        this.firstProj = proj;
    }

    public void setNad(boolean nadg) {
        this.targetNad = nadg;
    }

    public boolean getNad() {
        return this.targetNad;
    }

    public void setCode(int cod) {
        this.codeEpsg = cod;
    }

    public int getCode() {
        return this.codeEpsg;
    }

    public void setWKT(String cad) {
        this.cadWKT = cad;
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setTargetAuthority(String[] authority) {
        this.targetAuthority = authority;
        this.setTargetAbrev(this.targetAuthority[0], this.targetAuthority[1]);
        this.getJRadioButtonTarget();
    }

    public String[] getTargetAuthority() {
        return this.targetAuthority;
    }

    public void setTargetAbrev(String fuente, String codigo) {
        this.targetAbrev = String.valueOf(fuente) + ":" + codigo;
    }

    public String getTargetAbrev() {
        return this.targetAbrev;
    }

    public void setSourceAbrev(String fuente, String codigo) {
        this.sourceAbrev = String.valueOf(fuente) + ":" + codigo;
        this.getJRadioButtonSource();
    }

    public String getSourceAbrev() {
        return this.sourceAbrev;
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.grids"));
        return m_viewinfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getJRadioButtonSource()) {
            this.jRadioButtonSource.setSelected(true);
            this.jRadioButtonTarget.setSelected(false);
        }
        if (e.getSource() == this.getJRadioButtonTarget()) {
            this.jRadioButtonTarget.setSelected(true);
            this.jRadioButtonSource.setSelected(false);
        }
        if (e.getSource() == this.getJButtonOpen()) {
            this.jPanelOpen.add((Component)this.getOpenFileChooser(), null);
            int returnVal = this.openFileChooser.showOpenDialog(this);
            if (returnVal == 0) {
                File inFile = this.openFileChooser.getSelectedFile();
                String fileName = inFile.getName();
                RandomAccessFile raFile = null;
                try {
                    raFile = new RandomAccessFile(inFile.getAbsolutePath(), "r");
                }
                catch (FileNotFoundException ex) {
                    LOGGER.error((Object)ex);
                    this.getJComboNadFile().setSelectedIndex(0);
                    this.getTreePanel().setRoot(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.the-grids-file-can-not-be-found"));
                    this.setNadFile(null);
                    return;
                }
                GridShiftFile gsf = new GridShiftFile();
                try {
                    gsf.loadGridShiftFile(raFile);
                }
                catch (Exception ex) {
                    LOGGER.error((Object)ex);
                    this.getJComboNadFile().setSelectedIndex(0);
                    this.getTreePanel().setRoot(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.the-grids-file-format-is-unknown"));
                    this.setNadFile(null);
                    return;
                }
                File outFile = new File(String.valueOf(this.dataPath) + fileName);
                FileInputStream in = null;
                FileOutputStream out = null;
                try {
                    in = new FileInputStream(inFile);
                    out = new FileOutputStream(outFile);
                }
                catch (FileNotFoundException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
                byte[] buf = new byte[1024];
                try {
                    int len;
                    while ((len = ((InputStream)in).read(buf)) > 0) {
                        ((OutputStream)out).write(buf, 0, len);
                    }
                    ((InputStream)in).close();
                    ((OutputStream)out).close();
                }
                catch (IOException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
                boolean exists = false;
                int item = 0;
                while (item < this.getJComboNadFile().getItemCount() && !exists) {
                    if (this.getJComboNadFile().getItemAt(item).equals(fileName)) {
                        exists = true;
                    }
                    ++item;
                }
                if (!exists) {
                    this.getJComboNadFile().addItem(fileName);
                }
                this.getJComboNadFile().setSelectedItem(fileName);
                this.setNadFile(fileName);
            }
        }
    }

    public TreePanel getTreePanel() {
        if (this.treePanel == null) {
            this.treePanel = new TreePanel(String.valueOf(CRSI18NConstants.GRIDS_IN_KEY) + " : " + this.nadFile);
            this.treePanel.getTree().expandRow(0);
            this.treePanel.setPanelSize(530, 150);
        }
        return this.treePanel;
    }

    public void initializeTree() {
        RandomAccessFile raFile = null;
        try {
            raFile = new RandomAccessFile(String.valueOf(this.dataPath) + this.nadFile, "r");
        }
        catch (FileNotFoundException e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.getTreePanel().setRoot(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.the-grids-file-can-not-be-found"));
            this.setNadFile(null);
            return;
        }
        GridShiftFile gsf = new GridShiftFile();
        try {
            gsf.loadGridShiftFile(raFile);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.getTreePanel().setRoot(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.the-grids-file-format-is-unknown"));
            this.setNadFile(null);
            return;
        }
        SubGrid[] subGrid = gsf.getSubGridTree();
        int i = 0;
        while (i < subGrid.length) {
            this.getTreePanel().addClass(subGrid[i].getSubGridName(), i);
            this.getTreePanel().addEntry(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.minimum-longitude")) + ": " + String.valueOf(subGrid[i].getMaxLon() / -3600.0) + "\u00ba", subGrid[i].getSubGridName(), "");
            this.getTreePanel().addEntry(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.minimum-latitude")) + ": " + String.valueOf(subGrid[i].getMinLat() / 3600.0) + "\u00ba", subGrid[i].getSubGridName(), "");
            this.getTreePanel().addEntry(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.maximum-longitude")) + ": " + String.valueOf(String.valueOf(subGrid[i].getMinLon() / -3600.0) + "\u00ba"), subGrid[i].getSubGridName(), "");
            this.getTreePanel().addEntry(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.maximum-latitude")) + ": " + String.valueOf(String.valueOf(subGrid[i].getMaxLat() / 3600.0) + "\u00ba"), subGrid[i].getSubGridName(), "");
            this.getTreePanel().addEntry(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.node-number")) + ": " + String.valueOf(subGrid[i].getNodeCount()), subGrid[i].getSubGridName(), "");
            this.getTreePanel().addEntry(String.valueOf(CRSI18NConstants.DETAILS_KEY) + ": " + String.valueOf(subGrid[i].getDetails()), subGrid[i].getSubGridName(), "");
            ++i;
        }
        this.getTreePanel().getTree().expandRow(0);
        this.getTreePanel().getList().setText(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.warning-the-transformation-will-be-applied-inside-the-grids-limits"));
    }

    public JComboBox getJComboNadFile() {
        if (this.jComboNadFile == null) {
            this.jComboNadFile = new JComboBox();
            this.jComboNadFile.setPreferredSize(new Dimension(200, 25));
            this.jComboNadFile.setEditable(false);
            this.jComboNadFile.addItem(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.select")) + "...");
            File dataDir = new File(this.dataPath);
            int i = 0;
            while (i < dataDir.list().length) {
                if (dataDir.list()[i].substring(dataDir.list()[i].lastIndexOf(46) + 1).equals("gsb")) {
                    this.jComboNadFile.addItem(dataDir.list()[i]);
                }
                ++i;
            }
            if (this.getNadFile() != null) {
                this.jComboNadFile.setSelectedItem(this.getNadFile());
            }
        }
        return this.jComboNadFile;
    }

    public JLabel getJLabelSelectNad() {
        if (this.jLabelSelectNad == null) {
            this.jLabelSelectNad = new JLabel(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.TransformationNadgridsPanel.grids-file")) + ": ");
            this.jLabelSelectNad.setPreferredSize(new Dimension(130, 25));
        }
        return this.jLabelSelectNad;
    }

    public JPanel getJPanelSelectNad() {
        if (this.jPanelSelectNad == null) {
            this.jPanelSelectNad = new JPanel();
            this.jPanelSelectNad.setLayout(new FlowLayout(0, 5, 5));
            this.jPanelSelectNad.add(this.getJLabelSelectNad());
            this.jPanelSelectNad.add(this.getJComboNadFile());
        }
        return this.jPanelSelectNad;
    }

    public void setNadFile(String nad) {
        this.nadFile = nad;
    }

    public String getNadFile() {
        return this.nadFile;
    }

    public void saveNadFileName(String name) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
            int child = 0;
            while (child < this.xml.getChildrenCount()) {
                if (this.xml.getChild(child).getPropertyName(0).equals("nadFile")) {
                    this.xml.removeChild(child);
                }
                ++child;
            }
            XMLEntity xmlEnt = new XMLEntity();
            xmlEnt.putProperty("nadFile", name);
            this.xml.addChild(xmlEnt);
            XMLUtils.persistenceToXML(this.xml, XMLCRS);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    private String restoreNadFileName() {
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyName(0).equals("nadFile")) {
                String fileName = this.xml.getChild(child).getPropertyValue(0);
                File dataDir = new File(this.dataPath);
                int i = 0;
                while (i < dataDir.list().length) {
                    if (dataDir.list()[i].equals(fileName)) {
                        return fileName;
                    }
                    ++i;
                }
            }
            ++child;
        }
        return null;
    }

    public void fillData(String details) {
        RecentTrsPersistence trPersistence = new RecentTrsPersistence();
        TrData[] crsTrDataArray = trPersistence.getArrayOfTrData();
        int iRow = crsTrDataArray.length - 1;
        while (iRow >= 0) {
            if (details.equals(String.valueOf(crsTrDataArray[iRow].getAuthority()) + ":" + crsTrDataArray[iRow].getCode() + " <--> " + crsTrDataArray[iRow].getDetails()) && crsTrDataArray[iRow].getAuthority().equals("NADGR")) {
                String[] data = crsTrDataArray[iRow].getDetails().split(" ");
                String fichero = data[0];
                String authority = data[1].substring(1, data[1].length() - 1);
                int i = 0;
                while (i < this.getJComboNadFile().getItemCount()) {
                    if (fichero.equals((String)this.getJComboNadFile().getItemAt(i))) {
                        this.getJComboNadFile().setSelectedIndex(i);
                        break;
                    }
                    ++i;
                }
                if (authority.equals(this.getSourceAbrev())) {
                    this.getJRadioButtonSource().setSelected(true);
                    this.getJRadioButtonTarget().setSelected(false);
                    break;
                }
                this.getJRadioButtonSource().setSelected(false);
                this.getJRadioButtonTarget().setSelected(true);
                break;
            }
            --iRow;
        }
    }

    public void resetData() {
        this.getJRadioButtonSource().setSelected(true);
        this.getJRadioButtonTarget().setSelected(false);
        this.getJComboNadFile().setSelectedIndex(0);
    }

    public Object getWindowProfile() {
        return 8;
    }
}

