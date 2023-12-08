package io.github.sekassel.jfxframework.data;

public record Tuple<E, T>(
        E first,
        T second
) {

    public static <E, T> Tuple<E, T> of(E first, T second) {
        return new Tuple<>(first, second);
    }

    public static <E, T> Tuple<E, T> copy(Tuple<E, T> tuple) {
        return new Tuple<>(tuple.first, tuple.second);
    }

}
