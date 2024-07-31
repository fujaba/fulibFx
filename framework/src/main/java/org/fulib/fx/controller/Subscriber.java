package org.fulib.fx.controller;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.data.disposable.RefreshableCompositeDisposable;
import org.fulib.fx.data.disposable.RefreshableDisposableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A subscriber is used to subscribe to observables and listen to properties.
 * <p>
 * The subscriber saves all subscriptions and disposes them when it is destroyed.
 * Subscribers which are declared with a field in a controller are automatically destroyed when the controller is destroyed.
 */
public class Subscriber implements RefreshableDisposableContainer {

    /**
     * The composite disposable for this subscriber.
     * <p>
     * This field is initialized lazily. Use {@link #disposable()} to access it null-safely.
     */
    @Nullable
    private RefreshableDisposableContainer disposable;

    /**
     * Creates a new subscriber.
     */
    @Inject
    public Subscriber() {
    }

    /**
     * Adds a runnable to be executed when the controller is destroyed.
     *
     * @param action the runnable to execute
     */
    public void subscribe(@NotNull Runnable action) {
        disposable().add(Disposable.fromRunnable(action));
    }

    /**
     * Adds a disposable to be disposed when the controller is destroyed.
     *
     * @param disposable the disposable to dispose
     */
    public void subscribe(@NotNull Disposable disposable) {
        disposable().add(disposable);
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     *
     * @param completable the completable to subscribe to
     */
    public void subscribe(@NotNull Completable completable) {
        disposable().add(completable.observeOn(FulibFxApp.FX_SCHEDULER).subscribe());
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     *
     * @param completable the completable to subscribe to
     * @param onComplete  the consumer to call on each event
     */
    public void subscribe(@NotNull Completable completable, @NotNull Action onComplete) {
        disposable().add(completable.observeOn(FulibFxApp.FX_SCHEDULER).subscribe(onComplete));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     *
     * @param completable the completable to subscribe to
     * @param onError     the consumer to call on an error
     */
    public void subscribe(@NotNull Completable completable, @NotNull Consumer<? super @NotNull Throwable> onError) {
        disposable().add(completable.doOnError(onError).observeOn(FulibFxApp.FX_SCHEDULER).subscribe());
    }

    /**
     * Subscribes to an observable on a scheduler and observes it on the FX thread.
     *
     * @param observable  the observable to subscribe to
     * @param onNext      the action to call on each event
     * @param onError     the consumer to call on an error
     * @param subscribeOn the scheduler to subscribe on
     * @param <T>         the type of the items emitted by the Observable
     */
    public <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Scheduler subscribeOn, @NotNull Consumer<@NotNull T> onNext, @NotNull Consumer<? super @NotNull Throwable> onError) {
        disposable().add(observable.subscribeOn(subscribeOn).observeOn(FulibFxApp.FX_SCHEDULER).subscribe(onNext, onError));
    }

    /**
     * Subscribes to and observes an observable on the FX thread.
     *
     * @param observable the observable to subscribe to
     * @param <T>        the type of the items emitted by the Observable
     */
    public <T> void subscribe(@NotNull Observable<@NotNull T> observable) {
        subscribe(observable.ignoreElements());
    }

    /**
     * Subscribes to and observes an observable on the FX thread.
     *
     * @param observable the observable to subscribe to
     * @param onNext     the action to call on completion
     * @param <T>        the type of the items emitted by the Observable
     */
    public <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Consumer<@NotNull T> onNext) {
        disposable().add(observable.observeOn(FulibFxApp.FX_SCHEDULER).subscribe(onNext));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     *
     * @param observable the observable to subscribe to
     * @param onNext     the consumer to call on each event
     * @param onError    the consumer to call on an error
     * @param <T>        the type of the items emitted by the Observable
     */
    public <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Consumer<@NotNull T> onNext, @NotNull Consumer<? super @NotNull Throwable> onError) {
        disposable().add(observable.observeOn(FulibFxApp.FX_SCHEDULER).subscribe(onNext, onError));
    }

    /**
     * Adds a listener to a property and removes it on destroy.
     *
     * @param property the property or observable to listen to
     * @param listener the listener to add
     * @param <T>      the type of the property value
     */
    public <T> void listen(@NotNull ObservableValue<@NotNull T> property, @NotNull ChangeListener<? super @NotNull T> listener) {
        property.addListener(listener);
        subscribe(() -> property.removeListener(listener));
    }

    /**
     * Adds a beans property change listener to a property change support and removes it on destroy.
     *
     * @param support        The property change support (e.g. a model)
     * @param property       The property to listen to
     * @param changeListener The listener to add
     */
    public void listen(@NotNull PropertyChangeSupport support, @NotNull String property, @NotNull PropertyChangeListener changeListener) {
        support.addPropertyChangeListener(property, changeListener);
        subscribe(() -> support.removePropertyChangeListener(property, changeListener));
    }

    /**
     * Adds a beans property change listener to a property change support and removes it on destroy.
     *
     * @param support        The property change support (e.g. a model)
     * @param changeListener The listener to add
     */
    public void listen(@NotNull PropertyChangeSupport support, @NotNull PropertyChangeListener changeListener) {
        support.addPropertyChangeListener(changeListener);
        subscribe(() -> support.removePropertyChangeListener(changeListener));
    }

    /**
     * Binds a property to another property (unidirectional) and unbinds it on destroy.
     *
     * @param property The property to bind
     * @param other    The property to bind to
     * @param <T>      The type of the property
     */
    public <T> void bind(@NotNull Property<@NotNull T> property, @NotNull ObservableValue<@NotNull T> other) {
        property.bind(other);
        subscribe(property::unbind);
    }

    /**
     * Binds a property to another property (bidirectional) and unbinds it on destroy.
     *
     * @param property The property to bind
     * @param other    The property to bind to
     * @param <T>      The type of the property
     */
    public <T> void bindBidirectional(@NotNull Property<@NotNull T> property, @NotNull Property<@NotNull T> other) {
        property.bindBidirectional(other);
        subscribe(() -> property.unbindBidirectional(other));
    }

    /**
     * Returns the internal composite disposable for this subscriber. If the disposable does not exist yet, it will be created.
     *
     * @return The composite disposable for this subscriber
     */
    @NotNull
    private RefreshableDisposableContainer disposable() {
        if (this.disposable == null || this.disposable.isDisposed()) {
            this.disposable = new RefreshableCompositeDisposable();
        }
        return this.disposable;
    }

    @Override
    public boolean isDisposed() {
        if (this.disposable == null) {
            return false;
        }
        return this.disposable.isDisposed();
    }

    @Override
    public void dispose() {
        if (this.disposable != null) {
            this.disposable.dispose();
        }
    }

    @Override
    public boolean refresh() {
        return this.disposable().refresh();
    }

    @Override
    public boolean isFresh() {
        return this.disposable().isFresh();
    }

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #subscribe(Disposable)} instead
     */
    @Deprecated
    @Override
    public boolean add(Disposable d) {
        return this.disposable().add(d);
    }

    /**
     * {@inheritDoc}
     * @deprecated Removing disposables from the composite disposable should not be done manually.
     */
    @Deprecated
    @Override
    public boolean remove(Disposable d) {
        return this.disposable != null && this.disposable.remove(d);
    }

    /**
     * {@inheritDoc}
     * @deprecated Deleting disposables from the composite disposable should not be done manually.
     */
    @Deprecated
    @Override
    public boolean delete(Disposable d) {
        return this.disposable != null && this.disposable.delete(d);
    }
}
