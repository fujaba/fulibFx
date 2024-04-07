package de.uniks.example.dagger;

import dagger.Module;
import dagger.Provides;
import de.uniks.example.ExampleApp;
import org.fulib.fx.FulibFxApp;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Module
public class MainModule {

    @Provides
    FulibFxApp app(ExampleApp app) {
        return app;
    }

    @Provides
    ResourceBundle bundle() {
        return ResourceBundle.getBundle(
                "de/uniks/example/lang/lang",
                Locale.forLanguageTag(preferences().get("language", Locale.getDefault().getLanguage()))
        );
    }

    @Provides
    Preferences preferences() {
        return Preferences.userNodeForPackage(ExampleApp.class);
    }

}
