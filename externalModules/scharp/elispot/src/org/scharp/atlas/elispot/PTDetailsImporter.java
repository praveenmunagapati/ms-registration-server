package org.scharp.atlas.elispot;

import org.labkey.api.view.ViewContext;
import org.labkey.api.attachments.AttachmentFile;
import org.scharp.atlas.elispot.model.PTDetails;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 31, 2007
 * Time: 1:27:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PTDetailsImporter {
    public boolean process(ViewContext ctx, ElispotBaseController.PTDetailsForm form, AttachmentFile uploadFile, List<String> errors) throws SQLException {
        try{
            InputStreamReader inStream = new InputStreamReader(uploadFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line);
            if(!validFile)
            {
                errors.add("The Plate template Details file has to be tab delimited and should have 12 fields in the first line.\n"+
                        "File is not in the right format.Please check the file and try to upload again.");
                return false;
            }
            int lineNo = 1;
            ArrayList<PTDetails> ptDetailsList = new ArrayList<PTDetails>();
            while((line = br.readLine()) != null)
            {
                lineNo++;
                if(line.length()>0)
                {
                    PTDetails ptDetails = createPTDetails(line,form.getTemplate_seq_id());
                    if(ptDetails == null)
                    {
                        errors.add("The line Number "+ lineNo+" does not have enough fields or well id field which is the first field is null or empty.Please check the file and try to upload again.");

                    }
                    else
                        ptDetailsList.add(ptDetails);
                }
            }
            if(!errors.isEmpty())
            {
                return false;
            }
            PTDetails [] dbPTD = EliSpotManager.getPTDetails(ctx.getContainer(),form.getTemplate_seq_id());
            if(dbPTD != null &&dbPTD.length>0)
            {  if(dbPTD.length != 96)
                errors.add("The Plate Template you selected has already existing details but not 96 rows.Some thing is wrong with previous upload.You have to delete those on the backend and try to upload again.");
                if(dbPTD.length == 96)
                    errors.add("The Plate Template you selected has already existing details and 96 rows..You have to delete those on the backend and try to upload again.");
                return false;
            }
            for(PTDetails ptd : ptDetailsList)
            {
                EliSpotManager.insertPTDetails(ctx,ptd);
            }
            return true;
        }
        catch(Exception e)
        {
            errors.add(e.getMessage());
            return false;
        }

    }
    private PTDetails createPTDetails(String line,Integer templateSeqId)
    {
        //boolean  validLine = validateLine(line);
        //if(!validLine)
        // return null;

        String [] fields = new String[12];
        for(int i =0;i<line.split("\t",12).length;i++)
        {
            fields[i]=line.split("\t",12)[i];
        }

        PTDetails ptDetails = new PTDetails();
        if(fields[0] == null || fields[0].trim().length() == 0)
            return null;
        else
        {
            ptDetails.setWell_id(fields[0]);
            if(fields[1]!=null && fields[1].trim().length() !=0)
                ptDetails.setFriendly_name(fields[1]);
            if(fields[2]!=null && fields[2].trim().length() !=0)
                ptDetails.setAntigen_id(fields[2]);
            if(fields[3]!=null && fields[3].trim().length() !=0)
                ptDetails.setSpec_well_group(fields[3]);
            if(fields[4]!=null && fields[4].trim().length() !=0)
                ptDetails.setReplicate(Integer.valueOf(fields[4]));
            if(fields[5]!=null && fields[5].trim().length() !=0)
                ptDetails.setPepconc(Float.parseFloat(fields[5]));
            if(fields[6]!=null && fields[6].trim().length() !=0)
                ptDetails.setPepunit(fields[6]);
            if(fields[7]!=null && fields[7].trim().length() !=0)
                ptDetails.setEffector(fields[7]);
            if(fields[8]!=null && fields[8].trim().length() !=0)
                ptDetails.setCellsperwell(Integer.valueOf(fields[8]));
            if(fields[9] !=null && fields[9].trim().length() !=0)
                ptDetails.setStcl(fields[9]);
            if(fields[10] !=null && fields[10].trim().length() !=0)
                ptDetails.setStimconc(Float.parseFloat(fields[10]));
            ptDetails.setTemplate_seq_id(templateSeqId);
            if(fields[11] != null && fields[11].trim().length() != 0)
                ptDetails.setBlinded_name(fields[11]);
            return ptDetails;
        }
    }

    private boolean validateLine(String line)
    {
        String [] fields = line.split("\t");
        if(fields.length<9)
            return false;
        return true;
    }

    private boolean validateFirstLine(String line)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 12)
            return false;
        return true;
    }
}
