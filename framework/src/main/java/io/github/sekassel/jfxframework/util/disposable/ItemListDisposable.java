package io.github.sekassel.jfxframework.util.disposable;

import io.reactivex.rxjava3.disposables.Disposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A disposable which disposes a list of items when it is disposed.
 * <p>
 * The items are disposed in reverse order, so the last item is disposed first (LIFO).
 *
 * @param <T> the type of the items
 */
public class ItemListDisposable<T> implements RefreshableDisposable {

    private boolean disposed = false;
    private List<Disposable> disposables;
    private Consumer<T> onDispose;

    private ItemListDisposable() {
    }

    /**
     * Creates a new disposable which disposes all items in the list with the given action when it is disposed.
     *
     * @param onDispose the action to execute for an item when the disposable is disposed
     * @param toDispose the items to dispose
     * @param <T>       the type of the items
     * @return the disposable
     */
    @SafeVarargs
    public static <T> ItemListDisposable<T> of(Consumer<T> onDispose, T... toDispose) {
        return of(onDispose, Arrays.asList(toDispose));
    }

    /**
     * Creates a new disposable which disposes all items in the list with the given action when it is disposed.
     *
     * @param onDispose the action to execute for an item when the disposable is disposed
     * @param toDispose the items to dispose
     * @param <T>       the type of the items
     * @return the disposable
     */
    public static <T> ItemListDisposable<T> of(Consumer<T> onDispose, List<T> toDispose) {
        ItemListDisposable<T> listDisposable = new ItemListDisposable<>();
        listDisposable.disposables = new ArrayList<>();
        listDisposable.onDispose = onDispose;
        for (T item : toDispose) {
            listDisposable.disposables.add(0, Disposable.fromRunnable(() -> onDispose.accept(item)));
        }
        return listDisposable;
    }

    @Override
    public void dispose() {
        for (Disposable disposable : this.disposables) {
            disposable.dispose();
        }
        this.disposables = null;
        this.disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override
    public boolean refresh() {
        if (isDisposed()) {
            this.disposables = new ArrayList<>();
            this.disposed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFresh() {
        return !isDisposed() && this.disposables.isEmpty();
    }

    /**
     * Adds an item to the list of items to dispose.
     *
     * @param item the item to dispose
     */
    public void add(T item) {
        this.disposables.add(Disposable.fromRunnable(() -> onDispose.accept(item)));
    }
}
