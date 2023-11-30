package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.util.Util;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public interface Subscriber {

    HashMap<@NotNull Subscriber, @NotNull CompositeDisposable> disposables = new HashMap<>();

    /**
     * Method called by the framework when the controller using this subscriber is destroyed.
     * Internal use only.
     */
    default void destroy() {
        if (disposables.containsKey(this))
            disposables.get(this).dispose();
        disposables.remove(this);
    }

    /**
     * Adds a runnable to be executed when the controller is destroyed.
     *
     * @param action the runnable to execute
     */
    default void onDestroy(@NotNull Runnable action) {
        getDisposables().add(Disposable.fromRunnable(action));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param completable the completable to subscribe to
     */
    default void subscribe(@NotNull Completable completable) {
        getDisposables().add(completable.observeOn(FxFramework.scheduler()).subscribe());
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param completable the completable to subscribe to
     * @param onComplete  the consumer to call on each event
     */
    default void subscribe(@NotNull Completable completable, @NotNull Action onComplete) {
        getDisposables().add(completable.observeOn(FxFramework.scheduler()).subscribe(onComplete));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param completable the completable to subscribe to
     * @param onError     the consumer to call on an error
     */
    default void subscribe(@NotNull Completable completable, @NotNull Consumer<? super @NotNull Throwable> onError) {
        getDisposables().add(completable.doOnError(onError).observeOn(FxFramework.scheduler()).subscribe());
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
    default <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Scheduler subscribeOn, @NotNull Consumer<@NotNull T> onNext, @NotNull Consumer<? super @NotNull Throwable> onError) {
        getDisposables().add(observable.subscribeOn(subscribeOn).observeOn(FxFramework.scheduler()).subscribe(onNext, onError));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param observable the observable to subscribe to
     * @param <T>        the type of the items emitted by the Observable
     */
    default <T> void subscribe(@NotNull Observable<@NotNull T> observable) {
        subscribe(observable.ignoreElements());
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param observable the observable to subscribe to
     * @param onNext     the action to call on completion
     * @param <T>        the type of the items emitted by the Observable
     */
    default <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Consumer<@NotNull T> onNext) {
        getDisposables().add(observable.observeOn(FxFramework.scheduler()).subscribe(onNext));
    }

    /**
     * Subscribes to and observes a completable on the FX thread.
     * This method is only a utility method to avoid boilerplate code.
     *
     * @param observable the observable to subscribe to
     * @param onNext     the consumer to call on each event
     * @param onError    the consumer to call on an error
     * @param <T>        the type of the items emitted by the Observable
     */
    default <T> void subscribe(@NotNull Observable<@NotNull T> observable, @NotNull Consumer<@NotNull T> onNext, @NotNull Consumer<? super @NotNull Throwable> onError) {
        getDisposables().add(observable.observeOn(FxFramework.scheduler()).subscribe(onNext, onError));
    }

    /**
     * Adds a listener to a property and removes it on destroy.
     *
     * @param property the property or observable to listen to
     * @param listener the listener to add
     * @param <T>      the type of the property value
     */
    default <T> void listen(@NotNull ObservableValue<@NotNull T> property, @NotNull ChangeListener<? super @NotNull T> listener) {
        property.addListener(listener);
        onDestroy(() -> property.removeListener(listener));
    }

    /**
     * Returns the composite disposable for this subscriber. If the disposable does not exist yet, it will be created.
     *
     * @return The composite disposable for this subscriber
     */
    default @NotNull CompositeDisposable getDisposables() {
        return Util.putIfNull(disposables, this, new CompositeDisposable());
    }

}
