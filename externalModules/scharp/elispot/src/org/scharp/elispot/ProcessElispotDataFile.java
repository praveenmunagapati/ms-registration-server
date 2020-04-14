package org.scharp.elispot;

import org.scharp.atlas.elispot.EliSpotController;
import org.labkey.api.view.ViewContext;
import org.labkey.api.attachments.AttachmentFile;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @version $Id: ProcessElispotDataFile.java 35489 2009-09-16 16:52:04Z sravani $
 */
public class ProcessElispotDataFile {

    //Connection conn = null;
    /*public ProcessElispotDataFile(Connection conn) {
        this.conn = conn;
    }  */

    public boolean processFile(EliSpotController.StudyLabBatchForm form,String fileName,InputStream fileStream,ViewContext ctx, List<String> errors) throws IOException {

        try {
            AppendPlateData appendData = new AppendPlateData();
            HashMap NewPlateHashMap = null;
            int fileindex = fileName.indexOf('.');
            String fextension = fileName.substring(fileindex+1);
            String basefilename = "";
            int intPath1 = fileName.lastIndexOf("/");
            int intPath2 = fileName.lastIndexOf("\\");
            if (intPath1 > -1) {
                basefilename = fileName.substring(0, intPath1);
            }
            if (intPath2 > -1) {
                basefilename = fileName.substring(0, intPath2);
            }
            fextension = fextension.toUpperCase();
            Integer reader_id = form.getReader_id(); //readerObj.getReader_seq_id();
            String type= EliSpotController.getReaderObjfromid(reader_id).getReader_type();
            HashMap<String,InputStream> fileMap =  new HashMap<String,InputStream>();
            if (fextension.equalsIgnoreCase("TXT") || fextension.equalsIgnoreCase("XLS"))
                    fileMap.put(fileName,fileStream);
            if ( fileMap.size() ==0)
                System.out.println("Arrlist is null");

            if (type != null && type.length() > 0 && fileMap.size() > 0)
            {
                Integer batch_id = form.getBatchId();
                if (type.trim().equalsIgnoreCase("AID-TXT"))
                {
                    for(String key : fileMap.keySet())
                    {
                        BufferedInputStream bis = new BufferedInputStream(fileMap.get(key));
                        NewPlateHashMap = ReadFile.readAIDFile(bis,errors);
                        if (NewPlateHashMap !=null && NewPlateHashMap.size() == 96) {
                            boolean appended = appendData.appendPlateData(key, batch_id,reader_id, NewPlateHashMap,ctx,errors);
                            if(!appended)
                                return false;
                        } else {
                            errors.add(fileName+" does not match a known ELISpot data file format for "+type.trim());
                            return false;
                        }
                    }
                }
                if (type.trim().equalsIgnoreCase("Zeiss-TXT"))
                {
                    for(String key : fileMap.keySet())
                    {
                        BufferedInputStream bis = new BufferedInputStream(fileMap.get(key));
                        NewPlateHashMap = ReadFile.readZeissFile(bis,errors);
                        if (NewPlateHashMap !=null && NewPlateHashMap.size() == 96) {
                            boolean appended = appendData.appendPlateData(key, batch_id,reader_id, NewPlateHashMap,ctx,errors);
                            if(!appended)
                                return false;
                        } else {
                            errors.add(fileName+" does not match a known ELISpot data file format for "+type.trim());
                            return false;
                        }
                    }
                }
                if (type.equalsIgnoreCase("CTL-XLS"))
                {
                    for(String key : fileMap.keySet())
                    {
                        NewPlateHashMap = ReadFile.readCTLFile(fileMap.get(key),errors);
                        if (NewPlateHashMap !=null && NewPlateHashMap.size() == 96) {
                            boolean appended = appendData.appendPlateData(key, batch_id,reader_id,NewPlateHashMap,ctx,errors);
                            if(!appended)
                                return false;
                        } else {
                            errors.add(fileName+" does not match a known ELISpot data file format for "+type.trim());
                            return false;
                        }
                    }
                }
               if (type.trim().equalsIgnoreCase("A-EL-VIS-TXT"))
                {
                    for(String key : fileMap.keySet())
                    {
                        BufferedInputStream bis = new BufferedInputStream(fileMap.get(key));
                        NewPlateHashMap = ReadFile.readAelvisFile(bis,errors);
                        if (NewPlateHashMap !=null && NewPlateHashMap.size() == 96) {
                            boolean appended = appendData.appendPlateData(key, batch_id,reader_id, NewPlateHashMap,ctx,errors);
                            if(!appended)
                                return false;
                        } else {
                            errors.add(fileName+" does not match a known ELISpot data file format for "+type.trim());
                            return false;
                        }
                    }
                } 

            } //end of if (type!=null)
        } catch (IOException ioe) {
            errors.add("Unhandled exception:");
            errors.add(ioe.getMessage());
            ioe.printStackTrace();
            return false;
        }

        return true;
    }
    public ArrayList<String> getunZipFiles(AttachmentFile zipFile)
    {
        ArrayList<String> unZipFiles = new ArrayList<String>();

        if (zipFile != null)
        {
            try{
                // convert it to a zipinputstream
                ZipInputStream zipInputStream = new ZipInputStream(zipFile.openInputStream());
                // for each entry in the zip file do something
                ZipEntry entry = null;
                while ((entry = zipInputStream.getNextEntry()) != null)
                {
                    // this is the name of the entry (file that has been zipped)
                    if (!entry.isDirectory())
                    {
                        // String fileName = entry.getName();
                        unZipFiles.add(entry.getName());
                    }
                }
            }catch(Exception ze)
            {
                System.err.println("Error in unzipping files ElispotController (getunzipFiles): "+ze);
            }
        }
        return unZipFiles;
    }
}