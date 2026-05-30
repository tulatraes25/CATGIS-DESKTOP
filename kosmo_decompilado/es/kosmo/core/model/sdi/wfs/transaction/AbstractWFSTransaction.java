/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.httpclient.Credentials
 *  org.apache.commons.httpclient.HttpClient
 *  org.apache.commons.httpclient.HttpMethod
 *  org.apache.commons.httpclient.UsernamePasswordCredentials
 *  org.apache.commons.httpclient.auth.AuthScope
 *  org.apache.commons.httpclient.methods.PostMethod
 *  org.apache.commons.httpclient.methods.RequestEntity
 *  org.apache.commons.httpclient.methods.StringRequestEntity
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.util.TimeTools
 *  org.deegree.framework.xml.NamespaceContext
 *  org.deegree.framework.xml.XMLFragment
 *  org.deegree.ogcbase.CommonNamespaces
 */
package es.kosmo.core.model.sdi.wfs.transaction;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.dao.datasource.filedatasource.gml.AbstractGmlGeometryConverter;
import es.kosmo.core.model.sdi.wfs.transaction.WFSTransactionFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcbase.CommonNamespaces;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.sdi.wfs.WFSFeature;
import org.saig.core.model.sdi.wfs.WFSFeatureDataset;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.lang.I18N;

public abstract class AbstractWFSTransaction {
    private static final Logger LOGGER = Logger.getLogger(AbstractWFSTransaction.class);
    protected String crs = "-1";
    public static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
    public WFSFeatureTypeInfo info;
    public AbstractWFSWrapper service;
    protected AbstractGmlGeometryConverter geomConverter;

    protected AbstractWFSTransaction(WFSFeatureTypeInfo wfti, AbstractWFSWrapper serv) {
        this.info = wfti;
        this.service = serv;
    }

