package io.github.sekassel.jfxexample.dagger;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxexample.routes.Routing;

import javax.inject.Singleton;

@Component(modules = {MainModule.class})
@Singleton
public interface MainComponent {

    Routing router();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(ExampleApp app);

        MainComponent build();
    }

}
