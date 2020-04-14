package org.scharp.elispot;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Created by IntelliJ IDEA. User: cheryl Date: Apr 19, 2007 Time: 11:19:02 AM
 * To change this template use File | Settings | File Templates.
 *
 * Reads data from AID Elispot reader (.txt) file into PlateHashMap
 * @version $Id: ReadFile.java 30079 2009-03-13 23:09:58Z sravani $
 */
public class ReadFile {

    public static HashMap readAIDFile(BufferedInputStream bis, List<String> errors)
            throws IOException
    {

        DataInputStream fileDis = new DataInputStream(bis);
        ArrayList<String> FileLineArrayList = new ArrayList<String>();
        HashMap PlateHashMap = new HashMap();
        String PlateWellData[][] = new String[8][1];
        String text;
        int intArrayCount;
        // Read lines from file and put into an arraylist
        while ((text = fileDis.readLine()) != null) {
            text = text.trim();
            if (text.length() > 0) {
                FileLineArrayList.add(text.trim());
            }
        }

        String NewArray[] = FileLineArrayList.toArray(new String[0]);
        intArrayCount = (NewArray.length);
        int j;
        boolean boolBegin;
        boolean boolEnd;
        boolean boolFoundFirstKey;
        boolBegin = false;
        boolEnd = false;
        boolFoundFirstKey = false;
        String strRowValue;
        String strCurrRow;
        int rownum = 0;

        int WellArraySize = 0;
        int colnum;
        for (j = 0; j < intArrayCount; j++) {
            strRowValue = NewArray[j].trim();
            strRowValue = strRowValue.replaceAll("\\s+", " ");

            if (strRowValue.indexOf("Spot counts") > -1) {
                boolFoundFirstKey = true;
            }
            if (strRowValue.indexOf("surface of spots") > -1
                    || strRowValue.indexOf("surface of spots") > -1) {
                boolFoundFirstKey = false;
            }
            if (strRowValue.equals("1 2 3 4 5 6 7 8 9 10 11 12")
                    && boolFoundFirstKey) {
                boolBegin = true;
                j++;
                strRowValue = NewArray[j].trim();
            }

            /*
             * remove multiple spaces within string and replace with single
             * space
             */
            strRowValue = strRowValue.replaceAll("\\s+", " ");
            String WellArray[] = StringToArray(strRowValue, " ");

            if (boolBegin && !boolEnd) {
                strCurrRow = WellArray[0];
                String strColNum = "";
                WellArraySize = WellArray.length;
                if (WellArraySize == 13) {
                    for (colnum = 1; colnum < 13; colnum++) {
                        strColNum = "";
                        strColNum = strColNum + colnum;
                        if (strColNum.length() == 1) {
                            strColNum = "0" + strColNum;
                        }
                        // System.out.println(strCurrRow+strColNum+ "=" +
                        // WellArray[colnum]);
                        PlateHashMap.put(strCurrRow + strColNum,
                                WellArray[colnum]);
                    }
                }
                if (strCurrRow.equals("H")) {
                    boolEnd = true;
                }
                rownum++;
            }
        }
        if (!boolEnd) {
            errors.add("Unexpected file format for AID reader file");
            System.out.println("Unexpected file format for AID reader file");
        }
        /* if PlateHashMap.size()=96 then valid plate file */
        return PlateHashMap;
    }

