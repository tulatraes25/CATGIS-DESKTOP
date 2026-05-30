/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.crypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.saig.core.crypt.CryptManager;

public class BlowFishManager
extends CryptManager {
    public static final Logger LOGGER = Logger.getLogger(BlowFishManager.class);
    private SecretKeySpec skeySpec;
    private SecretKey blowFishKey;
    private Cipher blowFishCipher;

    @Override
    public void initialize() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("Blowfish");
            this.blowFishKey = kgen.generateKey();
            byte[] raw = this.blowFishKey.getEncoded();
            this.skeySpec = new SecretKeySpec(raw, "Blowfish");
            this.blowFishCipher = Cipher.getInstance("Blowfish");
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public String encrypt(String str) throws Exception {
        this.blowFishCipher.init(1, this.skeySpec);
        byte[] encrypted = this.blowFishCipher.doFinal(str.getBytes("UTF-8"));
        return new String(encrypted);
    }

    @Override
    public String decrypt(String str) throws Exception {
        this.blowFishCipher.init(2, this.skeySpec);
        byte[] decrypted = this.blowFishCipher.doFinal(str.getBytes("UTF-8"));
        return new String(decrypted);
    }
}

