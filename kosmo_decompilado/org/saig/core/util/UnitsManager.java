/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.Measure
 *  javax.measure.MeasureFormat
 *  javax.measure.converter.UnitConverter
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.NonSI
 *  javax.measure.unit.SI
 *  javax.measure.unit.Unit
 *  javax.measure.unit.UnitFormat
 *  org.apache.commons.collections.BidiMap
 *  org.apache.commons.collections.bidimap.DualHashBidiMap
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.util;

import com.vividsolutions.jump.workbench.model.Task;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Vector;
import javax.measure.Measure;
import javax.measure.MeasureFormat;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.util.NumberFormatManager;
import org.saig.jump.lang.I18N;

public class UnitsManager {
    private static final Logger LOGGER = Logger.getLogger(UnitsManager.class);
    public static final String UNIT_INCHES = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.inches");
    public static final String UNIT_FEETS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.feets");
    public static final String UNIT_YARDS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.yards");
    public static final String UNIT_MILES = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.miles");
    public static final String UNIT_MILIMETERS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.milimeters");
    public static final String UNIT_CENTIMETERS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.centimeters");
    public static final String UNIT_METERS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.meters");
    public static final String UNIT_KILOMETERS = I18N.getString("org.saig.jump.widgets.config.UnitsToolsPanel.kilometers");
    public static final String UNIT_GRADES = I18N.getString("org.saig.core.util.UnitsManager.degrees");
    public static final String UNIT_NAUTICAL_MILES = I18N.getString("org.saig.core.util.UnitsManager.nautical-miles");
    public static final String UNIT_SQUARE_METERS = I18N.getString("org.saig.core.util.UnitsManager.square-meters");
    public static final String UNIT_HECTAREAS = I18N.getString("org.saig.core.util.UnitsManager.hectares");
    public static final String UNIT_AREAS = I18N.getString("org.saig.core.util.UnitsManager.ares");
    private static BidiMap humanNameToUnitConverter = new DualHashBidiMap();
    private static Vector<Unit<Length>> defaultLengthUnits;
    private static Vector<Unit<Length>> defaultSILengthUnits;
    private static Vector<Unit<Length>> defaultNonSILengthUnits;
    private static Vector<Unit<Area>> defaultAreaUnits;
    public static Unit<Length> DEFAULT_LENGTH_UNIT;
    public static Unit<Area> DEFAULT_AREA_UNIT;
    public static Unit<Length> GRADES_LENGTH_UNIT;
    public static final int NON_SI_SYSTEM = 1;
    public static final int SI_SYSTEM = 2;

