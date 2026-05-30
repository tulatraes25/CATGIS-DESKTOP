/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.dbf.nio;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.BigByteBuffer;

public class DbaseFileHeaderNIO {
    private static final int FILE_DESCRIPTOR_SIZE = 32;
    private static final byte MAGIC = 3;
    private static final int MINIMUM_HEADER = 33;
    private Date date = new Date();
    private int recordCnt = 0;
    private int fieldCnt = 0;
    private int myFileType = 0;
    private int recordLength = 1;
    private int headerLength = -1;
    private int largestFieldSize = 0;
    private Logger LOGGER = Logger.getLogger(DbaseFileHeaderNIO.class);
    private DbaseField[] fields = null;

    private void read(ByteBuffer buffer, ReadableByteChannel channel) throws IOException {
        while (buffer.remaining() > 0) {
            if (channel.read(buffer) != -1) continue;
            throw new EOFException("Premature end of file");
        }
    }

    public Class getFieldClass(int i) {
        Class typeClass = null;
        switch (this.fields[i].fieldType) {
            case 'C': {
                typeClass = String.class;
                break;
            }
            case 'N': {
                if (this.fields[i].decimalCount == 0) {
                    if (this.fields[i].fieldLength <= 32) {
                        typeClass = Integer.class;
                        break;
                    }
                    typeClass = Long.class;
                    break;
                }
                typeClass = Double.class;
                break;
            }
            case 'F': {
                typeClass = Double.class;
                break;
            }
            case 'L': {
                typeClass = Boolean.class;
                break;
            }
            case 'D': {
                typeClass = Date.class;
                break;
            }
            default: {
                typeClass = String.class;
            }
        }
        return typeClass;
    }

