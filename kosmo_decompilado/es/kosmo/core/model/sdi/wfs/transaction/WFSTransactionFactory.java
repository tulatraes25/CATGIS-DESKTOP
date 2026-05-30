/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.model.sdi.wfs.transaction;

import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.model.sdi.wfs.transaction.AbstractWFSTransaction;
import es.kosmo.core.model.sdi.wfs.transaction.WFSTransaction_1_0_0;
import es.kosmo.core.model.sdi.wfs.transaction.WFSTransaction_1_1_0;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;

public class WFSTransactionFactory {
    public static AbstractWFSTransaction createTransaction(WFSFeatureTypeInfo info, AbstractWFSWrapper service) throws Exception {
        if ("1.1.0".equals(info.getServiceVersion())) {
            return new WFSTransaction_1_1_0(info, service);
        }
        if ("1.0.0".equals(info.getServiceVersion())) {
            return new WFSTransaction_1_0_0(info, service);
        }
        throw new Exception("Unsupported WFS version " + info.getServiceVersion());
    }
}

