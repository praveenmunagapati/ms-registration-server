import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: adam
 * Date: Apr 22, 2010
 * Time: 6:00:41 PM
 */
public class Main
{
    private static final PackageTree ALL_PACKAGES = new PackageTree();
    private static final Set<String> PATHS_TO_PRUNE;
    private static final Set<String> PATHS_TO_SKIP;

    static
    {
        PATHS_TO_PRUNE = new HashSet<>(Arrays.asList("META-INF"));
        PATHS_TO_SKIP = new HashSet<>(Arrays.asList("com", "com/google", "com/sun", "javax", "net", "net/sf", "org", "org/apache", "org/apache/commons", "org/globus", "org/w3c"));
    }

    public static void main(String[] args) throws IOException
    {
        // Root of LabKey enlistment (e.g., c:\labkey)
        String root = args.length > 0 ? args[0] : System.getProperty("user.dir");

        List<File> labkeydirs = new LinkedList<>();

        labkeydirs.add(new File(root, "external/lib/server"));
        labkeydirs.add(new File(root, "server/internal/lib"));
        labkeydirs.addAll(findLibDirectories(new File(root, "server/modules")));
        labkeydirs.addAll(findLibDirectories(new File(root, "server/customModules")));
//        labkeydirs.add(new File(root, "server/test/lib"));

        logDuplicatePackages(labkeydirs);
//        logSpecificClass(labkeydirs, "InstanceManager.class");
//        logSpecificPackage(labkeydirs, "org/apache/log4j");
//        logTomcatClass();
    }

    private static void logDuplicatePackages(List<File> dirs) throws IOException
    {
        JarTraverser traverser = new JarTraverser(dirs) {
            @Override
            protected void handleJarEntry(JarFile j, JarEntry entry) {
                if (entry.isDirectory())
                {
                    ALL_PACKAGES.addDirectory(entry.getName(), j.getName());
                }
            }
        };
        traverser.traverse();

        traverse("", ALL_PACKAGES);
    }

    private static void logSpecificClass(List<File> dirs, final String endsWith) throws IOException
    {
        JarTraverser traverser = new JarTraverser(dirs) {
            @Override
            protected void handleJarEntry(JarFile j, JarEntry entry) {
                if (entry.getName().endsWith(endsWith))
                    System.out.println(j.getName() + ": " + entry.getName());
            }
        };
        traverser.traverse();
    }

    private static void logSpecificPackage(List<File> dirs, final String packagePrefix) throws IOException
    {
        JarTraverser traverser = new JarTraverser(dirs) {
            @Override
            protected void handleJarEntry(JarFile j, JarEntry entry) {
                if (entry.getName().startsWith(packagePrefix))
                    System.out.println(j.getName() + ": " + entry.getName());
            }
        };
        traverser.traverse();
    }

    private static void logTomcatClass() throws IOException
    {
        JarTraverser traverser = new JarTraverser(Collections.singleton(new File("c:/tomcat/lib"))) {
            @Override
            protected void handleJarEntry(JarFile j, JarEntry entry) {
                if (entry.getName().endsWith("InstanceManager.class"))
                    System.out.println(j.getName() + ": " + entry.getName());
            }
        };
        traverser.traverse();
    }

    private static Collection<File> findLibDirectories(File path)
    {
        Collection<File> files = new LinkedList<>();

        for (File dir : path.listFiles())
        {
            File lib = new File(dir, "lib");

            if (lib.exists())
                files.add(lib);
        }

        return files;
    }

    private static void traverse(@NotNull String path, Tree<String, Set<String>> tree)
    {
        if (PATHS_TO_PRUNE.contains(path))
            return;

        Set<String> jars = tree.getValue();

        if (jars.size() > 1 && !PATHS_TO_SKIP.contains(path))
        {
            System.out.println(path + " -> " + jars);
            return;
        }

        Map<String, Tree<String, Set<String>>> children = tree.getChildren();
        Set<String> sortedChildKeys = new TreeSet<>(children.keySet());

        for (String childKey : sortedChildKeys)
        {
            traverse(path.isEmpty() ? childKey : path + "/" + childKey, children.get(childKey));
        }
    }
}