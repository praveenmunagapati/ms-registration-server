package org.scharp.elispot;

import org.apache.log4j.Logger;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.view.ViewContext;
import org.scharp.atlas.elispot.EliSpotManager;
import org.scharp.atlas.elispot.EliSpotSchema;
import org.scharp.atlas.elispot.model.Plate;
import org.scharp.atlas.elispot.model.PlateData;

import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @version $Id: AppendPlateData.java 50668 2011-08-17 19:04:18Z sravani $
 */
public class AppendPlateData {

    private static Logger log = Logger.getLogger(AppendPlateData.class);
    public boolean appendPlateData(String platefile,
                                   Integer batch_seq_id,
                                   Integer reader_id,
                                   HashMap NewPlateHashMap,
                                   ViewContext ctx,
                                   List<String> errors)
    {
        int i = platefile.lastIndexOf('.');
        String plateid = platefile.substring(0, i).toUpperCase();
        DbScope scope = EliSpotSchema.getInstance().getSchema().getScope();
        try (DbScope.Transaction transaction = scope.beginTransaction())
        {
            Plate plate = EliSpotManager.getPlates(ctx.getContainer(), plateid,batch_seq_id);
            if(plate == null)
            {
                errors.add("The file named "+platefile+" does not match a plate within this batch and could not be uploaded.Please check the file or contact administrator.");
                return false;
            }
            if(plate.getPlate_filename() != null && plate.getPlate_filename().length() != 0)
            {
                errors.add("A plate with this filename "+platefile+" has already been uploaded.Please check the file or contact administrator.");
                return false;
            }
            if(plate.isBool_report_plate())
            {
                errors.add("The Plate with this file name "+platefile+" already uploaded and approved.Check the file name and the plate information for this file.");
                return false;
            }
            plate.setPlate_filename(platefile);
            plate.setImport_date(new Date(new java.util.Date().getTime()));
            boolean updatedPlate = EliSpotManager.updatePlate(ctx.getContainer(),ctx.getUser(),plate);
            if(!updatedPlate)
            {
                errors.add("Could not update the table tblplate with filename.");
                return false;
            }
            boolean updateBatch = EliSpotManager.updateBatch(ctx.getUser(),batch_seq_id,reader_id);
            if(!updateBatch)
            {
                errors.add("Could not update the table tblbatch with readerseqId.");
                return false;
            }
            Iterator myHashMapIterator = NewPlateHashMap.keySet().iterator();
            while (myHashMapIterator.hasNext()) {
                Object key = myHashMapIterator.next();
                Object value = NewPlateHashMap.get(key);
                PlateData plateData = new PlateData();
                plateData.setPlate_seq_id(plate.getPlate_seq_id());
                plateData.setWell_id((String)key);
                plateData.setText_sfu((String)value);
                PlateData pData = EliSpotManager.insertPlateData(ctx,plateData);
                if(pData == null)
                {
                    errors.add("Could not insert data into the table tblplatedata.");
                    return false;
                }
            }

            java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
            String sqlPlateMap ="insert into elispot.tblplatemap(plate_seq_id, orig_well_id, final_well_id, spec_well_group, antigen_id, friendly_name, replicate, pepconc, pepunit, stcl,effector,stimconc, cellsperwell,blinded_name,container,createdby,created,modifiedby,modified)\n" +
                      "Select ? as plate_seq_id, well_id as orig_well_id, well_id as final_well_id, spec_well_group, antigen_id, friendly_name, replicate, pepconc, pepunit, stcl,effector,stimconc, cellsperwell,blinded_name,? as container,? as createdby,? as created,? as modifiedby,? as modified\n" +
                      " from elispot.tblplatetemplatedetails as t where t.template_seq_id = ?";
            new SqlExecutor(EliSpotSchema.getInstance().getSchema()).execute(
                    sqlPlateMap,
                    plate.getPlate_seq_id(),ctx.getContainer().getId(),ctx.getUser().getUserId(),date,ctx.getUser().getUserId(),date,plate.getTemplate_seq_id());
            String updateSfuQuery = "UPDATE elispot.tblplatedata pdata \n"+
                    "SET sfu =CASE \n" +
                    "WHEN text_sfu in (select text_sfu from elispot.tblsfutranslation where reader_seq_id = "+reader_id+") then (select sfu  from elispot.tblsfutranslation sf  where reader_seq_id = "+reader_id+" and sf.text_sfu = pdata.text_sfu)\n" +
                    "ELSE cast(text_sfu as int) END \n"+
                    "where plate_seq_id = ?";
            new SqlExecutor(EliSpotSchema.getInstance().getSchema()).execute(updateSfuQuery, plate.getPlate_seq_id());
           /* String updateSfuQuery ="UPDATE elispot.tblplatedata \n" +
                    "SET sfu = CASE WHEN text_sfu = '-1' then null\n" +
                    "when text_sfu = '-2' then '9999'\n" +
                    "when text_sfu= '-3' then null\n" +
                    "when text_sfu= '-4' then null\n" +
                    "when text_sfu='-' then null\n" +
                    "when text_sfu='TNTC' then '9999'\n" +
                    "when text_sfu='TMSTC' then '9999'\n" +
                    "ELSE cast(text_sfu as int) END \n" +
                    "where plate_seq_id = ?"; 
            Table.execute(EliSpotSchema.getInstance().getSchema(),
                    updateSfuQuery,
                    new Object[]{plate.getPlate_seq_id()});   */

            transaction.commit();
            return true;

        }
        catch(Exception e)
        {
            errors.add(e.getMessage());
            return false;
        }
    }
}





