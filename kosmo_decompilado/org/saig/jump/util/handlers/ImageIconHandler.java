/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.GeneralizedFieldHandler
 */
package org.saig.jump.util.handlers;

import javax.swing.ImageIcon;
import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class ImageIconHandler
extends GeneralizedFieldHandler {
    public Object convertUponGet(Object arg) {
        return ((ImageIcon)arg).toString();
    }

    public Object convertUponSet(Object arg) {
        return new ImageIcon((String)arg);
    }

    public Class<?> getFieldType() {
        return ImageIcon.class;
    }
}

