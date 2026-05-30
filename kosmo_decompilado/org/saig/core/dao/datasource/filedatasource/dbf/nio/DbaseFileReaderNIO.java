/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.NIOUtilities
 *  org.geotools.resources.NumberParser
 */
package org.saig.core.dao.datasource.filedatasource.dbf.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Calendar;
import org.geotools.resources.NIOUtilities;
import org.geotools.resources.NumberParser;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileHeaderNIO;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileWriterNIO;

public class DbaseFileReaderNIO {
    DbaseFileHeaderNIO header;
    ByteBuffer buffer;
    ReadableByteChannel channel;
    CharBuffer charBuffer;
    CharsetDecoder decoder;
    char[] fieldTypes;
    int[] fieldLengths;
    int cnt = 1;
    Row row;
    NumberParser numberParser = new NumberParser();
    private Charset charset;
    private Charset stringCharset;

    public DbaseFileReaderNIO(ReadableByteChannel channel, Charset charset) throws IOException {
        this.channel = channel;
        this.header = new DbaseFileHeaderNIO();
        this.init(charset);
    }

    private int fill(ByteBuffer buffer, ReadableByteChannel channel) throws IOException {
        int r = buffer.remaining();
        while (buffer.remaining() > 0 && r != -1) {
            r = channel.read(buffer);
        }
        if (r == -1) {
            buffer.limit(buffer.position());
        }
        return r;
    }

    private void bufferCheck() throws IOException {
        if (!this.buffer.isReadOnly() && this.buffer.remaining() < this.header.getRecordLength()) {
            this.buffer.compact();
            this.fill(this.buffer, this.channel);
            this.buffer.position(0);
        }
    }

    private int getOffset(int column) {
        int offset = 0;
        int i = 0;
        int ii = column;
        while (i < ii) {
            offset += this.fieldLengths[i];
            ++i;
        }
        return offset;
    }

    private void init(Charset charset) throws IOException {
        this.stringCharset = charset;
        this.charset = Charset.forName("ISO-8859-1");
        if (this.channel instanceof FileChannel) {
            FileChannel fc = (FileChannel)this.channel;
            this.buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
            this.buffer.position((int)fc.position());
        } else {
            int size = 8192;
            size = this.header.getRecordLength() > size ? this.header.getRecordLength() : size;
            this.buffer = ByteBuffer.allocateDirect(size);
            this.fill(this.buffer, this.channel);
            this.buffer.flip();
        }
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.fieldTypes = new char[this.header.getNumFields()];
        this.fieldLengths = new int[this.header.getNumFields()];
        int i = 0;
        int ii = this.header.getNumFields();
        while (i < ii) {
            this.fieldTypes[i] = this.header.getFieldType(i);
            this.fieldLengths[i] = this.header.getFieldLength(i);
            ++i;
        }
        this.charBuffer = CharBuffer.allocate(this.header.getRecordLength() - 1);
        Charset chars = Charset.forName("ISO-8859-1");
        this.decoder = chars.newDecoder();
        this.row = new Row();
    }

    public DbaseFileHeaderNIO getHeader() {
        return this.header;
    }

    public void close() throws IOException {
        if (this.channel.isOpen()) {
            this.channel.close();
        }
        if (this.buffer instanceof MappedByteBuffer) {
            NIOUtilities.clean((ByteBuffer)this.buffer);
        }
        this.buffer = null;
        this.channel = null;
        this.charBuffer = null;
        this.decoder = null;
        this.header = null;
        this.row = null;
    }

    public boolean hasNext() {
        return this.cnt < this.header.getNumRecords() + 1;
    }

    public Object[] readEntry() throws IOException {
        return this.readEntry(new Object[this.header.getNumFields()]);
    }

    public Row readRow() throws IOException {
        this.read();
        return this.row;
    }

    public void skip() throws IOException {
        boolean foundRecord = false;
        while (!foundRecord) {
            this.bufferCheck();
            char tempDeleted = (char)this.buffer.get();
            this.buffer.position(this.buffer.position() + this.header.getRecordLength() - 1);
            if (tempDeleted == '*') continue;
            foundRecord = true;
        }
    }

