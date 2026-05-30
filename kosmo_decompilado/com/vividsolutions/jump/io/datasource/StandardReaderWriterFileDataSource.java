/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.FMEGMLReader;
import com.vividsolutions.jump.io.FMEGMLWriter;
import com.vividsolutions.jump.io.GMLReader;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.io.JMLReader;
import com.vividsolutions.jump.io.JMLWriter;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.io.WKTWriter;
import com.vividsolutions.jump.io.datasource.DelegatingCompressedFileHandler;
import com.vividsolutions.jump.io.datasource.ReaderWriterFileDataSource;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import java.util.Arrays;
import java.util.Collection;

public abstract class StandardReaderWriterFileDataSource
extends ReaderWriterFileDataSource {
    protected String[] extensions;
    public static final String GML_EXTENSION = "gml";
    public static final String XML_EXTENSION = "xml";
    public static final String[] GML_EXTENSIONS = new String[]{"gml", "xml"};
    public static final String OUTPUT_TEMPLATE_FILE_KEY = "Output Template File";
    public static final String INPUT_TEMPLATE_FILE_KEY = "Input Template File";

    public StandardReaderWriterFileDataSource(JUMPReader reader, JUMPWriter writer, String[] extensions) {
        super(reader, writer);
        this.extensions = extensions;
    }

    public String[] getExtensions() {
        return this.extensions;
    }

    private static GMLWriter createGMLWriter() {
        return new GMLWriter();
    }

    private static DelegatingCompressedFileHandler createGMLReader() {
        return new DelegatingCompressedFileHandler((JUMPReader)new GMLReader(), (Collection)StandardReaderWriterFileDataSource.toEndings(GML_EXTENSIONS)){

            @Override
            public FeatureCollection read(DriverProperties dp) throws Exception {
                this.mangle(dp, "TemplateFile", "CompressedFileTemplate", Arrays.asList("_input.xml", ".input", ".template"));
                return super.read(dp);
            }
        };
    }

    public static Collection<String> toEndings(String[] extensions) {
        return CollectionUtil.collect(Arrays.asList(extensions), new Block(){

            @Override
            public Object yield(Object extension) {
                return "." + extension;
            }
        });
    }

    private static class ClassicReaderWriterFileDataSource
    extends StandardReaderWriterFileDataSource {
        public ClassicReaderWriterFileDataSource(JUMPReader reader, JUMPWriter writer, String[] extensions) {
            super(new DelegatingCompressedFileHandler(reader, ClassicReaderWriterFileDataSource.toEndings(extensions)), writer, extensions);
            this.extensions = extensions;
        }
    }

    public static class FMEGML
    extends ClassicReaderWriterFileDataSource {
        public FMEGML() {
            super(new FMEGMLReader(), new FMEGMLWriter(), new String[]{StandardReaderWriterFileDataSource.GML_EXTENSION, StandardReaderWriterFileDataSource.XML_EXTENSION, "fme"});
        }
    }

    public static class GML
    extends ClassicReaderWriterFileDataSource {
        public GML() {
            super(StandardReaderWriterFileDataSource.createGMLReader(), StandardReaderWriterFileDataSource.createGMLWriter(), GML_EXTENSIONS);
        }

        @Override
        protected DriverProperties getReaderDriverProperties() {
            return super.getReaderDriverProperties().set("TemplateFile", (String)this.getProperties().get(StandardReaderWriterFileDataSource.INPUT_TEMPLATE_FILE_KEY));
        }

        @Override
        protected DriverProperties getWriterDriverProperties() {
            return super.getWriterDriverProperties().set("TemplateFile", (String)this.getProperties().get(StandardReaderWriterFileDataSource.OUTPUT_TEMPLATE_FILE_KEY));
        }

        @Override
        public boolean isReadable() {
            return this.getProperties().containsKey(StandardReaderWriterFileDataSource.INPUT_TEMPLATE_FILE_KEY);
        }

        @Override
        public boolean isWritable() {
            return this.getProperties().containsKey(StandardReaderWriterFileDataSource.OUTPUT_TEMPLATE_FILE_KEY);
        }
    }

    public static class JML
    extends ClassicReaderWriterFileDataSource {
        public JML() {
            super(new JMLReader(), new JMLWriter(), new String[]{"jml"});
        }
    }

    public static class WKT
    extends ClassicReaderWriterFileDataSource {
        public WKT() {
            super(new WKTReader(), new WKTWriter(), new String[]{"wkt", "txt"});
        }
    }
}

