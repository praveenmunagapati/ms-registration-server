package org.scharp.atlas.peptide;

import org.labkey.api.security.User;
import org.labkey.api.attachments.AttachmentFile;
import org.apache.commons.lang3.StringUtils;
import org.scharp.atlas.peptide.model.Peptides;
import org.scharp.atlas.peptide.model.ManuFactureStatus;
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
 * Date: Sep 7, 2007
 * Time: 2:08:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManufactureStatusImporter
{
    public boolean process(User user, AttachmentFile peptideFile, Errors errors,List<Peptides> resultPeptides) throws SQLException
    {
        try
        {
            String filename = peptideFile.getFilename();
            InputStreamReader inStream = new InputStreamReader(peptideFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line,errors,filename);
            if(!validFile)
                return false;
            HashMap<Character,ManuFactureStatus> statusMap = PeptideManager.getStatusMap();
            HashMap<Integer,Peptides> peptideIdMap = PeptideManager.getPeptideIdMap();
            ArrayList<Peptides> updatePeptides = new ArrayList<Peptides>();
            int lineNo =1;
            while((line = br.readLine()) != null)
            {
                lineNo++;
                if(line.length()>0)
                {
                    if(validateLine(line,errors,lineNo))
                    {
                        String[] fields = line.split("\t");
                        if(validateFields(fields,lineNo,errors,statusMap,peptideIdMap))
                        {
                            Peptides pep = peptideIdMap.get(Integer.parseInt(fields[0].trim()));
                            if(!(String.valueOf(pep.getQc_passed()).equalsIgnoreCase(fields[2].trim())))
                            {
                                pep.setQc_passed(fields[2].trim().toLowerCase().charAt(0));
                                updatePeptides.add(pep);
                            }
                        }
                    }
                }
            }
            if(errors.getErrorCount() > 0)
            {
                errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file "+filename);
                return false;
            }
            for(Peptides p : updatePeptides)
            {
                PeptideManager.updatePeptide(user,p);
                resultPeptides.add(p);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            errors.reject(null,"File Import Failed.\nThere was a problem uploading File: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateFirstLine(String line,Errors errors,String fileName)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 3)
            errors.reject(null,"Line number : 1 has to be tab delimited and should contain the fields 'Peptide Id','Peptide Sequence' and 'Peptide Status'.\n"+
                    "Manufacture Status File : "+fileName+" is not in the right format.Please check the file and try to upload again.\n");
       else{
            for(int i = 0;i <3;i++)
            {
                if(fields[i] ==null || fields[i].length() == 0)
                    errors.reject(null,"Line number : 1 has to be tab delimited and should contain the fields 'Peptide Id','Peptide Sequence' and 'Peptide Status'.\n"+
                    "Manufacture Status File : "+fileName+" is not in the right format.Please check the file and try to upload again.\n");
            }
            if(fields[0] != null && fields[0].length() != 0 && !fields[0].trim().equalsIgnoreCase("Peptide Id"))
                errors.reject(null,"File Import Failed.\nThe first field in the first line nust be 'Peptide Id'");
            if(fields[1] != null && fields[1].length() != 0 && !fields[1].trim().equalsIgnoreCase("Peptide Sequence"))
                errors.reject(null,"File Import Failed.\nThe Second field in the first line nust be 'Peptide Sequence'");
            if(fields[2] != null && fields[2].length() != 0 && !fields[2].trim().equalsIgnoreCase("Peptide Status"))
                errors.reject(null,"File Import Failed.\nThe Third field in the first line nust be 'Peptide Status'");
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }

    private boolean validateLine(String line,Errors errors,Integer lineNo)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 3)
        {
            errors.reject(null,"Line number : "+lineNo+" must have values for 'Peptide Id','Peptide Sequence' and 'Peptide Status'");
            return false;
        }
        return true;
    }

    public static Integer validateInteger(String value)
    {
        try
        {
            Integer intValue = new Integer(value);
            return intValue;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
    
    public static Character validateStatusValue(String value)
    {
        Character charValue = null;
        if(value.length() == 1)
        {
            charValue = value.toLowerCase().charAt(0);
        }
        return charValue;
    }

    private boolean validateFields(String[] fields, int lineNo, Errors errors, HashMap<Character,ManuFactureStatus> status,HashMap<Integer,Peptides> peptideIdMap)
    {
        if(StringUtils.trimToNull(fields[0]) == null || StringUtils.trimToNull(fields[1]) == null || StringUtils.trimToNull(fields[2]) == null)
            errors.reject(null,"Line number : "+lineNo+" must have values for 'Peptide Id','Peptide Sequence' and 'Peptide Status'");
        else
        {
            Character statusValue = validateStatusValue(fields[2]);
            if (statusValue == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Status value.(It has to be a single letter representing status.)");
            else if (!status.containsKey(statusValue))
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Status value.It has to be defined in the database.");
            Integer intValue = validateInteger(fields[0]);
            if (intValue == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid Peptide Id number.It has to be a valid Integer");
            else if(!peptideIdMap.containsKey(Integer.parseInt(fields[0].trim())))
                errors.reject(null,"Line number : "+lineNo+" contains a Peptide(" + fields[0] + ") that is not in the database.");
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }

}