    static {
        DEFAULT_LENGTH_UNIT = SI.METER;
        DEFAULT_AREA_UNIT = SI.SQUARE_METRE;
        GRADES_LENGTH_UNIT = SI.METER.times(111319.49079327358);
        UnitFormat.getInstance().label(GRADES_LENGTH_UNIT, "\u00ba");
        defaultLengthUnits = new Vector();
        defaultSILengthUnits = new Vector();
        defaultNonSILengthUnits = new Vector();
        defaultSILengthUnits.add((Unit<Length>)SI.KILOMETER);
        defaultSILengthUnits.add(DEFAULT_LENGTH_UNIT);
        defaultSILengthUnits.add((Unit<Length>)SI.CENTIMETER);
        defaultSILengthUnits.add((Unit<Length>)SI.MILLIMETER);
        defaultNonSILengthUnits.add((Unit<Length>)NonSI.MILE);
        defaultNonSILengthUnits.add((Unit<Length>)NonSI.YARD);
        defaultNonSILengthUnits.add((Unit<Length>)NonSI.FOOT);
        defaultNonSILengthUnits.add((Unit<Length>)NonSI.INCH);
        defaultNonSILengthUnits.add(GRADES_LENGTH_UNIT);
        defaultLengthUnits.addAll(defaultSILengthUnits);
        defaultLengthUnits.addAll(defaultNonSILengthUnits);
        defaultLengthUnits.add((Unit<Length>)NonSI.NAUTICAL_MILE);
        defaultAreaUnits = new Vector();
        defaultAreaUnits.add(DEFAULT_AREA_UNIT);
        defaultAreaUnits.add((Unit<Area>)NonSI.HECTARE);
        defaultAreaUnits.add((Unit<Area>)NonSI.ARE);
        humanNameToUnitConverter.put((Object)UNIT_KILOMETERS, (Object)SI.KILOMETER);
        humanNameToUnitConverter.put((Object)UNIT_METERS, (Object)SI.METER);
        humanNameToUnitConverter.put((Object)UNIT_CENTIMETERS, (Object)SI.CENTIMETER);
        humanNameToUnitConverter.put((Object)UNIT_MILIMETERS, (Object)SI.MILLIMETER);
        humanNameToUnitConverter.put((Object)UNIT_NAUTICAL_MILES, (Object)NonSI.NAUTICAL_MILE);
        humanNameToUnitConverter.put((Object)UNIT_INCHES, (Object)NonSI.INCH);
        humanNameToUnitConverter.put((Object)UNIT_FEETS, (Object)NonSI.FOOT);
        humanNameToUnitConverter.put((Object)UNIT_YARDS, (Object)NonSI.YARD);
        humanNameToUnitConverter.put((Object)UNIT_MILES, (Object)NonSI.MILE);
        humanNameToUnitConverter.put((Object)UNIT_GRADES, GRADES_LENGTH_UNIT);
        humanNameToUnitConverter.put((Object)UNIT_SQUARE_METERS, (Object)SI.SQUARE_METRE);
        humanNameToUnitConverter.put((Object)UNIT_AREAS, (Object)NonSI.ARE);
        humanNameToUnitConverter.put((Object)UNIT_HECTAREAS, (Object)NonSI.HECTARE);
    }

    public static Vector<Unit<Length>> getDefaultLengthUnits() {
        return defaultLengthUnits;
    }

    public static Vector<Unit<Area>> getDefaultAreaUnits() {
        return defaultAreaUnits;
    }

    public static double convertDistanceValue(double sourceDistanceValue, Unit<Length> sourceUnit, Unit<Length> targetUnit) {
        UnitConverter sourceToTargetConverter = sourceUnit.getConverterTo(targetUnit);
        return sourceToTargetConverter.convert(sourceDistanceValue);
    }

    public static String convertDistanceValueToString(double sourceDistanceValue, Unit<Length> sourceUnit, Unit<Length> targetUnit) {
        Measure d = Measure.valueOf((double)sourceDistanceValue, sourceUnit);
        return MeasureFormat.getInstance((NumberFormat)NumberFormatManager.getDefaultNumberFormat(), (UnitFormat)UnitFormat.getInstance((Locale)I18N.getLocale())).format((Object)d.to(targetUnit));
    }

    public static double convertAreaValue(double sourceAreaValue, Unit<Area> sourceUnit, Unit<Area> targetUnit) {
        UnitConverter sourceToTargetConverter = sourceUnit.getConverterTo(targetUnit);
        return sourceToTargetConverter.convert(sourceAreaValue);
    }

    public static String convertAreaValueToString(double sourceDistanceValue, Unit<Area> sourceUnit, Unit<Area> targetUnit) {
        Measure d = Measure.valueOf((double)sourceDistanceValue, sourceUnit);
        return MeasureFormat.getInstance((NumberFormat)NumberFormatManager.getDefaultNumberFormat(), (UnitFormat)UnitFormat.getInstance((Locale)I18N.getLocale())).format((Object)d.to(targetUnit));
    }

    public static Unit<Area> convertToAreaUnit(Unit<Length> mapLengthUnit) {
        Unit result = null;
        result = mapLengthUnit.pow(2);
        return result;
    }

    public static Unit<Length> getMapLengthUnit(Task newTask) {
        String userUnit = newTask.getMapLengthUnit();
        if (StringUtils.isEmpty((String)userUnit)) {
            String oldUnit = newTask.getMapUnits();
            if (humanNameToUnitConverter.get((Object)oldUnit) != null) {
                return (Unit)humanNameToUnitConverter.get((Object)oldUnit);
            }
            return DEFAULT_LENGTH_UNIT;
        }
        return UnitsManager.getLengthUnit(userUnit);
    }

