/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package es.kosmo.desktop.utils;

import com.vividsolutions.jump.feature.AttributeType;
import es.kosmo.core.crs.CrsAxisOrder;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.Crs;
import org.gvsig.crs.ICrs;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;

public class GUITranslationsUtils {
    private static final Logger LOGGER = Logger.getLogger(GUITranslationsUtils.class);
    protected static final Map<Integer, String> GEOMETRY_TYPE_NAMES = new HashMap<Integer, String>();
    protected static Map<String, String> srsCodeToNameMap = new TreeMap<String, String>();

    static {
        GEOMETRY_TYPE_NAMES.put(new Integer(1), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.point"));
        GEOMETRY_TYPE_NAMES.put(new Integer(8), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.multipoint"));
        GEOMETRY_TYPE_NAMES.put(new Integer(3), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.lineal"));
        GEOMETRY_TYPE_NAMES.put(new Integer(2), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.multilineal"));
        GEOMETRY_TYPE_NAMES.put(new Integer(5), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.polygonal"));
        GEOMETRY_TYPE_NAMES.put(new Integer(4), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.multipolygonal"));
        GEOMETRY_TYPE_NAMES.put(new Integer(0), I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.unknow"));
    }

    public static String getAttributeTypesDescription(Set<AttributeType> types) {
        TreeSet<String> processed = new TreeSet<String>();
        StringBuffer result = new StringBuffer();
        result.append("(");
        if (CollectionUtils.isNotEmpty(types)) {
            for (AttributeType type : types) {
                processed.add(GUITranslationsUtils.getAttributeTypeName(type));
            }
            for (String str : processed) {
                result.append(String.valueOf(str) + ", ");
            }
        }
        if (result.length() > 1) {
            result = result.delete(result.length() - 2, result.length());
        }
        result.append(")");
        return result.toString();
    }

    public static String getAttributeTypeName(AttributeType type) {
        String translation = null;
        translation = AttributeType.STRING.equals(type) || AttributeType.LONGVARCHAR.equals(type) || AttributeType.TEXT.equals(type) || AttributeType.VARCHAR.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.String") : (AttributeType.INTEGER.equals(type) || AttributeType.SMALLINT.equals(type) || AttributeType.TINYINT.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Integer") : (AttributeType.LONG.equals(type) || AttributeType.BIGINT.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Long") : (AttributeType.FLOAT.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Float") : (AttributeType.DOUBLE.equals(type) || AttributeType.BIGDECIMAL.equals(type) || AttributeType.DECIMAL.equals(type) || AttributeType.NUMERIC.equals(type) || AttributeType.REAL.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Double") : (AttributeType.DATE.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Date") : (AttributeType.TIMESTAMP.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Timestamp") : (AttributeType.BOOLEAN.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Boolean-Yes-No") : (AttributeType.GEOMETRY.equals(type) ? I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Geometry") : type.getName()))))))));
        return translation;
    }

    public static String getGeometryTypeDescription(int[] geomTypes) {
        String result = "(";
        int i = 0;
        while (i < geomTypes.length) {
            result = String.valueOf(result) + GUITranslationsUtils.getGeometryName(geomTypes[i]) + ", ";
            ++i;
        }
        if (result.length() > 1) {
            result = result.substring(0, result.length() - 2);
        }
        result = String.valueOf(result) + ")";
        return result;
    }

    public static String getGeometryName(int geomType) {
        Integer geometryType = geomType;
        String type = null;
        type = GEOMETRY_TYPE_NAMES.containsKey(geometryType) ? GEOMETRY_TYPE_NAMES.get(geomType) : GEOMETRY_TYPE_NAMES.get(0);
        return type;
    }

    public static String getAxisOrderDescription(CrsAxisOrder axisOrder) {
        String description;
        switch (axisOrder) {
            case NORTH_EAST: {
                description = String.valueOf(I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Latitude")) + " / " + I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Longitude") + " (N/E)";
                break;
            }
            case WEST_SOUTH: {
                description = String.valueOf(I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Longitude")) + " / " + I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Latitude") + " (W/S)";
                break;
            }
            case SOUTH_WEST: {
                description = String.valueOf(I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Latitude")) + " / " + I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Longitude") + " (S/W)";
                break;
            }
            default: {
                description = String.valueOf(I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Longitude")) + " / " + I18N.getString("es.kosmo.desktop.utils.GUITranslationsUtils.Latitude") + " (E/N)";
            }
        }
        return description;
    }

    public static String getName(String srsCode) {
        if (srsCodeToNameMap.containsKey(srsCode)) {
            return srsCodeToNameMap.get(srsCode);
        }
        ICrs crs = null;
        try {
            crs = CrsRepositoryManager.getInstance().getCRS(srsCode);
        }
        catch (Exception e) {
            LOGGER.debug((Object)(String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSUtils.Spatial-Reference-System-{0}-unknown", new Object[]{crs})) + ": " + e.getMessage()));
        }
        String name = crs != null ? GUITranslationsUtils.getCRSDescription(crs) : srsCode;
        srsCodeToNameMap.put(srsCode, name);
        return name;
    }

    public static String getCRSDescription(IProjection proj) {
        String solucion = EPSGSelectionDialog.NO_SRS_DEFINED;
        String crsName = null;
        if (proj != null) {
            Crs crs;
            String crsAbrev = proj.getAbrev();
            if (proj instanceof Crs && (crs = (Crs)proj).getCrsWkt() != null) {
                crsName = !crs.getCrsWkt().getProjcs().equals("") ? crs.getCrsWkt().getProjcs() : crs.getCrsWkt().getGeogcs();
            }
            if (crsName == null && (crsName = proj.getAbrev()).startsWith("EPSG:")) {
                crsName = crsName.substring("EPSG:".length(), crsName.length());
            }
            solucion = String.valueOf(crsAbrev) + " - " + crsName;
        }
        return solucion;
    }
}