    public boolean validate(StringBuffer xmlRequest) {
        boolean valid = false;
        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema();
            Validator validator = schema.newValidator();
            StreamSource source = new StreamSource(new StringReader(xmlRequest.toString()));
            validator.validate(source);
            LOGGER.info((Object)"WFS request is valid");
        }
        catch (Exception ex) {
            LOGGER.warn((Object)("WFS request is not valid because " + ex.getMessage()));
        }
        return valid;
    }

    public XMLFragment doTransaction(String xmlRequest) throws Exception {
        XMLFragment result = new XMLFragment();
        if (LOGGER.isDebugEnabled()) {
            try {
                XMLFragment d = new XMLFragment((Reader)new StringReader(xmlRequest), "http://www.debug.org");
                LOGGER.debug((Object)(String.valueOf(I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.wfs-t-request-to-{0}", new Object[]{this.service.getGetFeatureURL()})) + ":\n" + d.getAsPrettyString() + "\n"));
            }
            catch (Exception e) {
                LOGGER.debug((Object)I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.oh-oh-generated-string-request-was-not-xml-{0}", new Object[]{xmlRequest}));
            }
        }
        HttpClient httpClient = new HttpClient();
        Properties systemSettings = System.getProperties();
        if (systemSettings != null) {
            String proxySet = systemSettings.getProperty("http.proxySet", "false");
            if (StringUtils.isNotEmpty((String)proxySet) && proxySet.equals("true")) {
                String proxyHost = systemSettings.getProperty("http.proxyHost");
                String proxyPort = systemSettings.getProperty("http.proxyPort");
                httpClient.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort).intValue());
                String proxyUserName = systemSettings.getProperty("http.proxyUserName");
                String proxyPassword = systemSettings.getProperty("http.proxyPassword");
                if (StringUtils.isNotEmpty((String)proxyUserName)) {
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUserName, proxyPassword);
                    AuthScope scope = new AuthScope(AuthScope.ANY_HOST, -1);
                    httpClient.getState().setProxyCredentials(scope, (Credentials)credentials);
                }
                String nonProxyHosts = systemSettings.getProperty("http.nonProxyHosts");
                LOGGER.info((Object)("Proxy settings: host='" + proxyHost + "' port='" + proxyPort + "' " + " user='" + proxyUserName + "' password='" + proxyPassword + "'"));
                LOGGER.info((Object)("Non proxy hosts=" + nonProxyHosts));
            } else {
                LOGGER.info((Object)"No proxy configuration set");
            }
        }
        PostMethod httppost = new PostMethod(this.service.getGetFeatureURL());
        String requestedCharset = "utf-8";
        httppost.setRequestEntity((RequestEntity)new StringRequestEntity(xmlRequest, "text/xml", requestedCharset));
        if (this.service.getBasicAuthData() != null) {
            LOGGER.info((Object)"Setting WFS Basic Auth credentials from user options");
            UsernamePasswordCredentials defaultcreds = new UsernamePasswordCredentials(this.service.getBasicAuthData().getUserName(), this.service.getBasicAuthData().getPassword());
            httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1), (Credentials)defaultcreds);
        } else {
            LOGGER.info((Object)"No authentification set");
        }
        int code = httpClient.executeMethod((HttpMethod)httppost);
        if (code != 200) {
            throw new Exception(I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.wfs-server-{0}-responded-incorrectly-code-{1}", new Object[]{this.service.getGetFeatureURL(), code}));
        }
        result.load(httppost.getResponseBodyAsStream(), "http://www.systemid.org");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getMessage(WFSTransactionFactory.class, "wfs-t-result", new Object[]{result.getAsPrettyString()}));
        }
        return result;
    }

    protected abstract String getRequestHeader();

    public StringBuffer createRequest(Collection<Feature> newFeatures, Collection<Feature> updatedFeatures, Collection<Feature> deletedFeatures, boolean useExisting) {
        StringBuffer sb = new StringBuffer();
        this.crs = this.info.getSelectedSRS().toString();
        QualifiedName featureType = this.info.getName();
        if (featureType == null) {
            return sb;
        }
        if (CollectionUtils.isEmpty(newFeatures) && CollectionUtils.isEmpty(updatedFeatures) && CollectionUtils.isEmpty(deletedFeatures)) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.there-are-no-elements-to-deal-with-in-the-current-transaction"));
            return sb;
        }
        sb.append(this.getRequestHeader());
        if (CollectionUtils.isNotEmpty(newFeatures)) {
            this.appendInsert(sb, featureType, this.info.getGeomAttrName(), newFeatures, useExisting);
        }
        if (CollectionUtils.isNotEmpty(updatedFeatures)) {
            this.appendUpdate(sb, featureType, updatedFeatures);
        }
        if (CollectionUtils.isNotEmpty(deletedFeatures)) {
            this.appendDelete(sb, featureType, deletedFeatures);
        }
        sb.append("</wfs:Transaction>");
        return sb;
    }

    protected void appendInsert(StringBuffer sb, QualifiedName featureType, QualifiedName geoPropName, Collection<Feature> features, boolean useExisting) {
        sb.append("<wfs:Insert handle=\"insert1\" idgen=\"" + (useExisting ? "UseExisting" : "GenerateNew") + "\">");
        for (WFSFeature wFSFeature : features) {
            StringBuffer sbComplex = new StringBuffer();
            String s = featureType.getPrefixedName();
            sb.append("<").append(s);
            if (useExisting) {
                String id = wFSFeature.getGMLId();
                sb.append(" gml:id=\"").append(id).append("\"");
            }
            String featTypeXmlns = "xmlns:" + this.info.getPrefix() + "=\"" + this.info.getNamespace() + "\"";
            sb.append(" " + featTypeXmlns);
            sb.append(">");
            sb.append(this.createInsertPropertiesFragment(sbComplex, geoPropName, featureType, wFSFeature));
            sb.append("</").append(s).append(">");
        }
        sb.append("</wfs:Insert>");
    }

    protected void appendUpdate(StringBuffer sb, QualifiedName featureType, Collection<Feature> features) {
        for (Feature feat : features) {
            StringBuffer sbComplex = new StringBuffer();
            sb.append("<wfs:Update typeName=\"").append(featureType.getPrefix()).append(":");
            sb.append(featureType.getLocalName()).append("\">");
            sb.append(this.createPropertiesFragment(sbComplex, featureType, feat));
            if (feat instanceof WFSFeature) {
                sb.append(this.getFeatureIdFilter(((WFSFeature)feat).getGMLId()));
            }
            sb.append("</wfs:Update>");
            sb.append(sbComplex.toString());
        }
    }

    protected void appendDelete(StringBuffer sb, QualifiedName featureType, Collection<Feature> features) {
        Iterator<Feature> iter = features.iterator();
        while (iter.hasNext()) {
            sb.append("<wfs:Delete typeName=\"").append(featureType.getPrefix()).append(":");
            sb.append(featureType.getLocalName()).append("\">");
            Feature feat = iter.next();
            if (feat instanceof WFSFeature) {
                sb.append(this.getFeatureIdFilter(((WFSFeature)feat).getGMLId()));
            }
            sb.append("</wfs:Delete>");
        }
    }

    protected abstract String getFeatureIdFilter(String var1);

    protected StringBuffer createInsertPropertiesFragment(StringBuffer sbComplex, QualifiedName geoAttName, QualifiedName featureType, Feature bf) {
        StringBuffer sb = new StringBuffer();
        FeatureSchema featSchema = bf.getSchema();
        int numAttributes = featSchema.getAttributeCount();
        int i = 0;
        while (i < numAttributes) {
            Attribute currentAttr = featSchema.getAttribute(i);
            String attrName = currentAttr.getName();
            LOGGER.debug((Object)I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.pondering-about-property-with-name-{0}", new Object[]{attrName}));
            if (!currentAttr.isPrimaryKey()) {
                if (currentAttr.getType() != AttributeType.GEOMETRY) {
                    Object attValue;
                    LOGGER.debug((Object)I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.not-a-geometry"));
                    if (currentAttr.getType() == AttributeType.DATE || currentAttr.getType() == AttributeType.TIMESTAMP || currentAttr.getType() == AttributeType.TIME) {
                        attValue = (Date)bf.getAttribute(attrName);
                        if (attValue != null) {
                            String val = TimeTools.getISOFormattedTime((Date)attValue);
                            sb.append("<").append(featureType.getPrefix()).append(":").append(attrName).append(">");
                            sb.append(val);
                            sb.append("</").append(featureType.getPrefix()).append(":").append(attrName).append(">");
                        }
                    } else {
                        attValue = bf.getAttribute(attrName);
                        if (attValue != null) {
                            if (attValue instanceof WFSFeatureDataset) {
                                WFSFeatureDataset wfsFeatureDataset = (WFSFeatureDataset)attValue;
                                for (Feature feat : wfsFeatureDataset.getNewFeatures()) {
                                    QualifiedName wfsDSFeatType = wfsFeatureDataset.getFeatureTypeName();
                                    sb.append("<").append(featureType.getPrefix()).append(":").append(attrName).append(">");
                                    sb.append("<").append(wfsDSFeatType.getPrefix()).append(":").append(wfsDSFeatType.getLocalName());
                                    if (feat instanceof WFSFeature && ((WFSFeature)feat).getGMLId() != null) {
                                        sb.append(" gml:id=\"" + ((WFSFeature)feat).getGMLId() + "\"");
                                    }
                                    sb.append(">");
                                    sb.append(this.createInsertPropertiesFragment(sbComplex, null, wfsFeatureDataset.getFeatureTypeName(), feat));
                                    sb.append("</").append(wfsDSFeatType.getPrefix()).append(":").append(wfsDSFeatType.getLocalName()).append("> ");
                                    sb.append("</").append(featureType.getPrefix()).append(":").append(attrName).append("> ");
                                }
                            } else {
                                sb.append("<").append(featureType.getPrefix()).append(":").append(attrName).append(">");
                                sb.append(attValue);
                                sb.append("</").append(featureType.getPrefix()).append(":").append(attrName).append(">");
                            }
                        }
                    }
                } else {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.it-is-a-geometr\u00eda"));
                    LOGGER.debug((Object)(attrName.equals("GEOMETRY") ? I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.schema-not-loaded-using-strange-mechanisms-here") : I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.ok-using-schema")));
                    if (attrName.equals("FAKE_GEOMETRY")) {
                        LOGGER.debug((Object)I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.skipping-fake-geometry"));
                    } else {
                        sb.append("<").append(featureType.getPrefix()).append(":");
                        sb.append(attrName.equals("GEOMETRY") ? geoAttName.getLocalName() : attrName).append(">");
                        sb.append(this.createGeometryGML(bf.getGeometry()));
                        sb.append("</").append(featureType.getPrefix()).append(":");
                        sb.append(attrName.equals("GEOMETRY") ? geoAttName.getLocalName() : attrName).append(">");
                    }
                }
            }
            ++i;
        }
        return sb;
    }

    protected StringBuffer createPropertiesFragment(StringBuffer sbComplex, QualifiedName featureType, Feature bf) {
        StringBuffer sb = new StringBuffer();
        Map<String, Object> attributes = bf.getAttributes();
        FeatureSchema fs = bf.getSchema();
        for (String attrName : attributes.keySet()) {
            Attribute currentAttr = fs.getAttribute(attrName);
            LOGGER.debug((Object)I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.shall-we-insert-attribute-{0}", new Object[]{attrName}));
            if (currentAttr.isPrimaryKey()) continue;
            if (currentAttr.getType() != AttributeType.GEOMETRY) {
                Object attValue;
                LOGGER.debug((Object)I18N.getString("org.saig.core.model.sdi.wfs.WFSTransactionFactory.inserting-modified-attribute"));
                if (currentAttr.getType() == AttributeType.DATE || currentAttr.getType() == AttributeType.TIMESTAMP || currentAttr.getType() == AttributeType.TIME) {
                    attValue = (Date)bf.getAttribute(attrName);
                    if (attValue == null) continue;
                    String val = TimeTools.getISOFormattedTime((Date)attValue);
                    LOGGER.debug((Object)I18N.getMessage("org.saig.core.model.sdi.wfs.WFSTransactionFactory.inserting-date-value-of-{0}", new Object[]{val}));
                    sb.append(this.buildProperty(attrName, featureType, val));
                    continue;
                }
                attValue = bf.getAttribute(attrName);
                if (attValue == null) continue;
                if (attValue instanceof WFSFeatureDataset) {
                    WFSFeatureDataset wfsFeatureDataset = (WFSFeatureDataset)attValue;
                    FeatureIterator itFeats = null;
                    try {
                        try {
                            itFeats = wfsFeatureDataset.iterator();
                            while (itFeats.hasNext()) {
                                Feature feat = itFeats.next();
                                if (wfsFeatureDataset.getNewFeatures().contains(feat)) {
                                    ArrayList<Feature> featsToInsert = new ArrayList<Feature>(1);
                                    featsToInsert.add(feat);
                                    this.appendInsert(sbComplex, wfsFeatureDataset.getFeatureTypeName(), null, featsToInsert, false);
                                    continue;
                                }
                                if (wfsFeatureDataset.getUpdatedFeatures().contains(feat)) {
                                    ArrayList<Feature> featsToUpdate = new ArrayList<Feature>(1);
                                    featsToUpdate.add(feat);
                                    this.appendUpdate(sbComplex, wfsFeatureDataset.getFeatureTypeName(), featsToUpdate);
                                    continue;
                                }
                                sb.append(this.buildFeatureProperty(attrName, wfsFeatureDataset.getFeatureTypeName(), feat, sbComplex));
                            }
                        }
                        catch (Exception ex) {
                            LOGGER.error((Object)"", (Throwable)ex);
                            if (itFeats == null) continue;
                            itFeats.close();
                            continue;
                        }
                    }
                    catch (Throwable throwable) {
                        if (itFeats != null) {
                            itFeats.close();
                        }
                        throw throwable;
                    }
                    if (itFeats == null) continue;
                    itFeats.close();
                    continue;
                }
                sb.append(this.buildProperty(attrName, featureType, attValue));
                continue;
            }
            if (currentAttr.getType() != AttributeType.GEOMETRY) continue;
            LOGGER.debug((Object)I18N.getString(WFSTransactionFactory.class, "inserting-modified-geometry"));
            if (attrName.equals("FAKE_GEOMETRY")) {
                LOGGER.debug((Object)I18N.getString(WFSTransactionFactory.class, "skipping-fake-geometry"));
                continue;
            }
            sb.append("<wfs:Property><wfs:Name>");
            sb.append(featureType.getPrefix());
            sb.append(":").append(attrName);
            sb.append("</wfs:Name><wfs:Value>");
            sb.append(this.createGeometryGML(bf.getGeometry()));
            sb.append("</wfs:Value></wfs:Property>");
        }
        return sb;
    }

    protected String buildFeatureProperty(String attrName, QualifiedName featType, Feature feat, StringBuffer sbComplex) {
        StringBuffer attrValue = new StringBuffer();
        attrValue.append("<").append(featType.getPrefix()).append(":").append(featType.getLocalName());
        if (feat instanceof WFSFeature && ((WFSFeature)feat).getGMLId() != null) {
            attrValue.append(" gml:id=\"" + ((WFSFeature)feat).getGMLId() + "\"");
        }
        attrValue.append(">");
        attrValue.append(this.createInsertPropertiesFragment(sbComplex, null, featType, feat));
        attrValue.append("</").append(featType.getPrefix()).append(":").append(featType.getLocalName()).append("> ");
        return this.buildProperty(attrName, featType, attrValue.toString());
    }

    protected String buildProperty(String attrName, QualifiedName featType, Object attValue) {
        StringBuffer sb = new StringBuffer();
        sb.append("<wfs:Property>");
        sb.append("<wfs:Name>");
        sb.append(featType.getPrefix());
        sb.append(":");
        sb.append(attrName);
        sb.append("</wfs:Name>");
        sb.append("<wfs:Value>");
        sb.append(attValue);
        sb.append("</wfs:Value>");
        sb.append("</wfs:Property>");
        return sb.toString();
    }

    protected String createGeometryGML(Geometry geometry) {
        StringBuffer sb = null;
        try {
            sb = new StringBuffer(this.geomConverter.geom2Gml(geometry));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

