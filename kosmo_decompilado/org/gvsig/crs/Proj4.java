/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.units.ConversionException
 *  javax.units.Unit
 *  org.geotools.referencing.crs.DefaultGeographicCRS
 *  org.geotools.referencing.crs.DefaultProjectedCRS
 *  org.geotools.referencing.datum.DefaultGeodeticDatum
 *  org.geotools.referencing.datum.DefaultPrimeMeridian
 *  org.opengis.metadata.Identifier
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 *  org.opengis.referencing.datum.Ellipsoid
 *  org.opengis.referencing.datum.PrimeMeridian
 */
package org.gvsig.crs;

import com.iver.andami.PluginServices;
import java.util.ArrayList;
import java.util.List;
import javax.units.ConversionException;
import javax.units.Unit;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;

public class Proj4 {
    private List<String[]> projectionNameList = new ArrayList<String[]>();
    private List<String[]> unitNameList = new ArrayList<String[]>();
    private List<String[]> projectionParameterNameList = new ArrayList<String[]>();
    private List<String[]> projectionParameterList = new ArrayList<String[]>();
    private List<String[]> projectionParameterDefaultValueList = new ArrayList<String[]>();
    private List<String[]> projectionParameterMaxValueList = new ArrayList<String[]>();
    private List<String[]> projectionParameterMinValueList = new ArrayList<String[]>();
    private List<String[]> projectionParameterUnitList = new ArrayList<String[]>();
    private List<String[]> projectionAcronymList = new ArrayList<String[]>();
    private List<String[]> projectionParameterAcronymList = new ArrayList<String[]>();
    int divider = 10000;
    private static double angularTolerance = 2.777777777777778E-4;
    private static final double EPS = 1.0E-8;

    public Proj4() throws CrsException {
        this.defineUnitNameList();
        this.defineProjectionParameterList();
        this.defineProjections();
    }

    private void defineUnitNameList() throws CrsException {
        int count = 0;
        String[] unitName = new String[]{"Angular"};
        this.unitNameList.add(count, unitName);
        unitName = new String[]{"Linear"};
        this.unitNameList.add(++count, unitName);
        unitName = new String[]{"Unitless"};
        this.unitNameList.add(++count, unitName);
        this.addUnitName(count, "Adimensional");
    }

