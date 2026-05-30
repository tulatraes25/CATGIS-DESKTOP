/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerFactory;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.MDBDataSource;
import org.saig.core.model.data.dao.jdbc.MySQLDataSource;
import org.saig.core.model.data.dao.jdbc.OracleDataSource;
import org.saig.core.model.data.dao.jdbc.PostgreSQLDataSource;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.jump.lang.I18N;

public abstract class TableRecordDataSource {
    private static final Logger LOGGER = Logger.getLogger(TableRecordDataSource.class);
    protected boolean editable = true;
    protected FeatureSchema schema;
    public static final String DATASOURCE_CLASS_KEY = "DATASOURCE";
    public static final String FILE_KEY = "FILE";
    public static final String USER_KEY = "USER";
    public static final String PASSWORD_KEY = "PASSWORD";
    public static final String TABLE_NAME_KEY = "TABLE_NAME";
    public static final String DATABASE_KEY = "DATABASE_NAME";
    public static final String HOST_KEY = "HOST";
    public static final String PORT_KEY = "PORT";
    public static final String SCHEMA_KEY = "SCHEMA";
    public static final String PRIMARY_KEY_COLUMN_NAME = "PRIMARY_KEY_COLUMN_NAME";
    public static final String ENCRYPTED_PASSWORD_KEY = "ENCRYPTED_PASSWORD";
    public static final String CHARSET_NAME_KEY = "CHARSET";
    protected Map<String, Object> properties = new HashMap<String, Object>();
    protected Set<Record> updateRecords = new HashSet<Record>();
    protected Set<Record> newRecords = new HashSet<Record>();
    protected Set<Record> deleteRecords = new HashSet<Record>();
    protected boolean inMemory = true;

    public abstract void add(Record var1) throws Exception;

    public abstract void addAll(Collection<Record> var1) throws Exception;

    public abstract void update(Record var1) throws Exception;

    public abstract void updateAll(Collection<Record> var1) throws Exception;

    public abstract void remove(Record var1) throws Exception;

    public abstract void removeAll(Collection<Record> var1) throws Exception;

    public abstract List<Record> getRecords();

    public abstract List<Record> getRecords(String var1);

    public abstract List<Record> getRecords(String var1, Filter var2);

    public abstract List<Record> getRecords(String var1, Filter var2, boolean var3);

    public abstract long size();

    public abstract void executeQuery(String var1) throws SQLException;

    public FeatureSchema getSchema() {
        return this.schema;
    }

    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
        this.updateRecords = new HashSet<Record>();
        this.newRecords = new HashSet<Record>();
        this.deleteRecords = new HashSet<Record>();
        this.inMemory = true;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public abstract void commit() throws Exception;

