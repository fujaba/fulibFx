package io.github.sekassel.jfxframework.util.disposable;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A disposable which can be refreshed.
 * <p>
 * When the disposable is disposed, it can be refreshed to make it usable again.
 */
public interface RefreshableDisposable extends Disposable {

    /**
     * Refreshes the disposable if it is disposed.
     * If the disposable is not disposed (or not initialized), this method does nothing.
     *
     * @return true if the disposable was refreshed
     */
    boolean refresh();

}
