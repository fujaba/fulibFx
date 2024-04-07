package de.uniks.example.dagger;

import dagger.BindsInstance;
import dagger.Component;
import de.uniks.example.ExampleApp;
import de.uniks.example.Routing;

import javax.inject.Singleton;
import java.util.ResourceBundle;

@Component(modules = {MainModule.class})
@Singleton
public interface MainComponent {

    Routing routes();

    ResourceBundle bundle();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(ExampleApp app);

        MainComponent build();
    }

}
