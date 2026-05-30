/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.GeneralizedFieldHandler
 */
package org.saig.jump.util.handlers;

import com.vividsolutions.jump.util.StringUtil;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.GeneralizedFieldHandler;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerFactory;

public class BasicAuthentificationDataHandler
extends GeneralizedFieldHandler {
    private static final Logger LOGGER = Logger.getLogger(BasicAuthentificationDataHandler.class);

    public Object convertUponGet(Object arg) {
        BasicAuthentificationData basicAuthData = (BasicAuthentificationData)arg;
        if (basicAuthData != null) {
            ArrayList<Object> parameters = new ArrayList<Object>();
            parameters.add(basicAuthData.getUserName());
            parameters.add(this.getEncryptedPassword(basicAuthData.getPassword()));
            return StringUtil.toPercentDelimitedString(parameters);
        }
        return null;
    }

    public Object convertUponSet(Object arg) {
        List<String> parameters = StringUtil.fromPercentDelimitedString((String)arg);
        return new BasicAuthentificationData(parameters.get(0), this.getDecryptedPassword(parameters.get(1)));
    }

    public Class<?> getFieldType() {
        return BasicAuthentificationData.class;
    }

    public String getEncryptedPassword(String password) {
        String encryptedPassword = "";
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            encryptedPassword = manager.encrypt(password);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return encryptedPassword;
    }

    public String getDecryptedPassword(String encryptedPassword) {
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
}

