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

public class IndexOfFunction
implements Function {
    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        String str = parametersValue[0] != null ? parametersValue[0].toString() : "";
        String searchStr = parametersValue[1] != null ? parametersValue[1].toString() : "";
        int startPos = 0;
        if (parametersValue.length == 3) {
            if (parametersValue[2] instanceof Number) {
                startPos = ((Number)parametersValue[2]).intValue();
            } else {
                throw new IllegalArgumentException();
            }
        }
        return StringUtils.indexOf((String)str, (String)searchStr, (int)startPos);
    }

    public String getName() {
        return "INDEXOF";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount >= 2 && parameterCount <= 3;
    }
}

