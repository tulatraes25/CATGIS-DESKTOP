/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.dbf;

import com.vividsolutions.jump.io.EndianDataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfConsts;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.jump.lang.I18N;

public class Dbf
implements DbfConsts {
    static final boolean DEBUG = false;
    static final String DBC = "Dbf->";
    int dbf_id;
    int last_update_d;
    int last_update_m;
    int last_update_y;
    int last_rec;
    int data_offset;
    int rec_size;
    StringBuffer[] records;
    int position = 0;
    boolean hasmemo;
    boolean isFile = false;
    RandomAccessFile rFile;
    EndianDataInputStream dFile;
    int filesize;
    int numfields;
    public DbfFieldDef[] fielddef;

    public Dbf(URL url) throws IOException, DbfFileException {
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        this.init(sfile);
    }

    public Dbf(InputStream in) throws IOException, DbfFileException {
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        this.init(sfile);
    }

    public Dbf(String name) throws IOException, DbfFileException {
        URL url = new URL(name);
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        this.init(sfile);
    }

    public Dbf(File file) throws IOException, DbfFileException {
        FileInputStream in = new FileInputStream(file);
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        this.rFile = new RandomAccessFile(file, "r");
        this.isFile = true;
        this.init(sfile);
    }

    public String getLastUpdate() {
        String date = String.valueOf(this.last_update_d) + "/" + (this.last_update_m + 1) + "/" + (1900 + this.last_update_y);
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

    public int getFieldNumber(String name) {
        int i = 0;
        while (i < this.numfields) {
            if (name.equalsIgnoreCase(this.fielddef[i].fieldname.toString())) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public int getFileSize() {
        return this.filesize;
    }

    public StringBuffer getFieldName(int col) {
        if (col >= this.numfields) {
            throw new IllegalArgumentException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.column-number-specified-is-invalid-it-is-higher-than-the-amount-of-columns-available-{0}", new Object[]{String.valueOf(this.numfields)}));
        }
        return this.fielddef[col].fieldname;
    }

    public char getFieldType(int col) {
        if (col >= this.numfields) {
            throw new IllegalArgumentException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.column-number-specified-is-invalid-it-is-higher-than-the-amount-of-columns-available-{0}", new Object[]{String.valueOf(this.numfields)}));
        }
        return this.fielddef[col].fieldtype;
    }

    private void init(EndianDataInputStream sfile) throws IOException, DbfFileException {
        DbfFileHeader head = new DbfFileHeader(sfile);
        this.dFile = sfile;
        this.fielddef = new DbfFieldDef[this.numfields];
        int widthsofar = 1;
        int index = 0;
        while (index < this.numfields) {
            this.fielddef[index] = new DbfFieldDef();
            this.fielddef[index].setup(widthsofar, sfile);
            widthsofar += this.fielddef[index].fieldlen;
            ++index;
        }
        sfile.skipBytes(1);
        if (!this.isFile) {
            this.records = this.GrabFile();
        }
    }

    public StringBuffer GetNextDbfRec() throws IOException {
        return this.records[this.position++];
    }

    private StringBuffer GrabNextDbfRec() throws IOException {
        StringBuffer record = new StringBuffer(this.rec_size + this.numfields);
        byte[] strbuf = new byte[this.rec_size];
        int i = 0;
        while (i < this.rec_size) {
            strbuf[i] = this.dFile.readByteLE();
            ++i;
        }
        record.append(new String(strbuf));
        return record;
    }

    private StringBuffer[] GrabFile() throws IOException {
        StringBuffer[] records = new StringBuffer[this.last_rec];
        int i = 0;
        while (i < this.last_rec) {
            records[i] = this.GrabNextDbfRec();
            ++i;
        }
        return records;
    }

    public StringBuffer GetDbfRec(int row) throws IOException {
        if (!this.isFile) {
            StringBuffer record = new StringBuffer(this.records[row].toString());
            return record;
        }
        StringBuffer record = new StringBuffer(this.rec_size + this.numfields);
        this.rFile.seek(this.data_offset + this.rec_size * row);
        byte[] strbuf = new byte[this.rec_size];
        int i = 0;
        while (i < this.rec_size) {
            strbuf[i] = this.dFile.readByteLE();
            ++i;
        }
        record.append(new String(strbuf));
        return record;
    }

    public Vector ParseDbfRecord(int row) throws IOException {
        return this.ParseRecord(this.GetDbfRec(row));
    }

    public Vector ParseRecord(StringBuffer rec) {
        Vector<Object> record = new Vector<Object>(this.numfields);
        Integer I = new Integer(0);
        Float F = new Float(0.0);
        String t = rec.toString();
        int i = 0;
        while (i < this.numfields) {
            switch (this.fielddef[i].fieldtype) {
                case 'C': {
                    record.addElement(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen));
                    break;
                }
                case 'F': 
                case 'N': {
                    if (this.fielddef[i].fieldnumdec == 0) {
                        try {
                            record.addElement(Integer.decode(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen)));
                        }
                        catch (NumberFormatException e) {
                            record.addElement(new Integer(0));
                        }
                        break;
                    }
                    try {
                        record.addElement(Float.valueOf(t.substring(this.fielddef[i].fieldstart, this.fielddef[i].fieldstart + this.fielddef[i].fieldlen)));
                        break;
                    }
                    catch (NumberFormatException e) {
                        record.addElement(new Float(0.0));
                    }
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
            throw new DbfFileException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (this.fielddef[col].fieldtype != 'N') {
            throw new DbfFileException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.column-{0}-is-not-integer-{1}", new Object[]{String.valueOf(col), String.valueOf(this.fielddef[col].fieldtype)}));
        }
        if (start < 0) {
            throw new DbfFileException(DBC + I18N.getString("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.start-must-be-greater-or-equal-than-cero"));
        }
        if (end > this.last_rec) {
            throw new DbfFileException(DBC + I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.end-must-be-lower-or-equal-than-{0}", new Object[]{String.valueOf(this.last_rec)}));
        }
        try {
            i = start;
            while (i < end) {
                sb.setLength(0);
                sb = this.GetDbfRec(i);
                record = sb.toString();
                column[i - start] = new Integer(record.substring(this.fielddef[col].fieldstart, this.fielddef[col].fieldstart + this.fielddef[col].fieldlen).trim());
                ++i;
            }
        }
        catch (NumberFormatException nfe) {
            column[i - start] = new Integer(0);
        }
        catch (EOFException e) {
            System.err.println(e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        catch (IOException e) {
            System.err.println(e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        return column;
    }

    public Float[] getFloatCol(int col) throws DbfFileException, IOException {
        return this.getFloatCol(col, 0, this.last_rec);
    }

    public Float[] getFloatCol(int col, int start, int end) throws DbfFileException, IOException {
        Float[] column = new Float[end - start];
        StringBuffer sb = new StringBuffer(this.rec_size);
        int k = 0;
        int i = 0;
        if (col >= this.numfields) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (this.fielddef[col].fieldtype != 'F' && this.fielddef[col].fieldtype != 'N') {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.column-{0}-is-not-float-{1}", new Object[]{String.valueOf(col), String.valueOf(this.fielddef[col].fieldtype)}));
        }
        if (start < 0) {
            throw new DbfFileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.start-must-be-greater-or-equal-than-cero"));
        }
        if (end > this.last_rec) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.end-must-be-lower-or-equal-than-{0}", new Object[]{String.valueOf(this.last_rec)}));
        }
        try {
            i = start;
            while (i < end) {
                sb.setLength(0);
                sb = this.GetDbfRec(i);
                String record = sb.toString();
                String st = new String(record.substring(this.fielddef[col].fieldstart, this.fielddef[col].fieldstart + this.fielddef[col].fieldlen)).trim();
                if (st.indexOf(46) == -1) {
                    st = String.valueOf(st) + ".0";
                }
                try {
                    column[i - start] = new Float(st);
                }
                catch (NumberFormatException e) {
                    column[i - start] = new Float(0.0);
                }
                ++i;
            }
        }
        catch (EOFException e) {
            System.err.println(DBC + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        catch (IOException e) {
            System.err.println(DBC + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        return column;
    }

    public String[] getStringCol(int col) throws DbfFileException, IOException {
        return this.getStringCol(col, 0, this.last_rec);
    }

    public String[] getStringCol(int col, int start, int end) throws DbfFileException, IOException {
        String[] column = new String[end - start];
        String record = "";
        StringBuffer sb = new StringBuffer(this.numfields);
        int k = 0;
        int i = 0;
        if (col >= this.numfields) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.no-such-column-in-file-{0}", new Object[]{String.valueOf(col)}));
        }
        if (start < 0) {
            throw new DbfFileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.start-must-be-greater-or-equal-than-cero"));
        }
        if (end > this.last_rec) {
            throw new DbfFileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.end-must-be-lower-or-equal-than-{0}", new Object[]{String.valueOf(this.last_rec)}));
        }
        try {
            i = start;
            while (i < end) {
                sb.setLength(0);
                sb = this.GetDbfRec(i);
                record = sb.toString();
                column[i - start] = new String(record.getBytes(), this.fielddef[col].fieldstart, this.fielddef[col].fieldlen).trim();
                ++i;
            }
        }
        catch (EOFException e) {
            System.err.println(DBC + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        catch (IOException e) {
            System.err.println(DBC + e);
            System.err.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.Dbf.record-{0}-byte-{1}-file-pos", new Object[]{String.valueOf(i), String.valueOf(k)}));
        }
        return column;
    }

    class DbfFileHeader {
        public DbfFileHeader(EndianDataInputStream file) throws IOException {
            this.getDbfFileHeader(file);
        }

        private void getDbfFileHeader(EndianDataInputStream file) throws IOException {
            Dbf.this.dbf_id = file.readUnsignedByteLE();
            Dbf.this.hasmemo = Dbf.this.dbf_id == 3;
            Dbf.this.last_update_y = file.readUnsignedByteLE();
            Dbf.this.last_update_m = file.readUnsignedByteLE();
            Dbf.this.last_update_d = file.readUnsignedByteLE();
            Dbf.this.last_rec = file.readIntLE();
            Dbf.this.data_offset = file.readShortLE();
            Dbf.this.rec_size = file.readShortLE();
            Dbf.this.filesize = Dbf.this.rec_size * Dbf.this.last_rec + Dbf.this.data_offset + 1;
            Dbf.this.numfields = (Dbf.this.data_offset - 32 - 1) / 32;
            file.skipBytes(20);
        }
    }
}

