/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.DoubleInterpreter
 *  com.eteks.parser.Function
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.eteks.parser.DoubleInterpreter;
import com.eteks.parser.Function;

class DumbInterpreter
extends DoubleInterpreter {
    DumbInterpreter() {
    }

    public Object getLiteralValue(Object literal) {
        return null;
    }

    public Object getParameterValue(Object parameter) {
        return null;
    }

    public Object getConstantValue(Object constant) {
        return null;
    }

    public Object getUnaryOperatorValue(Object unaryOperatoryKey, Object operand) {
        return null;
    }

    public Object getBinaryOperatorValue(Object binaryOperationKey, Object op1, Object op2) {
        return null;
    }

    public Object getCommonFunctionValue(Object commonFunctionKey, Object value) {
        return null;
    }

    public Object getConditionValue(Object paramIf, Object paramThen, Object paramElse) {
        return null;
    }

    public boolean isTrue(Object condition) {
        return false;
    }

    public boolean supportsRecursiveCall() {
        return super.supportsRecursiveCall();
    }

    public Object getFunctionValue(Function function, Object[] parametersValue, boolean recursive) {
        return null;
    }
}

