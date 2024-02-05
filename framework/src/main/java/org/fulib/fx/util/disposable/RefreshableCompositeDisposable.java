package org.fulib.fx.util.disposable;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Wrapper for a {@link CompositeDisposable} which can be refreshed.
 */
public class RefreshableCompositeDisposable implements RefreshableDisposableContainer {

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
    public boolean isFresh() {
        return this.compositeDisposable == null || (!this.compositeDisposable.isDisposed() && this.compositeDisposable.size() == 0);
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
