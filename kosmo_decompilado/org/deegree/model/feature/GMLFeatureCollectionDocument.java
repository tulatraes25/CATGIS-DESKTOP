/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.framework.util.IDGenerator
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.feature.FeatureCollection
 *  org.deegree.model.feature.FeatureFactory
 *  org.deegree.model.feature.Messages
 */
package org.deegree.model.feature;

import java.util.ArrayList;
import java.util.Collection;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.GMLFeatureDocument;
import org.deegree.model.feature.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class GMLFeatureCollectionDocument
extends GMLFeatureDocument {
    private static final long serialVersionUID = -6923435144671685710L;
    private Collection<String> xlinkedMembers = new ArrayList<String>();

    public GMLFeatureCollectionDocument() {
    }

    public GMLFeatureCollectionDocument(boolean guessSimpleTypes) {
        super(guessSimpleTypes);
    }

    public FeatureCollection parse() throws XMLParsingException {
        FeatureCollection fc = this.parse(this.getRootElement());
        this.resolveXLinkReferences();
        this.addXLinkedMembers(fc);
        return fc;
    }

    private void addXLinkedMembers(FeatureCollection fc) throws XMLParsingException {
        for (String fid : this.xlinkedMembers) {
            Feature feature = (Feature)this.featureMap.get(fid);
            if (feature == null) {
                String msg = Messages.format((String)"ERROR_XLINK_NOT_RESOLVABLE", (Object[])new Object[]{fid});
                throw new XMLParsingException(msg);
            }
            fc.add(feature);
        }
    }

    private FeatureCollection parse(Element element) throws XMLParsingException {
        String fcId = this.parseFeatureId(element);
        if ("".equals(fcId)) {
            fcId = element.getLocalName();
            fcId = String.valueOf(fcId) + IDGenerator.getInstance().generateUniqueID();
        }
        String srsName = XMLTools.getNodeAsString(element, "gml:boundedBy/*[1]/@srsName", nsContext, null);
        ElementList el = XMLTools.getChildElements(element);
        ArrayList<Feature> list = new ArrayList<Feature>(el.getLength());
        int i = 0;
        while (i < el.getLength()) {
            Feature member = null;
            Element propertyElement = el.item(i);
            Element featureElement = null;
            String propertyName = propertyElement.getNodeName();
            if (!(propertyName.endsWith("boundedBy") || propertyName.endsWith("name") || propertyName.endsWith("description"))) {
                if (propertyName.endsWith("featureMembers")) {
                    ElementList featureElementList = XMLTools.getChildElements(propertyElement);
                    int j = 0;
                    while (j < featureElementList.getLength()) {
                        featureElement = featureElementList.item(j);
                        propertyName = featureElement.getNodeName();
                        if (!(propertyName.endsWith("boundedBy") || propertyName.endsWith("name") || propertyName.endsWith("description") || (member = this.parseSingleFeature(propertyElement, featureElement, propertyName, srsName)) == null)) {
                            list.add(member);
                        }
                        ++j;
                    }
                } else if (propertyName.endsWith("featureMember") && (member = this.parseSingleFeature(propertyElement, featureElement = XMLTools.getChildElements(el.item(i)).item(0), propertyName, srsName)) != null) {
                    list.add(member);
                }
            }
            ++i;
        }
        Feature[] features = list.toArray(new Feature[list.size()]);
        FeatureCollection fc = FeatureFactory.createFeatureCollection((String)fcId, (Feature[])features);
        String nof = element.getAttribute("numberOfFeatures");
        if (nof == null) {
            nof = "" + features.length;
        }
        fc.setAttribute("numberOfFeatures", nof);
        return fc;
    }

    private Feature parseSingleFeature(Element propertyElement, Element featureElement, String propertyName, String srsName) throws XMLParsingException {
        Feature member = null;
        if (featureElement == null) {
            Text xlinkHref = (Text)XMLTools.getNode(propertyElement, "@xlink:href/text()", nsContext);
            if (xlinkHref == null) {
                String msg = Messages.format((String)"ERROR_INVALID_FEATURE_PROPERTY", (Object[])new Object[]{propertyName});
                throw new XMLParsingException(msg);
            }
            String href = xlinkHref.getData();
            if (!href.startsWith("#")) {
                String msg = Messages.format((String)"ERROR_EXTERNAL_XLINK_NOT_SUPPORTED", (Object[])new Object[]{href});
                throw new XMLParsingException(msg);
            }
            String fid = href.substring(1);
            this.xlinkedMembers.add(fid);
        } else {
            try {
                member = this.parseFeature(featureElement, srsName);
            }
            catch (Exception e) {
                throw new XMLParsingException("Error creating feature instance from element '" + featureElement.getLocalName() + "': " + e.getMessage(), (Throwable)e);
            }
        }
        return member;
    }
}

