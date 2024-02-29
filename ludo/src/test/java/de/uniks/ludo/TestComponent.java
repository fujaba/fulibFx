package de.uniks.ludo;

import dagger.Component;
import de.uniks.ludo.dagger.MainComponent;
import de.uniks.ludo.dagger.MainModule;

import javax.inject.Singleton;

@Component(modules = {MainModule.class, TestModule.class})
@Singleton
public interface TestComponent extends MainComponent {

    @Component.Builder
    interface Builder extends MainComponent.Builder {
        TestComponent build();
    }
}