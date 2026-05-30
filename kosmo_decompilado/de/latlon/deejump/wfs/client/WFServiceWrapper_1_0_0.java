/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.httpclient.Credentials
 *  org.apache.commons.httpclient.HttpMethod
 *  org.apache.commons.httpclient.UsernamePasswordCredentials
 *  org.apache.commons.httpclient.auth.AuthScope
 *  org.apache.commons.httpclient.methods.GetMethod
 *  org.apache.commons.lang.text.StrTokenizer
 *  org.apache.log4j.Logger
 *  org.deegree.framework.xml.DOMPrinter
 *  org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException
 */
package de.latlon.deejump.wfs.client;

import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.net.URL;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.saig.jump.lang.I18N;
import org.w3c.dom.Node;

public class WFServiceWrapper_1_0_0
extends AbstractWFSWrapper {
    private static final Logger LOGGER = Logger.getLogger(WFServiceWrapper_1_0_0.class);

    public WFServiceWrapper_1_0_0(BasicAuthentificationData logins, String baseUrl) {
        super(logins, baseUrl);
    }

    @Override
    public void initialize() throws Exception {
        URL capsURL = new URL(this.getCapabilitiesURL());
        WFSCapabilitiesDocument wfsCapsDoc = new WFSCapabilitiesDocument();
        GetMethod httpget = new GetMethod(this.getCapabilitiesURL());
        if (this.basicAuthData != null) {
            LOGGER.info((Object)"Setting WFS Basic Auth credentials from user options");
            UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(this.basicAuthData.getUserName(), this.basicAuthData.getPassword());
            this.httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
        } else if (capsURL.getUserInfo() != null) {
            String userInfo = capsURL.getUserInfo();
            StrTokenizer tokenizer = new StrTokenizer(userInfo, ":");
            String[] tokens = tokenizer.getTokenArray();
            LOGGER.info((Object)"Setting WFS Basic Auth credentials from URL");
            this.basicAuthData = new BasicAuthentificationData(tokens[0], tokens[1]);
            UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(this.basicAuthData.getUserName(), this.basicAuthData.getPassword());
            this.httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
        } else {
            LOGGER.info((Object)"No authentification set");
        }
        int code = this.httpClient.executeMethod((HttpMethod)httpget);
        if (code != 200) {
            this.initialized = false;
            throw new Exception(I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.wfs-server-{0}-responded-incorrectly-code-{1}", new Object[]{capsURL.toString(), code}));
        }
        LOGGER.info((Object)(String.valueOf(I18N.getString("de.latlon.deejump.wfs.client.WFServiceWrapper_1_0_0.loading-wfs-server-capabilities")) + " - " + capsURL));
        wfsCapsDoc.load(httpget.getResponseBodyAsStream(), "http://www.deegree.org");
        this.capsString = DOMPrinter.nodeToString((Node)wfsCapsDoc.getRootElement(), (String)"");
        try {
            this.wfsCapabilities = (WFSCapabilities)wfsCapsDoc.parseCapabilities();
        }
        catch (InvalidCapabilitiesException e) {
            LOGGER.error((Object)I18N.getString("de.latlon.deejump.wfs.client.WFServiceWrapper_1_1_0.could-not-initialize-wfs-capabilities"), (Throwable)e);
            throw new Exception(e);
        }
        this.serverTitle = this.getTitle();
        this.serverAbstract = this.getAbstract();
        this.transactional = this.checkTransactionOperation();
        this.globalFeatureTypeOperations = this.wfsCapabilities.getFeatureTypeList().getGlobalOperations();
        this.initialized = true;
    }

    @Override
    public String getServiceVersion() {
        return "1.0.0";
    }
}

