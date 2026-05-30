/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Envelope;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;

public interface BBoxExpression
extends LiteralExpression {
    public void setBounds(Envelope var1) throws IllegalFilterException;
}

