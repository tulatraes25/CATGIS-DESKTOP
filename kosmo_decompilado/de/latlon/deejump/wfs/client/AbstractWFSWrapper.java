/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.httpclient.Credentials
 *  org.apache.commons.httpclient.HttpClient
 *  org.apache.commons.httpclient.UsernamePasswordCredentials
 *  org.apache.commons.httpclient.auth.AuthScope
 *  org.apache.commons.httpclient.params.HttpClientParams
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.xml.DOMPrinter
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.GMLSchemaDocument
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.ogcwebservices.OWSUtils
 *  org.deegree.ogcwebservices.getcapabilities.DCPType
 *  org.deegree.ogcwebservices.getcapabilities.HTTP
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 */
package de.latlon.deejump.wfs.client;

import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.saig.jump.lang.I18N;
import org.w3c.dom.Node;
import sun.misc.BASE64Encoder;

public abstract class AbstractWFSWrapper {
    public static final String WFS_PREFIX = "wfs";
    private static Logger LOGGER = Logger.getLogger(AbstractWFSWrapper.class);
    public static final String TRANSACTION_OPERATION_NAME = "Transaction";
    protected String baseURL;
    protected String serverTitle;
    protected String serverAbstract;
    protected Map<String, WFSFeatureType> ftNameToWfsFT;
    private Map<String, GMLSchema> featureTypeToSchema;
    private Map<String, String> featureTypeToSchemaXML;
    private Map<String, QualifiedName[]> geoPropsNameToQNames;
    protected HttpClient httpClient;
    protected BasicAuthentificationData basicAuthData;
    protected boolean initialized = false;
    protected boolean transactional = false;
    protected Operation[] globalFeatureTypeOperations;
    protected String[] featureTypes;
    protected WFSCapabilities wfsCapabilities;
    protected String getFeatureUrl;
    protected String capsString;

    public abstract String getServiceVersion();

    public String getBaseWfsURL() {
        return this.baseURL;
    }

    protected AbstractWFSWrapper(BasicAuthentificationData logins, String baseUrl) {
        if (StringUtils.isEmpty((String)baseUrl)) {
            throw new IllegalArgumentException("The URL for the WFS server can't be null or empty");
        }
        this.baseURL = baseUrl;
        this.basicAuthData = logins;
        this.featureTypeToSchema = new HashMap<String, GMLSchema>();
        this.featureTypeToSchemaXML = new HashMap<String, String>();
        this.geoPropsNameToQNames = new HashMap<String, QualifiedName[]>();
        this.createHttpClient();
    }

    public String getCapabilitiesURL() {
        StringBuffer sb = new StringBuffer(OWSUtils.validateHTTPGetBaseURL((String)this.baseURL));
        sb.append("SERVICE=WFS&REQUEST=GetCapabilities&VERSION=");
        sb.append(this.getServiceVersion());
        return sb.toString();
    }

    public String getDescribeTypeURL(String baseURL, QualifiedName typeName) {
        String url = String.valueOf(baseURL) + "SERVICE=WFS&REQUEST=DescribeFeatureType&version=" + this.getServiceVersion() + "&TYPENAME=";
        String typeNameURL = "";
        if (StringUtils.isEmpty((String)typeName.getPrefix())) {
            typeNameURL = String.valueOf(typeNameURL) + typeName.getLocalName();
        } else {
            typeNameURL = String.valueOf(typeNameURL) + typeName.getPrefix() + ":" + URLEncoder.encode(typeName.getLocalName());
            if (typeName.getNamespace() != null) {
                String encodedNamespace = typeName.getNamespace().toString();
                typeNameURL = String.valueOf(typeNameURL) + "&NAMESPACE=xmlns(" + typeName.getPrefix() + "=" + encodedNamespace + ")";
            }
        }
        url = String.valueOf(url) + typeNameURL;
        return url;
    }

    public String getDescribeTypeURL(QualifiedName typename) {
        String url = this.getDescribeTypeURL(OWSUtils.validateHTTPGetBaseURL((String)this.createDescribeFTOnlineResource()), typename);
        LOGGER.debug((Object)("Describe Feature Type request:\n" + url));
        return url;
    }

    public synchronized GMLSchema getSchemaForFeatureType(String featureType) throws Exception {
        GMLSchema res = this.featureTypeToSchema.get(featureType);
        if (res != null) {
            return res;
        }
        this.createSchemaForFeatureType(featureType);
        return this.featureTypeToSchema.get(featureType);
    }

    public String getRawSchemaForFeatureType(String featureType) {
        return this.featureTypeToSchemaXML.get(featureType);
    }

