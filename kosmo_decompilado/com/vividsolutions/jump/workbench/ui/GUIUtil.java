/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import es.kosmo.desktop.gui.components.JFileChooserWithOvewriteAndExtension;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.saig.jump.lang.I18N;

public class GUIUtil {
    public static final String dbf = "dbf";
    public static final String dbfDesc = "DBF";
    public static final String fme = "fme";
    public static final String fmeDesc = "FME GML";
    public static final String gml = "gml";
    public static final String gmlDesc = "GML";
    public static final String jml = "jml";
    public static final String jmlDesc = "JCS GML";
    public static final String shp = "shp";
    public static final String shpDesc = "ESRI Shapefile";
    public static final String shx = "shx";
    public static final String shxDesc = "SHX";
    public static final String wkt = "wkt";
    public static final String wktDesc = I18N.getString("workbench.ui.GUIUtil.wkt");
    public static final String wktaDesc = I18N.getString("workbench.ui.GUIUtil.wkt-show-attribute");
    public static final String xml = "xml";
    public static final String xmlDesc = "XML";
    public static final FileFilter ALL_FILES_FILTER = new FileFilter(){

        @Override
        public boolean accept(File f) {
            return true;
        }

        @Override
        public String getDescription() {
            return I18N.getString("workbench.ui.GUIUtil.all-files");
        }
    };

    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf(46);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static final String escapeHTML(String s, boolean escapeSpaces) {
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            switch (c) {
                case '<': {
                    sb.append("&lt;");
                    break;
                }
                case '>': {
                    sb.append("&gt;");
                    break;
                }
                case '&': {
                    sb.append("&amp;");
                    break;
                }
                case '\"': {
                    sb.append("&quot;");
                    break;
                }
                case '\u00e0': {
                    sb.append("&agrave;");
                    break;
                }
                case '\u00c0': {
                    sb.append("&Agrave;");
                    break;
                }
                case '\u00e2': {
                    sb.append("&acirc;");
                    break;
                }
                case '\u00c2': {
                    sb.append("&Acirc;");
                    break;
                }
                case '\u00e4': {
                    sb.append("&auml;");
                    break;
                }
                case '\u00c4': {
                    sb.append("&Auml;");
                    break;
                }
                case '\u00e5': {
                    sb.append("&aring;");
                    break;
                }
                case '\u00c5': {
                    sb.append("&Aring;");
                    break;
                }
                case '\u00e6': {
                    sb.append("&aelig;");
                    break;
                }
                case '\u00c6': {
                    sb.append("&AElig;");
                    break;
                }
                case '\u00e7': {
                    sb.append("&ccedil;");
                    break;
                }
                case '\u00c7': {
                    sb.append("&Ccedil;");
                    break;
                }
                case '\u00e9': {
                    sb.append("&eacute;");
                    break;
                }
                case '\u00c9': {
                    sb.append("&Eacute;");
                    break;
                }
                case '\u00e8': {
                    sb.append("&egrave;");
                    break;
                }
                case '\u00c8': {
                    sb.append("&Egrave;");
                    break;
                }
                case '\u00ea': {
                    sb.append("&ecirc;");
                    break;
                }
                case '\u00ca': {
                    sb.append("&Ecirc;");
                    break;
                }
                case '\u00eb': {
                    sb.append("&euml;");
                    break;
                }
                case '\u00cb': {
                    sb.append("&Euml;");
                    break;
                }
                case '\u00ef': {
                    sb.append("&iuml;");
                    break;
                }
                case '\u00cf': {
                    sb.append("&Iuml;");
                    break;
                }
                case '\u00f4': {
                    sb.append("&ocirc;");
                    break;
                }
                case '\u00d4': {
                    sb.append("&Ocirc;");
                    break;
                }
                case '\u00f6': {
                    sb.append("&ouml;");
                    break;
                }
                case '\u00d6': {
                    sb.append("&Ouml;");
                    break;
                }
                case '\u00f8': {
                    sb.append("&oslash;");
                    break;
                }
                case '\u00d8': {
                    sb.append("&Oslash;");
                    break;
                }
                case '\u00df': {
                    sb.append("&szlig;");
                    break;
                }
                case '\u00f9': {
                    sb.append("&ugrave;");
                    break;
                }
                case '\u00d9': {
                    sb.append("&Ugrave;");
                    break;
                }
                case '\u00fb': {
                    sb.append("&ucirc;");
                    break;
                }
                case '\u00db': {
                    sb.append("&Ucirc;");
                    break;
                }
                case '\u00fc': {
                    sb.append("&uuml;");
                    break;
                }
                case '\u00dc': {
                    sb.append("&Uuml;");
                    break;
                }
                case '\u00ae': {
                    sb.append("&reg;");
                    break;
                }
                case '\u00a9': {
                    sb.append("&copy;");
                    break;
                }
                case '\u20ac': {
                    sb.append("&euro;");
                    break;
                }
                case ' ': {
                    sb.append(escapeSpaces ? "&nbsp;" : " ");
                    break;
                }
                case '\n': {
                    sb.append("<BR>");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            ++i;
        }
        return sb.toString();
    }

    public static Color alphaColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void centre(Component componentToMove, Component componentToCentreOn) {
        Dimension componentToCentreOnSize = componentToCentreOn.getSize();
        int x = componentToCentreOn.getX() + (componentToCentreOnSize.width - componentToMove.getWidth()) / 2;
        int y = componentToCentreOn.getY() + (componentToCentreOnSize.height - componentToMove.getHeight()) / 2;
        componentToMove.setLocation(x < 0 ? 0 : x, y < 0 ? 0 : y);
    }

    public static void centreOnScreen(Component componentToMove) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        componentToMove.setLocation((screenSize.width - componentToMove.getWidth()) / 2, (screenSize.height - componentToMove.getHeight()) / 2);
    }

