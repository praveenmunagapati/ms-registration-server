ALTER TABLE elispot.tblplatetemplatedetails ADD COLUMN blinded_name text;
ALTER TABLE elispot.tblplatemap ADD COLUMN blinded_name text;
ALTER TABLE elispot.tblplatetemplate ADD COLUMN bool_use_blinded_name boolean DEFAULT false;