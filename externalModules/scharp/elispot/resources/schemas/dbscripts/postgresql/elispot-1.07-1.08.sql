ALTER TABLE elispot.tblplatetemplatedetails DROP COLUMN blinded_name;
ALTER TABLE elispot.tblplatetemplatedetails
ADD COLUMN  blinded_name TEXT DEFAULT NULL;
ALTER TABLE elispot.tblplatemap DROP COLUMN blinded_name;
ALTER TABLE elispot.tblplatemap
ADD COLUMN  blinded_name TEXT DEFAULT NULL;
DROP VIEW elispot.batchinformation cascade;
DROP VIEW elispot.plateinformation cascade;
DROP VIEW elispot.elispotdataflatfile cascade;
CREATE OR REPLACE VIEW elispot.batchinformation(
                             batch_seq_id,
                             batch_description,
                             reader_seq_id,
                             lab_study_seq_id,
                             lab_seq_id,
                             lab_desc,
                             study_seq_id,
                             study_description,
                             study_identifier,
			                 plateinfo_reqd
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
         s.study_identifier,
	     s.plateinfo_reqd
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

CREATE OR REPLACE
    VIEW elispot.plateinformation (
               plate_seq_id,
               friendly_name,
	           blinded_name,
               spec_well_group,
               replicate,
               final_well_id,
               text_sfu,
               bool_use_blinded_name
           ) AS
SELECT
 pmap.plate_seq_id,
 pmap.friendly_name,
 pmap.blinded_name,
 pmap.spec_well_group,
 pmap.replicate,
 pmap.final_well_id,
 pdata.text_sfu,
 pt.bool_use_blinded_name
 FROM
 elispot.tblplatemap pmap LEFT JOIN (elispot.tblplate p LEFT JOIN elispot.tblplatetemplate pt ON p.template_seq_id=pt.template_seq_id) ON pmap.plate_seq_id = p.plate_seq_id,
 elispot.tblplatedata pdata
 WHERE
 trim(pmap.final_well_id) = trim(pdata.well_id)
 AND
 pmap.plate_seq_id = pdata.plate_seq_id;
  COMMENT ON VIEW elispot.plateinformation
  IS
      'To get Plate Information';

CREATE OR REPLACE VIEW elispot.elispotdataflatfile AS
 SELECT s.network_organization, s.study_description, s.study_identifier, l.lab_desc, b.batch_description, b.batch_type, r.reader_desc, p.plate_name, p.import_date, p.test_date, p.tech_id,
        CASE
            WHEN upper(btrim(p.isprecoated)) = 'TRUE'::text THEN 'Y'::text
            WHEN upper(btrim(p.isprecoated)) = 'FALSE'::text THEN 'N'::text
            ELSE p.isprecoated
        END AS precoated, sub.substrate_desc, ptype.platetype_desc, pt.readout, pt.incubate, cr.cryostatus_desc, a.additive_desc, ctr.counter_desc, sp.ptid, sp.visit_no, sp.draw_date, ps.runnum, ps.d1_cellcount, ps.d2_cellcount, ps.d1_viability, ps.d2_viability, pmap.antigen_id, pmap.friendly_name, pmap.spec_well_group, pmap.replicate, pmap.final_well_id, pmap.pepconc, pmap.pepunit, pmap.effector, pmap.cellsperwell, pmap.blinded_name, pdata.text_sfu, pdata.sfu,
        CASE
            WHEN pdata.assayrun = true THEN 'Y'::text
            ELSE 'N'::text
        END AS assayrun,
        CASE
            WHEN pdata.reliable = true THEN 'Y'::text
            ELSE 'N'::text
        END AS reliable, p.comment
   FROM elispot.tblstudylabs ls, elispot.tbllabs l, elispot.tblstudy s, elispot.tblplatetemplate pt, elispot.tblbatch b
   LEFT JOIN elispot.tblreaders r ON b.reader_seq_id = r.reader_seq_id, elispot.tblplate p
   LEFT JOIN elispot.tblplatedata pdata ON p.plate_seq_id = pdata.plate_seq_id
   LEFT JOIN elispot.tblsubstrate sub ON p.substrate_seq_id = sub.substrate_seq_id
   LEFT JOIN elispot.tblplatetype ptype ON p.platetype_seq_id = ptype.platetype_seq_id, elispot.tblplatemap pmap
   LEFT JOIN (elispot.tblplatespecimens ps
   LEFT JOIN elispot.tblcryostatus cr ON ps.cryostatus = cr.cryostatus
   LEFT JOIN elispot.tbladditive a ON ps.additive_seq_id = a.additive_seq_id
   LEFT JOIN elispot.tblcellcounter ctr ON ps.counter_seq_id = ctr.counter_seq_id
   LEFT JOIN elispot.tblspecimen sp ON ps.specimen_seq_id = sp.specimen_seq_id) ON pmap.plate_seq_id = ps.plate_seq_id AND pmap.spec_well_group::text = ps.spec_well_group::text
  WHERE btrim(pmap.final_well_id) = btrim(pdata.well_id) AND pmap.plate_seq_id = pdata.plate_seq_id AND p.batch_seq_id = b.batch_seq_id AND b.lab_study_seq_id = ls.lab_study_seq_id AND ls.lab_seq_id = l.lab_seq_id AND ls.study_seq_id = s.study_seq_id AND p.template_seq_id = pt.template_seq_id AND (ps.specimen_seq_id IS NOT NULL AND pmap.spec_well_group::text <> 'S'::text AND ps.bool_report_specimen = true OR ps.specimen_seq_id IS NULL AND pmap.spec_well_group::text = 'S'::text) AND pmap.antigen_id <> 'EMPTY'::text AND p.bool_report_plate = true
  ORDER BY pmap.plate_seq_id, pmap.spec_well_group, pmap.final_well_id;