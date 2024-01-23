package io.github.sekassel.jfxframework.data;

import io.github.sekassel.jfxframework.util.disposable.ItemListDisposable;
import io.github.sekassel.jfxframework.util.disposable.RefreshableCompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DisposableTest {

    @Test
    public void refreshableCompositeDisposableTest() {

        boolean[] isDisposed = {false, false};
        RefreshableCompositeDisposable disposable = new RefreshableCompositeDisposable();

        assertFalse(disposable.isDisposed()); // Disposable should not be disposed
        assertTrue(disposable.isFresh()); // The disposable should be clean
        assertTrue(disposable.refresh()); // The disposable should be clean already, so refresh should return true

        disposable.add(Disposable.fromRunnable(() -> isDisposed[0] = true));

        assertFalse(disposable.isDisposed()); // Disposable should not be disposed
        assertFalse(disposable.isFresh()); // Disposable should not be disposed and not clean
        assertFalse(disposable.refresh()); // Disposable is not clean and not disposed, so refresh should return false

        disposable.dispose();

        assertTrue(disposable.isDisposed()); // The disposable should be disposed
        assertTrue(isDisposed[0]); // The disposable should be disposed, so the action should have been executed
        assertFalse(disposable.isFresh()); // The disposable should not be clean

        assertTrue(disposable.refresh()); // The disposable should be disposed, so refresh should return true
        assertFalse(disposable.isDisposed()); // The disposable should not be disposed
        assertTrue(disposable.isFresh()); // The disposable should be clean

        disposable.add(Disposable.fromRunnable(() -> isDisposed[1] = true));

        disposable.dispose();

        assertTrue(disposable.isDisposed()); // The disposable should be disposed
        assertTrue(isDisposed[1]); // The disposable should be disposed, so the action should have been executed
        assertFalse(disposable.isFresh()); // The disposable should not be clean
        assertTrue(disposable.refresh()); // The disposable should be disposed, so refresh should return true
    }

    @Test
    public void itemListDisposableTest() {
        List<String> disposedItems = new ArrayList<>();
        ItemListDisposable<String> disposable = ItemListDisposable.of(disposedItems::add, "1", "2", "3");

        assertFalse(disposable.isDisposed()); // Disposable should not be disposed
        assertFalse(disposable.isFresh()); // The disposable should not be clean as items have been added already

        disposable.dispose();

        assertTrue(disposable.isDisposed()); // The disposable should be disposed
        assertFalse(disposable.isFresh()); // The disposable should not be clean
        assertTrue(disposable.refresh()); // The disposable should be disposed, so refresh should return true
        assertTrue(disposable.isFresh()); // The disposable should now be clean
        assertEquals(3, disposedItems.size()); // The action should have been executed for all items
        assertEquals(List.of("3", "2", "1"), disposedItems); // The action should have been executed for all items in reverse order

        disposable.add("4");
        disposable.add("5");
        disposable.add("6");

        assertFalse(disposable.isDisposed()); // Disposable should not be disposed
        assertFalse(disposable.isFresh()); // The disposable should not be clean as items have been added already
        assertFalse(disposable.refresh()); // The disposable should not be clean and not disposed, so refresh should return false

        disposable.dispose();

        assertTrue(disposable.isDisposed()); // The disposable should be disposed
        assertFalse(disposable.isFresh()); // The disposable should not be clean
        assertTrue(disposable.refresh()); // The disposable should be disposed, so refresh should return true

        assertEquals(6, disposedItems.size()); // The action should have been executed for all items
        assertEquals(List.of("3", "2", "1", "4", "5", "6"), disposedItems); // The action should have been executed for all items in reverse order
    }

}
