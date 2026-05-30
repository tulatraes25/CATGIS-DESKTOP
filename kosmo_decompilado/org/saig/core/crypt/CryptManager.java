/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.crypt;

import org.saig.core.crypt.CryptManagerFactory;

public abstract class CryptManager {
    public abstract void initialize();

    public abstract String encrypt(String var1) throws Exception;

    public abstract String decrypt(String var1) throws Exception;

    public static void main(String[] args) {
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            String toEncrypt = args[0];
            String toEncrypt2 = "";
            String encrypted = manager.encrypt(toEncrypt);
            String encrypted2 = manager.encrypt(toEncrypt2);
            System.out.println("Se ha encriptado la cadena " + toEncrypt + " y se recuperado la cadena <" + encrypted + ">");
            System.out.println("Se ha encriptado la cadena " + toEncrypt2 + " y se recuperado la cadena <" + encrypted2 + ">");
            String decrypted2 = manager.decrypt(encrypted2);
            String decrypted3 = manager.decrypt(encrypted);
            System.out.println("Se ha recuperado la cadena <" + decrypted2 + ">");
            System.out.println("Se ha desencriptado la cadena " + encrypted + " de <" + decrypted3 + ">");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

