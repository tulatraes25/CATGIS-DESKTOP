/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util;

import javax.swing.AbstractSpinnerModel;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class SpinnerNumberAndNullModel
extends AbstractSpinnerModel {
    public static final Logger LOGGER = Logger.getLogger(SpinnerNumberAndNullModel.class);
    protected Number stepSize;
    protected Object value;
    protected Comparable minimum;
    protected Comparable maximum;
    protected int type;
    public static final int TYPE_INTEGER = 0;
    public static final int TYPE_LONG = 1;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_SHORT = 4;
    public static final int TYPE_BYTE = 5;
    private static final String NULL_VALUE = "";

    public SpinnerNumberAndNullModel(Number value, Comparable minimum, Comparable maximum, Number stepSize, int type) {
        if (stepSize == null) {
            throw new IllegalArgumentException(I18N.getString(this.getClass(), "stepsize-must-be-non-null"));
        }
        if (value != null) {
            if (minimum != null && minimum.compareTo(value) > 0 || maximum != null && maximum.compareTo(value) < 0) {
                throw new IllegalArgumentException(I18N.getString(this.getClass(), "minimum-value-maximum-is-false"));
            }
        } else if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(I18N.getString(this.getClass(), "minimum-maximum-is-false"));
        }
        if (!this.typeIsValid(type)) {
            throw new IllegalArgumentException(I18N.getString(this.getClass(), "declared-type-is-not-valid"));
        }
        if (!this.sameTypes(value, type)) {
            throw new IllegalArgumentException(I18N.getString(this.getClass(), "initialvaluedoes-not-correspond-with-declared-type"));
        }
        this.type = type;
        this.value = value != null ? value : NULL_VALUE;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepSize = stepSize;
    }

    protected boolean sameTypes(Number number, int type) {
        if (number == null) {
            return true;
        }
        boolean sameTypes = false;
        switch (type) {
            case 0: {
                sameTypes = number instanceof Integer;
                break;
            }
            case 1: {
                sameTypes = number instanceof Long;
                break;
            }
            case 2: {
                sameTypes = number instanceof Float;
                break;
            }
            case 3: {
                sameTypes = number instanceof Double;
                break;
            }
            case 4: {
                sameTypes = number instanceof Short;
                break;
            }
            case 5: {
                sameTypes = number instanceof Byte;
            }
        }
        return sameTypes;
    }

    protected boolean typeIsValid(int type) {
        boolean valid = false;
        switch (type) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: {
                valid = true;
            }
        }
        return valid;
    }

    public SpinnerNumberAndNullModel(int value, int minimum, int maximum, int stepSize, int type) {
        this(new Integer(value), new Integer(minimum), new Integer(maximum), new Integer(stepSize), type);
    }

    public SpinnerNumberAndNullModel(double value, double minimum, double maximum, double stepSize, int type) {
        this(new Double(value), new Double(minimum), new Double(maximum), new Double(stepSize), type);
    }

    public SpinnerNumberAndNullModel() {
        this(null, null, null, (Number)1, 0);
    }

    @Override
    public Object getNextValue() {
        return this.incrValue(1);
    }

    @Override
    public Object getPreviousValue() {
        return this.incrValue(-1);
    }

    protected Number incrValue(int dir) {
        Number newValue = null;
        long longValue = this.getDefaultValue(dir == 1);
        double doubleValue = longValue;
        if (!this.value.equals(NULL_VALUE)) {
            longValue = ((Number)this.value).longValue() + this.stepSize.longValue() * (long)dir;
            doubleValue = ((Number)this.value).doubleValue() + this.stepSize.doubleValue() * (double)dir;
        }
        switch (this.type) {
            case 0: {
                newValue = new Integer((int)longValue);
                break;
            }
            case 1: {
                newValue = new Long(longValue);
                break;
            }
            case 4: {
                newValue = new Short((short)longValue);
                break;
            }
            case 5: {
                newValue = new Byte((byte)longValue);
                break;
            }
            case 2: {
                newValue = new Float(doubleValue);
                break;
            }
            case 3: {
                newValue = new Double(doubleValue);
            }
        }
        if (!this.value.equals(NULL_VALUE)) {
            if (this.maximum != null && this.maximum.compareTo(newValue) < 0) {
                return null;
            }
            if (this.minimum != null && this.minimum.compareTo(newValue) > 0) {
                return null;
            }
        }
        return newValue;
    }

    protected long getDefaultValue(boolean min) {
        if (min) {
            if (this.minimum != null && this.minimum instanceof Number) {
                return ((Number)((Object)this.minimum)).longValue();
            }
            if (this.maximum != null && this.maximum instanceof Number) {
                return ((Number)((Object)this.maximum)).longValue() - 100L * this.stepSize.longValue();
            }
            return 0L;
        }
        if (this.maximum != null && this.maximum instanceof Number) {
            return ((Number)((Object)this.maximum)).longValue();
        }
        if (this.minimum != null && this.minimum instanceof Number) {
            return ((Number)((Object)this.minimum)).longValue() + 100L * this.stepSize.longValue();
        }
        return 0L;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        if (!this.isNumber(value)) {
            if (!NULL_VALUE.equals(this.value)) {
                this.value = NULL_VALUE;
                this.fireStateChanged();
            } else if (value != null) {
                this.fireStateChanged();
            }
        } else if (!this.value.equals(value)) {
            if (value instanceof Number) {
                this.value = (Number)value;
            } else {
                try {
                    switch (this.type) {
                        case 5: {
                            this.value = Byte.valueOf((String)value);
                            break;
                        }
                        case 3: {
                            this.value = Double.valueOf((String)value);
                            break;
                        }
                        case 2: {
                            this.value = Float.valueOf((String)value);
                            break;
                        }
                        case 0: {
                            this.value = Integer.valueOf((String)value);
                            break;
                        }
                        case 1: {
                            this.value = Long.valueOf((String)value);
                            break;
                        }
                        case 4: {
                            this.value = Short.valueOf((String)value);
                        }
                    }
                }
                catch (NumberFormatException nfe) {
                    LOGGER.error((Object)NULL_VALUE, (Throwable)nfe);
                    this.value = NULL_VALUE;
                }
            }
            this.fireStateChanged();
        }
    }

    protected boolean isNumber(Object value) {
        if (value == null || value.equals(NULL_VALUE)) {
            return false;
        }
        if (value instanceof Number) {
            return true;
        }
        boolean isNumber = false;
        if (value instanceof String) {
            isNumber = ((String)value).matches("\\d*[,.]?\\d*");
        }
        return isNumber;
    }
}

