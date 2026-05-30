/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util.commandline;

import com.vividsolutions.jump.util.commandline.Option;
import com.vividsolutions.jump.util.commandline.ParseException;
import java.util.Iterator;
import java.util.Vector;
import org.saig.jump.lang.I18N;

public class OptionSpec {
    public static final int NARGS_ZERO_OR_MORE = -1;
    public static final int NARGS_ONE_OR_MORE = -2;
    public static final int NARGS_ZERO_OR_ONE = -3;
    public static final String OPTION_FREE_ARGS = "**FREE_ARGS**";
    protected String name;
    protected int nAllowedArgs = 0;
    protected String syntaxPattern;
    protected String argDoc = "";
    protected String doc = "";
    protected Vector<Option> options = new Vector();

    public OptionSpec(String optName) {
        this.name = optName;
        this.nAllowedArgs = 0;
    }

    public OptionSpec(String optName, int nAllowed) {
        this(optName);
        if (this.nAllowedArgs >= -3) {
            this.nAllowedArgs = nAllowed;
        }
    }

    public OptionSpec(String optName, String _syntaxPattern) {
        this(optName);
        this.syntaxPattern = _syntaxPattern;
    }

    public void setDoc(String _argDoc, String docLine) {
        this.argDoc = _argDoc;
        this.doc = docLine;
    }

    public String getArgDesc() {
        return this.argDoc;
    }

    public String getDocDesc() {
        return this.doc;
    }

    public int getNumOptions() {
        return this.options.size();
    }

    public Option getOption(int i) {
        if (this.options.size() > 0) {
            return this.options.elementAt(i);
        }
        return null;
    }

    public Iterator<Option> getOptions() {
        return this.options.iterator();
    }

    public boolean hasOption() {
        return this.options.size() > 0;
    }

    void addOption(Option opt) {
        this.options.addElement(opt);
    }

    String getName() {
        return this.name;
    }

    int getAllowedArgs() {
        return this.nAllowedArgs;
    }

    Option parse(String[] args) throws ParseException {
        this.checkNumArgs(args);
        return new Option(this, args);
    }

    void checkNumArgs(String[] args) throws ParseException {
        if (this.nAllowedArgs != -1) {
            if (this.nAllowedArgs == -2) {
                if (args.length <= 0) {
                    throw new ParseException(I18N.getMessage("com.vividsolutions.jump.util.commandline.OptionSpec.option-{0}-expected-one-or-more-args-found-{1}", new Object[]{this.name, new Integer(args.length)}));
                }
            } else if (this.nAllowedArgs == -3) {
                if (args.length > 1) {
                    throw new ParseException(I18N.getMessage("com.vividsolutions.jump.util.commandline.OptionSpec.option-{0}-expected-zero-or-more-args-found-{1}", new Object[]{this.name, new Integer(args.length)}));
                }
            } else if (args.length != this.nAllowedArgs) {
                throw new ParseException(I18N.getMessage("com.vividsolutions.jump.util.commandline.OptionSpec.option-{0}-expected-{1}-args-found-{2}", new Object[]{this.name, new Integer(this.nAllowedArgs), new Integer(args.length)}));
            }
        }
    }
}

