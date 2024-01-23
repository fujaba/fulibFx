package io.github.sekassel.jfxframework.data;

import io.reactivex.rxjava3.disposables.Disposable;

public record Rendered<R>(
        R rendered,
        Disposable disposable
) {

    public static <R> Rendered<R> of(R rendered, Disposable disposable) {
        return new Rendered<>(rendered, disposable);
    }

}
