/*
 * Decompiled with CFR 0.152.
 */
package com.iver.cit.gvsig.fmap.core.styles;

import com.iver.cit.gvsig.fmap.core.styles.LineEquation;

class ParallelLinesCannotBeResolvedException
extends Exception {
    private static final long serialVersionUID = 8322556508820067641L;

    public ParallelLinesCannotBeResolvedException(LineEquation eq1, LineEquation eq2) {
        super("Lines '" + eq1 + "' and '" + eq2 + "' are parallel and don't share any point!");
    }
}

