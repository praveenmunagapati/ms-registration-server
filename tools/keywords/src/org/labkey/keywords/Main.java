package org.labkey.keywords;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

/**
 * User: matthewb
 * Date: Oct 24, 2007
 * Time: 9:47:20 AM
 */
public class Main
{
    static boolean showByDefault(String keyword)
    {
        if (!keyword.startsWith("$"))
            return true;
        if ("$TOT".equals(keyword))
            return true;
        if ("$FIL".equals(keyword))
            return true;
        return false;
    }

    public static class FileKeywords
    {
        public File f;
        public Map<String, String> keywords;

        public FileKeywords(File f, Map<String, String> k)
        {
            this.f = f;
            this.keywords = k;
        }
    }

    static void dumpFilename(PrintStream out, String dir, File f, boolean fullPath) throws IOException
    {
        if (!fullPath && f.getPath().startsWith(dir))
            out.print(f.getPath().substring(dir.length()));
        else
            out.print(f.getPath());
    }

    static void dump(PrintStream out, String dir, Set<String> keys, List<FileKeywords> pairs, boolean noPath, boolean fullPath, boolean tabular, boolean verbose) throws IOException
    {
        // print header row
        if (tabular)
        {
            String sep = "";
            if (!noPath)
            {
                out.print("File");
                sep = "\t";
            }

            for (String key : keys)
            {
                if (verbose || showByDefault(key))
                {
                    out.print(sep);
                    out.print(key);
                }
                sep = "\t";
            }
            out.println();
        }

        for (FileKeywords pair : pairs)
        {
            File f = pair.f;
            Map<String, String> keywords = pair.keywords;

            String sep = "";
            if (tabular && !noPath)
            {
                dumpFilename(out, dir, f, fullPath);
                sep = "\t";
            }

            for (String key : keys)
            {
                if (verbose || showByDefault(key))
                {
                    String value = keywords.get(key);

                    if (tabular)
                    {
                        // normalize null values to empty string
                        value = value == null ? "" : value;

                        out.print(sep);
                        out.print(value);
                        sep = "\t";
                    }
                    else if (value != null)
                    {
                        // CONSIDER: When not tabular, we skip outputting the line if the value is null.
                        // CONSIDER: Perhaps we should provide an option to emit empty string when the value is null?
                        if (!noPath)
                        {
                            dumpFilename(out, dir, f, fullPath);
                            out.print("\t");
                        }
                        out.print(key);
                        out.print("\t");
                        out.print(value);
                        out.println();
                    }
                }

            }

            if (tabular)
                out.println();
        }
    }


    static void dump(PrintStream out, String dir, File f, boolean noPath, boolean fullPath, boolean properties, boolean verbose) throws IOException
    {
        FCSHeader h = new FCSHeader(f);
        Map keywords = h.getKeywords();
        Iterator i = keywords.entrySet().iterator();
        String propDelim = properties ? "=" : "\t";
        
        while (i.hasNext())
        {
            Map.Entry e = (Map.Entry)i.next();
            String keyword = (String)e.getKey();
            if (verbose || showByDefault(keyword))
            {
                if (!noPath)
                {
                    if (!fullPath && f.getPath().startsWith(dir))
                        out.print(f.getPath().substring(dir.length()));
                    else
                        out.print(f.getPath());
                    out.print("\t");
                }
                out.print(e.getKey());
                out.print(propDelim);
                out.print(e.getValue());
                out.println();
            }
        }
    }


    public static void main(String[] args)
    {
        boolean tabular = false;
        boolean noPath = false;
        boolean fullPath = false;
        boolean verbose = false;
        boolean quiet = false;
        Set<String> allowedKeys = null;
        List files = new ArrayList();

        String dir=(new File("").getAbsolutePath()) + File.separator;

        // default options
        for (int i=0 ; i<args.length; i++)
        {
            String arg = args[i];
            if (arg.startsWith("-"))
                continue;
            File f = (new File(args[i])).getAbsoluteFile();
            if (!f.getAbsolutePath().startsWith(dir))
                fullPath = true;
        }

        // parse options
        for (int i=0 ; i<args.length; i++)
        {
            String arg = args[i];
            if (arg.startsWith("-") && !arg.startsWith("--"))
            {
                if (-1 != arg.indexOf("t"))
                    tabular = true;
                if (-1 != arg.indexOf("b"))
                    noPath = true;
                if (-1 != arg.indexOf("l"))
                    fullPath = true;
                if (-1 != arg.indexOf("v"))
                    verbose = true;
                if (-1 != arg.indexOf("q"))
                    quiet = true;
                if (-1 != arg.indexOf("k"))
                {
                    if (i < args.length - 1)
                    {
                        allowedKeys = new LinkedHashSet<String>();
                        String[] keys = args[++i].split(",");
                        allowedKeys.addAll(Arrays.asList(keys));

                        // don't filter out any keywords the user
                        // specified on the command line.
                        verbose = true;
                    }
                    else
                    {
                        System.err.println("-k argument expects comma separated list of keywords");
                    }
                }
            }
            else if ("--help".equals(arg))
            {
                System.err.println("java -jar keywords.jar [-b] [-l] [-v] [-q] [-t] [-k key1,key2] files");
                System.err.println("  -b    bare, don't output file names, overrides -l");
                System.err.println("  -l    force using full paths");
                System.err.println("  -v    all keywords");
                System.err.println("  -k    comma separated list of keywords to display, overrides -v");
                System.err.println("  -q    quiet, no banner");
                System.err.println("  -t    tabular, ouput one line per file with keyword values separated by tabs");
                System.err.println();
                System.err.println("Example:");
                System.err.println("  java -jar keywords.jar -b -t -k \"\\$TOT,Comp\" *.fcs");
                return;
            }
            else
            {
                files.add((new File(arg)).getAbsoluteFile());
            }
        }

        if (!quiet)
            System.err.println("FCS keyword tool written by LabKey");

        Set<String> keys = allowedKeys != null ? allowedKeys : new LinkedHashSet<String>();
        List<FileKeywords> pairs = new ArrayList<FileKeywords>(files.size());

        for (int i=0 ; i<files.size(); i++)
        {
            try
            {
                File f = (File)files.get(i);
                if (!f.exists() || !f.isFile())
                    System.err.println("File not found: " + f.getPath());

                FCSHeader h = new FCSHeader(f);
                Map<String, String> k = h.getKeywords();
                if (allowedKeys == null)
                    keys.addAll(k.keySet());
                pairs.add(new FileKeywords(f, k));
            }
            catch (IOException x)
            {
                System.err.println(x.getMessage() == null ? x.toString() : x.getMessage());
            }
        }

        try
        {
            dump(System.out, dir, keys, pairs, noPath, fullPath, tabular, verbose);
        }
        catch (IOException x)
        {
            System.err.println(x.getMessage() == null ? x.toString() : x.getMessage());
        }
    }
}
