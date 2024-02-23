package de.uniks.ludo.dagger;

import dagger.Module;
import dagger.Provides;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Module
public class MainModule {

    @Provides
    ResourceBundle bundle() {
        return ResourceBundle.getBundle(
                "de/uniks/ludo/lang",
                Locale.forLanguageTag(preferences().get("language", Locale.getDefault().getLanguage()))
        );
    }

    @Provides
    Preferences preferences() {
        return Preferences.userNodeForPackage(de.uniks.ludo.App.class);
    }

}
