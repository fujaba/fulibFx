package org.fulib.fx.app.mocking.service;

import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;

public class MyService {

    @Inject
    public MyService() {
    }

    public Observable<String> getObservable() {
        return Observable.just("This is a real string.");
    }

}
