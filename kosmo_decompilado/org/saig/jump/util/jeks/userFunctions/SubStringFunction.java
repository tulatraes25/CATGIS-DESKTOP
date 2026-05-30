/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.util.jeks.userFunctions;

import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import org.apache.commons.lang.StringUtils;

public class SubStringFunction
implements Function {
    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        String str = parametersValue[0] != null ? parametersValue[0].toString() : "";
        int start = 0;
        int end = str.length();
        if (!(parametersValue[1] instanceof Number)) {
            throw new IllegalArgumentException();
        }
        start = ((Number)parametersValue[1]).intValue();
        if (parametersValue.length == 3) {
            if (parametersValue[2] instanceof Number) {
                end = ((Number)parametersValue[2]).intValue();
            } else {
                throw new IllegalArgumentException();
            }
        }
        return StringUtils.substring((String)str, (int)start, (int)end);
    }

    public String getName() {
        return "SUBSTRING";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount >= 2 && parameterCount <= 3;
    }
}

