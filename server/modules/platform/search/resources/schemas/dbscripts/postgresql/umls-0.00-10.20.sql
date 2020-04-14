/*
 * Copyright (c) 2012-2019 LabKey Corporation
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

/* umls-10.10-10.11.sql */

/* umls-0.00-10.10.sql */

CREATE SCHEMA umls;

CREATE TABLE umls.MRCOC (
    CUI1 CHAR(8) NOT NULL,
    AUI1 VARCHAR(9) NOT NULL,
    CUI2 CHAR(8),
    AUI2 VARCHAR(9),
    SAB VARCHAR(20) NOT NULL,
    COT VARCHAR(3) NOT NULL,
    COF INTEGER,
    COA VARCHAR(300),
    CVF INTEGER
);

CREATE TABLE umls.MRCOLS (
    COL VARCHAR(20),
    DES VARCHAR(200),
    REF VARCHAR(20),
    MIN INTEGER,
    AV numeric(5,2),
    MAX INTEGER,
    FIL VARCHAR(50),
    DTY VARCHAR(20)
);

CREATE TABLE umls.MRCONSO (
    CUI CHAR(8) NOT NULL,
    LAT CHAR(3) NOT NULL,
    TS CHAR(1) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    STT VARCHAR(3) NOT NULL,
    SUI VARCHAR(10) NOT NULL,
    ISPREF CHAR(1) NOT NULL,
    AUI VARCHAR(9) NOT NULL,
    SAUI VARCHAR(50),
    SCUI VARCHAR(50),
    SDUI VARCHAR(50),
    SAB VARCHAR(20) NOT NULL,
    TTY VARCHAR(20) NOT NULL,
    CODE VARCHAR(50) NOT NULL,
    STR VARCHAR(3000) NOT NULL,
    SRL INTEGER NOT NULL,
    SUPPRESS CHAR(1) NOT NULL,
    CVF INTEGER
);

CREATE TABLE umls.MRCUI (
    CUI1 CHAR(8) NOT NULL,
    VER VARCHAR(10) NOT NULL,
    REL VARCHAR(4) NOT NULL,
    RELA VARCHAR(100),
    MAPREASON VARCHAR(4000),
    CUI2 CHAR(8),
    MAPIN CHAR(1)
);

CREATE TABLE umls.MRCXT (
    CUI CHAR(8),
    SUI VARCHAR(10),
    AUI VARCHAR(9),
    SAB VARCHAR(20),
    CODE VARCHAR(50),
    CXN INTEGER,
    CXL CHAR(3),
    RANK INTEGER,
    CXS VARCHAR(3000),
    CUI2 CHAR(8),
    AUI2 VARCHAR(9),
    HCD VARCHAR(50),
    RELA VARCHAR(100),
    XC VARCHAR(1),
    CVF INTEGER
);

CREATE TABLE umls.MRDEF (
    CUI CHAR(8) NOT NULL,
    AUI VARCHAR(9) NOT NULL,
    ATUI VARCHAR(11) NOT NULL,
    SATUI VARCHAR(50),
    SAB VARCHAR(20) NOT NULL,
    DEF TEXT NOT NULL,
    SUPPRESS CHAR(1) NOT NULL,
    CVF INTEGER
);

CREATE TABLE umls.MRDOC (
    DOCKEY VARCHAR(50) NOT NULL,
    VALUE VARCHAR(200),
    TYPE VARCHAR(50) NOT NULL,
    EXPL VARCHAR(1000)
);


CREATE TABLE umls.MRFILES (
    FIL VARCHAR(50),
    DES VARCHAR(200),
    FMT VARCHAR(300),
    CLS INTEGER,
    RWS INTEGER,
    BTS INTEGER
);

CREATE TABLE umls.MRHIER (
    CUI CHAR(8) NOT NULL,
    AUI VARCHAR(9) NOT NULL,
    CXN INTEGER NOT NULL,
    PAUI VARCHAR(10),
    SAB VARCHAR(20) NOT NULL,
    RELA VARCHAR(100),
    PTR VARCHAR(1000),
    HCD VARCHAR(50),
    CVF INTEGER
);

