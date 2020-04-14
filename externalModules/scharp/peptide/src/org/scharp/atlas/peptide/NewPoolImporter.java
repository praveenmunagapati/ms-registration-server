package org.scharp.atlas.peptide;

import org.labkey.api.security.User;
import org.labkey.api.attachments.AttachmentFile;
import org.springframework.validation.Errors;
import org.scharp.atlas.peptide.model.PeptidePool;
import org.scharp.atlas.peptide.model.Peptides;
import org.scharp.atlas.peptide.model.PeptidePoolAssignment;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.sql.SQLException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 21, 2009
 * Time: 9:25:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class NewPoolImporter
{
    public Integer process(User user, AttachmentFile poolFile, Errors errors) throws SQLException
    {
        Integer poolId = null;
        try{
            InputStreamReader inStream = new InputStreamReader(poolFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String firstHeaderLine = br.readLine();
            boolean validFile = validateFirstHeaderLine(firstHeaderLine,errors);
            if(!validFile)
                return null;
            List<Integer> peptideList = new ArrayList<Integer>();
            Peptides[] peptides = PeptideManager.getPeptides();
            for(Peptides p: peptides)
                peptideList.add(p.getPeptide_id());
            List<Integer> peptidesinpoolList = new ArrayList<Integer>();
            String line = null;
            PeptidePool newPool = null;
            int lineNo = 1;
            int headerLineNo = 1;
            while((line = br.readLine()) != null)
            {
                lineNo ++;
                if(line.trim().length() > 0)
                {
                    headerLineNo ++;
                    if(headerLineNo == 2)
                    {
                        if(!validateFirstLine(line,errors,lineNo))
                            return null;
                        newPool = createPeptidePool(line);
                    }
                    else if(headerLineNo == 3)
                    {
                        if(!validateSecondHeaderLine(line,errors,lineNo))
                            return null;
                    }
                    else
                    {
                        Integer pid = validateLine(line,errors,lineNo,peptideList);
                        if(pid != null && !peptidesinpoolList.contains(pid))
                            peptidesinpoolList.add(pid);
                    }
                }
            }
            if(errors.getErrorCount() >0)
            {
                errors.reject(null,"There are "+errors.getErrorCount()+" errors in the file : "+ poolFile.getFilename());
                return null;
            }
            HashMap<Integer,List<Integer>>  poolsMap = new HashMap<Integer,List<Integer>>();
            List<Integer> pList = null;
            PeptidePoolAssignment[] pools = PeptideManager.getNonMatrixPools();
            for(PeptidePoolAssignment pa : pools)
            {
                pList = poolsMap.get(pa.getPeptide_pool_id());
                if(pList == null || pList.size() == 0)
                    pList = new ArrayList<Integer>();
                pList.add(pa.getPeptide_id());
                Arrays.sort(pList.toArray());
                poolsMap.put(pa.getPeptide_pool_id(),pList);
            }

            Arrays.sort(peptidesinpoolList.toArray());
            for(Integer i : poolsMap.keySet())
            {
                if(poolsMap.get(i).equals(peptidesinpoolList))
                {
                    errors.reject(null,"Import was not successful because the Non SCHARP Matrix peptide pool with Id "+i+" contains the same combination of peptides as in the file.");
                    return null;
                }
            }
            newPool = PeptideManager.insertPeptidePool(user,newPool);
            for(Integer pid : peptidesinpoolList)
            {
                PeptidePoolAssignment ppa  = new PeptidePoolAssignment();
                ppa.setPeptide_pool_id(newPool.getPeptide_pool_id());
                ppa.setPeptide_id(pid);
                ppa.setPeptide_in_pool(true);
                PeptideManager.insertPeptidesInPool(user,ppa);
            }
            poolId = newPool.getPeptide_pool_id();
        }
        catch(Exception e)
        {
            errors.reject(null,e.getMessage());
            return null;
        }
        return poolId;
    }

    private Integer validateLine(String line,Errors errors,int lineNo,List<Integer> peptideList)
    {
        String [] fields = line.split("\t");
        if(fields.length != 1)
            errors.reject(null,"Line number : "+lineNo+" must contain value for field 'Peptide Id' in new pool file.");
        else
        {
            if(fields[0] == null || fields[0].trim().length() == 0 || validateInteger(fields[0].trim()) == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid number or null value for Peptide Id.");
            else if(!peptideList.contains(validateInteger(fields[0].trim())))
                errors.reject(null,"Line number : "+lineNo+" contains a Peptide Id that is not in the database.Make sure the peptide exists in the database before creating pool.");
        }
        if(errors.getErrorCount() > 0)
            return null;
        else
            return validateInteger(fields[0].trim());
    }

    private boolean validateFirstLine(String line,Errors errors,Integer lineNo)
    {
        String [] fields = line.split("\t");
        if(fields.length < 2)
            errors.reject(null,"Line number : "+lineNo+" in new pool file must have values for 'Pool Type' and 'Pool Type Details'.The value for 'Peptide Group' is optional.");
        else if(fields[0] == null || fields[0].length() == 0 || fields[1] == null || fields[1].length() == 0)
            errors.reject(null,"Line number : "+lineNo+" in new pool file must have values for 'Pool Type' and 'Pool Type Details'.The value for 'Peptide Group' is optional.");
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

    private boolean validateFirstHeaderLine(String line,Errors errors)
    {
        String [] fields = line.split("\t");
        if(fields.length != 3)
            errors.reject(null,"Line number : 1 in new pool file must have three fields 'Pool Type','Pool Type Details' and 'Peptide Group' which are tab delimited.");
        else
        {
            if(fields[0] == null || fields[0].length() == 0
                    || fields[1] == null || fields[1].length() == 0
                    || fields[2] == null || fields[2].length() == 0)
                errors.reject(null,"Line number : 1 in new pool file must have three fields 'Pool Type','Pool Type Details' and 'Peptide Group' which are tab delimited.");
            else
            {
                if(!fields[0].trim().equalsIgnoreCase("Pool Type"))
                    errors.reject(null,"The First field in first header line should be 'Pool Type'.");
                if(!fields[1].trim().equalsIgnoreCase("Pool Type Details"))
                    errors.reject(null,"The Second field in first header line should be 'Pool Type Details'.");
                if(!fields[2].trim().equalsIgnoreCase("Peptide Group"))
                    errors.reject(null,"The Third field in first header line should be 'Peptide Group'.");
            }
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }

    private boolean validateSecondHeaderLine(String line,Errors errors,Integer lineNo)
    {
        String [] fields = line.split("\t");
        if(fields.length != 1)
            errors.reject(null,"Line number : "+lineNo+" in new pool file must have one field 'Peptide Id'");
        else{
            if(fields[0] == null || fields[0].trim().length() == 0 || !fields[0].trim().equalsIgnoreCase("Peptide Id"))
                errors.reject(null,"Line number : "+lineNo+" must contain the field named 'Peptide Id'.");
        }
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }

    private PeptidePool createPeptidePool(String line)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String [] fields = new String[3];
        for(int i =0;i<line.split("\t",3).length;i++)
        {
            fields[i]=line.split("\t",3)[i];
        }
        PeptidePool pPool = new PeptidePool();
        pPool.setPool_type(fields[0].trim().equalsIgnoreCase("MATRIX")?"OXFORD-MATRIX":fields[0].trim().toUpperCase());
        pPool.setDescription(fields[2]==null || fields[2].length() == 0 ?fields[1]:fields[1]+"|"+fields[2]);
        pPool.setComment("Non SCHARP defined peptide pool");
        pPool.setExists('n');
        pPool.setCreate_date(date);
        return pPool;

    }
}
