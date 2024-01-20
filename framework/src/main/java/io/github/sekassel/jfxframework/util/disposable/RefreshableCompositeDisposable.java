package io.github.sekassel.jfxframework.util.disposable;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.disposables.DisposableContainer;

/**
 * Wrapper for a {@link CompositeDisposable} which can be refreshed.
 */
public class RefreshableCompositeDisposable implements RefreshableDisposable, DisposableContainer {

    private CompositeDisposable compositeDisposable;

    @Override
    public boolean refresh() {

        if (compositeDisposable == null) {
            return true;
        }

        if (compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return compositeDisposable != null && compositeDisposable.isDisposed();
    }

    @Override
    public boolean add(Disposable d) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(d);
    }

    @Override
    public boolean remove(Disposable d) {
        return compositeDisposable != null && compositeDisposable.remove(d);
    }

    @Override
    public boolean delete(Disposable d) {
        return compositeDisposable != null && compositeDisposable.remove(d);
    }
}
