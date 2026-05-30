/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.cresques.px.dxf.Unicode
 */
package org.cresques.px.dxf;

import java.text.StringCharacterIterator;
import org.apache.commons.lang.StringUtils;
import org.cresques.px.dxf.Unicode;

public class DxfConvTexts {
    public static String ConvertText(String s) {
        if (StringUtils.isEmpty((String)s)) {
            return s;
        }
        StringCharacterIterator stringcharacteriterator = new StringCharacterIterator(s);
        StringBuffer stringbuffer = new StringBuffer();
        int[] ai = new int[s.length()];
        int i = 0;
        int j = 0;
        char c = stringcharacteriterator.first();
        while (c != '\uffff') {
            if (c == '%') {
                c = stringcharacteriterator.next();
                if (c != '%') {
                    stringbuffer.append('%');
                    c = stringcharacteriterator.previous();
                } else {
                    c = stringcharacteriterator.next();
                    switch (c) {
                        case '%': {
                            stringbuffer.append('%');
                            break;
                        }
                        case 'P': 
                        case 'p': {
                            stringbuffer.append('\u00f1');
                            break;
                        }
                        case 'C': 
                        case 'c': {
                            stringbuffer.append('\u00ed');
                            break;
                        }
                        case 'D': 
                        case 'd': {
                            stringbuffer.append('\u00b0');
                            break;
                        }
                        case 'U': 
                        case 'u': {
                            int n = stringbuffer.length();
                            ai[n] = ai[n] ^ 1;
                            ++i;
                            break;
                        }
                        case 'O': 
                        case 'o': {
                            int n = stringbuffer.length();
                            ai[n] = ai[n] ^ 2;
                            ++j;
                            break;
                        }
                        default: {
                            if (c >= '0' && c <= '9') {
                                int k = 3;
                                char c1 = (char)(c - 48);
                                c = stringcharacteriterator.next();
                                while (c >= '0' && c <= '9' && --k > 0) {
                                    c1 = (char)(10 * c1 + (c - 48));
                                    c = stringcharacteriterator.next();
                                }
                                stringbuffer.append(c1);
                            }
                            c = stringcharacteriterator.previous();
                            break;
                        }
                    }
                }
            } else if (c == '^') {
                c = stringcharacteriterator.next();
                if (c == ' ') {
                    stringbuffer.append('^');
                }
            } else {
                stringbuffer.append(c);
            }
            c = stringcharacteriterator.next();
        }
        String ss = s = Unicode.char2DOS437((StringBuffer)stringbuffer, (int)2, (char)'?');
        return ss;
    }
}

