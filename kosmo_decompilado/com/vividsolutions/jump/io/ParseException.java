/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.JUMPException;

public class ParseException
extends JUMPException {
    private static final long serialVersionUID = 1L;
    public String fname;
    public int lineno;
    public int cpos;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, String newFname, int newLineno, int newCpos) {
        super(String.valueOf(message) + " in file '" + newFname + "', line " + newLineno + ", char " + newCpos);
        this.fname = newFname;
        this.lineno = newLineno;
        this.cpos = newCpos;
    }
}

