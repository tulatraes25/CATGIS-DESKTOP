/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.ogcwebservices.getcapabilities.Contents
 *  org.deegree.ogcwebservices.getcapabilities.OperationsMetadata
 */
package org.deegree.owscommon;

import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;

public abstract class OWSCommonCapabilities
extends OGCCapabilities {
    private ServiceIdentification serviceIdentification;
    private ServiceProvider serviceProvider;
    private OperationsMetadata operationsMetadata;
    private Contents contents;

    protected OWSCommonCapabilities(String version, String updateSequence, ServiceIdentification serviceIdentification, ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, Contents contents) {
        super(version, updateSequence);
        this.serviceIdentification = serviceIdentification;
        this.serviceProvider = serviceProvider;
        this.operationsMetadata = operationsMetadata;
        this.contents = contents;
    }

    public Contents getContents() {
        return this.contents;
    }

    public void setContents(Contents contents) {
        this.contents = contents;
    }

    public OperationsMetadata getOperationsMetadata() {
        return this.operationsMetadata;
    }

    public void setOperationsMetadata(OperationsMetadata operationsMetadata) {
        this.operationsMetadata = operationsMetadata;
    }

    public ServiceIdentification getServiceIdentification() {
        return this.serviceIdentification;
    }

    public void setServiceIdentification(ServiceIdentification serviceIdentification) {
        this.serviceIdentification = serviceIdentification;
    }

    public ServiceProvider getServiceProvider() {
        return this.serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
}

