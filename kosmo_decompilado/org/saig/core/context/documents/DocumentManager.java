/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.Element
 *  org.dom4j.io.SAXReader
 */
package org.saig.core.context.documents;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerFactory;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class DocumentManager {
    private static final Logger LOGGER = Logger.getLogger(DocumentManager.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5432;
    private static final String DEFAULT_DATABASE = "";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";
    private static final String DEFAULT_DATASOURCE_CLASSKEY = "org.saig.core.model.data.dao.jdbc.PostgreSQLDataSource";
    private static final String DEFAULT_SCHEMA = "public";
    private static final String HOST_CONFIG_KEY_NAME = "host";
    private static final String PORT_CONFIG_KEY_NAME = "port";
    private static final String USER_CONFIG_KEY_NAME = "user";
    private static final String PASSWORD_CONFIG_KEY_NAME = "password";
    private static final String SCHEMA_CONFIG_KEY_NAME = "schema";
    private static final String DATABASE_CONFIG_KEY_NAME = "database";
    private static final String DATASOURCE_CLASSKEY_CONFIG_KEY_NAME = "dataSourceClassKey";
    public static final String HELP_DOCUMENT_MANAGER_CONFIG_PATH = "config/document_manager_config.xml";
    public static final String HELP_DOCUMENT_MANAGER_KEY = "_HELP_DOCUMENT_MANAGER_KEY_";
    public static final String DM_DOCUMENTS_TABLE_NAME = "dm_documents";
    public static final String DM_DOCUMENTS_FIELD_ID_DOC = "id_doc";
    public static final String DM_DOCUMENTS_FIELD_NOMBRE = "nombre";
    public static final String DM_DOCUMENTS_FIELD_F_ALTA = "f_alta";
    public static final String DM_DOCUMENTS_FIELD_F_BAJA = "f_baja";
    public static final String DM_DOCUMENTS_FIELD_OBSERVACIONES = "observaciones";
    public static final String DM_DOCUMENTS_FIELD_ID_TIPO_DOC = "id_tipo_doc";
    public static final String DM_DOCUMENTS_FIELD_VERSION = "version";
    public static final String DM_DOCUMENTS_FIELD_ID_ORIGEN_DOC = "id_origen_doc";
    public static final String DM_DOCUMENTS_FIELD_RUTA_ORIGINAL = "ruta_original";
    public static final String DM_DOCUMENTS_FIELD_SIZE = "size";
    public static final String DM_DOCUMENTS_FIELD_NOMBRE_INTERNO = "nombre_interno";
    public static final String DM_DOCUMENTS_FIELD_MD5 = "md5";
    public static final String DM_DOCUM_TYPES_TABLE_NAME = "dm_docum_types";
    public static final String DM_DOCUM_TYPES_FIELD_ID_TIPO_DOC = "id_tipo_doc";
    public static final String DM_DOCUM_TYPES_FIELD_EXTENSION = "extension";
    public static final String DM_DOCUM_TYPES_FIELD_DESCRIPCION = "descripcion";
    public static final String DM_DOCUM_ORIG_TABLE_NAME = "dm_docum_orig";
    public static final String DM_DOCUM_ORIG_FIELD_ID_ORIGEN_DOC = "id_origen_doc";
    public static final String DM_DOCUM_ORIG_FIELD_NOMBRE = "nombre";
    public static final String DM_DOCUM_ORIG_FIELD_DESCRIPCION = "descripcion";
    public static final String DM_DOCUM_FILES_TABLE_NAME = "dm_docum_files";
    public static final String DM_DOCUM_FILES_FIELD_ID_DOC = "id_doc";
    public static final String DM_DOCUM_FILES_FIELD_FILE = "file";
    protected static Map<String, DocumentManager> docManagerPool = new Hashtable<String, DocumentManager>();
    private static Properties helpTagsConfiguration;
    protected Map<String, TableDBRecordDataSource> tablesDataSources = new Hashtable<String, TableDBRecordDataSource>();
    protected Properties connectionProperties;
    private static final Properties DEFAULT_PROPERTIES;
    private Map<Object, String> tempFilePathCache = new HashMap<Object, String>();

    static {
        DEFAULT_PROPERTIES = new Properties();
        DEFAULT_PROPERTIES.put(HOST_CONFIG_KEY_NAME, DEFAULT_HOST);
        DEFAULT_PROPERTIES.put(PORT_CONFIG_KEY_NAME, 5432);
        DEFAULT_PROPERTIES.put(USER_CONFIG_KEY_NAME, "postgres");
        DEFAULT_PROPERTIES.put(PASSWORD_CONFIG_KEY_NAME, "postgres");
        DEFAULT_PROPERTIES.put(SCHEMA_CONFIG_KEY_NAME, DEFAULT_SCHEMA);
        DEFAULT_PROPERTIES.put(DATABASE_CONFIG_KEY_NAME, DEFAULT_DATABASE);
        DEFAULT_PROPERTIES.put(DATASOURCE_CLASSKEY_CONFIG_KEY_NAME, DEFAULT_DATASOURCE_CLASSKEY);
        DocumentManager.putDocumentManager(HELP_DOCUMENT_MANAGER_KEY, HELP_DOCUMENT_MANAGER_CONFIG_PATH);
        DocumentManager.loadHelpTagsProperties();
    }

    private DocumentManager() {
    }

    private DocumentManager(Properties connectionProp) {
        this.connectionProperties = connectionProp;
    }

    public static DocumentManager getInstance(String docManagerKey) {
        return docManagerPool.get(docManagerKey);
    }

    public static DocumentManager putDocumentManager(String docManagerKey, String pathDocConfig) {
        return docManagerPool.put(docManagerKey, new DocumentManager(DocumentManager.generateConnectionProperties(pathDocConfig)));
    }

    public static DocumentManager putDocumentManager(String docManagerKey, String host, String port, String user, String password, String schema, String dataBase, String dataSourceClassKey) {
        return docManagerPool.put(docManagerKey, new DocumentManager(DocumentManager.generateConnectionProperties(host, port, user, password, schema, dataBase, dataSourceClassKey)));
    }

    private static Properties generateConnectionProperties(String host, String port, String user, String password, String schema, String dataBase, String dataSourceClassKey) {
        Properties prop = new Properties(DEFAULT_PROPERTIES);
        prop.put(HOST_CONFIG_KEY_NAME, host);
        prop.put(PORT_CONFIG_KEY_NAME, port);
        prop.put(USER_CONFIG_KEY_NAME, user);
        prop.put(PASSWORD_CONFIG_KEY_NAME, password);
        prop.put(SCHEMA_CONFIG_KEY_NAME, schema);
        prop.put(DATABASE_CONFIG_KEY_NAME, dataBase);
        prop.put(DATASOURCE_CLASSKEY_CONFIG_KEY_NAME, dataSourceClassKey);
        return prop;
    }

    private static Properties generateConnectionProperties(String pathDocConfig) {
        InputStream in;
        Properties prop;
        block23: {
            block24: {
                prop = new Properties(DEFAULT_PROPERTIES);
                in = null;
                File documentManagerConfig = new File(pathDocConfig);
                if (documentManagerConfig.exists() && documentManagerConfig.canRead()) break block23;
                LOGGER.warn((Object)I18N.getString(DocumentManager.class, "document-manager-configuration-file-does-not-exist-or-can-not-be-read"));
                if (in == null) break block24;
                try {
                    in.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return null;
        }
        try {
            try {
                SAXReader reader = new SAXReader();
                in = new FileInputStream(pathDocConfig);
                Document documentIS = reader.read(in);
                Element elementRoot = documentIS.getRootElement();
                Iterator it = elementRoot.elementIterator();
                while (it.hasNext()) {
                    Element element = (Element)it.next();
                    String text = element.getText();
                    String elementName = element.getName();
                    if (elementName.equals(HOST_CONFIG_KEY_NAME)) {
                        prop.put(HOST_CONFIG_KEY_NAME, text);
                        continue;
                    }
                    if (elementName.equals(PORT_CONFIG_KEY_NAME)) {
                        prop.put(PORT_CONFIG_KEY_NAME, text);
                        continue;
                    }
                    if (elementName.equals(USER_CONFIG_KEY_NAME)) {
                        prop.put(USER_CONFIG_KEY_NAME, text);
                        continue;
                    }
                    if (elementName.equals(PASSWORD_CONFIG_KEY_NAME)) {
                        CryptManager cryptManager = CryptManagerFactory.getManager("Password based encryption");
                        prop.put(PASSWORD_CONFIG_KEY_NAME, cryptManager.decrypt(text));
                        continue;
                    }
                    if (elementName.equals(SCHEMA_CONFIG_KEY_NAME)) {
                        prop.put(SCHEMA_CONFIG_KEY_NAME, text);
                        continue;
                    }
                    if (elementName.equals(DATABASE_CONFIG_KEY_NAME)) {
                        prop.put(DATABASE_CONFIG_KEY_NAME, text);
                        continue;
                    }
                    if (!elementName.equals(DATASOURCE_CLASSKEY_CONFIG_KEY_NAME)) continue;
                    prop.put(DATASOURCE_CLASSKEY_CONFIG_KEY_NAME, text);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                return null;
            }
        }
        catch (Throwable throwable) {
            throw throwable;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception exception) {}
            }
        }
        return prop;
    }

    private static void loadHelpTagsProperties() {
        File file = new File("helpTags.properties");
        try {
            helpTagsConfiguration = new Properties();
            if (file.canRead()) {
                helpTagsConfiguration.load(new FileInputStream(file));
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
        }
    }

    public static String getHelpTag(String key) {
        return helpTagsConfiguration.getProperty(key);
    }

    public TableDBRecordDataSource getTableDataSource(String tableName) throws Exception {
        if (this.tablesDataSources.containsKey(tableName)) {
            return this.tablesDataSources.get(tableName);
        }
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("USER", this.connectionProperties.getProperty(USER_CONFIG_KEY_NAME));
        properties.put("PASSWORD", this.connectionProperties.getProperty(PASSWORD_CONFIG_KEY_NAME));
        properties.put("TABLE_NAME", tableName);
        properties.put("DATABASE_NAME", this.connectionProperties.getProperty(DATABASE_CONFIG_KEY_NAME));
        properties.put("SCHEMA", this.connectionProperties.getProperty(SCHEMA_CONFIG_KEY_NAME));
        properties.put("HOST", this.connectionProperties.getProperty(HOST_CONFIG_KEY_NAME));
        properties.put("PORT", Integer.valueOf(this.connectionProperties.getProperty(PORT_CONFIG_KEY_NAME)));
        String pkName = DEFAULT_DATABASE;
        boolean versionable = false;
        String startField = DEFAULT_DATABASE;
        String endField = DEFAULT_DATABASE;
        String versionableField = DEFAULT_DATABASE;
        if (tableName.equals(DM_DOCUMENTS_TABLE_NAME)) {
            pkName = "id_doc";
            versionable = true;
            startField = DM_DOCUMENTS_FIELD_F_ALTA;
            endField = DM_DOCUMENTS_FIELD_F_BAJA;
            versionableField = DM_DOCUMENTS_FIELD_VERSION;
        } else if (tableName.equals(DM_DOCUM_TYPES_TABLE_NAME)) {
            pkName = "id_tipo_doc";
        } else if (tableName.equals(DM_DOCUM_ORIG_TABLE_NAME)) {
            pkName = "id_origen_doc";
        }
        properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        properties.put("DATASOURCE", this.connectionProperties.getProperty(DATASOURCE_CLASSKEY_CONFIG_KEY_NAME));
        TableDBRecordDataSource ds = (TableDBRecordDataSource)TableRecordDataSource.buildTableRecordDataSourceFromProperties(properties);
        if (versionable) {
            ds.setVersionable(true);
            FeatureSchema schema = ds.getSchema();
            schema.setFieldStartDate(startField);
            schema.setFieldEndDate(endField);
            schema.setHistoryField(versionableField);
        }
        this.tablesDataSources.put(tableName, ds);
        return ds;
    }

    public Record saveDocument(File file, String observaciones, Object idOrigenDoc, String internalName, String path, boolean almacenar) throws Exception {
        Record documentRecord;
        block13: {
            if (!(file == null || file.exists() && file.canRead())) {
                throw new Exception(I18N.getMessage(this.getClass(), "file-{0}-does-not-exist-or-can-not-be-read", new Object[]{path}));
            }
            TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
            documentRecord = new Record(tableDocumentos.getSchema());
            if (file != null) {
                documentRecord.setAttribute("nombre", (Object)FileUtil.nameWithoutExtension(file.getName()));
                documentRecord.setAttribute(DM_DOCUMENTS_FIELD_SIZE, (Object)file.length());
                Record extenRecord = this.getExtension(FileUtil.getExtension(file));
                if (extenRecord != null) {
                    documentRecord.setAttribute("id_tipo_doc", extenRecord.getPrimaryKey());
                }
            } else {
                documentRecord.setAttribute("nombre", (Object)path);
            }
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_OBSERVACIONES, (Object)observaciones);
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL, (Object)path);
            documentRecord.setAttribute("id_origen_doc", idOrigenDoc);
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_NOMBRE_INTERNO, (Object)internalName);
            try {
                tableDocumentos.add(documentRecord);
                tableDocumentos.commit();
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                tableDocumentos.rollback();
                throw e;
            }
            if (file != null && almacenar) {
                Connection conn = null;
                PreparedStatement ps = null;
                FileInputStream fis = null;
                try {
                    try {
                        conn = tableDocumentos.getConnection();
                        conn.setAutoCommit(false);
                        fis = new FileInputStream(file);
                        ps = conn.prepareStatement("INSERT INTO dm_docum_files(id_doc,file) values (?,?)");
                        ps.setLong(1, ((Number)documentRecord.getPrimaryKey()).longValue());
                        ps.setBinaryStream(2, (InputStream)fis, (int)file.length());
                        ps.executeUpdate();
                        ps.close();
                        fis.close();
                        conn.commit();
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                        if (conn != null) {
                            conn.rollback();
                        }
                        this.closeFileInputStream(fis);
                        this.closeChannel(null, ps);
                        this.closeConnection(conn);
                        break block13;
                    }
                }
                catch (Throwable throwable) {
                    this.closeFileInputStream(fis);
                    this.closeChannel(null, ps);
                    this.closeConnection(conn);
                    throw throwable;
                }
                this.closeFileInputStream(fis);
                this.closeChannel(null, ps);
                this.closeConnection(conn);
            }
        }
        return documentRecord;
    }

    public boolean isHttp(Object idDoc) throws Exception {
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUM_FILES_TABLE_NAME);
        Record docRecod = tableDocumentos.getByPrimaryKey(idDoc);
        if (docRecod != null) {
            String ruta = (String)docRecod.getAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL);
            return ruta.startsWith("http");
        }
        return false;
    }

    public void saveDocumentWithThread(File file, String observaciones, Object idOrigenDoc, String internalName, String path, boolean almacenar) throws Exception {
        SaveDocumentThreadPlugIn save = new SaveDocumentThreadPlugIn(file, observaciones, idOrigenDoc, internalName, path, almacenar);
        PlugInContext context = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
        save.initialize(context);
        new TaskMonitorManager().execute(save, context);
    }

    public Record updateDocument(Object idDocument, File file, String observaciones, Object idOrigenDoc, String internalName, String path, boolean isHttp, boolean almacenar) throws Exception {
        if (!(file == null || file.exists() && file.canRead())) {
            throw new Exception(I18N.getMessage(this.getClass(), "file-{0}-does-not-exist-or-can-not-be-read", new Object[]{file.getAbsolutePath()}));
        }
        boolean hasFile = this.hasFile(idDocument);
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        Record documentRecord = tableDocumentos.getByPrimaryKey(idDocument);
        if (file != null) {
            documentRecord.setAttribute("nombre", (Object)FileUtil.nameWithoutExtension(file.getName()));
            Record extenRecord = this.getExtension(FileUtil.getExtension(file));
            if (extenRecord != null) {
                documentRecord.setAttribute("id_tipo_doc", extenRecord.getPrimaryKey());
            }
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_SIZE, (Object)file.length());
        } else if (!hasFile || isHttp) {
            documentRecord.setAttribute("id_tipo_doc", null);
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_SIZE, null);
            if (isHttp) {
                documentRecord.setAttribute("nombre", (Object)path);
            }
        }
        documentRecord.setAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL, (Object)path);
        documentRecord.setAttribute(DM_DOCUMENTS_FIELD_OBSERVACIONES, (Object)observaciones);
        documentRecord.setAttribute("id_origen_doc", idOrigenDoc);
        documentRecord.setAttribute(DM_DOCUMENTS_FIELD_NOMBRE_INTERNO, (Object)internalName);
        try {
            tableDocumentos.update(documentRecord);
            tableDocumentos.commit();
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            tableDocumentos.rollback();
            throw e;
        }
        if (hasFile) {
            String sqlVersion = "SELECT MAX(id_doc) FROM dm_documents WHERE version=?";
            Connection conn = null;
            PreparedStatement ps = null;
            FileInputStream fis = null;
            try {
                try {
                    conn = tableDocumentos.getConnection();
                    conn.setAutoCommit(false);
                    ps = conn.prepareStatement(sqlVersion);
                    ps.setLong(1, ((Number)idDocument).longValue());
                    Long versionID = null;
                    ResultSet res = ps.executeQuery();
                    if (res.next()) {
                        versionID = res.getLong(1);
                    }
                    res.close();
                    ps.close();
                    String sqlInsert = "INSERT INTO dm_docum_files SELECT " + versionID + "," + DM_DOCUM_FILES_FIELD_FILE + " FROM " + DM_DOCUM_FILES_TABLE_NAME + " WHERE " + "id_doc" + " = ?";
                    ps = conn.prepareStatement(sqlInsert);
                    ps.setLong(1, ((Number)idDocument).longValue());
                    ps.executeUpdate();
                    ps.close();
                    if (isHttp) {
                        String sqlDelete = "DELETE FROM dm_docum_files WHERE id_doc = ?";
                        ps = conn.prepareStatement(sqlDelete);
                        ps.setLong(1, ((Number)idDocument).longValue());
                        ps.executeUpdate();
                        ps.close();
                    } else if (file != null && almacenar) {
                        if (hasFile) {
                            fis = new FileInputStream(file);
                            ps = conn.prepareStatement("UPDATE dm_docum_files SET file=? WHERE id_doc=" + ((Number)documentRecord.getPrimaryKey()).intValue());
                            ps.setBinaryStream(1, (InputStream)fis, (int)file.length());
                            ps.executeUpdate();
                            ps.close();
                            fis.close();
                        } else {
                            fis = new FileInputStream(file);
                            ps = conn.prepareStatement("INSERT INTO dm_docum_files(id_doc,file) values (?,?)");
                            ps.setLong(1, ((Number)idDocument).longValue());
                            ps.setBinaryStream(2, (InputStream)fis, (int)file.length());
                            ps.executeUpdate();
                            ps.close();
                            fis.close();
                            conn.commit();
                        }
                    }
                    conn.commit();
                }
                catch (Exception e) {
                    LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                }
            }
            catch (Throwable throwable) {
                this.closeFileInputStream(fis);
                this.closeChannel(null, ps);
                this.closeConnection(conn);
                throw throwable;
            }
            this.closeFileInputStream(fis);
            this.closeChannel(null, ps);
            this.closeConnection(conn);
        }
        return documentRecord;
    }

    @Deprecated
    public void deleteDocument(List<Record> documentsToDelete) throws Exception {
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        try {
            tableDocumentos.removeAll(documentsToDelete);
            tableDocumentos.commit();
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            tableDocumentos.rollback();
            throw e;
        }
    }

    public Record getDocumentRecord(Object idDocument) throws Exception {
        return this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME).getByPrimaryKey(idDocument);
    }

    public Record getDocumentRecord(String documentName) throws Exception {
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        List<Record> documentos = tableDocumentos.getByAttribute(new String[]{"nombre"}, new Object[]{documentName});
        if (documentos == null || documentos.isEmpty()) {
            return null;
        }
        return documentos.get(0);
    }

    public List<Record> getDocumentsRecords(Object[] idDocuments, Object origenDocument) throws Exception {
        int originValue = ((Number)origenDocument).intValue();
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        List<Record> records = tableDocumentos.getByPrimaryKey(idDocuments);
        ArrayList<Record> recordsList = new ArrayList<Record>();
        for (Record record : records) {
            Object idOrigenDocument = record.getAttribute("id_origen_doc");
            if (idOrigenDocument == null || ((Number)idOrigenDocument).intValue() != originValue) continue;
            recordsList.add(record);
        }
        return recordsList;
    }

    public String getExtension(Object idExtension) throws Exception {
        if (idExtension == null) {
            return null;
        }
        TableDBRecordDataSource dsExtension = this.getTableDataSource(DM_DOCUM_TYPES_TABLE_NAME);
        Record extensionRecord = ((TableRecordDataSource)dsExtension).getByPrimaryKey(idExtension);
        return (String)extensionRecord.getAttribute(DM_DOCUM_TYPES_FIELD_EXTENSION);
    }

    public String getOrigenDocumento(Object idOrigen) throws Exception {
        if (idOrigen == null) {
            return null;
        }
        TableDBRecordDataSource dsOrigen = this.getTableDataSource(DM_DOCUM_ORIG_TABLE_NAME);
        Record origenRecord = ((TableRecordDataSource)dsOrigen).getByPrimaryKey(idOrigen);
        return (String)origenRecord.getAttribute("nombre");
    }

    private Record getExtension(String fileExt) throws Exception {
        if ((fileExt = StringUtils.trimToNull((String)fileExt)) != null) {
            fileExt.toLowerCase();
        }
        TableDBRecordDataSource dsFileExt = this.getTableDataSource(DM_DOCUM_TYPES_TABLE_NAME);
        Record extensionRecord = null;
        List<Record> extensions = ((TableRecordDataSource)dsFileExt).getByAttribute(new String[]{DM_DOCUM_TYPES_FIELD_EXTENSION}, new Object[]{fileExt});
        if (extensions.isEmpty()) {
            extensionRecord = new Record(dsFileExt.getSchema());
            extensionRecord.setAttribute(DM_DOCUM_TYPES_FIELD_EXTENSION, (Object)fileExt);
            try {
                ((TableRecordDataSource)dsFileExt).add(extensionRecord);
                ((TableRecordDataSource)dsFileExt).commit();
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                throw e;
            }
        } else {
            extensionRecord = extensions.get(0);
        }
        return extensionRecord;
    }

    public void openDocument(Container parent, Object doc_id) throws Exception {
        OpenDocumentThreadPlugIn open = new OpenDocumentThreadPlugIn(parent, doc_id, this);
        PlugInContext context = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
        open.initialize(context);
        new TaskMonitorManager().execute(open, context);
    }

    public void openDocument(JComponent parent, String documentName) {
        try {
            Record documentRecord = this.getDocumentRecord(documentName);
            if (documentRecord == null) {
                DialogFactory.showWarningDialog(parent, I18N.getString(this.getClass(), "this-file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
                return;
            }
            this.openDocument((Container)parent, documentRecord.getPrimaryKey());
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            DialogFactory.showWarningDialog(parent, I18N.getString(this.getClass(), "this-file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
        }
    }

    public void openDocumentByInternalName(JComponent parent, String documentName) {
        try {
            TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
            List<Record> documentos = tableDocumentos.getByAttribute(new String[]{DM_DOCUMENTS_FIELD_NOMBRE_INTERNO}, new Object[]{documentName});
            if (documentos == null || documentos.isEmpty()) {
                DialogFactory.showWarningDialog(parent, I18N.getString(this.getClass(), "this-file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
                return;
            }
            Record documentRecord = documentos.get(0);
            this.openDocument((Container)parent, documentRecord.getPrimaryKey());
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            DialogFactory.showWarningDialog(parent, I18N.getString(this.getClass(), "this-file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
        }
    }

    public File getFile(Object idDocument) throws Exception {
        if (idDocument == null) {
            return null;
        }
        String tempPath = this.tempFilePathCache.get(idDocument);
        if (tempPath != null) {
            File file = new File(tempPath);
            if (file.exists() && file.canRead() && file.isFile()) {
                return file;
            }
        } else if (this.tempFilePathCache.containsKey(idDocument)) {
            return null;
        }
        TableDBRecordDataSource dsDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        Record documentRecord = dsDocumentos.getByPrimaryKey(idDocument);
        Connection conn = null;
        File temporalFile = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        FileOutputStream fileWriter = null;
        try {
            try {
                conn = dsDocumentos.getConnection();
                ps = conn.prepareStatement("SELECT file FROM dm_docum_files WHERE id_doc = ?");
                ps.setObject(1, documentRecord.getPrimaryKey());
                rs = ps.executeQuery();
                if (rs != null && rs.next()) {
                    byte[] byteBuf = rs.getBytes(1);
                    if (byteBuf != null) {
                        String extension = this.getExtension(documentRecord.getAttribute("id_tipo_doc"));
                        String name = (String)documentRecord.getAttribute("nombre");
                        temporalFile = FileUtil.createTemporalFile(name == null ? DEFAULT_DATABASE : String.valueOf(name) + "_", extension);
                        temporalFile = temporalFile.getCanonicalFile();
                        fileWriter = new FileOutputStream(temporalFile);
                        fileWriter.write(byteBuf);
                        fileWriter.flush();
                        fileWriter.close();
                    }
                } else {
                    File pathFile;
                    String origPath = (String)documentRecord.getAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL);
                    if (origPath != null && (pathFile = new File(origPath)).canRead()) {
                        temporalFile = pathFile.getCanonicalFile();
                    }
                }
                rs.close();
                ps.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                throw e;
            }
        }
        catch (Throwable throwable) {
            this.closeFileOutputStream(fileWriter);
            this.closeChannel(rs, ps);
            this.closeConnection(conn);
            throw throwable;
        }
        this.closeFileOutputStream(fileWriter);
        this.closeChannel(rs, ps);
        this.closeConnection(conn);
        if (temporalFile == null) {
            this.tempFilePathCache.put(idDocument, null);
        } else {
            this.tempFilePathCache.put(idDocument, temporalFile.getCanonicalPath());
        }
        return temporalFile;
    }

    public boolean hasFile(Object idDoc) throws Exception {
        if (idDoc == null) {
            return false;
        }
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUM_FILES_TABLE_NAME);
        String sql = "SELECT count(*) FROM dm_docum_files WHERE id_doc=?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        int cont = 0;
        try {
            try {
                con = tableDocumentos.getConnection();
                ps = con.prepareStatement(sql);
                ps.setLong(1, ((Number)idDoc).longValue());
                res = ps.executeQuery();
                if (res.next()) {
                    cont = res.getInt(1);
                }
                res.close();
                ps.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                throw e;
            }
        }
        catch (Throwable throwable) {
            this.closeChannel(res, ps);
            this.closeConnection(con);
            throw throwable;
        }
        this.closeChannel(res, ps);
        this.closeConnection(con);
        return cont > 0;
    }

    public boolean deleteFileContent(Object idDoc) throws Exception {
        if (idDoc == null) {
            return false;
        }
        boolean executed = false;
        TableDBRecordDataSource tableDocumentos = this.getTableDataSource(DM_DOCUM_FILES_TABLE_NAME);
        String sql = "DELETE FROM dm_docum_files WHERE id_doc=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            try {
                con = tableDocumentos.getConnection();
                ps = con.prepareStatement(sql);
                ps.setLong(1, ((Number)idDoc).longValue());
                executed = ps.execute();
                ps.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                con.rollback();
                throw e;
            }
        }
        catch (Throwable throwable) {
            this.closeChannel(null, ps);
            this.closeConnection(con);
            throw throwable;
        }
        this.closeChannel(null, ps);
        this.closeConnection(con);
        this.tempFilePathCache.remove(idDoc);
        return executed;
    }

    public Record saveDocument(Object idDocument, Object idOrig, String name, String observ, String ruta, String md5ToAvoidDuplicated, String innerName, StoreAction storeAction, boolean allowDuplicates, TaskMonitorDialog progressDialog) throws Exception {
        Record docRec = this.createDocumentRecord(idDocument, idOrig, name, observ, ruta, md5ToAvoidDuplicated, innerName);
        if (docRec != null) {
            this.saveDocumentRecord(docRec, storeAction, allowDuplicates, progressDialog);
        }
        return docRec;
    }

    public void saveDocumentRecord(Record documentRecord, StoreAction storeAction, boolean allowDuplicates, TaskMonitorDialog progressDialog) throws Exception {
        if (progressDialog != null) {
            progressDialog.report(I18N.getString("org.saig.core.context.documents.DocumentManager.Saving-document"));
        }
        boolean nuevoRegistro = documentRecord.getAttribute("id_doc") == null;
        String ruta = (String)documentRecord.getAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL);
        TableDBRecordDataSource documDS = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
        TableDBRecordDataSource documFilesDS = this.getTableDataSource(DM_DOCUM_FILES_TABLE_NAME);
        String md5 = (String)documentRecord.getAttribute(DM_DOCUMENTS_FIELD_MD5);
        Object idOrigDoc = documentRecord.getAttribute("id_origen_doc");
        if (storeAction == StoreAction.CREATE || storeAction == StoreAction.UPDATE) {
            List<Record> sameDocumRecords;
            if (md5 != null && !allowDuplicates && !(sameDocumRecords = documDS.getByAttribute(new String[]{DM_DOCUMENTS_FIELD_MD5, "id_origen_doc"}, new Object[]{md5, idOrigDoc}, "nombre")).isEmpty()) {
                for (Record rec : sameDocumRecords) {
                    if (rec.getPrimaryKey().equals(documentRecord.getPrimaryKey())) continue;
                    throw new IllegalArgumentException(I18N.getMessage("org.saig.core.context.documents.DocumentManager.Another-document-from-{0}-with-MD5-{1}-already-exists", new Object[]{idOrigDoc, md5}));
                }
            }
            documentRecord.setAttribute("id_tipo_doc", this.getExtension(FileUtil.getExtension(new File(ruta))).getPrimaryKey());
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_SIZE, (Object)new File(ruta).length());
        } else if (storeAction == StoreAction.DELETE) {
            md5 = null;
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_MD5, null);
            documentRecord.setAttribute("id_tipo_doc", null);
            documentRecord.setAttribute(DM_DOCUMENTS_FIELD_SIZE, null);
        }
        try {
            if (nuevoRegistro) {
                documDS.add(documentRecord);
            } else {
                documDS.update(documentRecord);
            }
            documDS.commit();
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            documDS.rollback();
            throw e;
        }
        boolean recorFileExist = false;
        try {
            recorFileExist = this.hasFile(documentRecord.getPrimaryKey());
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getString("org.saig.core.context.documents.DocumentManager.It-could-not-be-checked-that-any-content-exists"), (Throwable)e);
            documDS.rollback();
            throw e;
        }
        if (storeAction == StoreAction.CREATE || storeAction == StoreAction.UPDATE) {
            String name;
            if (recorFileExist) {
                this.deleteFileContent(documentRecord.getPrimaryKey());
            }
            File file = new File(ruta);
            if (progressDialog != null) {
                progressDialog.report(I18N.getString("org.saig.core.context.documents.DocumentManager.Saving-the-content-in-the-database"));
            }
            Connection conn = null;
            PreparedStatement ps = null;
            FileInputStream fis = null;
            try {
                try {
                    conn = documFilesDS.getConnection();
                    conn.setAutoCommit(false);
                    fis = new FileInputStream(file);
                    ps = conn.prepareStatement("INSERT INTO dm_docum_files(id_doc,file) values (?,?)");
                    ps.setLong(1, ((Number)documentRecord.getPrimaryKey()).longValue());
                    ps.setBinaryStream(2, (InputStream)fis, (int)file.length());
                    ps.executeUpdate();
                    ps.close();
                    fis.close();
                    conn.commit();
                }
                catch (SQLException e) {
                    LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                }
            }
            catch (Throwable throwable) {
                this.closeFileInputStream(fis);
                this.closeChannel(null, ps);
                this.closeConnection(conn);
                throw throwable;
            }
            this.closeFileInputStream(fis);
            this.closeChannel(null, ps);
            this.closeConnection(conn);
            if (progressDialog != null) {
                progressDialog.report(I18N.getString("org.saig.core.context.documents.DocumentManager.Updating-the-temporal-file-cache"));
            }
            File temporalFile = FileUtil.createTemporalFile((name = (String)documentRecord.getAttribute("nombre")) == null ? DEFAULT_DATABASE : String.valueOf(name) + "_", this.getExtension(documentRecord.getAttribute("id_tipo_doc")));
            FileUtil.copy(file, temporalFile);
            this.tempFilePathCache.put(documentRecord.getPrimaryKey(), temporalFile.getAbsolutePath());
        } else if (storeAction == StoreAction.DELETE && recorFileExist) {
            if (progressDialog != null) {
                progressDialog.report(I18N.getString("org.saig.core.context.documents.DocumentManager.Deleting-the-document-content"));
            }
            this.deleteFileContent(documentRecord.getPrimaryKey());
        }
    }

    public Record createDocumentRecord(Object idDocument, Object idOrig, String name, String observ, String ruta, String md5ToAvoidDuplicated, String innerName) {
        Record docRec = null;
        try {
            docRec = new Record(this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME).getSchema());
            docRec.setAttribute("id_doc", idDocument);
            docRec.setAttribute("id_origen_doc", idOrig);
            docRec.setAttribute("nombre", (Object)name);
            docRec.setAttribute(DM_DOCUMENTS_FIELD_OBSERVACIONES, (Object)observ);
            docRec.setAttribute(DM_DOCUMENTS_FIELD_RUTA_ORIGINAL, (Object)ruta);
            docRec.setAttribute(DM_DOCUMENTS_FIELD_MD5, (Object)md5ToAvoidDuplicated);
            docRec.setAttribute(DM_DOCUMENTS_FIELD_NOMBRE_INTERNO, (Object)innerName);
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
        }
        return docRec;
    }

    public List<Record> getDocumentRecordsByIdOrig(Object idOrigDoc) {
        List<Record> records = null;
        try {
            records = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME).getByAttribute(new String[]{"id_origen_doc"}, new Object[]{idOrigDoc}, "nombre");
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            records = new ArrayList<Record>();
        }
        return records;
    }

    public boolean removeDocument(List<Record> documentsToRemove, boolean removeContentAndRelation) {
        TableDBRecordDataSource tableDocumentos = null;
        try {
            tableDocumentos = this.getTableDataSource(DM_DOCUMENTS_TABLE_NAME);
            boolean versionable = tableDocumentos.getSchema().isVersionable();
            if (removeContentAndRelation) {
                tableDocumentos.setVersionable(false);
            }
            tableDocumentos.removeAll(documentsToRemove);
            tableDocumentos.commit();
            if (removeContentAndRelation) {
                tableDocumentos.setVersionable(versionable);
                if (versionable) {
                    FeatureSchema schema = tableDocumentos.getSchema();
                    schema.setFieldStartDate(DM_DOCUMENTS_FIELD_F_ALTA);
                    schema.setFieldEndDate(DM_DOCUMENTS_FIELD_F_BAJA);
                    schema.setHistoryField(DM_DOCUMENTS_FIELD_VERSION);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)e);
            if (tableDocumentos != null) {
                tableDocumentos.rollback();
            }
            return false;
        }
        for (Record record : documentsToRemove) {
            this.tempFilePathCache.remove(record.getPrimaryKey());
        }
        return true;
    }

    public void resetTempFilePathCache() {
        this.tempFilePathCache = new HashMap<Object, String>();
    }

    protected void closeChannel(ResultSet resultset, Statement statement) {
        if (resultset != null) {
            try {
                resultset.close();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
            }
        }
    }

    protected void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        }
        catch (SQLException ex) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
        }
        catch (Exception ex) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
        }
    }

    protected void closeFileInputStream(FileInputStream fis) {
        try {
            if (fis != null) {
                fis.close();
            }
        }
        catch (IOException ex) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
        }
    }

    protected void closeFileOutputStream(FileOutputStream fos) {
        try {
            if (fos != null) {
                fos.close();
            }
        }
        catch (IOException ex) {
            LOGGER.error((Object)DEFAULT_DATABASE, (Throwable)ex);
        }
    }

    private class OpenDocumentThreadPlugIn
    extends AbstractPlugIn
    implements ThreadedPlugIn {
        private Object idDoc;
        private Container parent;
        private final DocumentManager documentManager;

        public OpenDocumentThreadPlugIn(Container parent, Object idDoc, DocumentManager docManager) {
            this.idDoc = idDoc;
            this.parent = parent;
            this.documentManager = docManager;
        }

        @Override
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            monitor.report(I18N.getString(this.getClass(), "opening-the-document"));
            try {
                String path = null;
                boolean hasFile = DocumentManager.this.hasFile(this.idDoc);
                if (hasFile) {
                    File file = this.documentManager.getFile(this.idDoc);
                    file.deleteOnExit();
                    if (file != null) {
                        path = file.getAbsolutePath();
                        if (path == null) {
                            DialogFactory.showWarningDialog(this.parent, I18N.getString(this.getClass(), "document-has-no-hyperlink-attached"), I18N.getString(this.getClass(), "warning"));
                            return;
                        }
                        File f = new File(path);
                        if (!f.canRead()) {
                            DialogFactory.showWarningDialog(this.parent, I18N.getString(this.getClass(), "file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
                            return;
                        }
                    }
                } else {
                    Record documentRecord = DocumentManager.this.getDocumentRecord(this.idDoc);
                    path = (String)documentRecord.getAttribute(DocumentManager.DM_DOCUMENTS_FIELD_RUTA_ORIGINAL);
                }
                HiperLinkNavigatorDialog.openHiperLinkWindow(path, null);
            }
            catch (Exception e) {
                LOGGER.error((Object)DocumentManager.DEFAULT_DATABASE, (Throwable)e);
                DialogFactory.showWarningDialog(this.parent, I18N.getString(this.getClass(), "this-file-is-not-accessible"), I18N.getString(this.getClass(), "warning"));
            }
        }

        @Override
        public String getName() {
            return I18N.getString(this.getClass(), "open-document");
        }
    }

    private class SaveDocumentThreadPlugIn
    extends AbstractPlugIn
    implements ThreadedPlugIn {
        private File file;
        private String observaciones;
        private Object idOrigenDoc;
        private String internalName;
        private String path;
        private boolean almacenar;

        public SaveDocumentThreadPlugIn(File file, String observaciones, Object idOrigenDoc, String internalName, String path, boolean almacenar) {
            this.file = file;
            this.observaciones = observaciones;
            this.idOrigenDoc = idOrigenDoc;
            this.internalName = internalName;
            this.path = path;
        }

        @Override
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            monitor.report(I18N.getString(this.getClass(), "saving-document"));
            DocumentManager.this.saveDocument(this.file, this.observaciones, this.idOrigenDoc, this.internalName, this.path, this.almacenar);
        }

        @Override
        public String getName() {
            return I18N.getString(this.getClass(), "save-document");
        }
    }

    public static enum StoreAction {
        NONE,
        CREATE,
        UPDATE,
        DELETE;

    }
}

