/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LabelExpressionUtil {
    public static List<String> getLabelsFromExpression(String expression) {
        ArrayList<String> al = new ArrayList<String>();
        if (expression.indexOf(34) != -1) {
            StringTokenizer strTok = new StringTokenizer(expression, "\"", false);
            while (strTok.hasMoreTokens()) {
                String tok = strTok.nextToken();
                if (tok.charAt(0) != '@') continue;
                expression = tok.subSequence(1, tok.length()).toString();
                if (expression.indexOf(46) == -1) {
                    al.add(expression);
                    continue;
                }
                StringTokenizer tokexp = new StringTokenizer(expression, ".");
                String key = null;
                if (tokexp.hasMoreTokens()) {
                    key = tokexp.nextToken();
                }
                String field = null;
                if (tokexp.hasMoreTokens()) {
                    field = tokexp.nextToken();
                }
                String attribute = null;
                if (tokexp.hasMoreTokens()) {
                    attribute = tokexp.nextToken();
                }
                if (key == null || field == null || attribute == null) continue;
                al.add(attribute);
            }
        } else {
            al.add(expression);
        }
        return al;
    }
}

