package de.uniks.ludo;

import de.uniks.ludo.controller.GameOverController;
import de.uniks.ludo.controller.IngameController;
import de.uniks.ludo.controller.SetupController;
import org.fulib.fx.annotation.Route;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Inject
    @Route("")
    // The empty route is the default route. It is often used as the starting point of the application.
    public Provider<SetupController> setupController;

    @Inject
    @Route("ingame")
    // Routes can be used to show controllers. Using the route "/ingame" will show the IngameController.
    public Provider<IngameController> ingameController;

    @Inject
    @Route("ingame/gameover")
    public Provider<GameOverController> gameOverController;

    @Inject
    public Routing() {
    }
}
