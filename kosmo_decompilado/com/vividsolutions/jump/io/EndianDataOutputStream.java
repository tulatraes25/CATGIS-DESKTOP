/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EndianDataOutputStream {
    private DataOutputStream outputStream;

    public EndianDataOutputStream(OutputStream out) {
        this.outputStream = new DataOutputStream(out);
    }

    public void close() throws IOException {
        this.outputStream.close();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.outputStream.write(b, off, len);
    }

    public void flush() throws IOException {
        this.outputStream.flush();
    }

    public void writeByteLE(int b) throws IOException {
        this.outputStream.writeByte(b);
    }

    public void writeByteBE(int b) throws IOException {
        this.outputStream.writeByte(b);
    }

    public void writeBytesLE(String s) throws IOException {
        this.outputStream.writeBytes(s);
    }

    public void writeBytesBE(String s) throws IOException {
        this.outputStream.writeBytes(s);
    }

    public void writeShortBE(int s) throws IOException {
        this.outputStream.writeShort(s);
    }

    public void writeShortLE(int s) throws IOException {
        this.outputStream.writeByte(s);
        this.outputStream.writeByte(s >> 8);
    }

    public void writeIntBE(int i) throws IOException {
        this.outputStream.writeInt(i);
    }

    public void writeIntLE(int i) throws IOException {
        this.outputStream.writeByte(i);
        this.outputStream.writeByte(i >> 8);
        this.outputStream.writeByte(i >> 16);
        this.outputStream.writeByte(i >> 24);
    }

    public void writeLongBE(long l) throws IOException {
        this.outputStream.writeLong(l);
    }

    public void writeLongLE(long l) throws IOException {
        this.outputStream.writeByte((byte)l);
        this.outputStream.writeByte((byte)(l >> 8));
        this.outputStream.writeByte((byte)(l >> 16));
        this.outputStream.writeByte((byte)(l >> 24));
        this.outputStream.writeByte((byte)(l >> 32));
        this.outputStream.writeByte((byte)(l >> 40));
        this.outputStream.writeByte((byte)(l >> 48));
        this.outputStream.writeByte((byte)(l >> 56));
    }

    public void writeDoubleBE(double d) throws IOException {
        this.outputStream.writeDouble(d);
    }

    public void writeDoubleLE(double d) throws IOException {
        this.writeLongLE(Double.doubleToLongBits(d));
    }
}

