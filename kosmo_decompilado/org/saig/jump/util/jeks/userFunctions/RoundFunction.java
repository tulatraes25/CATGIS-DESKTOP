/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 *  org.apache.commons.math.util.MathUtils
 */
package org.saig.jump.util.jeks.userFunctions;

import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import org.apache.commons.math.util.MathUtils;

public class RoundFunction
implements Function {
    private static final long serialVersionUID = 1L;

    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        double num = 0.0;
        int numberOfDecimals = 0;
        if (!(parametersValue[0] instanceof Number)) {
            throw new IllegalArgumentException();
        }
        num = ((Number)parametersValue[0]).doubleValue();
        if (!(parametersValue[1] instanceof Number)) {
            throw new IllegalArgumentException();
        }
        numberOfDecimals = ((Number)parametersValue[1]).intValue();
        return MathUtils.round((double)num, (int)numberOfDecimals);
    }

    public String getName() {
        return "ROUND";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount == 2;
    }
}

