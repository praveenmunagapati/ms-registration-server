package org.scharp.atlas.specimentracking.model;

import java.sql.Date;

import org.labkey.api.data.Entity;

public class Manifests extends Entity
{
    private Integer rowId;
    private String shipId;
    private Date shipDate;
    private String recipientLab;
    private String shippingLab;
    private String shippingMethod;

    private Date dateReceived;
    private String manifestFilename;
    public Manifests() {
    }

    public Manifests(Integer rowId, String shipId, Date shipDate, String recipientLab,
                     String shippingLab, String shippingMethod, Date dateReceived,
                     String manifestFilename) {
        this.rowId = rowId;
        this.shipId = shipId;
        this.shipDate = shipDate;
        this.recipientLab = recipientLab;
        this.shippingLab = shippingLab;
        this.shippingMethod = shippingMethod;
        this.dateReceived = dateReceived;
        this.manifestFilename = manifestFilename;
    }

    public Integer getRowId() {
        return rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public String getRecipientLab() {
        return recipientLab;
    }

    public void setRecipientLab(String recipientLab) {
        this.recipientLab = recipientLab;
    }

    public String getShippingLab() {
        return shippingLab;
    }

    public void setShippingLab(String shippingLab) {
        this.shippingLab = shippingLab;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public String getManifestFilename() {
        return manifestFilename;
    }

    public void setManifestFilename(String manifestFilename) {
        this.manifestFilename = manifestFilename;
    }

}