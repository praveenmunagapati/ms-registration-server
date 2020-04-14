CREATE TABLE elispot.tblsubstrate (
    substrate_seq_id serial NOT NULL,
    substrate_desc text,
    CONSTRAINT PK_tblsubstrate PRIMARY KEY (substrate_seq_id)
);

CREATE TABLE elispot.tblplatetype (
    platetype_seq_id serial NOT NULL,
    platetype_desc text,
    CONSTRAINT PK_tblplatetype PRIMARY KEY (platetype_seq_id)
);

ALTER TABLE elispot.tblplate ADD COLUMN isprecoated BOOLEAN;
ALTER TABLE elispot.tblplate ADD COLUMN substrate_seq_id integer;
ALTER TABLE elispot.tblplate ADD COLUMN platetype_seq_id integer;

ALTER TABLE elispot.tblstudy
ADD COLUMN plateinfo_reqd BOOLEAN default false;

ALTER TABLE elispot.tblplate
	ADD CONSTRAINT FK_tblplate3 FOREIGN KEY (substrate_seq_id) REFERENCES elispot.tblsubstrate (substrate_seq_id);

ALTER TABLE elispot.tblplate
	ADD CONSTRAINT FK_tblplate4 FOREIGN KEY (platetype_seq_id) REFERENCES elispot.tblplatetype (platetype_seq_id);

INSERT INTO elispot.tblsubstrate (substrate_seq_id,substrate_desc) VALUES (nextval('elispot.tblsubstrate_substrate_seq_id_seq'),'AEC');
INSERT INTO elispot.tblsubstrate (substrate_seq_id,substrate_desc) VALUES (nextval('elispot.tblsubstrate_substrate_seq_id_seq'),'NovaRed');
INSERT INTO elispot.tblsubstrate (substrate_seq_id,substrate_desc) VALUES (nextval('elispot.tblsubstrate_substrate_seq_id_seq'),'TMB');
INSERT INTO elispot.tblsubstrate (substrate_seq_id,substrate_desc) VALUES (nextval('elispot.tblsubstrate_substrate_seq_id_seq'),'BCIP/NBT');
INSERT INTO elispot.tblsubstrate (substrate_seq_id,substrate_desc) VALUES (nextval('elispot.tblsubstrate_substrate_seq_id_seq'),'FastRed');

INSERT INTO elispot.tblplatetype (platetype_seq_id,platetype_desc) VALUES (nextval('elispot.tblplatetype_platetype_seq_id_seq'),'BD');
INSERT INTO elispot.tblplatetype (platetype_seq_id,platetype_desc) VALUES (nextval('elispot.tblplatetype_platetype_seq_id_seq'),'Mabtech');
INSERT INTO elispot.tblplatetype (platetype_seq_id,platetype_desc) VALUES (nextval('elispot.tblplatetype_platetype_seq_id_seq'),'Millipore MAIP');
INSERT INTO elispot.tblplatetype (platetype_seq_id,platetype_desc) VALUES (nextval('elispot.tblplatetype_platetype_seq_id_seq'),'Millipore MSIP');
INSERT INTO elispot.tblplatetype (platetype_seq_id,platetype_desc) VALUES (nextval('elispot.tblplatetype_platetype_seq_id_seq'),'R&D Systems');