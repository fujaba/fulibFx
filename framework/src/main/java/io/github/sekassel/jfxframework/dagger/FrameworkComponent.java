package io.github.sekassel.jfxframework.dagger;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.AutoRefresher;
import io.github.sekassel.jfxframework.controller.ControllerManager;
import io.github.sekassel.jfxframework.controller.Router;

import javax.inject.Singleton;

@Component(modules = {ControllerModule.class})
@Singleton
public interface FrameworkComponent {

    @Singleton
    Router router();

    @Singleton
    FxFramework framework();

    @Singleton
    AutoRefresher autoRefresher();

    @Singleton
    ControllerManager controllerManager();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder framework(FxFramework framework);

        FrameworkComponent build();
    }

}
