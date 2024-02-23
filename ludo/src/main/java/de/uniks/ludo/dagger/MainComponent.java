package de.uniks.ludo.dagger;

import dagger.BindsInstance;
import dagger.Component;
import de.uniks.ludo.App;
import de.uniks.ludo.Routing;

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
        Builder mainApp(App app);

        MainComponent build();
    }

}
