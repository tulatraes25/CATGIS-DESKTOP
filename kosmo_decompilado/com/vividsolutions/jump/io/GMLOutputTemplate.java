/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

public class GMLOutputTemplate {
    String headerText;
    String footerText;
    String AllFeatureText;
    ArrayList featureText = new ArrayList();
    ArrayList codingText = new ArrayList();
    String featureTextfooter;
    int lineNumber = 0;
    String streamName;

    public void setHeaderText(String text) {
        this.headerText = text;
    }

    public void setFooterText(String text) {
        this.footerText = text;
    }

    public void setFeatureFooter(String text) {
        this.featureTextfooter = text;
    }

    public void addItem(String header, String coding) {
        this.featureText.add(header);
        this.codingText.add(coding);
    }

    public void load(Reader r) throws Exception {
        this.load(r, "Unknown Stream");
    }

    private String getLine(BufferedReader br) throws Exception {
        ++this.lineNumber;
        return br.readLine();
    }

    public void load(Reader r, String readerName) throws Exception {
        String token;
        int index2;
        boolean justFoundTag = false;
        BufferedReader buffRead = new BufferedReader(r);
        this.streamName = readerName;
        this.headerText = "";
        boolean keepgoing = true;
        String line = "";
        int index = 0;
        while (keepgoing && (line = this.getLine(buffRead)) != null) {
            index = line.indexOf("<%");
            if (index != -1) {
                index2 = line.indexOf("%>", index);
                if (index2 == -1) {
                    throw new ParseException("While trying to find the GML output header, found a <%, but no %>", this.streamName, this.lineNumber, index);
                }
                token = line.substring(index + 2, index2);
                if (!(token = token.trim()).equalsIgnoreCase("FEATURE")) {
                    throw new ParseException("While trying to find the GML output header, found a <%..%> that isnt a <%FEATURE%>", this.streamName, this.lineNumber, index);
                }
                keepgoing = false;
                this.headerText = String.valueOf(this.headerText) + line.substring(0, index);
                line = line.substring(index2 + 2);
                continue;
            }
            this.headerText = String.valueOf(this.headerText) + line + "\n";
        }
        if (line == null) {
            throw new ParseException("Unexpected EOF while looking for header", this.streamName, this.lineNumber, index);
        }
        this.AllFeatureText = "";
        keepgoing = true;
        String textAccum = "";
        while (keepgoing) {
            index = line.indexOf("<%");
            if (index != -1) {
                index2 = line.indexOf("%>", index);
                if (index2 == -1) {
                    throw new ParseException("While looking at the GML feature text, found a <%, but no %>", this.streamName, this.lineNumber, index);
                }
                token = line.substring(index + 2, index2).trim();
                if (token.equalsIgnoreCase("ENDFEATURE")) {
                    keepgoing = false;
                    this.AllFeatureText = String.valueOf(this.AllFeatureText) + line.substring(0, index);
                    this.featureTextfooter = String.valueOf(textAccum) + line.substring(0, index);
                    line = line.substring(index2 + 2);
                } else {
                    if (!this.validop(token)) {
                        throw new ParseException("invalid token in <%..%> :" + token, this.streamName, this.lineNumber, index);
                    }
                    justFoundTag = true;
                    String pre = String.valueOf(textAccum) + line.substring(0, index);
                    textAccum = line.substring(index2 + 2);
                    this.featureText.add(pre);
                    this.codingText.add(token);
                }
            }
            if (!keepgoing) continue;
            this.AllFeatureText = String.valueOf(this.AllFeatureText) + line + "\n";
            if (!justFoundTag) {
                textAccum = String.valueOf(textAccum) + line + "\n";
            } else {
                justFoundTag = false;
                textAccum = String.valueOf(textAccum) + "\n";
            }
            line = this.getLine(buffRead);
            if (line != null) continue;
            throw new ParseException("Unexpected EOF while looking for feature", this.streamName, this.lineNumber, index);
        }
        this.footerText = line;
        while ((line = this.getLine(buffRead)) != null) {
            this.footerText = String.valueOf(this.footerText) + line + "\n";
        }
    }

    public String asString() {
        String result = String.valueOf(this.headerText) + "\n--------------------------------------\n";
        int t = 0;
        while (t < this.featureText.size()) {
            result = String.valueOf(result) + this.featureText.get(t) + "<%" + this.codingText.get(t) + "%>";
            ++t;
        }
        result = String.valueOf(result) + this.featureTextfooter;
        result = String.valueOf(result) + "\n--------------------------------------\n";
        result = String.valueOf(result) + this.footerText;
        return result;
    }

    private boolean validop(String op) {
        String op2 = new String(op);
        op2 = op2.trim();
        if (!(op2 = op2.toLowerCase()).startsWith("=") || op2.length() < 2) {
            return false;
        }
        op2 = op2.substring(1);
        return (op2 = op2.trim()).startsWith("column") || op2.startsWith("geometry");
    }
}

