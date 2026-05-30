/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.gvsig.crs.ogr.CrsOgrException;
import org.gvsig.crs.ogr.JNIBaseCRS;
import org.gvsig.crs.ogr.OGRException;
import org.gvsig.crs.ogr.OGRSpatialReference;
import org.gvsig.crs.ogr.crsgdalException;
import org.saig.jump.lang.I18N;

public class CrsOgr
extends JNIBaseCRS {
    public static String exportToWkt(OGRSpatialReference ORGSpace) throws CrsOgrException, crsgdalException {
        String result = CrsOgr.exportToWktNat(ORGSpace.getPtr());
        if (result.length() == 0) {
            throw new CrsOgrException(ORGSpace);
        }
        return result;
    }

    public static int importFromWkt(OGRSpatialReference ORGSpace, String cadenas) throws crsgdalException, CrsOgrException, OGRException {
        int result = CrsOgr.importFromWktNat(ORGSpace.getPtr(), cadenas);
        if (result != 0) {
            throw new OGRException(result, I18N.getString(CrsOgr.class, "error-in-wkt-object-creation-was-not-successfull"), ORGSpace);
        }
        return result;
    }

    public static int setUTM(OGRSpatialReference ORGSpace, int zona, boolean norte_sur) throws OGRException, crsgdalException, CrsOgrException {
        if (ORGSpace.getPtr() <= 0L) {
            throw new OGRException(I18N.getString(CrsOgr.class, "error-in-setutm-object-creation-was-not-successfull"));
        }
        int ns = -1;
        ns = norte_sur ? 1 : 0;
        int res = CrsOgr.setUTMNat(ORGSpace.getPtr(), zona, ns);
        if (res != 0) {
            throw new OGRException(res, I18N.getString(CrsOgr.class, "error-in-setutm-specified-zone-could-not-be-assigned"), ORGSpace);
        }
        return res;
    }

    public static int setWellKnownGeogCS(OGRSpatialReference ORGSpace, String cs) throws OGRException, crsgdalException, CrsOgrException {
        if (ORGSpace.getPtr() <= 0L) {
            throw new OGRException(I18N.getString(CrsOgr.class, "error-in-setwellknowngeogcs-object-creation-was-not-successfull"));
        }
        int res = CrsOgr.setWellKnownGeogCSNat(ORGSpace.getPtr(), cs);
        if (res != 0) {
            throw new OGRException(res, I18N.getString(CrsOgr.class, "error-in-setwellknowngeogcs-could-not-assign-specified-coordinates-system"), ORGSpace);
        }
        return res;
    }

    public static int importFromEPSG(OGRSpatialReference ORGSpace, int cod) throws OGRException, crsgdalException, CrsOgrException {
        int result = CrsOgr.importFromEPSGNat(ORGSpace.getPtr(), cod);
        if (result != 0) {
            throw new OGRException(result, I18N.getString(CrsOgr.class, "error-in-epsg-object-creation-was-not-successfull"), ORGSpace);
        }
        return result;
    }

    public static int importFromProj4(OGRSpatialReference ORGSpace, String cs) throws OGRException, crsgdalException, CrsOgrException {
        if (ORGSpace.getPtr() <= 0L) {
            throw new OGRException(I18N.getString(CrsOgr.class, "error-in-proj4-object-creation-was-not-successfull"));
        }
        int res = CrsOgr.importFromProj4Nat(ORGSpace.getPtr(), cs);
        if (res != 0) {
            throw new OGRException(res, I18N.getString(CrsOgr.class, "error-in-proj4-could-not-assign-specified-coordinates-system"), ORGSpace);
        }
        return res;
    }

    public static String exportToProj4(OGRSpatialReference ORGSpace) throws CrsOgrException, crsgdalException {
        String result = CrsOgr.exportToProj4Nat(ORGSpace.getPtr());
        if (result.length() == 0) {
            throw new CrsOgrException(ORGSpace);
        }
        return result;
    }

    public static int importFromPCI(OGRSpatialReference ORGSpace, String cod, String cs, double[] coord) throws CrsOgrException, crsgdalException, OGRException {
        int result = CrsOgr.importFromPCINat(ORGSpace.getPtr(), cod, cs, coord);
        if (result != 0) {
            throw new OGRException(result, I18N.getString(CrsOgr.class, "error-in-pci-object-creation-was-not-successfull"), ORGSpace);
        }
        return result;
    }

    public static int importFromUSGS(OGRSpatialReference ORGSpace, long code, long zone, double[] params, long datum) throws crsgdalException, OGRException, CrsOgrException {
        int result = CrsOgr.importFromUSGSNat(ORGSpace.getPtr(), code, zone, params, datum);
        if (result != 0) {
            throw new OGRException(result, I18N.getString(CrsOgr.class, "error-in-usgs-object-creation-was-not-successfull"), ORGSpace);
        }
        return result;
    }

    public static int importFromESRI(OGRSpatialReference ORGSpace, String cadenas) throws crsgdalException, CrsOgrException, OGRException {
        int result = CrsOgr.importFromESRINat(ORGSpace.getPtr(), cadenas);
        if (result != 0) {
            throw new OGRException(result, I18N.getString(CrsOgr.class, "error-in-esri-object-creation-was-not-successfull"), ORGSpace);
        }
        return result;
    }
}

