/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Filter;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LogicFilter;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Rule;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;
import org.saig.core.util.LocaleManager;

public class RuleImpl
implements Rule,
Cloneable,
Comparable<Rule> {
    private static final String DEFAULT_TITLE = "title";
    private boolean enabled = true;
    private List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
    private List<Graphic> graphics = new ArrayList<Graphic>();
    private String name = "name";
    private String abstractStr = "";
    private Filter filter = null;
    private boolean elseFilter = false;
    private double maxScaleDenominator = Double.POSITIVE_INFINITY;
    private double minScaleDenominator = 0.0;
    private boolean geometryFilter = false;
    private Map<Locale, String> titleByLang = new HashMap<Locale, String>();

    public RuleImpl() {
    }

    public RuleImpl(Symbolizer[] symbolizers) {
        this.symbolizers.addAll(Arrays.asList(symbolizers));
    }

    @Override
    public Graphic[] getLegendGraphic() {
        return this.graphics.toArray(new Graphic[0]);
    }

    public void addLegendGraphic(Graphic graphic) {
        this.graphics.add(graphic);
    }

    @Override
    public void setLegendGraphic(Graphic[] graphics) {
        this.graphics.clear();
        int i = 0;
        while (i < graphics.length) {
            this.addLegendGraphic(graphics[i]);
            ++i;
        }
    }

    public void addSymbolizer(Symbolizer symb) {
        this.symbolizers.add(symb);
    }

    @Override
    public void setSymbolizers(Symbolizer[] syms) {
        this.symbolizers.clear();
        int i = 0;
        while (i < syms.length) {
            this.addSymbolizer(syms[i]);
            ++i;
        }
    }

    @Override
    public Symbolizer[] getSymbolizers() {
        return this.symbolizers.toArray(new Symbolizer[this.symbolizers.size()]);
    }

    @Override
    public String getAbstract() {
        return this.abstractStr;
    }

    @Override
    public void setAbstract(String abstractStr) {
        this.abstractStr = abstractStr;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (MapUtils.isEmpty(this.titleByLang)) {
            this.setTitle(name, LocaleManager.getActiveLocale());
        }
    }

    @Override
    public String getTitle() {
        return this.getTitle(LocaleManager.getActiveLocale());
    }

    @Override
    public void setTitle(String title) {
        this.setTitle(title, LocaleManager.getActiveLocale());
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.geometryFilter = this.checkGeometryFilter();
    }

    @Override
    public boolean isElseFilter() {
        return this.elseFilter;
    }

    @Override
    public void setElseFilter(boolean flag) {
        this.elseFilter = flag;
    }

    public void setHasElseFilter() {
        this.elseFilter = true;
    }

    @Override
    public double getMaxScaleDenominator() {
        return this.maxScaleDenominator;
    }

    @Override
    public void setMaxScaleDenominator(double maxScaleDenominator) {
        this.maxScaleDenominator = maxScaleDenominator;
    }

    @Override
    public double getMinScaleDenominator() {
        return this.minScaleDenominator;
    }

    @Override
    public void setMinScaleDenominator(double minScaleDenominator) {
        this.minScaleDenominator = minScaleDenominator;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        int i;
        RuleImpl clone = new RuleImpl();
        clone.graphics = new ArrayList<Graphic>();
        clone.symbolizers = new ArrayList<Symbolizer>();
        if (this.graphics != null) {
            Graphic[] legends = new Graphic[this.graphics.size()];
            i = 0;
            while (i < legends.length) {
                Graphic legend = this.graphics.get(i);
                legends[i] = (Graphic)((Cloneable)legend).clone();
                ++i;
            }
            clone.setLegendGraphic(legends);
        }
        if (this.symbolizers != null) {
            Symbolizer[] symbArray = new Symbolizer[this.symbolizers.size()];
            i = 0;
            while (i < symbArray.length) {
                Symbolizer symb = this.symbolizers.get(i);
                symbArray[i] = (Symbolizer)((Cloneable)symb).clone();
                ++i;
            }
            clone.setSymbolizers(symbArray);
        }
        clone.setAbstract(this.getAbstract());
        clone.setElseFilter(this.isElseFilter());
        clone.setEnabled(this.isEnabled());
        if (this.filter != null) {
            clone.setFilter((Filter)((Cloneable)this.filter).clone());
        }
        clone.setMaxScaleDenominator(this.getMaxScaleDenominator());
        clone.setMinScaleDenominator(this.getMinScaleDenominator());
        clone.setName(this.getName());
        HashMap<Locale, String> titleByLangClone = new HashMap<Locale, String>();
        for (Locale key : this.titleByLang.keySet()) {
            titleByLangClone.put(key, this.titleByLang.get(key));
        }
        clone.setTitleByLang(titleByLangClone);
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        result = 1000003 * result + this.symbolizers.hashCode();
        if (this.graphics != null) {
            result = 1000003 * result + this.graphics.hashCode();
        }
        if (this.name != null) {
            result = 1000003 * result + this.name.hashCode();
        }
        if (this.abstractStr != null) {
            result = 1000003 * result + this.abstractStr.hashCode();
        }
        if (this.filter != null) {
            result = 1000003 * result + this.filter.hashCode();
        }
        result = 1000003 * result + (this.elseFilter ? 1 : 0);
        long temp = Double.doubleToLongBits(this.maxScaleDenominator);
        result = 1000003 * result + (int)(temp >>> 32);
        result = 1000003 * result + (int)(temp & 0xFFFFFFFFFFFFFFFFL);
        temp = Double.doubleToLongBits(this.minScaleDenominator);
        result = 1000003 * result + (int)(temp >>> 32);
        result = 1000003 * result + (int)(temp & 0xFFFFFFFFFFFFFFFFL);
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof RuleImpl) {
            RuleImpl other = (RuleImpl)oth;
            return Utilities.equals((Object)this.name, (Object)other.name) && Utilities.equals((Object)this.abstractStr, (Object)other.abstractStr) && Utilities.equals((Object)this.filter, (Object)other.filter) && this.elseFilter == other.elseFilter && Utilities.equals(this.graphics, other.graphics) && Utilities.equals(this.symbolizers, other.symbolizers) && Double.doubleToLongBits(this.maxScaleDenominator) == Double.doubleToLongBits(other.maxScaleDenominator) && Double.doubleToLongBits(this.minScaleDenominator) == Double.doubleToLongBits(other.minScaleDenominator);
        }
        return false;
    }

    private boolean checkGeometryFilter() {
        if (this.filter != null) {
            if (this.filter instanceof GeometryFilter) {
                return true;
            }
            if (this.filter instanceof LogicFilter) {
                return this.logicFilterContainsGeometryFilter((LogicFilter)this.filter);
            }
        }
        return false;
    }

    private boolean logicFilterContainsGeometryFilter(LogicFilter filter) {
        Iterator<Filter> iter = filter.getFilterIterator();
        while (iter.hasNext()) {
            Filter element = iter.next();
            if (element instanceof GeometryFilter) {
                return true;
            }
            if (!(element instanceof LogicFilter)) continue;
            return this.logicFilterContainsGeometryFilter((LogicFilter)element);
        }
        return false;
    }

    @Override
    public boolean isGeometryFilter() {
        return this.geometryFilter;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String toString() {
        return this.getName();
    }

    @Override
    public int compareTo(Rule other) {
        if (this.getTitle() != null) {
            return this.getTitle().compareTo(other.getTitle());
        }
        if (other.getName() != null) {
            return -1 * other.getTitle().compareTo(this.getTitle());
        }
        if (this.getName() != null) {
            return this.getName().compareTo(other.getName());
        }
        if (other.getName() != null) {
            return -1 * other.getName().compareTo(this.getName());
        }
        return -1;
    }

    @Override
    public String getTitle(Locale locale) {
        if (!this.titleByLang.containsKey(locale)) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public void setTitle(String title, Locale locale) {
        this.titleByLang.put(locale, title);
    }

    @Override
    public Map<Locale, String> getTitleByLang() {
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale)) {
            this.titleByLang.put(locale, DEFAULT_TITLE);
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
        }
    }
}

