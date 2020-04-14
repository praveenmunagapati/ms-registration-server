package org.scharp.atlas.peptide;

import org.labkey.api.security.User;
import org.labkey.api.attachments.AttachmentFile;
import org.scharp.atlas.peptide.model.*;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Oct 22, 2008
 * Time: 1:57:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class NonChildPeptideImporter
{
    private HashMap<String,PeptideGroup> peptideGroupMap;
    private HashMap<String,ProteinCategory> proteinCategoryMap;
    private HashMap<String,TransmittedStatus> transmittedStatusMap;

    public HashMap<String, PeptideGroup> getPeptideGroupMap() throws SQLException
    {
        if(peptideGroupMap == null)
            peptideGroupMap = PeptideManager.getPeptideGroupMap();
        return peptideGroupMap;
    }

    public void setPeptideGroupMap(HashMap<String, PeptideGroup> peptideGroupMap)
    {
        this.peptideGroupMap = peptideGroupMap;
    }

    public HashMap<String, ProteinCategory> getProteinCategoryMap() throws SQLException
    {
        if(proteinCategoryMap == null)
            proteinCategoryMap = PeptideManager.getProteinCatMap();
        return proteinCategoryMap;
    }

    public void setProteinCategoryMap(HashMap<String, ProteinCategory> proteinCategoryMap)
    {
        this.proteinCategoryMap = proteinCategoryMap;
    }

    public HashMap<String, TransmittedStatus> getTransmittedStatusMap() throws SQLException
    {
        if(transmittedStatusMap == null)
            transmittedStatusMap = PeptideManager.getTransmittedMap();
        return transmittedStatusMap;
    }

    public void setTransmittedStatusMap(HashMap<String, TransmittedStatus> transmittedStatusMap)
    {
        this.transmittedStatusMap = transmittedStatusMap;
    }

    public boolean process(User user,AttachmentFile peptideFile, Errors errors,List<Peptides> resultPeptides) throws SQLException
    {
        try{
            String fileName = peptideFile.getFilename();
            InputStreamReader inStream = new InputStreamReader(peptideFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line,errors);
            if(!validFile)
                return false;
            getPeptideGroupMap();
            getProteinCategoryMap();
            HashMap<String,Peptides> peptideSequenceMap = PeptideManager.getPeptideSequenceMap();
            getTransmittedStatusMap();
            int lineNo =1;
            ArrayList<Peptides> newpeptidesList =new ArrayList<Peptides>();
            HashMap<String,String> lineMap = new HashMap<String,String>();
            while((line = br.readLine()) != null)
            {
                lineNo++;
                if(line.length()>0)
                {
                    if(validateLine(line,errors,lineNo))
                    {
                        Peptides peptide = createNonChildPeptide(line,fileName);
                        if(peptide != null)
                        {
                            newpeptidesList.add(peptide);
                            lineMap.put(peptide.getPeptide_sequence(), line);
                        }
                    }
                }
            }
            if(errors.getErrorCount() >0)
            {
                errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file : "+fileName);
                return false;
            }
            for(Peptides p : newpeptidesList)
            {
                if(peptideSequenceMap.containsKey(p.getPeptide_sequence()))
                {
                    if(p.getPeptide_id() == null || p.getPeptide_id().toString().length() == 0)
                        p.setPeptide_id(peptideSequenceMap.get(p.getPeptide_sequence()).getPeptide_id());
                    insertGroups(p,user);
                    resultPeptides.add(p);
                }
                else
                {
                    Peptides dbPep = PeptideManager.insertPeptide(user,p);
                    peptideSequenceMap.put(p.getPeptide_sequence(),dbPep);
                    p.setPeptide_id(dbPep.getPeptide_id());
                    insertGroups(p,user);
                    resultPeptides.add(p);
                }
            }
        }
        catch(Exception e)
        {
            errors.reject(null,e.getMessage());
            return false;
        }
        return true;
    }

    private void insertGroups(Peptides p,User user) throws Exception
    {
        PeptideGroup[] dbGroups = PeptideManager.getPeptideGroups(p.getPeptide_id());
        ArrayList<String> dbGroupList = new ArrayList<String>();
        for(PeptideGroup pg : dbGroups)
        {
            dbGroupList.add(pg.getPeptide_group_id().trim().toUpperCase());
        }
        for(PeptideGroup pg : p.getPeptideGroups())
        {
            if(!dbGroupList.contains(pg.getPeptide_group_id().trim().toUpperCase()))
            {
                Source src = new Source();
                src.setPeptide_group_id(pg.getPeptide_group_id());
                src.setPeptide_id(p.getPeptide_id());
                src.setTransmitted_status(pg.getTransmitted_status());
                PeptideManager.insertSource(user,src);
            }
        }
    }

    private boolean validateLine(String line,Errors errors,int lineNo)
    {
        String [] fields = line.split("\t");
        if(fields.length != 5 )
            errors.reject(null,"Line number : "+lineNo+"must have 5 fields.The Non-Child File has to be tab delimited and should have 5 fields.");
        else if((fields[0] == null || fields[0].length() == 0) || (fields[1] == null || fields[1].length() == 0)||
                (fields[2] == null || fields[2].length() == 0) || (fields[4] == null || fields[4].length() == 0))
            errors.reject(null,"Line number : "+lineNo+" is missing one of the field values 'Peptide Sequence', 'Peptide Group','Protein Category' or 'Transmitted Status'.");
        else
        {
            if(!fields[0].trim().toUpperCase().matches("[A-Z]+"))
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Peptide Sequence : " + fields[0] +
                        "The Peptide Sequence must only contain letters(A-Z) and no spaces.\n");
            if(!peptideGroupMap.containsKey(fields[1].trim().toUpperCase()))
                errors.reject(null,"Line number : "+lineNo+" contains a Peptide Group " + fields[1] +
                        " which is not in the database. You can add peptides to existing groups only or need to add a new Group.");
            if(!transmittedStatusMap.containsKey(fields[4].trim().toUpperCase()))
                errors.reject(null,"Line number : "+lineNo+" contains the Transmitted Status " + fields[4] +
                        " that does not exists in the database.");
            if(!proteinCategoryMap.containsKey(fields[2].trim().toUpperCase()))
                errors.reject(null,"Line number : "+lineNo+" contains the Protein Category " + fields[2] +
                        " that does not exists in the database. Contact SCHARP to add new Protein Category");
            else
            {
                String proteinCatMnem = proteinCategoryMap.get(fields[2].trim().toUpperCase()).getProtein_cat_mnem();
                if(fields[3] != null && fields[3].length() != 0 &&
                        proteinCatMnem != null && proteinCatMnem.length() != 0
                        && !String.valueOf(fields[3].trim().charAt(0)).equalsIgnoreCase(proteinCatMnem))
                    errors.reject(null,"Line number : "+lineNo+" contains the first letter of Protein Ali Pep " + fields[3] +
                            " does not match the protein category mnem of "+fields[2]+" Please check the file");
            }
        }
        if(errors.getErrorCount() >0)
            return false;
        return true;
    }

    private Peptides createNonChildPeptide(String line,String fileName) throws SQLException
    {
        String [] fields = line.split("\t");
        ArrayList<PeptideGroup> peptideGroupsList= new ArrayList<PeptideGroup>();
        PeptideGroup pg = new PeptideGroup();
        pg.setPeptide_group_id(peptideGroupMap.get(fields[1].trim().toUpperCase()).getPeptide_group_id());
        pg.setTransmitted_status(transmittedStatusMap.get(fields[4].trim().toUpperCase()).getTransmitted_status());
        peptideGroupsList.add(pg);
        Peptides peptide = new Peptides();
        peptide.setPeptide_sequence(fields[0].trim().toUpperCase());
        peptide.setProtein_cat_id(proteinCategoryMap.get(fields[2].trim().toUpperCase()).getProtein_cat_id());
        if (fields[3] != null && fields[3].length() !=0)
            peptide.setProtein_align_pep(fields[3].trim().toUpperCase());
        String proAliPep = peptide.getProtein_align_pep();
        if(proAliPep != null && proAliPep.matches("[A-Z]\\d+"))
        {
            int pLoc = Integer.parseInt(proAliPep.substring(1));
            Integer pSortValue = proteinCategoryMap.get(fields[2].trim().toUpperCase()).getProtein_sort_value();
            if(pSortValue != null)
            {
                int sortSequence =((pSortValue+pLoc)*100);
                peptide.setSort_sequence(sortSequence);
            }

        }
        peptide.setChild(false);
        peptide.setParent(false);
        peptide.setQc_passed('n');
        peptide.setSrc_file_name(fileName);
        peptide.setPeptideGroups(peptideGroupsList);
        return peptide;
    }

    public static Integer validateInteger(String value)
    {
        try{
            Integer intValue = new Integer(value);
            return intValue;
        }
        catch(NumberFormatException e){return null;}
    }
    private boolean validateFirstLine(String line, Errors errors)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 5)
            errors.reject(null,"Line number : 1 in the Non-Child peptides file has to be tab delimited and should have 5 fields.\n"+
                    "'Peptide Sequence','Peptide Group','Protein Category','Protein Align Pep' and 'Transmitted Status'.\n"+
                    "Non-Child peptides file is not in the right format.Please check the file and try to upload again.");
        else{
            for(int i = 0;i <5;i++)
            {
                if(fields[i] ==null || fields[i].length() == 0)
                    errors.reject(null,"Line number : 1 in the Non-Child peptides file has to be tab delimited and should have 5 fields.\n"+
                    "'Peptide Sequence','Peptide Group','Protein Category','Protein Align Pep' and 'Transmitted Status'.\n"+
                    "Non-Child peptides file is not in the right format.Please check the file and try to upload again.");
            }
        }
        if(fields[0] != null && !fields[0].trim().equalsIgnoreCase("Peptide Sequence"))
            errors.reject(null,"File Import Failed.\nThe first field in the first line (Header) must be 'Peptide Sequence'");
        if(fields[1] != null && !fields[1].trim().equalsIgnoreCase("Peptide Group"))
            errors.reject(null,"File Import Failed.\nThe Second field in the first line (Header) must be 'Peptide Group'");
        if(fields[2] != null && !fields[2].trim().equalsIgnoreCase("Protein Category"))
            errors.reject(null,"File Import Failed.\nThe Third field in the first line (Header) must be 'Protein Category'");
        if(fields[3] != null && !fields[3].trim().equalsIgnoreCase("Protein Align Pep"))
            errors.reject(null,"File Import Failed.\nThe Third field in the first line (Header) must be 'Protein Align Pep'");
        if(fields[4] != null && !fields[4].trim().equalsIgnoreCase("Transmitted Status"))
            errors.reject(null,"File Import Failed.\nThe Third field in the first line (Header) must be 'Transmitted Status'");
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }
}
