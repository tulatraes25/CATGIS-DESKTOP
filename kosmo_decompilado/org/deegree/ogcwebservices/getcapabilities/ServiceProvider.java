/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.xlink.SimpleLink
 *  org.deegree.model.metadata.iso19115.ContactInfo
 *  org.deegree.model.metadata.iso19115.TypeCode
 */
package org.deegree.ogcwebservices.getcapabilities;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.TypeCode;

public class ServiceProvider {
    private String providerName;
    private SimpleLink providerSite;
    private String individualName;
    private String positionName;
    private ContactInfo contactInfo;
    private TypeCode role;

    public ServiceProvider(String providerName, SimpleLink providerSite, String individualName, String positionName, ContactInfo contactInfo, TypeCode role) {
        this.providerName = providerName;
        this.providerSite = providerSite;
        this.individualName = individualName;
        this.positionName = positionName;
        this.contactInfo = contactInfo;
        this.role = role;
    }

    public ContactInfo getContactInfo() {
        return this.contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getIndividualName() {
        return this.individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getPositionName() {
        return this.positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public SimpleLink getProviderSite() {
        return this.providerSite;
    }

    public void setProviderSite(SimpleLink providerSite) {
        this.providerSite = providerSite;
    }

    public TypeCode getRole() {
        return this.role;
    }

    public void setRole(TypeCode role) {
        this.role = role;
    }
}

