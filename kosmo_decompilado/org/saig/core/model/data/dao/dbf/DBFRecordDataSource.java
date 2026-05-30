/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.dbf;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileNIO;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSourceException;
import org.saig.core.model.data.dao.dbf.iterator.DBFIterator;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class DBFRecordDataSource
extends TableRecordDataSource {
    private static final Logger LOGGER = Logger.getLogger(DBFRecordDataSource.class);
    public static final String ID = "DBF";
    private File file;
    private DbaseFileNIO dbaseFileChannel;
    private String name;
    private int numReg;
    private boolean open;
    private List<Integer> pKToNewFeaturesFIDList;
    private Charset dbfCharset;

    public DBFRecordDataSource() {
    }

    public DBFRecordDataSource(String fileName) throws Exception {
        this(fileName, null, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
    }

    public DBFRecordDataSource(String fileName, FeatureSchema schema) throws Exception {
        this(fileName, schema, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
    }

    public DBFRecordDataSource(String fileName, FeatureSchema schema, Charset charset) throws Exception {
        this.name = fileName;
        this.file = new File(fileName);
        this.dbfCharset = charset;
        this.schema = schema;
        if (!this.file.exists()) {
            this.createDataStore(null, null, -1);
        }
        this.open();
        this.schema = this.getTableSchema();
        this.numReg = this.dbaseFileChannel.getRecordCount();
        this.close();
        this.pKToNewFeaturesFIDList = new ArrayList<Integer>();
        this.properties.put("FILE", fileName);
        this.properties.put("CHARSET", charset.name());
        this.properties.put("DATASOURCE", this.getClass().getName());
    }

    @Override
    public void add(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.addAll(records);
    }

    @Override
    public void addAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record element : records) {
                if (element.isUnsaved()) {
                    if (!this.newRecords.contains(element)) {
                        this.pKToNewFeaturesFIDList.add(new Integer(element.getID()));
                    }
                    this.newRecords.add(element);
                } else {
                    this.updateRecords.add(element);
                }
                if (!this.deleteRecords.contains(element)) continue;
                this.deleteRecords.remove(element);
            }
            return;
        }
    }

    @Override
    public void update(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.updateAll(records);
    }

    @Override
    public void updateAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record object : records) {
                if (!object.isUnsaved()) {
                    this.updateRecords.add(object);
                    if (!this.deleteRecords.contains(object)) continue;
                    this.deleteRecords.remove(object);
                    continue;
                }
                if (!this.newRecords.contains(object)) {
                    this.pKToNewFeaturesFIDList.add(new Integer(object.getID()));
                } else {
                    this.newRecords.remove(object);
                }
                this.newRecords.add(object);
            }
            return;
        }
    }

    @Override
    public void remove(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.removeAll(records);
    }

    @Override
    public void removeAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record element : records) {
                if (this.newRecords.contains(element)) {
                    this.pKToNewFeaturesFIDList.remove(new Integer(element.getID()));
                    this.newRecords.remove(element);
                }
                if (this.updateRecords.contains(element)) {
                    this.updateRecords.remove(element);
                }
                this.deleteRecords.add(element);
            }
            return;
        }
    }

    @Override
    public synchronized List<Record> getRecords() {
        return this.getRecords(null, null);
    }

    public Record readRecord(int index) throws IOException {
        Record record = new Record(this.schema);
        Object[] values = this.dbaseFileChannel.getRecord(index);
        int j = 0;
        while (j < values.length) {
            record.setAttribute(j, values[j]);
            ++j;
        }
        record.setAttribute("GID", (Object)new Long(index));
        return record;
    }

    @Override
    public synchronized List<Record> getRecords(String fieldOrdered) {
        return this.getRecords(fieldOrdered, null, true);
    }

    @Override
    public synchronized List<Record> getRecords(String fieldOrdered, Filter filter) {
        return this.getRecords(fieldOrdered, filter, true);
    }

    @Override
    public synchronized Record getRecord(int index) throws Exception {
        Record record;
        block15: {
            block14: {
                record = new Record(this.schema);
                if (this.open) {
                    LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
                    return record;
                }
                if (index >= this.numReg) break block14;
                try {
                    try {
                        this.open();
                        Object[] values = this.dbaseFileChannel.getRecord(index);
                        int j = 0;
                        while (j < values.length) {
                            record.setAttribute(j, values[j]);
                            ++j;
                        }
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        this.close();
                        return null;
                    }
                }
                finally {
                    this.close();
                }
                record.setAttribute("GID", (Object)new Long(index));
                if (this.deleteRecords.contains(record)) {
                    return null;
                }
                if (!this.updateRecords.contains(record)) break block15;
                for (Record element : this.updateRecords) {
                    if (!element.equals(record)) continue;
                    return element;
                }
                break block15;
            }
            int row = index - this.numReg;
            if (row > this.pKToNewFeaturesFIDList.size()) {
                LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource.requested-row-exceeds-the-collections-size-row-{0}-size-{1}")) + row + " TAMA\u00ef\u00bf\u00bdO:" + this.pKToNewFeaturesFIDList.size()));
                return null;
            }
            Integer fid = this.pKToNewFeaturesFIDList.get(row);
            if (fid == null) {
                record = null;
            } else {
                for (Record element : this.newRecords) {
                    if (element.getID() != fid.intValue()) continue;
                    return element;
                }
            }
        }
        return record;
    }

    @Override
    public long size() {
        return this.numReg + this.newRecords.size() - this.deleteRecords.size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Record getByPrimaryKey(Object key) {
        if (key == null) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.primary-key-null"));
            return null;
        }
        if (key instanceof Record) {
            return (Record)key;
        }
        int row = ((Number)key).intValue();
        try {
            return this.getRecord(row);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    @Override
    public synchronized List<Record> getByPrimaryKey(Object[] keys) {
        ArrayList<Record> result;
        block19: {
            result = new ArrayList<Record>();
            if (this.open) {
                LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
                return result;
            }
            if (keys == null || keys.length == 0) {
                return result;
            }
            ArrayList<Object> realKeys = new ArrayList<Object>();
            int i = 0;
            while (i < keys.length) {
                if (keys[i] != null) {
                    if (keys[i] instanceof Record) {
                        result.add((Record)keys[i]);
                    } else {
                        realKeys.add(keys[i]);
                    }
                }
                ++i;
            }
            Object[] key = realKeys.toArray();
            Arrays.sort(key);
            try {
                try {
                    this.open();
                    int i2 = 0;
                    while (i2 < key.length) {
                        int row = ((Number)key[i2]).intValue();
                        Object[] values = this.dbaseFileChannel.getRecord(row);
                        Record record = new Record(this.schema);
                        int j = 0;
                        while (j < values.length) {
                            record.setAttribute(j, values[j]);
                            ++j;
                        }
                        record.setAttribute("GID", (Object)new Long(row));
                        result.add(record);
                        ++i2;
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    try {
                        this.close();
                    }
                    catch (Exception e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                    break block19;
                }
            }
            catch (Throwable throwable) {
                try {
                    this.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                throw throwable;
            }
            try {
                this.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        result.removeAll(this.deleteRecords);
        result.removeAll(this.updateRecords);
        result.addAll(this.updateRecords);
        result.addAll(this.newRecords);
        return result;
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values) {
        return this.getByAttribute(fields, values, null, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField) {
        return this.getByAttribute(fields, values, orderField, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField, Filter filter) {
        return this.getByAttribute(fields, values, orderField, null, true);
    }

    public synchronized void open() throws IOException {
        this.open = true;
        this.dbaseFileChannel = new DbaseFileNIO(this.dbfCharset);
        this.dbaseFileChannel.setFile(this.file);
        this.dbaseFileChannel.open();
    }

    public synchronized void close() throws Exception {
        this.dbaseFileChannel.close();
        this.open = false;
    }

    private FeatureSchema getTableSchema() {
        FeatureSchema fs = new FeatureSchema();
        int numfields = this.dbaseFileChannel.getFieldCount();
        int j = 0;
        while (j < numfields) {
            String currentAttrName = this.dbaseFileChannel.getFieldName(j);
            if (fs.hasAttribute(currentAttrName)) {
                String name;
                String baseName = StringUtils.substring((String)currentAttrName, (int)0, (int)(currentAttrName.length() - 2));
                int cont = 1;
                while (fs.hasAttribute(name = String.valueOf(baseName) + "_" + cont++)) {
                }
                LOGGER.warn((Object)I18N.getMessage(this.getClass(), "attribute-{0}-already-exists-and-will-be-substituted-by-attribute-{1}", new Object[]{currentAttrName, name}));
                currentAttrName = name;
            }
            fs.addAttribute(currentAttrName, AttributeType.toAttributeType(this.dbaseFileChannel.getRealFieldType(j)));
            ++j;
        }
        if (!fs.hasAttribute("GID")) {
            fs.addAttribute("GID", AttributeType.LONG, Boolean.TRUE);
        } else {
            fs.getAttribute("GID").setPrimaryKey(true);
        }
        return fs;
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        ArrayList<Object> resultado = new ArrayList<Object>();
        int i = 0;
        while ((long)i < this.size()) {
            resultado.add(new Long(i));
            ++i;
        }
        resultado.removeAll(this.getKeys(this.deleteRecords));
        return resultado;
    }

    @Override
    public synchronized List<Object> getSortKeys(String column, boolean ascending) {
        ArrayList<SortedAttribute> sort;
        int indexColumn;
        boolean isString;
        block15: {
            isString = this.schema.getAttribute(column).getType().toJavaClass().equals(String.class);
            indexColumn = this.schema.getAttributeIndex(column);
            sort = new ArrayList<SortedAttribute>();
            if (this.open) {
                LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
                return new ArrayList<Object>();
            }
            try {
                try {
                    this.open();
                    List<Object> keys = this.getOrderedPrimaryKeyList();
                    int i = 0;
                    while (i < keys.size()) {
                        Number n = (Number)keys.get(i);
                        Object value = this.readField(n.intValue(), indexColumn);
                        sort.add(new SortedAttribute(value, n, ascending, isString));
                        ++i;
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    try {
                        this.close();
                    }
                    catch (Exception e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                    break block15;
                }
            }
            catch (Throwable throwable) {
                try {
                    this.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                throw throwable;
            }
            try {
                this.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        for (Record element : this.newRecords) {
            Object value = element.getAttribute(indexColumn);
            sort.add(new SortedAttribute(value, element, ascending, isString));
        }
        Collections.sort(sort);
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        return result;
    }

    private Object readField(int recordNumber, int indexColumn) throws Exception {
        if (indexColumn + 1 > this.dbaseFileChannel.getFieldCount()) {
            return new Long(recordNumber);
        }
        return this.dbaseFileChannel.getRecord(recordNumber)[indexColumn];
    }

    private Object[] readField(int recordNumber) throws Exception {
        return this.dbaseFileChannel.getRecord(recordNumber);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        if (this.open) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
            return values;
        }
        int index = this.schema.getAttributeIndex(field);
        try {
            try {
                this.open();
                int i = 0;
                while ((long)i < this.size()) {
                    Object value = this.readField(i, index);
                    if (value != null) {
                        values.add(value);
                    }
                    ++i;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                try {
                    this.close();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)"", (Throwable)e2);
                }
            }
        }
        finally {
            try {
                this.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return values;
    }

    @Override
    public List<Object> getFieldValue(String field, String fieldKey, Object value) {
        ArrayList<Object> values = new ArrayList<Object>();
        if (!this.schema.hasAttribute(field) || !this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        if (this.open) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
            return values;
        }
        int index = this.schema.getAttributeIndex(field);
        int pkIndex = this.schema.getAttributeIndex(fieldKey);
        try {
            try {
                this.open();
                int i = 0;
                while ((long)i < this.size()) {
                    Object pkValue = this.readField(i, pkIndex);
                    if (pkValue.equals(value)) {
                        values.add(this.readField(i, index));
                    }
                    ++i;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                try {
                    this.close();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)"", (Throwable)e2);
                }
            }
        }
        finally {
            try {
                this.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return values;
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        int i = 0;
        while (i < fields.length) {
            if (!this.schema.hasAttribute(fields[i])) {
                return values;
            }
            ++i;
        }
        if (!this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            Attribute attr = this.schema.getAttribute(fields[i2]);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        int[] indexes = new int[attrNoCalculate.size()];
        int pkIndex = this.schema.getAttributeIndex(fieldKey);
        AttributeType attrType = this.schema.getAttributeType(pkIndex);
        int i3 = 0;
        while (i3 < attrNoCalculate.size()) {
            Attribute attr = (Attribute)attrNoCalculate.get(i3);
            indexes[i3] = this.schema.getAttributeIndex(attr.getName());
            ++i3;
        }
        if (indexes.length > 0) {
            if (this.open) {
                LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
                return values;
            }
            try {
                try {
                    this.open();
                    i3 = 0;
                    while ((long)i3 < this.size()) {
                        Object[] values_ = this.readField(i3);
                        RelationAttribute ra = new RelationAttribute();
                        int j = 0;
                        while (j < indexes.length) {
                            int currentIndex = indexes[j];
                            Object value = null;
                            value = currentIndex == values_.length ? FeatureUtil.getGoodAttribute(attrType, new Long(i3)) : values_[currentIndex];
                            ra.setFieldValue(((Attribute)attrNoCalculate.get(j)).getName(), value);
                            ++j;
                        }
                        if (pkIndex == values_.length) {
                            values.put(FeatureUtil.getGoodAttribute(attrType, new Long(i3)), ra);
                        } else {
                            values.put(values_[pkIndex], ra);
                        }
                        ++i3;
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    try {
                        this.close();
                    }
                    catch (Exception e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                }
            }
            finally {
                try {
                    this.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        return values;
    }

    @Override
    public void commit() throws Exception {
        String tempDBFName = FileUtil.uniqueTempFileName("temp", "dbf");
        DbfFileWriter dbfFileWriter = new DbfFileWriter(tempDBFName, this.dbfCharset);
        DbfFieldDef[] dbfFieldsDefs = new DbfFieldDef[this.schema.getAttributeCount()];
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            AttributeType columnType = this.schema.getAttributeType(i);
            if (!attr.isCalculated()) {
                String columnName = this.schema.getAttributeName(i);
                if (columnType == AttributeType.INTEGER || columnType == AttributeType.TINYINT || columnType == AttributeType.SMALLINT || columnType == AttributeType.BIT) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'N', 32, 0);
                } else if (columnType == AttributeType.LONG || columnType == AttributeType.BIGINT) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'N', 33, 0);
                } else if (columnType == AttributeType.DOUBLE || columnType == AttributeType.REAL || columnType == AttributeType.NUMERIC || columnType == AttributeType.FLOAT || columnType == AttributeType.BIGDECIMAL || columnType == AttributeType.DECIMAL) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'N', 33, 16);
                } else if (columnType == AttributeType.STRING || columnType == AttributeType.CHAR || columnType == AttributeType.VARCHAR || columnType == AttributeType.LONGVARCHAR || columnType == AttributeType.TEXT || columnType == AttributeType.OBJECT) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'C', 255, 0);
                } else if (columnType == AttributeType.DATE || columnType == AttributeType.TIME || columnType == AttributeType.TIMESTAMP) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'D', 8, 0);
                } else if (columnType == AttributeType.BOOLEAN) {
                    dbfFieldsDefs[i] = new DbfFieldDef(columnName, 'L', 1, 0);
                } else {
                    throw new Exception(I18N.getMessage("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.Unsupported-attribute-type-found-{0}", new Object[]{columnType}));
                }
            }
            ++i;
        }
        List<Record> values = this.getRecords();
        dbfFileWriter.writeHeader(dbfFieldsDefs, values.size());
        Iterator<Record> iterator = values.iterator();
        long cont = 0L;
        while (iterator.hasNext()) {
            Record record = iterator.next();
            Vector<Object> values_ = new Vector<Object>();
            int i2 = 0;
            while (i2 < this.schema.getAttributeCount()) {
                values_.add(record.getAttribute(i2));
                ++i2;
            }
            dbfFileWriter.writeRecord(values_);
            ++cont;
        }
        dbfFileWriter.close();
        File dbfTempFile = new File(tempDBFName);
        try {
            FileUtil.copy(dbfTempFile, this.file);
        }
        finally {
            dbfTempFile.delete();
        }
        this.pKToNewFeaturesFIDList.clear();
        this.newRecords.clear();
        this.updateRecords.clear();
        this.deleteRecords.clear();
        this.open();
        this.schema = this.getTableSchema();
        this.numReg = this.dbaseFileChannel.getRecordCount();
        this.close();
    }

    @Override
    public void executeQuery(String sqlQuery) throws SQLException {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public boolean createDataStore(Envelope vista, String geomColumn, int srid) throws Exception {
        boolean ok = true;
        if (this.schema == null) {
            throw new DBFRecordDataSourceException(I18N.getMessage("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.The-DBF-file-{0}-can-not-be-created", new Object[]{this.file.getAbsolutePath()}));
        }
        DbfFileWriter dbfFileWriter = SHP.writeDBFHeader(this.schema, 0, this.file.getAbsolutePath(), false, false, this.dbfCharset);
        dbfFileWriter.writeRealHeader(0);
        dbfFileWriter.close();
        return ok;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter, boolean ascending) {
        block11: {
            records = new ArrayList<Record>();
            sortedRecords = new ArrayList<SortedAttribute>();
            iterator = null;
            try {
                try {
                    isString = true;
                    v0 = hasFilter = filter != null;
                    if (hasFilter) {
                        DBFRecordDataSource.LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.unimplemented-filter-it-will-be-ignored"));
                    }
                    iterator = this.getIterator();
                    if (fieldOrdered == null || !this.schema.hasAttribute(fieldOrdered)) ** GOTO lbl26
                    isString = this.schema.getAttribute(fieldOrdered).getType().toJavaClass().equals(String.class);
                    while (iterator.hasNext()) {
                        currentRecord = iterator.next();
                        sortedRecords.add(new SortedAttribute(currentRecord.getAttribute(fieldOrdered), currentRecord, ascending, isString));
                    }
                    Collections.sort(sortedRecords);
                    for (SortedAttribute attr : sortedRecords) {
                        records.add((Record)attr.getRecordNumber());
                    }
                    break block11;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl26:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl27:
                    // 1 sources

                }
                catch (Exception e) {
                    DBFRecordDataSource.LOGGER.error((Object)"", (Throwable)e);
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
            finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }
        return records;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField, Filter filter, boolean ascending) {
        block17: {
            records = new ArrayList<Record>();
            sortedRecords = new ArrayList<SortedAttribute>();
            iterator = null;
            i = 0;
            while (i < fields.length) {
                if (!this.schema.hasAttribute(fields[i])) {
                    DBFRecordDataSource.LOGGER.warn((Object)I18N.getMessage("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.The-attribute-{0}-does-not-exist", new Object[]{fields[i]}));
                    return records;
                }
                ++i;
            }
            try {
                try {
                    isString = true;
                    v0 = hasFilter = filter != null;
                    if (hasFilter) {
                        DBFRecordDataSource.LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.unimplemented-filter-it-will-be-ignored"));
                    }
                    iterator = this.getIterator();
                    if (orderField == null || !this.schema.hasAttribute(orderField)) ** GOTO lbl50
                    isString = this.schema.getAttribute(orderField).getType().toJavaClass().equals(String.class);
                    while (iterator.hasNext()) {
                        currentRecord = iterator.next();
                        iguales = true;
                        i = 0;
                        while (i < fields.length && iguales) {
                            if (!currentRecord.getAttribute(fields[i]).equals(values[i])) {
                                iguales = false;
                            }
                            ++i;
                        }
                        if (!iguales) continue;
                        sortedRecords.add(new SortedAttribute(currentRecord.getAttribute(orderField), currentRecord, ascending, isString));
                    }
                    Collections.sort(sortedRecords);
                    for (SortedAttribute attr : sortedRecords) {
                        records.add((Record)attr.getRecordNumber());
                    }
                    break block17;
lbl-1000:
                    // 1 sources

                    {
                        currentRecord = iterator.next();
                        iguales = true;
                        i = 0;
                        while (i < fields.length && iguales) {
                            if (!currentRecord.getAttribute(fields[i]).equals(values[i])) {
                                iguales = false;
                            }
                            ++i;
                        }
                        if (!iguales) continue;
                        records.add(currentRecord);
lbl50:
                        // 3 sources

                        ** while (iterator.hasNext())
                    }
lbl51:
                    // 1 sources

                }
                catch (Exception e) {
                    DBFRecordDataSource.LOGGER.error((Object)"", (Throwable)e);
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
            finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }
        return records;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        if (this.open) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.data.dao.dbf.DBFRecordDataSource.the-file-is-open-the-operation-is-cancelled"));
            return values;
        }
        int index = this.schema.getAttributeIndex(field);
        try {
            try {
                this.open();
                int i = 0;
                while ((long)i < this.size()) {
                    if (i >= limite) {
                        return values;
                    }
                    Object value = this.readField(i, index);
                    if (value != null) {
                        values.add(value);
                    }
                    ++i;
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                try {
                    this.close();
                    return values;
                }
                catch (Exception e2) {
                    LOGGER.error((Object)"", (Throwable)e2);
                    return values;
                }
            }
        }
        finally {
            try {
                this.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    @Override
    public ITableIterator getIterator() {
        return new DBFIterator(this);
    }

    public int getNumReg() {
        return this.numReg;
    }

    @Override
    public void rollback() {
        super.rollback();
        this.pKToNewFeaturesFIDList.clear();
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public List<Record> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public TableRecordDataSource buildFromProperties(Map<String, Object> properties) throws Exception {
        String path = (String)properties.get("FILE");
        String charsetName = (String)properties.get("CHARSET");
        Charset charset = null;
        charset = StringUtils.isEmpty((String)charsetName) ? ShapeFileDataSource.DEFAULT_STRING_CHARSET : Charset.forName(charsetName);
        DBFRecordDataSource dbfRDS = new DBFRecordDataSource(path, null, charset);
        return dbfRDS;
    }
}

