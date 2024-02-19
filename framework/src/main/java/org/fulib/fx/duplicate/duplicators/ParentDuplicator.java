package org.fulib.fx.duplicate.duplicators;

import org.fulib.fx.duplicate.Duplicators;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

import static org.fulib.fx.util.ReflectionUtil.getChildrenList;

public abstract class ParentDuplicator<T extends Parent> extends NodeDuplicator<T> {

    @Override
    public T duplicate(T parent) {
        T newParent = super.duplicate(parent);

        ObservableList<Node> newChildren = getChildrenList(newParent.getClass(), newParent);

        for (Node child : parent.getChildrenUnmodifiable()) {
            Node newChild = Duplicators.duplicate(child);
            newChildren.add(newChild);
        }

        return newParent;
    }


}
