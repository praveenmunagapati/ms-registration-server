package org.scharp.atlas.specimentracking.model;

import org.labkey.api.data.Entity;

import java.sql.Date;

public class ManifestSpecimens extends Entity
{
    private String shipId;
    private String specimenId;
    private String groupName;
    private String ptid;
    private String protocol;
    private String visit;
    private Date CollectionDate;
    private String sampleType;
    private String additive;
    private String cellsperVial;
    private String volperVial;
    private String boxNumber;
    private String rowNumber;
    private String columnNumber;
    private String visitType;

    private boolean reConciled;
    private boolean onManifest;

    public ManifestSpecimens() {
    }

    public ManifestSpecimens(String shipId, String specimenId, String groupName,
                             String ptid, String protocol, String visit, Date collectionDate,
                             String sampleType, String additive, String cellsperVial,
                             String volperVial, String boxNumber, String rowNumber,
                             String columnNumber, String visitType, boolean reConciled,
                             boolean onManifest) {
        this.shipId = shipId;
        this.specimenId = specimenId;
        this.groupName = groupName;
        this.ptid = ptid;
        this.protocol = protocol;
        this.visit = visit;
        CollectionDate = collectionDate;
        this.sampleType = sampleType;
        this.additive = additive;
        this.cellsperVial = cellsperVial;
        this.volperVial = volperVial;
        this.boxNumber = boxNumber;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.visitType = visitType;
        this.reConciled = reConciled;
        this.onManifest = onManifest;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public String getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPtid() {
        return ptid;
    }

    public void setPtid(String ptid) {
        this.ptid = ptid;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public Date getCollectionDate() {
        return CollectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        CollectionDate = collectionDate;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getAdditive() {
        return additive;
    }

    public void setAdditive(String additive) {
        this.additive = additive;
    }

    public String getCellsperVial() {
        return cellsperVial;
    }

    public void setCellsperVial(String cellsperVial) {
        this.cellsperVial = cellsperVial;
    }

    public String getVolperVial() {
        return volperVial;
    }

    public void setVolperVial(String volperVial) {
        this.volperVial = volperVial;
    }

    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public boolean isReConciled() {
        return reConciled;
    }

    public void setReConciled(boolean reConciled) {
        this.reConciled = reConciled;
    }

    public boolean isOnManifest() {
        return onManifest;
    }

    public void setOnManifest(boolean onManifest) {
        this.onManifest = onManifest;
    }

}