DELETE from specimentracking.ManifestSpecimens;
DROP TABLE specimentracking.ManifestSpecimens;
DELETE from specimentracking.Manifests;
DROP TABLE specimentracking.Manifests;

CREATE TABLE specimentracking.Manifests
(
	-- standard fields
	RowId SERIAL,
	CreatedBy USERID,
	Created TIMESTAMP,
    Container ENTITYID NOT NULL,

    ShipId text ,
    ShipDate date,
    RecipientLab text,
    ShippingLab text,
    ShippingMethod text,

    DateReceived date,
    ManifestFilename text,

    CONSTRAINT PK_Manifests PRIMARY KEY (ShipId)
);

CREATE TABLE specimentracking.ManifestSpecimens
(
	RowId SERIAL,
	CreatedBy USERID,
	Created TIMESTAMP,
	ModifiedBy USERID,
	Modified TIMESTAMP,
	Container ENTITYID NOT NULL,

	ShipId text NOT NULL,
	SpecimenId text,
	GroupName text,
	Ptid text,
	Protocol text,
	Visit int,
	CollectionDate date,
	SampleType text,
	Additive text,
	CellsperVial text,
	VolperVial text,
	BoxNumber int,
	RowNumber int,
	ColumnNumber int,
	VisitType text,

	Reconciled boolean,
	OnManifest boolean,

    CONSTRAINT PK_ManifestSpecimens PRIMARY KEY (SpecimenId),
    CONSTRAINT FK_ManifestSpecimens_Manifests FOREIGN KEY (ShipId) REFERENCES specimentracking.Manifests(ShipId)
);