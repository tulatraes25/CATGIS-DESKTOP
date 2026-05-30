/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.measure.unit.UnitFormat
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.util.LocalConfiguration
 *  org.exolab.castor.xml.Marshaller
 *  org.exolab.castor.xml.Unmarshaller
 *  org.jdom.Attribute
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.input.SAXBuilder
 */
package com.vividsolutions.jump.util.java2xml;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.util.StringUtil;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.gvsig.crs.ICrs;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterTransformer;
import org.saig.core.filter.XMLFilterReader;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PointSymbolizerImpl;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.config.HTTPProxySettings;

public class XMLBinder {
    private static final Logger LOGGER = Logger.getLogger(XMLBinder.class);
    private HashMap<Class<?>, CustomConverter> classToCustomConverterMap = new HashMap();

    public XMLBinder() {
        this.classToCustomConverterMap.put(Class.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                try {
                    return Class.forName(value);
                }
                catch (ClassNotFoundException e) {
                    Assert.shouldNeverReachHere();
                    return null;
                }
            }

            @Override
            public String toXML(Object object) {
                return ((Class)object).getName();
            }
        });
        this.classToCustomConverterMap.put(Color.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                return new Color(Integer.parseInt(parameters.get(0)), Integer.parseInt(parameters.get(1)), Integer.parseInt(parameters.get(2)), Integer.parseInt(parameters.get(3)));
            }

            @Override
            public String toXML(Object object) {
                Color color = (Color)object;
                ArrayList<Integer> parameters = new ArrayList<Integer>();
                parameters.add(new Integer(color.getRed()));
                parameters.add(new Integer(color.getGreen()));
                parameters.add(new Integer(color.getBlue()));
                parameters.add(new Integer(color.getAlpha()));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Font.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                return new Font(parameters.get(0), Integer.parseInt(parameters.get(1)), Integer.parseInt(parameters.get(2)));
            }

            @Override
            public String toXML(Object object) {
                Font font = (Font)object;
                ArrayList<Object> parameters = new ArrayList<Object>();
                parameters.add(font.getName());
                parameters.add(new Integer(font.getStyle()));
                parameters.add(new Integer(font.getSize()));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Double.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Double(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Double.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Double(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Integer.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Integer(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Integer.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Integer(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(String.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return value;
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Boolean.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Boolean(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Boolean.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Boolean(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Short.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Short(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Short.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Short(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Float.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Float(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Float.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Float(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(float[].class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                float[] solucion = new float[parameters.size()];
                int i = 0;
                while (i < parameters.size()) {
                    solucion[i] = Float.valueOf(parameters.get(i)).floatValue();
                    ++i;
                }
                return solucion;
            }

            @Override
            public String toXML(Object object) {
                float[] array = (float[])object;
                ArrayList<Float> parameters = new ArrayList<Float>(array.length);
                int i = 0;
                while (i < array.length) {
                    parameters.add(new Float(array[i]));
                    ++i;
                }
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Long.TYPE, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Long(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Long.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new Long(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(BigDecimal.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                return new BigDecimal(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(URL.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws MalformedURLException {
                return new URL(value);
            }

            @Override
            public String toXML(Object object) {
                return object.toString();
            }
        });
        this.classToCustomConverterMap.put(Filter.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                StringReader reader = new StringReader(value);
                Filter filter = XMLFilterReader.readFilter(reader);
                return filter;
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                FilterTransformer transformer = new FilterTransformer();
                String solucion = transformer.transform((Filter)object);
                return solucion;
            }
        });
        this.classToCustomConverterMap.put(Envelope.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                double minX = Double.parseDouble(parameters.get(0));
                double maxX = Double.parseDouble(parameters.get(1));
                double minY = Double.parseDouble(parameters.get(2));
                double maxY = Double.parseDouble(parameters.get(3));
                return new Envelope(minX, maxX, minY, maxY);
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                ArrayList<Double> parameters = new ArrayList<Double>();
                Envelope env = (Envelope)object;
                parameters.add(new Double(env.getMinX()));
                parameters.add(new Double(env.getMaxX()));
                parameters.add(new Double(env.getMinY()));
                parameters.add(new Double(env.getMaxY()));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Point.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                int coordX = Integer.parseInt(parameters.get(0));
                int coordY = Integer.parseInt(parameters.get(1));
                return new Point(coordX, coordY);
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                ArrayList<Integer> parameters = new ArrayList<Integer>();
                Point p = (Point)object;
                parameters.add(new Integer(p.x));
                parameters.add(new Integer(p.y));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Dimension.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                int width = Integer.parseInt(parameters.get(0));
                int height = Integer.parseInt(parameters.get(1));
                return new Dimension(width, height);
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                ArrayList<Integer> parameters = new ArrayList<Integer>();
                Dimension dim = (Dimension)object;
                parameters.add(new Integer(dim.width));
                parameters.add(new Integer(dim.height));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Icon.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                return new ImageIcon(value);
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                Icon icon = (Icon)object;
                return icon.toString();
            }
        });
        this.classToCustomConverterMap.put(PageFormat.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                int orientation = Integer.parseInt(parameters.get(0));
                double imageableX = Double.parseDouble(parameters.get(1));
                double imageableY = Double.parseDouble(parameters.get(2));
                double imageableWidth = Double.parseDouble(parameters.get(3));
                double imageableHeight = Double.parseDouble(parameters.get(4));
                double width = Double.parseDouble(parameters.get(5));
                double height = Double.parseDouble(parameters.get(6));
                PageFormat pf = new PageFormat();
                Paper paper = new Paper();
                paper.setSize(width, height);
                paper.setImageableArea(imageableX, imageableY, imageableWidth, imageableHeight);
                pf.setOrientation(orientation);
                pf.setPaper(paper);
                return pf;
            }

            @Override
            public String toXML(Object object) throws TransformerException {
                ArrayList<Number> parameters = new ArrayList<Number>();
                PageFormat pf = (PageFormat)object;
                Paper paper = pf.getPaper();
                parameters.add(new Integer(pf.getOrientation()));
                parameters.add(new Double(paper.getImageableX()));
                parameters.add(new Double(paper.getImageableY()));
                parameters.add(new Double(paper.getImageableWidth()));
                parameters.add(new Double(paper.getImageableHeight()));
                parameters.add(new Double(paper.getWidth()));
                parameters.add(new Double(paper.getHeight()));
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Timestamp.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                Timestamp timestamp = null;
                try {
                    timestamp = new Timestamp(new Long(value));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                return timestamp;
            }

            @Override
            public String toXML(Object object) {
                return "" + ((Timestamp)object).getTime();
            }
        });
        this.classToCustomConverterMap.put(Date.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                Date date = null;
                try {
                    date = new SimpleDateFormat().parse(value);
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                return date;
            }

            @Override
            public String toXML(Object object) {
                return new SimpleDateFormat().format((Date)object);
            }
        });
        this.classToCustomConverterMap.put(PointSymbolizerImpl.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                Mapping java2XMLMappings = null;
                PointSymbolizer pointSymbolizer = null;
                try {
                    java2XMLMappings = LoadXMLMappings.loadJava2XMLMappings();
                    Unmarshaller unmar = new Unmarshaller(java2XMLMappings);
                    unmar.setWhitespacePreserve(true);
                    pointSymbolizer = (PointSymbolizerImpl)unmar.unmarshal((Reader)new StringReader(value));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    pointSymbolizer = SLDEditor.styleBuilder.createPointSymbolizer();
                }
                return pointSymbolizer;
            }

            @Override
            public String toXML(Object object) {
                StringWriter stringWriter;
                block5: {
                    stringWriter = new StringWriter();
                    try {
                        try {
                            Mapping java2XMLMappings = LoadXMLMappings.loadJava2XMLMappings();
                            Properties properties = LocalConfiguration.getInstance().getProperties();
                            properties.setProperty("org.exolab.castor.indent", "true");
                            Marshaller marshaller = new Marshaller((Writer)stringWriter);
                            marshaller.setMapping(java2XMLMappings);
                            marshaller.marshal(object);
                        }
                        catch (Exception ex) {
                            LOGGER.error((Object)"", (Throwable)ex);
                            stringWriter.flush();
                            break block5;
                        }
                    }
                    catch (Throwable throwable) {
                        stringWriter.flush();
                        throw throwable;
                    }
                    stringWriter.flush();
                }
                return stringWriter.toString();
            }
        });
        this.classToCustomConverterMap.put(IProjection.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromPercentDelimitedString(value);
                int crsCode = Integer.parseInt(parameters.get(0));
                String crsWKT = parameters.get(1);
                String nadGrid = parameters.get(2);
                boolean isTargetNad = Boolean.parseBoolean(parameters.get(3));
                ICrs proj = null;
                if (crsWKT != null) {
                    try {
                        proj = CrsRepositoryManager.getInstance().getCRS(crsCode, crsWKT);
                        proj.setTransParam(nadGrid);
                        proj.setTransInTarget(isTargetNad);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                return proj;
            }

            @Override
            public String toXML(Object object) {
                ArrayList<Object> parameters = new ArrayList<Object>();
                ICrs proj = (ICrs)object;
                parameters.add(new Integer(proj.getCode()));
                parameters.add(proj.getWKT());
                parameters.add(proj.getTransParam());
                parameters.add(new Boolean(proj.isTransInTarget()));
                return StringUtil.toPercentDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(String[].class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                String[] solucion = new String[parameters.size()];
                int i = 0;
                while (i < parameters.size()) {
                    solucion[i] = parameters.get(i);
                    ++i;
                }
                return solucion;
            }

            @Override
            public String toXML(Object object) {
                String[] array = (String[])object;
                ArrayList<String> parameters = new ArrayList<String>();
                int i = 0;
                while (i < array.length) {
                    parameters.add(new String(array[i]));
                    ++i;
                }
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(HTTPProxySettings.class, new CustomConverter(){

            @Override
            public Object toJava(String value) {
                List<String> parameters = StringUtil.fromCommaDelimitedString(value);
                HTTPProxySettings settings = new HTTPProxySettings();
                settings.setHost(parameters.get(0));
                settings.setPort(Integer.valueOf(parameters.get(1)));
                if (parameters.size() > 2) {
                    settings.setUserName(parameters.get(2));
                }
                if (parameters.size() > 3) {
                    settings.setEncryptedPassword(parameters.get(3));
                }
                if (parameters.size() > 4) {
                    settings.setDirectConnectionTo(parameters.get(4));
                }
                return settings;
            }

            @Override
            public String toXML(Object object) {
                HTTPProxySettings settings = (HTTPProxySettings)object;
                ArrayList<Object> parameters = new ArrayList<Object>();
                parameters.add(settings.getHost());
                parameters.add(settings.getPort());
                if (StringUtils.isNotEmpty((String)settings.getUserName())) {
                    parameters.add(settings.getUserName());
                }
                if (StringUtils.isNotEmpty((String)settings.getPassword())) {
                    parameters.add(settings.getEncryptedPassword());
                }
                if (StringUtils.isNotEmpty((String)settings.getDirectConnectionTo())) {
                    parameters.add(settings.getDirectConnectionTo());
                }
                return StringUtil.toCommaDelimitedString(parameters);
            }
        });
        this.classToCustomConverterMap.put(Unit.class, new CustomConverter(){

            @Override
            public Object toJava(String value) throws Exception {
                Unit<Length> unit = UnitsManager.getLengthUnitFromName(value);
                if (unit == null) {
                    unit = UnitsManager.getAreaUnitFromName(value);
                }
                return unit;
            }

            @Override
            public String toXML(Object object) throws Exception {
                Unit unit = (Unit)object;
                String unitName = UnitsManager.getNameForUnit(unit);
                if (StringUtils.isEmpty((String)unitName)) {
                    unitName = UnitFormat.getInstance((Locale)I18N.getLocale()).format((Object)unit);
                }
                return unitName;
            }
        });
    }

    private String specFilename(Class<?> c) {
        return String.valueOf(StringUtil.classNameWithoutPackageQualifiers(c.getName())) + ".java2xml";
    }

    protected List<Element> specElements(Class<?> c) throws XMLBinderException, JDOMException, IOException {
        InputStream stream = this.specResourceStream(c);
        if (stream == null) {
            throw new XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XMLBinder.could-not-find-java2xml-file-for-{0}-or-its-interfaces-or-superclasses", new Object[]{c.getName()}));
        }
        try {
            Element root = new SAXBuilder().build(stream).getRootElement();
            if (!root.getAttributes().isEmpty()) {
                throw new XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XMLBinder.root-element-of-{0}-should-not-have-attributes", new Object[]{this.specFilename(c)}));
            }
            if (!root.getName().equals("root")) {
                throw new XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XMLBinder.root-element-of-{0}-should-be-named-root", new Object[]{this.specFilename(c)}));
            }
            List list = root.getChildren();
            return list;
        }
        finally {
            stream.close();
        }
    }

    private InputStream specResourceStream(Class<?> c) {
        for (Class<?> type : LangUtil.classesAndInterfaces(c)) {
            Assert.isTrue((boolean)type.isAssignableFrom(c));
            InputStream stream = type.getResourceAsStream(this.specFilename(type));
            if (stream == null) continue;
            return stream;
        }
        return null;
    }

    public void addCustomConverter(Class<?> c, CustomConverter converter) {
        this.classToCustomConverterMap.put(c, converter);
    }

    protected void visit(List<Element> specElements, SpecVisitor visitor, Class<?> c) throws Exception {
        for (Element specElement : specElements) {
            Attribute xmlName = specElement.getAttribute("xml-name");
            if (xmlName == null) {
                throw new XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XMLBinder.{0}-expected-xml-name-attribute-in-{1}-but-found-none", new Object[]{StringUtil.classNameWithoutPackageQualifiers(c.getName()), specElement.getName()}));
            }
            Attribute javaName = specElement.getAttribute("java-name");
            if (specElement.getName().equals("element")) {
                visitor.tagSpecFound(xmlName.getValue(), javaName != null ? javaName.getValue() : null, specElement.getChildren());
            }
            if (!specElement.getName().equals("attribute")) continue;
            visitor.attributeSpecFound(xmlName.getValue(), javaName.getValue());
        }
    }

    public Object toJava(String text, Class<?> c) throws Exception {
        return !text.equals("null") ? this.classToCustomConverterMap.get(this.customConvertableClass(c)).toJava(text) : null;
    }

    protected boolean specifyingTypeExplicitly(Class<?> c) throws XMLBinderException {
        if (this.hasCustomConverter(c)) {
            return false;
        }
        return c == Object.class || Modifier.isAbstract(c.getModifiers()) || c.isInterface();
    }

    protected Class<?> fieldClass(Method setter) {
        Assert.isTrue((setter.getParameterTypes().length == 1 ? 1 : 0) != 0);
        return setter.getParameterTypes()[0];
    }

    public Method setter(Class<?> c, String field) throws XMLBinderException {
        Method[] methods = c.getMethods();
        int i = 0;
        while (i < methods.length) {
            if ((methods[i].getName().toUpperCase().equals("SET" + field.toUpperCase()) || methods[i].getName().toUpperCase().equals("ADD" + field.toUpperCase())) && methods[i].getParameterTypes().length == 1) {
                return methods[i];
            }
            ++i;
        }
        i = 0;
        while (i < methods.length) {
            if ((methods[i].getName().toUpperCase().startsWith("SET" + field.toUpperCase()) || methods[i].getName().toUpperCase().startsWith("ADD" + field.toUpperCase())) && methods[i].getParameterTypes().length == 1) {
                return methods[i];
            }
            ++i;
        }
        throw new XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XMLBinder.could-not-find-setter-named-like-{0}-in-class-{1}", new Object[]{field, c}));
    }

    protected String toXML(Object object) throws Exception {
        return this.classToCustomConverterMap.get(this.customConvertableClass(object.getClass())).toXML(object);
    }

    protected boolean hasCustomConverter(Class<?> fieldClass) {
        return this.customConvertableClass(fieldClass) != null;
    }

    private Class<?> customConvertableClass(Class<?> c) {
        ArrayList assignableClasses = new ArrayList();
        for (Class<?> customConvertableClass : this.classToCustomConverterMap.keySet()) {
            if (!customConvertableClass.isAssignableFrom(c)) continue;
            assignableClasses.add(customConvertableClass);
        }
        if (assignableClasses.size() == 1) {
            return (Class)assignableClasses.get(0);
        }
        if (assignableClasses.size() > 1) {
            if (assignableClasses.contains(c)) {
                return c;
            }
            return (Class)assignableClasses.get(0);
        }
        return null;
    }

    public static interface CustomConverter {
        public Object toJava(String var1) throws Exception;

        public String toXML(Object var1) throws Exception;
    }

    protected static interface SpecVisitor {
        public void tagSpecFound(String var1, String var2, List<Element> var3) throws Exception;

        public void attributeSpecFound(String var1, String var2) throws Exception;
    }

    public static class XMLBinderException
    extends Exception {
        private static final long serialVersionUID = 1L;

        public XMLBinderException(String message) {
            super(message);
        }
    }
}

