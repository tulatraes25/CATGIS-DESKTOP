/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.Big5Statistics;
import org.mozilla.intl.chardet.EUCJPStatistics;
import org.mozilla.intl.chardet.EUCKRStatistics;
import org.mozilla.intl.chardet.EUCTWStatistics;
import org.mozilla.intl.chardet.GB2312Statistics;
import org.mozilla.intl.chardet.nsBIG5Verifier;
import org.mozilla.intl.chardet.nsCP1252Verifier;
import org.mozilla.intl.chardet.nsEUCJPVerifier;
import org.mozilla.intl.chardet.nsEUCKRVerifier;
import org.mozilla.intl.chardet.nsEUCSampler;
import org.mozilla.intl.chardet.nsEUCStatistics;
import org.mozilla.intl.chardet.nsEUCTWVerifier;
import org.mozilla.intl.chardet.nsGB18030Verifier;
import org.mozilla.intl.chardet.nsGB2312Verifier;
import org.mozilla.intl.chardet.nsHZVerifier;
import org.mozilla.intl.chardet.nsISO2022CNVerifier;
import org.mozilla.intl.chardet.nsISO2022JPVerifier;
import org.mozilla.intl.chardet.nsISO2022KRVerifier;
import org.mozilla.intl.chardet.nsSJISVerifier;
import org.mozilla.intl.chardet.nsUCS2BEVerifier;
import org.mozilla.intl.chardet.nsUCS2LEVerifier;
import org.mozilla.intl.chardet.nsUTF8Verifier;
import org.mozilla.intl.chardet.nsVerifier;

public abstract class nsPSMDetector {
    public static final int ALL = 0;
    public static final int JAPANESE = 1;
    public static final int CHINESE = 2;
    public static final int SIMPLIFIED_CHINESE = 3;
    public static final int TRADITIONAL_CHINESE = 4;
    public static final int KOREAN = 5;
    public static final int NO_OF_LANGUAGES = 6;
    public static final int MAX_VERIFIERS = 16;
    nsVerifier[] mVerifier;
    nsEUCStatistics[] mStatisticsData;
    nsEUCSampler mSampler = new nsEUCSampler();
    byte[] mState = new byte[16];
    int[] mItemIdx = new int[16];
    int mItems;
    int mClassItems;
    boolean mDone;
    boolean mRunSampler;
    boolean mClassRunSampler;

    public nsPSMDetector() {
        this.initVerifiers(0);
        this.Reset();
    }

    public nsPSMDetector(int langFlag) {
        this.initVerifiers(langFlag);
        this.Reset();
    }

    public nsPSMDetector(int aItems, nsVerifier[] aVerifierSet, nsEUCStatistics[] aStatisticsSet) {
        this.mClassRunSampler = aStatisticsSet != null;
        this.mStatisticsData = aStatisticsSet;
        this.mVerifier = aVerifierSet;
        this.mClassItems = aItems;
        this.Reset();
    }

    public void Reset() {
        this.mRunSampler = this.mClassRunSampler;
        this.mDone = false;
        this.mItems = this.mClassItems;
        int i = 0;
        while (i < this.mItems) {
            this.mState[i] = 0;
            this.mItemIdx[i] = i;
            ++i;
        }
        this.mSampler.Reset();
    }

