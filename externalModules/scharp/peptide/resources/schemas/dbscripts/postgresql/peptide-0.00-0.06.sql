CREATE SCHEMA peptide;

CREATE FUNCTION peptide.createpeppools(matrix_id_in text, pgid_in text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    max_pep_count int4;
    record_no int4;
    peptide_pool_id int4;
    colname text;
    pool_data RECORD;
    mpref RECORD;
    mpid RECORD;
    gmpid RECORD;
    hoy date;
    v_peptide_id int4;
    v_matrix_pool_pep_id int4;
    existingpool RECORD;
    blank text;

BEGIN

    SELECT INTO hoy * from current_timestamp;

    SELECT INTO existingpool * from peptide.pool_details p WHERE p.peptide_group_id = pgid_in AND p.matrix_id = matrix_id_in;
        
    IF FOUND THEN
        DISCARD TEMP;
        RAISE WARNING 'Peptide Pools have already been created for this group: % and matrix: %', pgid_in, matrix_id_in;
	return 1;
    END IF;

    create TEMPORARY sequence rownum;
    
CREATE TEMPORARY TABLE temp_pool
(
  peptide_reference integer,
  peptide_id integer,
  peptide_group_id text
)
WITH (
  OIDS=FALSE
);

CREATE TEMPORARY TABLE temp_pool_1
(
  peptide_id integer,
  peptide_group_id text
)
WITH (
  OIDS=FALSE
);
    max_pep_count := 420;
    record_no := 0;
    
    BEGIN
        INSERT INTO temp_pool_1 SELECT p.peptide_id, g.peptide_group_id FROM peptide.peptides p, peptide.source s,
            peptide.peptide_group g WHERE g.peptide_group_id = s.peptide_group_id AND s.peptide_id = p.peptide_id
            AND s.peptide_group_id = pgid_in ORDER BY p.sort_sequence;

        INSERT INTO temp_pool SELECT nextval('rownum'), t.peptide_id, t.peptide_group_id from temp_pool_1 t;

    EXCEPTION WHEN OTHERS THEN
        DISCARD TEMP;
	RAISE WARNING 'Unable to populate temporary pool tables';
	return 1;
    END;

    FOR mpref IN 
	SELECT mpp.matrix_pool_pep_id, mpp.peptide_reference 
	    FROM peptide.matrices m, peptide.matrix_pool mp, peptide.matrix_pool_peptide mpp 
	    WHERE m.matrix_id = matrix_id_in 
	    AND mp.matrix_id = m.matrix_id 
	    AND mp.matrix_pool_id = mpp.matrix_pool_id LOOP

	    record_no := record_no + 1;

	BEGIN
	    SELECT INTO v_peptide_id, v_matrix_pool_pep_id temp_pool.peptide_id, mpref.matrix_pool_pep_id 
               FROM temp_pool
	        WHERE mpref.peptide_reference = temp_pool.peptide_reference;

	    blank := '';
	    IF (FOUND) THEN
	        INSERT INTO peptide.group_matrix_peptides VALUES (hoy, 0, hoy, hoy, pgid_in, v_matrix_pool_pep_id, v_peptide_id, hoy);
	    END IF;

        EXCEPTION WHEN OTHERS THEN
            DISCARD TEMP;
	    RAISE WARNING 'Unable to create peptide pools: group_matrix_peptides';
	    return 1;
	END;

    END LOOP;

    FOR mpid IN 
    
	SELECT DISTINCT(m.matrix_pool_id) as matrix_pool_id
	    FROM peptide.group_matrix_peptides g, peptide.matrix_pool_peptide m , peptide.matrix_pool mp
	    WHERE m.matrix_pool_pep_id = g.matrix_pool_pep_id 
	    AND g.peptide_group_id = pgid_in
	    AND m.matrix_pool_id = mp.matrix_pool_id
	    AND mp.matrix_id = matrix_id_in
	    ORDER BY m.matrix_pool_id LOOP

	BEGIN
	    INSERT INTO peptide.peptide_pool 
	        VALUES (hoy, 0, hoy, hoy, nextval('peptide.peptide_pool_peptide_pool_id_seq'), 'Matrix', '', '', 'n', hoy);
	    SELECT INTO peptide_pool_id currval('peptide.peptide_pool_peptide_pool_id_seq');

	    INSERT INTO peptide.pool_matrix_group
	        VALUES (hoy, 0, hoy, hoy, peptide_pool_id, mpid.matrix_pool_id, pgid_in);
	EXCEPTION WHEN OTHERS THEN
	    DISCARD TEMP;
	    RAISE WARNING 'Unable to create peptide pools: peptide_pool';
	    return 1;
	END;

	FOR gmpid IN 
	    SELECT g.peptide_id as gmpeptide_id
	        FROM peptide.group_matrix_peptides g, peptide.matrix_pool_peptide m 
	        WHERE m.matrix_pool_pep_id = g.matrix_pool_pep_id 
	        AND g.peptide_group_id = pgid_in 
	        AND m.matrix_pool_id = mpid.matrix_pool_id LOOP

	    BEGIN
	        INSERT INTO peptide.peptide_pool_assignment
		    VALUES (hoy, 0, hoy, hoy, peptide_pool_id, gmpid.gmpeptide_id, 'f');
	    EXCEPTION WHEN OTHERS THEN
	        DISCARD TEMP;
	        RAISE WARNING 'Unable to create peptide pools: group_matrix_peptides';
	        return 1;
	    END;
	END LOOP;
	
    END LOOP;

    colname := 'peptide_group_id';

    DISCARD TEMP;
return 0;
END;
$$;

COMMENT ON FUNCTION peptide.createpeppools(matrix_id_in text, pgid_in text) IS 'Function to create matrix pools and added missing FROM temp_pool and changed the definition of insert into group_patient.';

CREATE TABLE peptide.clade (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    clade_id SERIAL NOT NULL,
    clade_desc text
);

CREATE TABLE peptide.group_matrix_peptides (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_group_id text NOT NULL,
    matrix_pool_pep_id integer NOT NULL,
    peptide_id integer,
    version timestamp without time zone,
    history_id integer
);

CREATE TABLE peptide.group_patient (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modifiedby integer,
    modified timestamp without time zone,
    peptide_group_id text NOT NULL,
    ptid text,
    draw_date date,
    study text,
    visit_no integer
);

CREATE TABLE peptide.peptide_group (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modifiedby integer,
    modified timestamp without time zone,
    peptide_group_id text NOT NULL,
    pathogen_id integer,
    seq_ref text,
    seq_source text,
    clade_id integer,
    pep_align_ref text,
    pep_align_source text,
    peptide_set text,
    group_type_id integer
);


CREATE TABLE peptide.peptides (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_id SERIAL NOT NULL,
    protein_align_pep text,
    peptide_sequence text,
    sort_sequence integer,
    protein_cat_id integer,
    child boolean,
    qc_passed character(1),
    lanl_date text,
    parent boolean DEFAULT false NOT NULL,
    src_file_name text,
    modifiedby integer
);

CREATE TABLE peptide.source (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_id integer NOT NULL,
    peptide_group_id text NOT NULL,
    btk_code text,
    history_id integer,
    transmitted_status text DEFAULT 'N/A'::text NOT NULL,
    modifiedby integer
);


CREATE TABLE peptide.group_type (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    group_type_id SERIAL NOT NULL,
    group_type_desc text
);


CREATE TABLE peptide.lanl_input_error (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    record_id SERIAL NOT NULL,
    input_row text,
    reason text
);

CREATE TABLE peptide.matrices (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    matrix_id text NOT NULL,
    peptide_count integer,
    peptides_per_pool integer,
    dimension integer,
    pools_per_dimension integer,
    pool_count text,
    plate_count text,
    comment text
);

CREATE TABLE peptide.matrix_pool (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone DEFAULT now(),
    modified timestamp without time zone DEFAULT now(),
    matrix_pool_id text NOT NULL,
    matrix_id text
);

CREATE TABLE peptide.matrix_pool_peptide (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone DEFAULT now(),
    modified timestamp without time zone DEFAULT now(),
    matrix_pool_pep_id SERIAL NOT NULL,
    matrix_pool_id text,
    peptide_reference integer
);

CREATE TABLE peptide.parent (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_id integer NOT NULL,
    linked_parent integer NOT NULL,
    parent_position text NOT NULL,
    history_id integer
);

CREATE TABLE peptide.pathogen (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    pathogen_id SERIAL NOT NULL,
    pathogen_desc text
);

CREATE TABLE peptide.peptide_pool (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_pool_id SERIAL NOT NULL,
    pool_type text,
    description text,
    comment text,
    "exists" character(1),
    create_date timestamp without time zone
);

CREATE TABLE peptide.peptide_pool_assignment (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_pool_id integer NOT NULL,
    peptide_id integer NOT NULL,
    peptide_in_pool boolean,
    history_id integer
);

CREATE TABLE peptide.peptide_status (
    qc_passed character(1) NOT NULL,
    description text
);


CREATE TABLE peptide.protein_category (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    protein_cat_id SERIAL NOT NULL,
    protein_cat_desc text,
    protein_cat_mnem character(1),
    protein_sort_value integer
);

CREATE TABLE peptide.pool_matrix_group (
    _ts timestamp without time zone DEFAULT now(),
    createdby integer,
    created timestamp without time zone,
    modified timestamp without time zone,
    peptide_pool_id integer NOT NULL,
    matrix_pool_id text,
    peptide_group_id text NOT NULL
);

CREATE TABLE peptide.replicate_history (
    history_id SERIAL NOT NULL,
    peptide_id integer NOT NULL,
    btk_code text,
    protein_align_pep text,
    peptide_sequence text,
    sort_sequence integer,
    protein_cat_id integer,
    child boolean,
    qc_passed character(1),
    date_added timestamp without time zone,
    parent boolean DEFAULT false NOT NULL,
    master_peptide_id integer
);

CREATE TABLE peptide.temp_pool (
    peptide_reference integer,
    peptide_id integer,
    peptide_group_id text
);

CREATE TABLE peptide.temp_pool_1 (
    peptide_id integer,
    peptide_group_id text
);


CREATE TABLE peptide.transmitted_status (
    transmitted_status text NOT NULL,
    description text
);

ALTER TABLE ONLY peptide.clade
    ADD CONSTRAINT clade_pkey PRIMARY KEY (clade_id);

ALTER TABLE ONLY peptide.group_matrix_peptides
    ADD CONSTRAINT group_matrix_peptides_pkey PRIMARY KEY (peptide_group_id, matrix_pool_pep_id);

ALTER TABLE ONLY peptide.group_patient
    ADD CONSTRAINT group_patient_pkey PRIMARY KEY (peptide_group_id);

ALTER TABLE ONLY peptide.group_type
    ADD CONSTRAINT group_type_pkey PRIMARY KEY (group_type_id);

ALTER TABLE ONLY peptide.matrices
    ADD CONSTRAINT matrices_pkey PRIMARY KEY (matrix_id);

ALTER TABLE ONLY peptide.matrix_pool_peptide
    ADD CONSTRAINT matrix_pool_peptide_pkey PRIMARY KEY (matrix_pool_pep_id);

ALTER TABLE ONLY peptide.matrix_pool
    ADD CONSTRAINT matrix_pool_pkey PRIMARY KEY (matrix_pool_id);

ALTER TABLE ONLY peptide.parent
    ADD CONSTRAINT parent_pkey PRIMARY KEY (peptide_id, linked_parent, parent_position);

ALTER TABLE ONLY peptide.pathogen
    ADD CONSTRAINT pathogen_pkey PRIMARY KEY (pathogen_id);

ALTER TABLE ONLY peptide.peptide_group
    ADD CONSTRAINT peptide_group_pkey PRIMARY KEY (peptide_group_id);

ALTER TABLE ONLY peptide.peptide_pool_assignment
    ADD CONSTRAINT peptide_pool_assignment_pkey PRIMARY KEY (peptide_pool_id, peptide_id);

ALTER TABLE ONLY peptide.peptide_pool
    ADD CONSTRAINT peptide_pool_pkey PRIMARY KEY (peptide_pool_id);

ALTER TABLE ONLY peptide.peptides
    ADD CONSTRAINT peptides_pkey PRIMARY KEY (peptide_id);

ALTER TABLE ONLY peptide.peptide_status
    ADD CONSTRAINT pk_peptide_status PRIMARY KEY (qc_passed);

ALTER TABLE ONLY peptide.transmitted_status
    ADD CONSTRAINT pk_transmitted_status PRIMARY KEY (transmitted_status);

ALTER TABLE ONLY peptide.pool_matrix_group
    ADD CONSTRAINT pool_matrix_group_pkey PRIMARY KEY (peptide_pool_id, peptide_group_id);

ALTER TABLE ONLY peptide.protein_category
    ADD CONSTRAINT protein_category_pkey PRIMARY KEY (protein_cat_id);

ALTER TABLE ONLY peptide.replicate_history
    ADD CONSTRAINT replicate_history_history_id_pkey PRIMARY KEY (history_id);

ALTER TABLE ONLY peptide.source
    ADD CONSTRAINT source_pkey PRIMARY KEY (peptide_id, peptide_group_id);

ALTER TABLE ONLY peptide.peptide_pool
    ADD CONSTRAINT fk_peptidepool_status FOREIGN KEY ("exists") REFERENCES peptide.peptide_status(qc_passed);

ALTER TABLE ONLY peptide.peptides
    ADD CONSTRAINT fk_peptides_status FOREIGN KEY (qc_passed) REFERENCES peptide.peptide_status(qc_passed);

ALTER TABLE ONLY peptide.group_matrix_peptides
    ADD CONSTRAINT group_matrix_peptides_history_id_fkey FOREIGN KEY (history_id) REFERENCES peptide.replicate_history(history_id);

ALTER TABLE ONLY peptide.group_matrix_peptides
    ADD CONSTRAINT group_matrix_peptides_matrix_pool_pep_id_fkey FOREIGN KEY (matrix_pool_pep_id) REFERENCES peptide.matrix_pool_peptide(matrix_pool_pep_id);

ALTER TABLE ONLY peptide.group_matrix_peptides
    ADD CONSTRAINT group_matrix_peptides_peptide_group_id_fkey FOREIGN KEY (peptide_group_id) REFERENCES peptide.peptide_group(peptide_group_id);

ALTER TABLE ONLY peptide.group_matrix_peptides
    ADD CONSTRAINT group_matrix_peptides_peptide_id_fkey FOREIGN KEY (peptide_id) REFERENCES peptide.peptides(peptide_id);

ALTER TABLE ONLY peptide.group_patient
    ADD CONSTRAINT group_patient_peptide_group_id_fkey FOREIGN KEY (peptide_group_id) REFERENCES peptide.peptide_group(peptide_group_id);

ALTER TABLE ONLY peptide.matrix_pool
    ADD CONSTRAINT matrix_pool_matrix_id_fkey FOREIGN KEY (matrix_id) REFERENCES peptide.matrices(matrix_id);

ALTER TABLE ONLY peptide.matrix_pool_peptide
    ADD CONSTRAINT matrix_pool_peptide_matrix_pool_id_fkey FOREIGN KEY (matrix_pool_id) REFERENCES peptide.matrix_pool(matrix_pool_id);

ALTER TABLE ONLY peptide.parent
    ADD CONSTRAINT parent_history_id_fkey FOREIGN KEY (history_id) REFERENCES peptide.replicate_history(history_id);

ALTER TABLE ONLY peptide.parent
    ADD CONSTRAINT parent_peptide_id_fkey FOREIGN KEY (peptide_id) REFERENCES peptide.peptides(peptide_id);

ALTER TABLE ONLY peptide.peptide_group
    ADD CONSTRAINT peptide_group_clade_id_fkey FOREIGN KEY (clade_id) REFERENCES peptide.clade(clade_id);

ALTER TABLE ONLY peptide.peptide_group
    ADD CONSTRAINT peptide_group_group_type_id_fkey FOREIGN KEY (group_type_id) REFERENCES peptide.group_type(group_type_id);

ALTER TABLE ONLY peptide.peptide_group
    ADD CONSTRAINT peptide_group_pathogen_id_fkey FOREIGN KEY (pathogen_id) REFERENCES peptide.pathogen(pathogen_id);

ALTER TABLE ONLY peptide.peptide_pool_assignment
    ADD CONSTRAINT peptide_pool_assignment_history_id_fkey FOREIGN KEY (history_id) REFERENCES peptide.replicate_history(history_id);

ALTER TABLE ONLY peptide.peptide_pool_assignment
    ADD CONSTRAINT peptide_pool_assignment_peptide_id_fkey FOREIGN KEY (peptide_id) REFERENCES peptide.peptides(peptide_id);

ALTER TABLE ONLY peptide.peptide_pool_assignment
    ADD CONSTRAINT peptide_pool_assignment_peptide_pool_id_fkey FOREIGN KEY (peptide_pool_id) REFERENCES peptide.peptide_pool(peptide_pool_id);

ALTER TABLE ONLY peptide.peptides
    ADD CONSTRAINT peptides_protein_cat_id_fkey FOREIGN KEY (protein_cat_id) REFERENCES peptide.protein_category(protein_cat_id);

ALTER TABLE ONLY peptide.pool_matrix_group
    ADD CONSTRAINT pool_matrix_group_matrix_pool_id_fkey FOREIGN KEY (matrix_pool_id) REFERENCES peptide.matrix_pool(matrix_pool_id);

ALTER TABLE ONLY peptide.pool_matrix_group
    ADD CONSTRAINT pool_matrix_group_peptide_group_id_fkey FOREIGN KEY (peptide_group_id) REFERENCES peptide.peptide_group(peptide_group_id);

ALTER TABLE ONLY peptide.pool_matrix_group
    ADD CONSTRAINT pool_matrix_group_peptide_pool_id_fkey FOREIGN KEY (peptide_pool_id) REFERENCES peptide.peptide_pool(peptide_pool_id);

ALTER TABLE ONLY peptide.replicate_history
    ADD CONSTRAINT replicate_history_master_peptide_id_fkey FOREIGN KEY (master_peptide_id) REFERENCES peptide.peptides(peptide_id);

ALTER TABLE ONLY peptide.source
    ADD CONSTRAINT source_history_id_fkey FOREIGN KEY (history_id) REFERENCES peptide.replicate_history(history_id);

ALTER TABLE ONLY peptide.source
    ADD CONSTRAINT source_peptide_group_id_fkey FOREIGN KEY (peptide_group_id) REFERENCES peptide.peptide_group(peptide_group_id);

ALTER TABLE ONLY peptide.source
    ADD CONSTRAINT source_peptide_id_fkey FOREIGN KEY (peptide_id) REFERENCES peptide.peptides(peptide_id);

ALTER TABLE ONLY peptide.source
    ADD CONSTRAINT source_transmitted_status_fkey FOREIGN KEY (transmitted_status) REFERENCES peptide.transmitted_status(transmitted_status);


SELECT pg_catalog.setval('peptide.replicate_history_history_id_seq', 18, true);


INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.069902', 1121, '2007-05-03 10:28:22.069902', '2007-05-03 10:28:22.069902', 1, 'A');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.072292', 1121, '2007-05-03 10:28:22.072292', '2007-05-03 10:28:22.072292', 2, 'B');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.073539', 1121, '2007-05-03 10:28:22.073539', '2007-05-03 10:28:22.073539', 3, 'C');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.074661', 1121, '2007-05-03 10:28:22.074661', '2007-05-03 10:28:22.074661', 4, 'D');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.075787', 1121, '2007-05-03 10:28:22.075787', '2007-05-03 10:28:22.075787', 5, 'E');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.076912', 1121, '2007-05-03 10:28:22.076912', '2007-05-03 10:28:22.076912', 6, 'G');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.078919', 1121, '2007-05-03 10:28:22.078919', '2007-05-03 10:28:22.078919', 7, 'M');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.081562', 1121, '2007-05-03 10:28:22.081562', '2007-05-03 10:28:22.081562', 8, 'Other');
INSERT INTO peptide.clade VALUES ('2007-05-03 10:28:22.083282', 1121, '2007-05-03 10:28:22.083282', '2007-05-03 10:28:22.083282', 9, 'Unknown');
INSERT INTO peptide.clade VALUES ('2008-11-05 09:17:23.130038', 1121, '2008-11-05 09:17:23.130038', '2008-11-05 09:17:23.130038', 10, 'A1');
INSERT INTO peptide.clade VALUES ('2008-11-05 09:17:48.596607', 1121, '2008-11-05 09:17:48.596607', '2008-11-05 09:17:48.596607', 11, 'C/A1/D');
INSERT INTO peptide.clade VALUES ('2008-11-05 09:18:20.602261', 1121, '2008-11-05 09:18:20.602261', '2008-11-05 09:18:20.602261', 12, 'D/A1');
INSERT INTO peptide.clade VALUES ('2011-06-07 08:55:20.602261', 1121, '2011-06-07 08:55:20.602261', '2011-06-07 08:55:20.602261', 13, 'A1C');
INSERT INTO peptide.clade VALUES ('2011-06-07 08:55:20.602261', 1121, '2011-06-07 08:55:20.602261', '2011-06-07 08:55:20.602261', 14, 'A1A2/D');
INSERT INTO peptide.clade VALUES ('2011-09-19 09:27:55.846849', 1121, '2011-09-19 09:27:55.846849', '2011-09-19 09:27:55.846849', 15, 'CRF01-AE');
INSERT INTO peptide.clade VALUES ('2011-09-19 09:28:31.013248', 1121, '2011-09-19 09:28:31.013248', '2011-09-19 09:28:31.013248', 16, 'CA');

INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.505839', 1121, '2007-05-03 10:28:22.505839', '2007-05-03 10:28:22.505839', 1, 'Consensus');
INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.553699', 1121, '2007-05-03 10:28:22.553699', '2007-05-03 10:28:22.553699', 2, 'Autologous');
INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.556104', 1121, '2007-05-03 10:28:22.556104', '2007-05-03 10:28:22.556104', 3, 'Mosaic');
INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.557968', 1121, '2007-05-03 10:28:22.557968', '2007-05-03 10:28:22.557968', 4, 'Toggle');
INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.559966', 1121, '2007-05-03 10:28:22.559966', '2007-05-03 10:28:22.559966', 5, 'LABL CTL Epitope');
INSERT INTO peptide.group_type VALUES ('2007-05-03 10:28:22.562088', 1121, '2007-05-03 10:28:22.562088', '2007-05-03 10:28:22.562088', 6, 'Other');

INSERT INTO peptide.matrices VALUES ('2007-05-03 10:32:41.64996', 1121, '2007-05-03 10:32:41.64996', '2007-05-03 10:32:41.64996', 'vm001', 420, 21, 4, NULL, '80', NULL, NULL);
INSERT INTO peptide.matrices VALUES ('2007-05-03 10:32:48.22687', 1121, '2007-05-03 10:32:48.22687', '2007-05-03 10:32:48.22687', 'vm002', 420, 10, 3, NULL, '126', NULL, NULL);
INSERT INTO peptide.matrices VALUES ('2011-09-15 10:20:03.189721', 1121, '2011-09-15 10:20:03.189721', '2011-09-15 10:20:03.189721', 'vm003', 420, 10, 3, NULL, '126', NULL, 'New layout to replace #357 with other peptide numbers as per Michaels request');

INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.015989', 1121, '2007-05-03 10:28:22.015989', '2007-05-03 10:28:22.015989', 1, 'HIV-1');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.018478', 1121, '2007-05-03 10:28:22.018478', '2007-05-03 10:28:22.018478', 2, 'HIV-2');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.019726', 1121, '2007-05-03 10:28:22.019726', '2007-05-03 10:28:22.019726', 3, 'TB');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.02085', 1121, '2007-05-03 10:28:22.02085', '2007-05-03 10:28:22.02085', 4, 'Malaria');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.02198', 1121, '2007-05-03 10:28:22.02198', '2007-05-03 10:28:22.02198', 5, 'Flu');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.023012', 1121, '2007-05-03 10:28:22.023012', '2007-05-03 10:28:22.023012', 6, 'FEC');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.024393', 1121, '2007-05-03 10:28:22.024393', '2007-05-03 10:28:22.024393', 7, 'EBV');
INSERT INTO peptide.pathogen VALUES ('2007-05-03 10:28:22.025514', 1121, '2007-05-03 10:28:22.025514', '2007-05-03 10:28:22.025514', 8, 'Other');


INSERT INTO peptide.peptide_status VALUES ('n', 'New');
INSERT INTO peptide.peptide_status VALUES ('s', 'Success');
INSERT INTO peptide.peptide_status VALUES ('f', 'Failed');
INSERT INTO peptide.peptide_status VALUES ('t', 'Terminated');
INSERT INTO peptide.peptide_status VALUES ('o', 'Ordered');


INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.905973', 1121, '2007-05-03 10:28:21.905973', '2007-05-03 10:28:21.905973', 1, 'GAG', 'G', 1000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.910582', 1121, '2007-05-03 10:28:21.910582', '2007-05-03 10:28:21.910582', 2, 'POL', 'P', 2000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.911949', 1121, '2007-05-03 10:28:21.911949', '2007-05-03 10:28:21.911949', 3, 'VIF', 'F', 3000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.9132', 1121, '2007-05-03 10:28:21.9132', '2007-05-03 10:28:21.9132', 4, 'VPR', 'V', 4000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.914324', 1121, '2007-05-03 10:28:21.914324', '2007-05-03 10:28:21.914324', 5, 'TAT', 'T', 5000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.915481', 1121, '2007-05-03 10:28:21.915481', '2007-05-03 10:28:21.915481', 6, 'REV', 'R', 6000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.916704', 1121, '2007-05-03 10:28:21.916704', '2007-05-03 10:28:21.916704', 7, 'VPU', 'U', 7000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.917953', 1121, '2007-05-03 10:28:21.917953', '2007-05-03 10:28:21.917953', 8, 'ENV', 'E', 8000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.919327', 1121, '2007-05-03 10:28:21.919327', '2007-05-03 10:28:21.919327', 9, 'NEF', 'N', 9000);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.941341', 1121, '2007-05-03 10:28:21.941341', '2007-05-03 10:28:21.941341', 24, 'Other', 'O', 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.920452', 1121, '2007-05-03 10:28:21.920452', '2007-05-03 10:28:21.920452', 10, 'gp160', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.921831', 1121, '2007-05-03 10:28:21.921831', '2007-05-03 10:28:21.921831', 11, 'Integrase', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.92308', 1121, '2007-05-03 10:28:21.92308', '2007-05-03 10:28:21.92308', 12, 'p17', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.92433', 1121, '2007-05-03 10:28:21.92433', '2007-05-03 10:28:21.92433', 13, 'Antigen 85A', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.925597', 1121, '2007-05-03 10:28:21.925597', '2007-05-03 10:28:21.925597', 14, 'BZLF 1', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.926836', 1121, '2007-05-03 10:28:21.926836', '2007-05-03 10:28:21.926836', 15, 'CFP10', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.92821', 1121, '2007-05-03 10:28:21.92821', '2007-05-03 10:28:21.92821', 16, 'EBNA3A', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.929585', 1121, '2007-05-03 10:28:21.929585', '2007-05-03 10:28:21.929585', 17, 'ESAT-6', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.930834', 1121, '2007-05-03 10:28:21.930834', '2007-05-03 10:28:21.930834', 18, 'IE1', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.93221', 1121, '2007-05-03 10:28:21.93221', '2007-05-03 10:28:21.93221', 19, 'p24', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.934087', 1121, '2007-05-03 10:28:21.934087', '2007-05-03 10:28:21.934087', 20, 'p2p7p1p6', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.935356', 1121, '2007-05-03 10:28:21.935356', '2007-05-03 10:28:21.935356', 21, 'pp65', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.938849', 1121, '2007-05-03 10:28:21.938849', '2007-05-03 10:28:21.938849', 22, 'Protease', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-03 10:28:21.94009', 1121, '2007-05-03 10:28:21.94009', '2007-05-03 10:28:21.94009', 23, 'RT-Integrase', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-15 14:56:44.788388', 1121, '2007-05-15 14:56:44.788388', '2007-05-15 14:56:44.788388', 25, 'Gag_Pol_TF', NULL, 0);
INSERT INTO peptide.protein_category VALUES ('2007-05-15 14:56:44.946433', 1121, '2007-05-15 14:56:44.946433', '2007-05-15 14:56:44.946433', 26, 'RT', NULL, 0);

