/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.crypt;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.log4j.Logger;
import org.saig.core.crypt.CryptManager;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class PBEManager
extends CryptManager {
    public static final Logger LOGGER = Logger.getLogger(PBEManager.class);
    private static final byte[] SALT = new byte[]{-57, 115, 33, -116, 126, -56, -18, -103};
    private static final int COUNT = 20;
    private static final char[] PASSWD_KEY = new char[]{'k', 'o', 's', 'm', 'o'};
    private PBEKeySpec pbeKeySpec;
    private PBEParameterSpec pbeParamSpec;
    private SecretKeyFactory keyFac;
    private SecretKey pbeKey;
    private Cipher pbeCipher;

    @Override
    public void initialize() {
        this.pbeParamSpec = new PBEParameterSpec(SALT, 20);
        this.pbeKeySpec = new PBEKeySpec(PASSWD_KEY);
        try {
            this.keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            this.pbeKey = this.keyFac.generateSecret(this.pbeKeySpec);
            this.pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        }
        catch (Exception e) {
            LOGGER.error((Object)("Error inicializando PBEManager: " + e.getMessage()));
        }
    }

    @Override
    public String encrypt(String str) throws Exception {
        this.pbeCipher.init(1, (Key)this.pbeKey, this.pbeParamSpec);
        byte[] raw = this.pbeCipher.doFinal(str.getBytes());
        String hash = new BASE64Encoder().encode(raw);
        return hash;
    }

    @Override
    public String decrypt(String str) throws Exception {
        this.pbeCipher.init(2, (Key)this.pbeKey, this.pbeParamSpec);
        byte[] decodedStr = new BASE64Decoder().decodeBuffer(str);
        byte[] raw = this.pbeCipher.doFinal(decodedStr);
        String decrypted = new String(raw, "UTF-8");
        return decrypted;
    }
}

