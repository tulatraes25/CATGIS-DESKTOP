/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.dbf;

import com.iver.cit.gvsig.fmap.drivers.dbf.DbfEncodings;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfConsts;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFile;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.dao.datasource.filedatasource.dbf.FormatedString;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.jump.lang.I18N;

public class DbfFileWriter
implements DbfConsts {
    private static final String DBC = "DbFW>";
    private static final Logger LOGGER = Logger.getLogger(DbfFileWriter.class);
    int NoFields = 1;
    int NoRecs = 0;
    int recLength = 0;
    DbfFieldDef[] fields;
    EndianDataOutputStream ls;
    private boolean header = false;
    String[] fieldsNames;
    private String fileName;
    private Charset charset;
    int dp = 2;

    public DbfFileWriter(String file) throws IOException {
        this(file, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
    }

    public DbfFileWriter(String file, Charset charsetToApply) throws IOException {
        this.ls = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        this.fileName = file;
        this.charset = charsetToApply;
    }

    public void writeRealHeader(int nrecs) throws DbfFileException, IOException {
        if (!this.header) {
            throw new DbfFileException("DbFW>The starting header must be written before writting the real one");
        }
        this.ls.close();
        WritableByteChannel wbc = SHP.getWriteChannel(this.fileName);
        ByteBuffer bf = ByteBuffer.allocateDirect(16384);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        bf.put((byte)3);
        GregorianCalendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        bf.put((byte)(calendar.get(1) - 1900));
        bf.put((byte)(calendar.get(2) + 1));
        bf.put((byte)calendar.get(5));
        bf.putInt(nrecs);
        bf.flip();
        wbc.write(bf);
        wbc.close();
        bf = null;
    }

    public void writeHeader(DbfFieldDef[] f, int nrecs) throws IOException {
        this.NoFields = f.length;
        this.NoRecs = nrecs;
        this.fields = new DbfFieldDef[this.NoFields];
        int i = 0;
        while (i < this.NoFields) {
            this.fields[i] = f[i];
            ++i;
        }
        this.ls.writeByteLE(3);
        GregorianCalendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        this.ls.writeByteLE(calendar.get(1) - 1900);
        this.ls.writeByteLE(calendar.get(2) + 1);
        this.ls.writeByteLE(calendar.get(5));
        int dataOffset = 32 * this.NoFields + 32 + 1;
        int i2 = 0;
        while (i2 < this.NoFields) {
            this.recLength += this.fields[i2].fieldlen;
            ++i2;
        }
        ++this.recLength;
        this.ls.writeIntLE(this.NoRecs);
        this.ls.writeShortLE(dataOffset);
        this.ls.writeShortLE(this.recLength);
        i2 = 0;
        while (i2 < 17) {
            this.ls.writeByteLE(0);
            ++i2;
        }
        this.ls.writeByteLE(DbfEncodings.getInstance().getDbfIdForCharset(this.charset));
        i2 = 0;
        while (i2 < 2) {
            this.ls.writeByteLE(0);
            ++i2;
        }
        i2 = 0;
        while (i2 < this.NoFields) {
            this.ls.writeBytesLE(this.fields[i2].fieldname.toString());
            this.ls.writeByteLE(this.fields[i2].fieldtype);
            int j = 0;
            while (j < 4) {
                this.ls.writeByteLE(0);
                ++j;
            }
            this.ls.writeByteLE(this.fields[i2].fieldlen);
            this.ls.writeByteLE(this.fields[i2].fieldnumdec);
            j = 0;
            while (j < 14) {
                this.ls.writeByteLE(0);
                ++j;
            }
            ++i2;
        }
        this.ls.writeByteLE(13);
        this.header = true;
    }

    public void writeRecords(Vector[] recs) throws DbfFileException, IOException {
        if (!this.header) {
            throw new DbfFileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter.must-write-header-before-records"));
        }
        int i = 0;
        try {
            i = 0;
            while (i < recs.length) {
                if (recs[i].size() != this.NoFields) {
                    throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter.wrong-number-of-records-in-{0}-th-record-{1}-expected-{2}", new Object[]{String.valueOf(i), String.valueOf(recs[i].size()), String.valueOf(this.NoFields)}));
                }
                this.writeRecord(recs[i]);
                ++i;
            }
        }
        catch (DbfFileException e) {
            throw new DbfFileException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter.at-record-{0}", new Object[]{String.valueOf(i)}) + "\n" + e);
        }
    }

    public void writeRecord(Vector rec) throws DbfFileException, IOException {
        if (!this.header) {
            throw new DbfFileException(DBC + I18N.getString("org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter.must-write-header-before-records"));
        }
        if (rec.size() != this.NoFields) {
            throw new DbfFileException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter.wrong-number-of-records-in-{0}-th-record-{1}-expected-{2}", new Object[]{"", String.valueOf(rec.size()), String.valueOf(this.NoFields)}));
        }
        String s = "";
        this.ls.writeByteLE(32);
        int i = 0;
        while (i < this.NoFields) {
            int len = this.fields[i].fieldlen;
            Object o = rec.elementAt(i);
            switch (this.fields[i].fieldtype) {
                case 'C': 
                case 'D': 
                case 'G': 
                case 'L': 
                case 'M': 
                case 'c': {
                    if (o == null) {
                        o = "";
                    }
                    if (o instanceof Date) {
                        try {
                            o = DbfFile.DATE_PARSER.format((Date)o);
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            o = new String();
                        }
                    }
                    if (o instanceof Boolean) {
                        o = o.toString();
                    }
                    this.ls.write(this.getGoodString(o, this.fields[i].fieldlen).toString().getBytes(this.charset.name()), this.fields[i].fieldstart, this.fields[i].fieldlen);
                    break;
                }
                case 'N': 
                case 'n': {
                    if (o == null) {
                        this.ls.writeBytesLE(this.getGoodString("", this.fields[i].fieldlen));
                        break;
                    }
                    if (this.fields[i].fieldnumdec == 0) {
                        if (o instanceof Integer) {
                            this.ls.writeBytesLE(FormatedString.format((Integer)o, this.fields[i].fieldlen));
                            break;
                        }
                        if (o instanceof Long) {
                            this.ls.writeBytesLE(FormatedString.format((Long)o, this.fields[i].fieldlen));
                            break;
                        }
                        this.ls.writeBytesLE(FormatedString.format(((Number)o).intValue(), this.fields[i].fieldlen));
                        break;
                    }
                }
                case 'F': 
                case 'f': {
                    if (o == null) {
                        this.ls.writeBytesLE(this.getGoodString("", this.fields[i].fieldlen));
                        break;
                    }
                    if (o instanceof Double) {
                        s = ((Double)o).toString();
                    } else if (o instanceof Float) {
                        s = ((Float)o).toString();
                    } else if (o instanceof BigDecimal) {
                        s = ((BigDecimal)o).toString();
                    }
                    String x = FormatedString.format(s, this.fields[i].fieldnumdec, this.fields[i].fieldlen);
                    this.ls.writeBytesLE(x);
                }
            }
            ++i;
        }
    }

    public void close() throws IOException {
        this.ls.writeByteLE(26);
        this.ls.close();
    }

    public String[] getFieldsNames() {
        return this.fieldsNames;
    }

    public void setFieldsNames(String[] fieldsNames) {
        this.fieldsNames = fieldsNames;
    }

    public DbfFieldDef[] getFields() {
        return this.fields;
    }

    private String getGoodString(Object value, int fieldLength) {
        String ss = (String)value;
        while (ss.length() < fieldLength) {
            ss = String.valueOf(ss) + "                                                                                                                  ";
        }
        StringBuffer tmps = new StringBuffer(ss);
        tmps.setLength(fieldLength);
        return tmps.toString();
    }
}

