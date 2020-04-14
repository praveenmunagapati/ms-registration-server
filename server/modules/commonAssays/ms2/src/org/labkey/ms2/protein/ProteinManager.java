/*
 * Copyright (c) 2005-2018 Fred Hutchinson Cancer Research Center
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

package org.labkey.ms2.protein;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.collections.CaseInsensitiveHashSet;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.data.dialect.SqlDialect;
import org.labkey.api.exp.DomainNotFoundException;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.settings.PreferenceService;
import org.labkey.api.util.HashHelpers;
import org.labkey.api.util.UnexpectedException;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.NotFoundException;
import org.labkey.ms2.MS2Controller;
import org.labkey.ms2.MS2Manager;
import org.labkey.ms2.MS2Peptide;
import org.labkey.ms2.MS2Run;
import org.labkey.ms2.Protein;
import org.labkey.ms2.protein.fasta.FastaFile;
import org.labkey.ms2.protein.fasta.PeptideGenerator;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: arauch
 * Date: Mar 23, 2005
 * Time: 9:58:17 PM
 */
public class ProteinManager
{
    private static final Logger LOG = Logger.getLogger(ProteinManager.class);
    private static final String SCHEMA_NAME = "prot";

    public static final int RUN_FILTER = 1;
    public static final int URL_FILTER = 2;
    public static final int EXTRA_FILTER = 4;
    public static final int PROTEIN_FILTER = 8;
    public static final int ALL_FILTERS = RUN_FILTER + URL_FILTER + EXTRA_FILTER + PROTEIN_FILTER;
    private static final String ALL_PEPTIDES_PREFERENCE_NAME = ProteinManager.class.getName() + "." + MS2Controller.ProteinViewBean.ALL_PEPTIDES_URL_PARAM;

    public static String getSchemaName()
    {
        return SCHEMA_NAME;
    }