    public static HashMap readZeissFile(BufferedInputStream bis, List<String> errors)
            throws IOException
    {

        DataInputStream fileDis = new DataInputStream(bis);
        ArrayList<String> FileLineArrayList = new ArrayList<String>();
        HashMap PlateHashMap = new HashMap();
        String PlateWellData[][] = new String[8][1];
        String text;
        int intArrayCount;
        // Read lines from file and put into an arraylist
        while ((text = fileDis.readLine()) != null) {
            text = text.trim();
            if (text.length() > 0) {
                FileLineArrayList.add(text.trim());
            }
        }

        String NewArray[] = FileLineArrayList.toArray(new String[0]);
        intArrayCount = (NewArray.length);
        int j;
        boolean boolBegin;
        boolean boolEnd;
        boolean boolFoundFirstKey;
        boolBegin = false;
        boolEnd = false;
        boolFoundFirstKey = false;
        String strRowValue;
        String strCurrRow;
        int rownum = 0;

        int WellArraySize = 0;
        int colnum;
        for (j = 0; j < intArrayCount; j++) {
            strRowValue = NewArray[j].trim();
            strRowValue = strRowValue.replaceAll("\\t+", " ");
            strRowValue = strRowValue.replaceAll("\\s+", " ");
            if (strRowValue.indexOf("Quantity") > -1) {
                boolFoundFirstKey = true;
            }
            if (strRowValue.indexOf("Parameters") > -1) {
                boolFoundFirstKey = false;
            }
            if (strRowValue.equals("1 2 3 4 5 6 7 8 9 10 11 12 S")
                    && boolFoundFirstKey) {
                boolBegin = true;
                j++;
                strRowValue = NewArray[j].trim();
            }

            /*
             * remove multiple spaces within string and replace with single
             * space
             */
            strRowValue = strRowValue.replaceAll("\\t+", " ");
            strRowValue = strRowValue.replaceAll("\\s+", " ");
            String WellArray[] = StringToArray(strRowValue, " ");

            if (boolBegin && !boolEnd) {
                strCurrRow = WellArray[0];
                String strColNum = "";
                WellArraySize = WellArray.length;
                if (WellArraySize >= 13) {
                    for (colnum = 1; colnum < 13; colnum++) {
                        strColNum = "";
                        strColNum = strColNum + colnum;
                        if (strColNum.length() == 1) {
                            strColNum = "0" + strColNum;
                        }
                        // System.out.println(strCurrRow+strColNum+ "=" +
                        // WellArray[colnum]);
                        PlateHashMap.put(strCurrRow + strColNum,
                                WellArray[colnum]);
                    }
                }
                if (strCurrRow.equals("H")) {
                    boolEnd = true;
                }
                rownum++;
            }
        }
        if (!boolEnd) {
            errors.add("Unexpected file format for Zeiss reader file");
            System.out.println("Unexpected file format for Zeiss reader file");
        }
        /* if PlateHashMap.size()=96 then valid plate file */
        return PlateHashMap;
    }

