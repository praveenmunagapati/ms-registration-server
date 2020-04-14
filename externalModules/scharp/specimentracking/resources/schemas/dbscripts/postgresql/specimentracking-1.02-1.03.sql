DELETE from specimentracking.ManifestSpecimens;
DROP TABLE specimentracking.ManifestSpecimens;
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
	Visit text,
	CollectionDate date,
	SampleType text,
	Additive text,
	CellsperVial text,
	VolperVial text,
	BoxNumber text,
	RowNumber text,
	ColumnNumber text,
	VisitType text,

	Reconciled boolean,
	OnManifest boolean,

    CONSTRAINT PK_ManifestSpecimens PRIMARY KEY (SpecimenId),
    CONSTRAINT FK_ManifestSpecimens_Manifests FOREIGN KEY (ShipId) REFERENCES specimentracking.Manifests(ShipId)
);