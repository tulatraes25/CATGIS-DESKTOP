/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.List;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;

public class StyleImpl
implements Style,
Cloneable {
    private List<FeatureTypeStyle> featureTypeStyleList = new ArrayList<FeatureTypeStyle>();
    private FeatureTypeStyle selectedFeatureTypeStyle = null;
    private String name = "Default Styler";
    private String title = "Default Styler";
    private String abstractText = "";
    private boolean defaultB = false;
    private String selectedFeatureTypeStyleName;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getAbstract() {
        return this.abstractText;
    }

    @Override
    public void setAbstract(String abstractStr) {
        this.abstractText = abstractStr;
    }

    @Override
    public boolean isDefault() {
        return this.defaultB;
    }

    @Override
    public void setDefault(boolean isDefault) {
        this.defaultB = isDefault;
    }

    @Override
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        FeatureTypeStyle[] ret = new FeatureTypeStyleImpl[]{new FeatureTypeStyleImpl()};
        if (this.featureTypeStyleList != null && this.featureTypeStyleList.size() != 0) {
            ret = this.featureTypeStyleList.toArray(new FeatureTypeStyle[0]);
        }
        return ret;
    }

    @Override
    public void setFeatureTypeStyles(FeatureTypeStyle[] featureTypeStyles) {
        this.featureTypeStyleList.clear();
        int i = 0;
        while (i < featureTypeStyles.length) {
            if (featureTypeStyles.length == 1 || featureTypeStyles.length > 1 && !featureTypeStyles[i].isEmptyFeatureTypeStyle()) {
                this.addFeatureTypeStyle(featureTypeStyles[i]);
            }
            ++i;
        }
        if (this.selectedFeatureTypeStyle != null) {
            FeatureTypeStyle trueStyle;
            String name = this.selectedFeatureTypeStyle.getName();
            this.selectedFeatureTypeStyle = trueStyle = this.getFeatureTypeStyle(name);
        } else if (this.selectedFeatureTypeStyleName != null) {
            FeatureTypeStyle trueStyle;
            this.selectedFeatureTypeStyle = trueStyle = this.getFeatureTypeStyle(this.selectedFeatureTypeStyleName);
        }
    }

    @Override
    public void addFeatureTypeStyle(FeatureTypeStyle type) {
        this.featureTypeStyleList.add(type);
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        StyleImpl clone = new StyleImpl();
        clone.setAbstract(this.getAbstract());
        clone.setDefault(this.isDefault());
        clone.setName(this.getName());
        clone.setTitle(this.getTitle());
        FeatureTypeStyle[] ftsArray = new FeatureTypeStyle[this.featureTypeStyleList.size()];
        int i = 0;
        while (i < ftsArray.length) {
            FeatureTypeStyle fts = this.featureTypeStyleList.get(i);
            ftsArray[i] = (FeatureTypeStyle)((FeatureTypeStyleImpl)fts).clone();
            ++i;
        }
        clone.setFeatureTypeStyles(ftsArray);
        if (this.selectedFeatureTypeStyle != null && this.selectedFeatureTypeStyle.getName() != null) {
            clone.setSelectedFeatureTypeStyle(this.getSelectedFeatureTypeStyle().getName());
        }
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.featureTypeStyleList != null) {
            result = 1000003 * result + this.featureTypeStyleList.hashCode();
        }
        if (this.abstractText != null) {
            result = 1000003 * result + this.abstractText.hashCode();
        }
        if (this.name != null) {
            result = 1000003 * result + this.name.hashCode();
        }
        if (this.title != null) {
            result = 1000003 * result + this.title.hashCode();
        }
        result = 1000003 * result + (this.defaultB ? 1 : 0);
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof StyleImpl) {
            StyleImpl other = (StyleImpl)oth;
            return Utilities.equals((Object)this.name, (Object)other.name) && Utilities.equals((Object)this.title, (Object)other.title) && Utilities.equals((Object)this.abstractText, (Object)other.abstractText) && Utilities.equals(this.featureTypeStyleList, other.featureTypeStyleList);
        }
        return false;
    }

    @Override
    public void setSelectedFeatureTypeStyle(int pos) {
        if (pos < this.featureTypeStyleList.size()) {
            this.selectedFeatureTypeStyle = this.featureTypeStyleList.get(pos);
        }
    }

    @Override
    public FeatureTypeStyle getSelectedFeatureTypeStyle() {
        if (this.selectedFeatureTypeStyle == null || !this.featureTypeStyleList.contains(this.selectedFeatureTypeStyle)) {
            this.selectedFeatureTypeStyle = this.featureTypeStyleList.get(0);
        }
        return this.selectedFeatureTypeStyle;
    }

    @Override
    public void setSelectedFeatureTypeStyle(String featureTypeStyleName) {
        boolean enc = false;
        int i = 0;
        while (i < this.featureTypeStyleList.size() && !enc) {
            FeatureTypeStyle element = this.featureTypeStyleList.get(i);
            if (element.getName().equals(featureTypeStyleName)) {
                this.selectedFeatureTypeStyle = element;
                enc = true;
            }
            ++i;
        }
    }

    @Override
    public void setSelectedFeatureTypeStyle(FeatureTypeStyle featureTypeStyle) {
        this.selectedFeatureTypeStyle = featureTypeStyle;
        if (this.selectedFeatureTypeStyle != null) {
            String name = this.selectedFeatureTypeStyle.getName();
            FeatureTypeStyle trueStyle = this.getFeatureTypeStyle(name);
            if (trueStyle != null) {
                this.selectedFeatureTypeStyle = trueStyle;
            } else {
                this.selectedFeatureTypeStyleName = name;
            }
        }
    }

    @Override
    public FeatureTypeStyle getFeatureTypeStyle(String name) {
        boolean enc = false;
        FeatureTypeStyle findFeatureTypeStyle = null;
        int i = 0;
        while (i < this.featureTypeStyleList.size() && !enc) {
            FeatureTypeStyle element = this.featureTypeStyleList.get(i);
            if (element.getName().equals(name)) {
                findFeatureTypeStyle = element;
                enc = true;
            }
            ++i;
        }
        if (enc) {
            return findFeatureTypeStyle;
        }
        return null;
    }
}