INSERT INTO peptide.transmitted_status VALUES ('T', 'Transmitted');
INSERT INTO peptide.transmitted_status VALUES ('NT', 'Non-Transmitted');
INSERT INTO peptide.transmitted_status VALUES ('N/A', 'Not Applicable');

INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.56662', 1001, '2007-05-03 10:28:54.56662', 1001, '2007-05-03 10:28:54.56662', 'CON_B', 1, '', 'LANL', 2, '', 'Korber', '18 overlap of 10', 1);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.56662', 1001, '2007-05-03 10:28:54.56662', 1001, '2007-05-03 10:28:54.56662', 'CON_C', 1, '', 'LANL', 3, '', 'Korber', '18 overlap of 10', 1);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.641923', 1001, '2007-05-03 10:28:54.641923', 1001, '2007-05-03 10:28:54.641923', 'CON_A', 1, '', 'LANL', 1, '', 'Korber', '18 overlap of 10', 1);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.643663', 1001, '2007-05-03 10:28:54.643663', 1001, '2007-05-03 10:28:54.643663', 'CON_D', 1, '', 'LANL', 4, '', 'Korber', '18 overlap of 10', 1);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.645378', 1001, '2007-05-03 10:28:54.645378', 1001, '2007-05-03 10:28:54.645378', 'MM33d12', 1, '', 'Hahn', 2, '', 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.650389', 1001, '2007-05-03 10:28:54.650389', 1001, '2007-05-03 10:28:54.650389', 'MM39d11', 1, '', 'Hahn', 2, '', 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.652372', 1001, '2007-05-03 10:28:54.652372', 1001, '2007-05-03 10:28:54.652372', 'C1mos', 1, '', 'Korber', 3, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.653991', 1001, '2007-05-03 10:28:54.653991', 1001, '2007-05-03 10:28:54.653991', 'C2mos', 1, '', 'Korber', 3, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.656064', 1001, '2007-05-03 10:28:54.656064', 1001, '2007-05-03 10:28:54.656064', 'C3mos', 1, '', 'Korber', 3, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.658558', 1001, '2007-05-03 10:28:54.658558', 1001, '2007-05-03 10:28:54.658558', 'C4mos', 1, '', 'Korber', 3, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.660228', 1001, '2007-05-03 10:28:54.660228', 1001, '2007-05-03 10:28:54.660228', 'B1mos', 1, '', 'Korber', 2, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.66195', 1001, '2007-05-03 10:28:54.66195', 1001, '2007-05-03 10:28:54.66195', 'B2mos', 1, '', 'Korber', 2, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.663584', 1001, '2007-05-03 10:28:54.663584', 1001, '2007-05-03 10:28:54.663584', 'B3mos', 1, '', 'Korber', 2, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.665495', 1001, '2007-05-03 10:28:54.665495', 1001, '2007-05-03 10:28:54.665495', 'B4mos', 1, '', 'Korber', 2, '', '', '', 3);
INSERT INTO peptide.peptide_group VALUES ('2007-05-03 10:28:54.667107', 1001, '2007-05-03 10:28:54.667107', 1001, '2007-05-03 10:28:54.667107', 'FEC', 5, '', '', 9, '', '', '', 1);
INSERT INTO peptide.peptide_group VALUES ('2007-07-09 09:00:02.465418', 1001, '2007-07-09 09:00:02.465418', 1001, '2007-07-09 09:00:02.465418', 'MM43d21', 1, NULL, NULL, 9, NULL, NULL, NULL, 2);
INSERT INTO peptide.peptide_group VALUES ('2007-07-09 09:00:02.541101', 1001, '2007-07-09 09:00:02.541101', 1001, '2007-07-09 09:00:02.541101', 'MM42d22', 1, NULL, NULL, 9, NULL, NULL, NULL, 2);
INSERT INTO peptide.peptide_group VALUES ('2007-07-09 09:00:02.853696', 1001, '2007-07-09 09:00:02.853696', 1001, '2007-07-09 09:00:02.853696', 'MM45d22', 1, NULL, NULL, 9, NULL, NULL, NULL, 2);
INSERT INTO peptide.peptide_group VALUES ('2007-07-25 14:23:47.714944', 1001, '2007-07-25 14:23:47.714944', 1001, '2007-07-25 14:23:47.714944', 'CTL Epitopes', NULL, NULL, NULL, 9, NULL, NULL, NULL, 5);
INSERT INTO peptide.peptide_group VALUES ('2008-01-29 13:14:26.090745', 1001, '2008-01-29 13:14:26.090745', 1001, '2008-01-29 13:14:26.090745', '703010010', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-01-29 13:14:26.108698', 1001, '2008-01-29 13:14:26.108698', 1001, '2008-01-29 13:14:26.108698', '703010054', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-01-29 13:14:26.114389', 1001, '2008-01-29 13:14:26.114389', 1001, '2008-01-29 13:14:26.114389', '703010131', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-09-16 14:18:22.859852', 1001, '2008-09-16 14:18:22.859852', 1001, '2008-09-16 14:18:22.859852', '705010078', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-09-16 14:19:29.297634', 1001, '2008-09-16 14:19:29.297634', 1001, '2008-09-16 14:19:29.297634', '704010042', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-09-16 14:22:03.747447', 1001, '2008-09-16 14:22:03.747447', 1001, '2008-09-16 14:22:03.747447', '703010159', 1, NULL, 'Gao', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-11-05 09:19:25.826', 1001, '2008-11-05 09:19:25.826', 1001, '2008-11-05 09:19:25.826', '191084CON', 1, NULL, 'Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-11-05 09:21:48.518', 1001, '2008-11-05 09:21:48.518', 1001, '2008-11-05 09:21:48.518', '192018CON', 1, NULL, 'Shaw', 11, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-11-05 09:22:48.101', 1001, '2008-11-05 09:22:48.101', 1001, '2008-11-05 09:22:48.101', '270015CON', 1, NULL, 'Shaw', 12, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-11-05 09:23:39.752', 1001, '2008-11-05 09:23:39.752', 1001, '2008-11-05 09:23:39.752', 'R18553FCON', 1, NULL, 'Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2008-11-05 09:24:39.977', 1001, '2008-11-05 09:24:39.977', 1001, '2008-11-05 09:24:39.977', 'R66201FCON', 1, NULL, 'Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-22 03:01:02.578', 1001, '2011-09-19 07:22:02.339', 1001, '2011-09-22 03:01:02.578', '20225v01', 1, NULL, 'MHRP-Tovanabutra', 16, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-04-02 14:08:45.452137', 1001, '2009-04-02 14:08:45.452137', 1001, '2009-04-02 14:08:45.452137', '705010162', 1, '', 'Gao', 3, '', 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-04-02 14:09:55.58975', 1001, '2009-04-02 14:09:55.58975', 1001, '2009-04-02 14:09:55.58975', '705010198', 1, '', 'Gao', 3, '', 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-04-02 14:11:03.006158', 1001, '2009-04-02 14:11:03.006158', 1001, '2009-04-02 14:11:03.006158', '703010256', 1, '', 'Gao', 3, '', 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-09-23 09:33:34.156', 1101, '2009-09-23 09:33:34.156', 1101, '2009-09-23 09:33:34.156', '706010164', 1, NULL, 'GAO', 3, 'version 1', 'KORBER', '18 OVERLAP OF 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-09-23 09:35:27.726', 1101, '2009-09-23 09:35:27.726', 1101, '2009-09-23 09:35:27.726', '705010067', 1, NULL, 'KORBER', 3, 'Version 1', 'GAO', '18 OVERLAP OF 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2009-09-23 09:36:50.767', 1101, '2009-09-23 09:36:50.767', 1101, '2009-09-23 09:36:50.767', '705010185', 1, NULL, 'KORBER', 3, 'VERSION 1', 'GAO', '18 OVERLAP BY 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2010-06-30 11:19:02.458', 1001, '2010-06-30 11:19:02.458', 1001, '2010-06-30 11:19:02.458', '703010850', 1, NULL, 'Hahn', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2010-06-30 11:20:38.67', 1001, '2010-06-30 11:20:38.67', 1001, '2010-06-30 11:20:38.67', '705010569', 1, NULL, 'Hahn', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2010-06-30 11:21:28.778', 1001, '2010-06-30 11:21:28.778', 1001, '2010-06-30 11:21:28.778', '704010236', 1, NULL, 'Hahn', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:05:25.495', 1001, '2011-06-03 03:05:25.495', 1001, '2011-06-03 03:05:25.495', '00C210066CON', 1, NULL, 'Hahn/Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:12:45.654', 1001, '2011-06-03 03:12:45.654', 1001, '2011-06-03 03:12:45.654', '00B12151802CON', 1, NULL, 'Hahn/Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:19:22.509', 1001, '2011-06-03 03:19:22.509', 1001, '2011-06-03 03:19:22.509', 'R166017M', 1, NULL, 'Hahn/Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:21:04.712', 1001, '2011-06-03 03:21:04.712', 1001, '2011-06-03 03:21:04.712', 'R15993F', 1, NULL, 'Hahn/Shaw', 10, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:24:12.861', 1001, '2011-06-03 03:24:12.861', 1001, '2011-06-03 03:24:12.861', '703011432', 1, NULL, 'Swanstrom', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:25:22.988', 1001, '2011-06-03 03:25:22.988', 1001, '2011-06-03 03:25:22.988', '705010325', 1, NULL, 'Swanstrom', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:27:38.951', 1001, '2011-06-03 03:27:38.951', 1001, '2011-06-03 03:27:38.951', '705010264', 1, NULL, 'Swanstrom', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:29:57.555', 1001, '2011-06-03 03:29:57.555', 1001, '2011-06-03 03:29:57.555', '705010107', 1, NULL, 'Hahn/Shaw', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-03 03:32:04.954', 1001, '2011-06-03 03:32:04.954', 1001, '2011-06-03 03:32:04.954', '703010752', 1, NULL, 'Swanstrom', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-15 10:41:52.026', 1001, '2011-06-03 03:15:30.632', 1001, '2011-06-15 10:41:52.026', '00B2100950CON', 1, NULL, 'Hahn/Shaw', 14, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-06-15 10:42:55.651', 1001, '2011-06-03 03:09:41.883', 1001, '2011-06-15 10:42:55.651', '00C210032CON', 1, NULL, 'Hahn/Shaw', 13, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:43:21.18', 1001, '2007-05-03 10:29:39.452', 1001, '2011-09-19 07:43:21.18', '700010058', 1, NULL, 'Hahn/Shaw', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:33:44.299', 1001, '2009-04-02 14:06:33.722', 1001, '2011-09-19 07:33:44.299', '700010470', 1, NULL, 'Hahn/Shaw', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:38:42.06', 1001, '2007-05-03 10:28:55.359', 1001, '2011-09-19 07:38:42.06', 'CAP239', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:39:27.951', 1001, '2007-08-30 09:03:36.937', 1001, '2011-09-19 07:39:27.951', 'CAP45', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:36:56.722', 1001, '2007-08-30 09:03:36.88', 1001, '2011-09-19 07:36:56.722', 'CAP210', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap by 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:38:05.133', 1001, '2007-08-30 09:03:36.816', 1001, '2011-09-19 07:38:05.133', 'CAP228', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:40:27.559', 1001, '2007-08-30 09:03:36.238', 1001, '2011-09-19 07:40:27.559', 'CAP63', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:41:00.7', 1001, '2007-08-30 09:03:36.797', 1001, '2011-09-19 07:41:00.7', 'CAP69', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:41:34.519', 1001, '2007-05-03 10:28:55.351', 1001, '2011-09-19 07:41:34.519', 'CAP85', 1, NULL, 'Williamson', 3, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:42:48.831', 1001, '2007-05-03 10:29:39.355', 1001, '2011-09-19 07:42:48.831', '700010040', 1, NULL, 'Hahn/Shaw', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:43:54.23', 1001, '2007-05-03 10:29:39.367', 1001, '2011-09-19 07:43:54.23', '700010077', 1, NULL, 'Hahn/Shaw', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:32:51.67', 1001, '2009-04-02 14:11:56.425', 1001, '2011-09-19 07:32:51.67', '700010423', 1, NULL, 'Gao', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-19 07:41:57.864', 1001, '2009-04-02 14:07:31.881', 1001, '2011-09-19 07:41:57.864', '700010607', 1, NULL, 'Hahn/Shaw', 2, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-22 03:01:22.891', 1001, '2011-09-19 07:45:39.874', 1001, '2011-09-22 03:01:22.891', '40061v03', 1, NULL, 'MHRP-Tovanabutra', 15, NULL, 'Korber', '18 overlap of 10', 2);
INSERT INTO peptide.peptide_group VALUES ('2011-09-22 03:01:48.209', 1001, '2011-09-19 07:46:26.819', 1001, '2011-09-22 03:01:48.209', '40100v01', 1, NULL, 'MHRP-Tovanabutra', 15, NULL, 'Korber', '18 overlap of 10', 2);

