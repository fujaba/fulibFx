package io.github.sekassel.uno.dagger;

import dagger.Module;
import dagger.Provides;
import io.github.sekassel.uno.App;
import org.fulib.fx.FulibFxApp;

import java.util.Locale;
import java.util.ResourceBundle;

@Module
public class MainModule {
    @Provides
    FulibFxApp app(App app) {
        return app;
    }

    @Provides
    ResourceBundle bundle() {
        return ResourceBundle.getBundle("io/github/sekassel/uno/lang", Locale.GERMAN);
    }

}
