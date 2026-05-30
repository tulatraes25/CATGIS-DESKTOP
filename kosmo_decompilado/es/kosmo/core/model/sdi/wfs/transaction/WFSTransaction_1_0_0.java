/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringEscapeUtils
 */
package es.kosmo.core.model.sdi.wfs.transaction;

import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.dao.datasource.filedatasource.gml.GMLGeometryConverter_2_1_2;
import es.kosmo.core.model.sdi.wfs.transaction.AbstractWFSTransaction;
import org.apache.commons.lang.StringEscapeUtils;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;

public class WFSTransaction_1_0_0
extends AbstractWFSTransaction {
    private static final String BASE_REQUEST_HEADER = "<?xml version='1.0' encoding='UTF-8'?><wfs:Transaction version=\"1.0.0\" service=\"WFS\" %s xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd %s %s\">";

    public WFSTransaction_1_0_0(WFSFeatureTypeInfo info, AbstractWFSWrapper service) {
        super(info, service);
        this.geomConverter = new GMLGeometryConverter_2_1_2(info.getSelectedSRS().toString());
    }

    @Override
    protected String getRequestHeader() {
        String featTypeXmlns = "xmlns:" + this.info.getPrefix() + "=\"" + this.info.getNamespace() + "\"";
        return String.format(BASE_REQUEST_HEADER, featTypeXmlns, StringEscapeUtils.escapeXml((String)this.service.getDescribeTypeURL(this.info.getName())), this.info.getNamespace());
    }

    @Override
    protected String getFeatureIdFilter(String featId) {
        StringBuffer sb = new StringBuffer();
        sb.append("<ogc:Filter>");
        sb.append("<ogc:FeatureId fid=\"").append(featId).append("\"/>");
        sb.append("</ogc:Filter>");
        return sb.toString();
    }
}

