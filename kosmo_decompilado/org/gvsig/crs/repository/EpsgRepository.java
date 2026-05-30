/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.ogr.Epsg2wkt;
import org.gvsig.crs.ogr.GetCRSepsg;
import org.gvsig.crs.repository.ICrsRepository;
import org.saig.jump.lang.I18N;

public class EpsgRepository
implements ICrsRepository {
    private static final Logger LOGGER = Logger.getLogger(EpsgRepository.class);
    public CRSRepositoryConnection connection = new CRSRepositoryConnection();

    @Override
    public ICrs getCrs(String code) throws CrsException {
        ResultSet result;
        Crs crs;
        Epsg2wkt wkt;
        String crs_kind;
        int projection_conv_code;
        int datum_code;
        int source_cod;
        boolean source_yn;
        int epsg_code;
        block13: {
            epsg_code = Integer.parseInt(code);
            source_yn = false;
            source_cod = 0;
            datum_code = 0;
            projection_conv_code = 0;
            crs_kind = null;
            wkt = null;
            crs = null;
            result = null;
            String sentence = "SELECT source_geogcrs_code, projection_conv_code, coord_ref_sys_kind, datum_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
            this.connection.setConnectionEPSG();
            result = Query.select(sentence, this.connection.getConnection());
            try {
                if (result == null) {
                    throw new CrsException(I18N.getMessage("org.gvsig.crs.repository.EpsgRepository.srs-not-found-in-epsg-repository-with-code-{0}", new Object[]{code}));
                }
                if (result.next()) break block13;
                return null;
            }
            catch (SQLException e1) {
                LOGGER.error((Object)I18N.getMessage("org.gvsig.crs.repository.EpsgRepository.crs-code-{0}-has-not-been-found", new Object[]{code}));
                return null;
            }
        }
        source_cod = result.getInt("source_geogcrs_code");
        projection_conv_code = result.getInt("projection_conv_code");
        crs_kind = result.getString("coord_ref_sys_kind");
        datum_code = result.getInt("datum_code");
        source_yn = datum_code != 0 ? true : source_cod == 0;
        GetCRSepsg ep = new GetCRSepsg(epsg_code, source_yn, source_cod, projection_conv_code, this.connection);
        ep.Getepsgdata();
        if (crs_kind.equals("geographic 2D") || crs_kind.equals("geographic 3D")) {
            wkt = new Epsg2wkt(ep, "geog");
        } else if (crs_kind.equals("projected")) {
            wkt = new Epsg2wkt(ep, "proj");
        } else if (crs_kind.equals("compound")) {
            wkt = new Epsg2wkt(ep, "comp");
        } else if (crs_kind.equals("geocentric")) {
            wkt = new Epsg2wkt(ep, "geoc");
        }
        try {
            crs = new Crs(Integer.parseInt(code), wkt.getWKT());
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        return crs;
    }

    @Override
    public void close() throws Exception {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.shutdown();
        }
    }

    public String[] getAxis(int epsg_code) throws CrsException {
        ResultSet result;
        String[] axis;
        block6: {
            block5: {
                axis = null;
                result = null;
                String sentence = "SELECT epsg_coordinateaxis.coord_axis_orientation FROM epsg_coordinateaxis, epsg_coordinatereferencesystem WHERE epsg_coordinatereferencesystem.coord_sys_code = epsg_coordinateaxis.coord_sys_code AND epsg_coordinatereferencesystem.coord_ref_sys_code=" + epsg_code + " ORDER BY coord_axis_order";
                this.connection.setConnectionEPSG();
                result = Query.select(sentence, this.connection.getConnection());
                try {
                    if (result == null) {
                        throw new CrsException(I18N.getMessage("org.gvsig.crs.repository.EpsgRepository.srs-not-found-in-epsg-repository-with-code-{0}", new Object[]{epsg_code}));
                    }
                    if (result.next()) break block5;
                    return null;
                }
                catch (SQLException e1) {
                    LOGGER.error((Object)I18N.getMessage("org.gvsig.crs.repository.EpsgRepository.crs-code-{0}-has-not-been-found", new Object[]{epsg_code}));
                    return null;
                }
            }
            axis = new String[2];
            axis[0] = result.getString("coord_axis_orientation");
            if (result.next()) break block6;
            return null;
        }
        axis[1] = result.getString("coord_axis_orientation");
        return axis;
    }
}