    public static DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME, DbSchemaType.Module);
    }


    public static SqlDialect getSqlDialect()
    {
        return getSchema().getSqlDialect();
    }


    public static TableInfo getTableInfoFastaFiles()
    {
        return getSchema().getTable("FastaFiles");
    }


    public static TableInfo getTableInfoFastaSequences()
    {
        return getSchema().getTable("FastaSequences");
    }


    public static TableInfo getTableInfoFastaAdmin()
    {
        return getSchema().getTable("FastaAdmin");
    }


    public static TableInfo getTableInfoAnnotInsertions()
    {
        return getSchema().getTable("AnnotInsertions");
    }


    public static TableInfo getTableInfoCustomAnnotation()
    {
        return getSchema().getTable("CustomAnnotation");
    }

    public static TableInfo getTableInfoCustomAnnotationSet()
    {
        return getSchema().getTable("CustomAnnotationSet");
    }

    public static TableInfo getTableInfoAnnotations()
    {
        return getSchema().getTable("Annotations");
    }


    public static TableInfo getTableInfoAnnotationTypes()
    {
        return getSchema().getTable("AnnotationTypes");
    }


    public static TableInfo getTableInfoIdentifiers()
    {
        return getSchema().getTable("Identifiers");
    }


    public static TableInfo getTableInfoIdentTypes()
    {
        return getSchema().getTable("IdentTypes");
    }


    public static TableInfo getTableInfoOrganisms()
    {
        return getSchema().getTable("Organisms");
    }


    public static TableInfo getTableInfoInfoSources()
    {
        return getSchema().getTable("InfoSources");
    }


    public static TableInfo getTableInfoSequences()
    {
        return getSchema().getTable("Sequences");
    }


    public static TableInfo getTableInfoFastaLoads()
    {
        return getSchema().getTable("FastaLoads");
    }


    public static TableInfo getTableInfoSprotOrgMap()
    {
        return getSchema().getTable("SprotOrgMap");
    }

    public static TableInfo getTableInfoGoTerm()
    {
        return getSchema().getTable("GoTerm");
    }

    public static TableInfo getTableInfoGoTerm2Term()
    {
        return getSchema().getTable("GoTerm2Term");
    }

    public static TableInfo getTableInfoGoGraphPath()
    {
        return getSchema().getTable("GoGraphPath");
    }

    public static TableInfo getTableInfoGoTermDefinition()
    {
        return getSchema().getTable("GoTermDefinition");
    }

    public static TableInfo getTableInfoGoTermSynonym()
    {
        return getSchema().getTable("GoTermSynonym");
    }


    public static Protein getProtein(int seqId)
    {
        return new SqlSelector(getSchema(),
                "SELECT SeqId, ProtSequence AS Sequence, Mass, Description, BestName, BestGeneName FROM " + getTableInfoSequences() + " WHERE SeqId = ?",
                seqId).getObject(Protein.class);
    }

    public static Protein getProtein(String sequence, int organismId)
    {
        return new SqlSelector(getSchema(),
                "SELECT SeqId, ProtSequence AS Sequence, Mass, Description, BestName, BestGeneName FROM " + getTableInfoSequences() + " WHERE Hash = ? AND OrgId = ?",
                hashSequence(sequence), organismId).getObject(Protein.class);
    }

    public static List<Protein> getProtein(String sequence)
    {
        return new SqlSelector(getSchema(),
                "SELECT SeqId, ProtSequence AS Sequence, Mass, Description, BestName, BestGeneName FROM " + getTableInfoSequences() + " WHERE Hash = ?",
                hashSequence(sequence)).getArrayList(Protein.class);
    }

    public static String getProteinSequence(int seqId)
    {
        Protein protein = getProtein(seqId);
        return protein != null ? protein.getSequence() : null;
    }

    public static List<Protein> getProteinsContainingPeptide(MS2Peptide peptide, int... fastaIds)
    {
        if ((null == peptide) || ("".equals(peptide.getTrimmedPeptide())) || (peptide.getProteinHits() < 1))
            return Collections.emptyList();

        int hits = peptide.getProteinHits();
        SQLFragment sql = new SQLFragment();
        if (hits == 1 && peptide.getSeqId() != null)
        {
            sql.append("SELECT SeqId, ProtSequence AS Sequence, Mass, Description, ? AS BestName, BestGeneName FROM ");
            sql.append(getTableInfoSequences(), "s");
            sql.append(" WHERE SeqId = ?");
            sql.add(peptide.getProtein());
            sql.add(peptide.getSeqId());
        }
        else
        {
            // TODO: make search tryptic so that number that match = ProteinHits.
            sql.append("SELECT s.SeqId, s.ProtSequence AS Sequence, s.Mass, s.Description, fs.LookupString AS BestName, s.BestGeneName FROM ");
            sql.append(getTableInfoSequences(), "s");
            sql.append(", ");
            sql.append(getTableInfoFastaSequences(), "fs");
            sql.append(" WHERE fs.SeqId = s.SeqId AND fs.FastaId IN (");
            sql.append(StringUtils.repeat("?", ", ", fastaIds.length));
            sql.append(") AND ProtSequence ");
            sql.append(getSqlDialect().getCharClassLikeOperator());
            sql.append(" ?" );
            for (int fastaId : fastaIds)
            {
                sql.add(fastaId);
            }
            sql.add("%" + peptide.getTrimmedPeptide() + "%");

            //based on observations of 2 larger ms2 databases, TOP 20 causes better query plan generation in SQL Server
            sql = getSchema().getSqlDialect().limitRows(sql, Math.max(20, hits));
        }

        List<Protein> proteins = new SqlSelector(getSchema(), sql).getArrayList(Protein.class);

        if (proteins.isEmpty())
            LOG.warn("getProteinsContainingPeptide: Could not find peptide " + peptide + " in FASTA files " + Arrays.asList(fastaIds));

        return proteins;
    }


    private static final NumberFormat generalFormat = new DecimalFormat("0.0#");

    public static FastaFile getFastaFile(int fastaId)
    {
        return new TableSelector(ProteinManager.getTableInfoFastaFiles()).getObject(fastaId, FastaFile.class);
    }

    public static void addExtraFilter(SimpleFilter filter, MS2Run run, ActionURL currentUrl)
    {
        String paramName = run.getChargeFilterParamName();

        boolean includeChargeFilter = false;
        Float[] values = new Float[3];

        for (int i = 0; i < values.length; i++)
        {
            String threshold = currentUrl.getParameter(paramName + (i + 1));

            if (null != threshold && !"".equals(threshold))
            {
                try
                {
                    values[i] = Float.parseFloat(threshold);  // Make sure this parses to a float
                    includeChargeFilter = true;
                }
                catch(NumberFormatException e)
                {
                    // Ignore any values that can't be converted to float -- leave them null
                }
            }
        }

        // Add charge filter only if there's one or more valid values
        if (includeChargeFilter && run.getChargeFilterColumnName() != null)
            filter.addClause(new ChargeFilter(FieldKey.fromString(run.getChargeFilterColumnName()), values));

        String tryptic = currentUrl.getParameter("tryptic");

        // Add tryptic filter
        if ("1".equals(tryptic))
            filter.addClause(new TrypticFilter(1));
        else if ("2".equals(tryptic))
            filter.addClause(new TrypticFilter(2));
    }

    public static Map<String, CustomAnnotationSet> getCustomAnnotationSets(Container container, boolean includeProject)
    {
        SQLFragment sql = new SQLFragment();
        sql.append("SELECT * FROM ");
        sql.append(getTableInfoCustomAnnotationSet());
        sql.append(" WHERE Container = ? ");
        sql.add(container.getId());
        if (includeProject)
        {
            Container project = container.getProject();
            if (project != null && !project.equals(container))
            {
                sql.append(" OR Container = ? ");
                sql.add(project.getId());
            }
        }
        sql.append(" ORDER BY Name");
        Collection<CustomAnnotationSet> allSets = new SqlSelector(getSchema(), sql).getCollection(CustomAnnotationSet.class);

        Set<String> setNames = new CaseInsensitiveHashSet();
        List<CustomAnnotationSet> dedupedSets = new ArrayList<>(allSets.size());
        // If there are any name collisions, we want sets in this container to mask the ones in the project

        // Take a first pass through to add all the ones from this container
        for (CustomAnnotationSet set : allSets)
        {
            if (set.getContainer().equals(container.getId()))
            {
                setNames.add(set.getName());
                dedupedSets.add(set);
            }
        }

        // Take a second pass through to add all the ones from the project that don't collide
        for (CustomAnnotationSet set : allSets)
        {
            if (!set.getContainer().equals(container.getId()) && setNames.add(set.getName()))
            {
                dedupedSets.add(set);
            }
        }

        dedupedSets.sort(Comparator.comparing(CustomAnnotationSet::getName));
        Map<String, CustomAnnotationSet> result = new LinkedHashMap<>();
        for (CustomAnnotationSet set : dedupedSets)
        {
            result.put(set.getName(), set);
        }
        return result;
    }

    public static void deleteCustomAnnotationSet(CustomAnnotationSet set)
    {
        try
        {
            Container c = ContainerManager.getForId(set.getContainer());
            if (OntologyManager.getDomainDescriptor(set.getLsid(), c) != null)
            {
                OntologyManager.deleteOntologyObject(set.getLsid(), c, true);
                OntologyManager.deleteDomain(set.getLsid(), c);
            }
        }
        catch (DomainNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        try (DbScope.Transaction transaction = getSchema().getScope().ensureTransaction())
        {
            new SqlExecutor(getSchema()).execute("DELETE FROM " + getTableInfoCustomAnnotation() + " WHERE CustomAnnotationSetId = ?", set.getCustomAnnotationSetId());
            Table.delete(getTableInfoCustomAnnotationSet(), set.getCustomAnnotationSetId());
            transaction.commit();
        }
    }

    public static CustomAnnotationSet getCustomAnnotationSet(Container c, int id, boolean includeProject)
    {
        SQLFragment sql = new SQLFragment();
        sql.append("SELECT * FROM ");
        sql.append(getTableInfoCustomAnnotationSet());
        sql.append(" WHERE (Container = ?");
        sql.add(c.getId());
        if (includeProject)
        {
            sql.append(" OR Container = ?");
            sql.add(c.getProject().getId());
        }
        sql.append(") AND CustomAnnotationSetId = ?");
        sql.add(id);
        List<CustomAnnotationSet> matches = new SqlSelector(getSchema(), sql).getArrayList(CustomAnnotationSet.class);
        if (matches.size() > 1)
        {
            for (CustomAnnotationSet set : matches)
            {
                if (set.getContainer().equals(c.getId()))
                {
                    return set;
                }
            }
            assert false : "More than one matching set was found but none were in the current container";
            return matches.get(0);
        }
        if (matches.size() == 1)
        {
            return matches.get(0);
        }
        return null;
    }

    public static void migrateRuns(int oldFastaId, int newFastaId)
            throws SQLException
    {
        SQLFragment mappingSQL = new SQLFragment("SELECT fs1.seqid AS OldSeqId, fs2.seqid AS NewSeqId\n");
        mappingSQL.append("FROM \n");
        mappingSQL.append("\t(SELECT ff.SeqId, s.Hash, ff.LookupString FROM " + getTableInfoFastaSequences() + " ff, " + getTableInfoSequences() + " s WHERE ff.SeqId = s.SeqId AND ff.FastaId = " + oldFastaId + ") fs1 \n");
        mappingSQL.append("\tLEFT OUTER JOIN \n");
        mappingSQL.append("\t(SELECT ff.SeqId, s.Hash, ff.LookupString FROM " + getTableInfoFastaSequences() + " ff, " + getTableInfoSequences() + " s WHERE ff.SeqId = s.SeqId AND ff.FastaId = " + newFastaId + ") fs2 \n");
        mappingSQL.append("\tON (fs1.Hash = fs2.Hash AND fs1.LookupString = fs2.LookupString)");

        SQLFragment missingCountSQL = new SQLFragment("SELECT COUNT(*) FROM (");
        missingCountSQL.append(mappingSQL);
        missingCountSQL.append(") Mapping WHERE OldSeqId IN (\n");
        missingCountSQL.append("(SELECT p.SeqId FROM " + MS2Manager.getTableInfoPeptides() + " p, " + MS2Manager.getTableInfoRuns() + " r WHERE p.run = r.Run AND r.FastaId = " + oldFastaId + ")\n");
        missingCountSQL.append("UNION\n");
        missingCountSQL.append("(SELECT pgm.SeqId FROM ").append(MS2Manager.getTableInfoProteinGroupMemberships()).append(" pgm, ").append(MS2Manager.getTableInfoProteinGroups()).append(" pg, ").append(MS2Manager.getTableInfoProteinProphetFiles()).append(" ppf, ").append(MS2Manager.getTableInfoRuns()).append(" r WHERE pgm.ProteinGroupId = pg.RowId AND pg.ProteinProphetFileId = ppf.RowId AND ppf.Run = r.Run AND r.FastaId = ").append(oldFastaId).append("))\n");
        missingCountSQL.append("AND NewSeqId IS NULL");

        int missingCount = new SqlSelector(getSchema(), missingCountSQL).getObject(Integer.class);
        if (missingCount > 0)
        {
            throw new SQLException("There are " + missingCount + " protein sequences in the original FASTA file that are not in the new file");
        }

        SqlExecutor executor = new SqlExecutor(MS2Manager.getSchema());

        try (DbScope.Transaction transaction = MS2Manager.getSchema().getScope().ensureTransaction())
        {
            SQLFragment updatePeptidesSQL = new SQLFragment();
            updatePeptidesSQL.append("UPDATE " + MS2Manager.getTableInfoPeptidesData() + " SET SeqId = map.NewSeqId");
            updatePeptidesSQL.append("\tFROM " + MS2Manager.getTableInfoFractions() + " f \n");
            updatePeptidesSQL.append("\t, " + MS2Manager.getTableInfoRuns() + " r\n");
            updatePeptidesSQL.append("\t, " + MS2Manager.getTableInfoFastaRunMapping() + " frm\n");
            updatePeptidesSQL.append("\t, (");
            updatePeptidesSQL.append(mappingSQL);
            updatePeptidesSQL.append(") map \n");
            updatePeptidesSQL.append("WHERE f.Fraction = " + MS2Manager.getTableInfoPeptidesData() + ".Fraction\n");
            updatePeptidesSQL.append("\tAND r.Run = f.Run\n");
            updatePeptidesSQL.append("\tAND frm.Run = r.Run\n");
            updatePeptidesSQL.append("\tAND " + MS2Manager.getTableInfoPeptidesData() + ".SeqId = map.OldSeqId \n");
            updatePeptidesSQL.append("\tAND frm.FastaId = " + oldFastaId);

            executor.execute(updatePeptidesSQL);

            SQLFragment updateProteinsSQL = new SQLFragment();
            updateProteinsSQL.append("UPDATE " + MS2Manager.getTableInfoProteinGroupMemberships() + " SET SeqId= map.NewSeqId\n");
            updateProteinsSQL.append("FROM " + MS2Manager.getTableInfoProteinGroups() + " pg\n");
            updateProteinsSQL.append("\t, " + MS2Manager.getTableInfoProteinProphetFiles() + " ppf\n");
            updateProteinsSQL.append("\t, " + MS2Manager.getTableInfoRuns() + " r\n");
            updateProteinsSQL.append("\t, " + MS2Manager.getTableInfoFastaRunMapping() + " frm\n");
            updateProteinsSQL.append("\t, (");
            updateProteinsSQL.append(mappingSQL);
            updateProteinsSQL.append(") map \n");
            updateProteinsSQL.append("WHERE " + MS2Manager.getTableInfoProteinGroupMemberships() + ".ProteinGroupId = pg.RowId\n");
            updateProteinsSQL.append("\tAND pg.ProteinProphetFileId = ppf.RowId\n");
            updateProteinsSQL.append("\tAND r.Run = ppf.Run\n");
            updateProteinsSQL.append("\tAND frm.Run = r.Run\n");
            updateProteinsSQL.append("\tAND " + MS2Manager.getTableInfoProteinGroupMemberships() + ".SeqId = map.OldSeqId\n");
            updateProteinsSQL.append("\tAND frm.FastaId = " + oldFastaId);

            executor.execute(updateProteinsSQL);

            executor.execute("UPDATE " + MS2Manager.getTableInfoFastaRunMapping() + " SET FastaID = ? WHERE FastaID = ?", newFastaId, oldFastaId);
            transaction.commit();
        }
    }

    public static int ensureProtein(String sequence, String organismName, String name, String description)
    {
        Protein protein = ensureProteinInDatabase(sequence, organismName, name, description);
        return protein.getSeqId();
    }

    public static int ensureProtein(String sequence, int orgId, String name, String description)
    {
        Organism organism = new TableSelector(getTableInfoOrganisms()).getObject(orgId, Organism.class);
        if (organism == null)
            throw new IllegalArgumentException("Organism " + orgId + " does not exist");

        Protein protein = ensureProteinInDatabase(sequence, organism, name, description);
        return protein.getSeqId();
    }

    private static Protein ensureProteinInDatabase(String sequence, String organismName, String name, String description)
    {
        String genus = FastaDbLoader.extractGenus(organismName);
        String species = FastaDbLoader.extractSpecies(organismName);
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("species"), species);
        filter.addCondition(FieldKey.fromParts("genus"), genus);
        Organism organism = new TableSelector(getTableInfoOrganisms(), filter, null).getObject(Organism.class);
        if (organism == null)
        {
            organism = new Organism();
            organism.setGenus(genus);
            organism.setSpecies(species);
            organism = Table.insert(null, getTableInfoOrganisms(), organism);
        }

        return ensureProteinInDatabase(sequence, organism, name, description);
    }

    private static Protein ensureProteinInDatabase(String sequence, Organism organism, String name, String description)
    {
        Protein protein = getProtein(sequence, organism.getOrgId());
        if (protein == null)
        {
            Map<String, Object> map = new CaseInsensitiveHashMap<>();
            map.put("ProtSequence", sequence);
            byte[] sequenceBytes = getSequenceBytes(sequence);
            map.put("Mass", PeptideGenerator.computeMass(sequenceBytes, 0, sequenceBytes.length, PeptideGenerator.AMINO_ACID_AVERAGE_MASSES));
            map.put("OrgId", organism.getOrgId());
            map.put("Hash", hashSequence(sequence));
            map.put("Description", description == null ? null : (description.length() > 200 ? description.substring(0, 196) + "..." : description));
            map.put("BestName", name);
            map.put("Length", sequence.length());
            map.put("InsertDate", new Date());
            map.put("ChangeDate", new Date());

            Table.insert(null, getTableInfoSequences(), map);
            protein = getProtein(sequence, organism.getOrgId());
        }
        return protein;
    }

    public static void ensureIdentifiers(int seqId, Map<String, Set<String>> typeAndIdentifiers)
    {
        Protein protein = getProtein(seqId);
        if(protein == null)
        {
            throw new NotFoundException("SeqId " + seqId + " does not exist.");
        }
        ensureIdentifiers(protein, typeAndIdentifiers);
    }

    private static void ensureIdentifiers(Protein protein, Map<String, Set<String>> typeAndIdentifiers)
    {
        if(typeAndIdentifiers == null || typeAndIdentifiers.size() == 0)
        {
            return;
        }

        for(Map.Entry<String, Set<String>> typeAndIdentifier: typeAndIdentifiers.entrySet())
        {
            String identifierType = typeAndIdentifier.getKey();
            Set<String> identifiers = typeAndIdentifier.getValue();

            Integer identifierTypeId = ensureIdentifierType(identifierType);
            if(identifierTypeId == null)
                continue;

            for(String identifier: identifiers)
            {
                ensureIdentifier(protein, identifierTypeId, identifier);
            }
        }
    }

    private static void ensureIdentifier(Protein protein, Integer identifierTypeId, String identifier)
    {
        identifier = StringUtils.trimToNull(identifier);
        if(identifier == null || identifier.equalsIgnoreCase(protein.getBestName()))
        {
            return;
        }
        if(!identifierExists(identifier, identifierTypeId, protein.getSeqId()))
        {
           addIdentifier(identifier, identifierTypeId, protein.getSeqId());
        }
    }

    private static void addIdentifier(String identifier, int identifierTypeId, int seqId)
    {
        Map<String, Object> values = new HashMap<>();
        values.put("identifier", identifier);
        values.put("identTypeId", identifierTypeId);
        values.put("seqId", seqId);
        values.put("entryDate", new Date());
        Table.insert(null, getTableInfoIdentifiers(), values);
    }

    private static boolean identifierExists(String identifier, int identifierTypeId, int seqId)
    {
        SimpleFilter filter = new SimpleFilter();
        filter.addCondition(FieldKey.fromParts("identifier"), identifier);
        filter.addCondition(FieldKey.fromParts("identTypeId"), identifierTypeId);
        filter.addCondition(FieldKey.fromParts("seqId"), seqId);
        return new TableSelector(getTableInfoIdentifiers(), filter, null).exists();
    }

    @Nullable
    private static Integer ensureIdentifierType(String identifierType)
    {
        identifierType = StringUtils.trimToNull(identifierType);
        if(identifierType == null)
            return null;

        Integer identTypeId = new SqlSelector(getSchema(),
                            "SELECT MIN(identTypeId) FROM " + getTableInfoIdentTypes() + " WHERE LOWER(name) = ?",
                            identifierType.toLowerCase()).getObject(Integer.class);

        if(identTypeId == null)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("identTypeId", null);
            map.put("name", identifierType);
            map.put("entryDate", new Date());
            map = Table.insert(null, getTableInfoIdentTypes(), map);
            identTypeId = (Integer)map.get("identTypeId");
        }
        return identTypeId;
    }

    private static String hashSequence(String sequence)
    {
        return HashHelpers.hash(getSequenceBytes(sequence));
    }

    private static byte[] getSequenceBytes(String sequence)
    {
        byte[] bytes = sequence.getBytes();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(bytes.length);

        for (byte aByte : bytes)
    {
        if ((aByte >= 'A') && (aByte <= 'Z'))
        {
            bOut.write(aByte);
        }
    }
        return bOut.toByteArray();
    }


    public static class ChargeFilter extends SimpleFilter.FilterClause
    {
        private FieldKey _fieldKey;
        private Float[] _values;

        // At least one value must be non-null
        public ChargeFilter(FieldKey fieldKey, Float[] values)
        {
            _fieldKey = fieldKey;
            _values = values;
        }

        @Override
        public List<FieldKey> getFieldKeys()
        {
            return Arrays.asList(_fieldKey, FieldKey.fromParts("Charge"));
        }

        @Override
        public String getLabKeySQLWhereClause(Map<FieldKey, ? extends ColumnInfo> columnMap)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SQLFragment toSQLFragment(Map<FieldKey, ? extends ColumnInfo> columnMap, SqlDialect dialect)
        {
            ColumnInfo colInfo = columnMap != null ? columnMap.get(_fieldKey) : null;
            String name = colInfo != null ? colInfo.getAlias() : _fieldKey.getName();
            String alias = dialect.getColumnSelectName(name);

            SQLFragment sql = new SQLFragment();
            sql.append(alias);
            sql.append(" >= CASE Charge");

            for (int i = 0; i < _values.length; i++)
            {
                if (null != _values[i])
                {
                    sql.append(" WHEN ");
                    sql.append(i + 1);
                    sql.append(" THEN ");
                    sql.append(generalFormat.format(_values[i]));
                }
            }

            return sql.append(" ELSE 0 END");
        }


        @Override
        protected void appendFilterText(StringBuilder sb, SimpleFilter.ColumnNameFormatter formatter)
        {
            String sep = "";

            for (int i = 0; i < _values.length; i++)
            {
                if (null != _values[i])
                {
                    sb.append(sep);
                    sep = ", ";
                    sb.append('+').append(i + 1).append(':');
                    sb.append(formatter.format(_fieldKey));
                    sb.append(" >= ").append(generalFormat.format(_values[i]));
                }
            }
        }
    }


    public static class TrypticFilter extends SimpleFilter.FilterClause
    {
        private int _termini;

        public TrypticFilter(int termini)
        {
            _termini = termini;
        }

        @Override
        public String getLabKeySQLWhereClause(Map<FieldKey, ? extends ColumnInfo> columnMap)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SQLFragment toSQLFragment(Map<FieldKey, ? extends ColumnInfo> columnMap, SqlDialect dialect)
        {
            SQLFragment sql = new SQLFragment();
            switch(_termini)
            {
                case(0):
                    sql.append("");
                    break;

                case(1):
                    sql.append(nTerm(dialect)).append(" OR ").append(cTerm(dialect));
                    break;

                case(2):
                    sql.append(nTerm(dialect)).append(" AND ").append(cTerm(dialect));
                    break;

                default:
                    throw new IllegalArgumentException("INVALID PARAMETER: TERMINI = " + _termini);
            }
            sql.addAll(getParamVals());
            return sql;
        }

        private String nTerm(SqlDialect dialect)
        {
            return "(StrippedPeptide " + dialect.getCharClassLikeOperator() + " '[KR][^P]%' OR StrippedPeptide " + dialect.getCharClassLikeOperator() + " '-%')";
        }

        private String cTerm(SqlDialect dialect)
        {
            return "(StrippedPeptide " + dialect.getCharClassLikeOperator() + " '%[KR][^P]' OR StrippedPeptide " + dialect.getCharClassLikeOperator() + " '%-')";
        }

        public List<FieldKey> getFieldKeys()
        {
            return Arrays.asList(FieldKey.fromParts("StrippedPeptide"));
        }

        @Override
        protected void appendFilterText(StringBuilder sb, SimpleFilter.ColumnNameFormatter formatter)
        {
            sb.append("Trypic Ends ");
            sb.append(1 == _termini ? ">= " : "= ");
            sb.append(_termini);
        }
    }

    public static Sort getPeptideBaseSort()
    {
        // Always sort peptide lists by Fraction, Scan, HitRank, Charge
        return new Sort("Fraction,Scan,HitRank,Charge");
    }

    public static SimpleFilter getPeptideFilter(ActionURL currentUrl, List<MS2Run> runs, int mask, User user)
    {
        // Cop-out for now... we've already checked to make sure all runs are the same type
        // TODO: Allow runs of different type, by one of the following:
        // 1) verify that no search-engine-specific scores are used in the filter OR
        // 2) ignore filters that don't apply to a particular run, and provide a warning OR
        // 3) allowing picking one filter per run type
        return getPeptideFilter(currentUrl, mask, user, runs.get(0));
    }

    public static SimpleFilter reduceToValidColumns(SimpleFilter fullFilter, TableInfo... tables)
    {
        SimpleFilter validFilter = new SimpleFilter();
        for (SimpleFilter.FilterClause clause : fullFilter.getClauses())
        {
            boolean validClause = false;
            for (FieldKey fieldKey : clause.getFieldKeys())
            {
                for (TableInfo table : tables)
                {
                    ColumnInfo column = table.getColumn(fieldKey);
                    if (column == null)
                    {
                        String columnName = fieldKey.toString();
                        int index = columnName.lastIndexOf('.');
                        if (index != -1)
                        {
                            column = table.getColumn(columnName.substring(index + 1));
                        }
                    }

                    if (column != null)
                    {
                        try
                        {
                            // Coerce data types
                            Object[] values = clause.getParamVals();
                            if (values != null)
                            {
                                for (int i = 0; i < values.length; i++)
                                {
                                    if (values[i] != null)
                                    {
                                        values[i] = ConvertUtils.convert(values[i].toString(), column.getJavaClass());
                                    }
                                }
                            }
                            validClause = true;
                        }
                        catch (ConversionException ignored) {}
                    }
                }
            }
            if (validClause)
            {
                validFilter.addClause(clause);
            }
        }
        return validFilter;
    }

    public static Sort reduceToValidColumns(Sort fullSort, TableInfo... tables)
    {
        Sort validSort = new Sort();
        List<Sort.SortField> sortList = fullSort.getSortList();
        for (int i = sortList.size() - 1; i >=0; i--)
        {
            Sort.SortField field = sortList.get(i);
            boolean validClause = false;
            String columnName = field.getColumnName();
            for (TableInfo table : tables)
            {
                if (table.getColumn(columnName) != null)
                {
                    validClause = true;
                }
                else
                {
                    int index = columnName.lastIndexOf('.');
                    if (index != -1 && table.getColumn(columnName.substring(index + 1)) != null)
                    {
                        validClause = true;
                    }
                }
            }
            if (validClause)
            {
                validSort.insertSort(new Sort(field.getSortDirection().getDir() + field.getColumnName()));
            }
        }
        return validSort;
    }

    public static SimpleFilter getPeptideFilter(ActionURL currentUrl, int mask, User user, MS2Run... runs)
    {
        return getPeptideFilter(currentUrl, mask, null, user, runs);
    }

    public static SimpleFilter getPeptideFilter(ActionURL currentUrl, int mask, String runTableName, User user, MS2Run... runs)
    {
        return getTableFilter(currentUrl, mask, runTableName, MS2Manager.getDataRegionNamePeptides(), user, runs);
    }

    public static SimpleFilter getTableFilter(ActionURL currentUrl, int mask, String runTableName, String dataRegionName, User user, MS2Run... runs)
    {
        SimpleFilter filter = new SimpleFilter();

        if ((mask & RUN_FILTER) != 0)
        {
            addRunCondition(filter, runTableName, runs);
        }

        if ((mask & URL_FILTER) != 0)
            filter.addUrlFilters(currentUrl, dataRegionName);

        if ((mask & EXTRA_FILTER) != 0)
            addExtraFilter(filter, runs[0], currentUrl);

        if ((mask & PROTEIN_FILTER) != 0)
        {
            String groupNumber = currentUrl.getParameter("groupNumber");
            String indistId = currentUrl.getParameter("indistinguishableCollectionId");
            if (null != groupNumber)
            {
                try
                {
                    filter.addClause(new ProteinGroupFilter(Integer.parseInt(groupNumber), null == indistId ? 0 : Integer.parseInt(indistId)));
                    return filter;
                }
                catch (NumberFormatException e)
                {
                    throw new NotFoundException("Bad groupNumber or indistinguishableCollectionId " + groupNumber + ", " + indistId);
                }
            }

            String groupRowId = currentUrl.getParameter("proteinGroupId");
            if (groupRowId != null)
            {
                try
                {
                    filter.addCondition(FieldKey.fromParts("ProteinProphetData", "ProteinGroupId", "RowId"), Integer.parseInt(groupRowId));
                }
                catch (NumberFormatException e)
                {
                    throw new NotFoundException("Bad proteinGroupId " + groupRowId);
                }
                return filter;
            }

            String seqId = currentUrl.getParameter("seqId");
            if (null != seqId)
            {
                try
                {
                    // if "all peptides" flag is set, add a filter to match peptides to the seqid on the url
                    // rather than just filtering for search engine protein.
                    if (ProteinManager.showAllPeptides(currentUrl, user))
                    {
                        filter.addClause(new SequenceFilter(Integer.parseInt(seqId)));
                    }
                    else
                        filter.addCondition(FieldKey.fromParts("SeqId"), Integer.parseInt(seqId));
                }
                catch (NumberFormatException e)
                {
                    throw new NotFoundException("Bad seqId " + seqId);
                }
            }
        }
        return filter;
    }

    public static boolean showAllPeptides(ActionURL url, User user)
    {
        // First look for a value on the URL
        String param = url.getParameter(MS2Controller.ProteinViewBean.ALL_PEPTIDES_URL_PARAM);
        if (param != null)
        {
            boolean result = Boolean.parseBoolean(param);
            // Stash as the user's preference
            try (var ignored = SpringActionController.ignoreSqlUpdates())
            {
                PreferenceService.get().setProperty(ALL_PEPTIDES_PREFERENCE_NAME, Boolean.toString(result), user);
            }
            return result;
        }
        // Next check if the user has a preference stored
        param = PreferenceService.get().getProperty(ALL_PEPTIDES_PREFERENCE_NAME, user);
        if (param != null)
        {
            return Boolean.parseBoolean(param);
        }
        // Otherwise go with the default
        return false;
    }

    public static class SequenceFilter extends SimpleFilter.FilterClause
    {
        int _seqid;
        String _sequence;
        String _bestName;

        public SequenceFilter(int seqid)
        {
            _seqid = seqid;
            Protein prot = getProtein(seqid);
            _sequence = prot.getSequence();
            _bestName = prot.getBestName();
        }

        @Override
        public String getLabKeySQLWhereClause(Map<FieldKey, ? extends ColumnInfo> columnMap)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SQLFragment toSQLFragment(Map<FieldKey, ? extends ColumnInfo> columnMap, SqlDialect dialect)
        {
            SQLFragment sqlf = new SQLFragment();
            sqlf.append(dialect.getStringIndexOfFunction(new SQLFragment("TrimmedPeptide"), new SQLFragment("?", _sequence)));
            sqlf.append( " > 0 ");
            return sqlf;
        }

        public List<FieldKey> getFieldKeys()
        {
            return Arrays.asList(FieldKey.fromParts("TrimmedPeptide"));
        }

        @Override
        protected void appendFilterText(StringBuilder sb, SimpleFilter.ColumnNameFormatter formatter)
        {
            sb.append("Matches sequence of ");
            sb.append(_bestName);
        }
    }

    public static SimpleFilter.FilterClause getSequencesFilter(List<Integer> targetSeqIds)
    {
        SimpleFilter.FilterClause[] proteinClauses = new SimpleFilter.FilterClause[targetSeqIds.size()];
        int seqIndex = 0;
        for (Integer targetSeqId : targetSeqIds)
        {
            proteinClauses[seqIndex++] = (new ProteinManager.SequenceFilter(targetSeqId));
        }
        return new SimpleFilter.OrClause(proteinClauses);
    }

    public static class ProteinGroupFilter extends SimpleFilter.FilterClause
    {
        int _groupNum;
        int _indistinguishableProteinId;

        public ProteinGroupFilter(int groupNum, int indistId)
        {
            _groupNum = groupNum;
            _indistinguishableProteinId = indistId;
        }

        @Override
        public String getLabKeySQLWhereClause(Map<FieldKey, ? extends ColumnInfo> columnMap)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SQLFragment toSQLFragment(Map<FieldKey, ? extends ColumnInfo> columnMap, SqlDialect dialect)
        {
            SQLFragment sqlf = new SQLFragment();
            sqlf.append(" RowId IN (SELECT pm.PeptideId FROM ").append(String.valueOf(MS2Manager.getTableInfoPeptideMemberships())).append(" pm ");
            sqlf.append(" INNER JOIN ").append(String.valueOf(MS2Manager.getTableInfoProteinGroups())).append(" pg  ON (pm.ProteinGroupId = pg.RowId) \n");
            sqlf.append(" WHERE pg.GroupNumber = ").append(String.valueOf(_groupNum)).append("  and pg.IndistinguishableCollectionId = ").append(String.valueOf(_indistinguishableProteinId)).append(" ) ");
            return sqlf;
        }

        public List<FieldKey> getFieldKeys()
        {
            return Arrays.asList(FieldKey.fromParts("RowId"));
        }
         @Override
         protected void appendFilterText(StringBuilder sb, SimpleFilter.ColumnNameFormatter formatter)
         {
             sb.append("Peptide member of ProteinGroup ").append(_groupNum);
             if (_indistinguishableProteinId > 0)
             {
                 sb.append("-");
                 sb.append(_indistinguishableProteinId);
             }
         }
     }

    public static void addRunCondition(SimpleFilter filter, @Nullable String runTableName, MS2Run... runs)
    {
        String columnName = (runTableName == null ? "Run" : runTableName + ".Run");
        StringBuilder sb = new StringBuilder();
        sb.append(columnName);
        sb.append(" IN (");
        String separator = "";
        for (MS2Run run : runs)
        {
            sb.append(separator);
            separator = ", ";
            sb.append(run.getRun());
        }
        sb.append(")");
        filter.addWhereClause(sb.toString(), new Object[0], FieldKey.fromString("Run"));
    }


    // TODO: runTableName is null in all cases... remove parameter?
    public static void replaceRunCondition(SimpleFilter filter, @Nullable String runTableName, MS2Run... runs)
    {
        filter.deleteConditions(runTableName == null ? FieldKey.fromParts("Run") : FieldKey.fromParts(runTableName, "Run"));
        addRunCondition(filter, runTableName, runs);
    }


    public static MultiValuedMap<String, String> getIdentifiersFromId(int seqid)
    {
        final MultiValuedMap<String, String> map = new ArrayListValuedHashMap<>();

        new SqlSelector(getSchema(),
                "SELECT T.name AS name, I.identifier\n" +
                "FROM " + getTableInfoIdentifiers() + " I INNER JOIN " + getTableInfoIdentTypes() + " T ON I.identtypeid = T.identtypeid\n" +
                "WHERE seqId = ?",
                seqid).forEach(rs -> {
                    String name = rs.getString(1).toLowerCase();
                    String id = rs.getString(2);
                    if (name.startsWith("go_"))
                        name = "go";
                    map.put(name, id);
                });

        return map;
    }


    public static Set<String> getOrganismsFromId(int id)
    {
        HashSet<String> retVal = new HashSet<>();
        List<String> rvString = new SqlSelector(getSchema(),
                "SELECT annotVal FROM " + getTableInfoAnnotations() + " WHERE annotTypeId in (SELECT annotTypeId FROM " + getTableInfoAnnotationTypes() + " WHERE name " + getSqlDialect().getCharClassLikeOperator() + " '%Organism%') AND SeqId = ?",
                id).getArrayList(String.class);

        retVal.addAll(rvString);

        SQLFragment sql = new SQLFragment("SELECT " + getSchema().getSqlDialect().concatenate("genus", "' '", "species") +
                " FROM " + getTableInfoOrganisms() + " WHERE OrgId = " +
                "(SELECT OrgId FROM " + getTableInfoSequences() + " WHERE SeqId = ?)", id);
        String org = new SqlSelector(getSchema(), sql).getObject(String.class);
        retVal.add(org);

        return retVal;
    }


    public static String makeIdentURLString(String identifier, String infoSourceURLString)
    {
        if (identifier == null || infoSourceURLString == null)
            return null;

        try
        {
            identifier = java.net.URLEncoder.encode(identifier, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UnexpectedException(e);
        }

        return infoSourceURLString.replaceAll("\\{\\}", identifier);
    }


    static final String NOTFOUND = "NOTFOUND";
    static final Map<String, String> cacheURLs = new ConcurrentHashMap<>(200);

    public static String makeIdentURLStringWithType(String identifier, String identType)
    {
        if (identifier == null || identType == null)
            return null;

        String url = cacheURLs.get(identType);
        if (url == null)
        {
            url = new SqlSelector(getSchema(),
                    "SELECT S.url\n" +
                    "FROM " + ProteinManager.getTableInfoInfoSources() + " S INNER JOIN " + ProteinManager.getTableInfoIdentTypes() +" T " +
                        "ON S.sourceId = T.cannonicalSourceId\n" +
                    "WHERE T.name=?",
                    identType).getObject(String.class);
            cacheURLs.put(identType, null==url ? NOTFOUND : url);
        }
        if (null == url || NOTFOUND.equals(url))
            return null;

        return makeIdentURLString(identifier, url);
    }


    public static String makeFullAnchorString(String url, String target, String txt)
    {
        if (txt == null) return "";
        String retVal = "";
        if (url != null) retVal += "<a ";
        if (url != null && target != null) retVal += "target='" + target + "' ";
        if (url != null) retVal += "href='" + url + "'>";
        retVal += txt;
        if (url != null) retVal += "</a>";
        return retVal;
    }

    public static String[] makeFullAnchorStringArray(Collection<String> idents, String target, String identType)
    {
        if (idents == null || idents.isEmpty() || identType == null)
            return new String[0];
        String[] retVal = new String[idents.size()];
        int i = 0;
        for (String ident : idents)
            retVal[i++] = makeFullAnchorString(makeIdentURLStringWithType(ident, identType), target, ident);
        return retVal;
    }

    public static String[] makeFullGOAnchorStringArray(Collection<String> goStrings, String target)
    {
        if (goStrings == null) return new String[0];
        String[] retVal = new String[goStrings.size()];
        int i=0;
        for (String go : goStrings)
        {
            String sub = !go.contains(" ") ? go : go.substring(0, go.indexOf(" "));
            retVal[i++] = makeFullAnchorString(
                    makeIdentURLStringWithType(sub, "GO"),
                    target,
                    go
            );
        }
        return retVal;
    }


    /** Deletes all ProteinSequences, and the FastaFile record as well */
    public static void deleteFastaFile(int fastaId)
    {
        SqlExecutor executor = new SqlExecutor(getSchema());
        executor.execute("DELETE FROM " + getTableInfoFastaSequences() + " WHERE FastaId = ?", fastaId);
        executor.execute("UPDATE " + getTableInfoFastaFiles() + " SET Loaded=NULL WHERE FastaId = ?", fastaId);
        executor.execute("DELETE FROM " + getTableInfoFastaFiles() + " WHERE FastaId = ?", fastaId);
    }


    public static void deleteAnnotationInsertion(int id)
    {
        SQLFragment sql = new SQLFragment("DELETE FROM " + ProteinManager.getTableInfoAnnotInsertions() + " WHERE InsertId = ?");
        sql.add(id);

        new SqlExecutor(ProteinManager.getSchema()).execute(sql);
    }
}
