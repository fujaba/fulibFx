package io.github.sekassel.todo.dagger;

import dagger.BindsInstance;
import dagger.Component;
import io.github.sekassel.todo.TodoApp;
import io.github.sekassel.todo.routes.Routing;

import javax.inject.Singleton;

@Component(modules = {MainModule.class})
@Singleton
public interface MainComponent {

    Routing router();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder mainApp(TodoApp app);

        MainComponent build();
    }

}
