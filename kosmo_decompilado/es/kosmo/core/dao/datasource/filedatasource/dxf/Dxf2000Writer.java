/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jump.feature.FeatureCollection;
import es.kosmo.core.dao.datasource.filedatasource.dxf.AbstractDxfWriter;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfWriter;
import java.io.Writer;
import java.util.List;

public class Dxf2000Writer
extends AbstractDxfWriter {
    public static final String VERSION = "AC1015";

    public Dxf2000Writer() {
    }

    public Dxf2000Writer(Writer writer) {
        super(writer);
    }

    public Dxf2000Writer(Writer writer, String encoding) {
        super(writer, encoding);
    }

    @Override
    public IDxfWriter newInstance(Writer writer) {
        return new Dxf2000Writer(writer);
    }

    @Override
    public IDxfWriter newInstance(Writer writer, String encoding) {
        return new Dxf2000Writer(writer, encoding);
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
    protected void writeClasses(List<FeatureCollection> fcList) throws Exception {
        this.writeSectionStart("CLASSES");
        this.writeClass("ACDBDICTIONARYWDFLT", "AcDbDictionaryWithDefault", "ObjectDBX Classes", 0, false, false);
        this.writeClass("TABLESTYLE", "AcDbTableStyle", "ObjectDBX Classes", 2047, false, false);
        this.writeClass("DICTIONARYVAR", "AcDbDictionaryVar", "ObjectDBX Classes", 0, false, false);
        this.writeClass("XRECORD", "AcDbXrecord", "AutoCAD 2000", 0, false, false);
        this.writeClass("LWPOLYLINE", "AcDbPolyline", "AutoCAD 2000", 0, false, true);
        this.writeClass("HATCH", "AcDbHatch", "AutoCAD 2000", 0, false, true);
        this.writeClass("ACDBPLACEHOLDER", "AcDbPlaceHolder", "ObjectDBX Classes", 0, false, false);
        this.writeClass("LAYOUT", "AcDbLayout", "ObjectDBX Classes", 0, false, false);
        this.writeSectionEnd();
    }
}

