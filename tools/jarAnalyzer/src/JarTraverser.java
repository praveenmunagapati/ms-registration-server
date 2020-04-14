import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: adam
 * Date: 11/9/12
 * Time: 7:39 AM
 */
public abstract class JarTraverser
{
    private final Collection<File> _dirs;

    public JarTraverser(Collection<File> dirs)
    {
        _dirs = dirs;
    }

    abstract protected void handleJarEntry(JarFile j, JarEntry entry);

    public void traverse() throws IOException
    {
        for (File dir : _dirs)
            recurse(dir);
    }

    private void recurse(File dir) throws IOException
    {
        File[] jars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") && !name.endsWith("sources.jar") && !name.endsWith("javadoc.jar");
            }
        });

        for (File jar : jars)
        {
            JarFile j = new JarFile(jar);

            Enumeration<JarEntry> enumeration = j.entries();

            while (enumeration.hasMoreElements())
                handleJarEntry(j, enumeration.nextElement());
        }

        File[] dirs = dir.listFiles(new FileFilter(){
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File child : dirs)
            recurse(child);
    }
}
