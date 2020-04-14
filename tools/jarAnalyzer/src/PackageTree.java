import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * User: adam
 * Date: 6/8/12
 * Time: 9:22 AM
 */
public class PackageTree extends Tree<String, Set<String>>
{
    public PackageTree()
    {
        this(null);
    }

    public PackageTree(@Nullable Tree<String, Set<String>> parent)
    {
        super(new HashSet<String>(), parent);
    }

    public void addDirectory(String path, String jarName)
    {
        String[] parts = path.split("/");
        Tree<String, Set<String>> tree = this;

        for (String part : parts)
        {
            tree = tree.ensureChild(part, new HashSet<String>());
            tree.getValue().add(jarName);
        }
    }
}
