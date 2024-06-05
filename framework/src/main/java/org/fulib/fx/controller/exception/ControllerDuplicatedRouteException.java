package org.fulib.fx.controller.exception;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * Exception thrown if a route has been added to the router twice.
 */
public class ControllerDuplicatedRouteException extends RuntimeException {

    public ControllerDuplicatedRouteException(String route, Class<?> oldController, Class<?> newController) {
        super(error(3002).formatted(route, oldController.getName(), newController.getName()));
    }
}
