/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import com.vividsolutions.jump.feature.AttributeType;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.BetweenFilterImpl;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilterImpl;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.NullFilterImpl;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderConstants;
import org.saig.core.model.feature.Attribute;

public class SQLFilterBuilderRow
extends JPanel {
    public static final Logger LOGGER = Logger.getLogger(SQLFilterBuilderRow.class);
    Attribute attribute;
    String connectOperation;
    String comparationOperation;
    List<Object> values;
    boolean isParenthesisOpen;
    boolean isParenthesisClose;
    boolean isFirstCondition;
    int indent = 0;

    public SQLFilterBuilderRow(boolean isFirstCondition, String connectOperation, String comparationOperation, Attribute attribute, List<Object> values, boolean isParenthesisOpen, boolean isParenthesisClose, int indent) {
        this.indent = indent;
        this.isFirstCondition = isFirstCondition;
        this.values = values;
        if (isFirstCondition || connectOperation == null) {
            connectOperation = "        ";
        } else {
            this.connectOperation = connectOperation;
        }
        this.attribute = attribute;
        this.comparationOperation = comparationOperation;
    }

    @Override
    public String toString() {
        String str = "";
        int i = 0;
        while (i < this.indent) {
            str = String.valueOf(str) + "        ";
            ++i;
        }
        str = String.valueOf(str) + this.getConditionText();
        return str;
    }

    public String getConditionText() {
        String conditionText;
        String string = conditionText = this.connectOperation != null ? this.connectOperation : "";
        conditionText = this.isParenthesisOpen ? String.valueOf(conditionText) + "(" : (this.isParenthesisClose ? String.valueOf(conditionText) + ")" : String.valueOf(conditionText) + this.getCondition());
        return conditionText;
    }

    private String getCondition() {
        String sql = "";
        sql = String.valueOf(sql) + this.attribute.getName() + " ";
        sql = String.valueOf(sql) + this.comparationOperation + " ";
        if (SQLFilterBuilderConstants.operationHasOneValue(this.comparationOperation)) {
            sql = String.valueOf(sql) + this.formatText(this.values.get(0)) + " ";
        } else if (SQLFilterBuilderConstants.operationHasTwoValues(this.comparationOperation)) {
            sql = String.valueOf(sql) + this.formatText(this.values.get(0)) + " AND ";
            sql = String.valueOf(sql) + this.formatText(this.values.get(1)) + " ";
        } else if (SQLFilterBuilderConstants.operationHasManyValues(this.comparationOperation)) {
            int numValues = this.values.size();
            int i = 0;
            while (i < numValues) {
                Object value = this.values.get(i);
                sql = String.valueOf(sql) + this.formatText(value);
                if (i < numValues - 1) {
                    sql = String.valueOf(sql) + ", ";
                }
                ++i;
            }
            sql = String.valueOf(sql) + " ";
        }
        return sql;
    }

    public String getSQLText() {
        String sqlText;
        String string = sqlText = this.connectOperation != null ? String.valueOf(this.connectOperation) + " " : "";
        sqlText = this.isParenthesisOpen ? String.valueOf(sqlText) + "(" : (this.isParenthesisClose ? String.valueOf(sqlText) + ")" : String.valueOf(sqlText) + this.getSQLCondition());
        return sqlText;
    }

    private String getSQLCondition() {
        String sql = "";
        sql = String.valueOf(sql) + this.attribute.getName() + " ";
        sql = String.valueOf(sql) + SQLFilterBuilderConstants.getSQLOperation(this.comparationOperation) + " ";
        boolean isNumber = AttributeType.isNumeric(this.attribute.getType());
        if (SQLFilterBuilderConstants.operationHasOneValue(this.comparationOperation)) {
            sql = isNumber ? String.valueOf(sql) + this.formatText(this.values.get(0)) + " " : String.valueOf(sql) + "'" + this.formatText(this.values.get(0)) + "' ";
        } else if (SQLFilterBuilderConstants.operationHasTwoValues(this.comparationOperation)) {
            if (isNumber) {
                sql = String.valueOf(sql) + this.formatText(this.values.get(0)) + " AND ";
                sql = String.valueOf(sql) + this.formatText(this.values.get(1)) + " ";
            } else {
                sql = String.valueOf(sql) + "'" + this.formatText(this.values.get(0)) + "'" + " AND ";
                sql = String.valueOf(sql) + "'" + this.formatText(this.values.get(1)) + "'" + " ";
            }
        } else if (SQLFilterBuilderConstants.operationHasManyValues(this.comparationOperation)) {
            int numValues = this.values.size();
            int i = 0;
            while (i < numValues) {
                if (i == 0) {
                    sql = String.valueOf(sql) + "( ";
                }
                Object value = this.values.get(i);
                sql = !isNumber ? String.valueOf(sql) + "'" + this.formatText(value) + "'" : String.valueOf(sql) + this.formatText(value);
                sql = i < numValues - 1 ? String.valueOf(sql) + ", " : String.valueOf(sql) + " )";
                ++i;
            }
            sql = String.valueOf(sql) + " ";
        }
        return sql;
    }

    public Filter getFilter() {
        Filter filter = null;
        try {
            if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_EQUALS)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_DOES_NOT_EQUALS)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter.not();
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_LESS_THAN)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(15);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_LESS_THAN_OR_EQUAL_TO)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(17);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_GREATER_THAN)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(16);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_GREATER_THAN_OR_EQUAL_TO)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(18);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_DOES_NOT_EQUALS)) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                compareFilter.addRightValue(new LiteralExpressionImpl(this.values.get(0)));
                filter = compareFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_LIKE)) {
                LikeFilterImpl likeFilter = new LikeFilterImpl();
                likeFilter.setPattern(this.values.get(0).toString());
                likeFilter.setValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = likeFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_NOT_LIKE)) {
                LikeFilterImpl likeFilter = new LikeFilterImpl();
                likeFilter.setPattern(this.values.get(0).toString());
                likeFilter.setValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = likeFilter.not();
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_BETWEEN)) {
                BetweenFilterImpl betweenFilter = new BetweenFilterImpl();
                betweenFilter.addLeftValue(new LiteralExpressionImpl(this.values.get(0)));
                betweenFilter.addRightValue(new LiteralExpressionImpl(this.values.get(1)));
                betweenFilter.addMiddleValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = betweenFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_NOT_BETWEEN)) {
                BetweenFilterImpl betweenFilter = new BetweenFilterImpl();
                betweenFilter.addLeftValue(new LiteralExpressionImpl(this.values.get(0)));
                betweenFilter.addRightValue(new LiteralExpressionImpl(this.values.get(1)));
                betweenFilter.addMiddleValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = betweenFilter.not();
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IN)) {
                Iterator<Object> it = this.values.iterator();
                while (it.hasNext()) {
                    CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                    compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                    compareFilter.addRightValue(new LiteralExpressionImpl(it.next()));
                    filter = filter == null ? compareFilter : filter.or(compareFilter);
                }
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_NOT_IN)) {
                Iterator<Object> it = this.values.iterator();
                while (it.hasNext()) {
                    CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                    compareFilter.addLeftValue(new AttributeExpressionImpl2(this.attribute.getName()));
                    compareFilter.addRightValue(new LiteralExpressionImpl(it.next()));
                    filter = filter == null ? compareFilter.not() : filter.and(compareFilter.not());
                }
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_BLANK)) {
                NullFilterImpl nullFilter = new NullFilterImpl();
                nullFilter.setNullCheckValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = nullFilter;
            } else if (this.comparationOperation.equals(SQLFilterBuilderConstants.OP_IS_NOT_BLANK)) {
                NullFilterImpl nullFilter = new NullFilterImpl();
                nullFilter.setNullCheckValue(new AttributeExpressionImpl2(this.attribute.getName()));
                filter = nullFilter.not();
            }
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)"", (Throwable)ife);
            filter = null;
        }
        return filter;
    }

    private String formatText(Object value) {
        return SQLFilterBuilderConstants.valueFormatter(value);
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public String getConnectOperation() {
        return this.connectOperation;
    }

    public void setConnectOperation(String connectOperation) {
        this.connectOperation = connectOperation;
    }

    public String getComparationOperation() {
        return this.comparationOperation;
    }

    public void setComparationOperation(String comparationOperation) {
        this.comparationOperation = comparationOperation;
    }

    public List<Object> getValues() {
        return this.values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public int getIndent() {
        return this.indent;
    }

    public boolean isFirstCondition() {
        return this.isFirstCondition;
    }

    public void setFirstCondition(boolean isFirstCondition) {
        this.isFirstCondition = isFirstCondition;
        if (isFirstCondition) {
            this.connectOperation = "        ";
        }
    }
}

