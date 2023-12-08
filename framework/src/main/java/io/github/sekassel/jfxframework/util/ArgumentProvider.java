package io.github.sekassel.jfxframework.util;

@FunctionalInterface
public interface ArgumentProvider<T, A> {

    T get(A argument);

}
