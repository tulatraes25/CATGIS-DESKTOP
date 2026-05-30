/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;

public class LikeFilterImpl
extends AbstractFilterImpl
implements LikeFilter,
Cloneable {
    private Expression attribute = null;
    private String pattern = null;
    private String wildcardSingle = ".?";
    private String wildcardMulti = ".*";
    private String escape = "\\";
    private String ignoreCaseFlag = "(?i)";
    private Pattern compPattern = null;
    private Matcher match = null;

    private Matcher getMatcher() {
        if (this.match == null) {
            String pattern1 = new String(this.pattern);
            String wildcardMulti1 = new String(this.wildcardMulti);
            String wildcardSingle1 = new String(this.wildcardSingle);
            String ignoreCaseFlag1 = new String(this.ignoreCaseFlag);
            String escape1 = new String(this.escape);
            char esc = escape1.charAt(0);
            LOGGER.debug((Object)("wildcard " + wildcardMulti1 + " single " + wildcardSingle1));
            LOGGER.debug((Object)("escape " + escape1 + " esc " + esc + " esc == \\ " + (esc == '\\')));
            String escapedWildcardMulti = this.fixSpecials(wildcardMulti1);
            String escapedWildcardSingle = this.fixSpecials(wildcardSingle1);
            String escapedIgnoreCaseFlag = this.fixSpecials(this.ignoreCaseFlag);
            StringBuffer tmp = new StringBuffer("");
            boolean escapedMode = false;
            int i = 0;
            while (i < pattern1.length()) {
                char chr = pattern1.charAt(i);
                LOGGER.debug((Object)("tmp = " + tmp + " looking at " + chr));
                if (pattern1.regionMatches(false, i, escape1, 0, escape1.length())) {
                    LOGGER.debug((Object)"escape ");
                    escapedMode = true;
                    chr = pattern1.charAt(i += escape1.length());
                }
                if (pattern1.regionMatches(false, i, wildcardMulti1, 0, wildcardMulti1.length())) {
                    LOGGER.debug((Object)"multi wildcard");
                    if (escapedMode) {
                        LOGGER.debug((Object)"escaped ");
                        tmp.append(escapedWildcardMulti);
                    } else {
                        tmp.append(".*");
                    }
                    i += wildcardMulti1.length() - 1;
                    escapedMode = false;
                } else if (pattern1.regionMatches(false, i, wildcardSingle1, 0, wildcardSingle1.length())) {
                    LOGGER.debug((Object)"single wildcard");
                    if (escapedMode) {
                        LOGGER.debug((Object)"escaped ");
                        tmp.append(escapedWildcardSingle);
                    } else {
                        tmp.append(".?");
                    }
                    i += wildcardSingle1.length() - 1;
                    escapedMode = false;
                } else if (pattern1.regionMatches(false, i, ignoreCaseFlag1, 0, ignoreCaseFlag1.length())) {
                    LOGGER.debug((Object)"ignore case flag");
                    if (escapedMode) {
                        LOGGER.debug((Object)"escaped ");
                        tmp.append(escapedIgnoreCaseFlag);
                    } else {
                        tmp.append("(?i)");
                    }
                    i += ignoreCaseFlag1.length() - 1;
                    escapedMode = false;
                } else if (this.isSpecial(chr)) {
                    LOGGER.debug((Object)"special");
                    tmp.append(String.valueOf(this.escape) + chr);
                    escapedMode = false;
                } else {
                    tmp.append(chr);
                    escapedMode = false;
                }
                ++i;
            }
            pattern1 = tmp.toString();
            LOGGER.debug((Object)("final pattern " + pattern1));
            this.compPattern = Pattern.compile(pattern1);
            this.match = this.compPattern.matcher("");
        }
        return this.match;
    }

    public LikeFilterImpl() {
        this.filterType = (short)20;
    }

    @Override
    public void setValue(Expression attribute) throws IllegalFilterException {
        if (attribute.getType() == 111 && !this.permissiveConstruction) {
            throw new IllegalFilterException("Attempted to add something other than a string attribute expression to a like filter.");
        }
        this.attribute = attribute;
    }

    @Override
    public Expression getValue() {
        return this.attribute;
    }

    @Override
    public void setPattern(Expression p, String wildcardMulti, String wildcardSingle, String escape) {
        this.setPattern(p.toString(), wildcardMulti, wildcardSingle, escape);
    }

    @Override
    public void setPattern(String pattern, String wildcardMulti, String wildcardSingle, String escape) {
        this.match = null;
        this.pattern = pattern;
        this.wildcardMulti = wildcardMulti;
        this.wildcardSingle = wildcardSingle;
        this.escape = escape;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public boolean contains(Feature feature) {
        if (this.attribute == null) {
            return false;
        }
        Object value = this.attribute.getValue(feature);
        if (value == null) {
            return false;
        }
        Matcher matcher = this.getMatcher();
        matcher.reset(value.toString());
        boolean matches = false;
        try {
            matches = matcher.matches();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return matches;
    }

    public String toString() {
        return "like (" + this.attribute.toString() + ",'" + this.pattern + "')";
    }

    @Override
    public String getEscape() {
        return this.escape;
    }

    @Override
    public String getWildcardMulti() {
        return this.wildcardMulti;
    }

    @Override
    public String getWildcardSingle() {
        return this.wildcardSingle;
    }

    private boolean isSpecial(char chr) {
        return chr == '.' || chr == '?' || chr == '*' || chr == '^' || chr == '$' || chr == '+' || chr == '[' || chr == ']' || chr == '(' || chr == ')' || chr == '|' || chr == '\\' || chr == '&';
    }

    private String fixSpecials(String inString) {
        StringBuffer tmp = new StringBuffer("");
        int i = 0;
        while (i < inString.length()) {
            char chr = inString.charAt(i);
            if (this.isSpecial(chr)) {
                tmp.append(String.valueOf(this.escape) + chr);
            } else {
                tmp.append(chr);
            }
            ++i;
        }
        return tmp.toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof LikeFilterImpl) {
            LikeFilterImpl lFilter = (LikeFilterImpl)obj;
            return lFilter.getFilterType() == this.filterType && lFilter.getValue().equals(this.attribute) && lFilter.getPattern().equals(this.pattern);
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.attribute == null ? 0 : this.attribute.hashCode());
        result = 37 * result + (this.pattern == null ? 0 : this.pattern.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public void setPattern(String pattern) {
        this.match = null;
        this.pattern = pattern;
    }

    public void setWildcardMulti(String wildcardMulti) {
        this.wildcardMulti = wildcardMulti;
    }

    public void setWildcardSingle(String wildcardSingle) {
        this.wildcardSingle = wildcardSingle;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    public Object clone() {
        LikeFilterImpl clone = new LikeFilterImpl();
        clone.setPattern(this.getPattern());
        clone.attribute = this.getValue();
        clone.setWildcardMulti(this.getWildcardMulti());
        clone.setWildcardSingle(this.getWildcardSingle());
        clone.setEscape(this.getEscape());
        return clone;
    }
}

