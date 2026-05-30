/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.model.feature.FeatureCollection
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 */
package org.saig.jump.plugin.sdi.wfs;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import de.latlon.deejump.wfs.client.WFServiceWrapper_1_0_0;
import de.latlon.deejump.wfs.client.WFServiceWrapper_1_1_0;
import de.latlon.deejump.wfs.data.JUMPFeatureFactory2;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.gvsig.crs.ICrs;
import org.saig.core.filter.Filter;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.core.model.sdi.wfs.WFSLayer;
import org.saig.jump.plugin.sdi.wfs.WfsLoadingErrorException;
import org.saig.jump.widgets.util.SelectGeometryTypeDialog;

public class WfsLayerBuilder {
    public static final String V_1_1_0 = "1.1.0";
    public static final String V_1_0_0 = "1.0.0";

    public List<WFSLayer> createWfsLayers(String version, String url, String wfsFeatureType, LayerManager layerManager) throws WfsLoadingErrorException {
        try {
            AbstractWFSWrapper wrapper = null;
            if (version.equals(V_1_0_0)) {
                wrapper = new WFServiceWrapper_1_0_0(null, url);
            } else if (version.equals(V_1_1_0)) {
                wrapper = new WFServiceWrapper_1_1_0(null, url);
            }
            ((AbstractWFSWrapper)wrapper).initialize();
            WFSFeatureTypeInfo typeInfo = this.loadSelectedFeatureType(wrapper, wfsFeatureType);
            return this.createWfsLayers(wrapper, typeInfo, layerManager);
        }
        catch (Exception e) {
            throw new WfsLoadingErrorException(e);
        }
    }

    public List<WFSFeatureCollection> createCollections(String version, String url, String wfsFeatureType, List<String> selectedAttributes, String geomField, String format, String selectedSrs, Filter filter, int maxFeatures, BasicAuthentificationData basicAuthentificationData) throws WfsLoadingErrorException {
        try {
            AbstractWFSWrapper wrapper = null;
            if (version.equals(V_1_0_0)) {
                wrapper = new WFServiceWrapper_1_0_0(basicAuthentificationData, url);
            } else if (version.equals(V_1_1_0)) {
                wrapper = new WFServiceWrapper_1_1_0(basicAuthentificationData, url);
            }
            ((AbstractWFSWrapper)wrapper).initialize();
            WFSFeatureTypeInfo typeInfo = this.loadSelectedFeatureType(wrapper, wfsFeatureType);
            typeInfo.setSelectedAttributes(selectedAttributes);
            typeInfo.setGeomAttrName(geomField != null ? new QualifiedName(geomField) : null);
            typeInfo.setServiceVersion(version);
            typeInfo.setNumMaxFeatures(maxFeatures);
            typeInfo.setSelectedFormat(new FormatType(null, null, null, format));
            typeInfo.setSelectedSRS(new URI(selectedSrs));
            typeInfo.setQueryFilter(filter);
            Object[] wfsFcs = this.createCollections(wrapper, typeInfo);
            return ArrayUtils.isEmpty((Object[])wfsFcs) ? new ArrayList() : Arrays.asList(wfsFcs);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new WfsLoadingErrorException(e);
        }
    }

    protected WFSFeatureTypeInfo loadSelectedFeatureType(AbstractWFSWrapper wfsService, String selectedFeatureTypeName) {
        String baseDefaultPkName;
        WFSFeatureType ft = wfsService.getFeatureTypeByName(selectedFeatureTypeName);
        Object[] attributeNames = wfsService.getProperties(selectedFeatureTypeName);
        Arrays.sort(attributeNames);
        ArrayList<String> attributeNamesList = new ArrayList<String>();
        Object[] geoProperties = wfsService.getGeometryProperties(selectedFeatureTypeName);
        ArrayList<QualifiedName> geoPropertiesList = new ArrayList<QualifiedName>();
        if (attributeNames != null) {
            CollectionUtils.addAll(attributeNamesList, (Object[])attributeNames);
        }
        if (geoProperties != null) {
            CollectionUtils.addAll(geoPropertiesList, (Object[])geoProperties);
        }
        WFSFeatureTypeInfo info = new WFSFeatureTypeInfo(ft.getName(), attributeNamesList, geoPropertiesList, ft.getTitle());
        info.setEnabled(CollectionUtils.isNotEmpty(geoPropertiesList));
        Object[] formats = ft.getOutputFormats();
        if (ArrayUtils.isEmpty((Object[])formats)) {
            formats = new FormatType[]{wfsService.getServiceVersion().equals(V_1_0_0) ? new FormatType(null, null, null, "GML2") : new FormatType(null, null, null, "text/xml; subtype=gml/3.1.1")};
        }
        info.setAvailableFormats((FormatType[])formats);
        if (formats != null && formats.length == 1) {
            info.setSelectedFormat((FormatType)formats[0]);
        }
        URI defaultSRS = ft.getDefaultSRS();
        Object[] otherSRSs = ft.getOtherSrs();
        TreeSet<Object> srs = new TreeSet<Object>();
        if (defaultSRS != null) {
            srs.add(defaultSRS);
        }
        if (!ArrayUtils.isEmpty((Object[])otherSRSs)) {
            int i = 0;
            while (i < otherSRSs.length) {
                srs.add(otherSRSs[i]);
                ++i;
            }
        }
        URI[] uriArray = new URI[srs.size()];
        info.setAvailableSRS(srs.toArray(uriArray));
        if (defaultSRS != null) {
            info.setSelectedSRS(defaultSRS);
        } else if (uriArray != null && uriArray.length == 1) {
            info.setSelectedSRS(uriArray[0]);
        }
        String candidatePkName = baseDefaultPkName = "gid";
        int cont = 1;
        while (attributeNamesList.contains(candidatePkName)) {
            candidatePkName = String.valueOf(baseDefaultPkName) + "_" + cont++;
        }
        info.setPkName(candidatePkName);
        return info;
    }

