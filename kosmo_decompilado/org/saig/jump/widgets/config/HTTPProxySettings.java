/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerException;
import org.saig.core.crypt.CryptManagerFactory;

public class HTTPProxySettings {
    private static final Logger LOGGER = Logger.getLogger(HTTPProxySettings.class);
    private String host;
    private Integer port;
    private String userName;
    private String password;
    private String directConnectionTo;
    private CryptManager manager;

    public HTTPProxySettings() {
        try {
            this.manager = CryptManagerFactory.getManager("Password based encryption");
        }
        catch (CryptManagerException e) {
            LOGGER.error((Object)e);
        }
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        if (StringUtils.isEmpty((String)this.password)) {
            return "";
        }
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDirectConnectionTo(String directConnectionTo) {
        this.directConnectionTo = directConnectionTo;
    }

    public String getDirectConnectionTo() {
        return this.directConnectionTo;
    }

    public String getEncryptedPassword() {
        String encryptedPassword = this.password;
        if (this.manager != null) {
            try {
                encryptedPassword = this.manager.encrypt(this.password);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        }
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        try {
            this.password = this.manager.decrypt(encryptedPassword);
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
        }
    }
}

