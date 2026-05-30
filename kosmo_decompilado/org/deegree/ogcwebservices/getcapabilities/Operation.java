/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.ogcwebservices.getcapabilities.DCPType
 *  org.deegree.owscommon.OWSDomainType
 */
package org.deegree.ogcwebservices.getcapabilities;

import java.io.Serializable;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.owscommon.OWSDomainType;

public class Operation
implements Serializable {
    private static final long serialVersionUID = -4092984827471246029L;
    private String name;
    private DCPType[] dcps;
    private OWSDomainType[] parameters;
    private Object[] metadata;

    public Operation(String name, DCPType[] dcps) {
        this(name, dcps, new OWSDomainType[0]);
    }

    public Operation(String name, DCPType[] dcpTypes, OWSDomainType[] parameters) {
        this.name = name;
        this.dcps = dcpTypes;
        this.parameters = parameters;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DCPType[] getDCPs() {
        return this.dcps;
    }

    public void setDCPs(DCPType[] dcpTypes) {
        this.dcps = dcpTypes;
    }

    public OWSDomainType getParameter(String name) {
        int i = 0;
        while (i < this.parameters.length) {
            if (this.parameters[i].getName().equals(name)) {
                return this.parameters[i];
            }
            ++i;
        }
        return null;
    }

    public OWSDomainType[] getParameters() {
        return this.parameters;
    }

    public void setParameters(OWSDomainType[] parameters) {
        this.parameters = parameters;
    }

    public Object[] getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Object[] metadata) {
        this.metadata = metadata;
    }
}

