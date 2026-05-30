/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.simbology;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class SymbologyFileFilter
implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return Pattern.matches(".*\\.(sls)", pathname.getAbsolutePath());
    }
}

