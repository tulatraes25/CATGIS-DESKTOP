/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hardcode.gdbms.engine.values.Value
 *  com.hardcode.gdbms.engine.values.ValueWriter
 *  com.iver.cit.gvsig.drivers.dwg.DwgMemoryDriver
 *  com.iver.cit.gvsig.fmap.drivers.dgn.DgnMemoryDriver
 *  com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.dao.datasource.filedatasource.cad;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.cit.gvsig.drivers.dwg.DwgMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.dgn.DgnMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.dao.datasource.filedatasource.cad.CadException;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class CadDataAccesor
extends AbstractCadDataSource {
    public static final Logger LOGGER = Logger.getLogger(CadDataAccesor.class);
    public static final String DGN_FILE = "DGN";
    public static final String DWG_FILE = "DWG";
    protected String cadID = "";
    protected String fileName = "";
    private FeatureDataset dataset;
    private long id = 0L;
    private Hashtable<String, Set<Integer>> layerToColor = new Hashtable();

    private CadDataAccesor() {
    }

    public CadDataAccesor(String filePath, Class<?> cadType) throws Exception {
        File cadFile = new File(filePath);
        this.fileName = filePath;
        if (!cadFile.exists() || !cadFile.canRead()) {
            throw new FileNotFoundException(I18N.getMessage(this.getClass(), "cad-file-{0}-could-not-been-found-or-read", new Object[]{cadFile.getAbsolutePath()}));
        }
        LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Starting-the-reading-of-the-file-{0}", new Object[]{filePath}));
        DwgMemoryDriver driver = null;
        if (cadType.equals(DwgMemoryDriver.class)) {
            driver = new DwgMemoryDriver();
            File file = new File(filePath);
            if (file.exists()) {
                String versionSTR;
                char[] version;
                block41: {
                    version = new char[6];
                    FileReader reader = null;
                    try {
                        try {
                            reader = new FileReader(file);
                            reader.read(version, 0, 6);
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            if (reader != null) {
                                try {
                                    reader.close();
                                }
                                catch (IOException e2) {
                                    LOGGER.error((Object)e2);
                                }
                            }
                            break block41;
                        }
                    }
                    catch (Throwable throwable) {
                        if (reader != null) {
                            try {
                                reader.close();
                            }
                            catch (IOException e) {
                                LOGGER.error((Object)e);
                            }
                        }
                        throw throwable;
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e) {
                            LOGGER.error((Object)e);
                        }
                    }
                }
                if (!(versionSTR = new String(version)).equals("AC1015")) {
                    throw new CadException(I18N.getString("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Unsupported-version-Only-DWG-2000-format-is-supported"));
                }
            }
            this.cadID = DWG_FILE;
        } else if (cadType.equals(DgnMemoryDriver.class)) {
            driver = new DgnMemoryDriver();
            this.cadID = DGN_FILE;
        } else {
            throw new CadException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Unsupported-driver-type-{0}", new Object[]{cadType.getName()}));
        }
        try {
            if (driver instanceof DwgMemoryDriver) {
                driver.open(new File(filePath));
                driver.initialize();
            } else if (driver instanceof DgnMemoryDriver) {
                ((DgnMemoryDriver)driver).open(new File(filePath));
                ((DgnMemoryDriver)driver).initialize();
            }
            LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.The-reading-of-the-file-{0}-has-finished", new Object[]{filePath}));
            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Starting-Kosmo-import"));
            this.schema = new FeatureSchema();
            this.schema.setGeometryType(15);
            this.schema.addAttribute("Geometry", AttributeType.GEOMETRY);
            int i = 0;
            while (i < driver.getFieldCount()) {
                String field = driver.getFieldName(i);
                if (!this.schema.hasAttribute(field)) {
                    int type = driver.getFieldType(i);
                    AttributeType attrType = this.toKosmoType(type);
                    this.schema.addAttribute(field, attrType);
                }
                ++i;
            }
            this.checkField("ID", AttributeType.LONG);
            this.checkField("FShape", AttributeType.STRING);
            this.checkField("Entity", AttributeType.STRING);
            this.checkField("Layer", AttributeType.STRING);
            this.checkField("Color", AttributeType.INTEGER);
            this.checkField("Elevation", AttributeType.DOUBLE);
            this.checkField("Thickness", AttributeType.DOUBLE);
            this.checkField("Text", AttributeType.STRING);
            this.checkField("HeightText", AttributeType.DOUBLE);
            this.checkField("RotationText", AttributeType.DOUBLE);
            this.schema.getAttribute("ID").setPrimaryKey(true);
            this.schema.setGeometryType(15);
            this.dataset = new FeatureDataset(this.schema);
            this.dataset.setName(FileUtil.nameWithoutExtension(cadFile.getName()));
            this.id = driver.getShapeCount();
            long i2 = 0L;
            while (i2 < (long)driver.getShapeCount()) {
                Geometry geom = null;
                try {
                    geom = driver.getShape((int)i2).toJTSGeometry();
                }
                catch (Exception e) {
                    LOGGER.warn((Object)I18N.getMessage(this.getClass(), "failed-element-in-position-{0}-{1}-this-element-is-discarded", new Object[]{i2}));
                    LOGGER.error((Object)"", (Throwable)e);
                }
                if (geom != null) {
                    Set<Object> colores;
                    BasicFeature feature = new BasicFeature(this.schema);
                    feature.setGeometry(geom);
                    int j = 0;
                    while (j < driver.getFieldCount()) {
                        Value val = driver.getFieldValue(i2, j);
                        if (val != null) {
                            String value = val.getStringValue(ValueWriter.internalValueWriter);
                            if (value != null) {
                                value = value.replaceAll("'", "");
                                value = value.trim();
                            }
                            String fieldName = driver.getFieldName(j);
                            feature.setAttribute(fieldName, this.getGoodObject(fieldName, value));
                        }
                        ++j;
                    }
                    this.dataset.addWithNewKey(feature);
                    String layerName = feature.getString("Layer");
                    Integer auxColor = ((Number)feature.getAttribute("Color")).intValue();
                    if (this.layerToColor.containsKey(layerName)) {
                        colores = this.layerToColor.get(layerName);
                        colores.add(auxColor);
                    } else {
                        colores = new HashSet<Integer>();
                        colores.add(auxColor);
                        this.layerToColor.put(layerName, colores);
                    }
                }
                ++i2;
            }
            if (driver instanceof DwgMemoryDriver) {
                driver.close();
            } else if (driver instanceof DgnMemoryDriver) {
                ((DgnMemoryDriver)driver).close();
            } else {
                ((DXFMemoryDriver)driver).close();
            }
            driver = null;
            System.gc();
            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Kosmo-import-finished"));
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new CadException(String.valueOf(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Error-while-reading-the-file-{0}", new Object[]{filePath})) + " : " + e.getMessage());
        }
    }

    private void checkField(String field, AttributeType type) {
        if (!this.schema.hasAttribute(field)) {
            this.schema.addAttribute(field, type);
        } else {
            this.schema.getAttribute(field).setType(type);
        }
    }

    private AttributeType toKosmoType(int type) {
        AttributeType attType = null;
        switch (type) {
            case -1: 
            case 1: 
            case 12: {
                attType = AttributeType.STRING;
                break;
            }
            case 8: {
                attType = AttributeType.DOUBLE;
                break;
            }
            case 6: {
                attType = AttributeType.FLOAT;
                break;
            }
            case 4: {
                attType = AttributeType.INTEGER;
                break;
            }
            case 7: {
                attType = AttributeType.REAL;
                break;
            }
            case 2: {
                attType = AttributeType.NUMERIC;
                break;
            }
            case 3: {
                attType = AttributeType.DECIMAL;
                break;
            }
            case -7: {
                attType = AttributeType.BIT;
                break;
            }
            case -6: {
                attType = AttributeType.TINYINT;
                break;
            }
            case 5: {
                attType = AttributeType.SMALLINT;
                break;
            }
            case -5: {
                attType = AttributeType.BIGINT;
                break;
            }
            case 92: {
                attType = AttributeType.TIME;
                break;
            }
            case 93: {
                attType = AttributeType.TIMESTAMP;
                break;
            }
            case 91: {
                attType = AttributeType.DATE;
                break;
            }
            case 16: {
                attType = AttributeType.BOOLEAN;
                break;
            }
            case 0: {
                attType = AttributeType.STRING;
                break;
            }
            default: {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor.Unknow-attribute-type-{0}", new Object[]{new Integer(type)}));
            }
        }
        return attType;
    }

    @Override
    public void add(Feature feature) throws Exception {
        feature.setAttribute("ID", (Object)new Long(this.id++));
        this.dataset.add(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        for (Feature feature : features) {
            feature.setAttribute("ID", (Object)new Long(this.id++));
        }
        this.dataset.addAll(features);
    }

    @Override
    public void createSpatialIndex() throws Exception {
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues) {
        return this.dataset.getByAttribute(atributeNames, atributeValues);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, Filter filter) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered, filter);
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.dataset.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys) {
        return this.dataset.getByPrimaryKeys(keys);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoredUpdate) {
        return this.dataset.getByPrimaryKeys(keys);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.dataset.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.dataset.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.dataset.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.dataset.getDistintsValues(expr, limit);
    }

    @Override
    public List<Feature> getFeatures() {
        return this.dataset.getFeatures();
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.dataset.iterator();
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.dataset.getFeaturesSamples(n);
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        return this.dataset.getFieldsValues(field, fieldKey, value);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.dataset.getMapFieldsValues(fields, fieldKey);
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        return this.dataset.getKeys();
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.dataset.getSortKeys(column, ascending, values);
    }

    @Override
    public Envelope getViewBox() {
        return this.dataset.getEnvelope();
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope env = new Envelope();
        FeatureIterator it = null;
        try {
            it = this.dataset.queryIterator(filter, null);
            while (it.hasNext()) {
                Feature feat = it.next();
                env.expandToInclude(feat.getGeometry().getEnvelopeInternal());
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return env;
    }

    @Override
    public List<Feature> query(Envelope rectangle) {
        return this.dataset.query(rectangle);
    }

    @Override
    public List<Feature> query(Filter filter) {
        return this.dataset.query(filter);
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) {
        return this.dataset.query(view, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filtro, rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields, List<String> labels) {
        return null;
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.dataset.queryIterator(rectangle);
    }

    @Override
    public FeatureIterator queryIterator(Envelope view, Filter filter) {
        return this.dataset.queryIterator(filter, view);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        this.dataset.removeAll(features);
    }

    @Override
    public int size() {
        return this.dataset.size();
    }

    @Override
    public void update(Feature feature) throws Exception {
        if (feature.isUnsaved()) {
            this.add(feature);
        } else {
            this.dataset.update(feature);
        }
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        for (Feature feature : features) {
            this.update(feature);
        }
    }

    @Override
    public void commit() throws Exception {
        this.inMemory = false;
        try {
            if (this.newFeatures.size() > 0) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
                this.addAll(this.newFeatures);
                this.newFeatures.clear();
            }
            if (this.deletedFeatures.size() > 0) {
                ArrayList<Feature> featuresFilter = new ArrayList<Feature>();
                for (Feature element : this.deletedFeatures) {
                    if (element.isUnsaved()) continue;
                    featuresFilter.add(element);
                }
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.deleting-{0}-features", new Object[]{new Integer(featuresFilter.size())}));
                this.removeAll(featuresFilter);
                this.deletedFeatures.clear();
            }
            if (this.updateFeatures.size() > 0) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.updating-{0}-features", new Object[]{new Integer(this.updateFeatures.size())}));
                this.updateAll(this.updateFeatures);
                this.updateFeatures.clear();
            }
        }
        finally {
            this.inMemory = true;
        }
    }

    @Override
    public Hashtable<String, Set<Integer>> getLayerToColor() {
        return this.layerToColor;
    }

    private Object getGoodObject(String field, String value) {
        if (field.equals("ID")) {
            return new Long(value);
        }
        if (field.equals("FShape") || field.equals("Entity") || field.equals("Layer") || field.equals("Text")) {
            return value;
        }
        if (field.equals("Elevation") || field.equals("Thickness") || field.equals("HeightText") || field.equals("RotationText")) {
            return new Double(value);
        }
        if (field.equals("Color")) {
            return new Integer(value);
        }
        return FeatureUtil.getGoodAttribute(this.schema.getAttributeType(field), value);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.queryGeometryIterator(rectangle, filtro, orderByFields, null);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        throw new UnsupportedOperationException(I18N.getString(this.getClass(), "operation-not-supported"));
    }

    public String getCadType() {
        return this.cadID;
    }

    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.dataset.removeByPKs(pks);
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.dataset.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, boolean ascending) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.dataset.getByAttribute(names, values, fieldOrdered, ascending, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(rectangle, labels);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.dataset.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public Object clone() {
        CadDataAccesor newCadDataAccesor = new CadDataAccesor();
        newCadDataAccesor.fileName = this.fileName;
        newCadDataAccesor.dataset = (FeatureDataset)this.dataset.clone();
        if (this.layerFilter != null) {
            newCadDataAccesor.setLayerFilter((Filter)((Cloneable)this.getLayerFilter()).clone());
        }
        newCadDataAccesor.cadID = this.cadID;
        newCadDataAccesor.id = this.id;
        newCadDataAccesor.layerToColor = this.layerToColor;
        return newCadDataAccesor;
    }

    @Override
    public void dispose() {
        if (this.dataset != null) {
            this.dataset.dispose();
            this.dataset = null;
        }
        if (this.layerToColor != null) {
            this.layerToColor.clear();
            this.layerToColor = null;
        }
    }
}

