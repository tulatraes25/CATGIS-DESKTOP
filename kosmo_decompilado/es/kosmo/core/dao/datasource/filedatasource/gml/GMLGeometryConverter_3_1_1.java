/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.gml;

import es.kosmo.core.dao.datasource.filedatasource.gml.AbstractGmlGeometryConverter;

public class GMLGeometryConverter_3_1_1
extends AbstractGmlGeometryConverter {
    public GMLGeometryConverter_3_1_1(String crsName) {
        super(crsName);
    }

    @Override
    protected String getPointTemplate() {
        return "GmlPoint_3_1_1.template";
    }

    @Override
    protected String getLineStringTemplate() {
        return "GmlLineString_3_1_1.template";
    }

    @Override
    protected String getPolygonTemplate() {
        return "GmlPolygon_3_1_1.template";
    }

    @Override
    protected String getLinearRingTemplate() {
        return "GmlLinearRing_3_1_1.template";
    }

    @Override
    protected String getMultiPointTemplate() {
        return "GmlMultiPoint_3_1_1.template";
    }

    @Override
    protected String getMultiLineStringTemplate() {
        return "GmlMultiLineString_3_1_1.template";
    }

    @Override
    protected String getMultiPolygonTemplate() {
        return "GmlMultiPolygon_3_1_1.template";
    }
}

