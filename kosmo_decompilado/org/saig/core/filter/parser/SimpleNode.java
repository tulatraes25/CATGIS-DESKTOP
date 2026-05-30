/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import org.saig.core.filter.parser.ExpressionParser;
import org.saig.core.filter.parser.ExpressionParserTreeConstants;
import org.saig.core.filter.parser.ExpressionParserVisitor;
import org.saig.core.filter.parser.Node;
import org.saig.core.filter.parser.Token;

public class SimpleNode
implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected ExpressionParser parser;
    protected String value;
    Token token;

    public SimpleNode(int i) {
        this.id = i;
    }

    public SimpleNode(ExpressionParser p, int i) {
        this(i);
        this.parser = p;
    }

    @Override
    public void jjtOpen() {
    }

    @Override
    public void jjtClose() {
    }

    @Override
    public void jjtSetParent(Node n) {
        this.parent = n;
    }

    @Override
    public Node jjtGetParent() {
        return this.parent;
    }

    @Override
    public void jjtAddChild(Node n, int i) {
        if (this.children == null) {
            this.children = new Node[i + 1];
        } else if (i >= this.children.length) {
            Node[] c = new Node[i + 1];
            System.arraycopy(this.children, 0, c, 0, this.children.length);
            this.children = c;
        }
        this.children[i] = n;
    }

    @Override
    public Node jjtGetChild(int i) {
        return this.children[i];
    }

    @Override
    public int jjtGetNumChildren() {
        return this.children == null ? 0 : this.children.length;
    }

    @Override
    public Object jjtAccept(ExpressionParserVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    public String toString() {
        return ExpressionParserTreeConstants.jjtNodeName[this.id];
    }

    public String toString(String prefix) {
        return String.valueOf(prefix) + this.toString();
    }

    public void dump(String prefix) {
        System.out.println(this.toString(prefix));
        if (this.children != null) {
            int i = 0;
            while (i < this.children.length) {
                SimpleNode n = (SimpleNode)this.children[i];
                if (n != null) {
                    n.dump(String.valueOf(prefix) + " ");
                }
                ++i;
            }
        }
    }

    @Override
    public int getType() {
        return this.id;
    }

    @Override
    public void dispose() {
        this.parent = null;
        this.children = null;
        this.parser = null;
    }

    @Override
    public Token getToken() {
        return this.token;
    }
}