    public void rollback() {
        this.updateRecords.clear();
        this.deleteRecords.clear();
        this.newRecords.clear();
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public abstract String getName();

    public abstract void setName(String var1);

    public static TableRecordDataSource buildTableRecordDataSourceFromProperties(Map<String, Object> properties) throws Exception {
        TableRecordDataSource trds = null;
        String datasourceClassName = (String)properties.get(DATASOURCE_CLASS_KEY);
        if (datasourceClassName != null) {
            Class<?> dataSourceClass = Class.forName(datasourceClassName);
            trds = (TableRecordDataSource)dataSourceClass.newInstance();
            trds = trds.buildFromProperties(properties);
        }
        return trds;
    }

    public static Connection getConnection(Map<String, Object> properties) throws Exception {
        TableDBRecordDataSource trds = null;
        String datasourceClass = (String)properties.get(DATASOURCE_CLASS_KEY);
        if (datasourceClass != null) {
            String user = (String)properties.get(USER_KEY);
            String passw = (String)properties.get(PASSWORD_KEY);
            String encryptPassw = (String)properties.get(ENCRYPTED_PASSWORD_KEY);
            String databaseName = (String)properties.get(DATABASE_KEY);
            String schemaName = (String)properties.get(SCHEMA_KEY);
            if (databaseName == null) {
                databaseName = schemaName;
                schemaName = "";
            }
            if (passw == null && encryptPassw != null) {
                passw = TableRecordDataSource.getDecryptedPassword(encryptPassw);
            }
            String hostName = (String)properties.get(HOST_KEY);
            Integer port = (Integer)properties.get(PORT_KEY);
            if (datasourceClass.equals(DBFRecordDataSource.class.getName()) || datasourceClass.equals(MDBDataSource.class.getName())) {
                return null;
            }
            if (datasourceClass.equals(MySQLDataSource.class.getName())) {
                trds = new MySQLDataSource(hostName, port, databaseName, user, passw);
            } else if (datasourceClass.equals(OracleDataSource.class.getName())) {
                trds = new OracleDataSource(hostName, port, databaseName, user, passw);
                ((OracleDataSource)trds).setDataBaseSchema(schemaName);
            } else if (datasourceClass.equals(PostgreSQLDataSource.class.getName())) {
                trds = new PostgreSQLDataSource(hostName, port, databaseName, user, passw);
                ((PostgreSQLDataSource)trds).setDataBaseSchema(schemaName);
            } else {
                throw new Exception(I18N.getString(TableRecordDataSource.class, "database-type-not-supported"));
            }
            return DataBaseConnectionFactory.getConnection(trds);
        }
        throw new Exception(I18N.getString(TableRecordDataSource.class, "database-type-not-supported"));
    }

    public abstract Record getRecord(int var1) throws Exception;

    public boolean isEmpty() {
        return this.size() == 0L;
    }

    public abstract Record getByPrimaryKey(Object var1);

    public abstract List<Record> getByPrimaryKey(Object[] var1);

    public abstract List<Record> getByAttribute(String[] var1, Object[] var2);

    public abstract List<Record> getByAttribute(String[] var1, Object[] var2, String var3);

    public abstract List<Record> getByAttribute(String[] var1, Object[] var2, String var3, Filter var4);

    public abstract List<Record> getByAttribute(String[] var1, Object[] var2, String var3, Filter var4, boolean var5);

    protected List<Object> getKeys(Set<Record> records) {
        ArrayList<Object> resultado = new ArrayList<Object>();
        for (Record element : records) {
            resultado.add(element.getPrimaryKey());
        }
        return resultado;
    }

    public abstract List<Object> getOrderedPrimaryKeyList();

    public abstract List<Object> getSortKeys(String var1, boolean var2);

    public abstract Set<Object> getDistintsValues(String var1);

    public abstract Set<Object> getDistintsValues(String var1, int var2);

    public abstract List<Object> getFieldValue(String var1, String var2, Object var3);

    public abstract Map<Object, RelationAttribute> getMapFieldsValues(String[] var1, String var2);

    protected Record getUpdatedRecord(Record record) {
        if (this.updateRecords != null && this.updateRecords.contains(record)) {
            for (Record element : this.updateRecords) {
                if (!record.equals(element)) continue;
                return element;
            }
        }
        return record;
    }

    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }

    public abstract boolean createDataStore(Envelope var1, String var2, int var3) throws Exception;

    public void setVersionable(boolean versionable) {
        this.schema.setVersionable(versionable);
        if (!versionable) {
            this.schema.setFieldEndDate(null);
            this.schema.setFieldStartDate(null);
            this.schema.setVersionableViewDate(null);
        }
    }

    public static String getEncryptedPassword(String plainPassword) {
        String encryptedPassword = "";
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            encryptedPassword = manager.encrypt(plainPassword);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return encryptedPassword;
    }

    public static String getDecryptedPassword(String encryptedPassword) {
        String decryptedPassword = "";
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            decryptedPassword = manager.decrypt(encryptedPassword);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return decryptedPassword;
    }

    public Set<Record> getDeletedRecords() {
        return this.deleteRecords;
    }

    public Set<Record> getUpdatedRecords() {
        return this.updateRecords;
    }

    public Set<Record> getNewRecords() {
        return this.newRecords;
    }

    public abstract ITableIterator getIterator();

    public abstract List<Record> getHistoryOfElement(Object var1, Filter var2) throws Exception;

    protected List<long[]> getRanges(Object[] values) {
        long value1;
        ArrayList<long[]> ranges = new ArrayList<long[]>();
        if (values.length == 0) {
            return ranges;
        }
        ArrayList<Object> valuesCopyList = new ArrayList<Object>();
        int i = 0;
        while (i < values.length) {
            if (!(values[i] instanceof Record)) {
                valuesCopyList.add(values[i]);
            }
            ++i;
        }
        Object[] valuesCopy = new Object[valuesCopyList.size()];
        valuesCopyList.toArray(valuesCopy);
        Arrays.sort(valuesCopy);
        long value2 = value1 = ((Number)valuesCopy[0]).longValue();
        int i2 = 1;
        while (i2 < valuesCopy.length) {
            long value = ((Number)valuesCopy[i2]).longValue();
            if (value == value2 + 1L) {
                value2 = value;
            } else {
                ranges.add(new long[]{value1, value2});
                value1 = value;
                value2 = value;
            }
            ++i2;
        }
        ranges.add(new long[]{value1, value2});
        return ranges;
    }

    public abstract String getID();

    public abstract TableRecordDataSource buildFromProperties(Map<String, Object> var1) throws Exception;
}

