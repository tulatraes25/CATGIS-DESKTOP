/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.math.stat.descriptive.moment.Variance
 *  org.apache.commons.math.stat.descriptive.summary.Sum
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.stats;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class StatsOperatorsFactory {
    private static final Logger LOGGER = Logger.getLogger(StatsOperatorsFactory.class);
    private static final Map<String, String> operatorIDToNameMap = new TreeMap<String, String>();
    private static final Map<String, String> operatorIDToShortNameMap = new TreeMap<String, String>();
    public static final String OP_ALL = "OP_ALL";
    public static final String OP_COUNT = "OP_COUNT";
    public static final String OP_FIRST = "OP_FIRST";
    public static final String OP_LAST = "OP_LAST";
    public static final String OP_AVG = "OP_AVG";
    public static final String OP_SUM = "OP_SUM";
    public static final String OP_MIN = "OP_MIN";
    public static final String OP_MAX = "OP_MAX";
    public static final String OP_VARIANCE = "OP_VARIANCE";
    public static final String OP_STANDARD_DEVIANCE = "OP_STANDARD_DEVIANCE";
    public static final String OP_COUNT_YES = "OP_COUNT_YES";
    public static final String OP_COUNT_NO = "OP_COUNT_NO";

    static {
        operatorIDToNameMap.put(OP_ALL, I18N.getString(StatsOperatorsFactory.class, "all"));
        operatorIDToNameMap.put(OP_COUNT, I18N.getString(StatsOperatorsFactory.class, "count"));
        operatorIDToNameMap.put(OP_FIRST, I18N.getString(StatsOperatorsFactory.class, "first"));
        operatorIDToNameMap.put(OP_LAST, I18N.getString(StatsOperatorsFactory.class, "last"));
        operatorIDToNameMap.put(OP_AVG, I18N.getString(StatsOperatorsFactory.class, "average"));
        operatorIDToNameMap.put(OP_SUM, I18N.getString(StatsOperatorsFactory.class, "sum"));
        operatorIDToNameMap.put(OP_MIN, I18N.getString(StatsOperatorsFactory.class, "minimum"));
        operatorIDToNameMap.put(OP_MAX, I18N.getString(StatsOperatorsFactory.class, "maximum"));
        operatorIDToNameMap.put(OP_VARIANCE, I18N.getString(StatsOperatorsFactory.class, "variance"));
        operatorIDToNameMap.put(OP_STANDARD_DEVIANCE, I18N.getString(StatsOperatorsFactory.class, "standard-variation"));
        operatorIDToNameMap.put(OP_COUNT_YES, I18N.getString(StatsOperatorsFactory.class, "count-yes"));
        operatorIDToNameMap.put(OP_COUNT_NO, I18N.getString(StatsOperatorsFactory.class, "count-no"));
        operatorIDToShortNameMap.put(OP_ALL, "ALL");
        operatorIDToShortNameMap.put(OP_COUNT, "CNT");
        operatorIDToShortNameMap.put(OP_FIRST, "PRM");
        operatorIDToShortNameMap.put(OP_LAST, "ULT");
        operatorIDToShortNameMap.put(OP_AVG, "MED");
        operatorIDToShortNameMap.put(OP_SUM, "SUM");
        operatorIDToShortNameMap.put(OP_MIN, "MIN");
        operatorIDToShortNameMap.put(OP_MAX, "MAX");
        operatorIDToShortNameMap.put(OP_VARIANCE, "VAR");
        operatorIDToShortNameMap.put(OP_STANDARD_DEVIANCE, "D_E");
        operatorIDToShortNameMap.put(OP_COUNT_YES, "CSI");
        operatorIDToShortNameMap.put(OP_COUNT_NO, "CNO");
    }

    public static StatsOperatorsFactory getInstance() {
        return StatsOperatorsFactoryHolder.instance;
    }

    private StatsOperatorsFactory() {
    }

    public String getOperatorName(String operatorID) {
        return operatorIDToNameMap.get(operatorID);
    }

    public String getOperatorShortName(String operatorID) {
        return operatorIDToShortNameMap.get(operatorID);
    }

    public List<String> getOperatorsFor(AttributeType type) {
        ArrayList<String> validOps = new ArrayList<String>();
        Class<?> javaClass = type.toJavaClass();
        if (Number.class.isAssignableFrom(javaClass)) {
            validOps.add(OP_ALL);
            validOps.add(OP_COUNT);
            validOps.add(OP_FIRST);
            validOps.add(OP_LAST);
            validOps.add(OP_AVG);
            validOps.add(OP_SUM);
            validOps.add(OP_MIN);
            validOps.add(OP_MAX);
            validOps.add(OP_VARIANCE);
            validOps.add(OP_STANDARD_DEVIANCE);
        } else if (Date.class.isAssignableFrom(javaClass)) {
            validOps.add(OP_ALL);
            validOps.add(OP_COUNT);
            validOps.add(OP_FIRST);
            validOps.add(OP_LAST);
            validOps.add(OP_MIN);
            validOps.add(OP_MAX);
        } else if (Boolean.class.isAssignableFrom(javaClass)) {
            validOps.add(OP_ALL);
            validOps.add(OP_COUNT);
            validOps.add(OP_FIRST);
            validOps.add(OP_LAST);
            validOps.add(OP_COUNT_YES);
            validOps.add(OP_COUNT_NO);
        } else if (String.class.isAssignableFrom(javaClass)) {
            validOps.add(OP_ALL);
            validOps.add(OP_COUNT);
            validOps.add(OP_FIRST);
            validOps.add(OP_LAST);
        } else {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "there-are-no-statistic-operators-linked-to-attribute-{0}-{1}", new Object[]{type, javaClass}));
        }
        return validOps;
    }

    public String getColumnName(CalculateStatsDialog.StatPair statPair) {
        if (statPair.getOperatorID() == null) {
            return statPair.getFieldName();
        }
        return String.valueOf(this.getOperatorShortName(statPair.getOperatorID())) + "_" + statPair.getFieldName();
    }

    public List<String> getResultUniqueFieldsNames(List<CalculateStatsDialog.StatPair> statPairList) {
        ArrayList<String> res = new ArrayList<String>(statPairList.size());
        int maxLength = 11;
        int index = 0;
        for (CalculateStatsDialog.StatPair statPair : statPairList) {
            String uniqueFieldName = this.getColumnName(statPair);
            if (uniqueFieldName.length() > maxLength) {
                uniqueFieldName = uniqueFieldName.substring(0, maxLength);
            }
            int num = 1;
            while (res.contains(uniqueFieldName)) {
                String sub = "_" + num;
                int rest = uniqueFieldName.length() + sub.length() - maxLength;
                uniqueFieldName = rest > 0 ? String.valueOf(uniqueFieldName.substring(0, uniqueFieldName.length() - rest)) + sub : String.valueOf(uniqueFieldName) + sub;
                ++num;
            }
            res.add(index, uniqueFieldName);
            ++index;
        }
        return res;
    }

    public DbfFieldDef[] getDbfFieldsDefs(FeatureSchema featureSchema, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        DbfFieldDef[] dbfFieldsDefs = new DbfFieldDef[resultStatPairs.size()];
        List<AttributeType> attributeTypeList = this.getAttributeTypeFieldsList(featureSchema, resultStatPairs);
        List<String> uniqueFieldNames = this.getResultUniqueFieldsNames(resultStatPairs);
        int index = 0;
        while (index < resultStatPairs.size()) {
            dbfFieldsDefs[index] = this.getDbfFieldDef(uniqueFieldNames.get(index), attributeTypeList.get(index));
            ++index;
        }
        return dbfFieldsDefs;
    }

    public List<AttributeType> getAttributeTypeFieldsList(FeatureSchema featureSchema, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        ArrayList<AttributeType> attributeTypeList = new ArrayList<AttributeType>(resultStatPairs.size());
        for (CalculateStatsDialog.StatPair statPair : resultStatPairs) {
            String opId = statPair.getOperatorID();
            if (opId == null || opId == OP_FIRST || opId == OP_LAST || opId == OP_MAX || opId == OP_MIN) {
                attributeTypeList.add(featureSchema.getAttributeType(statPair.getFieldName()));
                continue;
            }
            if (opId == OP_AVG || opId == OP_STANDARD_DEVIANCE || opId == OP_SUM || opId == OP_VARIANCE) {
                attributeTypeList.add(AttributeType.DOUBLE);
                continue;
            }
            if (opId == OP_COUNT || opId == OP_COUNT_NO || opId == OP_COUNT_YES) {
                attributeTypeList.add(AttributeType.LONG);
                continue;
            }
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "operator-{0}-not-identified-dbf-return-as-string", new Object[]{opId}));
            attributeTypeList.add(AttributeType.STRING);
        }
        return attributeTypeList;
    }

    private DbfFieldDef getDbfFieldDef(String fieldName, AttributeType attType) {
        if (attType.equals(AttributeType.STRING) || attType.equals(AttributeType.CHAR) || attType.equals(AttributeType.VARCHAR) || attType.equals(AttributeType.LONGVARCHAR) || attType.equals(AttributeType.TEXT) || attType.equals(AttributeType.OBJECT)) {
            return new DbfFieldDef(fieldName, 'C', 255, 0);
        }
        if (attType.equals(AttributeType.INTEGER) || attType.equals(AttributeType.SMALLINT) || attType.equals(AttributeType.TINYINT) || attType.equals(AttributeType.BIT)) {
            return new DbfFieldDef(fieldName, 'N', 32, 0);
        }
        if (attType.equals(AttributeType.LONG) || attType.equals(AttributeType.BIGINT)) {
            return new DbfFieldDef(fieldName, 'N', 33, 0);
        }
        if (attType.equals(AttributeType.DOUBLE) || attType.equals(AttributeType.FLOAT) || attType.equals(AttributeType.REAL) || attType.equals(AttributeType.NUMERIC) || attType.equals(AttributeType.BIGDECIMAL) || attType.equals(AttributeType.DECIMAL)) {
            return new DbfFieldDef(fieldName, 'N', 33, 16);
        }
        if (attType.equals(AttributeType.DATE) || attType.equals(AttributeType.TIME) || attType.equals(AttributeType.TIMESTAMP)) {
            return new DbfFieldDef(fieldName, 'D', 8, 0);
        }
        if (attType.equals(AttributeType.BOOLEAN)) {
            return new DbfFieldDef(fieldName, 'L', 1, 0);
        }
        LOGGER.warn((Object)I18N.getMessage("org.saig.core.model.relations.Relation.Attribute-{0}-of-type-{1}-is-not-supported-it-will-be-used-a-string-instead", new Object[]{fieldName, attType}));
        return new DbfFieldDef(fieldName, 'C', 255, 0);
    }

    /*
     * Unable to fully structure code
     */
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, List<CalculateStatsDialog.StatPair> resultStatPairs, Set<String> availableOperators, int size, FeatureIterator itFeats, AbstractDataSource ds) throws Exception {
        block10: {
            statsList = new ArrayList<Object[]>();
            noGroupPksSet = new TreeSet<Object>();
            groupByMap = new TreeMap<Integer, TreeSet<Object>>(new TreeMapPermitNullComparator());
            numberOfFields = 0;
            if (resultStatPairs == null) {
                return statsList;
            }
            resultStatPairs.clear();
            if (!CollectionUtils.isEmpty(groupByFields)) {
                for (String queriedField : groupByFields) {
                    resultStatPairs.add(new CalculateStatsDialog.StatPair(queriedField, null));
                    ++numberOfFields;
                }
            }
            for (String currentField : operatorsByFieldMap.keySet()) {
                operatorsForCurrentField = operatorsByFieldMap.get(currentField);
                for (String currentOp : operatorsForCurrentField) {
                    if (!availableOperators.contains(currentOp)) continue;
                    resultStatPairs.add(new CalculateStatsDialog.StatPair(currentField, currentOp));
                    ++numberOfFields;
                }
            }
            if (resultStatPairs.isEmpty() || size < 1) {
                return statsList;
            }
            if (!CollectionUtils.isEmpty(groupByFields)) ** GOTO lbl36
            while (itFeats.hasNext()) {
                feat = itFeats.next();
                noGroupPksSet.add(feat.getPrimaryKey());
            }
            groupByMap.put(1, noGroupPksSet);
            break block10;
lbl-1000:
            // 1 sources

            {
                feat = itFeats.next();
                this.putPkIntoGroupMap(groupByMap, feat, groupByFields);
lbl36:
                // 2 sources

                ** while (itFeats.hasNext())
            }
        }
        pkSetsToProcessList = new ArrayList<TreeSet>();
        this.getRecursiveSet(groupByMap, pkSetsToProcessList);
        for (TreeSet pkTreeSet : pkSetsToProcessList) {
            result = new Object[resultStatPairs.size()];
            lastStatusCache = new StatisticalStatusCache();
            index = 0;
            while (index < result.length) {
                statPair = resultStatPairs.get(index);
                result[index] = this.getStatResult(pkTreeSet, statPair, groupByFields, lastStatusCache, ds);
                ++index;
            }
            statsList.add(result);
            pkTreeSet = null;
            lastStatusCache.clear();
            lastStatusCache = null;
        }
        pkSetsToProcessList.clear();
        pkSetsToProcessList = null;
        return statsList;
    }

    private void setupLastStatusCache(StatisticalStatusCache lastStatusCache, CalculateStatsDialog.StatPair statPair, List<String> groupByFields, TreeSet pkTreeSet, AbstractDataSource ds) {
        String field = statPair.getFieldName();
        String opId = statPair.getOperatorID();
        if (pkTreeSet.size() > 1 && !groupByFields.contains(field)) {
            if (!field.equals(lastStatusCache.getField())) {
                lastStatusCache.clear();
                lastStatusCache.setField(field);
            }
            if (opId == OP_MAX || opId == OP_MIN || opId == OP_COUNT_YES || opId == OP_COUNT_NO) {
                if (lastStatusCache.getValueArray() == null) {
                    lastStatusCache.setValueArray(this.getValueArray(pkTreeSet, field, ds));
                }
            } else if (opId == OP_SUM || opId == OP_AVG || opId == OP_VARIANCE || opId == OP_STANDARD_DEVIANCE) {
                if (lastStatusCache.getValueArray() == null) {
                    lastStatusCache.setValueArray(this.getValueArray(pkTreeSet, field, ds));
                }
                if (lastStatusCache.getDoubleArray() == null) {
                    lastStatusCache.setDoubleArray(this.getDoubleArray(lastStatusCache.getValueArray(), field));
                }
            }
            if ((opId == OP_SUM || opId == OP_AVG) && lastStatusCache.getCalculatedSum() == null) {
                lastStatusCache.setCalculatedSum(new Sum().evaluate(lastStatusCache.getDoubleArray()));
            }
            if ((opId == OP_VARIANCE || opId == OP_STANDARD_DEVIANCE) && lastStatusCache.getCalculatedVariance() == null) {
                lastStatusCache.setCalculatedVariance(new Variance(true).evaluate(lastStatusCache.getDoubleArray()));
            }
        }
    }

    private double[] getDoubleArray(Object[] valueArray, String field) {
        double[] res = new double[valueArray.length];
        int index = 0;
        int i = 0;
        while (i < valueArray.length) {
            Object obj = valueArray[i];
            if (obj != null) {
                res[index] = (Double)FeatureUtil.getGoodAttribute(AttributeType.DOUBLE, obj);
                ++index;
            }
            ++i;
        }
        return Arrays.copyOf(res, index);
    }

    private Object[] getValueArray(TreeSet pkTreeSet, String field, AbstractDataSource ds) {
        Object[] res = new Object[pkTreeSet.size()];
        int index = 0;
        Iterator iterator = pkTreeSet.iterator();
        while (iterator.hasNext()) {
            Object obj = ds.getByPrimaryKey(iterator.next()).getAttribute(field);
            if (obj == null) continue;
            res[index] = obj;
            ++index;
        }
        return Arrays.copyOf(res, index);
    }

    private Object getStatResult(TreeSet pkTreeSet, CalculateStatsDialog.StatPair statPair, List<String> groupByFields, StatisticalStatusCache statsCache, AbstractDataSource ds) {
        this.setupLastStatusCache(statsCache, statPair, groupByFields, pkTreeSet, ds);
        String field = statPair.getFieldName();
        String opId = statPair.getOperatorID();
        if (groupByFields.contains(field) || pkTreeSet.size() == 1) {
            Object value = ds.getByPrimaryKey(pkTreeSet.first()).getAttribute(field);
            int count = pkTreeSet.size();
            if (opId == null || opId == OP_FIRST || opId == OP_LAST || opId == OP_MAX || opId == OP_MIN) {
                return value;
            }
            if (opId == OP_AVG) {
                return FeatureUtil.getGoodAttribute(AttributeType.DOUBLE, value);
            }
            if (opId == OP_SUM) {
                if (value == null) {
                    return null;
                }
                return (Double)FeatureUtil.getGoodAttribute(AttributeType.DOUBLE, value) * (double)count;
            }
            if (opId == OP_STANDARD_DEVIANCE || opId == OP_VARIANCE) {
                return null;
            }
            if (opId == OP_COUNT_YES && value instanceof Boolean) {
                if (value == null) {
                    return null;
                }
                boolean bol = (Boolean)value;
                if (((Boolean)value).booleanValue()) {
                    return (long)count;
                }
                return 0L;
            }
            if (opId == OP_COUNT_NO && value instanceof Boolean) {
                if (value == null) {
                    return null;
                }
                boolean bol = (Boolean)value;
                if (!bol) {
                    return (long)count;
                }
                return 0L;
            }
            if (opId == OP_COUNT) {
                return (long)count;
            }
        } else {
            if (opId == OP_FIRST) {
                return ds.getByPrimaryKey(pkTreeSet.first()).getAttribute(field);
            }
            if (opId == OP_LAST) {
                return ds.getByPrimaryKey(pkTreeSet.last()).getAttribute(field);
            }
            if (opId == OP_MAX) {
                return this.getMax(statsCache.getValueArray());
            }
            if (opId == OP_MIN) {
                return this.getMin(statsCache.getValueArray());
            }
            if (opId == OP_SUM) {
                return statsCache.getCalculatedSum();
            }
            if (opId == OP_AVG) {
                return statsCache.getCalculatedSum() / (double)pkTreeSet.size();
            }
            if (opId == OP_STANDARD_DEVIANCE) {
                return Math.sqrt(statsCache.getCalculatedVariance());
            }
            if (opId == OP_VARIANCE) {
                return statsCache.getCalculatedVariance();
            }
            if (opId == OP_COUNT) {
                return (long)pkTreeSet.size();
            }
            if (opId == OP_COUNT_NO) {
                return this.countBoolean(statsCache.getValueArray(), false);
            }
            if (opId == OP_COUNT_YES) {
                return this.countBoolean(statsCache.getValueArray(), true);
            }
        }
        return null;
    }

    private Long countBoolean(Object[] lastUsedValueArray, boolean targetBoolean) {
        Long count = 0L;
        int i = 0;
        while (i < lastUsedValueArray.length) {
            Object obj = lastUsedValueArray[i];
            if (obj instanceof Boolean && (Boolean)obj == targetBoolean) {
                count = count + 1L;
            }
            ++i;
        }
        return count;
    }

    private Object getMax(Object[] valueArray) {
        Object max = valueArray[0];
        int i = 1;
        while (i < valueArray.length) {
            Comparable comparable;
            Object obj = valueArray[i];
            if (obj != null && (comparable = (Comparable)obj).compareTo(max) > 0) {
                max = obj;
            }
            ++i;
        }
        return max;
    }

    private Object getMin(Object[] valueArray) {
        Object min = valueArray[0];
        int i = 1;
        while (i < valueArray.length) {
            Comparable comparable;
            Object obj = valueArray[i];
            if (obj != null && (comparable = (Comparable)obj).compareTo(min) < 0) {
                min = obj;
            }
            ++i;
        }
        return min;
    }

    private void getRecursiveSet(TreeMap groupByMap, List<TreeSet> pkSetsToProcessList) {
        for (Object key : groupByMap.keySet()) {
            Object value = groupByMap.get(key);
            if (value instanceof TreeMap) {
                this.getRecursiveSet((TreeMap)value, pkSetsToProcessList);
                continue;
            }
            if (!(value instanceof TreeSet)) continue;
            pkSetsToProcessList.add((TreeSet)value);
        }
        groupByMap.clear();
        groupByMap = null;
    }

    private void putPkIntoGroupMap(TreeMap groupByMap, Feature feature, List<String> groupByFields) {
        TreeMap tmpMap = groupByMap;
        Iterator<String> itFieldsNames = groupByFields.iterator();
        while (itFieldsNames.hasNext()) {
            String fieldName = itFieldsNames.next();
            Object obj = feature.getAttribute(fieldName);
            if (!itFieldsNames.hasNext()) {
                TreeSet<Object> set = (TreeSet<Object>)tmpMap.get(obj);
                if (set == null) {
                    set = new TreeSet<Object>();
                    tmpMap.put(obj, set);
                }
                set.add(feature.getPrimaryKey());
                continue;
            }
            TreeMap map = (TreeMap)tmpMap.get(obj);
            if (map == null) {
                map = new TreeMap(new TreeMapPermitNullComparator());
                tmpMap.put(obj, map);
            }
            tmpMap = map;
        }
    }

    /* synthetic */ StatsOperatorsFactory(StatsOperatorsFactory statsOperatorsFactory) {
        this();
    }

    private static class StatisticalStatusCache {
        private String field;
        private Object[] valueArray;
        private double[] doubleArray;
        private Double calculatedSum;
        private Double calculatedVariance;

        public StatisticalStatusCache() {
            this.clear();
        }

        public String getField() {
            return this.field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object[] getValueArray() {
            return this.valueArray;
        }

        public void setValueArray(Object[] valueArray) {
            this.valueArray = valueArray;
        }

        public double[] getDoubleArray() {
            return this.doubleArray;
        }

        public void setDoubleArray(double[] doubleArray) {
            this.doubleArray = doubleArray;
        }

        public Double getCalculatedSum() {
            return this.calculatedSum;
        }

        public void setCalculatedSum(Double calculatedSum) {
            this.calculatedSum = calculatedSum;
        }

        public Double getCalculatedVariance() {
            return this.calculatedVariance;
        }

        public void setCalculatedVariance(Double calculatedVariance) {
            this.calculatedVariance = calculatedVariance;
        }

        public void clear() {
            this.field = null;
            this.valueArray = null;
            this.doubleArray = null;
            this.calculatedSum = null;
            this.calculatedVariance = null;
        }
    }

    private static class StatsOperatorsFactoryHolder {
        private static final StatsOperatorsFactory instance = new StatsOperatorsFactory(null);

        private StatsOperatorsFactoryHolder() {
        }
    }

    private static class TreeMapPermitNullComparator
    implements Comparator {
        private TreeMapPermitNullComparator() {
        }

        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            Comparable comparableO1 = (Comparable)o1;
            return comparableO1.compareTo(o2);
        }
    }
}

