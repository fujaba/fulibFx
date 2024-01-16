package io.github.sekassel.jfxframework.app;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.jfxframework.FxFramework;

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