    public static HashMap readAelvisFile(BufferedInputStream bis, List<String> errors)
            throws IOException
    {
        DataInputStream fileDis = new DataInputStream(bis);
        ArrayList<String> FileLineArrayList = new ArrayList<String>();
        HashMap PlateHashMap = new HashMap();
        String PlateWellData[][] = new String[8][1];
        String text;
        int intArrayCount;
        // Read lines from file and put into an arraylist
        while ((text = fileDis.readLine()) != null) {
            text = text.trim();
            if (text.length() > 0) {
                FileLineArrayList.add(text.trim());
            }
        }

        String NewArray[] = FileLineArrayList.toArray(new String[0]);
        intArrayCount = (NewArray.length);
        int j;
        boolean boolBegin;
        boolean boolEnd;
        boolean boolFoundFirstKey;
        boolBegin = false;
        boolEnd = false;
        boolFoundFirstKey = false;
        String strRowValue;
        String strCurrRow;
        int rownum = 0;

        int WellArraySize = 0;
        int colnum;
        for (j = 0; j < intArrayCount; j++) {
            strRowValue = NewArray[j].trim();
            strRowValue = strRowValue.replaceAll("\\t+", " ");
            strRowValue = strRowValue.replaceAll("\\s+", " ");
            if (strRowValue.indexOf("COUNT RESULTS") > -1) {
                boolFoundFirstKey = true;
            }
            if (strRowValue.equals("1 2 3 4 5 6 7 8 9 10 11 12")
                    && boolFoundFirstKey) {
                boolBegin = true;
                j++;
                strRowValue = NewArray[j].trim();
            }

            /*
             * remove multiple spaces within string and replace with single
             * space
             */

            //WellArray = StringToArray(strRowValue, " ");

            if (boolBegin && !boolEnd) {
                strRowValue = strRowValue.replaceAll("\\t+", " ");
            strRowValue = strRowValue.replaceAll("\\s+", " ");
            String [] WellArray = new String[13];
            String[] testArray = strRowValue.split(" ",13);
            for(int i=0;i<testArray.length;i++)
            {
                WellArray[i]=testArray[i];
            }
                strCurrRow = WellArray[0];
                String strColNum = "";
                WellArraySize = WellArray.length;
                if (WellArraySize >= 1) {
                    for (colnum = 1; colnum < 13; colnum++) {
                        strColNum = "";
                        strColNum = strColNum + colnum;
                        if (strColNum.length() == 1) {
                            strColNum = "0" + strColNum;
                        }
                        // System.out.println(strCurrRow+strColNum+ "=" +
                        // WellArray[colnum]);
                        PlateHashMap.put(strCurrRow + strColNum,
                                WellArray[colnum]);
                    }
                }
                if (strCurrRow.equals("H")) {
                    boolEnd = true;
                }
                rownum++;
            }
        }
        if (!boolEnd) {
            errors.add("Unexpected file format for A-EL-VIS reader file");
            System.out.println("Unexpected file format for A-EL-VIS reader file");
        }
        // if PlateHashMap.size()=96 then valid plate file
        return PlateHashMap;
    }
    public static HashMap readCTLFile(InputStream is,List<String> errors) throws IOException {

        HashMap PlateHashMap = null;
        try {

            Workbook wb = Workbook.getWorkbook(is);
            Sheet sheet = wb.getSheet(0);
            //get Cell contents by (COLUMN, ROW);
            Cell cell = sheet.getCell(1, 2);
            //Get row count
            int RowCount = sheet.getRows();
            //Set column count
            int ColCount = 14;
            int BeginRawRow = 0;
            String strCurrRow = "";
            String strColNum = "";
            Boolean boolEnd = false;
            PlateHashMap = new HashMap();
            //For each row

            for (int j = 0; j < RowCount && !boolEnd; j++) {
                String strRowValue = "";
                //For each column
                for (int k = 0; k < ColCount; k++) {
                    String strCell = sheet.getCell(k, j).getContents().trim();
                    strRowValue = strRowValue + strCell;
                }
                if (strRowValue.equals("123456789101112")) {
                    BeginRawRow = j + 3;
                    String strCellValue = sheet.getCell(1, BeginRawRow).getContents();
                    if (strCellValue.equals("A")) {
                        for (int m = BeginRawRow; m < BeginRawRow + 8; m++) {
                            strCurrRow = sheet.getCell(1, m).getContents();
                            for (int n = 0; n < 12; n++) {
                                strColNum = "";
                                strColNum = strColNum + (n + 1);
                                if (n < 9) {
                                    strColNum = "0" + strColNum;
                                }
                                PlateHashMap.put(strCurrRow + strColNum, sheet.getCell(n + 2, m).getContents());
//                                System.out.println(strCurrRow + strColNum + "=" + sheet.getCell(n + 2, m).getContents());
                            }

                        }
                        boolEnd = true;
                    }
                }
            }

            wb.close();

            if (!boolEnd) {
                errors.add("unexpected file format for CTL reader file");
//                System.out.println("unexpected file format for CTL reader file");
            }


        }
        catch (IOException e) {
            errors.add("error with the input stream...");
            errors.add(e.getMessage());
            System.out.println("error with the input stream...");
            return null;
            //e.printStackTrace();
        } catch (BiffException e) {
            errors.add(e.getMessage());
            return null;
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return PlateHashMap;
    }

    public static String[] StringToArray(String s, String sep) {
        // convert a String s to an Array, the elements
        // are delimited by sep
        StringBuffer buf = new StringBuffer(s);
        int arraysize = 1;

        for (int i = 0; i < buf.length(); i++) {
            if ((sep.indexOf(buf.charAt(i)) != -1))
                arraysize++;
        }
        String[] elements = new String[arraysize];
        int y, z = 0;
        if (buf.toString().indexOf(sep) != -1) {
            while (buf.length() > 0) {
                if (buf.toString().indexOf(sep) != -1) {
                    y = buf.toString().indexOf(sep);
                    if (y != buf.toString().lastIndexOf(sep)) {
                        elements[z] = buf.toString().substring(0, y);
                        z++;
                        buf.delete(0, y + 1);
                    } else if (buf.toString().lastIndexOf(sep) == y) {
                        elements[z] = buf.toString().substring(0,
                                buf.toString().indexOf(sep));
                        z++;
                        buf.delete(0, buf.toString().indexOf(sep) + 1);
                        elements[z] = buf.toString();
                        z++;
                        buf.delete(0, buf.length());
                    }
                }
            }
        } else {
            elements[0] = buf.toString();
        }
        buf = null;
        return elements;
    }

}