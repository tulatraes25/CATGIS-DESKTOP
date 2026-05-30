/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

public class nsEUCSampler {
    int mTotal = 0;
    int mThreshold = 200;
    int mState = 0;
    public int[] mFirstByteCnt = new int[94];
    public int[] mSecondByteCnt = new int[94];
    public float[] mFirstByteFreq = new float[94];
    public float[] mSecondByteFreq = new float[94];

    public nsEUCSampler() {
        this.Reset();
    }

    public void Reset() {
        this.mTotal = 0;
        this.mState = 0;
        int i = 0;
        while (i < 94) {
            this.mSecondByteCnt[i] = 0;
            this.mFirstByteCnt[i] = 0;
            ++i;
        }
    }

    boolean EnoughData() {
        return this.mTotal > this.mThreshold;
    }

    boolean GetSomeData() {
        return this.mTotal > 1;
    }

    boolean Sample(byte[] aIn, int aLen) {
        if (this.mState == 1) {
            return false;
        }
        int p = 0;
        int i = 0;
        while (i < aLen && 1 != this.mState) {
            switch (this.mState) {
                case 0: {
                    if ((aIn[p] & 0x80) == 0) break;
                    if (255 == (0xFF & aIn[p]) || 161 > (0xFF & aIn[p])) {
                        this.mState = 1;
                        break;
                    }
                    ++this.mTotal;
                    int n = (0xFF & aIn[p]) - 161;
                    this.mFirstByteCnt[n] = this.mFirstByteCnt[n] + 1;
                    this.mState = 2;
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    if ((aIn[p] & 0x80) != 0) {
                        if (255 == (0xFF & aIn[p]) || 161 > (0xFF & aIn[p])) {
                            this.mState = 1;
                            break;
                        }
                        ++this.mTotal;
                        int n = (0xFF & aIn[p]) - 161;
                        this.mSecondByteCnt[n] = this.mSecondByteCnt[n] + 1;
                        this.mState = 0;
                        break;
                    }
                    this.mState = 1;
                    break;
                }
                default: {
                    this.mState = 1;
                }
            }
            ++i;
            ++p;
        }
        return 1 != this.mState;
    }

    void CalFreq() {
        int i = 0;
        while (i < 94) {
            this.mFirstByteFreq[i] = (float)this.mFirstByteCnt[i] / (float)this.mTotal;
            this.mSecondByteFreq[i] = (float)this.mSecondByteCnt[i] / (float)this.mTotal;
            ++i;
        }
    }

    float GetScore(float[] aFirstByteFreq, float aFirstByteWeight, float[] aSecondByteFreq, float aSecondByteWeight) {
        return aFirstByteWeight * this.GetScore(aFirstByteFreq, this.mFirstByteFreq) + aSecondByteWeight * this.GetScore(aSecondByteFreq, this.mSecondByteFreq);
    }

    float GetScore(float[] array1, float[] array2) {
        float sum = 0.0f;
        int i = 0;
        while (i < 94) {
            float s = array1[i] - array2[i];
            sum += s * s;
            ++i;
        }
        return (float)Math.sqrt(sum) / 94.0f;
    }
}

