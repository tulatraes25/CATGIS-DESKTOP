/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Level
 *  org.apache.log4j.Logger
 *  org.apache.log4j.Priority
 */
package org.saig.core.util;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class LoggingOutputStream
extends OutputStream {
    protected static final String LINE_SEPERATOR = System.getProperty("line.separator");
    protected boolean hasBeenClosed = false;
    protected byte[] buf;
    protected int count;
    private int bufLength;
    public static final int DEFAULT_BUFFER_LENGTH = 2048;
    protected Logger logger;
    protected Level level;

    public LoggingOutputStream(Logger log, Level lvl) throws IllegalArgumentException {
        if (log == null) {
            throw new IllegalArgumentException("log == null");
        }
        if (lvl == null) {
            throw new IllegalArgumentException("lvl == null");
        }
        this.level = lvl;
        this.logger = log;
        this.bufLength = 2048;
        this.buf = new byte[2048];
        this.count = 0;
    }

    @Override
    public void close() {
        this.flush();
        this.hasBeenClosed = true;
    }

    @Override
    public void write(int b) throws IOException {
        if (this.hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }
        if (b == 0) {
            return;
        }
        if (this.count == this.bufLength) {
            int newBufLength = this.bufLength + 2048;
            byte[] newBuf = new byte[newBufLength];
            System.arraycopy(this.buf, 0, newBuf, 0, this.bufLength);
            this.buf = newBuf;
            this.bufLength = newBufLength;
        }
        this.buf[this.count] = (byte)b;
        ++this.count;
    }

    @Override
    public void flush() {
        if (this.count == 0) {
            return;
        }
        if (this.count == LINE_SEPERATOR.length() && (char)this.buf[0] == LINE_SEPERATOR.charAt(0) && (this.count == 1 || this.count == 2 && (char)this.buf[1] == LINE_SEPERATOR.charAt(1))) {
            this.reset();
            return;
        }
        byte[] theBytes = new byte[this.count];
        System.arraycopy(this.buf, 0, theBytes, 0, this.count);
        this.logger.log((Priority)this.level, (Object)new String(theBytes));
        this.reset();
    }

    private void reset() {
        this.count = 0;
    }
}