CREATE TABLE umls.MRHIST (
    CUI CHAR(8),
    SOURCEUI VARCHAR(50),
    SAB VARCHAR(20),
    SVER VARCHAR(20),
    CHANGETYPE VARCHAR(1000),
    CHANGEKEY VARCHAR(1000),
    CHANGEVAL VARCHAR(1000),
    REASON VARCHAR(1000),
    CVF INTEGER
);

CREATE TABLE umls.MRMAP (
    MAPSETCUI CHAR(8) NOT NULL,
    MAPSETSAB VARCHAR(20) NOT NULL,
    MAPSUBSETID VARCHAR(10),
    MAPRANK INTEGER,
    MAPID VARCHAR(50) NOT NULL,
    MAPSID VARCHAR(50),
    FROMID VARCHAR(50) NOT NULL,
    FROMSID VARCHAR(50),
    FROMEXPR VARCHAR(4000) NOT NULL,
    FROMTYPE VARCHAR(50) NOT NULL,
    FROMRULE VARCHAR(4000),
    FROMRES VARCHAR(4000),
    REL VARCHAR(4) NOT NULL,
    RELA VARCHAR(100),
    TOID VARCHAR(50),
    TOSID VARCHAR(50),
    TOEXPR VARCHAR(4000),
    TOTYPE VARCHAR(50),
    TORULE VARCHAR(4000),
    TORES VARCHAR(4000),
    MAPRULE VARCHAR(4000),
    MAPRES VARCHAR(4000),
    MAPTYPE VARCHAR(50),
    MAPATN VARCHAR(20),
    MAPATV VARCHAR(4000),
    CVF INTEGER
);

CREATE TABLE umls.MRRANK (
    RANK INTEGER NOT NULL,
    SAB VARCHAR(20) NOT NULL,
    TTY VARCHAR(20) NOT NULL,
    SUPPRESS CHAR(1) NOT NULL
);

CREATE TABLE umls.MRREL (
    CUI1 CHAR(8) NOT NULL,
    AUI1 VARCHAR(9),
    STYPE1 VARCHAR(50) NOT NULL,
    REL VARCHAR(4) NOT NULL,
    CUI2 CHAR(8) NOT NULL,
    AUI2 VARCHAR(9),
    STYPE2 VARCHAR(50) NOT NULL,
    RELA VARCHAR(100),
    RUI VARCHAR(10) NOT NULL,
    SRUI VARCHAR(50),
    SAB VARCHAR(20) NOT NULL,
    SL VARCHAR(20) NOT NULL,
    RG VARCHAR(10),
    DIR VARCHAR(1),
    SUPPRESS CHAR(1) NOT NULL,
    CVF INTEGER
);

CREATE TABLE umls.MRSAB (
    VCUI CHAR(8),
    RCUI CHAR(8) NOT NULL,
    VSAB VARCHAR(20) NOT NULL,
    RSAB VARCHAR(20) NOT NULL,
    SON VARCHAR(3000) NOT NULL,
    SF VARCHAR(20) NOT NULL,
    SVER VARCHAR(20),
    VSTART CHAR(8),
    VEND CHAR(8),
    IMETA VARCHAR(10) NOT NULL,
    RMETA VARCHAR(10),
    SLC VARCHAR(1000),
    SCC VARCHAR(1000),
    SRL INTEGER NOT NULL,
    TFR INTEGER,
    CFR INTEGER,
    CXTY VARCHAR(50),
    TTYL VARCHAR(300),
    ATNL VARCHAR(1000),
    LAT CHAR(3),
    CENC VARCHAR(20) NOT NULL,
    CURVER CHAR(1) NOT NULL,
    SABIN CHAR(1) NOT NULL,
    SSN VARCHAR(3000) NOT NULL,
    SCIT VARCHAR(4000) NOT NULL
);

CREATE TABLE umls.MRSAT (
    CUI CHAR(8) NOT NULL,
    LUI VARCHAR(10),
    SUI VARCHAR(10),
    METAUI VARCHAR(50),
    STYPE VARCHAR(50) NOT NULL,
    CODE VARCHAR(50),
    ATUI VARCHAR(11) NOT NULL,
    SATUI VARCHAR(50),
    ATN VARCHAR(50) NOT NULL,
    SAB VARCHAR(20) NOT NULL,
    ATV VARCHAR(4000),
    SUPPRESS CHAR(1) NOT NULL,
    CVF INTEGER
);

