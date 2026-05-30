/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.httpclient.Credentials
 *  org.apache.commons.httpclient.HttpClient
 *  org.apache.commons.httpclient.HttpException
 *  org.apache.commons.httpclient.HttpMethod
 *  org.apache.commons.httpclient.UsernamePasswordCredentials
 *  org.apache.commons.httpclient.auth.AuthScope
 *  org.apache.commons.httpclient.methods.PostMethod
 *  org.apache.commons.httpclient.methods.RequestEntity
 *  org.apache.commons.httpclient.methods.StringRequestEntity
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package de.latlon.deejump.wfs.client;

import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class WFSClientHelper {
    private static final Logger LOGGER = Logger.getLogger(WFSClientHelper.class);

    public static String createResponsefromWFS(String serverUrl, String request, BasicAuthentificationData basicAuthData) throws Exception {
        String mesg;
        HttpClient httpClient = new HttpClient();
        Properties systemSettings = System.getProperties();
        if (systemSettings != null) {
            String proxySet = systemSettings.getProperty("http.proxySet", "false");
            if (StringUtils.isNotEmpty((String)proxySet) && proxySet.equals("true")) {
                String proxyHost = systemSettings.getProperty("http.proxyHost");
                String proxyPort = systemSettings.getProperty("http.proxyPort");
                httpClient.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort).intValue());
                String proxyUserName = systemSettings.getProperty("http.proxyUserName");
                String proxyPassword = systemSettings.getProperty("http.proxyPassword");
                if (StringUtils.isNotEmpty((String)proxyUserName)) {
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUserName, proxyPassword);
                    AuthScope scope = new AuthScope(AuthScope.ANY_HOST, -1);
                    httpClient.getState().setProxyCredentials(scope, (Credentials)credentials);
                }
                String nonProxyHosts = systemSettings.getProperty("http.nonProxyHosts");
                LOGGER.info((Object)("Proxy settings: host='" + proxyHost + "' port='" + proxyPort + "' " + " user='" + proxyUserName + "' password='" + proxyPassword + "'"));
                LOGGER.info((Object)("Non proxy hosts=" + nonProxyHosts));
            } else {
                LOGGER.info((Object)"No proxy configuration set");
            }
        }
        PostMethod httppost = new PostMethod(serverUrl);
        String requestedCharset = "UTF-8";
        httppost.setRequestEntity((RequestEntity)new StringRequestEntity(request, "text/xml", requestedCharset));
        httppost.setRequestHeader("Content-Type", "text/xml");
        if (basicAuthData != null) {
            LOGGER.info((Object)"Setting WFS Basic Auth credentials from user options");
            UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(basicAuthData.getUserName(), basicAuthData.getPassword());
            httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
        } else {
            LOGGER.info((Object)"No authentification set");
        }
        try {
            httpClient.executeMethod((HttpMethod)httppost);
            WFSClientHelper.validateResponse(httppost);
            String responseCharSet = httppost.getResponseCharSet();
            responseCharSet.equalsIgnoreCase(requestedCharset);
            return WFSClientHelper.convertStreamToString(httppost.getResponseBodyAsStream(), requestedCharset);
        }
        catch (HttpException e) {
            mesg = String.valueOf(I18N.getString("de.latlon.deejump.wfs.client.WFSClientHelper.error-opening-connection-with")) + ": " + serverUrl + "\n" + e.getLocalizedMessage();
            throw new Exception(mesg, e);
        }
        catch (IOException e) {
            mesg = String.valueOf(I18N.getString("de.latlon.deejump.wfs.client.WFSClientHelper.error-opening-connection-with")) + ": " + serverUrl + "\n" + e.getLocalizedMessage();
            throw new Exception(mesg, e);
        }
    }

    private static String convertStreamToString(InputStream is, String charsetName) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charsetName));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(String.valueOf(line) + "\n");
        }
        is.close();
        return sb.toString();
    }

    protected static void validateResponse(PostMethod postMethod) throws IOException {
        if (postMethod.getStatusCode() >= 300) {
            throw new HttpException("Did not receive successful HTTP response: status code = " + postMethod.getStatusCode() + ", status message = [" + postMethod.getStatusText() + "]");
        }
    }
}

