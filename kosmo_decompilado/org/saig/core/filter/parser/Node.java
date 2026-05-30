/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import org.saig.core.filter.parser.ExpressionParserVisitor;
import org.saig.core.filter.parser.Token;

public interface Node {
    public void dispose();

    public Token getToken();

    public int getType();

    public void jjtOpen();

    public void jjtClose();

    public void jjtSetParent(Node var1);

    public Node jjtGetParent();

    public void jjtAddChild(Node var1, int var2);

    public Node jjtGetChild(int var1);

    public int jjtGetNumChildren();

    public Object jjtAccept(ExpressionParserVisitor var1, Object var2);
}