CREATE TABLE umls.MRSMAP (
    MAPSETCUI CHAR(8) NOT NULL,
    MAPSETSAB VARCHAR(20) NOT NULL,
    MAPID VARCHAR(50) NOT NULL,
    MAPSID VARCHAR(50),
    FROMEXPR VARCHAR(4000) NOT NULL,
    FROMTYPE VARCHAR(50) NOT NULL,
    REL VARCHAR(4) NOT NULL,
    RELA VARCHAR(100),
    TOEXPR VARCHAR(4000),
    TOTYPE VARCHAR(50),
    CVF INTEGER
);

CREATE TABLE umls.MRSTY (
    CUI CHAR(8) NOT NULL,
    TUI CHAR(4) NOT NULL,
    STN VARCHAR(100) NOT NULL,
    STY VARCHAR(50) NOT NULL,
    ATUI VARCHAR(11) NOT NULL,
    CVF INTEGER
);

CREATE TABLE umls.MRXNS_ENG (
    LAT CHAR(3) NOT NULL,
    NSTR VARCHAR(3000) NOT NULL,
    CUI CHAR(8) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    SUI VARCHAR(10) NOT NULL
);

CREATE TABLE umls.MRXNW_ENG (
    LAT CHAR(3) NOT NULL,
    NWD VARCHAR(100) NOT NULL,
    CUI CHAR(8) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    SUI VARCHAR(10) NOT NULL
);

CREATE TABLE umls.MRAUI (
    AUI1 VARCHAR(9) NOT NULL,
    CUI1 CHAR(8) NOT NULL,
    VER VARCHAR(10) NOT NULL,
    REL VARCHAR(4),
    RELA VARCHAR(100),
    MAPREASON VARCHAR(4000) NOT NULL,
    AUI2 VARCHAR(9) NOT NULL,
    CUI2 CHAR(8) NOT NULL,
    MAPIN CHAR(1) NOT NULL
);

CREATE TABLE umls.MRXW (
    LAT CHAR(3) NOT NULL,
    WD VARCHAR(200) NOT NULL,
    CUI CHAR(8) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    SUI VARCHAR(10) NOT NULL
);


CREATE TABLE umls.AMBIGSUI (
    SUI VARCHAR(10) NOT NULL,
    CUI CHAR(8) NOT NULL
);

CREATE TABLE umls.AMBIGLUI (
    LUI VARCHAR(10) NOT NULL,
    CUI CHAR(8) NOT NULL
);

CREATE TABLE umls.DELETEDCUI (
    PCUI CHAR(8) NOT NULL,
    PSTR VARCHAR(3000) NOT NULL
);

CREATE TABLE umls.DELETEDLUI (
    PLUI VARCHAR(10) NOT NULL,
    PSTR VARCHAR(3000) NOT NULL
);

CREATE TABLE umls.DELETEDSUI (
    PSUI VARCHAR(10) NOT NULL,
    LAT CHAR(3) NOT NULL,
    PSTR VARCHAR(3000) NOT NULL
);

CREATE TABLE umls.MERGEDCUI (
    PCUI CHAR(8) NOT NULL,
    CUI CHAR(8) NOT NULL
);

CREATE TABLE umls.MERGEDLUI (
    PLUI VARCHAR(10),
    LUI VARCHAR(10)
);


