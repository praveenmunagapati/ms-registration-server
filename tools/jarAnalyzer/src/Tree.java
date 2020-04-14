import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * User: adam
 * Date: 6/8/12
 * Time: 9:14 AM
 */

// A tree structure where each node (Tree) contains a map for finding specific children using a key (K)
public class Tree<K, V>
{
    private final Map<K, Tree<K, V>> _children = new HashMap<K, Tree<K, V>>();
    private final @Nullable
    Tree<K, V> _parent;
    private final @Nullable V _value;

    public Tree(@Nullable V data, @Nullable Tree<K, V> parent)
    {
        _value = data;
        _parent = parent;
    }

    public Tree<K, V> addChild(K key, @Nullable V data)
    {
        Tree<K, V> child = new Tree<K, V>(data, this);
        _children.put(key, child);
        return child;
    }

    public @Nullable Tree<K, V> getChild(K key)
    {
        return _children.get(key);
    }

    public Tree<K, V> ensureChild(K key, @Nullable V data)   // TODO: Factory
    {
        @Nullable Tree<K, V> child = getChild(key);

        if (null == child)
            child = addChild(key, data);

        return child;
    }

    public Map<K, Tree<K, V>> getChildren()
    {
        return _children;
    }

    public @Nullable V getValue()
    {
        return _value;
    }

    public @Nullable Tree<K, V> getParent()
    {
        return _parent;
    }
}

