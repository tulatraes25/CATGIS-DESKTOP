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

public class ConcatFunction
implements Function {
    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        String outStr = "";
        int i = 0;
        while (i < parametersValue.length) {
            Object paramTemp = parametersValue[i];
            if (paramTemp != null) {
                outStr = String.valueOf(outStr) + paramTemp.toString();
            }
            ++i;
        }
        return outStr;
    }

    public String getName() {
        return "CONCAT";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount >= 2;
    }
}

