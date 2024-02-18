package io.github.sekassel.uno.dagger;

import dagger.Module;
import dagger.Provides;

import java.util.Locale;
import java.util.ResourceBundle;

@Module
public class MainModule {

    @Provides
    ResourceBundle bundle() {
        return ResourceBundle.getBundle("io/github/sekassel/uno/lang", Locale.GERMAN);
    }

}
