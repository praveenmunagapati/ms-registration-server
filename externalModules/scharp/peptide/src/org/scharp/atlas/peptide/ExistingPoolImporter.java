package org.scharp.atlas.peptide;

import org.labkey.api.security.User;
import org.labkey.api.attachments.AttachmentFile;
import org.scharp.atlas.peptide.model.PeptidePool;
import org.scharp.atlas.peptide.PeptideBaseController.*;
import org.scharp.atlas.peptide.model.PeptidePoolAssignment;
import org.scharp.atlas.peptide.model.Peptides;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.SQLException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Oct 16, 2007
 * Time: 8:30:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExistingPoolImporter
{
    public boolean process(User user, FileForm form, AttachmentFile poolFile,Errors errors) throws SQLException
    {
        String actionType = form.getActionType();
        PeptidePool [] peptidePools = PeptideManager.getPeptidePools();
        HashMap<Integer,PeptidePool> peptidepoolMap = new HashMap<Integer,PeptidePool>();
        for(PeptidePool pPool : peptidePools)
        {
            peptidepoolMap.put(pPool.getPeptide_pool_id(),pPool);
        }
        if(actionType.equalsIgnoreCase("POOLDESC"))
        {
            try{
                InputStreamReader inStream = new InputStreamReader(poolFile.openInputStream());
                BufferedReader br = new BufferedReader(inStream);
                String line = br.readLine();
                boolean validFile = validateFirstLine(line,actionType);
                if(!validFile)
                {
                    errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Peptide Pool ID','Pool Type','Pool Type Details' and 'Peptide Group' in the Pool Descriptions file.\n"+
                            "The header line does not match this format. Please check the file : "+poolFile.getFilename()+" and try to upload again.");
                    return false;
                }

                ArrayList<PeptidePool> newPPools = new ArrayList<PeptidePool>();

                int lineNo =1;
                while((line = br.readLine()) != null)
                {
                    lineNo++;
                    if(line.length()>0)
                    {
                        PeptidePool pPool = createPeptidePool(line);
                        if(pPool == null)
                        {
                            errors.reject(null,"Line Number : "+ lineNo+" does not have enough fields or the first field is not a number.Please check the file and try to upload again.");

                        }
                        else
                            newPPools.add(pPool);
                    }

                }
                if(errors.getErrorCount() >0)
                {
                    errors.reject(null,"There are "+errors.getErrorCount()+" errors in the file : "+poolFile.getFilename());
                    return false;
                }
                for(PeptidePool pPool : newPPools)
                {
                    PeptidePool dbPeptidePool = null;
                    if(peptidepoolMap.containsKey(pPool.getPeptide_pool_id()))
                        errors.reject(null,"The peptide pool "+pPool.getPeptide_pool_id()+" already exists in the database with the description "+peptidepoolMap.get(pPool.getPeptide_pool_id()).getDescription()+" and the type "+peptidepoolMap.get(pPool.getPeptide_pool_id()).getPool_type());
                    else
                        dbPeptidePool=PeptideManager.insertPeptidePool(user,pPool);
                    if(dbPeptidePool != null)
                        peptidepoolMap.put(dbPeptidePool.getPeptide_pool_id(),dbPeptidePool);
                }
            }
            catch(Exception e)
            {
                errors.reject(null,e.getMessage());
                return false;
            }
        }
        if(actionType.equalsIgnoreCase("POOLPEPTIDES"))
        {
            try{
                InputStreamReader inStream = new InputStreamReader(poolFile.openInputStream());
                BufferedReader br = new BufferedReader(inStream);
                String line = br.readLine();
                boolean validFile = validateFirstLine(line,form.getActionType());
                if(!validFile)
                {
                    errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Peptide Pool ID' and 'Peptide ID in the Peptide Pool file'\n"+
                            "The header line does not match this format. Please check the file : "+poolFile.getFilename()+" and try to upload again.");
                    return false;
                }
                int lineNo =1;
                Peptides[]  peptides = PeptideManager.getPeptides();
                List<Integer> peptideList = new ArrayList<Integer>();
                for(Peptides p: peptides)
                {
                    peptideList.add(p.getPeptide_id());
                }
                ArrayList<PeptidePoolAssignment> newpeptidesInPools = new ArrayList<PeptidePoolAssignment>();
                while((line = br.readLine()) != null)
                {
                    lineNo++;
                    if(line.length()>0)
                    {
                        if(validateFirstLine(line,actionType))
                        {
                            String[] fields = line.split("\t");
                            if(!peptidepoolMap.containsKey(Integer.parseInt(fields[0])))
                                errors.reject(null,"Line number : "+lineNo+" contains a Peptide Pool Id("+Integer.parseInt(fields[0])+") that does not exist in the database.Please upload the pool description file first.");
                            else if(!peptideList.contains(Integer.parseInt(fields[1])))
                                errors.reject(null,"Line number : "+lineNo+"contains a Peptide Id("+Integer.parseInt(fields[1])+") that does not exist in the database.Please insert the peptide.");
                            else
                            {
                                PeptidePoolAssignment peptidesInPool  = new PeptidePoolAssignment();
                                peptidesInPool.setPeptide_pool_id(Integer.parseInt(fields[0]));
                                peptidesInPool.setPeptide_id(Integer.parseInt(fields[1]));
                                peptidesInPool.setPeptide_in_pool(true);
                                newpeptidesInPools.add(peptidesInPool);
                            }
                        }
                        else{
                            errors.reject(null,"Line number : "+lineNo+" must be tab delimited with the values for 'Peptide Pool ID' and 'Peptide ID in the Peptide Pool file'.");
                        }
                    }

                }
                if(errors.getErrorCount()>0)
                {
                    errors.reject(null,"There are "+errors.getErrorCount()+" errors in the file : "+poolFile.getFilename());
                    return false;
                }
                for(PeptidePoolAssignment pAssignment : newpeptidesInPools)
                {
                    PeptidePoolAssignment dbPoolAssignment = PeptideManager.insertPeptidesInPool(user,pAssignment);
                    if(dbPoolAssignment == null)
                        errors.reject(null,"The peptide pool "+pAssignment.getPeptide_pool_id()+" and the Peptide "+pAssignment.getPeptide_id()+" are already associated.");
                }
            }
            catch(Exception e)
            {
                errors.reject(null,e.getMessage());
                return false;
            }
        }
        return true;
    }
    private PeptidePool createPeptidePool(String line)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String [] fields = new String[4];
        for(int i =0;i<line.split("\t",4).length;i++)
        {
            fields[i]=line.split("\t",4)[i];
        }
        PeptidePool pPool = new PeptidePool();
        if(fields[0] == null || fields[0].length() == 0 || PeptideController.validateInteger(fields[0]) == null)
            return null;
        else
            pPool.setPeptide_pool_id(Integer.parseInt(fields[0]));
        pPool.setPool_type(fields[1].trim().equalsIgnoreCase("MATRIX")?"OXFORD-MATRIX":fields[1].trim().toUpperCase());
        pPool.setDescription(fields[3]==null || fields[3].length() == 0 ?fields[2]:fields[2]+"|"+fields[3]);
        pPool.setComment("Non SCHARP defined peptide pool");
        pPool.setExists('n');
        pPool.setCreate_date(date);
        return pPool;

    }
    private boolean validateFirstLine(String line,String importType)
    {
        String [] fields = line.split("\t");
        if(importType.equalsIgnoreCase("POOLDESC"))
        {
            if(fields.length != 4)
                return false;
        }
        if(importType.equalsIgnoreCase("POOLPEPTIDES"))
        {
            if(fields.length != 2)
                return false;
        }
        return true;
    }
}
