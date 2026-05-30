/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;
import org.saig.jump.lang.I18N;

public class CheckBoxJListSelectionPanel<T>
extends JPanel
implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private String LIST_TITLE = I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.elements");
    private JScrollPane scroller;
    private JList list;
    private JButton selectAllJButton;
    private JButton invertAllJButton;
    private DefaultListModel defModel;
    private Map<Object, Boolean> selectionMap;
    private boolean toMark;
    private static final Dimension DEFAULT_SCROLLER_SIZE = new Dimension(400, 200);
    private final Comparator<T> comparator;
    private boolean sortList;

    public CheckBoxJListSelectionPanel(Collection<T> objectCollection, String listTitle) {
        this(objectCollection, listTitle, DEFAULT_SCROLLER_SIZE, true, true);
    }

    public CheckBoxJListSelectionPanel(Collection<T> objectCollection, String listTitle, Dimension dimension, boolean showButtons, boolean allowMultipleSelections) {
        this(objectCollection, listTitle, dimension, showButtons, allowMultipleSelections, null, null);
    }

    public CheckBoxJListSelectionPanel(Collection<T> objectCollection, String listTitle, Dimension dimension, boolean showButtons, boolean allowMultipleSelections, Comparator<T> comparator, CheckBoxJListCellRenderer listCellRenderer) {
        this(objectCollection, listTitle, dimension, showButtons, allowMultipleSelections, comparator, listCellRenderer, true);
    }

    public CheckBoxJListSelectionPanel(Collection<T> objectCollection, String listTitle, Dimension dimension, boolean showButtons, boolean allowMultipleSelections, Comparator<T> comparator, CheckBoxJListCellRenderer listCellRenderer, boolean sortList) {
        super(new BorderLayout());
        this.comparator = comparator;
        this.sortList = sortList;
        ArrayList<T> objectList = new ArrayList<T>(objectCollection);
        if (listTitle != null) {
            this.LIST_TITLE = listTitle;
        }
        this.toMark = true;
        if (comparator == null) {
            comparator = new ObjectComparator();
        }
        if (sortList) {
            Collections.sort(objectList, comparator);
        }
        this.selectionMap = new HashMap<Object, Boolean>();
        this.defModel = new DefaultListModel();
        for (Object ob : objectList) {
            this.defModel.addElement(ob);
            this.selectionMap.put(ob, false);
        }
        this.setBorder(BorderFactory.createTitledBorder(this.LIST_TITLE));
        this.list = new JList();
        this.list.setModel(this.defModel);
        if (!allowMultipleSelections) {
            this.list.setSelectionMode(0);
        }
        if (listCellRenderer != null) {
            listCellRenderer.setSelectionMap(this.selectionMap);
            this.list.setCellRenderer(listCellRenderer);
        } else {
            CheckBoxJListCellRenderer cblcr = new CheckBoxJListCellRenderer();
            cblcr.setSelectionMap(this.selectionMap);
            this.list.setCellRenderer(cblcr);
        }
        this.list.addMouseListener(new CheckBoxJListMouseListener());
        this.list.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent ev) {
                CheckBoxJListSelectionPanel.this.refreshButtonsText();
            }
        });
        this.list.addPropertyChangeListener("selection_value", this);
        this.scroller = new JScrollPane(this.list, 22, 30);
        this.scroller.setMinimumSize(dimension);
        this.scroller.setPreferredSize(dimension);
        this.selectAllJButton = new JButton(I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.select-all"));
        this.selectAllJButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ev) {
                Object[] selectedValues = CheckBoxJListSelectionPanel.this.list.getSelectedValues();
                if (selectedValues.length > 1) {
                    int i = 0;
                    while (i < selectedValues.length) {
                        Object obj = selectedValues[i];
                        CheckBoxJListSelectionPanel.this.selectionMap.put(obj, new Boolean(CheckBoxJListSelectionPanel.this.toMark));
                        ++i;
                    }
                } else {
                    int i = 0;
                    while (i < CheckBoxJListSelectionPanel.this.list.getModel().getSize()) {
                        Object obj = CheckBoxJListSelectionPanel.this.list.getModel().getElementAt(i);
                        CheckBoxJListSelectionPanel.this.selectionMap.put(obj, CheckBoxJListSelectionPanel.this.toMark);
                        ++i;
                    }
                }
                CheckBoxJListSelectionPanel.this.toMark = !CheckBoxJListSelectionPanel.this.toMark;
                CheckBoxJListSelectionPanel.this.refreshButtonsText();
                CheckBoxJListSelectionPanel.this.list.repaint();
            }
        });
        this.invertAllJButton = new JButton(I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.invert-all"));
        this.invertAllJButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ev) {
                Object[] selectedValues = CheckBoxJListSelectionPanel.this.list.getSelectedValues();
                if (selectedValues.length > 1) {
                    int i = 0;
                    while (i < selectedValues.length) {
                        Object obj = selectedValues[i];
                        boolean selected = (Boolean)CheckBoxJListSelectionPanel.this.selectionMap.get(obj);
                        CheckBoxJListSelectionPanel.this.selectionMap.put(obj, new Boolean(!selected));
                        ++i;
                    }
                } else {
                    int i = 0;
                    while (i < CheckBoxJListSelectionPanel.this.list.getModel().getSize()) {
                        Object obj = CheckBoxJListSelectionPanel.this.list.getModel().getElementAt(i);
                        boolean selected = (Boolean)CheckBoxJListSelectionPanel.this.selectionMap.get(obj);
                        CheckBoxJListSelectionPanel.this.selectionMap.put(obj, !selected);
                        ++i;
                    }
                }
                CheckBoxJListSelectionPanel.this.refreshButtonsText();
                CheckBoxJListSelectionPanel.this.list.firePropertyChange("selection_value", false, true);
                CheckBoxJListSelectionPanel.this.list.repaint();
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(this.selectAllJButton);
        buttonPanel.add(this.invertAllJButton);
        this.add((Component)this.scroller, "Center");
        if (showButtons) {
            this.add((Component)buttonPanel, "South");
        }
    }

    public List<T> getSelectedObjects() {
        ArrayList selectedObjects = new ArrayList();
        int i = 0;
        while (i < this.list.getModel().getSize()) {
            Object obj = this.list.getModel().getElementAt(i);
            if (this.selectionMap.containsKey(obj) && this.selectionMap.get(obj).booleanValue()) {
                selectedObjects.add(obj);
            }
            ++i;
        }
        return selectedObjects;
    }

    public void clearList() {
        this.defModel.removeAllElements();
        this.selectionMap.clear();
        this.refreshButtonsText();
    }

    public void setListObjects(Collection<T> objectCollection) {
        ArrayList<T> objectList = new ArrayList<T>(objectCollection);
        if (this.sortList) {
            if (this.comparator != null) {
                Collections.sort(objectList, this.comparator);
            } else {
                Collections.sort(objectList, new ObjectComparator());
            }
        }
        this.selectionMap.clear();
        this.defModel.removeAllElements();
        for (Object ob : objectList) {
            this.defModel.addElement(ob);
            this.selectionMap.put(ob, false);
        }
        this.scroller.repaint();
    }

    private void refreshButtonsText() {
        List<T> selected = this.getSelectedObjects();
        if (selected == null) {
            return;
        }
        int numSelected = selected.size();
        int numElements = this.list.getModel().getSize();
        this.toMark = numSelected == 0;
        String selectButtonText = "";
        String invertButtonText = I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.invert");
        selectButtonText = this.toMark ? String.valueOf(selectButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.select") : String.valueOf(selectButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.unselect");
        if (numSelected != 0 && numSelected != numElements) {
            selectButtonText = String.valueOf(selectButtonText) + " " + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.selection");
            invertButtonText = String.valueOf(invertButtonText) + " " + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.selection");
        } else {
            selectButtonText = String.valueOf(selectButtonText) + " " + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.all");
            invertButtonText = String.valueOf(invertButtonText) + " " + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.all");
        }
        this.selectAllJButton.setText(selectButtonText);
        this.invertAllJButton.setText(invertButtonText);
    }

    public T getSelectedItem() {
        return (T)this.list.getSelectedValue();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        this.list.addListSelectionListener(listener);
    }

    public void addElementSelectionChangedListener(PropertyChangeListener listener) {
        this.addPropertyChangeListener("selection_value", listener);
        this.refreshButtonsText();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.list && evt.getPropertyName() == "selection_value") {
            this.firePropertyChange("selection_value", evt.getOldValue(), evt.getNewValue());
        }
        this.refreshButtonsText();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.list.setEnabled(enabled);
        this.scroller.setEnabled(enabled);
        this.invertAllJButton.setEnabled(enabled);
        this.selectAllJButton.setEnabled(enabled);
    }

    public void setSelectedObjects(Collection<T> objectToSelect) {
        if (CollectionUtils.isEmpty(objectToSelect)) {
            return;
        }
        int i = 0;
        while (i < this.defModel.size()) {
            Object obj = this.defModel.elementAt(i);
            this.selectionMap.put(obj, objectToSelect.contains(obj));
            ++i;
        }
        this.refreshButtonsText();
    }

    public static class CheckBoxJListCellRenderer
    extends JPanel
    implements ListCellRenderer {
        private static final long serialVersionUID = 1L;
        protected Font font = this.getFont().deriveFont(1);
        protected JCheckBox checkbox = new JCheckBox();
        protected Map<Object, Boolean> selectionMap = null;

        public void setSelectionMap(Map<Object, Boolean> selectionMap) {
            this.selectionMap = selectionMap;
        }

        public CheckBoxJListCellRenderer() {
            this.initialize();
        }

        public void initialize() {
            this.setOpaque(true);
            this.checkbox.setFont(this.font);
            this.checkbox.setOpaque(true);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(this.checkbox);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object obj = value;
            this.checkbox.setText(this.setCheckBoxText(obj));
            this.checkbox.setSelected(this.selectionMap.containsKey(obj) && this.selectionMap.get(obj) != false);
            if (isSelected) {
                this.checkbox.setForeground(list.getSelectionForeground());
                this.checkbox.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
                this.setBackground(list.getSelectionBackground());
            } else {
                this.checkbox.setForeground(list.getForeground());
                this.checkbox.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
                this.setBackground(list.getBackground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(this.font);
            return this.checkbox;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.checkbox.setEnabled(enabled);
        }

        protected String setCheckBoxText(Object obj) {
            return obj.toString();
        }
    }

    private class CheckBoxJListMouseListener
    implements MouseListener {
        private CheckBoxJListMouseListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (CheckBoxJListSelectionPanel.this.list.isEnabled() && e.getX() < 20) {
                this.doCheck(e.getPoint());
                CheckBoxJListSelectionPanel.this.list.repaint();
            }
        }

        private void doCheck(Point point) {
            Object obj = CheckBoxJListSelectionPanel.this.list.getModel().getElementAt(CheckBoxJListSelectionPanel.this.list.locationToIndex(point));
            boolean selected = (Boolean)CheckBoxJListSelectionPanel.this.selectionMap.get(obj);
            CheckBoxJListSelectionPanel.this.selectionMap.put(obj, !selected);
            CheckBoxJListSelectionPanel.this.list.firePropertyChange("selection_value", selected, !selected);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    private static class ObjectComparator<T>
    implements Comparator<T> {
        private ObjectComparator() {
        }

        @Override
        public int compare(T o1, T o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String one = o1.toString();
            String two = o2.toString();
            return Collator.getInstance(I18N.getLocale()).compare(one, two);
        }
    }
}

