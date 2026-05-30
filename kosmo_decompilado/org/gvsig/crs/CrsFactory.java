/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICRSFactory
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.cresques.cts.ICRSFactory;
import org.cresques.cts.IProjection;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.repository.EpsgRepository;
import org.gvsig.crs.repository.EsriRepository;
import org.gvsig.crs.repository.ICrsRepository;
import org.gvsig.crs.repository.Iau2000Repository;
import org.gvsig.crs.repository.UsrRepository;

public class CrsFactory
implements ICRSFactory {
    Map<String, ICrsRepository> repositories = new HashMap<String, ICrsRepository>();

    public CrsFactory() {
        this.repositories.put("EPSG", new EpsgRepository());
        this.repositories.put("IAU2000", new Iau2000Repository());
        this.repositories.put("ESRI", new EsriRepository());
        this.repositories.put("USR", new UsrRepository());
    }

    public ICrs getCRS(String code) throws CrsException {
        String repoId = "";
        String crsCode = "";
        ICrs crs = null;
        if (code.indexOf(":", code.indexOf(":") + 1) < 0) {
            repoId = code.substring(0, code.indexOf(":"));
            crsCode = code.substring(code.indexOf(":") + 1);
            ICrsRepository repo = this.repositories.get(repoId);
            if (repo == null) {
                return crs;
            }
            crs = repo.getCrs(crsCode);
        } else {
            String sourceParams = null;
            String targetParams = null;
            crsCode = code.substring(0, code.indexOf(":", code.indexOf(":") + 1));
            if (code.indexOf("@") == -1) {
                crsCode = crsCode.substring(0, crsCode.indexOf(","));
            } else {
                sourceParams = code.substring(code.indexOf("@") + 1, code.lastIndexOf("@"));
                targetParams = code.substring(code.lastIndexOf("@") + 1);
                if (sourceParams.equals("")) {
                    sourceParams = null;
                } else if (targetParams.equals("1")) {
                    targetParams = sourceParams;
                    sourceParams = "";
                }
                if (targetParams.equals("") || targetParams.equals("0")) {
                    targetParams = null;
                }
            }
            crs = this.getCRS(crsCode);
            crs.setTransformationParams(sourceParams, targetParams);
        }
        return crs;
    }

    public ICrs getCRS(int epsg_code, String code) throws CrsException {
        Crs crs = new Crs(epsg_code, code);
        return crs;
    }

    public ICrs getCRS(int epsg_code, String code, String params) throws CrsException {
        Crs crs = new Crs(epsg_code, code, params);
        return crs;
    }

    public IProjection get(String name) {
        try {
            return this.getCRS(name);
        }
        catch (CrsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean doesRigurousTransformations() {
        return true;
    }

    public Collection<ICrsRepository> getRepositories() {
        return this.repositories.values();
    }
}

