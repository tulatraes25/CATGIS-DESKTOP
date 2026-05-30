/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.Element
 *  org.dom4j.Node
 *  org.dom4j.io.SAXReader
 */
package org.saig.core.model.data.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.saig.core.context.GenericContext;
import org.saig.core.crypt.CryptManagerFactory;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;

public class TableMappings {
    private static final Logger LOGGER = Logger.getLogger(TableMappings.class);
    private static TableMappings instance;
    private Map<String, Table> tablesMap;

    private TableMappings() {
        this.initialize();
    }

    public static TableMappings getInstance() {
        if (instance == null) {
            instance = new TableMappings();
        }
        return instance;
    }

    public Object getFieldValue(String key, String field, Object keyValue) {
        if (this.tablesMap.containsKey(key)) {
            Table table = this.tablesMap.get(key);
            return table.getFieldValue(field, keyValue);
        }
        return null;
    }

    public TableDBRecordDataSource getTableDataSource(String host, String port, String user, String password, String dataBase, String schema, String tableName, String pkName, String dataSourceClassKey) throws Exception {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("USER", user);
        properties.put("PASSWORD", password);
        properties.put("TABLE_NAME", tableName);
        properties.put("DATABASE_NAME", dataBase);
        properties.put("SCHEMA", schema);
        properties.put("HOST", host);
        properties.put("PORT", Integer.parseInt(port));
        properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        properties.put("DATASOURCE", dataSourceClassKey);
        TableDBRecordDataSource ds = (TableDBRecordDataSource)TableRecordDataSource.buildTableRecordDataSourceFromProperties(properties);
        return ds;
    }

    protected void initialize() {
        this.initialize(new File("tableMappings.xml"));
    }

