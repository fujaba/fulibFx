package org.fulib.fx.data;

import org.fulib.fx.data.TraversableNodeTree;
import org.fulib.fx.data.TraversableTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TraversableTreeTest {

    @Test
    public void traverse() {
        TraversableTree<Integer> tree = new TraversableNodeTree<>();

        // Tree should be empty
        Assertions.assertNull(tree.root());
        Assertions.assertNull(tree.current());

        // Insert 1 as root
        tree.insert("/", 1);

        // Root should be 1, current should be 1 as it links root by default
        Assertions.assertEquals(1, tree.root());
        Assertions.assertEquals(1, tree.current());

        // Insert 2 at /a
        tree.insert("/a", 2);

        // No traversing yet, values should be the same
        Assertions.assertEquals(1, tree.root());
        Assertions.assertEquals(1, tree.current());

        // Traverse to /a
        tree.traverse("/a");

        // Root should be 1, current should be 2
        Assertions.assertEquals(1, tree.root());
        Assertions.assertEquals(2, tree.current());

        // Traversing should result in the same value
        Assertions.assertEquals(2, tree.traverse("/a"));

        // Traverse to /a/b, even though /a/b does not exist
        Assertions.assertNull(tree.traverse("/a/b"));

        // current should still be 2 as we did not traverse
        Assertions.assertEquals(2, tree.current());

        // Insert 3 at b (relative to current)
        tree.insert("b", 3);

        // Traverse to /a/b using relative path
        Assertions.assertEquals(3, tree.traverse("b"));

        // current should be 3
        Assertions.assertEquals(3, tree.current());

        // Traverse back to root, then to a, then to b, then back to a
        Assertions.assertEquals(2, tree.traverse("../../a/b/.."));

    }

}
