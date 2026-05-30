/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.MathExpression;
import org.xml.sax.Attributes;

public class ExpressionSAXParser {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final FilterFactory FILTER_FACT = FilterFactory.createFilterFactory();
    private ExpressionSAXParser expFactory = null;
    private Expression curExprssn = null;
    private String currentState = null;
    private List<Expression> accumalationOfExpressions = new ArrayList<Expression>();
    private String declaredType = null;
    private boolean readyFlag = false;
    private FeatureSchema schema;
    private boolean readChars = false;

    public ExpressionSAXParser(FeatureSchema schema) {
        this.schema = schema;
    }

    public void start(String declaredType, Attributes atts) throws IllegalFilterException {
        LOGGER.finer("incoming type: " + declaredType);
        LOGGER.finer("declared type: " + this.declaredType);
        LOGGER.finer("current state: " + this.currentState);
        if (this.expFactory == null) {
            this.declaredType = declaredType;
            if (DefaultExpression.isFunctionExpression(ExpressionSAXParser.convertType(declaredType))) {
                this.expFactory = new ExpressionSAXParser(this.schema);
                this.curExprssn = FILTER_FACT.createFunctionExpression(this.getFunctionName(atts));
                LOGGER.finer("is <function> expression");
            }
            if (DefaultExpression.isMathExpression(ExpressionSAXParser.convertType(declaredType))) {
                this.expFactory = new ExpressionSAXParser(this.schema);
                this.curExprssn = FILTER_FACT.createMathExpression(ExpressionSAXParser.convertType(declaredType));
                LOGGER.finer("is math expression");
            } else if (DefaultExpression.isLiteralExpression(ExpressionSAXParser.convertType(declaredType))) {
                this.curExprssn = FILTER_FACT.createLiteralExpression();
                this.readChars = true;
                LOGGER.finer("is literal expression");
            } else if (DefaultExpression.isAttributeExpression(ExpressionSAXParser.convertType(declaredType))) {
                this.curExprssn = FILTER_FACT.createAttributeExpression(this.schema);
                this.readChars = true;
                LOGGER.finer("is attribute expression");
            }
            this.currentState = ExpressionSAXParser.setInitialState(this.curExprssn);
            this.readyFlag = false;
        } else {
            this.expFactory.start(declaredType, atts);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void end(String message) throws IllegalFilterException {
        LOGGER.finer("declared type: " + this.declaredType);
        LOGGER.finer("end message: " + message);
        LOGGER.finer("current state: " + this.currentState);
        LOGGER.finest("expression factory: " + this.expFactory);
        if (this.expFactory != null) {
            this.expFactory.end(message);
            if (!this.expFactory.isReady()) return;
            if (this.currentState.equals("leftValue")) {
                ((MathExpression)this.curExprssn).addLeftValue(this.expFactory.create());
                this.currentState = "rightValue";
                this.expFactory = new ExpressionSAXParser(this.schema);
                LOGGER.finer("just added left value: " + this.currentState);
                return;
            } else if (this.currentState.equals("rightValue")) {
                ((MathExpression)this.curExprssn).addRightValue(this.expFactory.create());
                this.currentState = "complete";
                this.expFactory = null;
                LOGGER.finer("just added right value: " + this.currentState);
                return;
            } else {
                if (!this.currentState.equals("accumulate")) throw new IllegalFilterException("Attempted to add sub expression in a bad state: " + this.currentState);
                this.accumalationOfExpressions.add(this.expFactory.create());
                this.expFactory = null;
                LOGGER.finer("just added a parameter for a function: " + this.currentState);
                if (((FunctionExpression)this.curExprssn).getArgCount() == this.accumalationOfExpressions.size()) {
                    this.currentState = "complete";
                    ((FunctionExpression)this.curExprssn).setArgs(this.accumalationOfExpressions.toArray(new Expression[0]));
                    return;
                } else {
                    this.expFactory = new ExpressionSAXParser(this.schema);
                }
            }
            return;
        } else {
            if (!this.declaredType.equals(message) || !this.currentState.equals("complete")) throw new IllegalFilterException("Reached end of unready, non-nested expression: " + this.currentState);
            this.readChars = false;
            this.readyFlag = true;
        }
    }

    public boolean isReady() {
        return this.readyFlag;
    }

    public void message(String message) throws IllegalFilterException {
        LOGGER.finer("incoming message: " + message);
        LOGGER.finer("should read chars: " + this.readChars);
        if (this.readChars) {
            if (this.curExprssn instanceof AttributeExpression) {
                LOGGER.finer("...");
                String[] splitName = message.split("[.:/]");
                String newAttName = message;
                newAttName = splitName.length == 1 ? splitName[0] : splitName[splitName.length - 1];
                LOGGER.finer("setting attribute expression: " + newAttName);
                ((AttributeExpression)this.curExprssn).setAttributePath(newAttName);
                LOGGER.finer("...");
                this.currentState = "complete";
                LOGGER.finer("...");
            } else if (this.curExprssn instanceof LiteralExpression) {
                try {
                    Integer temp = new Integer(message);
                    ((LiteralExpression)this.curExprssn).setLiteral(temp);
                    this.currentState = "complete";
                }
                catch (NumberFormatException nfe1) {
                    try {
                        Double temp = new Double(message);
                        ((LiteralExpression)this.curExprssn).setLiteral(temp);
                        this.currentState = "complete";
                    }
                    catch (NumberFormatException nfe2) {
                        String temp = message;
                        ((LiteralExpression)this.curExprssn).setLiteral(temp);
                        this.currentState = "complete";
                    }
                }
            } else if (this.expFactory != null) {
                this.expFactory.message(message);
            }
        } else if (this.expFactory != null) {
            this.expFactory.message(message);
        }
    }

    public void geometry(Geometry geometry) throws IllegalFilterException {
        LOGGER.finer("got geometry: " + geometry.toString());
        this.curExprssn = FILTER_FACT.createLiteralExpression();
        ((LiteralExpression)this.curExprssn).setLiteral(geometry);
        LOGGER.finer("set expression: " + this.curExprssn.toString());
        this.currentState = "complete";
        LOGGER.finer("set current state: " + this.currentState);
    }

    public Expression create() {
        LOGGER.finer("about to create expression: " + this.curExprssn.toString());
        return this.curExprssn;
    }

    private static String setInitialState(Expression expression) throws IllegalFilterException {
        if (expression instanceof MathExpression) {
            return "leftValue";
        }
        if (expression instanceof AttributeExpression || expression instanceof LiteralExpression) {
            return "";
        }
        if (expression instanceof FunctionExpression) {
            return "accumulate";
        }
        throw new IllegalFilterException("Created illegal expression: " + expression.getClass().toString());
    }

    protected static short convertType(String expType) {
        if (expType.equals("Add")) {
            return 105;
        }
        if (expType.equals("Sub")) {
            return 106;
        }
        if (expType.equals("Mul")) {
            return 107;
        }
        if (expType.equals("Div")) {
            return 108;
        }
        if (expType.equals("PropertyName")) {
            return 109;
        }
        if (expType.equals("Literal")) {
            return 101;
        }
        if (expType.equals("Function")) {
            return 114;
        }
        return 100;
    }

    public String getFunctionName(Attributes map) {
        String result = map.getValue("name");
        if (result == null) {
            result = map.getValue("ogc:name");
        }
        if (result == null) {
            result = map.getValue("ows:name");
        }
        return result;
    }
}