    public Object[] readEntry(Object[] entry, int offset) throws IOException {
        if (entry.length - offset < this.header.getNumFields()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.read();
        int numFields = this.header.getNumFields();
        int fieldOffset = 0;
        int j = 0;
        while (j < numFields) {
            entry[j + offset] = this.readObject(fieldOffset, j);
            fieldOffset += this.fieldLengths[j];
            ++j;
        }
        return entry;
    }

    public void transferTo(DbaseFileWriterNIO writer) throws IOException {
        this.bufferCheck();
        this.buffer.limit(this.buffer.position() + this.header.getRecordLength());
        writer.channel.write(this.buffer);
        this.buffer.limit(this.buffer.capacity());
        ++this.cnt;
    }

    private void read() throws IOException {
        boolean foundRecord = false;
        while (!foundRecord) {
            this.bufferCheck();
            char deleted = (char)this.buffer.get();
            if (deleted == '*') continue;
            this.charBuffer.position(0);
            this.buffer.limit(this.buffer.position() + this.header.getRecordLength() - 1);
            this.decoder.decode(this.buffer, this.charBuffer, true);
            this.buffer.limit(this.buffer.capacity());
            this.charBuffer.flip();
            foundRecord = true;
        }
        ++this.cnt;
    }

    public Object[] readEntry(Object[] entry) throws IOException {
        return this.readEntry(entry, 0);
    }

    private Object readObject(int fieldOffset, int fieldNum) throws IOException {
        char type = this.fieldTypes[fieldNum];
        int fieldLen = this.fieldLengths[fieldNum];
        Object object = null;
        if (fieldLen > 0) {
            block4 : switch (type) {
                case 'L': 
                case 'l': {
                    switch (this.charBuffer.charAt(fieldOffset)) {
                        case 'T': 
                        case 'Y': 
                        case 't': 
                        case 'y': {
                            object = Boolean.TRUE;
                            break block4;
                        }
                        case 'F': 
                        case 'N': 
                        case 'f': 
                        case 'n': {
                            object = Boolean.FALSE;
                            break block4;
                        }
                    }
                    throw new IOException("Unknown logical value : '" + this.charBuffer.charAt(fieldOffset) + "'");
                }
                case 'C': 
                case 'c': {
                    char c;
                    int start = fieldOffset;
                    int end = fieldOffset + fieldLen - 1;
                    while (start < end) {
                        c = this.charBuffer.get(start);
                        if (c != '\u0000' && !Character.isWhitespace(c)) break;
                        ++start;
                    }
                    while (end > start) {
                        c = this.charBuffer.get(end);
                        if (c != '\u0000' && !Character.isWhitespace(c)) break;
                        --end;
                    }
                    this.charBuffer.position(start).limit(end + 1);
                    String s = this.charBuffer.toString();
                    if (!this.stringCharset.name().equals("ISO-8859-1")) {
                        s = new String(s.getBytes("ISO-8859-1"), this.stringCharset.name());
                    }
                    this.charBuffer.clear();
                    object = s;
                    break;
                }
                case 'D': 
                case 'd': {
                    try {
                        String tempString = this.charBuffer.subSequence(fieldOffset, fieldOffset + 4).toString();
                        int tempYear = Integer.parseInt(tempString);
                        tempString = this.charBuffer.subSequence(fieldOffset + 4, fieldOffset + 6).toString();
                        int tempMonth = Integer.parseInt(tempString) - 1;
                        tempString = this.charBuffer.subSequence(fieldOffset + 6, fieldOffset + 8).toString();
                        int tempDay = Integer.parseInt(tempString);
                        Calendar cal = Calendar.getInstance();
                        cal.clear();
                        cal.set(1, tempYear);
                        cal.set(2, tempMonth);
                        cal.set(5, tempDay);
                        object = cal.getTime();
                    }
                    catch (NumberFormatException tempString) {}
                    break;
                }
                case 'N': 
                case 'n': {
                    try {
                        if (this.header.getFieldDecimalCount(fieldNum) == 0) {
                            if (this.header.getFieldLength(fieldNum) <= 32) {
                                object = new Integer(this.numberParser.parseInt((CharSequence)this.charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
                                break;
                            }
                            object = new Long(this.numberParser.parseInt((CharSequence)this.charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
                            break;
                        }
                    }
                    catch (NumberFormatException e) {
                        try {
                            object = new Long(this.numberParser.parseLong((CharSequence)this.charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
                            break;
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                    }
                }
                case 'F': 
                case 'f': {
                    try {
                        object = new Double(this.numberParser.parseDouble((CharSequence)this.charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
                    }
                    catch (NumberFormatException e) {
                        object = new Double(0.0);
                    }
                    break;
                }
                default: {
                    throw new IOException("Invalid field type : " + type);
                }
            }
        }
        return object;
    }

    public final class Row {
        public Object read(int column) throws IOException {
            int offset = DbaseFileReaderNIO.this.getOffset(column);
            return DbaseFileReaderNIO.this.readObject(offset, column);
        }
    }
}

