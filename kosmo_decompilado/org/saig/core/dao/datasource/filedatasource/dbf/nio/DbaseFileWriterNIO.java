/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hardcode.gdbms.engine.values.BooleanValue
 *  com.hardcode.gdbms.engine.values.DateValue
 *  com.hardcode.gdbms.engine.values.DoubleValue
 *  com.hardcode.gdbms.engine.values.NullValue
 *  com.hardcode.gdbms.engine.values.StringValue
 */
package org.saig.core.dao.datasource.filedatasource.dbf.nio;

import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.StringValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileHeaderNIO;

public class DbaseFileWriterNIO {
    private DbaseFileHeaderNIO header;
    private FieldFormatter formatter = new FieldFormatter();
    WritableByteChannel channel;
    private ByteBuffer buffer;
    private final Number NULL_NUMBER = new Integer(0);
    private final String NULL_STRING = "";
    private final String NULL_DATE = "        ";

    public DbaseFileWriterNIO(DbaseFileHeaderNIO header, WritableByteChannel out) throws IOException {
        header.writeHeader(out);
        this.header = header;
        this.channel = out;
        this.init();
    }

    private void init() throws IOException {
        this.buffer = ByteBuffer.allocateDirect(this.header.getRecordLength());
    }

    private void write() throws IOException {
        this.buffer.position(0);
        int r = this.buffer.remaining();
        while ((r -= this.channel.write(this.buffer)) > 0) {
        }
    }

    public void write(Object[] record) throws IOException {
        this.buffer.position(0);
        this.buffer.put((byte)32);
        int i = 0;
        while (i < this.header.getNumFields()) {
            String fieldString = this.fieldString(record[i], i);
            this.buffer.put(fieldString.getBytes());
            ++i;
        }
        this.write();
    }

    private String fieldString(Object obj, int col) {
        String o;
        int fieldLen = this.header.getFieldLength(col);
        switch (this.header.getFieldType(col)) {
            case 'C': 
            case 'c': {
                o = this.formatter.getFieldString(fieldLen, obj instanceof NullValue ? "" : ((StringValue)obj).getValue());
                break;
            }
            case 'L': 
            case 'l': {
                o = obj instanceof NullValue ? "F" : (((BooleanValue)obj).getValue() ? "T" : "F");
                break;
            }
            case 'G': 
            case 'M': {
                o = this.formatter.getFieldString(fieldLen, obj instanceof NullValue ? "" : ((StringValue)obj).getValue());
                break;
            }
            case 'F': 
            case 'N': 
            case 'f': 
            case 'n': {
                o = this.formatter.getFieldString(fieldLen, this.header.getFieldDecimalCount(col), obj instanceof NullValue ? (Number)this.NULL_NUMBER : (Number)new Double(((DoubleValue)obj).getValue()));
                break;
            }
            case 'D': 
            case 'd': {
                if (obj instanceof NullValue) {
                    o = "        ";
                    break;
                }
                o = this.formatter.getFieldString(((DateValue)obj).getValue());
                break;
            }
            default: {
                throw new RuntimeException("Unknown type " + this.header.getFieldType(col));
            }
        }
        return o;
    }

    public void close() throws IOException {
        this.channel.close();
        boolean cfr_ignored_0 = this.buffer instanceof MappedByteBuffer;
        this.buffer = null;
        this.channel = null;
        this.formatter = null;
    }

    public static class FieldFormatter {
        private StringBuffer buffer = new StringBuffer(255);
        private NumberFormat numFormat = NumberFormat.getNumberInstance(Locale.US);
        private Calendar calendar = Calendar.getInstance(Locale.US);
        private String emtpyString;
        private static final int MAXCHARS = 255;

        public FieldFormatter() {
            this.numFormat.setGroupingUsed(false);
            StringBuffer sb = new StringBuffer(255);
            sb.setLength(255);
            int i = 0;
            while (i < 255) {
                sb.setCharAt(i, ' ');
                ++i;
            }
            this.emtpyString = sb.toString();
        }

        public String getFieldString(int size, String s) {
            this.buffer.replace(0, size, this.emtpyString);
            this.buffer.setLength(size);
            if (s != null) {
                this.buffer.replace(0, size, s);
                if (s.length() <= size) {
                    int i = s.length();
                    while (i < size) {
                        this.buffer.append(' ');
                        ++i;
                    }
                }
            }
            this.buffer.setLength(size);
            return this.buffer.toString();
        }

        public String getFieldString(Date d) {
            if (d != null) {
                this.buffer.delete(0, this.buffer.length());
                this.calendar.setTime(d);
                int year = this.calendar.get(1);
                int month = this.calendar.get(2) + 1;
                int day = this.calendar.get(5);
                if (year < 1000) {
                    if (year >= 100) {
                        this.buffer.append("0");
                    } else if (year >= 10) {
                        this.buffer.append("00");
                    } else {
                        this.buffer.append("000");
                    }
                }
                this.buffer.append(year);
                if (month < 10) {
                    this.buffer.append("0");
                }
                this.buffer.append(month);
                if (day < 10) {
                    this.buffer.append("0");
                }
                this.buffer.append(day);
            } else {
                this.buffer.setLength(8);
                this.buffer.replace(0, 8, this.emtpyString);
            }
            this.buffer.setLength(8);
            return this.buffer.toString();
        }

        public String getFieldString(int size, int decimalPlaces, Number n) {
            int diff;
            this.buffer.delete(0, this.buffer.length());
            if (n != null) {
                this.numFormat.setMaximumFractionDigits(decimalPlaces);
                this.numFormat.setMinimumFractionDigits(decimalPlaces);
                this.numFormat.format(n, this.buffer, new FieldPosition(0));
            }
            if ((diff = size - this.buffer.length()) >= 0) {
                while (diff-- > 0) {
                    this.buffer.insert(0, ' ');
                }
            } else {
                this.buffer.setLength(size);
            }
            return this.buffer.toString();
        }
    }
}