CREATE OR REPLACE FUNCTION umls.createIndexes() RETURNS INTEGER AS $$
BEGIN
    CREATE INDEX X_MRCOC_CUI1 ON umls.MRCOC(CUI1);
    CREATE INDEX X_MRCOC_AUI1 ON umls.MRCOC(AUI1);
    CREATE INDEX X_MRCOC_CUI2 ON umls.MRCOC(CUI2);
    CREATE INDEX X_MRCOC_AUI2 ON umls.MRCOC(AUI2);
    CREATE INDEX X_MRCOC_SAB ON umls.MRCOC(SAB);
    CREATE INDEX X_MRCONSO_CUI ON umls.MRCONSO(CUI);
    ALTER TABLE umls.MRCONSO ADD CONSTRAINT X_MRCONSO_PK PRIMARY KEY (AUI);
    CREATE INDEX X_MRCONSO_SUI ON umls.MRCONSO(SUI);
    CREATE INDEX X_MRCONSO_LUI ON umls.MRCONSO(LUI);
    CREATE INDEX X_MRCONSO_CODE ON umls.MRCONSO(CODE);
    CREATE INDEX X_MRCONSO_SAB_TTY ON umls.MRCONSO(SAB,TTY);
    CREATE INDEX X_MRCONSO_SCUI ON umls.MRCONSO(SCUI);
    CREATE INDEX X_MRCONSO_SDUI ON umls.MRCONSO(SDUI);
  --  CREATE INDEX X_MRCONSO_STR ON umls.MRCONSO(STR);
    CREATE INDEX X_MRCXT_CUI ON umls.MRCXT(CUI);
    CREATE INDEX X_MRCXT_AUI ON umls.MRCXT(AUI);
    CREATE INDEX X_MRCXT_SAB ON umls.MRCXT(SAB);
    CREATE INDEX X_MRDEF_CUI ON umls.MRDEF(CUI);
    CREATE INDEX X_MRDEF_AUI ON umls.MRDEF(AUI);
    ALTER TABLE umls.MRDEF ADD CONSTRAINT X_MRDEF_PK PRIMARY KEY (ATUI);
    CREATE INDEX X_MRDEF_SAB ON umls.MRDEF(SAB);
    CREATE INDEX X_MRHIER_CUI ON umls.MRHIER(CUI);
    CREATE INDEX X_MRHIER_AUI ON umls.MRHIER(AUI);
    CREATE INDEX X_MRHIER_SAB ON umls.MRHIER(SAB);
  --  CREATE INDEX X_MRHIER_PTR ON umls.MRHIER(PTR);
    CREATE INDEX X_MRHIER_PAUI ON umls.MRHIER(PAUI);
    CREATE INDEX X_MRHIST_CUI ON umls.MRHIST(CUI);
    CREATE INDEX X_MRHIST_SOURCEUI ON umls.MRHIST(SOURCEUI);
    CREATE INDEX X_MRHIST_SAB ON umls.MRHIST(SAB);
    ALTER TABLE umls.MRRANK ADD CONSTRAINT X_MRRANK_PK PRIMARY KEY (SAB,TTY);
    CREATE INDEX X_MRREL_CUI1 ON umls.MRREL(CUI1);
    CREATE INDEX X_MRREL_AUI1 ON umls.MRREL(AUI1);
    CREATE INDEX X_MRREL_CUI2 ON umls.MRREL(CUI2);
    CREATE INDEX X_MRREL_AUI2 ON umls.MRREL(AUI2);
    ALTER TABLE umls.MRREL ADD CONSTRAINT X_MRREL_PK PRIMARY KEY (RUI);
    CREATE INDEX X_MRREL_SAB ON umls.MRREL(SAB);
    ALTER TABLE umls.MRSAB ADD CONSTRAINT X_MRSAB_PK PRIMARY KEY (VSAB);
    CREATE INDEX X_MRSAB_RSAB ON umls.MRSAB(RSAB);
    CREATE INDEX X_MRSAT_CUI ON umls.MRSAT(CUI);
    CREATE INDEX X_MRSAT_METAUI ON umls.MRSAT(METAUI);
    ALTER TABLE umls.MRSAT ADD CONSTRAINT X_MRSAT_PK PRIMARY KEY (ATUI);
    CREATE INDEX X_MRSAT_SAB ON umls.MRSAT(SAB);
    CREATE INDEX X_MRSAT_ATN ON umls.MRSAT(ATN);
    CREATE INDEX X_MRSTY_CUI ON umls.MRSTY(CUI);
    ALTER TABLE umls.MRSTY ADD CONSTRAINT X_MRSTY_PK PRIMARY KEY (ATUI);
    CREATE INDEX X_MRSTY_STY ON umls.MRSTY(STY);
  --  CREATE INDEX X_MRXNS_ENG_NSTR ON umls.MRXNS_ENG(NSTR);
    CREATE INDEX X_MRXNW_ENG_NWD ON umls.MRXNW_ENG(NWD);
    CREATE INDEX X_MRXW_WD ON umls.MRXW(WD);
    CREATE INDEX X_AMBIGSUI_SUI ON umls.AMBIGSUI(SUI);
    CREATE INDEX X_AMBIGLUI_LUI ON umls.AMBIGLUI(LUI);
    CREATE INDEX X_MRAUI_CUI2 ON umls.MRAUI(CUI2);
    CREATE INDEX X_MRCUI_CUI2 ON umls.MRCUI(CUI2);
    CREATE INDEX X_MRMAP_MAPSETCUI ON umls.MRMAP(MAPSETCUI);
    RETURN 1;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION umls.dropIndexes() RETURNS INTEGER AS $$
