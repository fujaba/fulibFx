package io.github.sekassel.person.dagger;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.person.PersonApp;
import io.github.sekassel.person.routes.Routing;

import javax.inject.Singleton;

@Component(modules = {MainModule.class})
@Singleton
public interface MainComponent {

    Routing router();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(PersonApp app);

        MainComponent build();
    }

}
