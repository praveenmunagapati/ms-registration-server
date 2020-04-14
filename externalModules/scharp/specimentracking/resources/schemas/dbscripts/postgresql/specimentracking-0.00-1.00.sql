/*
 * Copyright (c) 2003-2005 Fred Hutchinson Cancer Research Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

BEGIN;

CREATE SCHEMA specimentracking;

CREATE TABLE specimentracking.Manifests
(
	-- standard fields
	_ts TIMESTAMP DEFAULT now(),
	RowId SERIAL,
	CreatedBy USERID,
	Created TIMESTAMP,

	-- other fields
    Container ENTITYID NOT NULL,
ManifestId varchar(50) ,
ShipDate date,
ShippingLab varchar(10),
RecipientLab varchar(10),

CONSTRAINT PK_Manifests PRIMARY KEY (ManifestId)
);

CREATE TABLE specimentracking.ManifestSpecimens
(
	RowId SERIAL,
	
	-- other fields
    Container ENTITYID NOT NULL,
	ManifestId varchar(50) NOT NULL,
	SpecimenId varchar(50),
	RowNumber int,
	ColumnNumber int,
	Reconciled boolean,
	OnManifest boolean,


    CONSTRAINT PK_ManifestSpecimens PRIMARY KEY (SpecimenId),
CONSTRAINT FK_ManifestSpecimens_Manifests FOREIGN KEY (ManifestId) REFERENCES specimentracking.Manifests(ManifestId)
);

COMMIT;