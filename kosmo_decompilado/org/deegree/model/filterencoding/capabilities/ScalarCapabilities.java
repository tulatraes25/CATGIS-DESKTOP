/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.model.filterencoding.capabilities.Operator
 */
package org.deegree.model.filterencoding.capabilities;

import java.util.HashMap;
import java.util.Map;
import org.deegree.model.filterencoding.capabilities.Operator;

public class ScalarCapabilities {
    private boolean supportsLogicalOperators;
    private Map<String, Operator> comparisonOperators;
    private Map<String, Operator> arithmeticOperators;

    public ScalarCapabilities(boolean supportsLogicalOperators, Operator[] comparisonOperators, Operator[] arithmeticOperators) {
        this.supportsLogicalOperators = supportsLogicalOperators;
        this.setComparisonOperators(comparisonOperators);
        this.setArithmeticOperators(arithmeticOperators);
    }

    public boolean hasLogicalOperatorsSupport() {
        return this.supportsLogicalOperators;
    }

    public void setLogicalOperatorsSupport(boolean supportsLogicalOperators) {
        this.supportsLogicalOperators = supportsLogicalOperators;
    }

    public void setComparisonOperators(Operator[] comparisonOperators) {
        this.comparisonOperators = new HashMap<String, Operator>();
        if (comparisonOperators != null) {
            int i = 0;
            while (i < comparisonOperators.length) {
                this.comparisonOperators.put(comparisonOperators[i].getName(), comparisonOperators[i]);
                ++i;
            }
        }
    }

    public Operator[] getComparisonOperators() {
        return this.comparisonOperators.values().toArray(new Operator[this.comparisonOperators.values().size()]);
    }

    public void setArithmeticOperators(Operator[] arithmeticOperators) {
        this.arithmeticOperators = new HashMap<String, Operator>();
        if (arithmeticOperators != null) {
            int i = 0;
            while (i < arithmeticOperators.length) {
                this.arithmeticOperators.put(arithmeticOperators[i].getName(), arithmeticOperators[i]);
                ++i;
            }
        }
    }

    public Operator[] getArithmeticOperators() {
        return this.arithmeticOperators.values().toArray(new Operator[this.arithmeticOperators.values().size()]);
    }

    public boolean hasComparisonOperator(String operatorName) {
        return this.comparisonOperators.get(operatorName) != null;
    }

    public boolean hasArithmeticOperator(String operatorName) {
        return this.arithmeticOperators.get(operatorName) != null;
    }
}

