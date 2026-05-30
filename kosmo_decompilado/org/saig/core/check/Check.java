/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.util.LocalConfiguration
 *  org.exolab.castor.xml.Marshaller
 *  org.exolab.castor.xml.Unmarshaller
 */
package org.saig.core.check;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.saig.core.check.AlphanumericCondition;
import org.saig.core.check.CheckGroup;
import org.saig.core.check.CheckingException;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.GeometryFilterImpl;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.summary.SummaryMessage;

public class Check {
    protected Map<String, Object> metadata;
    protected Layer sourceLayer;
    protected Layer targetLayer;
    protected GeometryFilter topologyCondition;
    protected AlphanumericCondition[] alphanumericConditions;
    protected String filePath;
    public static final Logger LOGGER = Logger.getLogger(Check.class);
    protected FeatureSchema incidentSchema;
    protected String selfCheck;
    protected static final String PRIMARY_KEY_ATTR = "GID";
    protected static final String CONDITION_ATTR = I18N.getString("org.saig.core.check.Check.Condition");
    protected static final String REASON_ATTR = I18N.getString("org.saig.core.check.Check.reason");
    protected static final String DATE_ATTR = I18N.getString("org.saig.core.check.Check.date");
    protected static final String GEOMETRY_ATTR = "GEOMETRY";
    protected static final String GEOMETRIC = I18N.getString("org.saig.core.check.Check.geometric");
    protected static final String GEOMETRIC_OR_ALPHANUMERIC = I18N.getString("org.saig.core.check.Check.geometric-or-alphanumeric");
    protected static FilterFactory filterFactory = FilterFactory.createFilterFactory();

    public Check(Map<String, Object> metadata, Layer source, Layer target, GeometryFilter geometryCondition, AlphanumericCondition[] additionalFilters) {
        this.metadata = metadata;
        this.sourceLayer = source;
        this.targetLayer = target;
        this.topologyCondition = geometryCondition;
        this.alphanumericConditions = additionalFilters;
    }

    public Check(Layer source, Layer target, GeometryFilter geometryCondition) {
        this(new HashMap<String, Object>(), source, target, geometryCondition, null);
    }

    public Check() {
    }

    public AlphanumericCondition[] getAlphanumericConditions() {
        return this.alphanumericConditions;
    }

    public void setAlphanumericConditions(AlphanumericCondition[] alphanumericFilters) {
        this.alphanumericConditions = alphanumericFilters;
    }

    public Layer getSourceLayer() {
        return this.sourceLayer;
    }

    public void setSourceLayer(Layer sourceLayer) {
        this.sourceLayer = sourceLayer;
    }

    public Layer getTargetLayer() {
        return this.targetLayer;
    }

    public void setTargetLayer(Layer targetLayer) {
        this.targetLayer = targetLayer;
    }

    public GeometryFilter getTopologyCondition() {
        return this.topologyCondition;
    }

    public void setTopologyCondition(GeometryFilter topologyCondition) {
        this.topologyCondition = topologyCondition;
    }

