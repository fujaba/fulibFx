package org.fulib.fx.dagger;

import dagger.BindsInstance;
import dagger.Component;
import org.fulib.fx.FxFramework;
import org.fulib.fx.controller.AutoRefresher;
import org.fulib.fx.controller.ControllerManager;
import org.fulib.fx.controller.Router;

import javax.inject.Singleton;

@Component()
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
