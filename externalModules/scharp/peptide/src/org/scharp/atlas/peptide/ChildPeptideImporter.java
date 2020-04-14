package org.scharp.atlas.peptide;

import org.labkey.api.security.User;
import org.labkey.api.attachments.AttachmentFile;
import org.scharp.atlas.peptide.model.Peptides;
import org.scharp.atlas.peptide.model.Parent;
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
 * Date: Nov 1, 2007
 * Time: 9:19:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChildPeptideImporter
{
    public boolean process(User user, AttachmentFile peptideFile, Errors errors,List<Peptides> resultPeptides) throws SQLException
    {
        try{
            String fileName = peptideFile.getFilename();
            InputStreamReader inStream = new InputStreamReader(peptideFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line,errors,fileName);
            if(!validFile)
                return false;
            ArrayList<Peptides> newPeptideList = new ArrayList<Peptides>();
            java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
            HashMap<Integer,Peptides> peptideIdMap = PeptideManager.getPeptideIdMap();
            HashMap<String,Peptides> peptideSequenceMap = PeptideManager.getPeptideSequenceMap();
            int lineNo =1;
            while((line = br.readLine()) != null)
            {
                lineNo++;
                if(line.length()>0)
                {
                    if(validateLine(line,errors,lineNo,peptideIdMap))
                    {
                        String [] fields = line.split("\t");
                        Peptides newPeptide = new Peptides();
                        Parent parent = new Parent();
                        Peptides parentPep = peptideIdMap.get(Integer.parseInt(fields[1]));
                        newPeptide.setPeptide_sequence(fields[0].trim().toUpperCase());
                        newPeptide.setProtein_cat_id(parentPep.getProtein_cat_id());
                        newPeptide.setChild(true);
                        newPeptide.setQc_passed('n');
                        newPeptide.setSrc_file_name(fileName);
                        newPeptide.setParent(false);
                        parent.setLinked_parent(validateInteger(fields[1].trim()));
                        parent.setParent_position(fields[2]);
                        newPeptide.setParentPep(parent);
                        newPeptideList.add(newPeptide);
                    }
                }
            }
            if(errors.getErrorCount() > 0)
            {
                errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file : "+fileName);
                return false;
            }
            for(Peptides p : newPeptideList)
            {
                Peptides dbPeptide = null;
                if(!peptideSequenceMap.containsKey(p.getPeptide_sequence()))
                    dbPeptide=PeptideManager.insertPeptide(user,p);
                else
                    dbPeptide = peptideSequenceMap.get(p.getPeptide_sequence());
                if(dbPeptide != null)
                {
                    if(!dbPeptide.isChild())
                    {
                        dbPeptide.setChild(true);
                        PeptideManager.updatePeptide(user,dbPeptide);
                    }
                    Parent parent = p.getParentPep();
                    parent.setPeptide_id(dbPeptide.getPeptide_id());
                    Parent dbParent = PeptideManager.parentExists(parent);
                    if(dbParent == null)
                        dbParent = PeptideManager.insertParent(user,parent);
                    Peptides parentPep = peptideIdMap.get(dbParent.getLinked_parent());
                    if(!parentPep.isParent())
                    {
                        parentPep.setParent(true);
                        PeptideManager.updatePeptide(user,parentPep);
                    }
                    dbPeptide.setParentPep(dbParent);
                    resultPeptides.add(dbPeptide);
                    if(!peptideSequenceMap.containsKey(p.getPeptide_sequence()))
                        peptideSequenceMap.put(dbPeptide.getPeptide_sequence(),dbPeptide);
                }
                else
                {
                    errors.reject(null,"There is a problem with the peptide with sequence : "+ p.getPeptide_sequence());
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            errors.reject(null,"There was a problem uploading File : "+peptideFile.getFilename() + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateLine(String line,Errors errors,int lineNo,HashMap<Integer,Peptides> peptideIdMap)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 3)
            errors.reject(null,"Line number : "+lineNo+" has "+fields.length+" in it. There should be 3 fields in each line.\n");
        else if(fields[0] == null || fields[0].length() == 0
                ||fields[1] == null || fields[1].length() == 0
                ||fields[2] == null || fields[2].length() == 0)
            errors.reject(null,"Line number : "+lineNo+" is missing a field. The Peptide Sequence, Linked Parent and Parent Position are required in each line.\n");
        else
        {
            if(!fields[0].trim().toUpperCase().matches("[A-Z]+"))
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Peptide Sequence : " + fields[0] +
                        "The Peptide Sequence must only contain letters(A-Z) and no spaces.\n");
            Integer parentNo = validateInteger(fields[1].trim());
            if(parentNo == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Linked Parent Id number("+fields[1].trim()+"). Linked Parent must be avalid Integer.\n");
            else if(!peptideIdMap.containsKey(parentNo))
                errors.reject(null,"Line number : "+lineNo+" contains a Parent Peptide Id number("+parentNo+") that is not in the database.\n");
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }

    public static Integer validateInteger(String value)
    {
        try{
            Integer intValue = new Integer(value);
            return intValue;
        }
        catch(NumberFormatException e){return null;}
    }

    private boolean validateFirstLine(String line,Errors errors,String fileName)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 3)
            errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Peptide Sequence','Linked Parent' and 'Parent Position'\n"+
                    "The header line does not match this format. Please check the file : "+fileName+" and try to upload again.");
        else{
            for(int i = 0;i <3;i++)
            {
                if(fields[i] ==null || fields[i].length() == 0)
                    errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Peptide Sequence','Linked Parent' and 'Parent Position'\n"+
                    "The header line does not match this format. Please check the file : "+fileName+" and try to upload again.");
            }
            if(fields[0] != null && fields[0].length() != 0 && !fields[0].trim().equalsIgnoreCase("Peptide Sequence"))
                errors.reject(null,"File Import Failed.\nThe first field in the first line nust be 'Peptide Sequence'");
            if(fields[1] != null && fields[1].length() != 0 && !fields[1].trim().equalsIgnoreCase("Linked Parent"))
                errors.reject(null,"File Import Failed.\nThe Second field in the first line nust be 'Linked Parent'");
            if(fields[2] != null && fields[2].length() != 0 && !fields[2].trim().equalsIgnoreCase("Parent Position"))
                errors.reject(null,"File Import Failed.\nThe Third field in the first line nust be 'Parent Position'");
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }
}
