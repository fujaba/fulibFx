package org.fulib.fx.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A class representing two possible values with different types.
 * @param <L> The first type (left)
 * @param <R> The second type (right)
 */
public class Either<L, R> {

    private final @Nullable L left;
    private final @Nullable R right;

    public Either(@Nullable L left, @Nullable R right) {
        if ((left == null) == (right == null)) {
            throw new IllegalArgumentException("Only one value must be present");
        }
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a new Either with the left value set.
     *
     * @param left the left value
     * @param <L>  the type of the left value
     * @param <R>  the type of the right value
     * @return the new Either
     */
    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(left, null);
    }

    /**
     * Creates a new Either with the right value set.
     *
     * @param right the right value
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return the new Either
     */
    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(null, right);
    }

    /**
     * Returns whether the left value is present.
     *
     * @return whether the left value is present
     */
    public boolean isLeft() {
        return left != null;
    }

    /**
     * Returns whether the right value is present.
     *
     * @return whether the right value is present
     */
    public boolean isRight() {
        return right != null;
    }

    /**
     * Returns the left value if present, otherwise null.
     *
     * @return the left value if present, otherwise null
     */
    public @NotNull Optional<L> getLeft() {
        return Optional.ofNullable(left);
    }

    /**
     * Returns the right value if present, otherwise null.
     *
     * @return the right value if present, otherwise null
     */
    public @NotNull Optional<R> getRight() {
        return Optional.ofNullable(right);
    }

    /**
     * Returns the value of the present side.
     *
     * @return the value of the present side
     */
    public Object get() {
        return left != null ? left : right;
    }

    /**
     * Returns the value of the present side cast to the given type.
     *
     * @param type the type of the value
     * @param <T>  the type of the value
     * @return the value of the present side
     * @throws ClassCastException if the value is not of the given type
     */
    public <T> T get(Class<T> type) {
        return type.cast(get());
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[%s=%s]".formatted(isLeft() ? "left" : "right", get());
    }

}
