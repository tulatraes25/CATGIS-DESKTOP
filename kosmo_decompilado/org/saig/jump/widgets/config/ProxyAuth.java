/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyAuth
extends Authenticator {
    private PasswordAuthentication auth;

    public ProxyAuth(String user, String pass) {
        this.auth = new PasswordAuthentication(user, pass.toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.auth;
    }
}

