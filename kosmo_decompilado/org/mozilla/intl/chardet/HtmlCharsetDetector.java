/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import java.io.BufferedInputStream;
import java.net.URL;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

public class HtmlCharsetDetector {
    public static boolean found = false;

    public static void main(String[] argv) throws Exception {
        int len;
        if (argv.length != 1 && argv.length != 2) {
            System.out.println("Usage: HtmlCharsetDetector <url> [<languageHint>]");
            System.out.println("");
            System.out.println("Where <url> is http://...");
            System.out.println("For optional <languageHint>. Use following...");
            System.out.println("\t\t1 => Japanese");
            System.out.println("\t\t2 => Chinese");
            System.out.println("\t\t3 => Simplified Chinese");
            System.out.println("\t\t4 => Traditional Chinese");
            System.out.println("\t\t5 => Korean");
            System.out.println("\t\t6 => Dont know (default)");
            return;
        }
        int lang = argv.length == 2 ? Integer.parseInt(argv[1]) : 0;
        nsDetector det = new nsDetector(lang);
        det.Init(new nsICharsetDetectionObserver(){

            @Override
            public void Notify(String charset) {
                found = true;
                System.out.println("CHARSET = " + charset);
            }
        });
        URL url = new URL(argv[0]);
        BufferedInputStream imp = new BufferedInputStream(url.openStream());
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
        if (isAscii) {
            System.out.println("CHARSET = ASCII");
            found = true;
        }
        if (!found) {
            String[] prob = det.getProbableCharsets();
            int i = 0;
            while (i < prob.length) {
                System.out.println("Probable Charset = " + prob[i]);
                ++i;
            }
        }
    }
}

