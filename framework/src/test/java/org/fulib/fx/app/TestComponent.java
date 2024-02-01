package org.fulib.fx.app;

import dagger.BindsInstance;
import dagger.Component;
import org.fulib.fx.FxFramework;

import javax.inject.Singleton;

@Component
@Singleton
public interface TestComponent {

    TestRouting routes();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(FxFramework app);

        TestComponent build();
    }

}
