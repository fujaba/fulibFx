package io.github.sekassel.uno.dagger;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.uno.App;
import io.github.sekassel.uno.UnoRouting;

import javax.inject.Singleton;
import java.util.ResourceBundle;

@Component(modules = {MainModule.class})
@Singleton
public interface MainComponent {

    UnoRouting routes();

    ResourceBundle bundle();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(App app);

        MainComponent build();
    }

}