    public static Unit<Length> getUserLengthUnit(Task newTask) {
        String userUnit = newTask.getUserLengthUnit();
        if (StringUtils.isEmpty((String)userUnit)) {
            String oldUnit = newTask.getAltUnits();
            if (humanNameToUnitConverter.get((Object)oldUnit) != null) {
                return (Unit)humanNameToUnitConverter.get((Object)oldUnit);
            }
            return DEFAULT_LENGTH_UNIT;
        }
        return UnitsManager.getLengthUnit(userUnit);
    }

    public static Unit<Area> getUserAreaUnit(Task newTask) {
        String areaUnit = newTask.getUserAreaUnit();
        if (StringUtils.isEmpty((String)areaUnit)) {
            return DEFAULT_AREA_UNIT;
        }
        return UnitsManager.getAreaUnit(areaUnit);
    }

    public static Unit<Length> getLengthUnit(String lengthUnits) {
        try {
            return Unit.valueOf((CharSequence)lengthUnits);
        }
        catch (IllegalArgumentException iae) {
            LOGGER.warn((Object)iae);
            return DEFAULT_LENGTH_UNIT;
        }
    }

    public static Unit<Area> getAreaUnit(String areaMeasureUnits) {
        try {
            return Unit.valueOf((CharSequence)areaMeasureUnits);
        }
        catch (IllegalArgumentException iae) {
            LOGGER.warn((Object)iae);
            return DEFAULT_AREA_UNIT;
        }
    }

    public static void main(String[] args) throws Exception {
        Unit sourceUnit = SI.SQUARE_METRE;
        Unit targetUnit = SI.KILO((Unit)SI.SQUARE_METRE);
        double valueInMeters = 2.5;
        LOGGER.info((Object)("JScience system -> Source unit " + sourceUnit + ", Target Unit " + targetUnit + " -> Value: " + valueInMeters + " Result: " + UnitsManager.convertAreaValue(valueInMeters, (Unit<Area>)sourceUnit, (Unit<Area>)targetUnit)));
        LOGGER.info((Object)("Value " + UnitsManager.convertAreaValue(valueInMeters, (Unit<Area>)sourceUnit, (Unit<Area>)targetUnit) + " " + UnitFormat.getInstance((Locale)I18N.getLocale()).format((Object)targetUnit)));
        Measure d = Measure.valueOf((double)valueInMeters, (Unit)sourceUnit);
        LOGGER.info((Object)("Value 2 " + d.to(targetUnit)));
        LOGGER.info((Object)("Value 3 " + UnitsManager.convertAreaValueToString(valueInMeters, (Unit<Area>)sourceUnit, (Unit<Area>)targetUnit)));
    }

    public static String getNameForUnit(Unit<?> unitValue) {
        String result = (String)humanNameToUnitConverter.getKey(unitValue);
        if (result == null) {
            result = "";
        }
        return result;
    }

    public static Unit<Length> getLengthUnitFromName(String name) {
        Unit result = DEFAULT_LENGTH_UNIT;
        if (humanNameToUnitConverter.get((Object)name) != null) {
            result = (Unit)humanNameToUnitConverter.get((Object)name);
        } else {
            try {
                result = Unit.valueOf((CharSequence)name);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        }
        return result;
    }

    public static Unit<Area> getAreaUnitFromName(String name) {
        Unit result = DEFAULT_AREA_UNIT;
        if (humanNameToUnitConverter.get((Object)name) != null) {
            result = (Unit)humanNameToUnitConverter.get((Object)name);
        } else {
            try {
                result = Unit.valueOf((CharSequence)name);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        }
        return result;
    }

    public static int getUnitsSystem(Unit<Length> mapLengthUnit) {
        if (NonSI.getInstance().getUnits().contains(mapLengthUnit)) {
            return 1;
        }
        SI.getInstance().getUnits();
        return 2;
    }

    public static Collection<Unit<Length>> getNonSILengthUnits() {
        return defaultNonSILengthUnits;
    }

    public static Collection<Unit<Length>> getSILengthUnits() {
        return defaultSILengthUnits;
    }
}

