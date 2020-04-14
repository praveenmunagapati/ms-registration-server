ALTER TABLE elispot.tblsfutranslation ADD COLUMN reader_seq_id integer;
UPDATE elispot.tblsfutranslation sfu SET reader_seq_id = (select read.reader_seq_id from elispot.tblreaders read where read.reader_type = sfu.reader_type);
ALTER TABLE elispot.tblsfutranslation DROP COLUMN reader_type;
ALTER TABLE elispot.tblsfutranslation
	ADD CONSTRAINT FK_tblsfutranslation FOREIGN KEY (reader_seq_id) REFERENCES elispot.tblreaders (reader_seq_id);
