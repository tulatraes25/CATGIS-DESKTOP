/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.model.sdi;

public class BasicAuthentificationData {
    protected String userName;
    protected String password;

    public BasicAuthentificationData(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

