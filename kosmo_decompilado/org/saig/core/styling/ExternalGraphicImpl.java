/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.WKTGraphic;

public class ExternalGraphicImpl
implements ExternalGraphic,
Symbol,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling");
    private URL location = null;
    private String format = null;
    private String uri = null;
    private Map<String, Object> customProps = null;
    private WKTGraphic wktGraphic;

    @Override
    public void setURI(String uri) {
        this.uri = uri;
    }

    @Override
    public String getFormat() {
        return this.format;
    }

    @Override
    public URL getLocation() throws MalformedURLException {
        if (this.location == null) {
            this.location = new URL(this.uri);
        }
        return this.location;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public void setLocation(URL location) {
        this.uri = location != null ? location.toString() : null;
        this.location = location;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        ExternalGraphicImpl clone = new ExternalGraphicImpl();
        if (this.customProps != null) {
            HashMap<String, Object> cloneProperties = new HashMap<String, Object>();
            for (String key : this.customProps.keySet()) {
                Object value = this.customProps.get(key);
                cloneProperties.put(key, value);
            }
            clone.setCustomProperties(cloneProperties);
        }
        clone.wktGraphic = this.getWKTGraphic();
        clone.setFormat(this.getFormat());
        clone.setLocation(this.location);
        clone.setURI(this.uri);
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.format != null) {
            result = 1000003 * result + this.format.hashCode();
        }
        if (this.uri != null) {
            result = 1000003 * result + this.uri.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof ExternalGraphicImpl) {
            ExternalGraphicImpl other = (ExternalGraphicImpl)oth;
            return Utilities.equals((Object)this.uri, (Object)other.uri) && Utilities.equals((Object)this.format, (Object)other.format);
        }
        return false;
    }

    @Override
    public Map<String, Object> getCustomProperties() {
        return this.customProps;
    }

    @Override
    public void setCustomProperties(Map<String, Object> list) {
        this.customProps = list;
    }

    @Override
    public WKTGraphic getWKTGraphic() {
        return this.wktGraphic;
    }

    @Override
    public void setWKTGraphic(WKTGraphic wktGraphic) {
        this.wktGraphic = wktGraphic;
        try {
            URL url = wktGraphic.getImageURL();
            this.setLocation(url);
            this.setFormat("image/png");
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public String getUri() {
        return this.uri;
    }
}

