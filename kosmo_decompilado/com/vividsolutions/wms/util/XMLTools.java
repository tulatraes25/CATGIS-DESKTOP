/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.wms.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTools {
    public static void printNode(Node n, String prefix) {
        System.out.println(String.valueOf(prefix) + n.toString());
        NodeList nl = n.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            XMLTools.printNode(nl.item(i), String.valueOf(prefix) + "  ");
            ++i;
        }
    }

    public static Node simpleXPath(Node parent, String xpath) {
        String name;
        String nextPath = null;
        if (xpath.indexOf(47) > 0) {
            name = xpath.substring(0, xpath.indexOf(47));
            nextPath = xpath.substring(xpath.indexOf(47) + 1);
        } else {
            name = xpath;
        }
        NodeList nl = parent.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1 && n.getNodeName().equals(name)) {
                if (nextPath == null) {
                    return n;
                }
                return XMLTools.simpleXPath(n, nextPath);
            }
            ++i;
        }
        return null;
    }
}