    private void defineProjectionParameterList() throws CrsException {
        int count = 0;
        String[] parameterName = new String[]{"azimuth"};
        this.projectionParameterNameList.add(count, parameterName);
        this.addProjectionParameterName(count, "Azimuth of initial line");
        this.addProjectionParameterName(count, "AzimuthAngle");
        String[] parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Longitude of natural origin");
        this.addProjectionParameterName(count, "NatOriginLong");
        this.addProjectionParameterName(count, "Longitude of projection center");
        this.addProjectionParameterName(count, "Longitude_of_center");
        this.addProjectionParameterName(count, "ProjCenterLong");
        this.addProjectionParameterName(count, "Longitude of false origin");
        this.addProjectionParameterName(count, "FalseOriginLong");
        this.addProjectionParameterName(count, "StraightVertPoleLong");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"false_easting"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Easting at projection centre");
        this.addProjectionParameterName(count, "Easting of false origin");
        this.addProjectionParameterName(count, "FalseEasting");
        this.addProjectionParameterName(count, "False_Easting");
        this.addProjectionParameterName(count, "FalseOriginEasting");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"false_northing"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Northing at projection centre");
        this.addProjectionParameterName(count, "Northing of false origin");
        this.addProjectionParameterName(count, "FalseNorthing");
        this.addProjectionParameterName(count, "False_Northing");
        this.addProjectionParameterName(count, "FalseOriginNorthing");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "CenterLat");
        this.addProjectionParameterName(count, "FalseOriginLat");
        this.addProjectionParameterName(count, "Latitude of false origin");
        this.addProjectionParameterName(count, "Latitude_of_origin");
        this.addProjectionParameterName(count, "Latitude of natural origin");
        this.addProjectionParameterName(count, "Latitude of projection center");
        this.addProjectionParameterName(count, "Latitude of projection centre");
        this.addProjectionParameterName(count, "NatOriginLat");
        this.addProjectionParameterName(count, "ProjCenterLat");
        this.addProjectionParameterName(count, "Spherical_latitude_of_origin");
        this.addProjectionParameterName(count, "Central_Parallel");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"Latitude_Of_1st_Point"};
        this.projectionParameterNameList.add(++count, parameterName);
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"Latitude_Of_2nd_Point"};
        this.projectionParameterNameList.add(++count, parameterName);
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "CenterLat");
        this.addProjectionParameterName(count, "FalseOriginLat");
        this.addProjectionParameterName(count, "Latitude of center");
        this.addProjectionParameterName(count, "Latitude of false origin");
        this.addProjectionParameterName(count, "Latitude of natural origin");
        this.addProjectionParameterName(count, "Latitude of projection center");
        this.addProjectionParameterName(count, "Latitude of projection centre");
        this.addProjectionParameterName(count, "NatOriginLat");
        this.addProjectionParameterName(count, "ProjCenterLat");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"latitude_of_standard_parallel"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "CenterLat");
        this.addProjectionParameterName(count, "FalseOriginLat");
        this.addProjectionParameterName(count, "Latitude of center");
        this.addProjectionParameterName(count, "Latitude of false origin");
        this.addProjectionParameterName(count, "Latitude of natural origin");
        this.addProjectionParameterName(count, "Latitude of projection center");
        this.addProjectionParameterName(count, "Latitude of projection centre");
        this.addProjectionParameterName(count, "Latitude_of_standard_parallel");
        this.addProjectionParameterName(count, "NatOriginLat");
        this.addProjectionParameterName(count, "ProjCenterLat");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"longitude_of_center"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Longitude of origin");
        this.addProjectionParameterName(count, "Longitude of false origin");
        this.addProjectionParameterName(count, "NatOriginLong");
        this.addProjectionParameterName(count, "central_meridian");
        this.addProjectionParameterName(count, "CenterLong");
        this.addProjectionParameterName(count, "Spherical_latitude_of_origin");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"Longitude_Of_1st_Point"};
        this.projectionParameterNameList.add(++count, parameterName);
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"Longitude_Of_2nd_Point"};
        this.projectionParameterNameList.add(++count, parameterName);
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"pseudo_standard_parallel_1"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Latitude of Pseudo Standard Parallel");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"rectified_grid_angle"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Angle from Rectified to Skew Grid");
        this.addProjectionParameterName(count, "XY_Plane_Rotation");
        this.addProjectionParameterName(count, "RectifiedGridAngle");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"satellite_height"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Satellite Height");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"scale_factor"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Scale factor at natural origin");
        this.addProjectionParameterName(count, "ScaleAtNatOrigin");
        this.addProjectionParameterName(count, "ScaleAtCenter");
        parameterUnit = new String[]{"Unitless"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"standard_parallel_1"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Latitude of first standard parallel");
        this.addProjectionParameterName(count, "Latitude of origin");
        this.addProjectionParameterName(count, "StdParallel1");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"standard_parallel_2"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "Latitude of second standard parallel");
        this.addProjectionParameterName(count, "StdParallel2");
        parameterUnit = new String[]{"Angular"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"semi_major"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "semi_major_axis");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"semi_minor"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "semi_minor_axis");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
        parameterName = new String[]{"Height"};
        this.projectionParameterNameList.add(++count, parameterName);
        this.addProjectionParameterName(count, "altitude");
        parameterUnit = new String[]{"Linear"};
        this.projectionParameterUnitList.add(count, parameterUnit);
    }

    private void defineProjections() throws CrsException {
        int count = 0;
        String[] projectionName = new String[]{"Aitoff"};
        this.projectionNameList.add(count, projectionName);
        String[] parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        String[] parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        String[] parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        String[] parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        String[] parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        String[] projectionAcronym = new String[]{"aitoff"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Albers_Conic_Equal_Area"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Albers Equal-Area Conic");
        this.addProjectionName(count, "Albers Equal Area");
        this.addProjectionName(count, "9822");
        parameterName = new String[]{"standard_parallel_1"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_2");
        this.addProjectionParameter(count, "latitude_of_center");
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_1"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "lat_0");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"aea"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Azimuthal_Equidistant"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Azimuthal Equidistant");
        this.addProjectionName(count, "Azimuthal-Equidistant");
        this.addProjectionName(count, "Postel");
        this.addProjectionName(count, "Zenithal Equidistant");
        this.addProjectionName(count, "Zenithal-Equidistant");
        this.addProjectionName(count, "Zenithal_Equidistant");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"aeqd"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Bonne"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Bonne");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_1");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"bonne"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Cassini_Soldner"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Cassini-Soldner");
        this.addProjectionName(count, "Cassini");
        this.addProjectionName(count, "9806");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"cass"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Craster_Parabolic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Craster-Parabolic");
        this.addProjectionName(count, "Craster Parabolic");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"craster"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Cylindrical_Equal_Area"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Cylindrical Equal Area");
        this.addProjectionName(count, "Normal Authalic Cylindrical (FME)");
        this.addProjectionName(count, "Lambert Cylindrical Equal Area");
        this.addProjectionName(count, "Lambert_Cylindrical_Equal_Area");
        this.addProjectionName(count, "Behrmann (standard parallel = 30)");
        this.addProjectionName(count, "Behrmann");
        this.addProjectionName(count, "Gall Orthographic (standard parallel = 45)");
        this.addProjectionName(count, "Gall Orthographic");
        this.addProjectionName(count, "Gall_Orthographic");
        this.addProjectionName(count, "Peters (approximated by Gall Orthographic)");
        this.addProjectionName(count, "Peters");
        this.addProjectionName(count, "Lambert Cylindrical Equal Area (Spherical)");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"cea"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_I"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert I");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck1"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_II"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert II");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck2"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_III"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert III");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck3"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_IV"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert IV");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck4"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_V"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert V");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck5"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Eckert_VI"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Eckert VI");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eck6"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Equidistant_Conic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Equidistant Conic");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "standard_parallel_2");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "lat_1");
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eqdc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Equirectangular"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Plate Caree");
        this.addProjectionName(count, "Plate Carree");
        this.addProjectionName(count, "Plate_Caree");
        this.addProjectionName(count, "Plate_Carree");
        this.addProjectionName(count, "Equidistant Cylindrical");
        this.addProjectionName(count, "Equidistant_Cylindrical");
        this.addProjectionName(count, "9823");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_ts"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"eqc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"McBryde_Thomas_Flat_Polar_Quartic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "McBryde-Thomas-Flat-Polar-Quartic");
        this.addProjectionName(count, "McBryde Thomas Flat Polar Quartic");
        this.addProjectionName(count, "Flat Polar Quartic");
        this.addProjectionName(count, "Flat-Polar-Quartic");
        this.addProjectionName(count, "Flat_Polar_Quartic");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"mbtfpq"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Gall_Stereographic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Gall Stereograpic");
        this.addProjectionName(count, "Gall");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"gall"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"GEOS"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Geostationary Satellite View");
        this.addProjectionName(count, "Normalized Geostationary Projection");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "satellite_height");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "h");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "35785831.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"geos"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Gnomonic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Gnomonic");
        this.addProjectionName(count, "Central");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"gnom"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Goode"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Homolosine");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"goode"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Hammer_Aitoff"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Hammer Aitoff");
        this.addProjectionName(count, "Hammer-Aitoff");
        this.addProjectionName(count, "Hammer");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"hammer"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Krovak"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Krovak Oblique Conic Conformal");
        this.addProjectionName(count, "9819");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"krovak"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Laborde_Madagascar"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Laborde Madagascar");
        this.addProjectionName(count, "Laborde");
        this.addProjectionName(count, "9813");
        parameterName = new String[]{};
        this.projectionParameterList.add(count, parameterName);
        parameterAcronym = new String[]{};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        parameterDefaultValue = new String[]{};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        parameterMaxValue = new String[]{};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        parameterMinValue = new String[]{};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        projectionAcronym = new String[]{"labrd"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Azimuthal_Equal_Area"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Azimuthal Equal Area");
        this.addProjectionName(count, "Lambert Azimuthal Equal Area (Spherical)");
        this.addProjectionName(count, "Lambert_Azimuthal_Equal_Area_(Spherical)");
        this.addProjectionName(count, "Lorgna");
        this.addProjectionName(count, "Zenithal Equal Area");
        this.addProjectionName(count, "Zenithal Equal-Area");
        this.addProjectionName(count, "Zenithal_Equal-Area");
        this.addProjectionName(count, "Zenithal-Equal-Area");
        this.addProjectionName(count, "Zenithal Eqivalent");
        this.addProjectionName(count, "Zenithal-Eqivalent");
        this.addProjectionName(count, "Zenithal_Eqivalent");
        this.addProjectionName(count, "9820");
        this.addProjectionName(count, "9821");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"laea"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Conic_Near_Conformal"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Conic Near-Conformal");
        this.addProjectionName(count, "Lambert Conic Near Conformal");
        this.addProjectionName(count, "Lambert_Conic_Near-Conformal");
        this.addProjectionName(count, "9817");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"lcca"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Conformal_Conic_1SP"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Conic Conformal (1SP)");
        this.addProjectionName(count, "9801");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"lcc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Conformal_Conic_2SP"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Conic Conformal (2SP)");
        this.addProjectionName(count, "9802");
        parameterName = new String[]{"standard_parallel_1"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_2");
        this.addProjectionParameter(count, "latitude_of_origin");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_1"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "lat_0");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"lcc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Conformal_Conic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Conic Conformal");
        this.addProjectionName(count, "9801");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "standard_parallel_2");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_1");
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"lcc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Lambert_Conformal_Conic_2SP_Belgium"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Lambert Conic Conformal (2SP Belgium)");
        this.addProjectionName(count, "9803");
        parameterName = new String[]{"standard_parallel_1"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_2");
        this.addProjectionParameter(count, "latitude_of_origin");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_1"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "lat_0");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"lcc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Loximuthal"};
        this.projectionNameList.add(++count, projectionName);
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "Central_Parallel");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_1");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"loxim"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Mercator_1SP"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Mercator");
        this.addProjectionName(count, "Wright");
        this.addProjectionName(count, "9804");
        this.addProjectionName(count, "Mercator (1SP)");
        this.addProjectionName(count, "Mercator_(1SP)");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "latitude_of_origin");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"merc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Mercator_1SP_Spherical"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "9841");
        this.addProjectionName(count, "Mercator (1SP) Spherical");
        this.addProjectionName(count, "Mercator_(1SP)_Spherical");
        this.addProjectionName(count, "Mercator (1SP) (Spherical)");
        this.addProjectionName(count, "Mercator_(1SP)_(Spherical)");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "latitude_of_origin");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"merc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Mercator_2SP"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Mercator");
        this.addProjectionName(count, "9805");
        this.addProjectionName(count, "Mercator (2SP)");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"merc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Miller_Cylindrical"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Miller Cylindrical");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"mill"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Mollweide"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Homolographic");
        this.addProjectionName(count, "Homalographic");
        this.addProjectionName(count, "Babinet");
        this.addProjectionName(count, "Elliptical");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"moll"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Near_Sided_Perspective"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Near Sided Perspective");
        this.addProjectionName(count, "Near-Sided Perspective");
        this.addProjectionName(count, "Near-Sided-Perspective");
        this.addProjectionName(count, "Near-Sided_Perspective");
        this.addProjectionName(count, "Vertical Near Side Perspective");
        this.addProjectionName(count, "Vertical-Near-Side-Perspective");
        this.addProjectionName(count, "Vertical_Near_Side_Perspective");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "Height");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "h");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.001");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.001");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"nsper"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"New_Zealand_Map_Grid"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "New Zealand Map Grid");
        this.addProjectionName(count, "9811");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"nzmg"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Oblique_Mercator"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Oblique Mercator");
        this.addProjectionName(count, "9815");
        this.addProjectionName(count, "CT_ObliqueMercator");
        this.addProjectionName(count, "Hotine_Oblique_Mercator_Azimuth_Center");
        this.addProjectionName(count, "Rectified_Skew_Orthomorphic_Center");
        this.addProjectionName(count, "Hotine Oblique Mercator");
        this.addProjectionName(count, "Hotine_Oblique_Mercator");
        parameterName = new String[]{"latitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "longitude_of_center");
        this.addProjectionParameter(count, "azimuth");
        this.addProjectionParameter(count, "rectified_grid_angle");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lonc");
        this.addProjectionParameterAcronymList(count, "alpha");
        this.addProjectionParameterAcronymList(count, "gamma");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"omerc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Hotine_Oblique_Mercator_Two_Point_Center"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Hotine_Oblique_Mercator_Two_Point_Natural_Origin");
        parameterName = new String[]{"Latitude_Of_1st_Point"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "Longitude_Of_1st_Point");
        this.addProjectionParameter(count, "Latitude_Of_2nd_Point");
        this.addProjectionParameter(count, "Longitude_Of_2nd_Point");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_1"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_1");
        this.addProjectionParameterAcronymList(count, "lat_2");
        this.addProjectionParameterAcronymList(count, "lon_2");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"omerc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Oblique_Stereographic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Oblique Stereographic");
        this.addProjectionName(count, "9809");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"sterea"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Orthographic"};
        this.projectionNameList.add(++count, projectionName);
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"ortho"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Polar_Stereographic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Polar Stereographic");
        this.addProjectionName(count, "Polar_Stereographic_(variant_A)");
        this.addProjectionName(count, "Polar_Stereographic_(variant_B)");
        this.addProjectionName(count, "9810");
        this.addProjectionName(count, "9829");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "latitude_of_standard_parallel");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"90.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "90.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"stere"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Polyconic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "American_Polyconic");
        this.addProjectionName(count, "American Polyconic");
        this.addProjectionName(count, "9818");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"poly"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Popular_Visualisation_Pseudo_Mercator"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "1024");
        this.addProjectionName(count, "Popular Visualisation Pseudo Mercator");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "latitude_of_origin");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"merc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Quartic_Authalic"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Quartic Authalic");
        this.addProjectionName(count, "Quartic-Authalic");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"qua_aut"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Robinson"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Orthophanic");
        parameterName = new String[]{"longitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"robin"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Sinusoidal"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Sanson-Flamsteed");
        this.addProjectionName(count, "Sanson Flamsteed");
        this.addProjectionName(count, "Sanson_Flamsteed");
        this.addProjectionName(count, "Mercator equal area");
        this.addProjectionName(count, "Mercator_equal_area");
        parameterName = new String[]{"longitude_of_center"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"sinu"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Swiss_Oblique_Cylindrical"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Swiss Oblique Cylindrical");
        this.addProjectionName(count, "Swiss Oblique Mercator");
        this.addProjectionName(count, "9814");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"somerc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Stereographic"};
        this.projectionNameList.add(++count, projectionName);
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "latitude_of_standard_parallel");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"stere"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Stereographic_North_Pole"};
        this.projectionNameList.add(++count, projectionName);
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"90.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"stere"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Stereographic_South_Pole"};
        this.projectionNameList.add(++count, projectionName);
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"-90.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"stere"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Transverse_Mercator"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Transverse Mercator");
        this.addProjectionName(count, "Gauss-Kruger");
        this.addProjectionName(count, "Gauss_Kruger");
        this.addProjectionName(count, "Gauss Conformal");
        this.addProjectionName(count, "Transverse Cylindrical Orthomorphic");
        this.addProjectionName(count, "9807");
        parameterName = new String[]{"latitude_of_origin"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "central_meridian");
        this.addProjectionParameter(count, "scale_factor");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lat_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lon_0");
        this.addProjectionParameterAcronymList(count, "k");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "1.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"90.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "360.0");
        this.addProjectionParameterMaxValue(count, "10.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-90.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-360.0");
        this.addProjectionParameterMinValue(count, "0.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"tmerc"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"VanDerGrinten"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "VanDerGrinten I");
        this.addProjectionName(count, "VanderGrinten");
        this.addProjectionName(count, "Van_der_Grinten_I");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"vandg"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Winkel_I"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Winkel I");
        this.addProjectionName(count, "Winkel-I");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"wink1"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Winkel_II"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Winkel II");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"wink2"};
        this.projectionAcronymList.add(count, projectionAcronym);
        projectionName = new String[]{"Winkel_Tripel"};
        this.projectionNameList.add(++count, projectionName);
        this.addProjectionName(count, "Winkel-Tripel");
        this.addProjectionName(count, "Winkel Tripel");
        parameterName = new String[]{"central_meridian"};
        this.projectionParameterList.add(count, parameterName);
        this.addProjectionParameter(count, "standard_parallel_1");
        this.addProjectionParameter(count, "false_easting");
        this.addProjectionParameter(count, "false_northing");
        parameterAcronym = new String[]{"lon_0"};
        this.projectionParameterAcronymList.add(count, parameterAcronym);
        this.addProjectionParameterAcronymList(count, "lat_ts");
        this.addProjectionParameterAcronymList(count, "x_0");
        this.addProjectionParameterAcronymList(count, "y_0");
        parameterDefaultValue = new String[]{"0.0"};
        this.projectionParameterDefaultValueList.add(count, parameterDefaultValue);
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        this.addProjectionParameterDefaultValue(count, "0.0");
        parameterMaxValue = new String[]{"360.0"};
        this.projectionParameterMaxValueList.add(count, parameterMaxValue);
        this.addProjectionParameterMaxValue(count, "90.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        this.addProjectionParameterMaxValue(count, "100000000.0");
        parameterMinValue = new String[]{"-360.0"};
        this.projectionParameterMinValueList.add(count, parameterMinValue);
        this.addProjectionParameterMinValue(count, "-90.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        this.addProjectionParameterMinValue(count, "-100000000.0");
        projectionAcronym = new String[]{"wintri"};
        this.projectionAcronymList.add(count, projectionAcronym);
    }

    public void addProjectionName(int pos, String projectionName) throws CrsException {
        if (pos < 0 || pos > this.projectionNameList.size() - 1) {
            String strError = "error_adding_projection_name";
            String strError2 = projectionName;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionNames = this.projectionNameList.get(pos);
        String[] newProjectionNames = new String[projectionNames.length + 1];
        int i = 0;
        while (i < projectionNames.length) {
            newProjectionNames[i] = projectionNames[i];
            ++i;
        }
        newProjectionNames[projectionNames.length] = projectionName;
        this.projectionNameList.remove(pos);
        this.projectionNameList.add(pos, newProjectionNames);
    }

    public void addUnitName(int pos, String unitName) throws CrsException {
        if (pos < 0 || pos > this.unitNameList.size() - 1) {
            String strError = "error_adding_unit_name";
            String strError2 = unitName;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] unitNames = this.unitNameList.get(pos);
        String[] newUnitNames = new String[unitNames.length + 1];
        int i = 0;
        while (i < unitNames.length) {
            newUnitNames[i] = unitNames[i];
            ++i;
        }
        newUnitNames[unitNames.length] = unitName;
        this.unitNameList.remove(pos);
        this.unitNameList.add(pos, newUnitNames);
    }

    public void addProjectionParameterName(int pos, String projectionParameterName) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterNameList.size() - 1) {
            String strError = "error_adding_parameter_projection_name";
            String strError2 = projectionParameterName;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameterNames = this.projectionParameterNameList.get(pos);
        String[] newProjectionParameterNames = new String[projectionParameterNames.length + 1];
        int i = 0;
        while (i < projectionParameterNames.length) {
            newProjectionParameterNames[i] = projectionParameterNames[i];
            ++i;
        }
        newProjectionParameterNames[projectionParameterNames.length] = projectionParameterName;
        this.projectionParameterNameList.remove(pos);
        this.projectionParameterNameList.add(pos, newProjectionParameterNames);
    }

    public void addProjectionParameter(int pos, String projectionParameter) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterList.size() - 1) {
            String strError = "error_adding_projection_parameter";
            String strError2 = projectionParameter;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameters = this.projectionParameterList.get(pos);
        String[] newProjectionParameters = new String[projectionParameters.length + 1];
        int i = 0;
        while (i < projectionParameters.length) {
            newProjectionParameters[i] = projectionParameters[i];
            ++i;
        }
        newProjectionParameters[projectionParameters.length] = projectionParameter;
        this.projectionParameterList.remove(pos);
        this.projectionParameterList.add(pos, newProjectionParameters);
    }

    public void addProjectionParameterDefaultValue(int pos, String projectionParameterDefaultValue) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterDefaultValueList.size() - 1) {
            String strError = "error_adding_default_value_to_projection_parameter";
            String strError2 = projectionParameterDefaultValue;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameterDefaultValues = this.projectionParameterDefaultValueList.get(pos);
        String[] newProjectionParameterDefaultValues = new String[projectionParameterDefaultValues.length + 1];
        int i = 0;
        while (i < projectionParameterDefaultValues.length) {
            newProjectionParameterDefaultValues[i] = projectionParameterDefaultValues[i];
            ++i;
        }
        newProjectionParameterDefaultValues[projectionParameterDefaultValues.length] = projectionParameterDefaultValue;
        this.projectionParameterDefaultValueList.remove(pos);
        this.projectionParameterDefaultValueList.add(pos, newProjectionParameterDefaultValues);
    }

    public void addProjectionParameterMaxValue(int pos, String projectionParameterMaxValue) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterMaxValueList.size() - 1) {
            String strError = "error_adding_max_value_to_projection_parameter";
            String strError2 = projectionParameterMaxValue;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameterMaxValues = this.projectionParameterMaxValueList.get(pos);
        String[] newProjectionParameterMaxValues = new String[projectionParameterMaxValues.length + 1];
        int i = 0;
        while (i < projectionParameterMaxValues.length) {
            newProjectionParameterMaxValues[i] = projectionParameterMaxValues[i];
            ++i;
        }
        newProjectionParameterMaxValues[projectionParameterMaxValues.length] = projectionParameterMaxValue;
        this.projectionParameterMaxValueList.remove(pos);
        this.projectionParameterMaxValueList.add(pos, newProjectionParameterMaxValues);
    }

    public void addProjectionParameterMinValue(int pos, String projectionParameterMinValue) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterMinValueList.size() - 1) {
            String strError = "error_adding_min_value_to_projection_parameter";
            String strError2 = projectionParameterMinValue;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameterMinValues = this.projectionParameterMinValueList.get(pos);
        String[] newProjectionParameterMinValues = new String[projectionParameterMinValues.length + 1];
        int i = 0;
        while (i < projectionParameterMinValues.length) {
            newProjectionParameterMinValues[i] = projectionParameterMinValues[i];
            ++i;
        }
        newProjectionParameterMinValues[projectionParameterMinValues.length] = projectionParameterMinValue;
        this.projectionParameterMinValueList.remove(pos);
        this.projectionParameterMinValueList.add(pos, newProjectionParameterMinValues);
    }

    public void addProjectionParameterAcronymList(int pos, String projectionParameterAcronym) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterAcronymList.size() - 1) {
            String strError = "error_adding_projection_acronym";
            String strError2 = projectionParameterAcronym;
            String strError3 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(strError) + ": " + strError2 + " " + strError3));
        }
        String[] projectionParameterAcronyms = this.projectionParameterAcronymList.get(pos);
        String[] newProjectionParameterAcronyms = new String[projectionParameterAcronyms.length + 1];
        int i = 0;
        while (i < projectionParameterAcronyms.length) {
            newProjectionParameterAcronyms[i] = projectionParameterAcronyms[i];
            ++i;
        }
        newProjectionParameterAcronyms[projectionParameterAcronyms.length] = projectionParameterAcronym;
        this.projectionParameterAcronymList.remove(pos);
        this.projectionParameterAcronymList.add(pos, newProjectionParameterAcronyms);
    }

    public int findProjection(String projectionName) {
        int i = 0;
        while (i < this.projectionNameList.size()) {
            String[] projectionNames = this.projectionNameList.get(i);
            int j = 0;
            while (j < projectionNames.length) {
                if (projectionNames[j].toLowerCase().replaceAll(" ", "").equals(projectionName.toLowerCase().replaceAll(" ", ""))) {
                    return i;
                }
                ++j;
            }
            ++i;
        }
        return -1;
    }

    public int findProjectionParameter(String parameterName) {
        int i = 0;
        while (i < this.projectionParameterNameList.size()) {
            String[] parameterNames = this.projectionParameterNameList.get(i);
            int j = 0;
            while (j < parameterNames.length) {
                if (parameterNames[j].toLowerCase().replaceAll(" ", "").equals(parameterName.toLowerCase().replaceAll(" ", ""))) {
                    return i;
                }
                ++j;
            }
            ++i;
        }
        return -1;
    }

    public int findProjectionParameters(String parameterName1, String parameterName2) {
        int i = 0;
        while (i < this.projectionParameterNameList.size()) {
            boolean existsParameter1 = false;
            boolean existsParameter2 = false;
            String[] parameterNames = this.projectionParameterNameList.get(i);
            if (parameterNames[0].toLowerCase().replaceAll(" ", "").equals(parameterName2.toLowerCase().replaceAll(" ", ""))) {
                existsParameter2 = true;
                int j = 0;
                while (j < parameterNames.length) {
                    if (parameterNames[j].toLowerCase().replaceAll(" ", "").equals(parameterName1.toLowerCase().replaceAll(" ", ""))) {
                        existsParameter1 = true;
                        break;
                    }
                    ++j;
                }
            }
            if (existsParameter1 && existsParameter2) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public int findUnit(String unitName) {
        int i = 0;
        while (i < this.unitNameList.size()) {
            String[] unitNames = this.unitNameList.get(i);
            int j = 0;
            while (j < unitNames.length) {
                if (unitNames[j].toLowerCase().replaceAll(" ", "").equals(unitName.toLowerCase().replaceAll(" ", ""))) {
                    return i;
                }
                ++j;
            }
            ++i;
        }
        return -1;
    }

    public String getProj4UnitName(int pos) throws CrsException {
        if (pos < 0 || pos > this.unitNameList.size() - 1) {
            String strError = "error_obtaining_unit_name";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        return this.unitNameList.get(pos)[0];
    }

    public String getProj4ProjectionName(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionNameList.size() - 1) {
            String strError = "error_obtaining_projection_name";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        return this.projectionNameList.get(pos)[0];
    }

    public String getProj4ProjectionParameterName(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterNameList.size() - 1) {
            String strError = "error_obtaining_projection_parameter_name";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        return this.projectionParameterNameList.get(pos)[0];
    }

    public List<String> getProj4ProjectionParameters(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterList.size() - 1) {
            throw new CrsException(new Exception());
        }
        String[] parameterList = this.projectionParameterList.get(pos);
        ArrayList<String> parameters = new ArrayList<String>();
        int i = 0;
        while (i < parameterList.length) {
            String parameterName = parameterList[i];
            int posParameter = this.findProjectionParameter(parameterName);
            if (posParameter == -1) {
                String strError = "the_parameter";
                String strError2 = parameterName;
                String strError3 = "not_in_parameter_list";
                throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " = " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
            }
            String parameterNameProj4 = parameterName;
            parameters.add(i, parameterNameProj4);
            ++i;
        }
        return parameters;
    }

    public List<String> getProj4ProjectionParameterDefaultValues(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterDefaultValueList.size() - 1) {
            String strError = "error_obtaining_default_value_to_projection_parameter";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        String[] parameterDefaultValueList = this.projectionParameterDefaultValueList.get(pos);
        ArrayList<String> parameterDefaultValues = new ArrayList<String>();
        int i = 0;
        while (i < parameterDefaultValueList.length) {
            String parameterDefaultValue = parameterDefaultValueList[i];
            parameterDefaultValues.add(i, parameterDefaultValue);
            ++i;
        }
        return parameterDefaultValues;
    }

    public List<String> getProj4ProjectionParameterMaxValues(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterMaxValueList.size() - 1) {
            String strError = "error_obtaining_max_value_to_projection_parameter";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        String[] parameterMaxValueList = this.projectionParameterMaxValueList.get(pos);
        ArrayList<String> parameterMaxValues = new ArrayList<String>();
        int i = 0;
        while (i < parameterMaxValueList.length) {
            String parameterMaxValue = parameterMaxValueList[i];
            parameterMaxValues.add(i, parameterMaxValue);
            ++i;
        }
        return parameterMaxValues;
    }

    public List<String> getProj4ProjectionParameterMinValues(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterMinValueList.size() - 1) {
            String strError = "error_obtaining_min_value_to_projection_parameter";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        String[] parameterMinValueList = this.projectionParameterMinValueList.get(pos);
        ArrayList<String> parameterMinValues = new ArrayList<String>();
        int i = 0;
        while (i < parameterMinValueList.length) {
            String parameterMinValue = parameterMinValueList[i];
            parameterMinValues.add(i, parameterMinValue);
            ++i;
        }
        return parameterMinValues;
    }

    public List<String> getProj4ProjectionParameterAcronyms(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterAcronymList.size() - 1) {
            String strError = "error_obtaining_projection_acronym";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        String[] parameterAcronymList = this.projectionParameterAcronymList.get(pos);
        ArrayList<String> parameterAcronyms = new ArrayList<String>();
        int i = 0;
        while (i < parameterAcronymList.length) {
            String parameterAcronym = parameterAcronymList[i];
            parameterAcronyms.add(i, parameterAcronym);
            ++i;
        }
        return parameterAcronyms;
    }

    public String getProjectionParameterUnitList(int pos) throws CrsException {
        if (pos < 0 || pos > this.projectionParameterUnitList.size() - 1) {
            String strError = "error_obtaining_unit_list_of_projection_parameter";
            String strError2 = "position_out_of_valid_limits";
            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + ". " + PluginServices.getText(this, strError2)));
        }
        String[] projParamUnit = this.projectionParameterUnitList.get(pos);
        return projParamUnit[0];
    }

    public String exportToProj4(Crs crs) throws CrsException {
        String strProj4 = "+proj=";
        String strDatumName = "";
        String strDatumCode = "";
        String strProj4ToMeter = "";
        String strProj4Datum = "";
        String[] primeMeridian = crs.getCrsWkt().getPrimen();
        String primeMeridianName = primeMeridian[0];
        double primeMeridianValue = Double.parseDouble(primeMeridian[1]);
        String[] strPrimeMeridianProj4 = this.primeMeridianToProj4(primeMeridianName, primeMeridianValue);
        primeMeridianValue = Double.parseDouble(strPrimeMeridianProj4[1]);
        primeMeridianName = strPrimeMeridianProj4[0];
        String primeMeridianAcronym = strPrimeMeridianProj4[2];
        String codDatum = "0";
        strDatumName = crs.getCrsWkt().getDatumName();
        int intCodDatum = 0;
        strProj4Datum = this.datumToProj4(strDatumName, intCodDatum);
        String strProj = crs.getCrsWkt().getProjcs();
        if (strProj.equals("")) {
            strProj4 = String.valueOf(strProj4) + "longlat ";
        } else {
            String strProjName = crs.getCrsWkt().getProjection();
            int indexProj = this.findProjection(strProjName);
            if (indexProj == -1) {
                String strError = "the_projection";
                String strError2 = strProjName;
                String strError3 = "not_in_proj4";
                throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
            }
            String projectionName = this.getProj4ProjectionName(indexProj).trim();
            List<String> parameterNames = this.getProj4ProjectionParameters(indexProj);
            List<String> parameterAcronyms = this.getProj4ProjectionParameterAcronyms(indexProj);
            List<String> parameterValues = this.getProj4ProjectionParameterDefaultValues(indexProj);
            List<String> parameterMaxValues = this.getProj4ProjectionParameterMaxValues(indexProj);
            List<String> parameterMinValues = this.getProj4ProjectionParameterMinValues(indexProj);
            String[] gtParameterValues = crs.getCrsWkt().getParam_value();
            String[] gtParameterNames = crs.getCrsWkt().getParam_name();
            int i = 0;
            while (i < parameterNames.size()) {
                boolean existsParameter = false;
                String parameterValue = "";
                int j = 0;
                while (j < gtParameterNames.length) {
                    String gtParameterName = gtParameterNames[j].trim();
                    int posGtParameter = this.findProjectionParameters(gtParameterName, parameterNames.get(i));
                    if (posGtParameter != -1) {
                        gtParameterNames[j] = gtParameterName = this.getProj4ProjectionParameterName(posGtParameter);
                        existsParameter = true;
                        double maxValue = Double.parseDouble(parameterMaxValues.get(i));
                        double minValue = Double.parseDouble(parameterMinValues.get(i));
                        parameterValue = gtParameterValues[j];
                        double auxValue = Double.parseDouble(parameterValue);
                        if (auxValue < minValue || auxValue > maxValue) {
                            String strError = "the_parameter";
                            String strError2 = gtParameterName;
                            String strError3 = "out_of_domain";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                        }
                        if (!parameterNames.get(i).trim().equals("scale_factor") || auxValue != minValue) break;
                        String strError = "the_parameter";
                        String strError2 = gtParameterName;
                        String strError3 = "out_of_domain";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                    ++j;
                }
                if (existsParameter) {
                    parameterValues.set(i, parameterValue);
                }
                ++i;
            }
            boolean isSomerc = false;
            boolean isOmerc = false;
            double valueAlpha = 0.0;
            double valueGamma = 0.0;
            boolean existsAlpha = false;
            boolean existsGamma = false;
            String[] projectionAcronym = this.projectionAcronymList.get(indexProj);
            if (projectionName.equals("Oblique_Mercator")) {
                isOmerc = true;
                int j = 0;
                while (j < gtParameterNames.length) {
                    double value;
                    String gtParameterName = gtParameterNames[j].trim();
                    if ((gtParameterName.equalsIgnoreCase("latitude_of_origin") || gtParameterName.equalsIgnoreCase("standard_parallel_1") || gtParameterName.equalsIgnoreCase("latitude_of_center")) && Math.abs(Math.abs(value = Double.parseDouble(gtParameterValues[j])) - 90.0) < 1.0E-4) {
                        String strError = "in_proj4_projection";
                        String strError2 = "Oblique_Mercator";
                        String strError3 = "not_admit_latitude_origin_close_to_the_poles";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                    if (gtParameterName.equalsIgnoreCase("azimuth")) {
                        valueAlpha = Double.parseDouble(gtParameterValues[j]);
                        if (Math.abs(valueAlpha - 90.0) < 1.0E-4) {
                            String strError = "in_proj4_projection";
                            String strError2 = "Oblique_Mercator";
                            String strError3 = "not_admit_azimut_close_to";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " 90"));
                        }
                        if (Math.abs(valueAlpha - 270.0) < 1.0E-4) {
                            String strError = "in_proj4_projection";
                            String strError2 = "Oblique_Mercator";
                            String strError3 = "not_admit_azimut_close_to";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " 270"));
                        }
                        existsAlpha = true;
                    }
                    if (gtParameterName.equalsIgnoreCase("rectified_grid_angle")) {
                        valueGamma = Double.parseDouble(gtParameterValues[j]);
                        existsGamma = true;
                    }
                    ++j;
                }
                if (existsAlpha && existsGamma && Math.abs(valueAlpha - valueGamma) > 1.0E-8) {
                    String strError = "in_proj4_projection";
                    String strError2 = "Oblique_Mercator";
                    String strError3 = "not_admit_different_azimut_and_spin_axis";
                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                }
            }
            if (projectionName.equals("Hotine_Oblique_Mercator_Two_Point_Center")) {
                double lat_1 = 0.0;
                double lat_2 = 0.0;
                boolean exists_Lat_1 = false;
                boolean exists_Lat_2 = false;
                int j = 0;
                while (j < gtParameterNames.length) {
                    String gtParameterName = gtParameterNames[j].trim();
                    if (gtParameterName.equalsIgnoreCase("Latitude_Of_1st_Point")) {
                        lat_1 = Double.parseDouble(gtParameterValues[j]);
                        exists_Lat_1 = true;
                    }
                    if (gtParameterName.equalsIgnoreCase("Latitude_Of_2nd_Point")) {
                        lat_2 = Double.parseDouble(gtParameterValues[j]);
                        exists_Lat_2 = true;
                    }
                    ++j;
                }
                if (exists_Lat_1 && exists_Lat_2) {
                    String strError2;
                    if (Math.abs(lat_1 - lat_2) < 1.0E-4) {
                        String strError = "in_proj4_projection";
                        strError2 = "Hotine-Oblique Mercator Two Points";
                        String strError3 = "not_equal_lat_1_and_lat_2";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                    if (Math.abs(lat_1) < 1.0E-4) {
                        String strError = "in_proj4_projection";
                        strError2 = "Hotine-Oblique Mercator Two Points";
                        String strError3 = "not_zero_lat_1";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                    if (Math.abs(Math.abs(lat_2) - 90.0) < 1.0E-4) {
                        String strError = "in_proj4_projection";
                        strError2 = "Hotine-Oblique Mercator Two Points";
                        String strError3 = "not_values_90_or_minus_90_lat_2";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                }
            }
            String strProjectionAcronym = projectionAcronym[0];
            String strExtraProj4 = "";
            boolean isLaborde = false;
            boolean isLcc1sp = false;
            boolean isMerc = false;
            boolean exists_sf = false;
            boolean exists_lo = false;
            boolean isSterePolar = false;
            boolean isStereOblique = false;
            boolean isGoogleProj = false;
            boolean exists_stdPar = false;
            double value_stdPar = 0.0;
            if (projectionAcronym[0].equals("merc")) {
                isMerc = true;
                double value_sf = 0.0;
                double value_lo = 0.0;
                int j = 0;
                while (j < gtParameterNames.length) {
                    String gtParameterName = gtParameterNames[j].trim();
                    if (gtParameterName.equalsIgnoreCase("latitude_of_origin") || gtParameterName.equalsIgnoreCase("standard_parallel_1") || gtParameterName.equalsIgnoreCase("latitude_of_center")) {
                        exists_lo = true;
                        value_lo = Double.parseDouble(gtParameterValues[j]);
                    }
                    if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                        exists_sf = true;
                        value_sf = Double.parseDouble(gtParameterValues[j]);
                    }
                    ++j;
                }
                if (exists_sf && exists_lo) {
                    if (value_sf != 1.0 && value_lo != 0.0) {
                        String strError = "in_proj4_projection";
                        String strError2 = "Mercator";
                        String strError3 = "not_admit_scale_factor_and_latitude_of_origin";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                    }
                    if (projectionName.equals("Mercator_1SP")) {
                        exists_lo = false;
                    } else if (projectionName.equals("Mercator_2SP")) {
                        exists_sf = false;
                    }
                }
                if (projectionName.equalsIgnoreCase("Mercator_1SP_Spherical") || projectionName.equals("Popular_Visualisation_Pseudo_Mercator")) {
                    strExtraProj4 = String.valueOf(strExtraProj4) + " +nadgrids=@null ";
                }
            } else if (projectionAcronym[0].equals("lcc")) {
                if (projectionName.equalsIgnoreCase("Lambert_Conformal_Conic_1SP")) {
                    isLcc1sp = true;
                }
            } else if (projectionAcronym[0].equals("stere")) {
                if (projectionName.equalsIgnoreCase("Polar_Stereographic")) {
                    isSterePolar = true;
                    int j = 0;
                    while (j < gtParameterNames.length) {
                        String gtParameterName = gtParameterNames[j].trim();
                        if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                            double value_sf = Double.parseDouble(gtParameterValues[j]);
                            exists_sf = true;
                        }
                        if (gtParameterName.equalsIgnoreCase("latitude_of_standard_parallel")) {
                            value_stdPar = Double.parseDouble(gtParameterValues[j]);
                            exists_stdPar = true;
                        }
                        ++j;
                    }
                }
                if (projectionName.equalsIgnoreCase("Stereographic")) {
                    boolean isPolar = false;
                    int j = 0;
                    while (j < gtParameterNames.length) {
                        String gtParameterAcronymn = parameterAcronyms.get(j).trim();
                        if (gtParameterNames[j].equalsIgnoreCase("latitude_of_origin")) {
                            double gtParameterValue = Double.parseDouble(gtParameterValues[j]);
                            if (Math.abs(gtParameterValue - 90.0) < angularTolerance) {
                                isPolar = true;
                                break;
                            }
                            if (!(Math.abs(gtParameterValue - -90.0) < angularTolerance)) break;
                            isPolar = true;
                            break;
                        }
                        ++j;
                    }
                    if (isPolar) {
                        isSterePolar = true;
                        strProjectionAcronym = "stere";
                        j = 0;
                        while (j < gtParameterNames.length) {
                            String gtParameterName = gtParameterNames[j].trim();
                            if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                                double value_sf = Double.parseDouble(gtParameterValues[j]);
                                exists_sf = true;
                            }
                            if (gtParameterName.equalsIgnoreCase("latitude_of_standard_parallel")) {
                                value_stdPar = Double.parseDouble(gtParameterValues[j]);
                                exists_stdPar = true;
                            }
                            ++j;
                        }
                    }
                    if (!isPolar) {
                        isStereOblique = true;
                        if (exists_stdPar) {
                            String strError = "in_proj4_projection";
                            String strError2 = "Oblique_Stereographic";
                            String strError3 = "not_admit_parameter";
                            String strError4 = "latitude_of_standard_parallel";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " " + PluginServices.getText(this, strError4)));
                        }
                        strProjectionAcronym = "sterea";
                    }
                }
            } else if (projectionAcronym[0].equals("omerc")) {
                boolean existsLat1 = false;
                boolean existsLat2 = false;
                boolean existsLon1 = false;
                boolean strError3 = false;
            } else if (projectionAcronym[0].equals("mill")) {
                strExtraProj4 = String.valueOf(strExtraProj4) + " +R_A ";
            } else if (projectionAcronym[0].equals("vandg")) {
                strExtraProj4 = String.valueOf(strExtraProj4) + " +R_A ";
            } else if (projectionAcronym[0].equals("labrd")) {
                isLaborde = true;
            }
            strProj4 = String.valueOf(strProj4) + strProjectionAcronym + " ";
            int i2 = 0;
            while (i2 < parameterNames.size()) {
                boolean control = true;
                String parameterName = parameterNames.get(i2).trim();
                String parameterAcronym = parameterAcronyms.get(i2).trim();
                String strParameterValue = parameterValues.get(i2).trim();
                if (isMerc) {
                    if ((parameterName.equalsIgnoreCase("latitude_of_origin") || parameterName.equalsIgnoreCase("standard_parallel_1") || parameterName.equalsIgnoreCase("latitude_of_center")) && !exists_lo) {
                        control = false;
                    }
                    if (parameterName.equalsIgnoreCase("scale_factor") && !exists_sf) {
                        control = false;
                    }
                }
                if (isSterePolar) {
                    String gtParameterAcronymn = parameterAcronyms.get(i2).trim();
                    if (gtParameterAcronymn.equalsIgnoreCase("lat_0") && exists_stdPar) {
                        double parameterValue = Double.parseDouble(strParameterValue);
                        if (parameterValue > 0.0 && value_stdPar < 0.0) {
                            strParameterValue = "-90.0";
                        }
                        if (parameterValue < 0.0 && value_stdPar > 0.0) {
                            strParameterValue = "90.0";
                        }
                    }
                    if (gtParameterAcronymn.equalsIgnoreCase("lat_ts") && exists_sf) {
                        control = false;
                    }
                    if (parameterName.equalsIgnoreCase("scale_factor") && !exists_sf) {
                        control = false;
                    }
                }
                if (isSomerc) {
                    if (parameterName.equals("rectified_grid_angle")) {
                        control = false;
                    }
                    if (parameterName.equals("azimuth")) {
                        control = false;
                    }
                }
                if (isOmerc && parameterName.equals("rectified_grid_angle")) {
                    if (existsAlpha) {
                        control = false;
                    } else {
                        parameterAcronym = "alpha";
                    }
                }
                if (parameterAcronym.equals("lon_0") || parameterAcronym.equals("lonc")) {
                    double parameterValue = Double.parseDouble(strParameterValue);
                    if (!projectionAcronym[0].equalsIgnoreCase("krovak")) {
                        parameterValue -= primeMeridianValue;
                    }
                    strParameterValue = Double.toString(parameterValue);
                }
                if (control) {
                    strProj4 = String.valueOf(strProj4) + "+" + parameterAcronym + "=" + strParameterValue + " ";
                }
                if (isLcc1sp && parameterAcronym.equals("lat_0")) {
                    strProj4 = String.valueOf(strProj4) + "+lat_1=" + strParameterValue + " ";
                    strProj4 = String.valueOf(strProj4) + "+lat_2=" + strParameterValue + " ";
                }
                ++i2;
            }
            if (isLaborde) {
                strProj4 = String.valueOf(strProj4) + "+azi=18.9 +lat_0=-18.9 +lon_0=44.1 +k_0=0.9995 +x_0=400000 +y_0=800000 +ellps=intl ";
            }
            strProj4 = String.valueOf(strProj4) + strExtraProj4;
        }
        double a = 0.0;
        double inv_f = 0.0;
        String elipName = crs.getCrsWkt().getSpheroid()[0];
        a = Double.parseDouble(crs.getCrsWkt().getSpheroid()[1]);
        inv_f = Double.parseDouble(crs.getCrsWkt().getSpheroid()[2]);
        String strEllipseAcronym = this.ellipseToProj4(a, inv_f);
        String strEllipse = "";
        strEllipse = strEllipseAcronym.equals("") ? (!Double.isInfinite(inv_f) ? (inv_f > 0.0 ? "+a=" + a + " +rf=" + inv_f + " " : "+R=" + a + " ") : "+R=" + a + " ") : "+ellps=" + strEllipseAcronym + " ";
        strProj4 = String.valueOf(strProj4) + strEllipse;
        if (!strProj4Datum.equals("")) {
            strProj4 = String.valueOf(strProj4) + strProj4Datum;
        }
        strProj4 = String.valueOf(strProj4) + primeMeridianAcronym;
        String strWkt = crs.getWKT();
        if (!strProj4ToMeter.equals("")) {
            strProj4 = String.valueOf(strProj4) + strProj4ToMeter;
        }
        return strProj4;
    }

    public String exportToProj4(CoordinateReferenceSystem crs) throws CrsException {
        String primeMeridianAcronym;
        double inv_f;
        double a;
        String strProj4ToMeter;
        String strProj4Datum;
        String strProj4;
        block104: {
            double value_stdPar;
            boolean exists_stdPar;
            boolean isStereOblique;
            boolean isSterePolar;
            boolean exists_lo;
            boolean exists_sf;
            boolean isMerc;
            boolean isLcc1sp;
            boolean isLaborde;
            String strExtraProj4;
            String projAcronym;
            String[] projectionAcronym;
            boolean existsAlpha;
            boolean isOmerc;
            boolean isSomerc;
            List<String> parameterValues;
            List<String> parameterAcronyms;
            List<String> parameterNames;
            String projectionName;
            double primeMeridianValue;
            String[] gtParameterNames;
            String[] gtParameterValues;
            block106: {
                block105: {
                    block107: {
                        String strProjName;
                        block103: {
                            strProj4 = "+proj=";
                            String[] primeMeridian = new String[2];
                            String strProj = "";
                            strProjName = "";
                            String strDatumName = "";
                            String strDatumCode = "";
                            strProj4Datum = "";
                            strProj4ToMeter = "";
                            gtParameterValues = new String[1];
                            gtParameterNames = new String[1];
                            String[] spheroid = new String[3];
                            a = 0.0;
                            inv_f = 0.0;
                            String elipName = "";
                            if (crs instanceof DefaultProjectedCRS) {
                                DefaultProjectedCRS crsProjected = (DefaultProjectedCRS)crs;
                                primeMeridian = this.Primem(((DefaultGeodeticDatum)crsProjected.getDatum()).getPrimeMeridian());
                                String[] proj = crsProjected.getName().toString().split(":");
                                strProj = proj.length > 1 ? proj[1] : proj[0];
                                strProjName = this.getName(crsProjected.getConversionFromBase().getMethod().getName());
                                gtParameterValues = new String[crsProjected.getConversionFromBase().getParameterValues().values().size()];
                                gtParameterNames = new String[crsProjected.getConversionFromBase().getParameterValues().values().size()];
                                int i = 0;
                                while (i < crsProjected.getConversionFromBase().getParameterValues().values().size()) {
                                    String str = crsProjected.getConversionFromBase().getParameterValues().values().get(i).toString();
                                    Unit u = crsProjected.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).getUnit();
                                    double value = crsProjected.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).doubleValue();
                                    value = this.convert(value, u.toString());
                                    gtParameterNames[i] = str.split("=")[0];
                                    gtParameterValues[i] = String.valueOf(value);
                                    ++i;
                                }
                                spheroid = this.Spheroid(((DefaultGeodeticDatum)crsProjected.getDatum()).getEllipsoid());
                                elipName = spheroid[0];
                                a = Double.parseDouble(spheroid[1]);
                                inv_f = Double.parseDouble(spheroid[2]);
                                Ellipsoid ellip = ((DefaultGeodeticDatum)crsProjected.getDatum()).getEllipsoid();
                                Unit unit2 = ellip.getAxisUnit();
                                String[] i_un = unit2.toString().split("[*]");
                                if (i_un.length > 1) {
                                    try {
                                        a *= Double.parseDouble(i_un[1].replaceAll("]", ""));
                                    }
                                    catch (NumberFormatException numberFormatException) {}
                                } else if (!i_un[0].equals("m")) {
                                    i_un[0].equals("ft");
                                }
                                String codDatum = "0";
                                String[] val = ((DefaultProjectedCRS)crs).getDatum().getName().toString().split(":");
                                strDatumName = val.length < 2 ? val[0] : val[1];
                                for (Identifier element : ((DefaultProjectedCRS)crs).getDatum().getIdentifiers()) {
                                    codDatum = element.getCode();
                                }
                                int intCodDatum = Integer.parseInt(codDatum);
                                strProj4Datum = this.datumToProj4(strDatumName, intCodDatum);
                                double factor_to_meter = 1.0;
                                Unit u = crs.getCoordinateSystem().getAxis(0).getUnit();
                                String[] un = u.toString().split("[*]");
                                if (un.length > 1) {
                                    try {
                                        factor_to_meter = Double.parseDouble(un[1].replaceAll("]", ""));
                                    }
                                    catch (NumberFormatException numberFormatException) {}
                                } else if (un[0].equals("m")) {
                                    try {
                                        factor_to_meter = Double.parseDouble(un[0]);
                                    }
                                    catch (NumberFormatException numberFormatException) {}
                                } else if (un[0].equals("ft")) {
                                    factor_to_meter = 0.3048;
                                }
                                strProj4ToMeter = factor_to_meter != 1.0 ? "+to_meter=" + factor_to_meter + " " : "+units=m ";
                            } else if (crs instanceof DefaultGeographicCRS) {
                                DefaultGeographicCRS crsGeographic = (DefaultGeographicCRS)crs;
                                primeMeridian = this.Primem(((DefaultGeodeticDatum)crsGeographic.getDatum()).getPrimeMeridian());
                                spheroid = this.Spheroid(((DefaultGeodeticDatum)crsGeographic.getDatum()).getEllipsoid());
                                elipName = spheroid[0];
                                a = Double.parseDouble(spheroid[1]);
                                inv_f = Double.parseDouble(spheroid[2]);
                                Ellipsoid ellip = ((DefaultGeodeticDatum)crsGeographic.getDatum()).getEllipsoid();
                                Unit unit = ellip.getAxisUnit();
                                String[] i_un = unit.toString().split("[*]");
                                if (i_un.length > 1) {
                                    try {
                                        a *= Double.parseDouble(i_un[1].replaceAll("]", ""));
                                    }
                                    catch (NumberFormatException unit2) {}
                                } else if (!i_un[0].equals("m")) {
                                    i_un[0].equals("ft");
                                }
                                String codDatum = "0";
                                String[] val = ((DefaultGeographicCRS)crs).getDatum().getName().toString().split(":");
                                strDatumName = val.length < 2 ? val[0] : val[1];
                                for (Identifier element : ((DefaultGeographicCRS)crs).getDatum().getIdentifiers()) {
                                    codDatum = element.getCode();
                                }
                                int intCodDatum = Integer.parseInt(codDatum);
                                strProj4Datum = this.datumToProj4(strDatumName, intCodDatum);
                            } else {
                                throw new CrsException(new Exception(PluginServices.getText(this, "not_geographic_nor_projected")));
                            }
                            String primeMeridianName = primeMeridian[0];
                            primeMeridianValue = -1.0;
                            if (primeMeridian[1] == null) {
                                throw new CrsException(new Exception(PluginServices.getText(this, "error_prime_meridiam_parameters")));
                            }
                            primeMeridianValue = Double.parseDouble(primeMeridian[1]);
                            String[] strPrimeMeridianProj4 = this.primeMeridianToProj4(primeMeridianName, primeMeridianValue);
                            primeMeridianValue = Double.parseDouble(strPrimeMeridianProj4[1]);
                            primeMeridianName = strPrimeMeridianProj4[0];
                            primeMeridianAcronym = strPrimeMeridianProj4[2];
                            if (!strProj.equals("")) break block103;
                            strProj4 = String.valueOf(strProj4) + "longlat ";
                            break block104;
                        }
                        int indexProj = this.findProjection(strProjName);
                        if (indexProj == -1) {
                            String strError = "the_projection";
                            String strError2 = strProjName;
                            String strError3 = "not_in_proj4";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                        }
                        projectionName = this.getProj4ProjectionName(indexProj).trim();
                        parameterNames = this.getProj4ProjectionParameters(indexProj);
                        parameterAcronyms = this.getProj4ProjectionParameterAcronyms(indexProj);
                        parameterValues = this.getProj4ProjectionParameterDefaultValues(indexProj);
                        List<String> parameterMaxValues = this.getProj4ProjectionParameterMaxValues(indexProj);
                        List<String> parameterMinValues = this.getProj4ProjectionParameterMinValues(indexProj);
                        int i = 0;
                        while (i < parameterNames.size()) {
                            boolean existsParameter = false;
                            String parameterValue = "";
                            int j = 0;
                            while (j < gtParameterNames.length) {
                                String gtParameterName = gtParameterNames[j].trim();
                                int posGtParameter = this.findProjectionParameters(gtParameterName, parameterNames.get(i));
                                if (posGtParameter != -1) {
                                    gtParameterNames[j] = gtParameterName = this.getProj4ProjectionParameterName(posGtParameter);
                                    existsParameter = true;
                                    double maxValue = Double.parseDouble(parameterMaxValues.get(i));
                                    double minValue = Double.parseDouble(parameterMinValues.get(i));
                                    parameterValue = gtParameterValues[j];
                                    double auxValue = Double.parseDouble(parameterValue);
                                    if (auxValue < minValue || auxValue > maxValue) {
                                        String strError = "the_parameter";
                                        String strError2 = gtParameterName;
                                        String strError3 = "out_of_domain";
                                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                    }
                                    if (!parameterNames.get(i).trim().equals("scale_factor") || auxValue != minValue) break;
                                    String strError = "the_parameter";
                                    String strError2 = gtParameterName;
                                    String strError3 = "out_of_domain";
                                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                }
                                ++j;
                            }
                            if (existsParameter) {
                                parameterValues.set(i, parameterValue);
                            }
                            ++i;
                        }
                        isSomerc = false;
                        isOmerc = false;
                        boolean isLcc = false;
                        double valueAlpha = 0.0;
                        double valueGamma = 0.0;
                        existsAlpha = false;
                        boolean existsGamma = false;
                        projectionAcronym = this.projectionAcronymList.get(indexProj);
                        if (projectionName.equals("Lambert_Conformal_Conic")) {
                            int i2 = 0;
                            while (i2 < parameterNames.size()) {
                                boolean control = true;
                                String parameterName = parameterNames.get(i2).trim();
                                if (parameterName.equalsIgnoreCase("standard_parallel_2")) {
                                    isLcc = true;
                                }
                                ++i2;
                            }
                        }
                        if (projectionName.equals("Oblique_Mercator")) {
                            isOmerc = true;
                            int j = 0;
                            while (j < gtParameterNames.length) {
                                double value;
                                String gtParameterName = gtParameterNames[j].trim();
                                if ((gtParameterName.equalsIgnoreCase("latitude_of_origin") || gtParameterName.equalsIgnoreCase("standard_parallel_1") || gtParameterName.equalsIgnoreCase("latitude_of_center")) && Math.abs(Math.abs(value = Double.parseDouble(gtParameterValues[j])) - 90.0) < 1.0E-4) {
                                    String strError = "in_proj4_projection";
                                    String strError2 = "Oblique_Mercator";
                                    String strError3 = "not_admit_latitude_origin_close_to_the_poles";
                                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                }
                                if (gtParameterName.equalsIgnoreCase("azimuth")) {
                                    valueAlpha = Double.parseDouble(gtParameterValues[j]);
                                    if (Math.abs(valueAlpha - 90.0) < 1.0E-4) {
                                        String strError = "in_proj4_projection";
                                        String strError2 = "Oblique_Mercator";
                                        String strError3 = "not_admit_azimut_close_to";
                                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " 90"));
                                    }
                                    if (Math.abs(valueAlpha - 270.0) < 1.0E-4) {
                                        String strError = "in_proj4_projection";
                                        String strError2 = "Oblique_Mercator";
                                        String strError3 = "not_admit_azimut_close_to";
                                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " 270"));
                                    }
                                    existsAlpha = true;
                                }
                                if (gtParameterName.equalsIgnoreCase("rectified_grid_angle")) {
                                    valueGamma = Double.parseDouble(gtParameterValues[j]);
                                    existsGamma = true;
                                }
                                ++j;
                            }
                            if (existsAlpha && existsGamma && Math.abs(valueAlpha - valueGamma) > 1.0E-8) {
                                String strError = "in_proj4_projection";
                                String strError2 = "Oblique_Mercator";
                                String strError3 = "not_admit_different_azimut_and_spin_axis";
                                throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                            }
                        }
                        if (projectionName.equals("Hotine_Oblique_Mercator_Two_Point_Center")) {
                            double lat_1 = 0.0;
                            double lat_2 = 0.0;
                            boolean exists_Lat_1 = false;
                            boolean exists_Lat_2 = false;
                            int j = 0;
                            while (j < gtParameterNames.length) {
                                double d;
                                String gtParameterName = gtParameterNames[j].trim();
                                if (gtParameterName.equalsIgnoreCase("Latitude_Of_1st_Point")) {
                                    d = Double.parseDouble(gtParameterValues[j]);
                                }
                                if (gtParameterName.equalsIgnoreCase("Latitude_Of_2nd_Point")) {
                                    d = Double.parseDouble(gtParameterValues[j]);
                                }
                                ++j;
                            }
                            if (exists_Lat_1 && exists_Lat_2) {
                                String strError2;
                                if (Math.abs(lat_1 - lat_2) < 1.0E-4) {
                                    String strError = "in_proj4_projection";
                                    strError2 = "Hotine-Oblique Mercator Two Points";
                                    String strError3 = "not_equal_lat_1_and_lat_2";
                                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                }
                                if (Math.abs(lat_1) < 1.0E-4) {
                                    String strError = "in_proj4_projection";
                                    strError2 = "Hotine-Oblique Mercator Two Points";
                                    String strError3 = "not_zero_lat_1";
                                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                }
                                if (Math.abs(Math.abs(lat_2) - 90.0) < 1.0E-4) {
                                    String strError = "in_proj4_projection";
                                    strError2 = "Hotine-Oblique Mercator Two Points";
                                    String strError3 = "not_values_90_or_minus_90_lat_2";
                                    throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                                }
                            }
                        }
                        projAcronym = projectionAcronym[0];
                        if (isLcc) {
                            projAcronym = "lcc";
                        }
                        strExtraProj4 = "";
                        isLaborde = false;
                        isLcc1sp = false;
                        isMerc = false;
                        exists_sf = false;
                        exists_lo = false;
                        isSterePolar = false;
                        isStereOblique = false;
                        exists_stdPar = false;
                        value_stdPar = 0.0;
                        if (!projectionAcronym[0].equals("merc")) break block105;
                        isMerc = true;
                        double value_sf = 0.0;
                        double value_lo = 0.0;
                        int j = 0;
                        while (j < gtParameterNames.length) {
                            String gtParameterName = gtParameterNames[j].trim();
                            if (gtParameterName.equalsIgnoreCase("latitude_of_origin") || gtParameterName.equalsIgnoreCase("standard_parallel_1") || gtParameterName.equalsIgnoreCase("latitude_of_center")) {
                                exists_lo = true;
                                value_lo = Double.parseDouble(gtParameterValues[j]);
                            }
                            if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                                exists_sf = true;
                                value_sf = Double.parseDouble(gtParameterValues[j]);
                            }
                            ++j;
                        }
                        if (!exists_sf || !exists_lo) break block106;
                        if (value_sf != 1.0 && value_lo != 0.0) {
                            String strError = "in_proj4_projection";
                            String strError2 = "Mercator";
                            String strError3 = "not_admit_scale_factor_and_latitude_of_origin";
                            throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3)));
                        }
                        if (!projectionName.equals("Mercator_1SP")) break block107;
                        exists_lo = false;
                        break block106;
                    }
                    if (!projectionName.equals("Mercator_2SP")) break block106;
                    exists_sf = false;
                    break block106;
                }
                if (projectionAcronym[0].equals("lcc")) {
                    if (projectionName.equalsIgnoreCase("Lambert_Conformal_Conic_1SP")) {
                        isLcc1sp = true;
                    }
                } else if (projectionAcronym[0].equals("stere") && projectionName.equalsIgnoreCase("Polar_Stereographic")) {
                    isSterePolar = true;
                    int j = 0;
                    while (j < gtParameterNames.length) {
                        String gtParameterName = gtParameterNames[j].trim();
                        if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                            double value_sf = Double.parseDouble(gtParameterValues[j]);
                            exists_sf = true;
                        }
                        if (gtParameterName.equalsIgnoreCase("latitude_of_standard_parallel")) {
                            value_stdPar = Double.parseDouble(gtParameterValues[j]);
                            exists_stdPar = true;
                        }
                        ++j;
                    }
                }
            }
            if (projectionName.equalsIgnoreCase("Stereographic")) {
                boolean isPolar = false;
                int j = 0;
                while (j < gtParameterNames.length) {
                    String gtParameterAcronymn = parameterAcronyms.get(j).trim();
                    if (gtParameterNames[j].equalsIgnoreCase("latitude_of_origin")) {
                        double gtParameterValue = Double.parseDouble(gtParameterValues[j]);
                        if (Math.abs(gtParameterValue - 90.0) < angularTolerance) {
                            isPolar = true;
                            break;
                        }
                        if (!(Math.abs(gtParameterValue - -90.0) < angularTolerance)) break;
                        isPolar = true;
                        break;
                    }
                    ++j;
                }
                if (isPolar) {
                    isSterePolar = true;
                    projAcronym = "stere";
                    j = 0;
                    while (j < gtParameterNames.length) {
                        String gtParameterName = gtParameterNames[j].trim();
                        if (gtParameterName.equalsIgnoreCase("scale_factor")) {
                            double value_sf = Double.parseDouble(gtParameterValues[j]);
                            exists_sf = true;
                        }
                        if (gtParameterName.equalsIgnoreCase("latitude_of_standard_parallel")) {
                            value_stdPar = Double.parseDouble(gtParameterValues[j]);
                            exists_stdPar = true;
                        }
                        ++j;
                    }
                }
                if (!isPolar) {
                    isStereOblique = true;
                    projAcronym = "sterea";
                    if (exists_stdPar) {
                        String strError = "in_proj4_projection";
                        String strError2 = "Oblique_Stereographic";
                        String strError3 = "not_admit_parameter";
                        String strError4 = "latitude_of_standard_parallel";
                        throw new CrsException(new Exception(String.valueOf(PluginServices.getText(this, strError)) + " " + PluginServices.getText(this, strError2) + " " + PluginServices.getText(this, strError3) + " " + PluginServices.getText(this, strError4)));
                    }
                }
            } else if (projectionAcronym[0].equals("mill")) {
                strExtraProj4 = "+R_A ";
            } else if (projectionAcronym[0].equals("vandg")) {
                strExtraProj4 = "+R_A ";
            } else if (projectionAcronym[0].equals("labrd")) {
                isLaborde = true;
            }
            strProj4 = String.valueOf(strProj4) + projAcronym + " ";
            int i = 0;
            while (i < parameterNames.size()) {
                boolean control = true;
                String parameterName = parameterNames.get(i).trim();
                String parameterAcronym = parameterAcronyms.get(i).trim();
                String strParameterValue = parameterValues.get(i).trim();
                if (isMerc) {
                    if ((parameterName.equalsIgnoreCase("latitude_of_origin") || parameterName.equalsIgnoreCase("standard_parallel_1") || parameterName.equalsIgnoreCase("latitude_of_center")) && !exists_lo) {
                        control = false;
                    }
                    if (parameterName.equalsIgnoreCase("scale_factor") && !exists_sf) {
                        control = false;
                    }
                }
                if (isSterePolar) {
                    String gtParameterAcronymn = parameterAcronyms.get(i).trim();
                    if (gtParameterAcronymn.equalsIgnoreCase("lat_0") && exists_stdPar) {
                        double parameterValue = Double.parseDouble(strParameterValue);
                        if (parameterValue > 0.0 && value_stdPar < 0.0) {
                            strParameterValue = "-90.0";
                        }
                        if (parameterValue < 0.0 && value_stdPar > 0.0) {
                            strParameterValue = "90.0";
                        }
                    }
                    if (gtParameterAcronymn.equalsIgnoreCase("lat_ts") && exists_sf) {
                        control = false;
                    }
                    if (parameterName.equalsIgnoreCase("scale_factor") && !exists_sf) {
                        control = false;
                    }
                }
                if (isSomerc) {
                    if (parameterName.equals("rectified_grid_angle")) {
                        control = false;
                    }
                    if (parameterName.equals("azimuth")) {
                        control = false;
                    }
                }
                if (isOmerc && parameterName.equals("rectified_grid_angle")) {
                    if (existsAlpha) {
                        control = false;
                    } else {
                        parameterAcronym = "alpha";
                    }
                }
                if (parameterAcronym.equals("lon_0") || parameterAcronym.equals("lonc")) {
                    double parameterValue = Double.parseDouble(strParameterValue);
                    if (!projectionAcronym[0].equalsIgnoreCase("krovak")) {
                        parameterValue -= primeMeridianValue;
                    }
                    strParameterValue = Double.toString(parameterValue);
                }
                if (control) {
                    strProj4 = String.valueOf(strProj4) + "+" + parameterAcronym + "=" + strParameterValue + " ";
                }
                if (isLcc1sp && parameterAcronym.equals("lat_0")) {
                    strProj4 = String.valueOf(strProj4) + "+lat_1=" + strParameterValue + " ";
                    strProj4 = String.valueOf(strProj4) + "+lat_2=" + strParameterValue + " ";
                }
                ++i;
            }
            if (isLaborde) {
                strProj4 = String.valueOf(strProj4) + "+azi=18.9 +lat_0=-18.9 +lon_0=44.1 +k_0=0.9995 +x_0=400000 +y_0=800000 +ellps=intl ";
            }
            strProj4 = String.valueOf(strProj4) + strExtraProj4;
        }
        String strEllipseAcronym = this.ellipseToProj4(a, inv_f);
        String strEllipse = "";
        strEllipse = strEllipseAcronym.equals("") ? (!Double.isInfinite(inv_f) ? (inv_f > 0.0 ? "+a=" + a + " +rf=" + inv_f + " " : "+R=" + a + " ") : "+R=" + a + " ") : "+ellps=" + strEllipseAcronym + " ";
        strProj4 = String.valueOf(strProj4) + strEllipse;
        strProj4 = String.valueOf(strProj4) + primeMeridianAcronym;
        if (!strProj4Datum.equals("")) {
            strProj4 = String.valueOf(strProj4) + strProj4Datum;
        }
        if (!strProj4ToMeter.equals("")) {
            strProj4 = String.valueOf(strProj4) + strProj4ToMeter;
        }
        String strWkt = crs.toWKT();
        return strProj4;
    }

    private String[] primeMeridianToProj4(String pmName, double pmValue) throws CrsException {
        String strError;
        String[] primeMeridian = new String[3];
        String pszPM = "";
        String acronym = "";
        double dfFromGreenwich = 0.0;
        double tolerance = 5.555555555555555E-7;
        int nPMCode = -1;
        dfFromGreenwich = -9.131906111111112;
        if (pmName.equalsIgnoreCase("lisbon") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "lisbon";
            nPMCode = 8902;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 2.337229166666667;
        if (pmName.equalsIgnoreCase("paris") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "paris";
            nPMCode = 8903;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = -74.08091666666667;
        if (pmName.equalsIgnoreCase("bogota") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "bogota";
            nPMCode = 8904;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = -3.687938888888889;
        if (pmName.equalsIgnoreCase("madrid") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "madrid";
            nPMCode = 8905;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 12.452333333333332;
        if (pmName.equalsIgnoreCase("rome") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "rome";
            nPMCode = 8906;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 7.439583333333333;
        if (pmName.equalsIgnoreCase("bern") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "bern";
            nPMCode = 8907;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 106.80771944444444;
        if (pmName.equalsIgnoreCase("jakarta") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "jakarta";
            nPMCode = 8908;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = -17.666666666666668;
        if (pmName.equalsIgnoreCase("ferro") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "ferro";
            nPMCode = 8909;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 4.3679749999999995;
        if (pmName.equalsIgnoreCase("brussels") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "brussels";
            nPMCode = 8910;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 18.05827777777778;
        if (pmName.equalsIgnoreCase("stockholm") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "stockholm";
            nPMCode = 8911;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 23.716337499999998;
        if (pmName.equalsIgnoreCase("athens") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "athens";
            nPMCode = 8912;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 10.722916666666666;
        if (pmName.equalsIgnoreCase("oslo") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "oslo";
            nPMCode = 8913;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
            acronym = "+pm=" + pszPM + " ";
        }
        dfFromGreenwich = 0.0;
        if (pmName.equalsIgnoreCase("Greenwich") || Math.abs(dfFromGreenwich - pmValue) < tolerance) {
            pszPM = "Greenwich";
            nPMCode = 0;
            if (Math.abs(dfFromGreenwich - pmValue) > tolerance) {
                strError = "No concuerdan el nombre del meridiano origen y su valor";
                System.out.println(strError);
            }
            pmValue = dfFromGreenwich;
        }
        primeMeridian[0] = pszPM;
        primeMeridian[1] = Double.toString(pmValue);
        primeMeridian[2] = acronym;
        return primeMeridian;
    }

    private String ellipseToProj4(double dfSemiMajor, double dfInvFlattening) {
        double yo = Math.abs(4.5);
        String pszPROJ4Ellipse = "";
        if (Math.abs(dfSemiMajor - 6378249.145) < 0.01 && Math.abs(dfInvFlattening - 293.465) < 1.0E-4) {
            pszPROJ4Ellipse = "clrk80";
        } else if (Math.abs(dfSemiMajor - 6378245.0) < 0.01 && Math.abs(dfInvFlattening - 298.3) < 1.0E-4) {
            pszPROJ4Ellipse = "krass";
        } else if (Math.abs(dfSemiMajor - 6378388.0) < 0.01 && Math.abs(dfInvFlattening - 297.0) < 1.0E-4) {
            pszPROJ4Ellipse = "intl";
        } else if (Math.abs(dfSemiMajor - 6378160.0) < 0.01 && Math.abs(dfInvFlattening - 298.25) < 1.0E-4) {
            pszPROJ4Ellipse = "aust_SA";
        } else if (Math.abs(dfSemiMajor - 6377397.155) < 0.01 && Math.abs(dfInvFlattening - 299.1528128) < 1.0E-4) {
            pszPROJ4Ellipse = "bessel";
        } else if (Math.abs(dfSemiMajor - 6377483.865) < 0.01 && Math.abs(dfInvFlattening - 299.1528128) < 1.0E-4) {
            pszPROJ4Ellipse = "bess_nam";
        } else if (Math.abs(dfSemiMajor - 6378160.0) < 0.01 && Math.abs(dfInvFlattening - 298.247167427) < 1.0E-4) {
            pszPROJ4Ellipse = "GRS67";
        } else if (Math.abs(dfSemiMajor - 6378137.0) < 0.01 && Math.abs(dfInvFlattening - 298.257222101) < 1.0E-6) {
            pszPROJ4Ellipse = "GRS80";
        } else if (Math.abs(dfSemiMajor - 6378206.4) < 0.01 && Math.abs(dfInvFlattening - 294.9786982) < 1.0E-4) {
            pszPROJ4Ellipse = "clrk66";
        } else if (Math.abs(dfSemiMajor - 6378206.4) < 0.01 && Math.abs(dfInvFlattening - 294.9786982) < 1.0E-4) {
            pszPROJ4Ellipse = "mod_airy";
        } else if (Math.abs(dfSemiMajor - 6377563.396) < 0.01 && Math.abs(dfInvFlattening - 299.3249646) < 1.0E-4) {
            pszPROJ4Ellipse = "airy";
        } else if (Math.abs(dfSemiMajor - 6378200.0) < 0.01 && Math.abs(dfInvFlattening - 298.3) < 1.0E-4) {
            pszPROJ4Ellipse = "helmert";
        } else if (Math.abs(dfSemiMajor - 6378155.0) < 0.01 && Math.abs(dfInvFlattening - 298.3) < 1.0E-4) {
            pszPROJ4Ellipse = "fschr60m";
        } else if (Math.abs(dfSemiMajor - 6377298.556) < 0.01 && Math.abs(dfInvFlattening - 300.8017) < 1.0E-4) {
            pszPROJ4Ellipse = "evrstSS";
        } else if (Math.abs(dfSemiMajor - 6378165.0) < 0.01 && Math.abs(dfInvFlattening - 298.3) < 1.0E-4) {
            pszPROJ4Ellipse = "WGS60";
        } else if (Math.abs(dfSemiMajor - 6378145.0) < 0.01 && Math.abs(dfInvFlattening - 298.25) < 1.0E-4) {
            pszPROJ4Ellipse = "WGS66";
        } else if (Math.abs(dfSemiMajor - 6378135.0) < 0.01 && Math.abs(dfInvFlattening - 298.26) < 1.0E-4) {
            pszPROJ4Ellipse = "WGS72";
        } else if (Math.abs(dfSemiMajor - 6378137.0) < 0.01 && Math.abs(dfInvFlattening - 298.257223563) < 1.0E-6) {
            pszPROJ4Ellipse = "WGS84";
        }
        return pszPROJ4Ellipse;
    }

    private String datumToProj4(String datumName, int epsgCode) {
        String datumProj4 = "";
        String SRS_DN_NAD27 = "North_American_Datum_1927";
        String SRS_DN_NAD83 = "North_American_Datum_1983";
        String SRS_DN_WGS72 = "WGS_1972";
        String SRS_DN_WGS84 = "WGS_1984";
        if (datumName.equals("")) {
            datumProj4 = "";
        } else if (datumName.equalsIgnoreCase(SRS_DN_NAD27) || epsgCode == 6267) {
            datumProj4 = "+datum=NAD27 ";
        } else if (datumName.equalsIgnoreCase(SRS_DN_NAD83) || epsgCode == 6269) {
            datumProj4 = "+datum=NAD83 ";
        } else if (datumName.equalsIgnoreCase(SRS_DN_WGS84) || epsgCode == 6326) {
            datumProj4 = "+datum=WGS84 ";
        } else if (epsgCode == 6314) {
            datumProj4 = "+datum=potsdam ";
        } else if (epsgCode == 6272) {
            datumProj4 = "+datum=nzgd49 ";
        }
        return datumProj4;
    }

    private String getName(Identifier name) {
        String[] correctName = name.toString().split(":");
        if (correctName.length < 2) {
            return correctName[0];
        }
        return correctName[1];
    }

    private String[] Spheroid(Ellipsoid ellips) {
        String[] spheroid = new String[3];
        Unit u = ellips.getAxisUnit();
        double semi_major = this.convert(ellips.getSemiMajorAxis(), u.toString());
        double inv_f = ellips.getInverseFlattening();
        String[] val = ellips.getName().toString().split(":");
        spheroid[0] = val.length < 2 ? ellips.getName().toString().split(":")[0] : ellips.getName().toString().split(":")[1];
        spheroid[1] = String.valueOf(semi_major);
        spheroid[2] = String.valueOf(inv_f);
        return spheroid;
    }

    private String[] Primem(PrimeMeridian prim) {
        String[] primem = new String[2];
        DefaultPrimeMeridian pm = (DefaultPrimeMeridian)prim;
        Unit u = pm.getAngularUnit();
        double value = this.convert(pm.getGreenwichLongitude(), u.toString());
        String[] val = pm.getName().toString().split(":");
        primem[0] = val.length < 2 ? pm.getName().toString().split(":")[0] : pm.getName().toString().split(":")[1];
        primem[1] = String.valueOf(value);
        return primem;
    }

    public double convert(double value, String measure) throws ConversionException {
        if (measure.equals("D.MS")) {
            int deg = (int)((value *= (double)this.divider) / 10000.0);
            int min = (int)((value -= (double)(10000 * deg)) / 100.0);
            value -= (double)(100 * min);
            if (min <= -60 || min >= 60) {
                if ((double)Math.abs(Math.abs(min) - 100) <= 1.0E-8) {
                    deg = min >= 0 ? ++deg : --deg;
                    min = 0;
                } else {
                    throw new ConversionException("Invalid minutes: " + min);
                }
            }
            if (value <= -60.0 || value >= 60.0) {
                if (Math.abs(Math.abs(value) - 100.0) <= 1.0E-8) {
                    min = value >= 0.0 ? ++min : --min;
                    value = 0.0;
                } else {
                    throw new ConversionException("Invalid secondes: " + value);
                }
            }
            value = (value / 60.0 + (double)min) / 60.0 + (double)deg;
            return value;
        }
        if (measure.equals("grad") || measure.equals("grade")) {
            return value * 180.0 / 200.0;
        }
        if (measure.equals("\u00b0")) {
            return value;
        }
        if (measure.equals("DMS")) {
            return value;
        }
        if (measure.equals("m") || measure.startsWith("[m")) {
            return value;
        }
        if (measure.equals("")) {
            return value;
        }
        if (measure.equalsIgnoreCase("ft") || measure.equalsIgnoreCase("foot") || measure.equalsIgnoreCase("feet")) {
            return value * 0.3048 / 1.0;
        }
        throw new ConversionException("Conversion no contemplada: " + measure);
    }

    public List<String[]> getProjectionNameList() {
        return this.projectionNameList;
    }
}

