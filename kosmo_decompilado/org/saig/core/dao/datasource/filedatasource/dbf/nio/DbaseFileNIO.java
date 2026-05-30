/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.dbf.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileHeaderNIO;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.BigByteBuffer;

public class DbaseFileNIO {
    private static final Logger LOGGER = Logger.getLogger(DbaseFileNIO.class);
    private Charset stringCharset;
    private DbaseFileHeaderNIO myHeader;
    private FileInputStream fin;
    private FileChannel channel;
    private BigByteBuffer buffer;
    private File file;

    public DbaseFileNIO() {
        this.stringCharset = Charset.forName("ISO-8859-1");
    }

    public DbaseFileNIO(Charset charset) {
        this.stringCharset = charset;
    }

    public int getRecordCount() {
        return this.myHeader.getNumRecords();
    }

    public int getFieldCount() {
        return this.myHeader.getNumFields();
    }

    public boolean getBooleanFieldValue(int rowIndex, int fieldId) {
        int recordOffset = this.myHeader.getRecordLength() * rowIndex + this.myHeader.getHeaderLength() + 1;
        int fieldOffset = 0;
        int i = 0;
        while (i < fieldId - 1) {
            fieldOffset += this.myHeader.getFieldLength(i);
            ++i;
        }
        this.buffer.position(recordOffset + fieldOffset);
        char bool = (char)this.buffer.get();
        return bool == 't' || bool == 'T' || bool == 'Y' || bool == 'y';
    }

    public String getStringFieldValue(int rowIndex, int fieldId) {
        int recordOffset = this.myHeader.getRecordLength() * rowIndex + this.myHeader.getHeaderLength() + 1;
        int fieldOffset = 0;
        int i = 0;
        while (i < fieldId) {
            fieldOffset += this.myHeader.getFieldLength(i);
            ++i;
        }
        this.buffer.position(recordOffset + fieldOffset);
        byte[] data = new byte[this.myHeader.getFieldLength(fieldId)];
        this.buffer.get(data);
        return new String(data);
    }

    public Number getNumberFieldValue(int rowIndex, int fieldId) {
        int recordOffset = this.myHeader.getRecordLength() * rowIndex + this.myHeader.getHeaderLength() + 1;
        int fieldOffset = 0;
        int i = 0;
        while (i < fieldId) {
            fieldOffset += this.myHeader.getFieldLength(i);
            ++i;
        }
        this.buffer.position(recordOffset + fieldOffset);
        byte[] data = new byte[this.myHeader.getFieldLength(fieldId)];
        this.buffer.get(data);
        String s = new String(data);
        s = s.trim();
        if (this.getFieldType(fieldId) == 'N') {
            Double tempObject = Double.valueOf(s);
            return new Double(((Object)tempObject).toString());
        }
        Integer tempObject = Integer.valueOf(s);
        return new Integer(((Object)tempObject).toString());
    }

