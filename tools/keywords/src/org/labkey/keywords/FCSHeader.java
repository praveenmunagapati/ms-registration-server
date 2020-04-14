package org.labkey.keywords;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;


public class FCSHeader
{
    private Map<String, String> keywords = new TreeMap<String, String>();
    int dataLast;
    int dataOffset;
    int textOffset;
    int textLast;
    char chDelimiter;
    String version;
    File _file;


    public FCSHeader(File file) throws IOException
    {
        load(file);
    }

    protected FCSHeader()
    {
    }


    protected void load(File file) throws IOException
    {
        _file = file;
        InputStream is = new FileInputStream(file);
        try
        {
            load(is);
        }
        finally
        {
            is.close();
        }
    }

    public File getFile()
    {
        return _file;
    }

    public String getKeyword(String key)
    {
        return keywords.get(key);
    }

    public Map<String, String> getKeywords()
    {
        return Collections.unmodifiableMap(keywords);
    }

    protected void load(InputStream is) throws IOException
    {
        textOffset = 0;
        textLast = 0;
        long cbRead = 0;

        //
        // HEADER
        //
        {
            byte[] headerBuf = new byte[58];
            long read = is.read(headerBuf, 0, headerBuf.length);
            if (read != 58)
                throw new FcsIOException( _file);
            cbRead += read;
            String header = new String(headerBuf);

            version = header.substring(0, 6).trim();
            textOffset = Integer.parseInt(header.substring(10, 18).trim());
            textLast = Integer.parseInt(header.substring(18, 26).trim());
            dataOffset = Integer.parseInt(header.substring(26, 34).trim());
            dataLast = Integer.parseInt(header.substring(34, 42).trim());
        }

        //
        // TEXT
        //

        {
            if (cbRead > textOffset)
                throw new FcsIOException(_file);
            cbRead += is.skip(textOffset - cbRead);
            byte[] textBuf = new byte[textLast - textOffset + 1];
            long read = is.read(textBuf, 0, textBuf.length);
            if (read != textBuf.length)
                throw new FcsIOException(_file);
            cbRead += read;
            String fullText = new String(textBuf);
            chDelimiter = fullText.charAt(0);
            int ichStart = 0;
            while (true)
            {
                int ichMid = fullText.indexOf(chDelimiter, ichStart + 1);
                if (ichMid < 0)
                    break;
                int ichEnd = fullText.indexOf(chDelimiter, ichMid + 1);
                if (ichEnd == -1)
                    //throw new FcsIOException(_file);
                    ichEnd = fullText.length();
                String strKey = fullText.substring(ichStart + 1, ichMid);
                String strValue = fullText.substring(ichMid + 1, ichEnd);
                keywords.put(strKey, strValue.trim());
                ichStart = ichEnd;
            }
        }
    }

    public static class FcsIOException extends IOException
    {
        FcsIOException(File f)
        {
            super("File is not an FCS file or is corrupt" + (f==null?"":": " + f.getPath()));
        }
    }
}
