/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListEvent;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityListListener;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.PString;
import com.pcauto.gui.table.ProxyEntityList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.EventListenerList;

public class OrderTranslatorEntityList
implements EntityList,
EntityListListener {
    private Vector<Integer> sortingColumns = new Vector();
    private boolean ascending = true;
    private int compares;
    private int[] entityMap = new int[0];
    private boolean orderLocked = true;
    private boolean sortingAllowed = true;
    private EntityList entityList = null;
    private EntityTableColumnModel columnModel = null;
    private EventListenerList listenerList = new EventListenerList();
    private int sortedColumn = -1;

    public OrderTranslatorEntityList() {
    }

    public OrderTranslatorEntityList(EntityList e, EntityTableColumnModel c) {
        this.setEntityList(e);
        this.setColumnModel(c);
    }

    public EntityList getEntityList() {
        return this.entityList;
    }

    public void setEntityList(EntityList e) {
        if (this.entityList != null) {
            this.entityList.removeEntityListListener(this);
        }
        this.entityList = e;
        this.reallocateIndexes();
        if (this.entityList != null) {
            this.entityList.addEntityListListener(this);
        }
        this.fireListChanged(null);
    }

    public EntityTableColumnModel getColumnModel() {
        return this.columnModel;
    }

    public void setColumnModel(EntityTableColumnModel c) {
        this.columnModel = c;
    }

    @Override
    public int getCount() {
        if (this.entityList != null) {
            return this.entityList.getCount();
        }
        return 0;
    }

    public int getEntityIndex(int index) {
        if (this.entityMap.length > index) {
            return this.entityMap[index];
        }
        return -1;
    }

    @Override
    public Object getEntity(int index) {
        if (this.entityMap.length > index && index >= 0) {
            return this.entityList.getEntity(this.entityMap[index]);
        }
        return null;
    }

    @Override
    public boolean setEntity(int index, Object entityInstance) throws EntityListException {
        boolean retVal = false;
        if (this.entityList != null && this.entityMap.length > index) {
            if (!this.orderLocked) {
                this.entityList.removeEntityListListener(this);
            }
            retVal = this.entityList.setEntity(this.entityMap[index], entityInstance);
            if (!this.orderLocked) {
                this.entityList.addEntityListListener(this);
                this.fireListChanged(new EntityListEvent(this, index, 2));
            }
        }
        return retVal;
    }

    @Override
    public Object getNewEntity() {
        if (this.entityList != null) {
            return this.entityList.getNewEntity();
        }
        return null;
    }

    @Override
    public Object getNewDefaultEntity() {
        if (this.entityList != null) {
            return this.entityList.getNewDefaultEntity();
        }
        return null;
    }

    @Override
    public Object getDefaultEntity() {
        if (this.entityList != null) {
            return this.entityList.getDefaultEntity();
        }
        return null;
    }

    @Override
    public void setDefaultEntity(Object o) {
        if (this.entityList != null) {
            this.entityList.setDefaultEntity(o);
        }
    }

    @Override
    public boolean addEntity(Object entityInstance) throws EntityListException {
        boolean retVal = false;
        if (this.entityList != null) {
            this.entityList.removeEntityListListener(this);
            retVal = this.entityList.addEntity(entityInstance);
            this.entityList.addEntityListListener(this);
            this.reallocateIndexes();
            if (this.sortingColumns.size() != 0) {
                this.sort(this);
            }
            this.fireListChanged(new EntityListEvent(this, this.entityList.getCount() - 1, 0));
        }
        return retVal;
    }

    @Override
    public boolean insertEntity(int index, Object entityInstance) throws EntityListException {
        boolean retVal = false;
        if (this.entityList != null) {
            if (index >= this.entityList.getCount()) {
                System.out.println("OrderTranslatorEntityList:insertEntity() - invalid index");
            }
            if (this.orderLocked) {
                retVal = this.entityList.insertEntity(index, entityInstance);
            } else {
                this.entityList.removeEntityListListener(this);
                retVal = this.entityList.addEntity(entityInstance);
                this.entityList.addEntityListListener(this);
                int[] tmpList = new int[this.getCount()];
                int i = 0;
                while (i < this.getCount()) {
                    if (i < index) {
                        tmpList[i] = this.entityMap[i];
                    } else if (i > index) {
                        tmpList[i] = this.entityMap[i - 1];
                    } else if (i == index) {
                        tmpList[i] = this.orderLocked ? index : this.getCount() - 1;
                    }
                    ++i;
                }
                this.entityMap = tmpList;
                this.fireListChanged(null);
            }
        }
        return retVal;
    }

    @Override
    public boolean moveEntity(int startIndex, int endIndex, int newIndex) throws EntityListException {
        if (this.entityList != null) {
            if (this.orderLocked) {
                return this.entityList.moveEntity(startIndex, endIndex, newIndex);
            }
            if (newIndex < 0 || newIndex + (endIndex - startIndex) >= this.entityMap.length || startIndex >= this.entityMap.length || startIndex < 0) {
                return false;
            }
            int[] tmpList = new int[endIndex - startIndex + 1];
            int i = 0;
            while (i < endIndex - startIndex + 1) {
                tmpList[i] = this.entityMap[startIndex + i];
                ++i;
            }
            if (newIndex > startIndex) {
                i = 0;
                while (i < newIndex - startIndex) {
                    this.entityMap[startIndex + i] = this.entityMap[endIndex + 1 + i];
                    ++i;
                }
                i = 0;
                while (i < endIndex - startIndex + 1) {
                    this.entityMap[newIndex + i] = tmpList[i];
                    ++i;
                }
            } else if (newIndex < startIndex) {
                i = 0;
                while (i < startIndex - newIndex) {
                    this.entityMap[newIndex + (endIndex - startIndex + 1) + i] = this.entityMap[newIndex + i];
                    ++i;
                }
                i = 0;
                while (i < endIndex - startIndex + 1) {
                    this.entityMap[newIndex + i] = tmpList[i];
                    ++i;
                }
            }
            this.fireListChanged(new EntityListEvent(this, startIndex, endIndex, 1));
            this.fireListChanged(new EntityListEvent(this, newIndex, newIndex + (endIndex - startIndex), 0));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEntity(int index) throws EntityListException {
        if (this.entityList != null) {
            return this.entityList.removeEntity(index);
        }
        return false;
    }

    @Override
    public void replaceAll(EntityList e) {
        if (this.entityList != null) {
            this.entityList.replaceAll(e);
        }
    }

    public boolean isOrderLockedToList() {
        return this.orderLocked;
    }

    public void setOrderLockedToList(boolean b) {
        this.orderLocked = b;
        if (this.orderLocked) {
            this.reallocateIndexes();
            this.fireListChanged(null);
        }
    }

    public boolean isRowSortingAllowed() {
        return this.sortingAllowed;
    }

    public void setRowSortingAllowed(boolean b) {
        this.sortingAllowed = b;
    }

    private int compareRowsByColumn(int row1, int row2, int column) {
        Object v2;
        String s2;
        Class<?> type = this.columnModel.getColumnClass(column);
        Object o1 = this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
        Object o2 = this.columnModel.getCellValue(column, this.entityList.getEntity(row2));
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        Class<?> type1 = o1.getClass();
        if (type != type1) {
            type = type1;
        }
        if (type.getSuperclass() == Number.class) {
            Number n2;
            double d2;
            Number n1 = (Number)this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
            double d1 = n1.doubleValue();
            if (d1 < (d2 = (n2 = (Number)this.columnModel.getCellValue(column, this.entityList.getEntity(row2))).doubleValue())) {
                return -1;
            }
            if (d1 > d2) {
                return 1;
            }
            return 0;
        }
        if (type == Date.class) {
            Date d2;
            long n2;
            Date d1 = (Date)this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
            long n1 = d1.getTime();
            if (n1 < (n2 = (d2 = (Date)this.columnModel.getCellValue(column, this.entityList.getEntity(row2))).getTime())) {
                return -1;
            }
            if (n1 > n2) {
                return 1;
            }
            return 0;
        }
        if (type == String.class) {
            int result;
            String s1 = (String)this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
            String s22 = (String)this.columnModel.getCellValue(column, this.entityList.getEntity(row2));
            if (this.columnModel.isRightAlignmentSortable(column)) {
                int maxLength = s1.length() > s22.length() ? s1.length() : s22.length();
                s1 = PString.alignRight(s1, maxLength, ' ');
                s22 = PString.alignRight(s22, maxLength, ' ');
            }
            if ((result = s1.compareTo(s22)) < 0) {
                return -1;
            }
            if (result > 0) {
                return 1;
            }
            return 0;
        }
        if (type == Boolean.class) {
            Boolean bool2;
            boolean b2;
            Boolean bool1 = (Boolean)this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
            boolean b1 = bool1;
            if (b1 == (b2 = (bool2 = (Boolean)this.columnModel.getCellValue(column, this.entityList.getEntity(row2))).booleanValue())) {
                return 0;
            }
            if (b1) {
                return 1;
            }
            return -1;
        }
        if (this.columnModel.getCellValue(column, this.entityList.getEntity(row1)) instanceof Comparable) {
            Comparable v22;
            Comparable v1 = (Comparable)this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
            int result = v1.compareTo(v22 = (Comparable)this.columnModel.getCellValue(column, this.entityList.getEntity(row2)));
            if (result < 0) {
                return -1;
            }
            if (result > 0) {
                return 1;
            }
            return 0;
        }
        Object v1 = this.columnModel.getCellValue(column, this.entityList.getEntity(row1));
        String s1 = v1.toString();
        int result = s1.compareTo(s2 = (v2 = this.columnModel.getCellValue(column, this.entityList.getEntity(row2))).toString());
        if (result < 0) {
            return -1;
        }
        if (result > 0) {
            return 1;
        }
        return 0;
    }

    private int compare(int row1, int row2) {
        ++this.compares;
        int level = 0;
        while (level < this.sortingColumns.size()) {
            Integer column = this.sortingColumns.elementAt(level);
            int result = this.compareRowsByColumn(row1, row2, column);
            if (result != 0) {
                return this.ascending ? result : -result;
            }
            ++level;
        }
        return 0;
    }

    private void reallocateIndexes() {
        if (this.entityList == null) {
            return;
        }
        int count = this.entityList.getCount();
        this.entityMap = new int[count];
        int i = 0;
        while (i < count) {
            this.entityMap[i] = i;
            ++i;
        }
    }

    private void checkModel() {
        if (this.entityMap.length != this.entityList.getCount()) {
            System.err.println("OrderTranslatorEntityList not informed of a change in model.");
        }
    }

    private void sort(Object sender) {
        if (!this.sortingAllowed) {
            return;
        }
        Iterator<Integer> i = this.sortingColumns.iterator();
        while (i.hasNext()) {
            if (this.columnModel.isSortable(i.next())) continue;
            return;
        }
        this.checkModel();
        this.compares = 0;
        this.shuttlesort((int[])this.entityMap.clone(), this.entityMap, 0, this.entityMap.length);
        this.fireListChanged(null);
    }

    private void n2sort() {
        int i = 0;
        while (i < this.getCount()) {
            int j = i + 1;
            while (j < this.getCount()) {
                if (this.compare(this.entityMap[i], this.entityMap[j]) == -1) {
                    this.swap(i, j);
                }
                ++j;
            }
            ++i;
        }
    }

    private void shuttlesort(int[] from, int[] to, int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        this.shuttlesort(to, from, low, middle);
        this.shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && this.compare(from[middle - 1], from[middle]) <= 0) {
            int i = low;
            while (i < high) {
                to[i] = from[i];
                ++i;
            }
            return;
        }
        int i = low;
        while (i < high) {
            to[i] = q >= high || p < middle && this.compare(from[p], from[q]) <= 0 ? from[p++] : from[q++];
            ++i;
        }
    }

    private void swap(int i, int j) {
        int tmp = this.entityMap[i];
        this.entityMap[i] = this.entityMap[j];
        this.entityMap[j] = tmp;
    }

    public void sortByColumns(Vector<Integer> columns) {
        this.sortingColumns.removeAllElements();
        this.sortingColumns.addAll(columns);
        this.sort(this);
        this.fireListChanged(null);
    }

    public void sortByColumn(int column) {
        Iterator<Integer> i = this.sortingColumns.iterator();
        while (i.hasNext()) {
            if (i.next() != column) continue;
            boolean bl = this.ascending = !this.ascending;
        }
        this.sortByColumn(column, this.ascending);
    }

    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        this.sortingColumns.removeAllElements();
        this.sortedColumn = column;
        this.sortingColumns.addElement(new Integer(column));
        this.sort(this);
        if (this.orderLocked) {
            this.setEntityListOrder();
        }
        this.fireListChanged(null);
        if (this.orderLocked) {
            this.ascending = ascending;
            this.sortingColumns.removeAllElements();
            this.sortingColumns.addElement(new Integer(column));
        }
    }

    public void getEntityListOrder() {
        this.reallocateIndexes();
        this.fireListChanged(null);
    }

    public void setEntityListOrder() {
        ProxyEntityList tmpList = new ProxyEntityList();
        tmpList.replaceAll(this);
        this.entityList.replaceAll(tmpList);
    }

    public int getViewIndex(int entityIndex) {
        int i = 0;
        while (i < this.entityMap.length) {
            if (this.entityMap[i] == entityIndex) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    public void listChanged(EntityListEvent evt) {
        this.reallocateIndexes();
        this.ascending = true;
        this.sortingColumns.removeAllElements();
        this.fireListChanged(null);
    }

    protected void fireListChanged(EntityListEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        int i = listeners.length - 2;
        while (i >= 0) {
            if (listeners[i] == EntityListListener.class) {
                if (e == null) {
                    e = new EntityListEvent(this);
                }
                ((EntityListListener)listeners[i + 1]).listChanged(e);
            }
            i -= 2;
        }
    }

    @Override
    public void addEntityListListener(EntityListListener e) {
        this.listenerList.add(EntityListListener.class, e);
    }

    @Override
    public void removeEntityListListener(EntityListListener e) {
        this.listenerList.remove(EntityListListener.class, e);
    }

    public boolean isAscending() {
        return this.ascending;
    }

    public int getSortedColumn() {
        return this.sortedColumn;
    }
}

