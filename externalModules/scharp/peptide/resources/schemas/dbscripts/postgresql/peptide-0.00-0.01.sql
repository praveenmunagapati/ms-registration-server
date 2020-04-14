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



CREATE OR REPLACE
  VIEW peptide.group_peptides (
             peptide_id,
             peptide_group_id,
             peptide_sequence,
             sort_sequence,
             pathogen_id
         ) AS
  SELECT
         src.peptide_id,
         src.peptide_group_id,
         pp.peptide_sequence,
         pp.sort_sequence,
         pathogen.pathogen_id
    FROM
         peptide.source src,
         peptide.peptides pp,
         peptide.pathogen pathogen,
         peptide.peptide_group pgroup
   WHERE
         (
             (
                 (
                     src.peptide_group_id = pgroup.peptide_group_id
                 )
                 AND
                 (
                     src.peptide_id = pp.peptide_id
                 )
             )
             AND
             (
                 pathogen.pathogen_id = pgroup.pathogen_id
             )
         )
         ;
COMMENT ON VIEW peptide.group_peptides
IS
    'To get peptides in peptide group';

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

COMMIT;
