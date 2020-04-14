CREATE TABLE peptide.peptide_status(
qc_passed character(1),
description text,

CONSTRAINT PK_peptide_status PRIMARY KEY (qc_passed)
);

INSERT INTO peptide.peptide_status (qc_passed, description) VALUES ('n', 'New');
INSERT INTO peptide.peptide_status (qc_passed, description) VALUES ('s', 'Success');
INSERT INTO peptide.peptide_status (qc_passed, description) VALUES ('f', 'Failed');
INSERT INTO peptide.peptide_status (qc_passed, description) VALUES ('t', 'Terminated');

DROP VIEW peptide.group_peptides cascade;

CREATE OR REPLACE
  VIEW peptide.group_peptides (
             peptide_id,
             peptide_group_id,
             peptide_sequence,
             sort_sequence,
             btk_code,
             protein_cat_id,
             protein_align_pep,
             pathogen_id,
             qc_passed
         ) AS
  SELECT
         src.peptide_id,
         src.peptide_group_id,
         pp.peptide_sequence,
         pp.sort_sequence,
         pp.btk_code,
         pp.protein_cat_id,
         pp.protein_align_pep,
          pgroup.pathogen_id,
          pp.qc_passed
    FROM
        peptide.source src,
        peptide.peptides pp,
        peptide.peptide_group pgroup
   WHERE
         (
                 (
                     src.peptide_group_id = pgroup.peptide_group_id
                 )
                 AND
                 (
                     src.peptide_id = pp.peptide_id
                 )
          )
         ;
COMMENT ON VIEW peptide.group_peptides
IS
    'To get peptides in peptide group';

DROP VIEW peptide.peptide_view cascade;
-- View for getting specific information on a single peptide
CREATE OR REPLACE VIEW peptide.peptide_view ( peptide_id, btk_code, peptide_sequence, protein_align_pep, child, qc_passed, date_added, protein_cat_desc, linked_parent ) AS
  SELECT DISTINCT
         (p.peptide_id),
         p.btk_code,
         p.peptide_sequence,
         p.protein_align_pep,
         p.child,
         p.qc_passed,
         p.date_added,
         pc.protein_cat_desc,
         pp.linked_parent
    FROM
         peptide.peptides p
     LEFT OUTER JOIN
         peptide.parent pp
         ON
         (
             p.peptide_id = pp.peptide_id
         )
         ,
         peptide.protein_category pc
   WHERE
         p.protein_cat_id = pc.protein_cat_id ;
COMMENT ON VIEW peptide.peptide_view IS 'To get detailed information for a peptide';

CREATE OR REPLACE VIEW peptide.pool_peptides(
peptide_id,
peptide_sequence,
peptide_pool_id,
peptide_in_pool) AS
 SELECT
src.peptide_id,
 pp.peptide_sequence,
 src.peptide_pool_id,
src.peptide_in_pool
  FROM
peptide.peptide_pool_assignment src,
 peptide.peptides pp
 WHERE src.peptide_id = pp.peptide_id;

UPDATE peptide.peptide_pool  SET "exists"='n' where "exists" = 'f';

ALTER TABLE peptide.peptides
      ADD CONSTRAINT FK_peptides_status FOREIGN KEY(qc_passed) REFERENCES peptide.peptide_status(qc_passed);

ALTER TABLE peptide.peptide_pool
      ADD CONSTRAINT FK_peptidepool_status FOREIGN KEY(exists) REFERENCES peptide.peptide_status(qc_passed);