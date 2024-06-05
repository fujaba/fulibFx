package org.fulib.fx.controller.exception;

/**
 * Thrown when navigating to an invalid route.
 */
public class ControllerInvalidRouteException extends RuntimeException {

    public ControllerInvalidRouteException(String message) {
        super(message);
    }

}
