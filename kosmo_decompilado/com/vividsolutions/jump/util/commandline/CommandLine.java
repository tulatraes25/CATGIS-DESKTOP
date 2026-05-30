/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util.commandline;

import com.vividsolutions.jump.util.commandline.Option;
import com.vividsolutions.jump.util.commandline.OptionSpec;
import com.vividsolutions.jump.util.commandline.ParseException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.saig.jump.lang.I18N;

public class CommandLine {
    protected Hashtable<String, OptionSpec> optSpecs = new Hashtable();
    protected Vector<OptionSpec> optVec = new Vector();
    protected char optionChar;

    public CommandLine() {
        this('/');
    }

    public CommandLine(char optionCh) {
        this.optionChar = optionCh;
    }

    public void addOptionSpec(OptionSpec optSpec) {
        String name = optSpec.getName();
        this.optSpecs.put(name.toLowerCase(), optSpec);
        this.optVec.add(optSpec);
    }

    public OptionSpec getOptionSpec(String name) {
        if (this.optSpecs.containsKey(name.toLowerCase())) {
            return this.optSpecs.get(name.toLowerCase());
        }
        return null;
    }

    public Option getOption(String name) {
        OptionSpec spec = this.getOptionSpec(name);
        if (spec == null) {
            return null;
        }
        return spec.getOption(0);
    }

    public Iterator<Option> getOptions(String name) {
        OptionSpec spec = this.getOptionSpec(name);
        return spec.getOptions();
    }

    public boolean hasOption(String name) {
        OptionSpec spec = this.getOptionSpec(name);
        if (spec == null) {
            return false;
        }
        return spec.hasOption();
    }

    public void addOption(Option opt) {
        String name = opt.getName();
        this.optSpecs.get(name.toLowerCase()).addOption(opt);
    }

    public void printDoc(PrintStream out) {
        OptionSpec os2 = null;
        out.println(String.valueOf(I18N.getString("com.vividsolutions.jump.util.commandline.CommandLine.options")) + ":");
        for (OptionSpec os2 : this.optVec) {
            String name = String.valueOf(this.optionChar) + os2.getName();
            if (os2.getName() == "**FREE_ARGS**") {
                name = "(" + I18N.getString("com.vividsolutions.jump.util.commandline.CommandLine.free") + ")";
            }
            out.println("  " + name + " " + os2.getArgDesc() + " - " + os2.getDocDesc());
        }
    }

    public void parse(String[] args) throws ParseException {
        Vector<String> params = new Vector<String>();
        int i = 0;
        while (i < args.length) {
            int paramStart;
            String noOptMsg;
            String optName;
            if (args[i].charAt(0) == this.optionChar) {
                optName = args[i].substring(1);
                noOptMsg = String.valueOf(I18N.getString("com.vividsolutions.jump.util.commandline.CommandLine.invalid-option")) + ": " + args[i];
                paramStart = i + 1;
            } else {
                optName = "**FREE_ARGS**";
                noOptMsg = String.valueOf(I18N.getString("com.vividsolutions.jump.util.commandline.CommandLine.invalid-option")) + ": " + args[i];
                paramStart = i;
            }
            OptionSpec optSpec = this.getOptionSpec(optName);
            if (optSpec == null) {
                throw new ParseException(noOptMsg);
            }
            int expectedArgCount = optSpec.getAllowedArgs();
            this.parseParams(args, params, paramStart, expectedArgCount);
            Option opt = optSpec.parse(params.toArray(new String[0]));
            this.addOption(opt);
            ++i;
            i += params.size();
        }
    }

    protected void parseParams(String[] args, Vector<String> params, int i, int expectedArgCount) {
        params.clear();
        int count = 0;
        int expected = expectedArgCount;
        if (expectedArgCount == -3) {
            expected = 1;
        }
        if (expectedArgCount == -1) {
            expected = 999999999;
        }
        if (expectedArgCount == -2) {
            expected = 999999999;
        }
        while (i < args.length && count < expected && args[i].charAt(0) != this.optionChar) {
            params.addElement(args[i++]);
            ++count;
        }
    }
}

