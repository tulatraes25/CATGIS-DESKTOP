/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException
 */
package org.deegree.ogcwebservices.wfs.capabilities;

import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument_1_0_0;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument_1_1_0;

public class WFSCapabilitiesDocument
extends WFSCapabilitiesDocument_1_1_0 {
    private static final long serialVersionUID = 3975709039097932869L;

    @Override
    public OGCCapabilities parseCapabilities() throws InvalidCapabilitiesException {
        OGCCapabilities capabilities = null;
        String version = this.getRootElement().getAttribute("version");
        if ("1.0.0".equals(version)) {
            WFSCapabilitiesDocument_1_0_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_0_0();
            capabilitiesDoc.setRootElement(this.getRootElement());
            capabilities = capabilitiesDoc.parseCapabilities();
        } else {
            WFSCapabilitiesDocument_1_1_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_1_0();
            capabilitiesDoc.setRootElement(this.getRootElement());
            capabilities = capabilitiesDoc.parseCapabilities();
        }
        return capabilities;
    }
}

