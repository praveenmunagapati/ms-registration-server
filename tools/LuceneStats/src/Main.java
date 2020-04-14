import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: adam
 * Date: Apr 26, 2010
 * Time: 1:15:03 PM
 */
public class Main
{
    public static void main(String[] args)
    {
        String path = null;

        if (args.length > 0)
            path = args[0].trim();

        if (null == path || path.isEmpty())
        {
            System.out.println("Specify a directory containing a LabKey Lucene index as the first parameter.");
            System.exit(1);
        }

        File dir = new File(path);

        if (!dir.exists())
        {
            System.out.println("\"" + path + "\" does not exist.");
            System.exit(1);
        }

        try (Directory directory = FSDirectory.open(dir); IndexReader reader = DirectoryReader.open(directory))
        {
            System.out.println("# Documents: " + reader.maxDoc());
            System.out.println("# Deleted documents: " + reader.numDeletedDocs());

            // Lucene provides no way to query a document for its size in the index, so we enumerate the terms and increment
            // term counts on each document to calculate a proxy for doc size.
            int[] termCountPerDoc = new int[reader.maxDoc()];
            int[] termLengthPerDoc = new int[reader.maxDoc()];

            for (AtomicReaderContext arc : reader.leaves())
            {
                AtomicReader ar = arc.reader();
                TermsEnum termsEnum = ar.terms("body").iterator(null);
                BytesRef br;

                while (null != (br = termsEnum.next()))
                {
                    String value = br.utf8ToString();
                    int length = value.length();

                    DocsEnum de = termsEnum.docs(ar.getLiveDocs(), null);
                    int doc;

                    while((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS)
                    {
                        termCountPerDoc[doc]++;
                        termLengthPerDoc[doc] += length;
                    }
                }
            }

            System.out.println("title\turl\tuniqueid\tbody term count\tbody term length");

            Map<String, TypeStats> typeMap = new HashMap<>();

            // The stored terms are much easier to get. For each document, output stored fields plus the statistics computed above
            for (int i = 0; i < reader.maxDoc(); i++)
            {
                Document doc = reader.document(i);
                String[] titles = doc.getValues("title");
                String[] urls = doc.getValues("url");
                String[] uniqueIds = doc.getValues("uniqueId");

                if (titles.length != 1 || urls.length != 1 || uniqueIds.length != 1)
                {
                    System.out.println("Incorrect number of term values found for document " + i);
                    System.exit(1);
                }

                String type = getType(uniqueIds[0]);
                TypeStats stats = typeMap.get(type);

                if (null == stats)
                {
                    stats = new TypeStats();
                    typeMap.put(type, stats);
                }

                stats.docCount++;
                stats.termCount += termCountPerDoc[i];

                System.out.println(titles[0] + "\t" + urls[0] + "\t" + uniqueIds[0] + "\t" + termCountPerDoc[i] + "\t" + termLengthPerDoc[i]);
            }

            System.out.println();
            System.out.println("type\tdoc count\tterm count");

            for (Map.Entry<String, TypeStats> entry : typeMap.entrySet())
            {
                TypeStats stats = entry.getValue();
                System.out.println(entry.getKey() + "\t" + stats.docCount + "\t" + stats.termCount);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static String getType(String uid)
    {
        return uid.substring(0, uid.indexOf(':'));
    }

    private static class TypeStats
    {
        private int docCount;
        private long termCount;
    }
}