    public void initialize(File configFile) {
        block16: {
            this.tablesMap = new HashMap<String, Table>();
            InputStream inputStream = null;
            try {
                try {
                    LOGGER.info((Object)"Cargando la configuracion asociada");
                    if (configFile != null && configFile.exists() && configFile.canRead()) {
                        SAXReader saxReader = new SAXReader();
                        inputStream = new FileInputStream(configFile);
                        Document doc = saxReader.read(inputStream);
                        Element root = doc.getRootElement();
                        List datasorceNodes = root.selectNodes("DATASOURCE");
                        for (Node dataSourceNode : datasorceNodes) {
                            String host = ((Element)dataSourceNode).attributeValue("host");
                            String port = ((Element)dataSourceNode).attributeValue("port");
                            String dataBase = ((Element)dataSourceNode).attributeValue("dataBase");
                            String user = ((Element)dataSourceNode).attributeValue("user");
                            String password = CryptManagerFactory.getManager("Password based encryption").decrypt(((Element)dataSourceNode).attributeValue("password"));
                            String dataSourceClass = ((Element)dataSourceNode).attributeValue("class");
                            List tables = dataSourceNode.selectNodes("TABLE");
                            for (Node tableNode : tables) {
                                String id = ((Element)tableNode).attributeValue("id");
                                String schema = ((Element)tableNode).attributeValue("schema");
                                String tableName = ((Element)tableNode).attributeValue("table_name");
                                String codField = ((Element)tableNode).attributeValue("pk_name");
                                TableDBRecordDataSource tableDBRecordDataSource = this.getTableDataSource(host, port, user, password, dataBase, schema, tableName, codField, dataSourceClass);
                                ArrayList<String> fieldNames = new ArrayList<String>();
                                List fields = tableNode.selectNodes("FIELD");
                                for (Node fieldNode : fields) {
                                    String fieldName = ((Element)fieldNode).attributeValue("name");
                                    fieldNames.add(fieldName);
                                }
                                Table table = new Table(codField, fieldNames, tableDBRecordDataSource);
                                this.tablesMap.put(id, table);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (inputStream == null) break block16;
                    try {
                        inputStream.close();
                    }
                    catch (IOException e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                }
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
    }

    public void initialize(File configFile, GenericContext context) throws Exception {
        block14: {
            this.tablesMap = new HashMap<String, Table>();
            InputStream inputStream = null;
            try {
                try {
                    LOGGER.info((Object)"Cargando la configuracion asociada");
                    if (configFile != null && configFile.exists() && configFile.canRead()) {
                        SAXReader saxReader = new SAXReader();
                        inputStream = new FileInputStream(configFile);
                        Document doc = saxReader.read(inputStream);
                        Element root = doc.getRootElement();
                        List datasorceNodes = root.selectNodes("DATASOURCE");
                        for (Node dataSourceNode : datasorceNodes) {
                            String dataSourceClass = ((Element)dataSourceNode).attributeValue("class");
                            List tables = dataSourceNode.selectNodes("TABLE");
                            for (Node tableNode : tables) {
                                String id = ((Element)tableNode).attributeValue("id");
                                String tableName = ((Element)tableNode).attributeValue("table_name");
                                String codField = ((Element)tableNode).attributeValue("pk_name");
                                TableDBRecordDataSource tableDBRecordDataSource = context.getTableDataSource(tableName, codField);
                                ArrayList<String> fieldNames = new ArrayList<String>();
                                List fields = tableNode.selectNodes("FIELD");
                                for (Node fieldNode : fields) {
                                    String fieldName = ((Element)fieldNode).attributeValue("name");
                                    fieldNames.add(fieldName);
                                }
                                Table table = new Table(codField, fieldNames, tableDBRecordDataSource);
                                this.tablesMap.put(id, table);
                            }
                        }
                        break block14;
                    }
                    throw new Exception("Se han producido errores relacionados con la carga de la configuraci\u00f3n asociada al mapeo de tablas.");
                }
                catch (Exception e) {
                    throw new Exception("Se han producido errores relacionados con la carga de la configuraci\u00f3n asociada al mapeo de tablas.");
                }
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        TableMappings tableMappings = TableMappings.getInstance();
        LOGGER.info((Object)("Diametro 1 -> " + tableMappings.getFieldValue("diametro", "valor", new Integer(0))));
        LOGGER.info((Object)("Diametro 3 -> " + tableMappings.getFieldValue("diametro", "valor", new Integer(5))));
        LOGGER.info((Object)("Diametro 25 -> " + tableMappings.getFieldValue("diametro", "valor", new Integer(25))));
        LOGGER.info((Object)("ESTADOS 1 -> " + tableMappings.getFieldValue("estados", "nombre", 1)));
        LOGGER.info((Object)("ESTADOS 3 -> " + tableMappings.getFieldValue("estados", "descripcion", 3)));
    }

    private class Table {
        private List<String> fields;
        private Map<Object, Object[]> fieldValues;
        private String codField;

        public Table(String codField, List<String> fields, TableDBRecordDataSource ds) throws Exception {
            this.fields = fields;
            this.fieldValues = new HashMap<Object, Object[]>();
            this.codField = codField;
            this.initialize(ds);
        }

        private void initialize(TableDBRecordDataSource ds) throws Exception {
            ITableIterator iterator = null;
            try {
                iterator = ds.getIterator();
                while (iterator.hasNext()) {
                    Record record = iterator.next();
                    Object[] row = new Object[this.fields.size()];
                    int i = 0;
                    while (i < this.fields.size()) {
                        row[i] = record.getAttribute(this.fields.get(i));
                        ++i;
                    }
                    this.fieldValues.put(record.getAttribute(this.codField), row);
                }
            }
            finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }

        public Object getFieldValue(String field, Object keyValue) {
            if (this.fieldValues.containsKey(keyValue)) {
                Object[] row = this.fieldValues.get(keyValue);
                int index = this.fields.indexOf(field);
                if (index >= 0) {
                    return row[index];
                }
            }
            return null;
        }
    }
}

