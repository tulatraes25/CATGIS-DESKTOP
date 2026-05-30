/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.Element
 *  org.dom4j.io.SAXReader
 */
package org.saig.core.model.globes;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class TextBalloon
implements Cloneable {
    private String text = "";
    private String expr = "";
    private Color backgroundColor = Color.YELLOW;
    private Color lineColor = Color.BLACK;
    private double lineWidth = 1.0;
    private double margin = 0.05;
    private Coordinate balloonEnd;
    private Envelope balloonTextZone;
    private Font textfont = new Font("Dialog", 0, 10);

    public String getExpr() {
        return this.expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getText() {
        return this.text;
    }

    public double getMargin() {
        return this.margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public double getLineWidth() {
        return this.lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public Coordinate getBalloonEnd() {
        return this.balloonEnd;
    }

    public void setBalloonEnd(Coordinate globleEnd) {
        this.balloonEnd = globleEnd;
    }

    public Envelope getBalloonTextZone() {
        return this.balloonTextZone;
    }

    public void setBalloonTextZone(Envelope globeTextZone) {
        this.balloonTextZone = globeTextZone;
    }

    public Font getTextfont() {
        return this.textfont;
    }

    public void setTextfont(Font textfont) {
        this.textfont = textfont;
    }

    public String toXML() {
        String xml = "";
        xml = String.valueOf(xml) + "<TextBalloon>";
        xml = String.valueOf(xml) + "<Text>" + this.text + "</Text>";
        xml = String.valueOf(xml) + "<Expr>" + this.expr + "</Expr>";
        xml = String.valueOf(xml) + "<BackgroundColor r=\"" + this.backgroundColor.getRed() + "\" g=\"" + this.backgroundColor.getGreen() + "\" b=\"" + this.backgroundColor.getBlue() + "\"/>";
        xml = String.valueOf(xml) + "<LineColor r=\"" + this.lineColor.getRed() + "\" g=\"" + this.lineColor.getGreen() + "\" b=\"" + this.lineColor.getBlue() + "\"/>";
        xml = String.valueOf(xml) + "<LineWidth>" + this.lineWidth + "</LineWidth>";
        xml = String.valueOf(xml) + "<BalloonEnd x=\"" + this.balloonEnd.x + "\" y=\"" + this.balloonEnd.y + "\"/>";
        xml = String.valueOf(xml) + "<BalloonTextZone xmin=\"" + this.balloonTextZone.getMinX() + "\" xmax=\"" + this.balloonTextZone.getMaxX() + "\" ymin=\"" + this.balloonTextZone.getMinY() + "\" ymax=\"" + this.balloonTextZone.getMaxY() + "\"" + " margin=\"" + this.margin + "\"/>";
        xml = String.valueOf(xml) + "<TextFont family=\"" + this.textfont.getFamily() + "\" name=\"" + this.textfont.getName() + "\" style=\"" + this.textfont.getStyle() + "\" size=\"" + this.textfont.getSize() + "\" />";
        xml = String.valueOf(xml) + "</TextBalloon>";
        return xml;
    }

    public static TextBalloon parseFromXMLRawString(String xml) {
        SAXReader xmlReader = new SAXReader();
        TextBalloon tb = null;
        try {
            xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + xml;
            ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
            Document doc = xmlReader.read((InputStream)in);
            Element balloonElement = doc.getRootElement();
            tb = TextBalloon.parseFromXMLElement(balloonElement);
        }
        catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
        return tb;
    }

    public static TextBalloon parseFromXMLElement(Element balloonElement) {
        TextBalloon tb = new TextBalloon();
        tb.setText(balloonElement.element("Text").getText());
        if (balloonElement.element("Expr") != null) {
            tb.setExpr(balloonElement.element("Expr").getText());
        }
        int r = new Integer(balloonElement.element("BackgroundColor").attributeValue("r"));
        int g = new Integer(balloonElement.element("BackgroundColor").attributeValue("g"));
        int b = new Integer(balloonElement.element("BackgroundColor").attributeValue("b"));
        Color backgroundColor = new Color(r, g, b);
        tb.setBackgroundColor(backgroundColor);
        r = new Integer(balloonElement.element("LineColor").attributeValue("r"));
        g = new Integer(balloonElement.element("LineColor").attributeValue("g"));
        b = new Integer(balloonElement.element("LineColor").attributeValue("b"));
        Color lineColor = new Color(r, g, b);
        tb.setLineColor(lineColor);
        double x = new Double(balloonElement.element("BalloonEnd").attributeValue("x"));
        double y = new Double(balloonElement.element("BalloonEnd").attributeValue("y"));
        Coordinate c = new Coordinate(x, y);
        tb.setBalloonEnd(c);
        double xmin = new Double(balloonElement.element("BalloonTextZone").attributeValue("xmin"));
        double xmax = new Double(balloonElement.element("BalloonTextZone").attributeValue("xmax"));
        double ymin = new Double(balloonElement.element("BalloonTextZone").attributeValue("ymin"));
        double ymax = new Double(balloonElement.element("BalloonTextZone").attributeValue("ymax"));
        double margin = new Double(balloonElement.element("BalloonTextZone").attributeValue("margin"));
        Envelope env = new Envelope(xmin, xmax, ymin, ymax);
        tb.setBalloonTextZone(env);
        tb.setMargin(margin);
        double linewidth = new Double(balloonElement.element("LineWidth").getText());
        tb.setLineWidth(linewidth);
        Font f = new Font(balloonElement.element("TextFont").attributeValue("name"), (int)new Integer(balloonElement.element("TextFont").attributeValue("style")), new Integer(balloonElement.element("TextFont").attributeValue("size")));
        tb.setTextfont(f);
        return tb;
    }

    public Object clone() {
        TextBalloon tb = new TextBalloon();
        tb.backgroundColor = this.backgroundColor;
        tb.lineColor = this.lineColor;
        tb.balloonEnd = (Coordinate)this.balloonEnd.clone();
        tb.balloonTextZone = new Envelope(this.balloonTextZone);
        tb.text = new String(this.text);
        tb.textfont = this.textfont;
        tb.margin = this.margin;
        tb.expr = this.expr;
        return tb;
    }
}

