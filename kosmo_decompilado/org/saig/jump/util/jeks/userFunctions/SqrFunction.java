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

public class SqrFunction
implements Function {
    public Object computeFunction(Interpreter interpreter, Object[] parametersValue) {
        double num = 0.0;
        if (!(parametersValue[0] instanceof Number)) {
            throw new IllegalArgumentException();
        }
        num = ((Number)parametersValue[0]).doubleValue();
        return Math.pow(num, 2.0);
    }

    public String getName() {
        return "SQR";
    }

    public boolean isValidParameterCount(int parameterCount) {
        return parameterCount == 1;
    }
}

