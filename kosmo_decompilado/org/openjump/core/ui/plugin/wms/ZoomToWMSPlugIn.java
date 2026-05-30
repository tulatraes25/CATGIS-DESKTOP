/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.openjump.core.ui.plugin.wms;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.Capabilities;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.saig.jump.lang.I18N;

public class ZoomToWMSPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn.zoom-to-wms-layer");
    public static final Icon ICON = IconLoader.icon("ZoomToLayer.gif");
    protected PlugInContext context;
    protected Object[][] values = null;
    protected String[] columnNames = null;
    protected JTable infoTable = null;
    protected TableColumnModel tcm = null;
    protected JScrollPane infoTableSc = null;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ZoomToWMSPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory enableCheckFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(enableCheckFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(enableCheckFactory.createExactlyNLayerablesMustBeSelectedCheck(1, WMSLayer.class));
        check.add(enableCheckFactory.createSelectedLayerMustBeActiveCheck());
        return check;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.context = context;
        List<MapLayer> mapLayerOfChoosenLayers = this.getMapLayerOfChoosenLayers(context);
        String selectedSRS = this.getSelectedSRS(context);
        Hashtable<String, BoundingBox> boundingBoxesForSRS = this.getBoundingBoxesForSRS(mapLayerOfChoosenLayers, selectedSRS);
        this.zoomToBoundingBox(context, boundingBoxesForSRS, selectedSRS);
        return true;
    }

    private WMSLayer[] getSelectedWMSLayer(PlugInContext context) {
        Collection<WMSLayer> listWMS = context.getLayerNamePanel().selectedNodes(WMSLayer.class);
        Object[] obWMSLayer = listWMS.toArray();
        int anzSelectedWMSLayer = Array.getLength(obWMSLayer);
        if (anzSelectedWMSLayer <= 0) {
            return null;
        }
        WMSLayer[] wmsLayer = new WMSLayer[anzSelectedWMSLayer];
        int i = 0;
        while (i < anzSelectedWMSLayer) {
            wmsLayer[i] = (WMSLayer)obWMSLayer[i];
            ++i;
        }
        return wmsLayer;
    }

    private List<MapLayer> getMapLayerOfChoosenLayers(PlugInContext context) throws Exception {
        ArrayList<MapLayer> mapLayerOfChoosenLayers = new ArrayList<MapLayer>();
        ArrayList<String> wmsLayerNames = new ArrayList<String>();
        WMSLayer[] wmsLayer = this.getSelectedWMSLayer(context);
        if (wmsLayer == null) {
            JOptionPane.showMessageDialog(context.getWorkbenchFrame(), I18N.getString("org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn.no-wms-layer-selected"));
            return null;
        }
        int i = 0;
        while (i < Array.getLength(wmsLayer)) {
            List<String> wmsList = wmsLayer[i].getLayerNames();
            int k = 0;
            while (k < wmsList.size()) {
                String name = wmsList.get(k);
                wmsLayerNames.add(name);
                ++k;
            }
            ++i;
        }
        List<MapLayer> allLayer = this.getAllMapLayer(context);
        int i2 = 0;
        while (i2 < wmsLayerNames.size()) {
            String name = (String)wmsLayerNames.get(i2);
            int k = 0;
            while (k < allLayer.size()) {
                MapLayer mapLayer = allLayer.get(k);
                String mapLayerTitle = mapLayer.getTitle();
                String mapLayerName = mapLayer.getName();
                if (mapLayerTitle != null && mapLayerName != null) {
                    if (mapLayerTitle.indexOf(name) >= 0 || mapLayerName.indexOf(name) >= 0) {
                        mapLayerOfChoosenLayers.add(mapLayer);
                    }
                } else if (mapLayerTitle != null) {
                    if (mapLayerTitle.indexOf(name) >= 0) {
                        mapLayerOfChoosenLayers.add(mapLayer);
                    }
                } else if (mapLayerName != null && mapLayerName.indexOf(name) >= 0) {
                    mapLayerOfChoosenLayers.add(mapLayer);
                }
                ++k;
            }
            ++i2;
        }
        return mapLayerOfChoosenLayers;
    }

    private String getSelectedSRS(PlugInContext context) {
        String selectedSRS = "0";
        WMSLayer[] wmsLayer = this.getSelectedWMSLayer(context);
        int i = 0;
        while (i < Array.getLength(wmsLayer)) {
            selectedSRS = wmsLayer[i].getSrs();
            if ((selectedSRS = selectedSRS.toLowerCase()) != null) {
                if (selectedSRS.indexOf("4326") >= 0) {
                    selectedSRS = "LatLon";
                }
            } else {
                selectedSRS = "LatLon";
            }
            ++i;
        }
        return selectedSRS;
    }

    private Hashtable<String, BoundingBox> getBoundingBoxesForSRS(List<MapLayer> mapLayerList, String srs) {
        Hashtable<String, BoundingBox> boundingBoxesForSRS = new Hashtable<String, BoundingBox>();
        int i = 0;
        while (i < mapLayerList.size()) {
            int anzBB;
            MapLayer mapLayer = mapLayerList.get(i);
            List<BoundingBox> boundingBoxList = mapLayer.getAllBoundingBoxList();
            BoundingBox latLonBoundingBox = mapLayer.getLatLonBoundingBox();
            if (latLonBoundingBox != null) {
                boundingBoxList.add(latLonBoundingBox);
            }
            if ((anzBB = boundingBoxList.size()) == 0) {
                System.out.println(String.valueOf(I18N.getString("org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn.no-bounding-box")) + mapLayer.getTitle());
            } else {
                HashSet<String> doppelt = new HashSet<String>();
                int zaehler = 0;
                int k = 0;
                while (k < anzBB) {
                    BoundingBox tmpBB = boundingBoxList.get(k);
                    String tmpSRS = tmpBB.getSRS().toLowerCase();
                    if (tmpSRS.equals(srs.toLowerCase())) {
                        String key = mapLayer.getTitle();
                        if (!doppelt.add(key)) {
                            key = String.valueOf(key) + " (" + ++zaehler + ")";
                        }
                        boundingBoxesForSRS.put(key, tmpBB);
                    }
                    ++k;
                }
            }
            ++i;
        }
        return boundingBoxesForSRS;
    }

    private JComboBox makeComboBox(Hashtable<String, BoundingBox> boundingBoxesForSRS) {
        JComboBox<Object> comboBox = new JComboBox<String>();
        if (boundingBoxesForSRS.size() > 0) {
            Object[] keys = boundingBoxesForSRS.keySet().toArray();
            Arrays.sort(keys);
            comboBox = new JComboBox<Object>(keys);
        } else {
            comboBox.addItem(I18N.getString("org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn.no-bounding-boxes-available"));
        }
        return comboBox;
    }

    private void zoomToBoundingBox(PlugInContext context, Hashtable<String, BoundingBox> boundingBoxesForSRS, String selectedSRS) throws Exception {
        if (boundingBoxesForSRS.size() == 1) {
            String key = boundingBoxesForSRS.keySet().iterator().next();
            BoundingBox selectedBB = boundingBoxesForSRS.get(key);
            this.zoomToSelectedBoundingBox(selectedBB);
            return;
        }
        JComboBox comboBox = this.makeComboBox(boundingBoxesForSRS);
        JPanel jp = new JPanel();
        JButton jb = new JButton("?");
        jb.setActionCommand("showInfoTable");
        jb.addActionListener(new AL());
        String tmpLatLon = "";
        if (selectedSRS.indexOf("4326") >= 0) {
            tmpLatLon = " (LatLon)";
        }
        jp.add(comboBox);
        jp.add(jb);
        int back = JOptionPane.showConfirmDialog(context.getWorkbenchFrame(), jp, String.valueOf(I18N.getString("org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn.bounding-box-for")) + " " + selectedSRS + tmpLatLon, 2, 1);
        if (back == 2 || back < 0) {
            return;
        }
        BoundingBox selectedBB = boundingBoxesForSRS.get(comboBox.getSelectedItem());
        this.zoomToSelectedBoundingBox(selectedBB);
    }

    private void zoomToSelectedBoundingBox(BoundingBox selectedBB) throws NoninvertibleTransformException {
        if (selectedBB == null) {
            return;
        }
        String tmpSRS = selectedBB.getSRS();
        if (tmpSRS.toLowerCase().indexOf("latlon") >= 0) {
            tmpSRS = "EPSG:4326";
        }
        String message = String.valueOf(tmpSRS) + " (" + Math.round(selectedBB.getMinX()) + ", " + Math.round(selectedBB.getMinY()) + ") (" + Math.round(selectedBB.getMaxX()) + ", " + Math.round(selectedBB.getMaxY()) + ")";
        this.context.getWorkbenchFrame().setStatusMessage(message);
        Coordinate min = new Coordinate(selectedBB.getMinX(), selectedBB.getMinY());
        Coordinate max = new Coordinate(selectedBB.getMaxX(), selectedBB.getMaxY());
        Envelope env = new Envelope(min, max);
        this.context.getLayerViewPanel().getViewport().zoom(env);
        this.context.getLayerViewPanel().fireSelectionChanged(true);
        JInternalFrame intFrame = this.context.getActiveInternalFrame();
        intFrame.updateUI();
    }

    private List<MapLayer> getAllMapLayer(PlugInContext context) throws Exception {
        WMSLayer[] wmsLayer = this.getSelectedWMSLayer(context);
        if (wmsLayer == null || Array.getLength(wmsLayer) == 0) {
            return null;
        }
        WMService wmService = wmsLayer[0].getService();
        Capabilities cap = wmService.getCapabilities();
        MapLayer topLayer = cap.getTopLayer();
        List<MapLayer> allMapLayer = topLayer.getLayerList();
        return allMapLayer;
    }

    private void showInformationTable(PlugInContext context) throws Exception {
        this.values = this.getMapLayerInformationForTable(context);
        this.columnNames = MapLayerAttributes.getColumnNames();
        InfoTableModel itm = new InfoTableModel();
        this.infoTable = new JTable(itm);
        JTableHeader th = this.infoTable.getTableHeader();
        th.setReorderingAllowed(false);
        th.addMouseListener(new MASort());
        this.tcm = this.infoTable.getColumnModel();
        TableColumn tc0 = this.tcm.getColumn(0);
        TableColumn tc1 = this.tcm.getColumn(1);
        TableColumn tc2 = this.tcm.getColumn(2);
        TableColumn tc3 = this.tcm.getColumn(3);
        TableColumn tc4 = this.tcm.getColumn(4);
        TableColumn tc5 = this.tcm.getColumn(5);
        TableColumn tc6 = this.tcm.getColumn(6);
        tc0.setMinWidth(160);
        tc1.setMinWidth(120);
        tc2.setMinWidth(70);
        tc3.setMinWidth(90);
        tc4.setMinWidth(90);
        tc5.setMinWidth(90);
        tc6.setMinWidth(90);
        th.setResizingAllowed(true);
        this.infoTable.setAutoResizeMode(0);
        this.infoTableSc = new JScrollPane(this.infoTable);
        this.infoTableSc.setPreferredSize(new Dimension(735, 300));
        JOptionPane.showMessageDialog(context.getWorkbenchFrame(), this.infoTableSc, "InfoTable", 1);
    }

    public void sortTable(int sortAfter) {
        Object[] mapLayerAttributes = this.toMapLayerAttributesArray(this.values);
        MapLayerAttributes.setSortAfter(sortAfter);
        Arrays.sort(mapLayerAttributes);
        this.getMapLayerInformationForTable((MapLayerAttributes[])mapLayerAttributes);
        this.infoTable.updateUI();
    }

    private Object[][] getMapLayerInformationForTable(PlugInContext context) throws Exception {
        Object[][] mapLayerInformationForTable = null;
        MapLayerAttributes mapLayerAttr = new MapLayerAttributes();
        ArrayList<MapLayerAttributes> mapLayerRows = new ArrayList<MapLayerAttributes>();
        List<MapLayer> mapLayerList = this.getAllMapLayer(context);
        if (mapLayerList == null) {
            return null;
        }
        int anzLayer = mapLayerList.size();
        if (anzLayer == 0) {
            return null;
        }
        int i = 0;
        while (i < anzLayer) {
            MapLayer mapLayer = mapLayerList.get(i);
            mapLayerRows.addAll(mapLayerAttr.getMapLayerRows(mapLayer));
            ++i;
        }
        int anzRows = mapLayerRows.size();
        int anzColumns = Array.getLength(MapLayerAttributes.getColumnNames());
        mapLayerInformationForTable = new Object[anzRows][anzColumns];
        int k = 0;
        while (k < anzRows) {
            MapLayerAttributes mLA = (MapLayerAttributes)mapLayerRows.get(k);
            Object[] attrib = mLA.toObjectArray();
            int m = 0;
            while (m < Array.getLength(attrib)) {
                mapLayerInformationForTable[k][m] = attrib[m];
                ++m;
            }
            ++k;
        }
        return mapLayerInformationForTable;
    }

    private String[][] getMapLayerInformationForTable(MapLayerAttributes[] mapLayerAttributesArray) {
        int numRows = Array.getLength(mapLayerAttributesArray);
        int numCols = Array.getLength(MapLayerAttributes.getColumnNames());
        String[][] mapLayerInformationForTable = new String[numRows][numCols];
        int k = 0;
        while (k < numRows) {
            MapLayerAttributes mLA = mapLayerAttributesArray[k];
            Object[] attrib = mLA.toObjectArray();
            int m = 0;
            while (m < Array.getLength(attrib)) {
                this.values[k][m] = attrib[m];
                ++m;
            }
            ++k;
        }
        return mapLayerInformationForTable;
    }

    private MapLayerAttributes[] toMapLayerAttributesArray(Object[][] m) {
        if (m == null) {
            return null;
        }
        int numRows = m.length;
        MapLayerAttributes[] mapLayerAttributesArray = new MapLayerAttributes[numRows];
        int i = 0;
        while (i < numRows) {
            String title = (String)m[i][0];
            String name = (String)m[i][1];
            String srs = (String)m[i][2];
            double minx = (Double)m[i][3];
            double miny = (Double)m[i][4];
            double maxx = (Double)m[i][5];
            double maxy = (Double)m[i][6];
            mapLayerAttributesArray[i] = new MapLayerAttributes(title, name, srs, minx, miny, maxx, maxy);
            ++i;
        }
        return mapLayerAttributesArray;
    }

    public class AL
    implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("showInfoTable")) {
                try {
                    ZoomToWMSPlugIn.this.showInformationTable(ZoomToWMSPlugIn.this.context);
                }
                catch (Exception e) {
                    System.out.println("Error in AL");
                }
            }
        }
    }

    private class InfoTableModel
    extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private InfoTableModel() {
        }

        @Override
        public int getColumnCount() {
            return ZoomToWMSPlugIn.this.columnNames.length;
        }

        @Override
        public int getRowCount() {
            return ZoomToWMSPlugIn.this.values.length;
        }

        @Override
        public String getColumnName(int col) {
            return ZoomToWMSPlugIn.this.columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return ZoomToWMSPlugIn.this.values[row][col];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return this.getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private class MASort
    extends MouseAdapter {
        private MASort() {
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.getButton() == 3) {
                int viewColumn = ZoomToWMSPlugIn.this.tcm.getColumnIndexAtX(me.getX());
                int column = ZoomToWMSPlugIn.this.infoTable.convertColumnIndexToModel(viewColumn);
                if (column == 0) {
                    ZoomToWMSPlugIn.this.sortTable(1);
                } else if (column == 1) {
                    ZoomToWMSPlugIn.this.sortTable(2);
                } else if (column == 2) {
                    ZoomToWMSPlugIn.this.sortTable(3);
                } else if (column == 3) {
                    ZoomToWMSPlugIn.this.sortTable(4);
                } else if (column == 4) {
                    ZoomToWMSPlugIn.this.sortTable(5);
                } else if (column == 5) {
                    ZoomToWMSPlugIn.this.sortTable(6);
                } else if (column == 6) {
                    ZoomToWMSPlugIn.this.sortTable(7);
                }
            }
        }
    }

    public static class MapLayerAttributes
    implements Comparable<MapLayerAttributes> {
        public static final int SORT_UP = 1;
        public static final int SORT_DOWN = -1;
        public static final int SORT_AFTER_TITLE = 1;
        public static final int SORT_AFTER_NAME = 2;
        public static final int SORT_AFTER_SRS = 3;
        public static final int SORT_AFTER_MINX = 4;
        public static final int SORT_AFTER_MINY = 5;
        public static final int SORT_AFTER_MAXX = 6;
        public static final int SORT_AFTER_MAXY = 7;
        public static int[] sortUpDown = new int[]{-1, -1, -1, -1, -1, -1, -1};
        public static int sortAfter;
        String srs;
        String name;
        String title;
        double minx;
        double miny;
        double maxx;
        double maxy;

        public MapLayerAttributes() {
            this.srs = " ";
            this.name = "Unknown";
            this.title = "Unknown";
            this.minx = 0.0;
            this.miny = 0.0;
            this.maxx = 0.0;
            this.maxy = 0.0;
        }

        public MapLayerAttributes(String title, String name, String srs, double minx, double miny, double maxx, double maxy) {
            this.title = title;
            this.name = name;
            this.srs = srs;
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;
        }

        @Override
        public int compareTo(MapLayerAttributes mla) {
            int ret = 1;
            if (sortAfter == 1) {
                ret = this.title.compareTo(mla.title) * sortUpDown[0];
            } else if (sortAfter == 2) {
                ret = this.name.compareTo(mla.name) * sortUpDown[1];
            } else if (sortAfter == 3) {
                ret = this.srs.compareTo(mla.srs) * sortUpDown[2];
            } else if (sortAfter == 4) {
                ret = this.minx > mla.minx ? 1 * sortUpDown[3] : -1 * sortUpDown[3];
            } else if (sortAfter == 5) {
                ret = this.miny > mla.miny ? 1 * sortUpDown[4] : -1 * sortUpDown[4];
            } else if (sortAfter == 6) {
                ret = this.maxx > mla.maxx ? 1 * sortUpDown[5] : -1 * sortUpDown[5];
            } else if (sortAfter == 7) {
                ret = this.maxy > mla.maxy ? 1 * sortUpDown[6] : -1 * sortUpDown[6];
            }
            return ret;
        }

        public static String[] getColumnNames() {
            String[] columNames = new String[]{"Title", "Name", "SRS", "MinX", "MinY", "MaxX", "MaxY"};
            return columNames;
        }

        public List<MapLayerAttributes> getMapLayerRows(MapLayer mapLayer) {
            String title;
            String unknown = "Unknown";
            ArrayList<MapLayerAttributes> mapLayerRows = new ArrayList<MapLayerAttributes>();
            String name = mapLayer.getName();
            if (name == null) {
                name = unknown;
            }
            if ((title = mapLayer.getTitle()) == null) {
                title = unknown;
            }
            List<BoundingBox> boundingBoxList = mapLayer.getAllBoundingBoxList();
            int i = 0;
            while (i < boundingBoxList.size()) {
                double maxY;
                double maxX;
                double minY;
                double minX;
                String srs;
                BoundingBox bb = boundingBoxList.get(i);
                if (bb == null) {
                    srs = unknown;
                    minX = 0.0;
                    minY = 0.0;
                    maxX = 400.0;
                    maxY = 400.0;
                } else {
                    srs = bb.getSRS().toLowerCase();
                    minX = bb.getMinX();
                    minY = bb.getMinY();
                    maxX = bb.getMaxX();
                    maxY = bb.getMaxY();
                }
                mapLayerRows.add(new MapLayerAttributes(title, name, srs, minX, minY, maxX, maxY));
                ++i;
            }
            return mapLayerRows;
        }

        String getName() {
            return this.name;
        }

        String getTitle() {
            return this.title;
        }

        String getSRS() {
            return this.srs;
        }

        double getMinx() {
            return this.minx;
        }

        double getMiny() {
            return this.miny;
        }

        double getMaxx() {
            return this.maxx;
        }

        double getMaxy() {
            return this.maxy;
        }

        Object[] toObjectArray() {
            int anzColumns = Array.getLength(MapLayerAttributes.getColumnNames());
            Object[] objectArray = new Object[anzColumns];
            objectArray[0] = this.getTitle();
            objectArray[1] = this.getName();
            objectArray[2] = this.getSRS();
            objectArray[3] = Double.valueOf(String.valueOf(this.getMinx()));
            objectArray[4] = Double.valueOf(String.valueOf(this.getMiny()));
            objectArray[5] = Double.valueOf(String.valueOf(this.getMaxx()));
            objectArray[6] = Double.valueOf(String.valueOf(this.getMaxy()));
            return objectArray;
        }

        public static void setSortAfter(int sortAfter) {
            if (sortAfter == 1) {
                MapLayerAttributes.sortUpDown[0] = -1 * sortUpDown[0];
            }
            if (sortAfter == 2) {
                MapLayerAttributes.sortUpDown[1] = -1 * sortUpDown[1];
            }
            if (sortAfter == 3) {
                MapLayerAttributes.sortUpDown[2] = -1 * sortUpDown[2];
            }
            if (sortAfter == 4) {
                MapLayerAttributes.sortUpDown[3] = -1 * sortUpDown[3];
            }
            if (sortAfter == 5) {
                MapLayerAttributes.sortUpDown[4] = -1 * sortUpDown[4];
            }
            if (sortAfter == 6) {
                MapLayerAttributes.sortUpDown[5] = -1 * sortUpDown[5];
            }
            if (sortAfter == 7) {
                MapLayerAttributes.sortUpDown[6] = -1 * sortUpDown[6];
            }
            MapLayerAttributes.sortAfter = sortAfter;
        }
    }
}

