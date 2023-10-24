package io.github.sekassel.jfxframework.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TraversableTree<E> {

    @Nullable E root();

    @Nullable E traverse(String path);

    @Nullable E current();

    void insert(@NotNull String path, @NotNull E value);

}
