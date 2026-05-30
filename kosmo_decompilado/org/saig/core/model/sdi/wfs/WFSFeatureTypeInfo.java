/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 */
package org.saig.core.model.sdi.wfs;

import java.net.URI;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToWFSQueryTranslator;

public class WFSFeatureTypeInfo {
    private static final Logger LOGGER = Logger.getLogger(WFSFeatureTypeInfo.class);
    private QualifiedName name;
    private String title;
    private List<String> availableAttributes;
    private List<String> selectedAttributes;
    private List<QualifiedName> geometryAttributes;
    private URI[] availableSRS;
    private FormatType[] availableFormats;
    private String pkName;
    private QualifiedName geomAttrName;
    private URI selectedSRS;
    private FormatType selectedFormat;
    private String serviceVersion;
    private int numMaxFeatures;
    private Filter queryFilter;
    private boolean enabled;

    public WFSFeatureTypeInfo(QualifiedName ftName, List<String> attributes, List<QualifiedName> geomAttributes, String ftTitle) {
        this.name = ftName;
        this.availableAttributes = attributes;
        this.geometryAttributes = geomAttributes;
        this.enabled = true;
        this.title = StringUtils.isNotEmpty((String)ftTitle) ? ftTitle : ftName.getLocalName();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public QualifiedName getName() {
        return this.name;
    }

    public void setName(QualifiedName name) {
        this.name = name;
    }

    public String getLocalName() {
        return this.name.getLocalName();
    }

    public List<String> getAvailableAttributes() {
        return this.availableAttributes;
    }

    public void setAvailableAttributes(List<String> availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    public List<String> getSelectedAttributes() {
        return this.selectedAttributes;
    }

    public void setSelectedAttributes(List<String> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

    public List<QualifiedName> getGeometryAttributes() {
        return this.geometryAttributes;
    }

    public void setGeometryAttributes(List<QualifiedName> geometryAttributes) {
        this.geometryAttributes = geometryAttributes;
    }

    public URI[] getAvailableSRS() {
        return this.availableSRS;
    }

    public void setAvailableSRS(URI[] availableSRS) {
        this.availableSRS = availableSRS;
    }

    public FormatType[] getAvailableFormats() {
        return this.availableFormats;
    }

    public void setAvailableFormats(FormatType[] availableFormats) {
        this.availableFormats = availableFormats;
    }

    public String getPkName() {
        return this.pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public QualifiedName getGeomAttrName() {
        return this.geomAttrName;
    }

    public void setGeomAttrName(QualifiedName geomAttrName) {
        this.geomAttrName = geomAttrName;
    }

    public URI getSelectedSRS() {
        return this.selectedSRS;
    }

    public void setSelectedSRS(URI selectedSRS) {
        this.selectedSRS = selectedSRS;
    }

    public FormatType getSelectedFormat() {
        return this.selectedFormat;
    }

    public void setSelectedFormat(FormatType selectedFormat) {
        this.selectedFormat = selectedFormat;
    }

    public String getPrefix() {
        return this.name.getPrefix();
    }

    public URI getNamespace() {
        return this.name.getNamespace();
    }

    public String getServiceVersion() {
        return this.serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public int getNumMaxFeatures() {
        return this.numMaxFeatures;
    }

    public void setNumMaxFeatures(int numMaxFeatures) {
        this.numMaxFeatures = numMaxFeatures;
    }

    public String buildRequest() {
        return this.buildRequest(false);
    }

    public String buildRequest(boolean testNumberOfHits) {
        StringBuffer request = new StringBuffer();
        request.append("<wfs:GetFeature ");
        request.append("service=\"WFS\" ");
        request.append("version=\"").append(this.getServiceVersion()).append("\" ");
        request.append("xmlns:wfs=\"http://www.opengis.net/wfs\" ");
        request.append("xmlns:ogc=\"http://www.opengis.net/ogc\" ");
        request.append("xmlns:gml=\"http://www.opengis.net/gml\" ");
        request.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        request.append("xsi:schemaLocation=\"http://www.opengis.net/wfs ");
        if (this.getServiceVersion().equals("1.1.0")) {
            request.append("http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
        } else {
            request.append("http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd");
        }
        request.append("\" ");
        if (!testNumberOfHits) {
            request.append("maxFeatures=\"").append(this.getNumMaxFeatures()).append("\" ");
        }
        request.append("outputFormat=\"").append(this.getSelectedFormat().getValue()).append("\"");
        if (testNumberOfHits) {
            request.append(" resultType=\"hits\"");
        }
        request.append(">");
        request.append("<wfs:Query ");
        String prefix = this.getPrefix();
        String namespace = "";
        if (this.getNamespace() != null) {
            namespace = this.getNamespace().toString();
        }
        if (!StringUtils.isEmpty((String)prefix)) {
            request.append("xmlns:").append(prefix).append("=\"").append(namespace);
            request.append("\" ");
        }
        if (!this.getServiceVersion().equals("1.0.0")) {
            request.append("srsName=\"").append(this.getSelectedSRS()).append("\" ");
        }
        request.append("typeName=\"");
        if (!StringUtils.isEmpty((String)prefix)) {
            request.append(prefix).append(":");
        }
        request.append(this.getLocalName()).append("\">");
        String openPropertyName = this.getServiceVersion() != "1.0.0" ? "<wfs:PropertyName>" : "<ogc:PropertyName>";
        String closedPropertyName = this.getServiceVersion() != "1.0.0" ? "</wfs:PropertyName>" : "</ogc:PropertyName>";
        for (String currentAttr : this.selectedAttributes) {
            request.append(openPropertyName);
            if (!StringUtils.isEmpty((String)this.getPrefix())) {
                request.append(prefix).append(":");
            }
            request.append(currentAttr).append(closedPropertyName);
        }
        if (this.geomAttrName != null) {
            request.append(openPropertyName);
            if (!StringUtils.isEmpty((String)this.getPrefix())) {
                request.append(prefix).append(":");
            }
            request.append(this.geomAttrName.getLocalName()).append(closedPropertyName);
        }
        if (this.queryFilter != null) {
            FilterToWFSQueryTranslator translator = new FilterToWFSQueryTranslator(prefix, namespace, this.getSelectedFormat(), this.getSelectedSRS());
            request.append(translator.translateFilter(this.queryFilter));
        }
        request.append("</wfs:Query></wfs:GetFeature>");
        LOGGER.info((Object)("Creating REQUEST:\n" + request.toString()));
        return request.toString();
    }

    public Filter getQueryFilter() {
        return this.queryFilter;
    }

    public void setQueryFilter(Filter queryFilter) {
        this.queryFilter = queryFilter;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrettyString() {
        return String.valueOf(this.getTitle()) + " [" + this.name.getPrefixedName() + "]";
    }
}