    public void addColumn(String inFieldName, char inFieldType, int inFieldLength, int inDecimalCount) {
        if (this.fields == null) {
            this.fields = new DbaseField[0];
        }
        int tempLength = 1;
        DbaseField[] tempFieldDescriptors = new DbaseField[this.fields.length + 1];
        int i = 0;
        while (i < this.fields.length) {
            this.fields[i].fieldDataAddress = tempLength;
            tempLength += this.fields[i].fieldLength;
            tempFieldDescriptors[i] = this.fields[i];
            ++i;
        }
        tempFieldDescriptors[this.fields.length] = new DbaseField();
        tempFieldDescriptors[this.fields.length].fieldLength = inFieldLength;
        tempFieldDescriptors[this.fields.length].decimalCount = inDecimalCount;
        tempFieldDescriptors[this.fields.length].fieldDataAddress = tempLength;
        String tempFieldName = inFieldName;
        if (tempFieldName == null) {
            tempFieldName = "NoName";
        }
        if (tempFieldName.length() > 10) {
            tempFieldName = tempFieldName.substring(0, 10);
            this.warn("FieldName " + inFieldName + " is longer than 10 characters, truncating to " + tempFieldName);
        }
        tempFieldDescriptors[this.fields.length].fieldName = tempFieldName;
        if (inFieldType == 'C' || inFieldType == 'c') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)67;
            if (inFieldLength > 254) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Which is longer than 254, not consistent with dbase III");
            }
        } else if (inFieldType == 'S' || inFieldType == 's') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)67;
            this.warn("Field type for " + inFieldName + " set to S which is flat out wrong people!, I am setting this to C, in the hopes you meant character.");
            if (inFieldLength > 254) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Which is longer than 254, not consistent with dbase III");
            }
            tempFieldDescriptors[this.fields.length].fieldLength = 8;
        } else if (inFieldType == 'D' || inFieldType == 'd') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)68;
            if (inFieldLength != 8) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Setting to 8 digets YYYYMMDD");
            }
            tempFieldDescriptors[this.fields.length].fieldLength = 8;
        } else if (inFieldType == 'F' || inFieldType == 'f') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)70;
            if (inFieldLength > 20) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Preserving length, but should be set to Max of 20 not valid for dbase IV, and UP specification, not present in dbaseIII.");
            }
        } else if (inFieldType == 'N' || inFieldType == 'n') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)78;
            if (inFieldLength > 18) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Preserving length, but should be set to Max of 18 for dbase III specification.");
            }
            if (inDecimalCount < 0) {
                this.warn("Field Decimal Position for " + inFieldName + " set to " + inDecimalCount + " Setting to 0 no decimal data will be saved.");
                tempFieldDescriptors[this.fields.length].decimalCount = 0;
            }
            if (inDecimalCount > inFieldLength - 1) {
                this.warn("Field Decimal Position for " + inFieldName + " set to " + inDecimalCount + " Setting to " + (inFieldLength - 1) + " no non decimal data will be saved.");
                tempFieldDescriptors[this.fields.length].decimalCount = inFieldLength - 1;
            }
        } else if (inFieldType == 'L' || inFieldType == 'l') {
            tempFieldDescriptors[this.fields.length].fieldType = (char)76;
            if (inFieldLength != 1) {
                this.warn("Field Length for " + inFieldName + " set to " + inFieldLength + " Setting to length of 1 for logical fields.");
            }
            tempFieldDescriptors[this.fields.length].fieldLength = 1;
        }
        this.fields = tempFieldDescriptors;
        this.fieldCnt = this.fields.length;
        this.headerLength = 33 + 32 * this.fields.length;
        this.recordLength = tempLength += tempFieldDescriptors[this.fields.length].fieldLength;
    }

    public int removeColumn(String inFieldName) {
        int retCol = -1;
        int tempLength = 1;
        DbaseField[] tempFieldDescriptors = new DbaseField[this.fields.length - 1];
        int i = 0;
        int j = 0;
        while (i < this.fields.length) {
            if (!inFieldName.equalsIgnoreCase(this.fields[i].fieldName.trim())) {
                if (i == j && i == this.fields.length - 1) {
                    System.err.println("Could not find a field named '" + inFieldName + "' for removal");
                    return retCol;
                }
                tempFieldDescriptors[j] = this.fields[i];
                tempFieldDescriptors[j].fieldDataAddress = tempLength;
                tempLength += tempFieldDescriptors[j].fieldLength;
                ++j;
            } else {
                retCol = i;
            }
            ++i;
        }
        this.fields = tempFieldDescriptors;
        this.headerLength = 33 + 32 * this.fields.length;
        this.recordLength = tempLength;
        return retCol;
    }

    private void warn(String inWarn) {
        this.LOGGER.warn((Object)inWarn);
    }

    public int getFieldLength(int inIndex) {
        return this.fields[inIndex].fieldLength;
    }

    public int getFieldDecimalCount(int inIndex) {
        return this.fields[inIndex].decimalCount;
    }

    public String getFieldName(int inIndex) {
        return this.fields[inIndex].fieldName;
    }

    public int getFieldIndex(String name) {
        int i = 0;
        while (i < this.fields.length) {
            if (this.fields[i].fieldName.equals(name)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public char getFieldType(int inIndex) {
        return this.fields[inIndex].fieldType;
    }

    public String getRealFieldType(int row) {
        char type = this.fields[row].fieldType;
        String realtype = "";
        switch (type) {
            case 'C': {
                realtype = "STRING";
                break;
            }
            case 'N': {
                if (this.getFieldDecimalCount(row) == 0) {
                    int lenght = this.getFieldLength(row);
                    if (lenght <= 32) {
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
            case 'L': {
                realtype = "BOOLEAN";
                break;
            }
            default: {
                realtype = "STRING";
            }
        }
        return realtype;
    }

    public Date getLastUpdateDate() {
        return this.date;
    }

    public int getNumFields() {
        return this.fields.length;
    }

    public int getNumRecords() {
        return this.recordCnt;
    }

    public int getRecordLength() {
        return this.recordLength;
    }

    public int getHeaderLength() {
        return this.headerLength;
    }

    public void readHeader(BigByteBuffer in) throws IOException {
        this.myFileType = in.get();
        if (this.myFileType != 3) {
            throw new IOException("Unsupported DBF file Type " + Integer.toHexString(this.myFileType));
        }
        int tempUpdateYear = in.get();
        byte tempUpdateMonth = in.get();
        byte tempUpdateDay = in.get();
        Calendar c = Calendar.getInstance();
        c.set(1, tempUpdateYear += 1900);
        c.set(2, tempUpdateMonth - 1);
        c.set(5, tempUpdateDay);
        this.date = c.getTime();
        in.order(ByteOrder.LITTLE_ENDIAN);
        this.recordCnt = in.getInt();
        this.headerLength = in.getShort();
        this.recordLength = in.getShort();
        in.order(ByteOrder.BIG_ENDIAN);
        in.position(in.position() + 20L);
        this.fieldCnt = (this.headerLength - 32 - 1) / 32;
        this.fields = new DbaseField[this.fieldCnt];
        int i = 0;
        while (i < this.fieldCnt) {
            this.fields[i] = new DbaseField();
            byte[] buffer = new byte[11];
            in.get(buffer);
            this.fields[i].fieldName = new String(buffer);
            this.fields[i].fieldType = (char)in.get();
            this.fields[i].fieldDataAddress = in.getInt();
            int tempLength = in.get();
            if (tempLength < 0) {
                tempLength += 256;
            }
            this.fields[i].fieldLength = tempLength;
            this.fields[i].decimalCount = in.get();
            in.position(in.position() + 14L);
            ++i;
        }
        in.get();
    }

    public int getLargestFieldSize() {
        return this.largestFieldSize;
    }

    public void setNumRecords(int inNumRecords) {
        this.recordCnt = inNumRecords;
    }

    public void writeHeader(WritableByteChannel out) throws IOException {
        if (this.headerLength == -1) {
            this.headerLength = 33;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(this.headerLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)3);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        buffer.put((byte)(c.get(1) % 100));
        buffer.put((byte)(c.get(2) + 1));
        buffer.put((byte)c.get(5));
        buffer.putInt(this.recordCnt);
        buffer.putShort((short)this.headerLength);
        buffer.putShort((short)this.recordLength);
        buffer.position(buffer.position() + 20);
        int tempOffset = 0;
        int i = 0;
        while (i < this.fields.length) {
            int j = 0;
            while (j < 11) {
                if (this.fields[i].fieldName.length() > j) {
                    buffer.put((byte)this.fields[i].fieldName.charAt(j));
                } else {
                    buffer.put((byte)0);
                }
                ++j;
            }
            buffer.put((byte)this.fields[i].fieldType);
            buffer.putInt(tempOffset);
            tempOffset += this.fields[i].fieldLength;
            buffer.put((byte)this.fields[i].fieldLength);
            buffer.put((byte)this.fields[i].decimalCount);
            buffer.position(buffer.position() + 14);
            ++i;
        }
        buffer.put((byte)13);
        buffer.position(0);
        int r = buffer.remaining();
        while ((r -= out.write(buffer)) > 0) {
        }
    }

    public String toString() {
        StringBuffer fs = new StringBuffer();
        int i = 0;
        int ii = this.fields.length;
        while (i < ii) {
            DbaseField f = this.fields[i];
            fs.append(String.valueOf(f.fieldName) + " " + f.fieldType + " " + f.fieldLength + " " + f.decimalCount + " " + f.fieldDataAddress + "\n");
            ++i;
        }
        return "DB3 Header\nDate : " + this.date + "\n" + "Records : " + this.recordCnt + "\n" + "Fields : " + this.fieldCnt + "\n" + fs;
    }

    public static DbaseFileHeaderNIO createNewDbaseHeader() throws IOException {
        DbaseFileHeaderNIO header = new DbaseFileHeaderNIO();
        int i = 0;
        int ii = 1;
        while (i < ii) {
            Class<Integer> colType = Integer.class;
            String colName = "ID";
            int fieldLen = 10;
            if (fieldLen <= 0) {
                fieldLen = 255;
            }
            if (colType == Integer.class || colType == Short.class || colType == Byte.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 10), 0);
            } else if (colType == Long.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 19), 0);
            } else if (colType == Double.class || colType == Float.class || colType == Number.class) {
                int l = Math.min(fieldLen, 33);
                int d = Math.max(l - 2, 0);
                header.addColumn(colName, 'N', l, d);
            } else if (Date.class.isAssignableFrom(colType)) {
                header.addColumn(colName, 'D', fieldLen, 0);
            } else if (colType == Boolean.class) {
                header.addColumn(colName, 'L', 1, 0);
            } else if (CharSequence.class.isAssignableFrom(colType)) {
                header.addColumn(colName, 'C', Math.min(254, fieldLen), 0);
            } else if (!Geometry.class.isAssignableFrom(colType)) {
                throw new IOException("Unable to write : " + colType.getName());
            }
            ++i;
        }
        return header;
    }

    public static DbaseFileHeaderNIO createDbaseHeader(FeatureSchema fs) throws IOException {
        try {
            int[] fieldTypes = new int[fs.getAttributeCount() - 1];
            int[] fieldLength = new int[fieldTypes.length];
            int i = 0;
            while (i < fieldTypes.length) {
                fieldTypes[i] = ShapeFileDataSource.getFieldType(i, fs);
                fieldLength[i] = 100;
                ++i;
            }
            return DbaseFileHeaderNIO.createDbaseHeader(ShapeFileDataSource.getFieldNames(fs), fieldTypes, fieldLength);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DbaseFileHeaderNIO createDbaseHeader(String[] fieldNames, int[] fieldTypes, int[] fieldLength) throws IOException {
        DbaseFileHeaderNIO header = new DbaseFileHeaderNIO();
        int i = 0;
        int ii = fieldNames.length;
        while (i < ii) {
            int type = fieldTypes[i];
            String colName = fieldNames[i];
            int fieldLen = fieldLength[i];
            int decimales = 5;
            if (type == 8 || type == 6 || type == 4) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 10), decimales);
            }
            if (type == 91) {
                header.addColumn(colName, 'D', fieldLen, 0);
            }
            if (type == -7 || type == 16) {
                header.addColumn(colName, 'L', 1, 0);
            }
            if (type == 12 || type == 1 || type == -1) {
                header.addColumn(colName, 'C', Math.min(254, fieldLen), 0);
            }
            ++i;
        }
        return header;
    }

    class DbaseField {
        String fieldName;
        char fieldType;
        int fieldDataAddress;
        int fieldLength;
        int decimalCount;

        DbaseField() {
        }
    }
}

