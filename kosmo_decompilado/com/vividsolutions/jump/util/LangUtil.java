/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LangUtil {
    private static Map<Class<?>, Class<?>> primitiveToWrapperMap = new HashMap<Class<?>, Class<?>>(){
        private static final long serialVersionUID = 1L;
        {
            this.put(Byte.TYPE, Byte.class);
            this.put(Character.TYPE, Character.class);
            this.put(Short.TYPE, Short.class);
            this.put(Integer.TYPE, Integer.class);
            this.put(Long.TYPE, Long.class);
            this.put(Float.TYPE, Float.class);
            this.put(Double.TYPE, Double.class);
            this.put(Boolean.TYPE, Boolean.class);
        }
    };

    public static String emptyStringIfNull(String s) {
        return s == null ? "" : s;
    }

    public static Object ifNull(Object o, Object alternative) {
        return o == null ? alternative : o;
    }

    public static Object ifNotNull(Object o, Object alternative) {
        return o != null ? alternative : o;
    }

    public static Class<?> toPrimitiveWrapperClass(Class<?> primitiveClass) {
        return primitiveToWrapperMap.get(primitiveClass);
    }

    public static boolean isPrimitive(Class<?> c) {
        return primitiveToWrapperMap.containsKey(c);
    }

    public static boolean bothNullOrEqual(Object a, Object b) {
        return a == null && b == null || a != null && b != null && a.equals(b);
    }

    public static Object newInstance(Class<?> c) {
        try {
            return c.newInstance();
        }
        catch (Exception e) {
            Assert.shouldNeverReachHere((String)e.toString());
            return null;
        }
    }

    public static Collection<Class<?>> classesAndInterfaces(Class<?> c) {
        ArrayList classesAndInterfaces = new ArrayList();
        classesAndInterfaces.add(c);
        LangUtil.superclasses(c, classesAndInterfaces);
        classesAndInterfaces.addAll(Arrays.asList(c.getInterfaces()));
        return classesAndInterfaces;
    }

    private static void superclasses(Class<?> c, Collection<Class<?>> results) {
        if (c.getSuperclass() == null) {
            return;
        }
        results.add(c.getSuperclass());
        LangUtil.superclasses(c.getSuperclass(), results);
    }
}