    public static void centreOnWindow(Component componentToMove) {
        GUIUtil.centre(componentToMove, SwingUtilities.windowForComponent(componentToMove));
    }

    public static void chooseGoodColumnWidths(JTable table) {
        int PADDING = 10;
        if (table.getModel().getRowCount() == 0) {
            return;
        }
        int i = 0;
        while (i < table.getModel().getColumnCount()) {
            TableColumn column = table.getColumnModel().getColumn(i);
            double headerWidth = table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, table.getModel().getColumnName(i), false, false, 0, i).getPreferredSize().getWidth() + 10.0;
            double valueWidth = table.getCellRenderer(0, i).getTableCellRendererComponent(table, table.getModel().getValueAt(0, i), false, false, 0, i).getPreferredSize().getWidth() + 10.0;
            if (i != 0) {
                if (table.getModel().getColumnClass(i).isAssignableFrom(Date.class)) {
                    headerWidth = Math.max(headerWidth + 30.0, 30.0);
                    valueWidth = Math.max(valueWidth + 30.0, 30.0);
                }
                int width = Math.min(200, Math.max((int)headerWidth, (int)valueWidth));
                column.setPreferredWidth(width);
                column.setWidth(width);
            }
            ++i;
        }
    }

    public static JFileChooser createJFileChooserWithExistenceChecking() {
        return new JFileChooser(){
            private static final long serialVersionUID = 1L;

            @Override
            public void approveSelection() {
                File[] files = GUIUtil.selectedFiles(this);
                if (files.length == 0) {
                    return;
                }
                int i = 0;
                while (i < files.length) {
                    if (!files[i].exists() && !files[i].isFile()) {
                        return;
                    }
                    ++i;
                }
                super.approveSelection();
            }
        };
    }

    public static JFileChooser createJFileChooserWithOverwritePrompting() {
        return new JFileChooser(){
            private static final long serialVersionUID = 1L;

            @Override
            public void approveSelection() {
                int response;
                if (GUIUtil.selectedFiles(this).length != 1) {
                    return;
                }
                File selectedFile = GUIUtil.selectedFiles(this)[0];
                if (selectedFile.exists() && !selectedFile.isFile()) {
                    return;
                }
                if (selectedFile.exists() && (response = JOptionPane.showConfirmDialog(this, I18N.getMessage("workbench.ui.GUIUtil.the-file-{0}-already-exists-do-you-want-to-replace-the-existing-file", new Object[]{selectedFile.getName()}), I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Overwrite-existing-files"), 0)) != 0) {
                    return;
                }
                super.approveSelection();
            }
        };
    }

    public static JFileChooser createJFileChooserWithOverwritePromptingAndDefaultExtension(String extension) {
        return new JFileChooserWithOvewriteAndExtension(extension);
    }

    public static void doNotRoundDoubles(JTable table) {
        table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            {
                this.setHorizontalAlignment(4);
            }

            @Override
            public void setValue(Object value) {
                this.setText(value == null ? "" : "" + value);
            }
        });
    }

    public static void fixEditableComboBox(JComboBox cb) {
        Assert.isTrue((boolean)cb.isEditable());
        if (!UIManager.getLookAndFeel().getName().equals("Windows")) {
            return;
        }
        cb.setEditor(new BasicComboBoxEditor(){

            @Override
            public void setItem(Object item) {
                super.setItem(item);
                this.editor.selectAll();
            }
        });
    }

    public static void handleThrowable(final Throwable t, final Component parent) {
        try {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    t.printStackTrace(System.out);
                    JOptionPane.showMessageDialog(parent, StringUtil.split(t.toString(), 80), I18N.getString("workbench.ui.GUIUtil.exception"), 0);
                }
            });
        }
        catch (Throwable t2) {
            t2.printStackTrace(System.out);
        }
    }

    public static void invokeOnEventThread(Runnable r) throws InterruptedException, InvocationTargetException {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }

    public static String nameWithoutExtension(File file) {
        String name = file.getName();
        int dotPosition = name.lastIndexOf(46);
        return dotPosition < 0 ? name : name.substring(0, dotPosition);
    }

    public static String nameWithoutPathAndExtension(File file) {
        String name = file.getName();
        int pathPosition = name.lastIndexOf(File.pathSeparator);
        int dotPosition = name.lastIndexOf(46);
        return dotPosition < 0 ? name.substring(pathPosition + 1, name.length()) : name.substring(pathPosition + 1, dotPosition);
    }

    public static void removeChoosableFileFilters(JFileChooser fc) {
        FileFilter[] filters = fc.getChoosableFileFilters();
        int i = 0;
        while (i < filters.length) {
            fc.removeChoosableFileFilter(filters[i]);
            ++i;
        }
    }

    public static JFormattedTextField getUSFormatedNumberTextField(int lenght) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setGroupingUsed(false);
        JFormattedTextField numberTextFiel = new JFormattedTextField(format);
        numberTextFiel.setPreferredSize(new Dimension(lenght, numberTextFiel.getPreferredSize().height));
        return numberTextFiel;
    }

    public static FileFilter createFileFilter(final String description, final String[] extensions) {
        return new FileFilter(){

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                int i = 0;
                while (i < extensions.length) {
                    if (GUIUtil.getExtension(f).equalsIgnoreCase(extensions[i])) {
                        return true;
                    }
                    ++i;
                }
                return false;
            }

            @Override
            public String getDescription() {
                ArrayList<String> extensionStrings = new ArrayList<String>();
                int i = 0;
                while (i < extensions.length) {
                    String ext = extensions[i];
                    String extStr = "";
                    if (!"".equals(ext)) {
                        extStr = "." + ext;
                    }
                    extensionStrings.add("*" + extStr);
                    ++i;
                }
                return String.valueOf(description) + " (" + StringUtil.replaceAll(StringUtil.toCommaDelimitedString(extensionStrings), ",", ";") + ")";
            }
        };
    }

    public static Color toSimulatedTransparency(Color color) {
        return new Color(color.getRed() + (int)((double)((255 - color.getRed()) * (255 - color.getAlpha())) / 255.0), color.getGreen() + (int)((double)((255 - color.getGreen()) * (255 - color.getAlpha())) / 255.0), color.getBlue() + (int)((double)((255 - color.getBlue()) * (255 - color.getAlpha())) / 255.0));
    }

    public static String truncateString(String s, int maxLength) {
        if (s.length() < maxLength) {
            return s;
        }
        return String.valueOf(s.substring(0, maxLength - 3)) + "...";
    }

    public static Point2D subtract(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }

    public static Point2D add(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static Point2D multiply(Point2D v, double x) {
        return new Point2D.Double(v.getX() * x, v.getY() * x);
    }

    public static Transferable getContents(Clipboard clipboard) {
        try {
            return clipboard.getContents(null);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static double trueAscent(TextLayout layout) {
        return -layout.getBounds().getY();
    }

    public static ImageIcon resize(ImageIcon icon, int extent) {
        if (icon == null) {
            return null;
        }
        return new ImageIcon(icon.getImage().getScaledInstance(extent, extent, 4));
    }

    public static ImageIcon toSmallIcon(Icon icon) {
        return GUIUtil.resize((ImageIcon)icon, 16);
    }

    public static int swingThreadPriority() {
        final Int i = new Int();
        try {
            GUIUtil.invokeOnEventThread(new Runnable(){

                @Override
                public void run() {
                    i.i = Thread.currentThread().getPriority();
                }
            });
        }
        catch (InvocationTargetException e) {
            Assert.shouldNeverReachHere();
        }
        catch (InterruptedException e) {
            Assert.shouldNeverReachHere();
        }
        return i.i;
    }

    public static void fixClicks(final Component c) {
        c.addMouseListener(new MouseListener(){
            private List<MouseEvent> events = new ArrayList<MouseEvent>();

            @Override
            public void mousePressed(MouseEvent e) {
                this.add(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                this.add(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                this.add(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                this.add(e);
            }

            private MouseEvent event(int i) {
                return this.events.get(i);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                this.add(e);
                if (this.events.size() == 4 && this.event(0).getID() == 501 && this.event(1).getID() == 505 && this.event(2).getID() == 504) {
                    c.dispatchEvent(new MouseEvent(c, 500, System.currentTimeMillis(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger()));
                }
            }

            private void add(MouseEvent e) {
                if (this.events.size() == 4) {
                    this.events.remove(0);
                }
                this.events.add(e);
            }
        });
    }

    public static void addInternalFrameListener(JDesktopPane pane, final InternalFrameListener listener) {
        JInternalFrame[] frames = pane.getAllFrames();
        int i = 0;
        while (i < frames.length) {
            frames[i].addInternalFrameListener(listener);
            ++i;
        }
        pane.addContainerListener(new ContainerAdapter(){

            @Override
            public void componentAdded(ContainerEvent e) {
                if (e.getChild() instanceof JInternalFrame) {
                    ((JInternalFrame)e.getChild()).addInternalFrameListener(listener);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (e.getChild() instanceof JInternalFrame) {
                    ((JInternalFrame)e.getChild()).removeInternalFrameListener(listener);
                }
            }
        });
    }

    public static InternalFrameListener toInternalFrameListener(final ActionListener listener) {
        return new InternalFrameListener(){

            private void fireActionPerformed(InternalFrameEvent e) {
                listener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.toString()));
            }

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                this.fireActionPerformed(e);
            }
        };
    }

    public static Timer createRestartableSingleEventTimer(int delay, ActionListener listener) {
        Timer timer = new Timer(delay, listener);
        timer.setCoalesce(true);
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        return timer;
    }

    public static List<Object> items(JComboBox comboBox) {
        ArrayList<Object> items = new ArrayList<Object>();
        int i = 0;
        while (i < comboBox.getItemCount()) {
            items.add(comboBox.getItemAt(i));
            ++i;
        }
        return items;
    }

    public static void setSelectedWithClick(JCheckBox checkBox, boolean selected) {
        checkBox.setSelected(!selected);
        checkBox.doClick();
    }

    public static void setLocation(Component componentToMove, Location location, Component other) {
        Point p = new Point((int)other.getLocationOnScreen().getX() + (location.fromRight ? other.getWidth() - componentToMove.getWidth() - location.x : location.x), (int)other.getLocationOnScreen().getY() + (location.fromBottom ? other.getHeight() - componentToMove.getHeight() - location.y : location.y));
        SwingUtilities.convertPointFromScreen(p, componentToMove.getParent());
        componentToMove.setLocation(p);
    }

    public static void highlightForDebugging(JComponent component, Color color) {
        component.setBackground(color);
        component.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, color));
    }

    public static Component topCard(Container c) {
        Assert.isTrue((boolean)(c.getLayout() instanceof CardLayout));
        Component[] components = c.getComponents();
        int i = 0;
        while (i < components.length) {
            if (components[i].isVisible()) {
                return components[i];
            }
            ++i;
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public static File[] selectedFiles(JFileChooser chooser) {
        File[] fileArray;
        if (chooser.getSelectedFiles().length == 0 && chooser.getSelectedFile() != null) {
            File[] fileArray2 = new File[1];
            fileArray = fileArray2;
            fileArray2[0] = chooser.getSelectedFile();
        } else {
            fileArray = chooser.getSelectedFiles();
        }
        return fileArray;
    }

    public static ImageIcon toDisabledIcon(ImageIcon icon) {
        return new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
    }

    public static Component getDescendantOfClass(Class<?> c, Container container) {
        int i = 0;
        while (i < container.getComponentCount()) {
            Component descendant;
            if (c.isInstance(container.getComponent(i))) {
                return container.getComponent(i);
            }
            if (container.getComponent(i) instanceof Container && (descendant = GUIUtil.getDescendantOfClass(c, (Container)container.getComponent(i))) != null) {
                return descendant;
            }
            ++i;
        }
        return null;
    }

    public static ValidatingTextField createSyncdTextField(JSlider s) {
        int columns = (int)Math.ceil(Math.log(s.getMaximum()) / Math.log(10.0));
        return GUIUtil.createSyncdTextField(s, columns);
    }

    public static ValidatingTextField createSyncdTextField(JSlider s, int columns) {
        ValidatingTextField t = new ValidatingTextField(String.valueOf(s.getValue()), columns, 4, ValidatingTextField.INTEGER_VALIDATOR, new ValidatingTextField.CompositeCleaner(new ValidatingTextField.Cleaner[]{new ValidatingTextField.BlankCleaner("" + s.getMinimum()), new ValidatingTextField.MinIntCleaner(s.getMinimum()), new ValidatingTextField.MaxIntCleaner(s.getMaximum())}));
        GUIUtil.sync(s, t);
        GUIUtil.syncEnabledStates(s, t);
        return t;
    }

    public static void sync(final JSlider s, final ValidatingTextField t) {
        t.setText("" + s.getValue());
        final Boolean[] changing = new Boolean[]{Boolean.FALSE};
        s.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    t.setText("" + s.getValue());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }
        });
        t.getDocument().addDocumentListener(new DocumentListener(){

            private void changed() {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    s.setValue(t.getInteger());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.changed();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.changed();
            }
        });
    }

    public static void syncEnabledStates(final JComponent c1, final JComponent c2) {
        c2.setEnabled(c1.isEnabled());
        c1.addPropertyChangeListener("enabled", new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (c1.isEnabled() == c2.isEnabled()) {
                    return;
                }
                c2.setEnabled(c1.isEnabled());
            }
        });
        c2.addPropertyChangeListener("enabled", new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (c1.isEnabled() == c2.isEnabled()) {
                    return;
                }
                c1.setEnabled(c2.isEnabled());
            }
        });
    }

    public static void sync(final JSlider s1, final JSlider s2) {
        s2.setValue(s1.getValue());
        Assert.isTrue((s1.getMinimum() == s2.getMinimum() ? 1 : 0) != 0);
        Assert.isTrue((s1.getMaximum() == s2.getMaximum() ? 1 : 0) != 0);
        final Boolean[] changing = new Boolean[]{Boolean.FALSE};
        s1.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    s2.setValue(s1.getValue());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }
        });
        s2.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    s1.setValue(s2.getValue());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }
        });
    }

    public static void sync(final JCheckBox c1, final JCheckBox c2) {
        c2.setSelected(c1.isSelected());
        final Boolean[] changing = new Boolean[]{Boolean.FALSE};
        c1.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    c2.setSelected(c1.isSelected());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }
        });
        c2.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (changing[0] == Boolean.TRUE) {
                    return;
                }
                changing[0] = Boolean.TRUE;
                try {
                    c1.setSelected(c2.isSelected());
                }
                finally {
                    changing[0] = Boolean.FALSE;
                }
            }
        });
    }

    public static void dispose(JInternalFrame internalFrame, JDesktopPane desktopPane) {
        desktopPane.getDesktopManager().closeFrame(internalFrame);
        internalFrame.dispose();
    }

    private static class Int {
        public volatile int i;

        private Int() {
        }
    }

    public static class Location {
        private int x;
        private int y;
        private boolean fromRight;
        private boolean fromBottom;

        public Location(int x, boolean fromRight, int y, boolean fromBottom) {
            this.x = x;
            this.y = y;
            this.fromRight = fromRight;
            this.fromBottom = fromBottom;
        }
    }
}

