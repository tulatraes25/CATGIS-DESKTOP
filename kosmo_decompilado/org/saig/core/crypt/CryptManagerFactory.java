/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.crypt;

import org.saig.core.crypt.BlowFishManager;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerException;
import org.saig.core.crypt.PBEManager;
import org.saig.jump.lang.I18N;

public class CryptManagerFactory {
    public static final String PASSWORD_BASED_ENCRYPTION = "Password based encryption";
    public static final String BLOWFISH_ENCRYPTION = "BlowFish";

    public static CryptManager getManager(String algorithm) throws CryptManagerException {
        CryptManager manager = null;
        if (algorithm.equalsIgnoreCase(PASSWORD_BASED_ENCRYPTION)) {
            manager = new PBEManager();
        } else if (algorithm.equalsIgnoreCase(BLOWFISH_ENCRYPTION)) {
            manager = new BlowFishManager();
        } else {
            throw new CryptManagerException(I18N.getMessage("org.saig.core.crypt.CryptManagerFactory.a-{0}-algorithm-manager-can-not-be-found", new Object[]{algorithm}));
        }
        ((CryptManager)manager).initialize();
        return manager;
    }
}

