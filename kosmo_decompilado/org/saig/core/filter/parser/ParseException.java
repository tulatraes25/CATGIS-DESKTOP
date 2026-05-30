/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import org.saig.core.filter.parser.Token;

public class ParseException
extends Exception {
    protected boolean specialConstructor;
    public Token currentToken;
    public int[][] expectedTokenSequences;
    public String[] tokenImage;
    protected String eol = System.getProperty("line.separator", "\n");

    public ParseException(Token currentTokenVal, int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
        super("");
        this.specialConstructor = true;
        this.currentToken = currentTokenVal;
        this.expectedTokenSequences = expectedTokenSequencesVal;
        this.tokenImage = tokenImageVal;
    }

    public ParseException() {
        this.specialConstructor = false;
    }

    public ParseException(String message) {
        super(message);
        this.specialConstructor = false;
    }

    @Override
    public String getMessage() {
        if (!this.specialConstructor) {
            return super.getMessage();
        }
        String expected = "";
        int maxSize = 0;
        int i = 0;
        while (i < this.expectedTokenSequences.length) {
            if (maxSize < this.expectedTokenSequences[i].length) {
                maxSize = this.expectedTokenSequences[i].length;
            }
            int j = 0;
            while (j < this.expectedTokenSequences[i].length) {
                expected = String.valueOf(expected) + this.tokenImage[this.expectedTokenSequences[i][j]] + " ";
                ++j;
            }
            if (this.expectedTokenSequences[i][this.expectedTokenSequences[i].length - 1] != 0) {
                expected = String.valueOf(expected) + "...";
            }
            expected = String.valueOf(expected) + this.eol + "    ";
            ++i;
        }
        String retval = "Encountered \"";
        Token tok = this.currentToken.next;
        int i2 = 0;
        while (i2 < maxSize) {
            if (i2 != 0) {
                retval = String.valueOf(retval) + " ";
            }
            if (tok.kind == 0) {
                retval = String.valueOf(retval) + this.tokenImage[0];
                break;
            }
            retval = String.valueOf(retval) + this.add_escapes(tok.image);
            tok = tok.next;
            ++i2;
        }
        retval = String.valueOf(retval) + "\" at line " + this.currentToken.next.beginLine + ", column " + this.currentToken.next.beginColumn;
        retval = String.valueOf(retval) + "." + this.eol;
        retval = this.expectedTokenSequences.length == 1 ? String.valueOf(retval) + "Was expecting:" + this.eol + "    " : String.valueOf(retval) + "Was expecting one of:" + this.eol + "    ";
        retval = String.valueOf(retval) + expected;
        return retval;
    }

    protected String add_escapes(String str) {
        StringBuffer retval = new StringBuffer();
        int i = 0;
        while (i < str.length()) {
            switch (str.charAt(i)) {
                case '\u0000': {
                    break;
                }
                case '\b': {
                    retval.append("\\b");
                    break;
                }
                case '\t': {
                    retval.append("\\t");
                    break;
                }
                case '\n': {
                    retval.append("\\n");
                    break;
                }
                case '\f': {
                    retval.append("\\f");
                    break;
                }
                case '\r': {
                    retval.append("\\r");
                    break;
                }
                case '\"': {
                    retval.append("\\\"");
                    break;
                }
                case '\'': {
                    retval.append("\\'");
                    break;
                }
                case '\\': {
                    retval.append("\\\\");
                    break;
                }
                default: {
                    char ch = str.charAt(i);
                    if (ch < ' ' || ch > '~') {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                        break;
                    }
                    retval.append(ch);
                }
            }
            ++i;
        }
        return retval.toString();
    }
}

