/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

public interface nsICharsetDetector {
    public void Init(nsICharsetDetectionObserver var1);

    public boolean DoIt(byte[] var1, int var2, boolean var3);

    public void Done();
}

