/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util.commandline;

import com.vividsolutions.jump.util.commandline.OptionSpec;

public class Option {
    protected OptionSpec optSpec;
    protected String[] args;

    public Option(OptionSpec spec, String[] _args) {
        this.optSpec = spec;
        this.args = _args;
    }

    public String getName() {
        return this.optSpec.getName();
    }

    public int getNumArgs() {
        return this.args.length;
    }

    public String getArg(int i) {
        return this.args[i];
    }
}