    protected void initVerifiers(int currVerSet) {
        boolean idx = false;
        int currVerifierSet = currVerSet >= 0 && currVerSet < 6 ? currVerSet : 0;
        this.mVerifier = null;
        this.mStatisticsData = null;
        if (currVerifierSet == 4) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsBIG5Verifier(), new nsISO2022CNVerifier(), new nsEUCTWVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
            nsEUCStatistics[] nsEUCStatisticsArray = new nsEUCStatistics[7];
            nsEUCStatisticsArray[1] = new Big5Statistics();
            nsEUCStatisticsArray[3] = new EUCTWStatistics();
            this.mStatisticsData = nsEUCStatisticsArray;
        } else if (currVerifierSet == 5) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsEUCKRVerifier(), new nsISO2022KRVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
        } else if (currVerifierSet == 3) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsGB2312Verifier(), new nsGB18030Verifier(), new nsISO2022CNVerifier(), new nsHZVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
        } else if (currVerifierSet == 1) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsSJISVerifier(), new nsEUCJPVerifier(), new nsISO2022JPVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
        } else if (currVerifierSet == 2) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsGB2312Verifier(), new nsGB18030Verifier(), new nsBIG5Verifier(), new nsISO2022CNVerifier(), new nsHZVerifier(), new nsEUCTWVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
            nsEUCStatistics[] nsEUCStatisticsArray = new nsEUCStatistics[10];
            nsEUCStatisticsArray[1] = new GB2312Statistics();
            nsEUCStatisticsArray[3] = new Big5Statistics();
            nsEUCStatisticsArray[6] = new EUCTWStatistics();
            this.mStatisticsData = nsEUCStatisticsArray;
        } else if (currVerifierSet == 0) {
            this.mVerifier = new nsVerifier[]{new nsUTF8Verifier(), new nsSJISVerifier(), new nsEUCJPVerifier(), new nsISO2022JPVerifier(), new nsEUCKRVerifier(), new nsISO2022KRVerifier(), new nsBIG5Verifier(), new nsEUCTWVerifier(), new nsGB2312Verifier(), new nsGB18030Verifier(), new nsISO2022CNVerifier(), new nsHZVerifier(), new nsCP1252Verifier(), new nsUCS2BEVerifier(), new nsUCS2LEVerifier()};
            nsEUCStatistics[] nsEUCStatisticsArray = new nsEUCStatistics[15];
            nsEUCStatisticsArray[2] = new EUCJPStatistics();
            nsEUCStatisticsArray[4] = new EUCKRStatistics();
            nsEUCStatisticsArray[6] = new Big5Statistics();
            nsEUCStatisticsArray[7] = new EUCTWStatistics();
            nsEUCStatisticsArray[8] = new GB2312Statistics();
            this.mStatisticsData = nsEUCStatisticsArray;
        }
        this.mClassRunSampler = this.mStatisticsData != null;
        this.mClassItems = this.mVerifier.length;
    }

    public abstract void Report(String var1);

    public boolean HandleData(byte[] aBuf, int len) {
        int i = 0;
        while (i < len) {
            byte b = aBuf[i];
            int j = 0;
            while (j < this.mItems) {
                byte st = nsVerifier.getNextState(this.mVerifier[this.mItemIdx[j]], b, this.mState[j]);
                if (st == 2) {
                    this.Report(this.mVerifier[this.mItemIdx[j]].charset());
                    this.mDone = true;
                    return this.mDone;
                }
                if (st == 1) {
                    --this.mItems;
                    if (j >= this.mItems) continue;
                    this.mItemIdx[j] = this.mItemIdx[this.mItems];
                    this.mState[j] = this.mState[this.mItems];
                    continue;
                }
                this.mState[j++] = st;
            }
            if (this.mItems <= 1) {
                if (1 == this.mItems) {
                    this.Report(this.mVerifier[this.mItemIdx[0]].charset());
                }
                this.mDone = true;
                return this.mDone;
            }
            int nonUCS2Num = 0;
            int nonUCS2Idx = 0;
            j = 0;
            while (j < this.mItems) {
                if (!this.mVerifier[this.mItemIdx[j]].isUCS2() && !this.mVerifier[this.mItemIdx[j]].isUCS2()) {
                    ++nonUCS2Num;
                    nonUCS2Idx = j;
                }
                ++j;
            }
            if (1 == nonUCS2Num) {
                this.Report(this.mVerifier[this.mItemIdx[nonUCS2Idx]].charset());
                this.mDone = true;
                return this.mDone;
            }
            ++i;
        }
        if (this.mRunSampler) {
            this.Sample(aBuf, len);
        }
        return this.mDone;
    }

    public void DataEnd() {
        if (this.mDone) {
            return;
        }
        if (this.mItems == 2) {
            if (this.mVerifier[this.mItemIdx[0]].charset().equals("GB18030")) {
                this.Report(this.mVerifier[this.mItemIdx[1]].charset());
                this.mDone = true;
            } else if (this.mVerifier[this.mItemIdx[1]].charset().equals("GB18030")) {
                this.Report(this.mVerifier[this.mItemIdx[0]].charset());
                this.mDone = true;
            }
        }
        if (this.mRunSampler) {
            this.Sample(null, 0, true);
        }
    }

    public void Sample(byte[] aBuf, int aLen) {
        this.Sample(aBuf, aLen, false);
    }

    public void Sample(byte[] aBuf, int aLen, boolean aLastChance) {
        int possibleCandidateNum = 0;
        int eucNum = 0;
        int j = 0;
        while (j < this.mItems) {
            if (this.mStatisticsData[this.mItemIdx[j]] != null) {
                ++eucNum;
            }
            if (!this.mVerifier[this.mItemIdx[j]].isUCS2() && !this.mVerifier[this.mItemIdx[j]].charset().equals("GB18030")) {
                ++possibleCandidateNum;
            }
            ++j;
        }
        boolean bl = this.mRunSampler = eucNum > 1;
        if (this.mRunSampler) {
            this.mRunSampler = this.mSampler.Sample(aBuf, aLen);
            if ((aLastChance && this.mSampler.GetSomeData() || this.mSampler.EnoughData()) && eucNum == possibleCandidateNum) {
                this.mSampler.CalFreq();
                int bestIdx = -1;
                int eucCnt = 0;
                float bestScore = 0.0f;
                j = 0;
                while (j < this.mItems) {
                    if (this.mStatisticsData[this.mItemIdx[j]] != null && !this.mVerifier[this.mItemIdx[j]].charset().equals("Big5")) {
                        float score = this.mSampler.GetScore(this.mStatisticsData[this.mItemIdx[j]].mFirstByteFreq(), this.mStatisticsData[this.mItemIdx[j]].mFirstByteWeight(), this.mStatisticsData[this.mItemIdx[j]].mSecondByteFreq(), this.mStatisticsData[this.mItemIdx[j]].mSecondByteWeight());
                        if (eucCnt++ == 0 || bestScore > score) {
                            bestScore = score;
                            bestIdx = j;
                        }
                    }
                    ++j;
                }
                if (bestIdx >= 0) {
                    this.Report(this.mVerifier[this.mItemIdx[bestIdx]].charset());
                    this.mDone = true;
                }
            }
        }
    }

    public String[] getProbableCharsets() {
        if (this.mItems <= 0) {
            String[] nomatch = new String[]{"nomatch"};
            return nomatch;
        }
        String[] ret = new String[this.mItems];
        int i = 0;
        while (i < this.mItems) {
            ret[i] = this.mVerifier[this.mItemIdx[i]].charset();
            ++i;
        }
        return ret;
    }
}

