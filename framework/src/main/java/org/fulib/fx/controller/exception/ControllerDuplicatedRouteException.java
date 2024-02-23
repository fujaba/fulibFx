package org.fulib.fx.controller.exception;

import static org.fulib.fx.util.FrameworkUtil.error;

public class ControllerDuplicatedRouteException extends RuntimeException {

    public ControllerDuplicatedRouteException(String route, Class<?> oldController, Class<?> newController) {
        super(error(3002).formatted(route, oldController.getName(), newController.getName()));
    }
}
