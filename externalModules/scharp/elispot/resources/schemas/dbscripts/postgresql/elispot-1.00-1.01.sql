CREATE TABLE elispot.tblsfutranslation (

    reader_type text,
    text_sfu text,
    sfu integer

);

INSERT INTO elispot.tblsfutranslation (reader_type, text_sfu, sfu) VALUES ('CTL-XLS', '-1', NULL);
INSERT INTO elispot.tblsfutranslation (reader_type, text_sfu, sfu) VALUES ('CTL-XLS', '-2', '9999');
INSERT INTO elispot.tblsfutranslation (reader_type, text_sfu, sfu) VALUES ('CTL-XLS', '-3', NULL);
INSERT INTO elispot.tblsfutranslation (reader_type, text_sfu, sfu) VALUES ('CTL-XLS', '-4', NULL);
INSERT INTO elispot.tblsfutranslation (reader_type, text_sfu, sfu) VALUES ('AID-TXT', '-', NULL);

CREATE VIEW elispot.elispotdataflatfile(
    network_organization, study_description, study_identifier,
    lab_desc,
    batch_description, batch_type,
    reader_desc,
    plate_name, import_date, test_date, tech_id,
    readout, incubate,
    cryostatus_desc,
    additive_desc,
    counter_desc,
    specimen_seq_id,
    ptid, visit_no, draw_date,
    antigen_id, friendly_name, spec_well_group, replicate, final_well_id,
    text_sfu,sfu
)AS
SELECT
    s.network_organization, s.study_description, s.study_identifier,
    l.lab_desc,
    b.batch_description, b.batch_type,
    r.reader_desc,
    p.plate_name, p.import_date, p.test_date, p.tech_id,
    pt.readout, pt.incubate,
    cr.cryostatus_desc,
    a.additive_desc,
    ctr.counter_desc,
    ps.specimen_seq_id,
    sp.ptid, sp.visit_no, sp.draw_date,
    pmap.antigen_id, pmap.friendly_name, pmap.spec_well_group, pmap.replicate, pmap.final_well_id,
    pdata.text_sfu, pdata.sfu

   FROM
   elispot.tblstudylabs ls, elispot.tbllabs l, elispot.tblstudy s, elispot.tblplatetemplate pt, elispot.tblbatch b
   LEFT JOIN elispot.tblreaders r ON b.reader_seq_id = r.reader_seq_id, elispot.tblplate p
   LEFT JOIN elispot.tblplatedata pdata ON p.plate_seq_id = pdata.plate_seq_id, elispot.tblplatemap pmap
   LEFT JOIN (elispot.tblplatespecimens ps
   LEFT JOIN elispot.tblcryostatus cr ON ps.cryostatus = cr.cryostatus
   LEFT JOIN elispot.tbladditive a ON ps.additive_seq_id = a.additive_seq_id
   LEFT JOIN elispot.tblcellcounter ctr ON ps.counter_seq_id = ctr.counter_seq_id
   LEFT JOIN elispot.tblspecimen sp ON ps.specimen_seq_id = sp.specimen_seq_id) ON pmap.plate_seq_id = ps.plate_seq_id AND pmap.spec_well_group::text = ps.spec_well_group::text
  WHERE btrim(pmap.final_well_id) = btrim(pdata.well_id) AND pmap.plate_seq_id = pdata.plate_seq_id AND p.batch_seq_id = b.batch_seq_id AND b.lab_study_seq_id = ls.lab_study_seq_id AND ls.lab_seq_id = l.lab_seq_id AND ls.study_seq_id = s.study_seq_id AND p.template_seq_id = pt.template_seq_id AND ps.specimen_seq_id IS NOT NULL AND pmap.antigen_id <> 'EMPTY'::text
  ORDER BY pmap.plate_seq_id, pmap.spec_well_group, pmap.final_well_id;