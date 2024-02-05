package org.fulib.fx.app;

import dagger.BindsInstance;
import dagger.Component;
import org.fulib.fx.FulibFxApp;

import javax.inject.Singleton;

@Component
@Singleton
public interface TestComponent {

    TestRouting routes();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(FulibFxApp app);

        TestComponent build();
    }

}
