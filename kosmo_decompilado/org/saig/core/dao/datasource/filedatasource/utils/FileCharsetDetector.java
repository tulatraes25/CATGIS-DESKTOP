/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.CharsetDetector
 *  com.ibm.icu.text.CharsetMatch
 */
package org.saig.core.dao.datasource.filedatasource.utils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

public class FileCharsetDetector {
    /*
     * Loose catch block
     */
    public static String getCharset(InputStream f) {
        String string;
        BufferedInputStream bis;
        block12: {
            bis = null;
            CharsetDetector cd = new CharsetDetector();
            bis = new BufferedInputStream(f);
            cd.setText((InputStream)bis);
            CharsetMatch match = cd.detect();
            string = match.getName();
            if (bis == null) break block12;
            try {
                bis.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return string;
        catch (IOException e) {
            if (bis != null) {
                try {
                    bis.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            return null;
            catch (Throwable throwable) {
                if (bis != null) {
                    try {
                        bis.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                throw throwable;
            }
        }
    }

    public static String getCharsetByMozillaDetector(InputStream f) {
        String result;
        StandardCharsetObserver scObserver;
        nsDetector det;
        block12: {
            int len;
            det = new nsDetector(0);
            scObserver = new StandardCharsetObserver();
            det.Init(scObserver);
            result = null;
            BufferedInputStream imp = new BufferedInputStream(f);
            byte[] buf = new byte[1024];
            boolean done = false;
            boolean isAscii = true;
            while ((len = imp.read(buf, 0, buf.length)) != -1) {
                if (isAscii) {
                    isAscii = det.isAscii(buf, len);
                }
                if (isAscii || done) continue;
                done = det.DoIt(buf, len, false);
            }
            det.DataEnd();
            if (!isAscii) break block12;
            return "ASCII";
        }
        try {
            if (!scObserver.isFound()) {
                String[] prob = det.getProbableCharsets();
                if (prob.length > 0) {
                    result = prob[0];
                }
                int i = 0;
                while (i < prob.length) {
                    if ("BIG5".equalsIgnoreCase(prob[i])) {
                        return prob[i];
                    }
                    if ("GB2312".equalsIgnoreCase(prob[i])) {
                        return prob[i];
                    }
                    if ("UTF-8".equalsIgnoreCase(prob[i])) {
                        return prob[i];
                    }
                    ++i;
                }
            } else {
                result = scObserver.getCharset();
            }
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] argv) throws Exception {
        String filePath = "C:/Temp/Comprobaci\u00f3n0.dbf";
        System.out.println(FileCharsetDetector.getCharset(new FileInputStream(filePath)));
        System.out.println(FileCharsetDetector.getCharsetByMozillaDetector(new FileInputStream(filePath)));
    }

    private static class StandardCharsetObserver
    implements nsICharsetDetectionObserver {
        private String result = null;
        private boolean found = false;

        private StandardCharsetObserver() {
        }

        public String getCharset() {
            return this.result;
        }

        public boolean isFound() {
            return this.found;
        }

        @Override
        public void Notify(String charset) {
            this.result = charset;
            this.found = true;
        }
    }
}