    private WFSFeatureCollection[] createCollections(AbstractWFSWrapper wfsWrapper, WFSFeatureTypeInfo featureTypeInfo) throws Exception {
        String request = featureTypeInfo.buildRequest();
        QualifiedName qn = null;
        WFSFeatureType ft = wfsWrapper.getFeatureTypeByName(featureTypeInfo.getLocalName());
        if (ft != null) {
            qn = ft.getName();
        }
        FeatureCollection dfc = JUMPFeatureFactory2.createDeegreeFCfromWFS(wfsWrapper, request, qn);
        WFSFeatureCollection[] datasets = JUMPFeatureFactory2.createWFSCollectionsFromDeegreeFC(dfc, (Geometry)(featureTypeInfo.getGeomAttrName() == null ? new GeometryFactory().createPoint(new Coordinate(0.0, 0.0)) : null), featureTypeInfo.getPkName(), featureTypeInfo.getGeomAttrName() != null ? featureTypeInfo.getGeomAttrName().getLocalName() : null, featureTypeInfo, wfsWrapper, qn);
        return datasets;
    }

    public List<WFSLayer> createWfsLayers(AbstractWFSWrapper wfsWrapper, WFSFeatureTypeInfo featureTypeInfo, LayerManager layerManager) throws Exception {
        ArrayList<WFSLayer> layersToLoad = new ArrayList<WFSLayer>();
        Object[] datasets = this.createCollections(wfsWrapper, featureTypeInfo);
        if (!ArrayUtils.isEmpty((Object[])datasets) && datasets[0] != null) {
            String crs = featureTypeInfo.getSelectedSRS().toString();
            String validCrs = GMLGeometryAdapter.transformCRSNameToEPSG(crs);
            ICrs proj = CrsRepositoryManager.getInstance().getCRS(validCrs);
            int i = 0;
            while (i < datasets.length) {
                Object wfsFC = datasets[i];
                if (((WFSFeatureCollection)wfsFC).getFeatureSchema().getGeometryType() == 0) {
                    SelectGeometryTypeDialog d = new SelectGeometryTypeDialog(JUMPWorkbench.getFrameInstance(), true, false, ((WFSFeatureCollection)wfsFC).getName());
                    GUIUtil.centre(d, JUMPWorkbench.getFrameInstance());
                    d.setVisible(true);
                    if (d.wasOKPressed()) {
                        ((WFSFeatureCollection)wfsFC).getFeatureSchema().setGeometryType(d.getGeometryType());
                        ((WFSFeatureCollection)wfsFC).set3d(d.is3D());
                    }
                }
                WFSLayer layer = new WFSLayer(wfsWrapper.getServiceVersion(), wfsWrapper.getBaseWfsURL(), featureTypeInfo.getLocalName(), featureTypeInfo.getSelectedAttributes(), featureTypeInfo.getGeomAttrName().toString(), featureTypeInfo.getSelectedFormat().getValue(), featureTypeInfo.getSelectedSRS().toString(), featureTypeInfo.getQueryFilter(), featureTypeInfo.getNumMaxFeatures(), layerManager.generateLayerFillColor(), wfsWrapper.getBasicAuthData(), (WFSFeatureCollection)wfsFC, layerManager);
                layer.setProjection(proj);
                layersToLoad.add(layer);
                ++i;
            }
        }
        return layersToLoad;
    }
}

