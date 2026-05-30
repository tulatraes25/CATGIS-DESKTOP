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
import java.math.BigDecimal;

public class TruncFunction
implements Function {
    private static final long serialVersionUID = 1L;

    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        double num = 0.0;
        int numberOfDecimals = 0;
        int roundingMode = 1;
        if (!(parametersValue[0] instanceof Number)) {
            throw new IllegalArgumentException();
        }
        num = ((Number)parametersValue[0]).doubleValue();
        if (parametersValue[1] instanceof Number) {
            numberOfDecimals = ((Number)parametersValue[1]).intValue();
            if (num < 0.0) {
                roundingMode = 0;
            }
        } else {
            throw new IllegalArgumentException();
        }
        return new BigDecimal(num).setScale(numberOfDecimals, roundingMode).doubleValue();
    }

    public String getName() {
        return "TRUNC";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount == 2;
    }
}

