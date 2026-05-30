/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.Collator
 *  org.apache.commons.lang.StringUtils
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.ibm.icu.text.Collator;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.saig.core.util.LocaleManager;

public class WFSFeatureTypeDescription
implements Comparable<WFSFeatureTypeDescription> {
    protected WFSFeatureType wfsFT;

    public WFSFeatureTypeDescription(WFSFeatureType ft) {
        this.wfsFT = ft;
    }

    public String getName() {
        return this.wfsFT.getName().getLocalName();
    }

    public String getQualifiedName() {
        return this.wfsFT.getName().getPrefixedName();
    }

    public String getTitle() {
        String title = this.wfsFT.getTitle();
        if (StringUtils.isNotEmpty((String)title)) {
            return title;
        }
        return this.getName();
    }

    public String getAbstract() {
        return this.wfsFT.getAbstract();
    }

    @Override
    public int compareTo(WFSFeatureTypeDescription o) {
        return Collator.getInstance((Locale)LocaleManager.getActiveLocale()).compare(this.getTitle(), o.getTitle());
    }
}

