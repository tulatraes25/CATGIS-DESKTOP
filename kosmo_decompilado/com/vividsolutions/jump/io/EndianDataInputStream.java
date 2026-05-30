/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndianDataInputStream {
    private DataInputStream inputStream;
    private byte[] workSpace = new byte[8];

    public EndianDataInputStream(InputStream in) {
        this.inputStream = new DataInputStream(new BufferedInputStream(in));
    }

    public void close() throws IOException {
        this.inputStream.close();
    }

    public byte readByteBE() throws IOException {
        return this.inputStream.readByte();
    }

    public byte readByteLE() throws IOException {
        return this.inputStream.readByte();
    }

    public void readByteLEnum(byte[] b) throws IOException {
        this.inputStream.readFully(b);
    }

    public int readUnsignedByteBE() throws IOException {
        return this.inputStream.readUnsignedByte();
    }

    public int readUnsignedByteLE() throws IOException {
        return this.inputStream.readUnsignedByte();
    }

    public short readShortBE() throws IOException {
        return this.inputStream.readShort();
    }

    public short readShortLE() throws IOException {
        this.inputStream.readFully(this.workSpace, 0, 2);
        return (short)((this.workSpace[1] & 0xFF) << 8 | this.workSpace[0] & 0xFF);
    }

    public int readIntBE() throws IOException {
        return this.inputStream.readInt();
    }

    public int readIntLE() throws IOException {
        this.inputStream.readFully(this.workSpace, 0, 4);
        return (this.workSpace[3] & 0xFF) << 24 | (this.workSpace[2] & 0xFF) << 16 | (this.workSpace[1] & 0xFF) << 8 | this.workSpace[0] & 0xFF;
    }

    public long readLongBE() throws IOException {
        return this.inputStream.readLong();
    }

    public long readLongLE() throws IOException {
        this.inputStream.readFully(this.workSpace, 0, 8);
        return (long)(this.workSpace[7] & 0xFF) << 56 | (long)(this.workSpace[6] & 0xFF) << 48 | (long)(this.workSpace[5] & 0xFF) << 40 | (long)(this.workSpace[4] & 0xFF) << 32 | (long)(this.workSpace[3] & 0xFF) << 24 | (long)(this.workSpace[2] & 0xFF) << 16 | (long)(this.workSpace[1] & 0xFF) << 8 | (long)(this.workSpace[0] & 0xFF);
    }

    public double readDoubleBE() throws IOException {
        return this.inputStream.readDouble();
    }

    public double readDoubleLE() throws IOException {
        this.inputStream.readFully(this.workSpace, 0, 8);
        long l = (long)(this.workSpace[7] & 0xFF) << 56 | (long)(this.workSpace[6] & 0xFF) << 48 | (long)(this.workSpace[5] & 0xFF) << 40 | (long)(this.workSpace[4] & 0xFF) << 32 | (long)(this.workSpace[3] & 0xFF) << 24 | (long)(this.workSpace[2] & 0xFF) << 16 | (long)(this.workSpace[1] & 0xFF) << 8 | (long)(this.workSpace[0] & 0xFF);
        return Double.longBitsToDouble(l);
    }

    public int skipBytes(int num) throws IOException {
        return this.inputStream.skipBytes(num);
    }
}

