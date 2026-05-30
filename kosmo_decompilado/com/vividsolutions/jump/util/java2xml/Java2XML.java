/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.jdom.Attribute
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.output.Format
 *  org.jdom.output.XMLOutputter
 */
package com.vividsolutions.jump.util.java2xml;

import com.vividsolutions.jump.util.java2xml.XMLBinder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.saig.jump.lang.I18N;

public class Java2XML
extends XMLBinder {
    private static final Logger LOGGER = Logger.getLogger(Java2XML.class);

    public void write(Object object, String rootTagName, File file) throws Exception {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter((OutputStream)stream, Charset.forName("UTF-8"));
            this.write(object, rootTagName, writer);
        }
        finally {
            if (stream != null) {
                ((OutputStream)stream).close();
            }
        }
    }

    public void write(Object object, String rootTagName, Writer writer) throws Exception {
        Document document = new Document(new Element(rootTagName));
        this.write(object, document.getRootElement(), this.specElements(object.getClass()));
        XMLOutputter xmlOutputter = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        xmlOutputter.setFormat(format);
        xmlOutputter.output(document, writer);
    }

    private void write(final Object object, final Element tag, List<Element> specElements) throws Exception {
        try {
            this.visit(specElements, new XMLBinder.SpecVisitor(){

                @Override
                public void tagSpecFound(String xmlName, String javaName, List<Element> specChildElements) throws Exception {
                    ArrayList<Element> childTags = new ArrayList<Element>();
                    if (javaName != null) {
                        childTags.addAll(Java2XML.this.writeChildTags(tag, xmlName, Java2XML.this.getter(object.getClass(), javaName).invoke(object, new Object[0]), Java2XML.this.specifyingTypeExplicitly(Java2XML.this.fieldClass(Java2XML.this.setter(object.getClass(), javaName)))));
                    } else {
                        Element childTag = new Element(xmlName);
                        tag.addContent((Content)childTag);
                        childTags.add(childTag);
                    }
                    for (Element childTag : childTags) {
                        Java2XML.this.write(object, childTag, specChildElements);
                    }
                }

                @Override
                public void attributeSpecFound(String xmlName, String javaName) throws Exception {
                    Java2XML.this.writeAttribute(tag, xmlName, Java2XML.this.getter(object.getClass(), javaName).invoke(object, new Object[0]));
                }
            }, object.getClass());
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getMessage("com.vividsolutions.jump.util.java2xml.Java2XML.java2xml-exception-writing-{0}", new Object[]{object.getClass()}), (Throwable)e);
            throw e;
        }
    }

    private void writeAttribute(Element tag, String name, Object value) throws Exception {
        if (value == null) {
            throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.Java2XML.cannot-store-null-value-as-attribute-store-as-element-instead-{0}", new Object[]{name}));
        }
        tag.setAttribute(new Attribute(name, this.toXML(value)));
    }

    private Element writeChildTag(Element tag, String name, Object value, boolean specifyingType) throws Exception {
        Element childTag = new Element(name);
        if (value != null && specifyingType) {
            childTag.setAttribute(new Attribute("class", value.getClass().getName()));
        }
        if (value == null) {
            childTag.setAttribute(new Attribute("null", "true"));
        } else if (this.hasCustomConverter(value.getClass())) {
            childTag.setText(this.toXML(value));
        } else if (value instanceof Map) {
            for (Object key : ((Map)value).keySet()) {
                Element mappingTag = new Element("mapping");
                childTag.addContent((Content)mappingTag);
                this.writeChildTag(mappingTag, "key", key, true);
                this.writeChildTag(mappingTag, "value", ((Map)value).get(key), true);
            }
        } else if (value instanceof Collection) {
            for (Object item : (Collection)value) {
                this.writeChildTag(childTag, "item", item, true);
            }
        } else {
            this.write(value, childTag, this.specElements(value.getClass()));
        }
        tag.addContent((Content)childTag);
        return childTag;
    }

    private Collection<Element> writeChildTags(Element tag, String name, Object value, boolean specifyingType) throws Exception {
        ArrayList<Element> childTags = new ArrayList<Element>();
        if (value instanceof Collection) {
            for (Object item : (Collection)value) {
                childTags.add(this.writeChildTag(tag, name, item, specifyingType));
            }
        } else {
            childTags.add(this.writeChildTag(tag, name, value, specifyingType));
        }
        return childTags;
    }

    private Method getter(Class<?> fieldClass, String field) throws XMLBinder.XMLBinderException {
        Method[] methods = fieldClass.getMethods();
        int i = 0;
        while (i < methods.length) {
            if ((methods[i].getName().toUpperCase().equals("GET" + field.toUpperCase()) || methods[i].getName().toUpperCase().equals("IS" + field.toUpperCase())) && methods[i].getParameterTypes().length == 0) {
                return methods[i];
            }
            ++i;
        }
        i = 0;
        while (i < methods.length) {
            if ((methods[i].getName().toUpperCase().startsWith("GET" + field.toUpperCase()) || methods[i].getName().toUpperCase().startsWith("IS" + field.toUpperCase())) && methods[i].getParameterTypes().length == 0) {
                return methods[i];
            }
            ++i;
        }
        throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.Java2XML.coud-not-find-getter-named-like-{0}-{1}", new Object[]{field, fieldClass}));
    }
}