    public Object[] getRecord(long inIndex) throws IOException {
        long nRecordOffset = (long)this.myHeader.getRecordLength() * inIndex + (long)this.myHeader.getHeaderLength();
        int tempNumFields = this.myHeader.getNumFields();
        Object[] tempRow = new Object[tempNumFields];
        this.buffer.position((int)nRecordOffset);
        char tempDeleted = (char)this.buffer.get();
        int tempRecordLength = 1;
        int j = 0;
        while (j < tempNumFields) {
            int tempFieldLength = this.myHeader.getFieldLength(j);
            tempRecordLength += tempFieldLength;
            char tempFieldType = this.myHeader.getFieldType(j);
            Object tempObject = null;
            switch (tempFieldType) {
                case 'L': {
                    char tempChar = (char)this.buffer.get();
                    if (tempChar == 'T' || tempChar == 't' || tempChar == 'Y' || tempChar == 'y') {
                        tempObject = new Boolean(true);
                        break;
                    }
                    if (tempChar == ' ') {
                        tempObject = null;
                        break;
                    }
                    tempObject = new Boolean(false);
                    break;
                }
                case 'C': {
                    byte[] sbuffer = new byte[tempFieldLength];
                    this.buffer.get(sbuffer);
                    tempObject = new String(sbuffer, this.stringCharset.name()).trim();
                    break;
                }
                case 'D': {
                    byte[] dbuffer = new byte[8];
                    this.buffer.get(dbuffer);
                    String tempString = new String(dbuffer, 0, 4);
                    try {
                        int tempYear = Integer.parseInt(tempString);
                        tempString = new String(dbuffer, 4, 2);
                        int tempMonth = Integer.parseInt(tempString) - 1;
                        tempString = new String(dbuffer, 6, 2);
                        int tempDay = Integer.parseInt(tempString);
                        Calendar c = Calendar.getInstance();
                        c.set(1, tempYear);
                        c.set(2, tempMonth);
                        c.set(5, tempDay);
                        c.set(10, 0);
                        c.set(12, 0);
                        c.set(13, 0);
                        c.set(14, 0);
                        c.set(9, 0);
                        tempObject = c.getTime();
                    }
                    catch (NumberFormatException tempYear) {}
                    break;
                }
                case 'M': {
                    byte[] mbuffer = new byte[10];
                    this.buffer.get(mbuffer);
                    break;
                }
                case 'F': {
                    String tempString;
                    byte[] fbuffer = new byte[tempFieldLength];
                    this.buffer.get(fbuffer);
                    try {
                        tempString = new String(fbuffer);
                        tempObject = Double.valueOf(tempString.trim());
                    }
                    catch (NumberFormatException tempDay) {}
                    break;
                }
                case 'N': {
                    String tempString;
                    byte[] nbuffer = new byte[tempFieldLength];
                    this.buffer.get(nbuffer);
                    try {
                        tempString = new String(nbuffer);
                        if (this.myHeader.getFieldDecimalCount(j) == 0) {
                            if (this.getFieldLength(j) <= 32) {
                                tempObject = Integer.valueOf(tempString.trim());
                                break;
                            }
                            tempObject = Long.valueOf(tempString.trim());
                            break;
                        }
                        tempObject = Double.valueOf(tempString.trim());
                    }
                    catch (NumberFormatException c) {}
                    break;
                }
                default: {
                    byte[] defbuffer = new byte[tempFieldLength];
                    this.buffer.get(defbuffer);
                    System.out.println("Do not know how to parse Field type " + tempFieldType);
                }
            }
            tempRow[j] = tempObject;
            ++j;
        }
        if (tempRecordLength < this.myHeader.getRecordLength()) {
            byte[] tempbuff = new byte[this.myHeader.getRecordLength() - tempRecordLength];
            this.buffer.get(tempbuff);
        }
        return tempRow;
    }

    public String getFieldName(int inIndex) {
        return this.myHeader.getFieldName(inIndex).trim();
    }

    public int getFieldIndex(String name) {
        return this.myHeader.getFieldIndex(name);
    }

    public char getFieldType(int inIndex) {
        return this.myHeader.getFieldType(inIndex);
    }

    public String getRealFieldType(int inIndex) {
        return this.myHeader.getRealFieldType(inIndex);
    }

    public int getFieldLength(int inIndex) {
        return this.myHeader.getFieldLength(inIndex);
    }

    public int getFieldDecimalLength(int inIndex) {
        return this.myHeader.getFieldDecimalCount(inIndex);
    }

    public void open() throws IOException {
        this.fin = new FileInputStream(this.file);
        this.channel = this.fin.getChannel();
        this.buffer = new BigByteBuffer(this.channel, FileChannel.MapMode.READ_ONLY);
        this.myHeader = new DbaseFileHeaderNIO();
        this.myHeader.readHeader(this.buffer);
    }

    public void close() throws Exception {
        this.close(false);
    }

    public void close(boolean makeGC) throws Exception {
        if (this.fin != null) {
            this.fin.close();
        }
        if (this.channel != null) {
            this.channel.close();
        }
        if (this.buffer != null) {
            this.buffer.terminate();
            this.buffer = null;
            if (makeGC) {
                System.gc();
            }
        }
    }

    public void setFile(File file) {
        this.file = file;
    }
}

