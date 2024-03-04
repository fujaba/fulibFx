package de.uniks.ludo.dagger;

import dagger.Module;
import dagger.Provides;
import de.uniks.ludo.App;
import org.fulib.fx.FulibFxApp;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Module
public class MainModule {

    @Provides
    FulibFxApp app(App app) {
        return app;
    }

    @Provides
    ResourceBundle bundle() {
        return ResourceBundle.getBundle(
                "de/uniks/ludo/lang/lang",
                Locale.forLanguageTag(preferences().get("language", Locale.getDefault().getLanguage()))
        );
    }

    @Provides
    Preferences preferences() {
        return Preferences.userNodeForPackage(de.uniks.ludo.App.class);
    }

}
