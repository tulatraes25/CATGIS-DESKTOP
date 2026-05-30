/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.httpclient.Credentials
 *  org.apache.commons.httpclient.HttpClient
 *  org.apache.commons.httpclient.HttpMethod
 *  org.apache.commons.httpclient.UsernamePasswordCredentials
 *  org.apache.commons.httpclient.auth.AuthScope
 *  org.apache.commons.httpclient.methods.GetMethod
 *  org.apache.commons.httpclient.params.HttpClientParams
 *  org.apache.commons.lang.StringUtils
 *  org.apache.commons.lang.text.StrTokenizer
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.wms;

import com.vividsolutions.wms.Capabilities;
import com.vividsolutions.wms.FeatureInfoRequest;
import com.vividsolutions.wms.MapRequest;
import com.vividsolutions.wms.Parser;
import es.kosmo.core.crs.CrsAxisOrder;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class WMService {
    public static final String WMS_1_0_0 = "1.0.0";
    public static final String WMS_1_1_0 = "1.1.0";
    public static final String WMS_1_1_1 = "1.1.1";
    public static final String WMS_1_3_X = "1.3";
    public static final Logger LOGGER = Logger.getLogger(WMService.class);
    public static final String VND_OGC_SE_XML_FORMAT = "application/vnd.ogc.se_xml";
    public static final String XML_FORMAT = "XML";
    private boolean initialized = false;
    private String serverUrl;
    private String wmsVersion;
    private Capabilities cap;
    private String title;
    private String imageFormat;
    private boolean transparent;
    private String informationFormat;
    private int informationFeatureCount = 1;
    private String exceptionFormat;
    private boolean useDeclaredCapabilitiesURLs = true;
    private String vendorParameters;
    private CrsAxisOrder axisOrder;
    protected BasicAuthentificationData basicAuthData;
    private static final int WMS_CONNECTION_TIMEOUT = 30000;
    private HttpClient httpClient;

    public WMService(String serverUrl) {
        this.serverUrl = serverUrl;
        this.cap = null;
        this.createHttpClient();
    }

    public void initialize() throws Exception {
        try {
            String req = "SERVICE=WMS&REQUEST=GetCapabilities";
            String requestUrlString = String.valueOf(this.serverUrl) + req;
            if (!StringUtils.isEmpty((String)this.getVersion())) {
                requestUrlString = String.valueOf(requestUrlString) + "&VERSION=" + this.getVersion();
            }
            LOGGER.debug((Object)requestUrlString);
            URL requestUrl = new URL(requestUrlString);
            GetMethod httpget = new GetMethod(requestUrlString);
            if (this.basicAuthData != null) {
                LOGGER.debug((Object)"Setting WMS Basic Auth credentials from user options");
                UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(this.basicAuthData.getUserName(), this.basicAuthData.getPassword());
                this.httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
            } else if (requestUrl.getUserInfo() != null) {
                String userInfo = requestUrl.getUserInfo();
                StrTokenizer tokenizer = new StrTokenizer(userInfo, ":");
                String[] tokens = tokenizer.getTokenArray();
                LOGGER.debug((Object)"Setting WMS Basic Auth credentials from url");
                this.basicAuthData = new BasicAuthentificationData(tokens[0], tokens[1]);
                UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(this.basicAuthData.getUserName(), this.basicAuthData.getPassword());
                this.httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
            } else {
                LOGGER.debug((Object)"No authentification set");
            }
            int code = this.httpClient.executeMethod((HttpMethod)httpget);
            if (code != 200) {
                throw new Exception(I18N.getMessage("com.vividsolutions.wms.WMService.wms-server-{0}-responded-incorrectly-code-{1}", new Object[]{requestUrlString, code}));
            }
            Parser p = new Parser();
            this.cap = p.parseCapabilities(this, httpget.getResponseBodyAsStream(), httpget.getResponseCharSet());
            this.initialized = true;
        }
        catch (MalformedURLException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public Capabilities getCapabilities() {
        return this.cap;
    }

    public MapRequest createMapRequest() {
        MapRequest mr = new MapRequest(this);
        mr.setVersion(this.wmsVersion);
        mr.setFormat(this.imageFormat);
        mr.setTransparent(this.transparent);
        return mr;
    }

    public FeatureInfoRequest createFeatureInfoRequest() {
        FeatureInfoRequest ir = new FeatureInfoRequest(this);
        ir.setVersion(this.wmsVersion);
        ir.setFormat(this.informationFormat);
        ir.setImgFormat(this.imageFormat);
        return ir;
    }

    public String getVersion() {
        return this.wmsVersion;
    }

    public void setWmsVersion(String wmsVersion) {
        this.wmsVersion = wmsVersion;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTransparent(boolean transp) {
        this.transparent = transp;
    }

    public void setFormat(String format) {
        this.imageFormat = format;
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public String getFormat() {
        return this.imageFormat;
    }

    public void setInformationFormat(String infoFormat) {
        this.informationFormat = infoFormat;
    }

    public String getExceptionFormat() {
        return this.exceptionFormat;
    }

    public void setExceptionFormat(String exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
    }

    public String getInformationFormat() {
        return this.informationFormat;
    }

    public String getGetMapUrl() {
        if (this.useDeclaredCapabilitiesURLs) {
            return this.cap.getMapUrl() != null ? this.cap.getMapUrl() : this.serverUrl;
        }
        return this.serverUrl;
    }

    public String getGetFeatureInfoUrl() {
        if (this.useDeclaredCapabilitiesURLs) {
            return this.cap.getFeatureInfoUrl() != null ? this.cap.getFeatureInfoUrl() : this.serverUrl;
        }
        return this.serverUrl;
    }

    public void setUseDeclaredCapabilitiesURLs(boolean useURLs) {
        this.useDeclaredCapabilitiesURLs = useURLs;
    }

    public boolean isUseDeclaredCapabilitiesURLs() {
        return this.useDeclaredCapabilitiesURLs;
    }

    public String getVendorParameters() {
        return this.vendorParameters;
    }

    public void setVendorParameters(String vendorParameters) {
        this.vendorParameters = vendorParameters;
    }

    private void createHttpClient() {
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
        clientPars.setConnectionManagerTimeout(30000L);
        this.httpClient.setParams(clientPars);
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public CrsAxisOrder getAxisOrder() {
        return this.axisOrder;
    }

    public void setAxisOrder(CrsAxisOrder axisOrder) {
        this.axisOrder = axisOrder;
    }

    public int getInformationFeatureCount() {
        return this.informationFeatureCount;
    }

    public void setInformationFeatureCount(int informationFeatureCount) {
        this.informationFeatureCount = informationFeatureCount;
    }

    public BasicAuthentificationData getBasicAuthData() {
        return this.basicAuthData;
    }

    public void setBasicAuthData(BasicAuthentificationData basicAuthData) {
        this.basicAuthData = basicAuthData;
    }
}

