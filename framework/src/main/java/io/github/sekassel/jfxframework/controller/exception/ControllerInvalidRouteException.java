package io.github.sekassel.jfxframework.controller.exception;

public class ControllerInvalidRouteException extends RuntimeException {

    public ControllerInvalidRouteException(String route) {
        super("Controller with route %s couldn't be found".formatted(route));
    }

}