    public FeatureCollection check(List<SummaryMessage> messageList) {
        ArrayList<Feature> incorrectFeatures;
        FeatureCollection fcIncident;
        block25: {
            fcIncident = null;
            try {
                this.getLayersForCheck();
                this.checkAttributesFromConditions();
            }
            catch (CheckingException ce) {
                messageList.add(this.buildCheckErrorMessage(null, ce));
                return fcIncident;
            }
            this.incidentSchema = this.generateIncidentSchema(this.sourceLayer.getGeometryType());
            incorrectFeatures = new ArrayList<Feature>();
            FeatureIterator itSource = null;
            try {
                try {
                    itSource = this.sourceLayer.getFeatureCollectionWrapper().iterator();
                    while (itSource.hasNext()) {
                        GeometryFilterImpl currentFilter;
                        Feature currentFeature = itSource.next();
                        try {
                            currentFilter = new GeometryFilterImpl(this.topologyCondition.getFilterType());
                            currentFilter.addLeftGeometry(filterFactory.createLiteralExpression(currentFeature.getGeometry()));
                        }
                        catch (IllegalFilterException e) {
                            LOGGER.error((Object)I18N.getString("org.saig.core.check.Check.Error-generating-the-geometric-filter"), (Throwable)e);
                            messageList.add(this.buildCheckErrorMessage(currentFeature, e));
                            continue;
                        }
                        FeatureIterator itTarget = null;
                        try {
                            try {
                                Filter alphanumericFilter;
                                Feature currentTargetFeature;
                                boolean ok = false;
                                boolean okAnyGeometry = false;
                                if (this.topologyCondition.getFilterType() != 6) {
                                    itTarget = this.targetLayer.getFeatureCollectionWrapper().queryIterator(currentFeature.getGeometry().getEnvelopeInternal());
                                    while (itTarget.hasNext() && !ok) {
                                        currentTargetFeature = itTarget.next();
                                        ok = currentFilter.contains(currentTargetFeature);
                                        if (!ok) continue;
                                        okAnyGeometry = true;
                                        alphanumericFilter = this.generateAlphanumericFilter(currentFeature);
                                        if (alphanumericFilter == null) continue;
                                        ok = alphanumericFilter.contains(currentTargetFeature);
                                    }
                                } else {
                                    ok = true;
                                    itTarget = this.targetLayer.getFeatureCollectionWrapper().queryIterator(currentFeature.getGeometry().getEnvelopeInternal());
                                    while (itTarget.hasNext() && ok) {
                                        currentTargetFeature = itTarget.next();
                                        ok = currentFilter.contains(currentTargetFeature);
                                        if (!ok) continue;
                                        okAnyGeometry = true;
                                        alphanumericFilter = this.generateAlphanumericFilter(currentFeature);
                                        if (alphanumericFilter == null) continue;
                                        ok = alphanumericFilter.contains(currentTargetFeature);
                                    }
                                }
                                if (!ok) {
                                    incorrectFeatures.add(this.generateIncidentFeature(currentFeature, okAnyGeometry));
                                }
                            }
                            catch (Exception e) {
                                messageList.add(this.buildCheckErrorMessage(currentFeature, e));
                                if (itTarget == null) continue;
                                itTarget.close();
                                continue;
                            }
                        }
                        catch (Throwable throwable) {
                            if (itTarget != null) {
                                itTarget.close();
                            }
                            throw throwable;
                        }
                        if (itTarget == null) continue;
                        itTarget.close();
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (itSource != null) {
                        itSource.close();
                    }
                    break block25;
                }
            }
            catch (Throwable throwable) {
                if (itSource != null) {
                    itSource.close();
                }
                throw throwable;
            }
            if (itSource != null) {
                itSource.close();
            }
        }
        if (incorrectFeatures.size() != 0) {
            fcIncident = this.generateCollectionFromWrongFeatures(incorrectFeatures);
        }
        return fcIncident;
    }

    protected void checkAttributesFromConditions() throws CheckingException {
        if (this.alphanumericConditions == null) {
            return;
        }
        int i = 0;
        while (i < this.alphanumericConditions.length) {
            AlphanumericCondition currentCondition = this.alphanumericConditions[i];
            String sourceAttributeName = currentCondition.getSourceAttributeName();
            String targetAttributeName = currentCondition.getTargetAttributeName();
            FeatureSchema sourceSchema = this.sourceLayer.getFeatureSchema();
            FeatureSchema targetSchema = this.targetLayer.getFeatureSchema();
            if (!sourceSchema.hasAttribute(sourceAttributeName)) {
                throw new CheckingException(I18N.getMessage("org.saig.core.check.Check.The-layer-{0}-does-not-have-the-attribute-{1}", new Object[]{this.sourceLayer.getName(), sourceAttributeName}));
            }
            if (!targetSchema.hasAttribute(targetAttributeName)) {
                throw new CheckingException(I18N.getMessage("org.saig.core.check.Check.the-layer-{0}-does-not-have-the-attribute-{1}", new Object[]{this.targetLayer.getName(), targetAttributeName}));
            }
            ++i;
        }
    }

    protected void getLayersForCheck() throws CheckingException {
        String sourceLayerName = this.sourceLayer.getName();
        String targetLayerName = this.targetLayer.getName();
        this.sourceLayer = JUMPWorkbench.getLayer(sourceLayerName);
        this.targetLayer = JUMPWorkbench.getLayer(targetLayerName);
        if (this.sourceLayer == null) {
            throw new CheckingException(I18N.getMessage("org.saig.core.check.Check.The-source-layer-{0}-could-not-be-loaded", new Object[]{sourceLayerName}));
        }
        if (this.targetLayer == null) {
            throw new CheckingException(I18N.getMessage("org.saig.core.check.Check.The-target-layer-{0}-could-not-be-loaded", new Object[]{targetLayerName}));
        }
    }

    protected FeatureCollection generateCollectionFromWrongFeatures(List<Feature> incorrectFeatures) {
        FeatureDataset dataset = new FeatureDataset(incorrectFeatures, this.incidentSchema);
        return dataset;
    }

    protected FeatureSchema generateIncidentSchema(int geometryType) {
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute(PRIMARY_KEY_ATTR, AttributeType.LONG, new Boolean(true));
        schema.addAttribute(CONDITION_ATTR, AttributeType.STRING);
        schema.addAttribute(REASON_ATTR, AttributeType.STRING);
        schema.addAttribute(DATE_ATTR, AttributeType.DATE);
        schema.addAttribute(GEOMETRY_ATTR, AttributeType.GEOMETRY);
        schema.setGeometryType(geometryType);
        return schema;
    }

    protected Feature generateIncidentFeature(Feature currentFeature, boolean okAnyGeometry) {
        Feature newFeature = FeatureUtil.toFeature(currentFeature.getGeometry(), this.incidentSchema);
        newFeature.setAttribute(CONDITION_ATTR, (Object)this.generateConditionFromFilter());
        newFeature.setAttribute(REASON_ATTR, (Object)this.generateReasonFromFilter(okAnyGeometry));
        newFeature.setAttribute(DATE_ATTR, (Object)new Date());
        return newFeature;
    }

    protected String generateReasonFromFilter(boolean okAnyGeometry) {
        String reason = "";
        reason = okAnyGeometry ? GEOMETRIC_OR_ALPHANUMERIC : GEOMETRIC;
        return reason;
    }

    protected String generateConditionFromFilter() {
        String condition = String.valueOf(this.sourceLayer.getName()) + " " + this.getTopologyOperation() + " " + this.targetLayer.getName();
        if (this.alphanumericConditions != null && this.alphanumericConditions.length > 0) {
            int i = 0;
            while (i < this.alphanumericConditions.length) {
                AlphanumericCondition currentCondition = this.alphanumericConditions[i];
                condition = String.valueOf(condition) + " AND " + currentCondition.getSourceAttributeName() + "(" + this.sourceLayer.getName() + ") " + currentCondition.getOperator() + " " + currentCondition.getTargetAttributeName() + "(" + this.targetLayer.getName() + ")";
                ++i;
            }
        }
        return condition;
    }

    protected Filter generateFullFilter(Filter currentFilter, Filter alphanumericFilter) throws CheckingException {
        Filter fullFilter = currentFilter;
        if (alphanumericFilter != null) {
            fullFilter = fullFilter.and(alphanumericFilter);
        }
        return fullFilter;
    }

    private Filter generateAlphanumericFilter(Feature currentFeature) throws CheckingException {
        Filter alphanumericFilter = null;
        if (this.alphanumericConditions != null) {
            int i = 0;
            while (i < this.alphanumericConditions.length) {
                alphanumericFilter = alphanumericFilter == null ? this.createFilterFromCondition(currentFeature, this.alphanumericConditions[i]) : alphanumericFilter.and(this.createFilterFromCondition(currentFeature, this.alphanumericConditions[i]));
                ++i;
            }
        }
        return alphanumericFilter;
    }

    private Filter createFilterFromCondition(Feature currentFeature, AlphanumericCondition condition) throws CheckingException {
        Filter filter = null;
        Object sourceValue = currentFeature.getAttribute(condition.getSourceAttributeName());
        String expression = "";
        expression = sourceValue != null && !sourceValue.toString().trim().equals("") ? "'" + sourceValue + "' " + condition.getOperator() + " " + condition.getTargetAttributeName() : "isNull(" + condition.getTargetAttributeName() + ")";
        try {
            filter = (Filter)ExpressionBuilder.parse(expression);
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getMessage("org.saig.core.check.Check.Error-while-parsing-the-expression-{0}", new Object[]{expression}), (Throwable)e);
            throw new CheckingException(I18N.getMessage("org.saig.core.check.Check.Alphanumeric-condition-invalid-{0}", new Object[]{expression}));
        }
        return filter;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static void main(String[] args) {
        CheckGroup restoredCheckGroup;
        block24: {
            File file;
            StringWriter stringWriter;
            block22: {
                HashMap<String, Object> metadataMap = new HashMap<String, Object>();
                metadataMap.put("Nombre", "Chequeo de prueba");
                metadataMap.put("Fecha", new Date());
                metadataMap.put("Color", Color.BLACK);
                CheckGroup grupo = new CheckGroup();
                grupo.setMetadata(metadataMap);
                Layer source = new Layer();
                source.setName("Source Layer");
                Layer target = new Layer();
                target.setName("Target Layer");
                GeometryFilterImpl geometryFilter = null;
                GeometryFilterImpl equalsFilter = null;
                try {
                    geometryFilter = new GeometryFilterImpl(9);
                    equalsFilter = new GeometryFilterImpl(5);
                }
                catch (IllegalFilterException e) {
                    LOGGER.error((Object)"Filtro geometrico de prueba ilegal", (Throwable)e);
                }
                Check newCheck = new Check(metadataMap, source, target, geometryFilter, null);
                Check newCheck2 = new Check(metadataMap, source, target, equalsFilter, null);
                AlphanumericCondition condition = new AlphanumericCondition("campo1", "campo2", ">");
                AlphanumericCondition condition2 = new AlphanumericCondition("campo3", "campo4", "!=");
                Layer sourceSelfCheckLayer = new Layer();
                sourceSelfCheckLayer.setName("sourceSelfCheckLayer");
                newCheck.setFilePath("C:\\temp\\newCheck.shp");
                newCheck2.setFilePath("C:\\temp\\newCheck2.shp");
                newCheck.setAlphanumericConditions(new AlphanumericCondition[]{condition, condition2});
                ArrayList<Check> checkList = new ArrayList<Check>();
                checkList.add(newCheck);
                checkList.add(newCheck2);
                grupo.setCheckList(checkList);
                stringWriter = new StringWriter();
                file = new File("C:\\temp\\check.txt");
                try {
                    try {
                        Mapping mapping = LoadXMLMappings.loadTopologyCheckingMappings();
                        Properties properties = LocalConfiguration.getInstance().getProperties();
                        properties.setProperty("org.exolab.castor.indent", "true");
                        Marshaller marshaller = new Marshaller((Writer)stringWriter);
                        marshaller.setMapping(mapping);
                        marshaller.marshal((Object)grupo);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"Error generando el contenido del fichero", (Throwable)e);
                        stringWriter.flush();
                        break block22;
                    }
                }
                catch (Throwable throwable) {
                    stringWriter.flush();
                    throw throwable;
                }
                stringWriter.flush();
            }
            try {
                FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
            }
            catch (IOException e) {
                LOGGER.error((Object)"Error escribiendo el fichero", (Throwable)e);
            }
            FileReader reader = null;
            restoredCheckGroup = null;
            try {
                try {
                    reader = new FileReader(file);
                    Mapping mapping = LoadXMLMappings.loadTopologyCheckingMappings();
                    Unmarshaller unmar = new Unmarshaller(mapping);
                    unmar.setWhitespacePreserve(true);
                    restoredCheckGroup = (CheckGroup)unmar.unmarshal((Reader)reader);
                }
                catch (Exception ex) {
                    LOGGER.error((Object)"Error al recuperar el archivo", (Throwable)ex);
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException ioe) {
                            LOGGER.error((Object)"", (Throwable)ioe);
                        }
                    }
                    break block24;
                }
            }
            catch (Throwable throwable) {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)"", (Throwable)ioe);
                    }
                }
                throw throwable;
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ioe) {
                    LOGGER.error((Object)"", (Throwable)ioe);
                }
            }
        }
        LOGGER.info((Object)("Generado el chequeo " + restoredCheckGroup));
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTopologyOperation() {
        if (this.topologyCondition != null) {
            return FilterUtil.getFilterName(this.topologyCondition.getFilterType());
        }
        return null;
    }

    public void setTopologyOperation(String geometricOperation) {
        try {
            this.topologyCondition = (GeometryFilter)FilterUtil.fromName(geometricOperation);
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)I18N.getMessage("org.saig.core.check.Check.Illegal-filter-{0}", new Object[]{geometricOperation}), (Throwable)e);
        }
    }

    protected SummaryMessage buildCheckErrorMessage(Feature feat, Exception e) {
        String basicMessage = I18N.getString("org.saig.core.check.Check.Error-while-processing-the-check");
        if (feat != null && feat.getPrimaryKey() != null) {
            basicMessage = String.valueOf(basicMessage) + ": " + I18N.getMessage("org.saig.core.check.Check.Feature-{0}", new Object[]{feat.getPrimaryKey()});
        }
        String extendedMessage = e.getMessage();
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }
}

