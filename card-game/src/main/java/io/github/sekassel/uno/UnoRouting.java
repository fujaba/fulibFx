package io.github.sekassel.uno;

import io.github.sekassel.jfxframework.controller.annotation.Providing;
import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.uno.controller.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * This class is used to set up the routing for the application.
 * Every route is mapped to a controller, which is then used to display the corresponding view.
 * <p>
 * The {@link Route} annotation is used to mark a controller as a route.
 * The value of the annotation is the path of the route.
 * <p>
 * The controllers will be created using Providers, which in this case are injected by dagger. Therefore, the controllers
 * need an empty constructor annotated with {@link Inject}.
 * TOOD: Maybe rewrite the Providing part
 */
@Singleton
public class UnoRouting {

    @Inject
    @Route("")
    // The empty route is the default route. It is often used as the starting point of the application.
    public Provider<SetupController> setupController;

    @Inject
    @Route("ingame")
    public Provider<IngameController> ingameController;

    @Inject
    @Route("gameover")
    public Provider<GameOverController> gameOverController;

    @Inject
    @Providing
    public Provider<BotController> botController;

    @Inject
    @Providing
    public Provider<CardController> cardController;


    @Inject
    public UnoRouting() {
    }

}
