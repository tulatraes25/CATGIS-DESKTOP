/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.WKTGraphic;

public interface ExternalGraphic
extends Symbol {
    public void setURI(String var1);

    public URL getLocation() throws MalformedURLException;

    public void setLocation(URL var1);

    public String getFormat();

    public void setFormat(String var1);

    public void setCustomProperties(Map<String, Object> var1);

    public Map<String, Object> getCustomProperties();

    public void setWKTGraphic(WKTGraphic var1);

    public WKTGraphic getWKTGraphic();

    public String getUri();
}

