/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.CalculatorParser
 *  com.eteks.parser.CompilationException
 *  com.eteks.parser.DoubleInterpreter
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 *  com.eteks.parser.JavaSyntax
 *  com.eteks.parser.Syntax
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.feature;

import com.eteks.parser.CalculatorParser;
import com.eteks.parser.CompilationException;
import com.eteks.parser.DoubleInterpreter;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.JavaSyntax;
import com.eteks.parser.Syntax;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.Date;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.saig.core.model.data.mapping.TableMappings;
import org.saig.core.model.feature.Attribute;

public abstract class AbstractBasicFeature
implements Feature {
    protected static final Logger LOGGER = Logger.getLogger(AbstractBasicFeature.class);
    protected static String NULLVALUE = "@NULL";
    private static CalculatorParser parser = new CalculatorParser((Syntax)new JavaSyntax(true));
    private FeatureCollection parent;
    protected FeatureSchema schema;
    public AttributeInterpreter attributeInterpreter = new AttributeInterpreter();
    protected int pkAsInt;
    private int id = FeatureUtil.nextID();

    public AbstractBasicFeature(FeatureSchema featureSchema) {
        this.schema = featureSchema;
    }

    public static CalculatorParser getParser() {
        return parser;
    }

    @Override
    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public void setAttribute(String attributeName, Object newAttribute) {
        this.setAttribute(this.schema.getAttributeIndex(attributeName), newAttribute);
    }

    @Override
    public void setAttributeCorrectType(String attributeName, Object newAttribute) {
        this.setAttributeCorrectType(this.schema.getAttributeIndex(attributeName), newAttribute);
    }

    @Override
    public void setGeometry(Geometry geometry) {
        this.setAttribute(this.schema.getGeometryIndex(), (Object)geometry);
    }

    @Override
    public Object getAttribute(String name) {
        return this.getAttribute(this.schema.getAttributeIndex(name));
    }

    @Override
    public String getString(int attributeIndex) {
        Object result = this.getAttribute(attributeIndex);
        if (result != null) {
            return result.toString();
        }
        return "";
    }

    @Override
    public int getInteger(int attributeIndex) {
        return (Integer)this.getAttribute(attributeIndex);
    }

    @Override
    public double getDouble(int attributeIndex) {
        return (Double)this.getAttribute(attributeIndex);
    }

    @Override
    public String getString(String attributeName) {
        return this.getString(this.schema.getAttributeIndex(attributeName));
    }

    @Override
    public Geometry getGeometry() {
        return (Geometry)this.getAttribute(this.schema.getGeometryIndex());
    }

    @Override
    public FeatureSchema getSchema() {
        return this.schema;
    }

    @Override
    public Object clone() {
        return this.clone(true);
    }

    @Override
    public Feature clone(boolean deep) {
        return this.clone(deep, false);
    }

    @Override
    public Feature clone(boolean deep, boolean generateID) {
        BasicFeature clone = new BasicFeature(this.schema);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getAttributeType(i) == AttributeType.GEOMETRY) {
                if (deep && this.getAttribute(i) != null) {
                    clone.setAttribute(i, ((Geometry)this.getAttribute(i)).clone());
                } else {
                    clone.setAttribute(i, this.getAttribute(i));
                }
            } else {
                clone.setAttribute(i, this.getAttribute(i));
            }
            ++i;
        }
        if (this.isUnsaved() && generateID) {
            clone.setID(FeatureUtil.nextID());
        } else {
            clone.setID(this.getID());
        }
        return clone;
    }

    @Override
    public int compareTo(Feature o) {
        if (this.getGeometry() == null) {
            if (o.getGeometry() != null) {
                return 1;
            }
            return 0;
        }
        return this.getGeometry().compareTo((Object)o.getGeometry());
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public FeatureCollection getParent() {
        return this.parent;
    }

    @Override
    public void setParent(FeatureCollection parent) {
        this.parent = parent;
    }

    public Object getExpression(String expression) {
        Object val = null;
        try {
            val = parser.computeExpression(expression, (Interpreter)this.attributeInterpreter);
        }
        catch (CompilationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return val;
    }

    @Override
    public boolean isUnsaved() {
        Attribute pk = this.schema.getPrimaryKey();
        if (pk == null) {
            return false;
        }
        Object pkValue = this.getAttribute(pk.getName());
        return pkValue == null;
    }

    @Override
    public int getPrimaryKeyAsInt() {
        return this.pkAsInt;
    }

    class AttributeInterpreter
    extends DoubleInterpreter {
        AttributeInterpreter() {
        }

        public Object getLiteralValue(Object literal) {
            String literalString;
            Object val = null;
            val = !(literal instanceof String) ? super.getLiteralValue(literal) : (!(literalString = (String)literal).isEmpty() && literalString.charAt(0) == '@' ? this.extractValue(literalString) : literal);
            return val;
        }

        private Object extractValue(String literalString) {
            Object val;
            if (literalString.indexOf(46) == -1) {
                String attribute = literalString.substring(1, literalString.length());
                val = AbstractBasicFeature.this.getAttribute(attribute);
                if (val == null) {
                    val = "";
                }
                if (val instanceof Date) {
                    val = val.toString();
                }
            } else {
                TableMappings tableMappings = TableMappings.getInstance();
                String command = literalString.substring(1, literalString.length());
                StringTokenizer tok = new StringTokenizer(command, ".");
                String key = null;
                if (tok.hasMoreTokens()) {
                    key = tok.nextToken();
                }
                String field = null;
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                }
                String attribute = null;
                if (tok.hasMoreTokens()) {
                    attribute = tok.nextToken();
                }
                if (key == null || field == null || attribute == null) {
                    return "";
                }
                val = AbstractBasicFeature.this.getAttribute(attribute);
                if (val == null) {
                    return "";
                }
                val = tableMappings.getFieldValue(key, field, val);
            }
            return val;
        }

        public Object getParameterValue(Object parameter) {
            return super.getParameterValue(parameter);
        }

        public Object getConstantValue(Object constant) {
            return super.getConstantValue(constant);
        }

        public Object getUnaryOperatorValue(Object unaryOperatoryKey, Object operand) {
            return super.getUnaryOperatorValue(unaryOperatoryKey, operand);
        }

        public Object getBinaryOperatorValue(Object binaryOperationKey, Object op1, Object op2) {
            Object val = null;
            if (op1 instanceof String || op2 instanceof String) {
                boolean isMinusOperator;
                boolean bl = isMinusOperator = binaryOperationKey instanceof Integer && (Integer)binaryOperationKey == 2001;
                if (isMinusOperator) {
                    if (op1 == null || op1.toString().isEmpty()) {
                        return "";
                    }
                    if (op2 == null || op2.toString().isEmpty()) {
                        return "";
                    }
                }
                val = String.valueOf(op1.toString()) + op2.toString();
            } else {
                val = super.getBinaryOperatorValue(binaryOperationKey, op1, op2);
            }
            return val;
        }

        public Object getCommonFunctionValue(Object commonFunctionKey, Object value) {
            return super.getCommonFunctionValue(commonFunctionKey, value);
        }

        public Object getConditionValue(Object paramIf, Object paramThen, Object paramElse) {
            return super.getConditionValue(paramIf, paramThen, paramElse);
        }

        public boolean isTrue(Object condition) {
            return super.isTrue(condition);
        }

        public boolean supportsRecursiveCall() {
            return super.supportsRecursiveCall();
        }

        public Object getFunctionValue(Function function, Object[] parametersValue, boolean recursive) {
            return super.getFunctionValue(function, parametersValue, recursive);
        }
    }
}

