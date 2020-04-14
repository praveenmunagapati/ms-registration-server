

CREATE VIEW peptide.group_peptides AS
    SELECT src.peptide_id, src.peptide_group_id, src.btk_code, src.transmitted_status, src.history_id, pgroup.pathogen_id, p.peptide_sequence, p.sort_sequence, p.protein_cat_id, p.protein_align_pep, p.qc_passed, p.child, p.parent FROM ((peptide.source src LEFT JOIN peptide.peptide_group pgroup ON ((src.peptide_group_id = pgroup.peptide_group_id))) LEFT JOIN peptide.peptides p ON ((src.peptide_id = p.peptide_id)));

CREATE VIEW peptide.peptide_view AS
    SELECT DISTINCT p.peptide_id, p.protein_align_pep, p.peptide_sequence, p.sort_sequence, p.protein_cat_id, pc.protein_cat_desc, p.child, p.qc_passed, p.lanl_date, p.parent FROM (peptide.peptides p LEFT JOIN peptide.protein_category pc ON ((p.protein_cat_id = pc.protein_cat_id))) ORDER BY p.peptide_id, p.protein_align_pep, p.peptide_sequence, p.sort_sequence, p.protein_cat_id, pc.protein_cat_desc, p.child, p.qc_passed, p.lanl_date, p.parent;

CREATE VIEW peptide.pool_details AS
    SELECT ppool.peptide_pool_id, ppool.pool_type, ppool.description, pmg.peptide_group_id, pmg.matrix_pool_id, mp.matrix_id FROM (peptide.peptide_pool ppool LEFT JOIN (peptide.pool_matrix_group pmg LEFT JOIN peptide.matrix_pool mp ON ((pmg.matrix_pool_id = mp.matrix_pool_id))) ON ((ppool.peptide_pool_id = pmg.peptide_pool_id))) ORDER BY ppool.peptide_pool_id;

COMMENT ON VIEW peptide.pool_details IS 'To get the pool details';

CREATE VIEW peptide.pool_peptides AS
    SELECT src.peptide_id, pp.peptide_sequence, src.peptide_pool_id, src.peptide_in_pool, src.history_id FROM peptide.peptide_pool_assignment src, peptide.peptides pp WHERE (src.peptide_id = pp.peptide_id);

