/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 */
package org.saig.jump.util.jeks.userFunctions;

import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;

public class LengthFunction
implements Function {
    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        String str = parametersValue[0] != null ? parametersValue[0].toString() : "";
        return str.length();
    }

    public String getName() {
        return "LENGTH";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount == 1;
    }
}

