/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsICharsetDetector;
import org.mozilla.intl.chardet.nsPSMDetector;

public class nsDetector
extends nsPSMDetector
implements nsICharsetDetector {
    nsICharsetDetectionObserver mObserver = null;

    public nsDetector() {
    }

    public nsDetector(int langFlag) {
        super(langFlag);
    }

    @Override
    public void Init(nsICharsetDetectionObserver aObserver) {
        this.mObserver = aObserver;
    }

    @Override
    public boolean DoIt(byte[] aBuf, int aLen, boolean oDontFeedMe) {
        if (aBuf == null || oDontFeedMe) {
            return false;
        }
        this.HandleData(aBuf, aLen);
        return this.mDone;
    }

    @Override
    public void Done() {
        this.DataEnd();
    }

    @Override
    public void Report(String charset) {
        if (this.mObserver != null) {
            this.mObserver.Notify(charset);
        }
    }

    public boolean isAscii(byte[] aBuf, int aLen) {
        int i = 0;
        while (i < aLen) {
            if ((0x80 & aBuf[i]) != 0) {
                return false;
            }
            ++i;
        }
        return true;
    }
}

