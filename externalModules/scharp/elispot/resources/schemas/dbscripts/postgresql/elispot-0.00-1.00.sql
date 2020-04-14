BEGIN;

CREATE SCHEMA elispot;

CREATE TABLE elispot.tblstudy (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    study_seq_id serial NOT NULL,
    study_description text,
    network_organization text,
    study_identifier text,
    protocol text,
    status text,

    CONSTRAINT PK_tblstudy PRIMARY KEY (study_seq_id)
);

CREATE TABLE elispot.tblreaders(
    reader_seq_id serial,
    reader_desc text,
    reader_type text,
    file_ext character(3),

    CONSTRAINT PK_tblreaders PRIMARY KEY (reader_seq_id)
);

CREATE TABLE elispot.tbllabs(
       _ts TIMESTAMP DEFAULT now(),
     CreatedBy USERID,
     Created TIMESTAMP,
     ModifiedBy USERID,
     Modified TIMESTAMP,

     Container ENTITYID NOT NULL,

     lab_seq_id serial,
     lab_desc text,
     permgroupname text,

     CONSTRAINT PK_tbllabs PRIMARY KEY (lab_seq_id)
 );

CREATE TABLE elispot.tblstudylabs(
   _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    lab_study_seq_id serial NOT NULL unique,
    lab_seq_id integer,
    study_seq_id integer,


    CONSTRAINT PK_tblstudylabs PRIMARY KEY (study_seq_id,lab_seq_id),
    CONSTRAINT FK_tblstudylabs1 FOREIGN KEY(lab_seq_id) REFERENCES elispot.tbllabs(lab_seq_id),
    CONSTRAINT FK_tblstudylabs2 FOREIGN KEY(study_seq_id) REFERENCES elispot.tblstudy(study_seq_id)
);

    CREATE TABLE elispot.tblbatchtype(
        batch_type character(1),
        batch_type_desc text,

        CONSTRAINT pk_tblbatchtype PRIMARY KEY(batch_type)
    );


CREATE TABLE elispot.tblbatch (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    batch_seq_id serial NOT NULL,
    batch_description text NOT NULL,
    batch_type character(1),
    reader_seq_id integer,
    lab_study_seq_id integer,

    CONSTRAINT PK_tblbatch PRIMARY KEY (batch_seq_id),
    CONSTRAINT FK_tblbatch1 FOREIGN KEY(reader_seq_id) REFERENCES elispot.tblreaders(reader_seq_id),
    CONSTRAINT FK_tblbatch2 FOREIGN KEY(batch_type) REFERENCES elispot.tblbatchtype(batch_type),
    CONSTRAINT FK_tblbatch3 FOREIGN KEY(lab_study_seq_id) REFERENCES elispot.tblstudylabs(lab_study_seq_id)
);

CREATE TABLE elispot.tbldefault_rows_columns (

    wellrow character(1) NOT NULL,
    wellcol smallint NOT NULL,
    well_id text NOT NULL,

    CONSTRAINT PK_tbldefault_rows_columns PRIMARY KEY (well_id)

);

INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '1', 'A01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '2', 'A02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '3', 'A03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '4', 'A04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '5', 'A05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '6', 'A06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '7', 'A07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '8', 'A08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '9', 'A09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '1', 'B01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '2', 'B02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '3', 'B03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '4', 'B04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '5', 'B05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '6', 'B06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '7', 'B07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '8', 'B08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '9', 'B09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '1', 'C01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '2', 'C02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '3', 'C03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '4', 'C04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '5', 'C05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '6', 'C06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '7', 'C07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '8', 'C08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '9', 'C09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '1', 'D01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '2', 'D02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '3', 'D03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '4', 'D04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '5', 'D05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '6', 'D06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '7', 'D07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '8', 'D08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '9', 'D09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '1', 'E01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '2', 'E02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '3', 'E03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '4', 'E04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '5', 'E05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '6', 'E06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '7', 'E07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '8', 'E08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '9', 'E09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '1', 'F01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '2', 'F02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '3', 'F03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '4', 'F04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '5', 'F05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '6', 'F06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '7', 'F07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '8', 'F08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '9', 'F09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '1', 'G01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '2', 'G02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '3', 'G03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '4', 'G04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '5', 'G05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '6', 'G06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '7', 'G07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '8', 'G08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '9', 'G09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '1', 'H01');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '2', 'H02');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '3', 'H03');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '4', 'H04');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '5', 'H05');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '6', 'H06');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '7', 'H07');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '8', 'H08');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '9', 'H09');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '10', 'A10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '11', 'A11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('A', '12', 'A12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '10', 'B10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '11', 'B11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('B', '12', 'B12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '10', 'C10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '11', 'C11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('C', '12', 'C12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '10', 'D10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '11', 'D11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('D', '12', 'D12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '10', 'E10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '11', 'E11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('E', '12', 'E12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '10', 'F10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '11', 'F11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('F', '12', 'F12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '10', 'G10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '11', 'G11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('G', '12', 'G12');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '10', 'H10');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '11', 'H11');
INSERT INTO elispot.tbldefault_rows_columns (wellrow, wellcol, well_id) VALUES ('H', '12', 'H12');

CREATE TABLE elispot.tblplatetemplate (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    template_seq_id serial NOT NULL,
    template_description text NOT NULL,
    num_well_groups_per_plate smallint,
    stimulated boolean,
    incubate double precision,/*hours*/
    readout text,/*IFNG*/
    study_seq_id integer,

    CONSTRAINT PK_tblplatetemplate PRIMARY KEY(template_seq_id),
    CONSTRAINT FK_tblplatetemplate  FOREIGN KEY (study_seq_id) REFERENCES elispot.tblstudy(study_seq_id)

);

CREATE TABLE elispot.tblplate (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    plate_seq_id serial NOT NULL,
    plate_name text NOT NULL,
    template_seq_id integer,
    batch_seq_id integer,
    import_date date,
    test_date date,
    freezer_plate_id text,
    tech_id text,
    plate_filename text,
    bool_report_plate boolean DEFAULT true,
    approved_by USERID DEFAULT NULL,
    comment text,

    CONSTRAINT PK_tblplate PRIMARY KEY (plate_seq_id),
    CONSTRAINT FK_tblplate1  FOREIGN KEY (batch_seq_id) REFERENCES elispot.tblbatch(batch_seq_id),
    CONSTRAINT FK_tblplate2  FOREIGN KEY (template_seq_id) REFERENCES elispot.tblplatetemplate(template_seq_id)

);

CREATE TABLE elispot.tblplatedata (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    well_id text NOT NULL,
    text_sfu text,
    sfu integer,
    plate_seq_id integer NOT NULL,
    assayrun BOOLEAN  DEFAULT true,
    reliable BOOLEAN  DEFAULT true,

    CONSTRAINT PK_tblplatedata PRIMARY KEY (plate_seq_id, well_id),
    CONSTRAINT FK1_tblplatedata  FOREIGN KEY (plate_seq_id) REFERENCES elispot.tblplate(plate_seq_id),
    CONSTRAINT FK2_tblplatedata  FOREIGN KEY (well_id) REFERENCES elispot.tbldefault_rows_columns(well_id)

);

CREATE TABLE elispot.tblspecimen (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    specimen_seq_id serial,
    ptid text ,
    visit_no double precision default 0.0,
    draw_date date,
    study_seq_id integer,

    CONSTRAINT PK_tblspecimen PRIMARY KEY(specimen_seq_id),
    CONSTRAINT FK_tblspecimen FOREIGN KEY(study_seq_id) REFERENCES elispot.tblstudy(study_seq_id)

);

CREATE TABLE elispot.tblplatemap (

   _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    orig_well_id text NOT NULL,
    final_well_id text,
    spec_well_group varchar(1),
    antigen_id text,
    friendly_name text,
    replicate text,
    pepconc double precision,
    pepunit text,
    stcl text,
    effector text,
    stimconc text,
    cellsperwell integer,
    plate_seq_id integer NOT NULL,

     CONSTRAINT PK_tblplatemap  PRIMARY KEY (plate_seq_id, orig_well_id),
     CONSTRAINT FK_tblplatemap  FOREIGN KEY (plate_seq_id) REFERENCES elispot.tblplate(plate_seq_id)
);

CREATE TABLE elispot.tblcellcounter(
    counter_seq_id serial,
    counter_desc text,
    CONSTRAINT PK_tblcellcounter  PRIMARY KEY (counter_seq_id)
);

CREATE TABLE elispot.tblcryostatus(
cryostatus integer,
cryostatus_desc text,

CONSTRAINT PK_tblcryostatus PRIMARY KEY(cryostatus)
);

INSERT INTO elispot.tblcryostatus (cryostatus, cryostatus_desc) VALUES ('0', 'Fresh');
INSERT INTO elispot.tblcryostatus (cryostatus, cryostatus_desc) VALUES ('1', 'Frozen');

CREATE TABLE elispot.tbladditive(
additive_seq_id serial,
additive_desc text,

CONSTRAINT PK_tbladditive PRIMARY KEY(additive_seq_id)
);

CREATE TABLE elispot.tblplatespecimens (

    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    Container ENTITYID NOT NULL,

    spec_well_group varchar(1) NOT NULL,
    specimen_id text,
    bool_report_specimen boolean DEFAULT true,
    runnum integer,
    additive_seq_id integer,
    cryostatus integer,
    plate_seq_id integer NOT NULL,
    d1_cellcount float8 DEFAULT NULL,
    d2_cellcount float8 DEFAULT NULL,
    d1_viability float8 DEFAULT NULL,
    d2_viability float8 DEFAULT NULL,
    counter_seq_id integer,
    specimen_seq_id integer,

    CONSTRAINT PK_tblplatespecimens PRIMARY KEY (plate_seq_id, spec_well_group),
    CONSTRAINT FK_tblplatespecimens1  FOREIGN KEY (plate_seq_id) REFERENCES elispot.tblplate(plate_seq_id),
    CONSTRAINT FK_tblplatespecimens2  FOREIGN KEY (counter_seq_id) REFERENCES elispot.tblcellcounter(counter_seq_id),
    CONSTRAINT FK_tblplatespecimens3  FOREIGN KEY (specimen_seq_id) REFERENCES elispot.tblspecimen(specimen_seq_id),
    CONSTRAINT FK_tblplatespecimens4  FOREIGN KEY (cryostatus) REFERENCES elispot.tblcryostatus(cryostatus),
    CONSTRAINT FK_tblplatespecimens5 FOREIGN KEY (additive_seq_id) REFERENCES elispot.tbladditive(additive_seq_id)
);

COMMENT ON COLUMN elispot.tblplatespecimens.runnum IS 'Allows a specimen to be run multiple times for a given template';

CREATE TABLE elispot.tblplatetemplatedetails (

      _ts TIMESTAMP DEFAULT now(),
     CreatedBy USERID,
     Created TIMESTAMP,
     ModifiedBy USERID,
     Modified TIMESTAMP,

     Container ENTITYID NOT NULL,

     spec_well_group varchar(1),
     antigen_id text,
     friendly_name text,
     replicate smallint,
     pepconc double precision,
     pepunit text,
     stcl text,
     effector text,
     stimconc double precision,
     cellsperwell smallint,
     well_id text NOT NULL,
     template_seq_id integer NOT NULL,

     CONSTRAINT PK_tblplatetemplatedetails PRIMARY KEY (template_seq_id, well_id),
     CONSTRAINT FK_tblplatetemplatedetails FOREIGN KEY (template_seq_id) REFERENCES elispot.tblplatetemplate(template_seq_id)
 );

 CREATE OR REPLACE
    VIEW elispot.plateinformation (
               plate_seq_id,
               friendly_name,
               spec_well_group,
               replicate,
               final_well_id,
               text_sfu
           ) AS
    SELECT
           pmap.plate_seq_id,
           pmap.friendly_name,
           pmap.spec_well_group,
           pmap.replicate,
           pmap.final_well_id,
           pdata.text_sfu
      FROM
           elispot.tblplatemap pmap,
           elispot.tblplatedata pdata
     WHERE
           (
               (
                   (
                       trim(pmap.final_well_id) = trim(pdata.well_id)
                   )
                   AND
                   (
                       pmap.plate_seq_id = pdata.plate_seq_id
                   )
               )

           )
           ;
  COMMENT ON VIEW elispot.plateinformation IS 'To get Plate Information';

 CREATE OR REPLACE VIEW elispot.batchinformation(
                             batch_seq_id,
                             batch_description,
                             reader_seq_id,
                             lab_study_seq_id,
                             lab_seq_id,
                             lab_desc,
                             study_seq_id,
                             study_description,
                             study_identifier
                         )AS
   SELECT
         b.batch_seq_id,
         b.batch_description,
         b.reader_seq_id,
         b.lab_study_seq_id,
         sl.lab_seq_id,
         l.lab_desc,
         sl.study_seq_id,
         s.study_description,
         s.study_identifier
    FROM
     elispot.tblbatch b,
     elispot.tblstudylabs sl,
     elispot.tbllabs l,
     elispot.tblstudy s
   WHERE
   (((b.lab_study_seq_id = sl.lab_study_seq_id )
   AND
   (sl.lab_seq_id = l.lab_seq_id))
    AND
   (sl.study_seq_id = s.study_seq_id));

   COMMIT;


