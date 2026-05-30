/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package es.kosmo.core.crs;

import es.kosmo.core.crs.CrsAxisOrder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.repository.EpsgRepository;
import org.gvsig.crs.repository.ICrsRepository;

public class CrsRepositoryManager {
    private static final Logger LOGGER = Logger.getLogger(CrsRepositoryManager.class);
    protected static CrsRepositoryManager _instance;
    private static CrsFactory factory;
    public static final String DEFAULT_SRS = "EPSG:4326";

    private CrsRepositoryManager() {
        factory = new CrsFactory();
    }

    public static CrsRepositoryManager getInstance() {
        if (_instance == null) {
            _instance = new CrsRepositoryManager();
        }
        return _instance;
    }

    public void close() throws Exception {
        for (ICrsRepository currentRep : factory.getRepositories()) {
            currentRep.close();
        }
    }

    public ICrs getCRS(String code) throws CrsException {
        return factory.getCRS(code);
    }

    public ICrs getCRS(int epsg_code, String code) throws CrsException {
        return factory.getCRS(epsg_code, code);
    }

    public ICrs getCRS(int epsg_code, String code, String params) throws CrsException {
        return factory.getCRS(epsg_code, code, params);
    }

    public IProjection get(String name) {
        return factory.get(name);
    }

    public CrsAxisOrder getAxisOrder(String crsName) throws CrsException {
        CrsAxisOrder axisOrder = CrsAxisOrder.EAST_NORTH;
        ICrs proj = this.getCRS(crsName);
        if (proj == null) {
            return axisOrder;
        }
        int epsg_code = proj.getCode();
        for (ICrsRepository repo : factory.getRepositories()) {
            EpsgRepository epsgRepository;
            Object[] axis;
            if (!(repo instanceof EpsgRepository) || ArrayUtils.isEmpty((Object[])(axis = (epsgRepository = (EpsgRepository)repo).getAxis(epsg_code)))) continue;
            Object firstAxis = axis[0];
            Object secondAxis = axis[1];
            if (((String)firstAxis).equalsIgnoreCase("east") && ((String)secondAxis).equalsIgnoreCase("north")) {
                axisOrder = CrsAxisOrder.EAST_NORTH;
                continue;
            }
            if (((String)firstAxis).equalsIgnoreCase("north") && ((String)secondAxis).equalsIgnoreCase("east")) {
                axisOrder = CrsAxisOrder.NORTH_EAST;
                continue;
            }
            if (((String)firstAxis).equalsIgnoreCase("west") && ((String)secondAxis).equalsIgnoreCase("south")) {
                axisOrder = CrsAxisOrder.WEST_SOUTH;
                continue;
            }
            if (((String)firstAxis).equalsIgnoreCase("south") && ((String)secondAxis).equalsIgnoreCase("west")) {
                axisOrder = CrsAxisOrder.SOUTH_WEST;
                continue;
            }
            LOGGER.warn((Object)("Unknow axis orientation " + (String)firstAxis + " - " + (String)secondAxis));
        }
        return axisOrder;
    }
}

