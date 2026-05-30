/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.jdom.Element
 *  org.jdom.input.SAXBuilder
 */
package com.vividsolutions.jump.util.java2xml;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.util.java2xml.XMLBinder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.saig.jump.lang.I18N;

public class XML2Java
extends XMLBinder {
    private List<Listener> listeners = new ArrayList<Listener>();
    private ClassLoader classLoader = this.getClass().getClassLoader();

    public XML2Java() {
    }

    public XML2Java(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object read(String xml, Class<?> c) throws Exception {
        StringReader reader = new StringReader(xml);
        try {
            Object object = this.read(reader, c);
            return object;
        }
        finally {
            reader.close();
        }
    }

    public Object read(Reader reader, Class<?> c) throws Exception {
        return this.read(new SAXBuilder().build(reader).getRootElement(), c);
    }

    public Object read(File file, Class<?> c) throws Exception {
        FileReader fileReader = new FileReader(file);
        try {
            Object object;
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                object = new XML2Java().read(bufferedReader, c);
            }
            catch (Throwable throwable) {
                bufferedReader.close();
                throw throwable;
            }
            bufferedReader.close();
            return object;
        }
        finally {
            fileReader.close();
        }
    }

    private void read(final Element tag, final Object object, List<Element> specElements) throws Exception {
        Assert.isTrue((tag != null ? 1 : 0) != 0);
        this.visit(specElements, new XMLBinder.SpecVisitor(){

            private void fillerTagSpecFound(String xmlName, List<Element> specChildElements) throws Exception {
                if (tag.getChildren(xmlName).size() != 1) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-1-{0}-tag-but-found-{1}", new Object[]{xmlName, new Integer(tag.getChildren(xmlName).size())}));
                }
                XML2Java.this.read(tag.getChild(xmlName), object, specChildElements);
            }

            private void normalTagSpecFound(String xmlName, String javaName, List<Element> specChildElements) throws Exception {
                XML2Java.this.setValuesFromTags(object, XML2Java.this.setter(object.getClass(), javaName), tag.getChildren(xmlName));
                for (Element childTag : tag.getChildren(xmlName)) {
                    XML2Java.this.read(childTag, object, specChildElements);
                }
            }

            @Override
            public void tagSpecFound(String xmlName, String javaName, List<Element> specChildElements) throws Exception {
                if (javaName == null) {
                    this.fillerTagSpecFound(xmlName, specChildElements);
                } else {
                    this.normalTagSpecFound(xmlName, javaName, specChildElements);
                }
            }

            @Override
            public void attributeSpecFound(String xmlName, String javaName) throws Exception {
                if (tag.getAttribute(xmlName) == null) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-attribute-but-found-none-tag-{1}-attributes-{2}", new Object[]{xmlName, tag.getName(), StringUtil.toCommaDelimitedString(tag.getAttributes())}));
                }
                Method setter = XML2Java.this.setter(object.getClass(), javaName);
                XML2Java.this.setValue(object, setter, XML2Java.this.toJava(tag.getAttribute(xmlName).getValue(), setter.getParameterTypes()[0]));
            }
        }, object.getClass());
    }

    private Object read(Element tag, Class<?> c) throws Exception {
        if (tag.getAttribute("null") != null && tag.getAttributeValue("null").equals("true")) {
            return null;
        }
        if (this.specifyingTypeExplicitly(c)) {
            if (tag.getAttribute("class") == null) {
                throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-class-attribute-but-found-none", new Object[]{tag.getName()}));
            }
            return this.read(tag, Class.forName(tag.getAttributeValue("class"), true, this.classLoader));
        }
        this.fireCreatingObject(c);
        if (this.hasCustomConverter(c)) {
            return this.toJava(tag.getTextTrim(), c);
        }
        Object object = c.newInstance();
        if (object instanceof Map) {
            for (Element mappingTag : tag.getChildren()) {
                if (!mappingTag.getName().equals("mapping")) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-mapping-tag-but-found-none", new Object[]{tag.getName()}));
                }
                if (mappingTag.getChildren().size() != 2) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-2-tags-under-mapping-but-found-{1}", new Object[]{tag.getName(), new Integer(mappingTag.getChildren().size())}));
                }
                if (mappingTag.getChildren("key").size() != 1) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-1-key-tag-under-mapping-but-found-{1}", new Object[]{tag.getName(), new Integer(mappingTag.getChildren("key").size())}));
                }
                if (mappingTag.getChildren("value").size() != 1) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-1-value-tag-under-mapping-but-found-{1}", new Object[]{tag.getName(), new Integer(mappingTag.getChildren("key").size())}));
                }
                ((Map)object).put(this.read(mappingTag.getChild("key"), Object.class), this.read(mappingTag.getChild("value"), Object.class));
            }
        } else if (object instanceof Collection) {
            for (Element itemTag : tag.getChildren()) {
                if (!itemTag.getName().equals("item")) {
                    throw new XMLBinder.XMLBinderException(I18N.getMessage("com.vividsolutions.jump.util.java2xml.XML2Java.expected-{0}-to-have-item-tag-but-found-none", new Object[]{tag.getName()}));
                }
                ((Collection)object).add(this.read(itemTag, Object.class));
            }
        } else {
            this.read(tag, object, this.specElements(object.getClass()));
        }
        return object;
    }

    private void fireCreatingObject(Class<?> c) {
        for (Listener l : this.listeners) {
            l.creatingObject(c);
        }
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    private void setValuesFromTags(Object object, Method setter, Collection<Element> tags) throws Exception {
        for (Element tag : tags) {
            this.setValueFromTag(object, setter, tag);
        }
    }

    private void setValueFromTag(Object object, Method setter, Element tag) throws Exception {
        this.setValue(object, setter, this.read(tag, this.fieldClass(setter)));
    }

    private void setValue(Object object, Method setter, Object value) throws IllegalAccessException, InvocationTargetException {
        setter.invoke(object, value);
    }

    public static interface Listener {
        public void creatingObject(Class<?> var1);
    }
}

