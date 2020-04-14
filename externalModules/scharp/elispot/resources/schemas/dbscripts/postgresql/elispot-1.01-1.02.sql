INSERT INTO elispot.tblbatchtype VALUES ('E', 'End point');
INSERT INTO elispot.tblbatchtype VALUES ('P', 'Proficiency');
INSERT INTO elispot.tblbatchtype VALUES ('V', 'Validation');
INSERT INTO elispot.tblbatchtype VALUES ('D', 'R & D');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('elispot.tblcellcounter', 'counter_seq_id'), 2, true);


INSERT INTO elispot.tblcellcounter VALUES (1, 'Guava Cell Counter');
INSERT INTO elispot.tblcellcounter VALUES (2, 'Coulter Vi Cell Counter');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('elispot.tblreaders', 'reader_seq_id'), 2, true);


INSERT INTO elispot.tblreaders VALUES (1, 'AID ELISpot Reader (TXT)', 'AID-TXT', 'TXT');
INSERT INTO elispot.tblreaders VALUES (2, 'CTL ImmunoSpot Reader (XLS)', 'CTL-XLS', 'XLS');


SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('elispot.tbladditive', 'additive_seq_id'), 5, true);

INSERT INTO elispot.tbladditive VALUES (1, 'EDTA');
INSERT INTO elispot.tbladditive VALUES (2, 'ACD');
INSERT INTO elispot.tbladditive VALUES (3, 'NaHEP');
INSERT INTO elispot.tbladditive VALUES (4, 'LiHEP');
INSERT INTO elispot.tbladditive VALUES (5, 'NONE');

