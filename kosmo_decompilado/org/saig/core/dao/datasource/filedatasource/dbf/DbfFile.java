/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.dbf;

import com.vividsolutions.jump.io.EndianDataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfConsts;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class DbfFile
implements DbfConsts {
    static final boolean DEBUG = false;
    int dbf_id;
    int last_update_d;
    int last_update_m;
    int last_update_y;
    int last_rec;
    int data_offset;
    int rec_size;
    boolean hasmemo;
    public EndianDataInputStream dFile;
    RandomAccessFile rFile;
    int filesize;
    int numfields;
    public DbfFieldDef[] fielddef;
    public static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd"){
        {
            this.setLenient(false);
        }
    };

    protected DbfFile() {
    }

    public DbfFile(String file) throws IOException, DbfFileException {
        FileInputStream in = new FileInputStream(file);
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        this.rFile = new RandomAccessFile(new File(file), "r");
        this.init(sfile);
    }

    public String getLastUpdate() {
        String date = String.valueOf(this.last_update_d) + "/" + this.last_update_m + "/" + this.last_update_y;
        return date;
    }

    public int getLastRec() {
        return this.last_rec;
    }

    public int getRecSize() {
        return this.rec_size;
    }

    public int getNumFields() {
        return this.numfields;
    }

    public String getFieldName(int row) {
        return this.fielddef[row].fieldname.toString();
    }

    public String getFieldType(int row) {
        char type = this.fielddef[row].fieldtype;
        String realtype = "";
        switch (type) {
            case 'C': {
                realtype = "STRING";
                break;
            }
            case 'N': {
                if (this.fielddef[row].fieldnumdec == 0) {
                    if (this.fielddef[row].fieldlen <= 32) {
                        realtype = "INTEGER";
                        break;
                    }
                    realtype = "LONG";
                    break;
                }
                realtype = "DOUBLE";
                break;
            }
            case 'F': {
                realtype = "DOUBLE";
                break;
            }
            case 'D': {
                realtype = "DATE";
                break;
            }
            default: {
                realtype = "STRING";
            }
        }
        return realtype;
    }

    public int getFileSize() {
        return this.filesize;
    }

    private void init(EndianDataInputStream sfile) throws IOException, DbfFileException {
        DbfFileHeader head = new DbfFileHeader(sfile);
        this.dFile = sfile;
        this.fielddef = new DbfFieldDef[this.numfields];
        int widthsofar = 1;
        int index = 0;
        while (index < this.numfields) {
            this.fielddef[index] = new DbfFieldDef();
            this.fielddef[index].setup(widthsofar, this.dFile);
            widthsofar += this.fielddef[index].fieldlen;
            ++index;
        }
        sfile.skipBytes(1);
    }

    public StringBuffer GetNextDbfRec() throws IOException {
        StringBuffer record = new StringBuffer(this.rec_size + this.numfields);
        int i = 0;
        while (i < this.rec_size) {
            record.append((char)this.rFile.readUnsignedByte());
            ++i;
        }
        return record;
    }

    public StringBuffer GetDbfRec(int row) throws IOException {
        StringBuffer record = new StringBuffer(this.rec_size + this.numfields);
        this.rFile.seek(this.data_offset + this.rec_size * row);
        byte[] strbuf = new byte[this.rec_size];
        this.dFile.readByteLEnum(strbuf);
        record.append(new String(strbuf));
        return record;
    }

    public Vector ParseDbfRecord(int row) throws IOException {
        return this.ParseRecord(this.GetDbfRec(row));
    }

    public Object ParseRecordColumn(StringBuffer rec, int wantedCol) throws Exception {
        int start = this.fielddef[wantedCol].fieldstart;
        int end = start + this.fielddef[wantedCol].fieldlen;
        switch (this.fielddef[wantedCol].fieldtype) {
            case 'C': {
                return rec.substring(start, end);
            }
            case 'F': 
            case 'N': {
                boolean isInteger = this.fielddef[wantedCol].fieldnumdec == 0 && this.fielddef[wantedCol].fieldlen <= 32 && this.fielddef[wantedCol].fieldtype == 'N';
                boolean isLong = this.fielddef[wantedCol].fieldnumdec == 0 && this.fielddef[wantedCol].fieldlen > 32 && this.fielddef[wantedCol].fieldtype == 'N';
                String numb = rec.substring(start, end).trim();
                if (isInteger) {
                    try {
                        return new Integer(numb);
                    }
                    catch (NumberFormatException e) {
                        return new Integer(0);
                    }
                }
                if (isLong) {
                    try {
                        return new Long(numb);
                    }
                    catch (NumberFormatException e) {
                        return new Long(0L);
                    }
                }
                try {
                    return new Double(numb);
                }
                catch (NumberFormatException e) {
                    return new Double(Double.NaN);
                }
            }
            case 'D': {
                return this.parseDate(rec.substring(start, end));
            }
        }
        return rec.substring(start, end);
    }

    public Vector ParseRecord(StringBuffer rec) {
        Vector<Object> record = new Vector<Object>(this.numfields);
        Integer I = new Integer(0);
        Double F = new Double(0.0);
        Long L = new Long(0L);
        String t = rec.toString();
        int i = 0;
        while (i < this.numfields) {
            switch (this.fielddef[i].fieldtype) {
                case 'C': {
                    record.addElement(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen));
                    break;
                }
                case 'N': {
                    if (this.fielddef[i].fieldnumdec == 0) {
                        String tt;
                        if (this.fielddef[i].fieldlen <= 32) {
                            try {
                                tt = t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen);
                                record.addElement(Integer.valueOf(tt.trim()));
                            }
                            catch (NumberFormatException e) {
                                record.addElement(new Integer(0));
                            }
                            break;
                        }
                        try {
                            tt = t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen);
                            record.addElement(Long.valueOf(tt.trim()));
                        }
                        catch (NumberFormatException e) {
                            record.addElement(new Long(0L));
                        }
                        break;
                    }
                    try {
                        record.addElement(Double.valueOf(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen).trim()));
                    }
                    catch (NumberFormatException e) {
                        record.addElement(new Double(0.0));
                    }
                    break;
                }
                case 'F': {
                    try {
                        record.addElement(Double.valueOf(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen).trim()));
                    }
                    catch (NumberFormatException e) {
                        record.addElement(new Double(0.0));
                    }
                    break;
                }
                case 'D': {
                    throw new I18NUnsupportedOperationException();
                }
                default: {
                    record.addElement(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen));
                }
            }
            ++i;
        }
        return record;
    }

    public Integer[] getIntegerCol(int col) throws IOException, DbfFileException {
        return this.getIntegerCol(col, 0, this.last_rec);
    }

    public Integer[] getIntegerCol(int col, int start, int end) throws IOException, DbfFileException {
        Integer[] column = new Integer[end - start];
        String record = "";
        StringBuffer sb = new StringBuffer(this.numfields);
        int k = 0;
        int i = 0;
        if (col >= this.numfields) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (this.fielddef[col].fieldtype != 'N') {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.column-{0}-is-not-integer", new Object[]{String.valueOf(col)}));
        }
        try {
            this.rFile.seek(this.data_offset + this.rec_size * start);
            i = start;
            while (i < end) {
                sb.setLength(0);
                k = 0;
                while (k < this.rec_size) {
                    sb.append((char)this.rFile.readUnsignedByte());
                    ++k;
                }
                record = sb.toString();
                try {
                    column[i - start] = new Integer(record.substring(this.fielddef[col].fieldstart, this.fielddef[col].fieldstart + this.fielddef[col].fieldlen));
                }
                catch (NumberFormatException e) {
                    column[i - start] = new Integer(0);
                }
                ++i;
            }
        }
        catch (EOFException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        catch (IOException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        return column;
    }

    public Double[] getFloatCol(int col) throws DbfFileException, IOException {
        return this.getFloatCol(col, 0, this.last_rec);
    }

    public Double[] getFloatCol(int col, int start, int end) throws DbfFileException, IOException {
        Double[] column = new Double[end - start];
        StringBuffer sb = new StringBuffer(this.rec_size);
        int k = 0;
        int i = 0;
        if (col >= this.numfields) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (this.fielddef[col].fieldtype != 'F') {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.column-{0}-is-not-double-{1}", new Object[]{String.valueOf(col), String.valueOf(this.fielddef[col].fieldtype)}));
        }
        try {
            this.rFile.seek(this.data_offset + this.rec_size * start);
            i = start;
            while (i < end) {
                sb.setLength(0);
                k = 0;
                while (k < this.rec_size) {
                    sb.append((char)this.rFile.readUnsignedByte());
                    ++k;
                }
                String record = sb.toString();
                String st = new String(record.substring(this.fielddef[col].fieldstart, this.fielddef[col].fieldstart + this.fielddef[col].fieldlen));
                if (st.indexOf(46) == -1) {
                    st = String.valueOf(st) + ".0";
                }
                try {
                    column[i - start] = new Double(st);
                }
                catch (NumberFormatException e) {
                    column[i - start] = new Double(0.0);
                }
                ++i;
            }
        }
        catch (EOFException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        catch (IOException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        return column;
    }

    public String[] getStringCol(int col) throws DbfFileException, IOException {
        return this.getStringCol(col, 0, this.last_rec);
    }

    public String[] getStringCol(int col, int start, int end) throws DbfFileException, IOException {
        String[] column = new String[end - start];
        String record = "";
        int k = 0;
        int i = 0;
        if (col >= this.numfields) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (this.fielddef[col].fieldtype != 'C') {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.column-{0}-is-not-a-string", new Object[]{String.valueOf(col)}));
        }
        try {
            this.rFile.seek(this.data_offset + start * this.rec_size);
            i = start;
            while (i < end) {
                byte[] strbuf = new byte[this.rec_size];
                k = 0;
                while (k < this.rec_size) {
                    strbuf[k] = this.rFile.readByte();
                    ++k;
                }
                record = new String(strbuf);
                column[i - start] = new String(strbuf, this.fielddef[col].fieldstart, this.fielddef[col].fieldlen);
                ++i;
            }
        }
        catch (EOFException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        catch (IOException e) {
            System.err.println("DbFi>" + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFile.record-{0}-byte-{1}-file-pos-{2}", new Object[]{String.valueOf(i), String.valueOf(k), String.valueOf(this.rFile.getFilePointer())}));
        }
        return column;
    }

    public void close() throws IOException {
        this.dFile.close();
        this.rFile.close();
    }

    protected Date parseDate(String s) throws ParseException {
        if (s.trim().length() == 0) {
            return null;
        }
        if (s.equals("00000000")) {
            return DATE_PARSER.parse("00010101");
        }
        return DATE_PARSER.parse(s);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new SimpleDateFormat("yyyymmdd"){
            {
                this.setLenient(false);
            }
        }.parse("00010101"));
    }

    class DbfFileHeader {
        public DbfFileHeader(EndianDataInputStream file) throws IOException {
            this.getDbfFileHeader(file);
        }

        private void getDbfFileHeader(EndianDataInputStream file) throws IOException {
            DbfFile.this.dbf_id = file.readUnsignedByteLE();
            DbfFile.this.hasmemo = DbfFile.this.dbf_id != 3;
            DbfFile.this.last_update_y = file.readUnsignedByteLE() + 1900;
            DbfFile.this.last_update_m = file.readUnsignedByteLE();
            DbfFile.this.last_update_d = file.readUnsignedByteLE();
            DbfFile.this.last_rec = file.readIntLE();
            DbfFile.this.data_offset = file.readShortLE();
            DbfFile.this.rec_size = file.readShortLE();
            DbfFile.this.filesize = DbfFile.this.rec_size * DbfFile.this.last_rec + DbfFile.this.data_offset + 1;
            DbfFile.this.numfields = (DbfFile.this.data_offset - 32 - 1) / 32;
            file.skipBytes(20);
        }
    }
}

