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
 * Date: Jul 24, 2007
 * Time: 10:25:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class LANLFileImporter
{
    private HashMap<String,Peptides> peptideSequenceMap;
    private HashMap<String,PeptideGroup> peptideGroupMap;

    public HashMap<String, Peptides> getPeptideSequenceMap() throws SQLException
    {
        if(peptideSequenceMap == null)
            peptideSequenceMap = PeptideManager.getPeptideSequenceMap();
        return peptideSequenceMap;
    }

    public void setPeptideSequenceMap(HashMap<String, Peptides> peptideSequenceMap)
    {
        this.peptideSequenceMap = peptideSequenceMap;
    }

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

    public boolean process(User user, AttachmentFile peptideFile, Errors errors,List<Peptides> resultPeptides) throws SQLException
    {
        try
        {
            String fileName = peptideFile.getFilename();
            InputStreamReader inStream = new InputStreamReader(peptideFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line);
            if(!validFile)
            {
                errors.reject(null,"Line number : 1 is not in the right format.The LANL file has to be tab delimited and should have more than 7 fields in the first Line.\n " +
                        "Please check the file : "+fileName+" and try to upload again.");
                return false;
            }
            getPeptideGroupMap();
            HashMap headerGroups = validateHeaderGroups(line,errors);
            if(headerGroups == null)
                return false;
            getPeptideSequenceMap();
            ProteinCategory[] proCats = PeptideManager.getProteinCategory();
            HashMap<Character,Integer> proCatMap = new HashMap<Character,Integer>();
            HashMap<Character,Integer> proSeqMap = new HashMap<Character,Integer>();
            for(ProteinCategory proCat : proCats)
            {
                if(proCat.getProtein_cat_mnem() != null && proCat.getProtein_cat_mnem().length()!=0)
                {
                    proCatMap.put(proCat.getProtein_cat_mnem().trim().charAt(0),proCat.getProtein_cat_id());
                    proSeqMap.put(proCat.getProtein_cat_mnem().trim().charAt(0),proCat.getProtein_sort_value());
                }
            }
            ArrayList<Peptides> newpeptidesList =new ArrayList<Peptides>();
            HashMap<String,String> lineMap = new HashMap<String,String>();
            int lineNo = 1;
            while ((line = br.readLine()) != null)
            {
                lineNo++;
                if (line.length() > 0)
                {
                    Peptides peptide = createLANLPeptide(line,fileName, errors, lineNo,headerGroups,proCatMap,proSeqMap);
                    if (peptide != null)
                    {
                        newpeptidesList.add(peptide);
                        lineMap.put(peptide.getPeptide_sequence(), line);
                    }
                }
            }
            if (errors.getErrorCount() > 0)
            {
                errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file "+fileName);
                return false;
            }
            for(Peptides p : newpeptidesList)
            {
                if(peptideSequenceMap.containsKey(p.getPeptide_sequence()))
                {
                    if(p.getPeptide_id() == null || p.getPeptide_id().toString().length() == 0)
                        p.setPeptide_id(peptideSequenceMap.get(p.getPeptide_sequence()).getPeptide_id());
                    insertGroups(p,user,errors);
                    resultPeptides.add(p);
                }
                else
                {
                    Peptides dbPep = PeptideManager.insertPeptide(user,p);
                    peptideSequenceMap.put(p.getPeptide_sequence(),dbPep);
                    p.setPeptide_id(dbPep.getPeptide_id());
                    insertGroups(p,user,errors);
                    resultPeptides.add(p);
                }
            }
        }
        catch(Exception e)
        {
            errors.reject(null,"File Import Failed.\n"+e.getMessage());
            return false;
        }
        return true;
    }

    private void insertGroups(Peptides p,User user,Errors errors)
    {
        try
        {
            PeptideGroup[] dbGroups = PeptideManager.getPeptideGroups(p.getPeptide_id());
            ArrayList<String> dbGroupList = new ArrayList<String>();
            for(PeptideGroup pg : dbGroups)
            {
                dbGroupList.add(pg.getPeptide_group_id().trim());
            }
            for(PeptideGroup pg : p.getPeptideGroups())
            {
                if(!dbGroupList.contains(pg.getPeptide_group_id().trim()))

                {
                    Source src = new Source();
                    src.setPeptide_group_id(pg.getPeptide_group_id());
                    src.setBtk_code(pg.getBtk_code());
                    src.setPeptide_id(p.getPeptide_id());
                    src.setTransmitted_status(pg.getTransmitted_status());
                    PeptideManager.insertSource(user,src);
                }
            }
        }
        catch (Exception e)
        {
            errors.reject(null,e.getMessage());
        }
    }

    private boolean validateFirstLine(String line)
    {
        String [] fields = line.split("\t");
        if(fields.length <= 7)
            return false;
        return true;
    }

    private Peptides createLANLPeptide(String line,String fileName, Errors errors, int lineNo, HashMap headerGroups,HashMap<Character,Integer> proCatMap,HashMap<Character,Integer> proSeqMap)
    {
        Peptides peptide = new Peptides();
        String [] fields = line.split("\t");
        if(fields.length <= 7 )
            errors.reject(null,"Line number : "+lineNo+" is not in the right format.Each line has to be tab delimited and should have more than 7 fields.\n");
        else if((fields[1] == null || fields[1].length() == 0) ||
                (fields[2] == null || fields[2].length() == 0)||
                (fields[3] == null || fields[3].length() == 0))
            errors.reject(null, "Line number : "+lineNo+" is missing a value for one or more of the following : BTK code, Peptide Sequence and Protein Align Peptide.\n");
        else
        {
            ArrayList<PeptideGroup> peptideGroupsList= new ArrayList<PeptideGroup>();
            for (int i = 7; i < fields.length; i++)
            {
                PeptideGroup peptideGroup = new PeptideGroup();
                if (fields[i] != null && fields[i].length() != 0 && (!(fields[i].trim().equals("-"))))
                {
                    if(headerGroups.containsKey(i))
                    {
                        if(!(headerGroups.get(i).equals(fields[i].trim().toUpperCase())))
                        {
                            String errorMessage = "Line number : "+lineNo+" contains a group(" + fields[i] + ") that does not match the group in the first line.\n";
                            errors.reject(null,errorMessage);
                        }
                        else
                        {
                            peptideGroup.setPeptide_group_id(peptideGroupMap.get(fields[i].trim().toUpperCase()).getPeptide_group_id());
                            peptideGroup.setBtk_code(fields[1].trim());
                            peptideGroup.setTransmitted_status("T");
                            peptideGroupsList.add(peptideGroup);
                        }
                    }
                    else
                    {
                        String errorMessage = "Line number : "+lineNo+" has the field value " + fields[i] + " in the field number " + i +
                                " not exists in the first line. The  no.of fields in this line are more than in the first line.There are "+headerGroups.size()+" groups in the header line.\n";
                        errors.reject(null,errorMessage);
                    }
                }
            }
            if(errors.getErrorCount() == 0)
            {
                if(fields[2].trim().endsWith("*"))
                    fields[2] = trimAsterisk(fields[2]);
                if(peptideSequenceMap.containsKey(fields[2].trim().toUpperCase()))
                    peptide = peptideSequenceMap.get(fields[2].trim().toUpperCase());
                else{
                    peptide.setPeptide_sequence(fields[2].trim().toUpperCase());
                    peptide.setProtein_align_pep(fields[3].trim().toUpperCase());
                    peptide.setChild(false);
                    peptide.setParent(false);
                    peptide.setQc_passed('n');
                    if(fields[6] != null && fields[6].length() != 0)
                        peptide.setLanl_date(fields[6].trim());
                    peptide.setSrc_file_name(fileName);
                    String proAliPep = peptide.getProtein_align_pep();
                    if(proAliPep.matches("[A-Z]\\d+"))
                    {
                        Character prot = proAliPep.charAt(0);
                        int pLoc = Integer.parseInt(proAliPep.substring(1));
                        if(proCatMap.containsKey(peptide.getProtein_align_pep().charAt(0)))
                        {
                            peptide.setProtein_cat_id(proCatMap.get(prot));
                            int pSortValue = proSeqMap.get(prot);
                            int subSort = 0;
                            String btkCode = fields[1].trim().toUpperCase();
                            if (btkCode.matches("[A-Z]\\d+[A-Z]"))
                            {
                                subSort = (int) btkCode.charAt(btkCode.length() - 1);
                                subSort = subSort - (int)'A'+1;
                            }
                            int sortSequence =((pSortValue+pLoc)*100)+subSort;
                            peptide.setSort_sequence(sortSequence);
                        }
                        else
                        {
                         errors.reject(null,"Line number : "+lineNo+" contains a Protein Align Peptide value with a Protein Category letter that has not been defined in the database.");
                        }
                    }
                    else{
                        String errorMessage = "Line number : "+lineNo+" contains an invalid Protein Align Peptide value("+peptide.getProtein_align_pep()+").\n";
                        errors.reject(null,errorMessage);
                    }
                }
                if(peptideGroupsList == null ||peptideGroupsList.size() == 0)
                    errors.reject(null,"Line number : "+lineNo+" contains a Peptide that is not associated to any group.Please check the peptide.All the Non-Child peptides have to be assigned to atleast one defined Peptide Group.");
                else
                    peptide.setPeptideGroups(peptideGroupsList);
            }
        }
        if(errors.getErrorCount() > 0)
            return null;
        else
            return peptide;
    }

    private HashMap validateHeaderGroups(String line,Errors errors)
    {
        HashMap<Integer,String> headerGroups = new HashMap<Integer,String>();
        String[] fields = line.split("\t");
        for (int i = 7; i < fields.length; i++)
        {
            if(peptideGroupMap.containsKey(fields[i].trim().toUpperCase()))
                headerGroups.put(i, fields[i].trim().toUpperCase());
            else
                errors.reject(null,"Line number : 1 contains a group("+fields[i].trim()+") that has not been predefined.Please enter that group into the database prior to reimporting this file.");
        }
        if(errors.getErrorCount() >0)
            return null;
        else
            return headerGroups;
    }

    private static String trimAsterisk(String sequence)
    {
        sequence = sequence.substring(0,(sequence.length()-1));
        if(sequence.endsWith("*"))
            return trimAsterisk(sequence);
        else
            return sequence;
    }
}

