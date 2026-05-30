/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.Code
 *  org.deegree.model.metadata.iso19115.Keywords
 */
package org.deegree.ogcwebservices.getcapabilities;

import org.deegree.datatypes.Code;
import org.deegree.model.metadata.iso19115.Keywords;

public class ServiceIdentification {
    private String name;
    private Code serviceType;
    private String[] serviceTypeVersions;
    private String title;
    private String serviceAbstract;
    private Keywords[] keywords;
    private String fees;
    private String[] accessConstraints;

    public ServiceIdentification(String name, Code serviceType, String[] serviceTypeVersions, String title, String serviceAbstract, Keywords[] keywords, String fees, String[] accessConstraints) {
        this.name = name;
        this.serviceType = serviceType;
        this.serviceTypeVersions = serviceTypeVersions;
        this.title = title;
        this.serviceAbstract = serviceAbstract;
        this.keywords = keywords;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    public ServiceIdentification(Code serviceType, String[] serviceTypeVersions, String title, String serviceAbstract, Keywords[] keywords, String fees, String[] accessConstraints) {
        this.name = title;
        this.serviceType = serviceType;
        this.serviceTypeVersions = serviceTypeVersions;
        this.title = title;
        this.serviceAbstract = serviceAbstract;
        this.keywords = keywords;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    public Code getServiceType() {
        return this.serviceType;
    }

    public String[] getServiceTypeVersions() {
        return this.serviceTypeVersions;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAbstract() {
        return this.serviceAbstract;
    }

    public Keywords[] getKeywords() {
        return this.keywords;
    }

    public String getFees() {
        return this.fees;
    }

    public String[] getAccessConstraints() {
        return this.accessConstraints;
    }

    public String getName() {
        return this.name;
    }
}