    protected String loadSchemaForFeatureType(String featureType) throws Exception {
        String descrFtUrl = this.createDescribeFTOnlineResource();
        if (descrFtUrl == null) {
            throw new Exception("Service does not have a DescribeFeatureType operation accessible by HTTP GET or POST");
        }
        WFSFeatureType wfsFt = this.getFeatureTypeByName(featureType);
        if (wfsFt == null) {
            return null;
        }
        QualifiedName ft = wfsFt.getName();
        String serverReq = this.getDescribeTypeURL(ft);
        try {
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            URL url = new URL(serverReq);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (this.basicAuthData != null) {
                String userPassword = String.valueOf(this.basicAuthData.getUserName()) + ":" + this.basicAuthData.getPassword();
                String encoding = new BASE64Encoder().encode(userPassword.getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encoding);
                LOGGER.info((Object)"Setting WFS Basic Auth credentials from user options");
            } else {
                LOGGER.info((Object)"No authentification set");
            }
            String uri = url.toExternalForm();
            xsdDoc.load(conn.getInputStream(), uri);
            return DOMPrinter.nodeToString((Node)xsdDoc.getRootElement(), null);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            String mesg = "Error fetching FeatureType description";
            LOGGER.error((Object)(String.valueOf(mesg) + " for " + featureType + " using " + serverReq));
            throw new Exception(mesg, e);
        }
    }

    protected synchronized void createSchemaForFeatureType(String featureTypeName) throws Exception {
        try {
            String rawXML = this.loadSchemaForFeatureType(featureTypeName);
            if (rawXML == null) {
                return;
            }
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            xsdDoc.load((Reader)new StringReader(rawXML), "http://www.deegree.org");
            GMLSchema xsd = xsdDoc.parseGMLSchema();
            this.featureTypeToSchema.put(featureTypeName, xsd);
            this.featureTypeToSchemaXML.put(featureTypeName, rawXML);
            QualifiedName[] geoProp = AbstractWFSWrapper.guessGeomProperty(xsd, featureTypeName);
            this.geoPropsNameToQNames.put(featureTypeName, geoProp);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw e;
        }
    }

