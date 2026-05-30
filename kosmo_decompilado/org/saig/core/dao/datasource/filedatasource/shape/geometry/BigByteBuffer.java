/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Cleaner;

public class BigByteBuffer {
    private static long DEFAULT_SIZE = 0x3200000L;
    MappedByteBuffer bb;
    FileChannel fc;
    long minAbs;
    long maxAbs;
    long posAbs;
    int minRel;
    int maxRel;
    int posRel;
    long sizeChunk;
    long amountMem;
    long fileSize;
    FileChannel.MapMode mode;

    private void prepareBuffer(long posActual, int numBytesToRead) {
        long desiredPos = posActual + (long)numBytesToRead;
        if (desiredPos > this.maxAbs || posActual < this.minAbs) {
            this.sizeChunk = Math.min(this.fileSize - posActual, this.amountMem);
            try {
                ByteOrder lastOrder = this.bb.order();
                try {
                    this.bb = this.fc.map(this.mode, posActual, this.sizeChunk);
                }
                catch (IOException e) {
                    System.gc();
                    System.runFinalization();
                    this.bb = this.fc.map(this.mode, posActual, this.sizeChunk);
                }
                this.minAbs = posActual;
                this.maxAbs = this.sizeChunk + posActual;
                this.bb.order(lastOrder);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.posAbs = desiredPos;
    }

    public BigByteBuffer(FileChannel fc, FileChannel.MapMode mode, long amountMem) throws IOException {
        this.amountMem = amountMem;
        this.fc = fc;
        this.fileSize = fc.size();
        this.mode = mode;
        this.sizeChunk = Math.min(fc.size(), amountMem);
        try {
            this.bb = fc.map(mode, 0L, this.sizeChunk);
        }
        catch (IOException e) {
            System.gc();
            System.runFinalization();
            this.bb = fc.map(mode, 0L, this.sizeChunk);
        }
        this.minAbs = 0L;
        this.maxAbs = this.sizeChunk;
    }

    public BigByteBuffer(FileChannel fc, FileChannel.MapMode mode) throws IOException {
        this.amountMem = DEFAULT_SIZE;
        this.fc = fc;
        this.fileSize = fc.size();
        this.mode = mode;
        this.sizeChunk = Math.min(fc.size(), this.amountMem);
        try {
            this.bb = fc.map(mode, 0L, this.sizeChunk);
        }
        catch (IOException e) {
            System.gc();
            System.runFinalization();
            this.bb = fc.map(mode, 0L, this.sizeChunk);
        }
        this.minAbs = 0L;
        this.maxAbs = this.sizeChunk;
    }

    public synchronized byte get() {
        this.prepareBuffer(this.posAbs, 1);
        return this.bb.get();
    }

    public synchronized ByteBuffer get(byte[] dst) {
        this.prepareBuffer(this.posAbs, dst.length);
        return this.bb.get(dst);
    }

    public synchronized char getChar() {
        this.prepareBuffer(this.posAbs, 2);
        return this.bb.getChar();
    }

    public synchronized double getDouble() {
        this.prepareBuffer(this.posAbs, 8);
        return this.bb.getDouble();
    }

    public synchronized float getFloat() {
        this.prepareBuffer(this.posAbs, 4);
        return this.bb.getFloat();
    }

    public synchronized int getInt() {
        this.prepareBuffer(this.posAbs, 4);
        return this.bb.getInt();
    }

    public synchronized long getLong() {
        this.prepareBuffer(this.posAbs, 8);
        return this.bb.getLong();
    }

    public synchronized short getShort() {
        this.prepareBuffer(this.posAbs, 2);
        return this.bb.getShort();
    }

    public boolean isDirect() {
        return this.bb.isDirect();
    }

    public synchronized byte get(int index) {
        this.prepareBuffer(index, 1);
        return this.bb.get(index - (int)this.minAbs);
    }

    public synchronized char getChar(int index) {
        this.prepareBuffer(index, 2);
        return this.bb.getChar(index - (int)this.minAbs);
    }

    public synchronized double getDouble(int index) {
        this.prepareBuffer(index, 8);
        return this.bb.getDouble(index - (int)this.minAbs);
    }

    public synchronized float getFloat(int index) {
        this.prepareBuffer(index, 4);
        return this.bb.getFloat(index - (int)this.minAbs);
    }

    public synchronized int getInt(int index) {
        this.prepareBuffer(index, 4);
        return this.bb.getInt(index - (int)this.minAbs);
    }

    public synchronized long getLong(int index) {
        this.prepareBuffer(index, 8);
        return this.bb.getLong(index - (int)this.minAbs);
    }

    public synchronized short getShort(int index) {
        this.prepareBuffer(index, 2);
        return this.bb.getShort(index - (int)this.minAbs);
    }

    public ByteBuffer asReadOnlyBuffer() {
        return this.bb.asReadOnlyBuffer();
    }

    public ByteBuffer compact() {
        return this.bb.compact();
    }

    public ByteBuffer duplicate() {
        return this.bb.duplicate();
    }

    public ByteBuffer slice() {
        return this.bb.slice();
    }

    public synchronized ByteBuffer put(byte b) {
        this.prepareBuffer(this.posAbs, 1);
        return this.bb.put(b);
    }

    public synchronized ByteBuffer putChar(char value) {
        this.prepareBuffer(this.posAbs, 2);
        return this.bb.putChar(value);
    }

    public synchronized ByteBuffer putDouble(double value) {
        this.prepareBuffer(this.posAbs, 8);
        return this.bb.putDouble(value);
    }

    public synchronized ByteBuffer putFloat(float value) {
        this.prepareBuffer(this.posAbs, 4);
        return this.bb.putFloat(value);
    }

    public synchronized ByteBuffer putInt(int value) {
        this.prepareBuffer(this.posAbs, 4);
        return this.bb.putInt(value);
    }

    public synchronized ByteBuffer put(int index, byte b) {
        this.prepareBuffer(index, 1);
        return this.bb.put(index - (int)this.minAbs, b);
    }

    public synchronized ByteBuffer putChar(int index, char value) {
        this.prepareBuffer(index, 2);
        return this.bb.putChar(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putDouble(int index, double value) {
        this.prepareBuffer(index, 8);
        return this.bb.putDouble(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putFloat(int index, float value) {
        this.prepareBuffer(index, 4);
        return this.bb.putFloat(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putInt(int index, int value) {
        this.prepareBuffer(index, 4);
        return this.bb.putInt(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putLong(int index, long value) {
        this.prepareBuffer(index, 8);
        return this.bb.putLong(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putShort(int index, short value) {
        this.prepareBuffer(index, 2);
        return this.bb.putShort(index - (int)this.minAbs, value);
    }

    public synchronized ByteBuffer putLong(long value) {
        this.prepareBuffer(this.posAbs, 8);
        return this.bb.putLong(value);
    }

    public synchronized ByteBuffer putShort(short value) {
        this.prepareBuffer(this.posAbs, 2);
        return this.bb.putShort(value);
    }

    public CharBuffer asCharBuffer() {
        return this.bb.asCharBuffer();
    }

    public DoubleBuffer asDoubleBuffer() {
        return this.bb.asDoubleBuffer();
    }

    public FloatBuffer asFloatBuffer() {
        return this.bb.asFloatBuffer();
    }

    public IntBuffer asIntBuffer() {
        return this.bb.asIntBuffer();
    }

    public LongBuffer asLongBuffer() {
        return this.bb.asLongBuffer();
    }

    public ShortBuffer asShortBuffer() {
        return this.bb.asShortBuffer();
    }

    public boolean isReadOnly() {
        return this.bb.isReadOnly();
    }

    public final ByteOrder order() {
        return this.bb.order();
    }

    public final ByteBuffer order(ByteOrder bo) {
        return this.bb.order(bo);
    }

    public final long position() {
        return this.posAbs;
    }

    public final synchronized Buffer position(long newPosition) {
        this.prepareBuffer(newPosition, 0);
        int relPos = (int)(newPosition - this.minAbs);
        return this.bb.position(relPos);
    }

    public void terminate() throws Exception {
        BigByteBuffer.clean(this.bb);
        this.fc.close();
    }

    public static void clean(final Object buffer) throws Exception {
        AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    Cleaner cleaner = (Cleaner)getCleanerMethod.invoke(buffer, new Object[0]);
                    if (cleaner != null) {
                        cleaner.clean();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}

