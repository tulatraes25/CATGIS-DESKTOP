/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jump.feature.FeatureDataset;
import es.kosmo.core.dao.datasource.filedatasource.dxf.AbstractDxfReader;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfReader;
import java.io.Reader;

public class Dxf2000Reader
extends AbstractDxfReader {
    public static final String VERSION = "AC1015";

    public Dxf2000Reader() {
    }

    public Dxf2000Reader(Reader reader) {
        super(reader);
    }

    public Dxf2000Reader(Reader reader, String encoding) {
        super(reader, encoding);
    }

    @Override
    public IDxfReader newInstance(Reader reader) {
        return new Dxf2000Reader(reader);
    }

    @Override
    public IDxfReader newInstance(Reader reader, String encoding) {
        return new Dxf2000Reader(reader, encoding);
    }

    @Override
    public String getDescription() {
        return "DXF 2000";
    }

    @Override
    protected String getVersion() {
        return VERSION;
    }

    @Override
    public FeatureDataset getFeatureDataset() {
        return (FeatureDataset)this.datasets.get("");
    }
}

