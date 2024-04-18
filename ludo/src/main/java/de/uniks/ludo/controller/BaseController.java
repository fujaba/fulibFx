package de.uniks.ludo.controller;

import de.uniks.ludo.App;
import de.uniks.ludo.service.GameService;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class BaseController {

    @Inject
    public BaseController() {
    }

    @Inject
    @Resource
    ResourceBundle bundle;

    @Inject
    App app;
    @Inject
    GameService gameService;
    @Inject
    Subscriber subscriber;

    @OnDestroy
    void destroy() {
        subscriber.dispose();
    }

}