    public String[] getProperties(String featureType) {
        ArrayList<String> propsList = new ArrayList<String>();
        try {
            this.createSchemaForFeatureType(featureType);
            GMLSchema schema = this.featureTypeToSchema.get(featureType);
            if (schema != null) {
                FeatureType[] fts = schema.getFeatureTypes();
                int i = 0;
                while (i < fts.length) {
                    if (fts[i].getName().getLocalName().equals(featureType)) {
                        PropertyType[] props = fts[i].getProperties();
                        int j = 0;
                        while (j < props.length) {
                            if (!AbstractWFSWrapper.isGeometryPropertyType(props[j].getType())) {
                                propsList.add(props[j].getName().getPrefixedName());
                            }
                            ++j;
                        }
                    }
                    ++i;
                }
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
            propsList = new ArrayList();
        }
        return propsList.toArray(new String[propsList.size()]);
    }

    public synchronized WFSFeatureType getFeatureTypeByName(String ftName) {
        if (this.ftNameToWfsFT == null) {
            this.getFeatureTypes();
        }
        return this.ftNameToWfsFT.get(ftName);
    }

    private static QualifiedName[] guessGeomProperty(GMLSchema schema, String featureTypeName) {
        QualifiedName[] geoPropNames = null;
        ArrayList<QualifiedName> tmpList = new ArrayList<QualifiedName>(20);
        FeatureType[] fts = schema.getFeatureTypes();
        int i = 0;
        while (i < fts.length) {
            if (fts[i].getName().getLocalName().equals(featureTypeName)) {
                PropertyType[] props = fts[i].getProperties();
                int j = 0;
                while (j < props.length) {
                    if (AbstractWFSWrapper.isGeometryPropertyType(props[j].getType())) {
                        tmpList.add(props[j].getName());
                    }
                    ++j;
                }
            }
            ++i;
        }
        geoPropNames = tmpList.toArray(new QualifiedName[tmpList.size()]);
        return geoPropNames;
    }

    private static boolean isGeometryPropertyType(int type) {
        boolean isGeometry = false;
        switch (type) {
            case 10012: 
            case 10013: 
            case 11012: 
            case 11013: 
            case 11014: 
            case 11015: 
            case 11016: 
            case 11017: {
                isGeometry = true;
                break;
            }
            default: {
                isGeometry = false;
            }
        }
        return isGeometry;
    }

    public QualifiedName[] getGeometryProperties(String featureType) {
        return this.geoPropsNameToQNames.get(featureType);
    }

    protected void createHttpClient() {
        String proxySet;
        this.httpClient = new HttpClient();
        Properties systemSettings = System.getProperties();
        if (systemSettings != null && StringUtils.isNotEmpty((String)(proxySet = systemSettings.getProperty("http.proxySet", "false"))) && proxySet.equals("true")) {
            String proxyHost = systemSettings.getProperty("http.proxyHost");
            String proxyPort = systemSettings.getProperty("http.proxyPort");
            this.httpClient.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort).intValue());
            String proxyUserName = systemSettings.getProperty("http.proxyUserName");
            String proxyPassword = systemSettings.getProperty("http.proxyPassword");
            if (StringUtils.isNotEmpty((String)proxyUserName)) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUserName, proxyPassword);
                AuthScope scope = new AuthScope(AuthScope.ANY_HOST, -1);
                this.httpClient.getState().setProxyCredentials(scope, (Credentials)credentials);
            }
            String string = systemSettings.getProperty("http.nonProxyHosts");
        }
        HttpClientParams clientPars = new HttpClientParams();
        clientPars.setConnectionManagerTimeout(60000L);
        this.httpClient.setParams(clientPars);
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public String getServerTitle() {
        return this.serverTitle;
    }

    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
    }

    public String getServerAbstract() {
        return this.serverAbstract;
    }

    public void setServerAbstract(String serverAbstract) {
        this.serverAbstract = serverAbstract;
    }

    public boolean isTransactional() {
        return this.transactional;
    }

    public BasicAuthentificationData getBasicAuthData() {
        return this.basicAuthData;
    }

    public void setBasicAuthData(BasicAuthentificationData basicAuthData) {
        this.basicAuthData = basicAuthData;
    }

    protected boolean checkTransactionOperation() {
        Object[] operations = this.wfsCapabilities.getOperationsMetadata().getOperations();
        boolean allowEditability = false;
        if (!ArrayUtils.isEmpty((Object[])operations)) {
            int i = 0;
            while (i < operations.length && !allowEditability) {
                allowEditability = TRANSACTION_OPERATION_NAME.equalsIgnoreCase(((org.deegree.ogcwebservices.getcapabilities.Operation)operations[i]).getName());
                ++i;
            }
        }
        return allowEditability;
    }

    public synchronized String[] getFeatureTypes() {
        if (this.featureTypes == null) {
            this.featureTypes = this.extractFeatureTypes();
        }
        return this.featureTypes;
    }

    private synchronized String[] extractFeatureTypes() {
        String[] fts = null;
        WFSFeatureType[] featTypes = this.wfsCapabilities.getFeatureTypeList().getFeatureTypes();
        this.ftNameToWfsFT = new HashMap<String, WFSFeatureType>();
        fts = new String[featTypes.length];
        int i = 0;
        while (i < fts.length) {
            String ftName;
            QualifiedName qn = featTypes[i].getName();
            fts[i] = ftName = qn.getLocalName();
            this.ftNameToWfsFT.put(ftName, featTypes[i]);
            this.ftNameToWfsFT.put(ftName, featTypes[i]);
            ++i;
        }
        return fts;
    }

    protected String createDescribeFTOnlineResource() {
        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = this.wfsCapabilities.getOperationsMetadata().getOperations();
        String descrFtUrl = null;
        int i = 0;
        while (i < ops.length && descrFtUrl == null) {
            if (ops[i].getName().equals("DescribeFeatureType")) {
                DCPType[] dcps = ops[i].getDCPs();
                if (dcps.length > 0) {
                    descrFtUrl = ((HTTP)dcps[0].getProtocol()).getGetOnlineResources()[0].toString();
                }
                if (descrFtUrl == null) {
                    descrFtUrl = ((HTTP)dcps[0].getProtocol()).getPostOnlineResources()[0].toString();
                }
            }
            ++i;
        }
        return descrFtUrl;
    }

    public String getCapabilitesAsString() {
        return this.capsString;
    }

    public String getGetFeatureURL() {
        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = this.wfsCapabilities.getOperationsMetadata().getOperations();
        this.getFeatureUrl = null;
        int i = 0;
        while (i < ops.length && this.getFeatureUrl == null) {
            if (ops[i].getName().equals("GetFeature")) {
                DCPType[] dcps = ops[i].getDCPs();
                int j = 0;
                while (j < dcps.length && this.getFeatureUrl == null) {
                    HTTP http;
                    DCPType currentDCP = dcps[j];
                    if (currentDCP.getProtocol() instanceof HTTP && (http = (HTTP)currentDCP.getProtocol()).getPostOnlineResources().length > 0) {
                        this.getFeatureUrl = http.getPostOnlineResources()[0].toString();
                    }
                    ++j;
                }
            }
            ++i;
        }
        if (this.getFeatureUrl == null) {
            throw new RuntimeException(I18N.getString("de.latlon.deejump.wfs.client.WFServiceWrapper_1_1_0.service-does-not-have-a-getfeature-operation-accesible-by-http-post"));
        }
        return this.getFeatureUrl;
    }

    protected String getAbstract() {
        return this.wfsCapabilities.getServiceIdentification().getAbstract();
    }

    protected String getTitle() {
        return this.wfsCapabilities.getServiceIdentification().getTitle();
    }

    public abstract void initialize() throws Exception;
}