BEGIN
    DROP INDEX umls.X_MRCOC_CUI1;
    DROP INDEX umls.X_MRCOC_AUI1;
    DROP INDEX umls.X_MRCOC_CUI2;
    DROP INDEX umls.X_MRCOC_AUI2;
    DROP INDEX umls.X_MRCOC_SAB;
    DROP INDEX umls.X_MRCONSO_CUI;
    ALTER TABLE umls.MRCONSO DROP CONSTRAINT X_MRCONSO_PK;
    DROP INDEX umls.X_MRCONSO_SUI;
    DROP INDEX umls.X_MRCONSO_LUI;
    DROP INDEX umls.X_MRCONSO_CODE;
    DROP INDEX umls.X_MRCONSO_SAB_TTY;
    DROP INDEX umls.X_MRCONSO_SCUI;
    DROP INDEX umls.X_MRCONSO_SDUI ;
  --  DROP INDEX umls.X_MRCONSO_STR;
    DROP INDEX umls.X_MRCXT_CUI;
    DROP INDEX umls.X_MRCXT_AUI;
    DROP INDEX umls.X_MRCXT_SAB;
    DROP INDEX umls.X_MRDEF_CUI;
    DROP INDEX umls.X_MRDEF_AUI;
    ALTER TABLE umls.MRDEF DROP CONSTRAINT X_MRDEF_PK;
    DROP INDEX umls.X_MRDEF_SAB;
    DROP INDEX umls.X_MRHIER_CUI;
    DROP INDEX umls.X_MRHIER_AUI;
    DROP INDEX umls.X_MRHIER_SAB;
  --  DROP INDEX umls.X_MRHIER_PTR;
    DROP INDEX umls.X_MRHIER_PAUI;
    DROP INDEX umls.X_MRHIST_CUI;
    DROP INDEX umls.X_MRHIST_SOURCEUI;
    DROP INDEX umls.X_MRHIST_SAB;
    ALTER TABLE umls.MRRANK DROP CONSTRAINT X_MRRANK_PK;
    DROP INDEX umls.X_MRREL_CUI1;
    DROP INDEX umls.X_MRREL_AUI1;
    DROP INDEX umls.X_MRREL_CUI2;
    DROP INDEX umls.X_MRREL_AUI2;
    ALTER TABLE umls.MRREL DROP CONSTRAINT X_MRREL_PK;
    DROP INDEX umls.X_MRREL_SAB;
    ALTER TABLE umls.MRSAB DROP CONSTRAINT X_MRSAB_PK;
    DROP INDEX umls.X_MRSAB_RSAB;
    DROP INDEX umls.X_MRSAT_CUI;
    DROP INDEX umls.X_MRSAT_METAUI;
    ALTER TABLE umls.MRSAT DROP CONSTRAINT X_MRSAT_PK;
    DROP INDEX umls.X_MRSAT_SAB;
    DROP INDEX umls.X_MRSAT_ATN;
    DROP INDEX umls.X_MRSTY_CUI;
    ALTER TABLE umls.MRSTY DROP CONSTRAINT X_MRSTY_PK;
    DROP INDEX umls.X_MRSTY_STY;
  --  DROP INDEX umls.X_MRXNS_ENG_NSTR;
    DROP INDEX umls.X_MRXNW_ENG_NWD;
    DROP INDEX umls.X_MRXW_WD;
    DROP INDEX umls.X_AMBIGSUI_SUI;
    DROP INDEX umls.X_AMBIGLUI_LUI;
    DROP INDEX umls.X_MRAUI_CUI2;
    DROP INDEX umls.X_MRCUI_CUI2;
    DROP INDEX umls.X_MRMAP_MAPSETCUI;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;