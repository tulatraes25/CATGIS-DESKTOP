/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.ogcservices.wms.context.model;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class WMSService
implements Serializable {
    private static final long serialVersionUID = -3608814229607082833L;
    private Boolean servicePublic;
    private String context;
    private String title;
    private String description;
    private String contactPerson;
    private String contactOrganization;
    private String contactEMail;
    private String accessConstraints;
    private Boolean enabled;
    private String imageFormat;
    private Integer tileSize;
    private Boolean transparent;
    private Boolean baseLayer;
    private Boolean tiled;
    private String startDate;
    private String modifyDate;
    private int[] image;

    public Boolean isServicePublic() {
        return this.servicePublic;
    }

    public void setServicePublic(Boolean servicePublic) {
        this.servicePublic = servicePublic;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactPerson() {
        return this.contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactOrganization() {
        return this.contactOrganization;
    }

    public void setContactOrganization(String contactOrganization) {
        this.contactOrganization = contactOrganization;
    }

    public String getContactEMail() {
        return this.contactEMail;
    }

    public void setContactEMail(String contactEMail) {
        this.contactEMail = contactEMail;
    }

    public String getAccessConstraints() {
        return this.accessConstraints;
    }

    public void setAccessConstraints(String accessConstraints) {
        this.accessConstraints = accessConstraints;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getImageFormat() {
        return this.imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public Integer getTileSize() {
        return this.tileSize;
    }

    public void setTileSize(Integer tileSize) {
        this.tileSize = tileSize;
    }

    public Boolean isTransparent() {
        return this.transparent;
    }

    public void setTransparent(Boolean transparent) {
        this.transparent = transparent;
    }

    public Boolean isBaseLayer() {
        return this.baseLayer;
    }

    public void setBaseLayer(Boolean baseLayer) {
        this.baseLayer = baseLayer;
    }

    public Boolean isTiled() {
        return this.tiled;
    }

    public void setTiled(Boolean tiled) {
        this.tiled = tiled;
    }

    public String toString() {
        return this.title;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getModifyDate() {
        return this.modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.context == null ? 0 : this.context.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        WMSService other = (WMSService)obj;
        return !(this.context == null ? other.context != null : !this.context.equals(other.context));
    }

    public Image getImage() {
        return this.getImageFromArray(this.image, 100, 100);
    }

    public void setImage(int[] image) {
        this.image = image;
    }

    private Image getImageFromArray(int[] pixels, int width, int height) {
        if (pixels == null) {
            return null;
        }
        MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.createImage(mis);
    }

    public static class Collator1Comparator
    implements Comparator<WMSService> {
        private Collator collator = Collator.getInstance(new Locale("es", "ES"));

        @Override
        public int compare(WMSService wms1, WMSService wms2) {
            CollationKey key1 = this.collator.getCollationKey(wms1.getContext());
            CollationKey key2 = this.collator.getCollationKey(wms1.getContext());
            return key1.compareTo(key2);
        }
    }

    public static class Collator2Comparator
    implements Comparator<WMSService> {
        private Collator collator = Collator.getInstance(new Locale("es", "ES"));

        @Override
        public int compare(WMSService wms1, WMSService wms2) {
            CollationKey key2;
            CollationKey key1 = this.collator.getCollationKey(wms1.getContext());
            int diferencia = key1.compareTo(key2 = this.collator.getCollationKey(wms2.getContext()));
            if (diferencia > 0) {
                return -1;
            }
            if (diferencia < 0) {
                return 1;
            }
            return 0;
        }
    }
}

